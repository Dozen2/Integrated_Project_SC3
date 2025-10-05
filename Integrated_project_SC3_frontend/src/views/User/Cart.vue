<script setup>
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useCartStore } from "@/stores/cartStore";
import { createOrder, fetchSellers, getImageByImageName } from "@/libs/callAPI/apiSaleItem";

const imagesMap = ref({});
const address = ref("");
const note = ref("");
const description = ref("")
// -------------------- store --------------------
const auth = useAuthStore();
const cartStore = useCartStore();

// -------------------- reactive --------------------
// const sellerMap = ref({});

// ทำ computed เพื่อให้ template reactive สวย ๆ
const cartItems = computed(() => cartStore.cart);

// -------------------- ฟังก์ชันเพิ่ม / ลดสินค้า --------------------
const increment = (item) => {
     cartStore.updateQuantity(item.id, item.sellerId, item.quantity + 1);
};

const decrement = (item) => {
     if (item.quantity - 1 <= 0) {
          cartStore.removeFromCart(item.id, item.sellerId);
     } else {
          cartStore.updateQuantity(item.id, item.sellerId, item.quantity - 1);
     }
};

// -------------------- select item --------------------
const selectedItems = ref([]);
const selectedSellers = ref([]);

// toggle select all
const isAllSelected = computed(() => {
     return cartItems.value.length > 0 &&
          selectedItems.value.length === cartItems.value.length;
});
const toggleSelectAll = () => {
     if (isAllSelected.value) {
          selectedItems.value = [];
     } else {
          selectedItems.value = cartItems.value.map(it => it.id + "-" + it.sellerId);
     }
};
// ✅ รวมเฉพาะสินค้าที่ถูกเลือก
const selectedSummary = computed(() => {
     let totalQty = 0;
     let totalPrice = 0;

     for (const item of cartItems.value) {
          const key = item.id + "-" + item.sellerId;
          if (selectedItems.value.includes(key)) {
               totalQty += item.quantity;
               totalPrice += item.price * item.quantity;
          }
     }
     return { totalQty, totalPrice };
});

// toggle seller checkbox
const toggleSeller = (sellerId) => {
     const items = groupedCart.value[sellerId] || [];

     if (selectedSellers.value.includes(sellerId)) {
          // ถ้าเลือกอยู่ → ยกเลิกทั้งหมดของ seller นี้
          selectedSellers.value = selectedSellers.value.filter(id => id !== sellerId);
          selectedItems.value = selectedItems.value.filter(key => {
               return !items.some(it => key === it.id + "-" + it.sellerId);
          });
     } else {
          // ถ้ายังไม่ได้เลือก → เลือกทั้งหมดของ seller นี้
          selectedSellers.value.push(sellerId);
          for (const it of items) {
               const key = it.id + "-" + it.sellerId;
               if (!selectedItems.value.includes(key)) {
                    selectedItems.value.push(key);
               }
          }
     }
};
const isSellerSelected = (sellerId) => {
     const items = groupedCart.value[sellerId] || [];
     if (items.length === 0) return false;
     return items.every(it => selectedItems.value.includes(it.id + "-" + it.sellerId));
};
// -------------------- mock seller --------------------
const sellerMap = ref({});
// const sellerMap = ref({
//      1: "Somsuan",
//      2: "Somsuk",
//      3: "Somsak"
// });

const groupedCart = computed(() => {
     const groups = {};
     for (const item of cartItems.value) {
          if (!groups[item.sellerId]) {
               groups[item.sellerId] = [];
          }
          groups[item.sellerId].push(item);
     }
     return groups;
});

// -------------------- order description --------------------
const getDescription = (item) => {
     return `${item.brandName} ${item.model} (${item.storageGb}GB, ${item.color})`;
};


// -------------------- order --------------------
const PlaceOrder = () => {
     if (selectedItems.value.length === 0) {
          alert("กรุณาเลือกสินค้าอย่างน้อย 1 รายการ");
          return;
     }

     // หา sellerId ของสินค้าที่ถูกเลือก
     const sellerIds = [...new Set(
          selectedItems.value.map(key => key.split("-")[1])
     )];

     const orders = [];

     for (const sellerId of sellerIds) {
          const itemsOfSeller = groupedCart.value[sellerId].filter(item =>
               selectedItems.value.includes(item.id + "-" + item.sellerId)
          );

          if (itemsOfSeller.length === 0) continue;

          const orderItems = itemsOfSeller.map((item, idx) => ({
               no: idx + 1,
               saleItemId: item.id,
               price: item.price,
               quantity: item.quantity,
               description: getDescription(item),
               mainImageFileName: item.images?.length ? item.images[0].fileName : null
          }));

          const order = {
               id: Math.floor(Math.random() * 100000),
               buyerId: auth.user?.id || 6,
               sellerDTO: {
                    id: sellerId,
                    userName: sellerMap.value[sellerId] || "Unknown Seller"
               },
               orderDate: new Date().toISOString(),
               shippingAddress: address.value,
               orderNote: note.value,
               orderItems
          };

          orders.push(order);
     }

     console.log("📦 Orders Created:", orders);
};



// -------------------- onMounted --------------------
onMounted(async () => {
     cartStore.loadCart();
     //get seller
     const sellerIds = [...new Set(cartStore.cart.map(item => item.sellerId))];
     const sellersData = await fetchSellers(sellerIds);
     sellersData.forEach(s => {
          sellerMap.value = {
               ...sellerMap.value,
               [s.id]: s.userName
          };
     });

     cartStore.updateQuantity();
     //img
     console.log(cartStore.cart);
     for (const img of cartStore.cart) {
          const sorted = [...img.images].sort(
               (a, b) => a.imageViewOrder - b.imageViewOrder
          );
          const urls = await Promise.all(
               sorted.map(img => getImageByImageName(img.fileName))
          )
          imagesMap.value[img.id] = urls;
     }
     console.log(imagesMap.value);


});
</script>

<template>
     <div class="p-4">
          <h1 class="text-xl font-bold mb-4">🛒 Shopping cart</h1>

          <!-- ถ้าไม่มีสินค้า -->
          <div v-if="cartItems.length === 0" class="text-gray-500">
               ไม่มีสินค้าในตะกร้า
          </div>

          <!-- รายการสินค้า -->
          <div v-else class="space-y-4">

               <!-- ✅ ปุ่มเลือกทั้งหมด -->
               <div class="flex items-center gap-2 mb-2">
                    <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
                    <label>เลือกทั้งหมด</label>
               </div>

               <div v-for="(items, sellerId) in groupedCart" :key="sellerId" class="mb-6">

                    <!-- ✅ Checkbox Seller -->
                    <div class="flex items-center gap-2 mb-2">
                         <input type="checkbox" :checked="isSellerSelected(sellerId)"
                              @change="toggleSeller(sellerId)" />
                         <label class="font-bold">🏪 {{ sellerMap[Number(sellerId)] || "Unknown Seller" }}</label>
                    </div>

                    <!-- รายการสินค้า -->
                    <div v-for="item in items" :key="item.id + '-' + item.sellerId"
                         class="flex items-center justify-between border p-3 rounded mb-2">

                         <div class="flex items-center gap-3">
                              <input type="checkbox" :value="item.id + '-' + item.sellerId" v-model="selectedItems" />

                              <div v-if="item.images && item.images.length > 0" class="flex gap-1">
                                   <img v-for="(img, idx) in imagesMap[item.id]" :key="idx" :src="img"
                                        alt="Product Image" class="w-16 h-16 object-cover rounded" />
                              </div>

                              <div>
                                   <p class="font-semibold">
                                        {{ getDescription(item) }}
                                   </p>
                                   <p class="text-sm text-gray-500">฿{{ item.price }}</p>
                              </div>
                         </div>

                         <div class="flex items-center gap-2">
                              <button @click="decrement(item)" class="px-2 py-1 bg-gray-200 rounded">-</button>
                              <span>{{ item.quantity }}</span>
                              <button @click="increment(item)" class="px-2 py-1 bg-gray-200 rounded">+</button>
                         </div>

                         <div class="font-bold text-green-600">
                              ฿{{ item.price * item.quantity }}
                         </div>
                    </div>
               </div>


               <div>
                    <h1>Cart summary</h1>
                    <h2>ship to</h2>
                    <h3>address</h3>
                    <input type="text" v-model="address" placeholder="กรอกที่อยู่">
                    <h3>note</h3>
                    <input type="text" v-model="note" placeholder="หมายเหตุ">

                    <!-- ✅ Summary เฉพาะที่เลือก -->
                    <div class="border-t pt-4 text-right">
                         <p>จำนวนสินค้าที่เลือก: {{ selectedSummary.totalQty }} ชิ้น</p>
                         <p class="font-bold">
                              ยอดที่ต้องชำระ: ฿{{ selectedSummary.totalPrice }}
                         </p>
                    </div>
               </div>
          </div>

          <div>
               <button @click="PlaceOrder()">
                    Place Order
               </button>
          </div>
     </div>
</template>

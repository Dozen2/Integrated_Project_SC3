<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useCartStore } from "@/stores/cartStore";
import { createOrder, fetchSellers, getImageByImageName } from "@/libs/callAPI/apiSaleItem";
import { useAlertStore } from "@/stores/alertStore";
import { nullCatching, unitPrice } from "@/libs/utils.js";


const imagesMap = ref({});
const address = ref([]);
const selectedAddress = ref("");
const newAddress = ref("");
const note = ref("");
const showConfirmModal = ref(false)
const confirmMessage = ref('')
let confirmAction = null;
// -------------------- store --------------------
const auth = useAuthStore();
const cartStore = useCartStore();
const alertStore = useAlertStore()
const placeholder = "https://cdn-icons-png.freepik.com/512/9280/9280762.png";

// -------------------- reactive --------------------
// const sellerMap = ref({});

// ทำ computed เพื่อให้ template reactive สวย ๆ
const cartItems = computed(() => cartStore.cart);

// -------------------- model comfrim ------------------------
const openConfirmModal = (message, onConfirm) => {
     confirmMessage.value = message;
     confirmAction = onConfirm;
     showConfirmModal.value = true;
};

const confirmYes = () => {
     if (confirmAction) confirmAction();
     showConfirmModal.value = false;
};

const confirmNo = () => {
     showConfirmModal.value = false;
};

// -------------------- ฟังก์ชันเพิ่ม / ลดสินค้า --------------------
const increment = (item) => {
     cartStore.updateQuantity(item.id, item.sellerId, item.quantity + 1);
};

const decrement = (item) => {
     if (item.quantity - 1 <= 0) {
          openConfirmModal(`คุณต้องการลบ "${getDescription(item)}" ออกจากตะกร้าหรือไม่?`, () => {
               cartStore.removeFromCart(item.id, item.sellerId);
               alertStore.addToast("ลบสินค้าที่เลือกเรียบร้อยแล้ว", "Delete Selected", "success")
          })
     } else {
          cartStore.updateQuantity(item.id, item.sellerId, item.quantity - 1);
     }
};

const deleteselected = () => {
     if (selectedItems.value.length === 0) {
          alertStore.addToast("กรุณาเลือกสินค้าที่ต้องการลบ", "Delete Selected", "warning")
     }

     openConfirmModal("คุณต้องการลบสินค้าที่เลือกทั้งหมดออกจากตะกร้าหรือไม่?", () => {
          selectedItems.value.forEach(key => {
               const [id, sellerId] = key.split("-")
               cartStore.removeFromCart(id, sellerId)
          })

          selectedItems.value = []
          selectedSellers.value = []
          alertStore.addToast("ลบสินค้าที่เลือกเรียบร้อยแล้ว", "Delete Selected", "success")
     })

}


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
     const allSelected = items.every(it => selectedItems.value.includes(it.id + "-" + it.sellerId));

     if (allSelected) {
          // ยกเลิกเลือกทั้งหมดของ seller นี้
          selectedItems.value = selectedItems.value.filter(
               key => !items.some(it => key === it.id + "-" + it.sellerId)
          );
          selectedSellers.value = selectedSellers.value.filter(id => id !== sellerId);
     } else {
          // เลือกทั้งหมดของ seller นี้
          for (const it of items) {
               const key = it.id + "-" + it.sellerId;
               if (!selectedItems.value.includes(key)) {
                    selectedItems.value.push(key);
               }
          }
          if (!selectedSellers.value.includes(sellerId)) {
               selectedSellers.value.push(sellerId);
          }
     }
};
watch(selectedItems, () => {
     const sellerIds = Object.keys(groupedCart.value);

     for (const sellerId of sellerIds) {
          const items = groupedCart.value[sellerId] || [];
          const allSelected = items.length > 0 && items.every(it => selectedItems.value.includes(it.id + "-" + it.sellerId));

          if (allSelected) {
               if (!selectedSellers.value.includes(sellerId)) {
                    selectedSellers.value.push(sellerId);
               }
          } else {
               selectedSellers.value = selectedSellers.value.filter(id => id !== sellerId);
          }
     }
});

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
// -------------------- varidate placeorder --------------------
const isValid = computed(() => {
     return (
          selectedItems.value.length > 0 &&
          selectedAddress.value.trim() !== ""
     )
})
watch(
     () => cartStore.cart,
     (newCart) => {
          if (newCart.length === 0) {
               selectedItems.value = [];
               selectedSellers.value = [];
               // address.value = ""
          } else {
               // ลบ item ที่ไม่อยู่ใน cart ออก
               selectedItems.value = selectedItems.value.filter(key =>
                    newCart.some(item => key === item.id + "-" + item.sellerId)
               );
          }
     },
     { deep: true }
);

// -------------------- order --------------------
const PlaceOrder = async () => {
     // if (selectedItems.value.length === 0) {
     //      alert("กรุณาเลือกสินค้าอย่างน้อย 1 รายการ");
     //      return;
     // }
     const buyerId = auth.getAuthData().id
     console.log(buyerId);


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
               buyerId: buyerId,
               sellerId: sellerId,
               orderDate: new Date().toISOString(),
               shippingAddress: selectedAddress.value,
               orderNote: note.value,
               orderItems,
               orderStatus: "Cancelled"
          };

          orders.push(order);
     }

     console.log("📦 Orders Created:", orders);
     console.log(selectedItems.value);
     console.log(selectedSellers.value);



     const result = await createOrder(orders);
     if (result) {
          // alert("สั่งซื้อเรียบร้อยแล้ว!");
          alertStore.addToast("สั่งซื้อเรียบร้อยแล้ว", "PlaceOrder", "success");
          // cartStore.clearCart(); // ล้างตะกร้า
          selectedItems.value.forEach(key => {
               const [id, sellerId] = key.split("-")
               cartStore.removeFromCart(id, sellerId)
          })
          selectedItems.value = [];
          selectedSellers.value = [];
          // address.value = ""
     }
};
//-------------------- addess -----------------------
const getAddressKey = (userId) => `address_${userId}`

const isLoadingAddress = ref(false);

watch(selectedAddress, (val) => {
     console.log("Selected address:", val);
     // บันทึก selectedAddress ที่เลือกล่าสุด
     const user = auth.getAuthData();
     if (user && user.id && val) {
          localStorage.setItem(`selectedAddress_${user.id}`, val);
     }
});

watch(address, () => {
     if (isLoadingAddress.value) return;
     saveAddresses();
}, { deep: true });

// เพิ่ม address ใหม่
const addAddress = () => {
     const trimmed = newAddress.value.trim();
     if (trimmed === "") return;

     // ถ้ายังไม่มี address นี้ใน list
     if (!address.value.includes(trimmed)) {
          address.value.push(trimmed);
          // saveAddresses();
     }
     newAddress.value = "";
     selectedAddress.value = trimmed; // auto select
};

// บันทึก address ลง localStorage
const saveAddresses = () => {
     const user = auth.getAuthData();
     if (user && user.id) {
          localStorage.setItem(getAddressKey(user.id), JSON.stringify(address.value));
     }
};


// -------------------- onMounted --------------------
onMounted(async () => {
     cartStore.loadCart();

     //addess in localstroage
     const user = auth.getAuthData();
     console.log(user.id);


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
     try {
          for (const item of cartStore.cart) {
               if (!item.images || item.images.length === 0) {
                    imagesMap.value[item.id] = [placeholder];
               } else {
                    const sorted = [...item.images].sort(
                         (a, b) => a.imageViewOrder - b.imageViewOrder
                    );

                    const urls = await Promise.all(
                         sorted.map(async (img) => {
                              try {
                                   const url = await getImageByImageName(img.fileName);
                                   return url || placeholder;
                              } catch (e) {
                                   console.error("โหลดรูปไม่ได้:", img.fileName, e);
                                   return placeholder; // ใช้ placeholder แทน
                              }
                         })
                    );

                    imagesMap.value[item.id] = urls;
               }
          }
     } catch (e) {
          console.error("เกิดข้อผิดพลาดขณะโหลดรูปทั้งหมด:", e);
     }

     console.log(imagesMap.value);


     if (user && user.id) {
          isLoadingAddress.value = true;
          const savedAddress = localStorage.getItem(getAddressKey(user.id))
          console.log(savedAddress);

          if (savedAddress) {
               address.value = JSON.parse(savedAddress);

               // โหลด selectedAddress ที่บันทึกไว้ล่าสุด
               const lastSelected = localStorage.getItem(`selectedAddress_${user.id}`);

               if (lastSelected && address.value.includes(lastSelected)) {
                    // ถ้ามีและยังอยู่ใน array → ใช้ตัวนั้น
                    selectedAddress.value = lastSelected;
               } else {
                    // ถ้าไม่มีหรือถูกลบไปแล้ว → ใช้ตัวแรก
                    selectedAddress.value = address.value[0] || "";
               }
          }
          isLoadingAddress.value = false;
     }
});
</script>

<template>
     <div class="p-6 bg-gradient-to-br from-blue-50 via-white to-blue-100 min-h-screen">
          <h1 class="text-3xl font-extrabold mb-8 text-blue-700 drop-shadow-md">
               Shopping Cart
          </h1>

          <!-- Layout 2 คอลัมน์ -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

               <!-- ✅ ฝั่งซ้าย: สินค้า -->
               <div class="lg:col-span-2 space-y-8">
                    <!-- ถ้าไม่มีสินค้า -->
                    <div v-if="cartItems.length === 0"
                         class="text-gray-400 text-center py-16 border-2 border-dashed rounded-2xl bg-white/50 backdrop-blur-sm shadow-inner">
                         ไม่มีสินค้าในตะกร้า
                    </div>

                    <!-- ✅ Confirm Modal -->
                    <div v-if="showConfirmModal"
                         class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">
                         <div class="bg-white p-6 rounded-2xl shadow-2xl w-[90%] max-w-sm text-center">
                              <h2 class="text-lg font-semibold text-gray-800 mb-4">ยืนยันการลบ</h2>
                              <p class="text-gray-600 mb-6">{{ confirmMessage }}</p>
                              <div class="flex justify-center gap-4">
                                   <button @click="confirmYes"
                                        class="bg-red-500 text-white px-5 py-2 rounded-lg hover:bg-red-600 transition">
                                        ยืนยัน
                                   </button>
                                   <button @click="confirmNo"
                                        class="bg-gray-300 text-gray-800 px-5 py-2 rounded-lg hover:bg-gray-400 transition">
                                        ยกเลิก
                                   </button>
                              </div>
                         </div>
                    </div>


                    <!-- รายการสินค้า -->
                    <div v-else>
                         <!-- ปุ่มเลือกทั้งหมด -->
                         <div class="flex items-center gap-2 mb-6 text-blue-700">
                              <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll"
                                   class="itbms-select-all w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400" />
                              <label class="font-semibold text-lg">Select All</label>
                         </div>

                         <button @click="deleteselected" :disabled="selectedItems.length === 0"
                              class="px-4 py-2 rounded-lg shadow-md transition text-white font-medium bg-red-500 hover:bg-red-600 disabled:bg-gray-300 disabled:text-gray-500 disabled:cursor-not-allowed">
                              🗑️ Delete Selected
                         </button>

                         <!-- Seller Group -->
                         <div v-for="(items, sellerId) in groupedCart" :key="sellerId" class="itbms-row mb-10">
                              <!-- Checkbox Seller -->
                              <div class="flex items-center gap-2 mb-4">
                                   <input type="checkbox" :checked="isSellerSelected(sellerId)"
                                        @change="toggleSeller(sellerId)"
                                        class="itbms-select-nickname w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400" />
                                   <label class="itbms-nickname font-bold text-xl text-blue-800 drop-shadow-sm">
                                        {{ sellerMap[Number(sellerId)] || "Unknown Seller" }}
                                   </label>
                              </div>

                              <!-- รายการสินค้า -->
                              <div v-for="item in items" :key="item.id + '-' + item.sellerId"
                                   class="itbms-item-row group flex items-center justify-between bg-white/80 backdrop-blur-md border border-blue-100 p-5 rounded-2xl mb-5 shadow-md hover:shadow-2xl hover:scale-[1.02] transition-all duration-300 ease-in-out">

                                   <!-- ✅ Product Info -->
                                   <div class="flex items-center gap-4 flex-1">
                                        <input type="checkbox" :value="item.id + '-' + item.sellerId"
                                             v-model="selectedItems"
                                             class="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400" />

                                        <div v-if="item.images && item.images.length > 0"
                                             class="relative flex-shrink-0 overflow-hidden rounded-xl shadow-md group-hover:scale-105 transition-transform duration-300">
                                             <img :src="imagesMap[item.id]?.[0] || placeholder" alt="Product Image"
                                                  class="w-20 h-20 object-cover rounded-xl" />
                                        </div>

                                        <div>
                                             <p
                                                  class="itbms-item-description font-semibold text-gray-900 text-lg group-hover:text-blue-700 transition">
                                                  {{ getDescription(item) }}
                                             </p>
                                             <p class="text-sm text-gray-500">฿{{ unitPrice(item.price) }}</p>
                                        </div>
                                   </div>

                                   <!-- ✅ Quantity -->
                                   <div class="flex items-center gap-3">
                                        <button @click="decrement(item)"
                                             class="itbms-dec-qty-button w-8 h-8 flex items-center justify-center rounded-full bg-white border border-gray-300 text-blue-600 font-bold hover:bg-blue-100 hover:scale-110 transition">
                                             -
                                        </button>

                                        <span class="itbms-item-quantity w-6 text-center font-medium">{{
                                             item.quantity }}</span>

                                        <button @click="increment(item)"
                                             class="itbms-inc-qty-button w-8 h-8 flex items-center justify-center rounded-full bg-blue-500 text-white font-bold hover:bg-blue-600 hover:scale-110 transition">
                                             +
                                        </button>
                                   </div>

                                   <!-- ✅ Price -->
                                   <div class="min-w-[120px] text-right">
                                        <span
                                             class="itbms-item-total-price inline-block bg-gradient-to-r from-blue-100 to-blue-200 text-blue-800 font-bold px-4 py-2 rounded-xl shadow-sm">
                                             ฿{{ unitPrice(item.price * item.quantity) }}
                                        </span>
                                   </div>
                              </div>
                         </div>
                    </div>
               </div>

               <!-- ✅ ฝั่งขวา: Cart Summary -->
               <div class="bg-white border border-blue-100 rounded-xl p-6 shadow-xl h-fit sticky top-6">
                    <h1 class="text-lg font-semibold mb-4 text-gray-700 border-b pb-2">Cart Summary</h1>

                    <!-- ที่อยู่ -->
                    <div class="mb-4">

                         <!-- Dropdown เลือก address -->
                         <select v-model="selectedAddress"
                              class="w-full border rounded-md p-2 mb-2 focus:ring focus:ring-blue-200 focus:border-blue-400">
                              <option disabled value="">-- เลือกที่อยู่ --</option>
                              <option v-for="(addr, index) in address" :key="index" :value="addr">
                                   {{ addr }}
                              </option>
                         </select>

                         <!-- Input เพิ่ม address ใหม่ -->
                         <input type="text" v-model="newAddress" placeholder="เพิ่มที่อยู่ใหม่"
                              class="w-full border rounded-md p-2 mb-2 focus:ring focus:ring-blue-200 focus:border-blue-400" />

                         <!-- ปุ่มเพิ่ม address -->
                         <button @click="addAddress" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
                              เพิ่มที่อยู่
                         </button>

                    </div>

                    <!-- Note -->
                    <div class="mb-4">
                         <label class="block text-sm font-medium text-gray-600 mb-1">Note</label>
                         <input type="text" v-model="note" placeholder="หมายเหตุ"
                              class="itbms-order-note w-full border rounded-md p-2 focus:ring focus:ring-blue-200 focus:border-blue-400" />
                    </div>

                    <!-- Summary -->
                    <div class="border-t pt-4 text-gray-700 space-y-2">
                         <p>Total items: <span class="itbms-total-order-items font-medium">{{
                              selectedSummary.totalQty }}</span> ชิ้น
                         </p>

                         <p class="itbms-total-total-price font-bold text-xl text-blue-700">Total price:
                              {{ unitPrice(selectedSummary.totalPrice) }}
                         </p>
                    </div>

                    <!-- ปุ่มสั่งซื้อ -->
                    <div class="mt-6">
                         <button @click="PlaceOrder()" :disabled="!isValid"
                              class="itbms-place-order-button w-full px-6 py-3 text-white font-bold rounded-lg shadow-lg transition"
                              :class="isValid
                                   ? 'bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 hover:scale-[1.02]'
                                   : 'bg-blue-300 cursor-not-allowed opacity-60'">
                              Place Order
                         </button>
                    </div>

               </div>
          </div>
     </div>
</template>

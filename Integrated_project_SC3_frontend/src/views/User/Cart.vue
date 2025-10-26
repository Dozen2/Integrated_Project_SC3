<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useCartStore } from "@/stores/cartStore";
import { createOrder, fetchSellers, getImageByImageName } from "@/libs/callAPI/apiSaleItem";
import { useAlertStore } from "@/stores/alertStore";
import { nullCatching, unitPrice } from "@/libs/utils.js";
import ConfirmDelete from "@/components/Common/ConfirmDelete.vue";
import { Store } from "lucide-vue-next";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";

const imagesMap = ref({});
const address = ref([]);
const selectedAddress = ref("");
const newAddress = ref("");
const note = ref("");
const showConfirmModal = ref(false);
const confirmMessage = ref("");
let confirmAction = null;
// -------------------- store --------------------
const auth = useAuthStore();
const cartStore = useCartStore();
const alertStore = useAlertStore();
const placeholder = "https://cdn-icons-png.freepik.com/512/9280/9280762.png";

// -------------------- reactive --------------------
// computed properties for reactive template binding
const cartItems = computed(() => cartStore.cart);

// -------------------- modal confirm ------------------------
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

// -------------------- Increase / decrease item functions --------------------
const increment = (item) => {
  cartStore.updateQuantity(item.id, item.sellerId, item.quantity + 1);
};

const decrement = (item) => {
  if (item.quantity - 1 <= 0) {
    openConfirmModal(`Do you want to remove "${getDescription(item)}" from your cart?`, () => {
      cartStore.removeFromCart(item.id, item.sellerId);
      alertStore.addToast("Item removed successfully", "Delete Selected", "success");
    });
  } else {
    cartStore.updateQuantity(item.id, item.sellerId, item.quantity - 1);
  }
};

// Function for the new 'x' remove button, adapted from reference
const removeFromCartConfirmed = (item) => {
  openConfirmModal(`Do you want to remove "${getDescription(item)}" from your cart?`, () => {
    cartStore.removeFromCart(item.id, item.sellerId);
    alertStore.addToast("Item removed successfully", "Delete Item", "success");
  });
};

const deleteselected = () => {
  if (selectedItems.value.length === 0) {
    alertStore.addToast("Please select items to delete", "Delete Selected", "warning");
    return; // prevent opening confirm when nothing selected
  }

  openConfirmModal("Do you want to remove all selected items from your cart?", () => {
    selectedItems.value.forEach((key) => {
      const [id, sellerId] = key.split("-");
      cartStore.removeFromCart(id, sellerId);
    });

    selectedItems.value = [];
    selectedSellers.value = [];
    alertStore.addToast("Selected items removed successfully", "Delete Selected", "success");
  });
};

// -------------------- select item --------------------
const selectedItems = ref([]);
const selectedSellers = ref([]);

// toggle select all
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length;
});
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = [];
  } else {
    selectedItems.value = cartItems.value.map((it) => it.id + "-" + it.sellerId);
  }
};
// ✅ Summary for selected items
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
  const allSelected = items.every((it) => selectedItems.value.includes(it.id + "-" + it.sellerId));

  if (allSelected) {
    // Unselect all items of this seller
    selectedItems.value = selectedItems.value.filter((key) => !items.some((it) => key === it.id + "-" + it.sellerId));
    selectedSellers.value = selectedSellers.value.filter((id) => id !== sellerId);
  } else {
    // Select all items of this seller
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
    const allSelected = items.length > 0 && items.every((it) => selectedItems.value.includes(it.id + "-" + it.sellerId));

    if (allSelected) {
      if (!selectedSellers.value.includes(sellerId)) {
        selectedSellers.value.push(sellerId);
      }
    } else {
      selectedSellers.value = selectedSellers.value.filter((id) => id !== sellerId);
    }
  }
});

const isSellerSelected = (sellerId) => {
  const items = groupedCart.value[sellerId] || [];
  if (items.length === 0) return false;
  return items.every((it) => selectedItems.value.includes(it.id + "-" + it.sellerId));
};
// -------------------- mock seller --------------------
const sellerMap = ref({});

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
  return selectedItems.value.length > 0 && selectedAddress.value.trim() !== "";
});
watch(
  () => cartStore.cart,
  (newCart) => {
    if (newCart.length === 0) {
      selectedItems.value = [];
      selectedSellers.value = [];
    } else {
      // remove items that are no longer in cart
      selectedItems.value = selectedItems.value.filter((key) => newCart.some((item) => key === item.id + "-" + item.sellerId));
    }
  },
  { deep: true }
);

// -------------------- order --------------------
const PlaceOrder = async () => {
  const buyerId = auth.getAuthData().id;
  // console.log(buyerId);

  // find sellerIds of selected items
  const sellerIds = [...new Set(selectedItems.value.map((key) => key.split("-")[1]))];

  const orders = [];

  for (const sellerId of sellerIds) {
    const itemsOfSeller = groupedCart.value[sellerId].filter((item) => selectedItems.value.includes(item.id + "-" + item.sellerId));

    if (itemsOfSeller.length === 0) continue;

    const orderItems = itemsOfSeller.map((item, idx) => ({
      no: idx + 1,
      saleItemId: item.id,
      price: item.price,
      quantity: item.quantity,
      description: getDescription(item),
      mainImageFileName: item.images?.length ? item.images[0].fileName : null,
    }));

    const order = {
      buyerId: buyerId,
      sellerId: sellerId,
      orderDate: new Date().toISOString(),
      shippingAddress: selectedAddress.value,
      orderNote: note.value,
      orderItems,
      orderStatus: "new_complete",
    };

    orders.push(order);
  }

  // console.log("📦 Orders Created:", orders);
  // console.log(selectedItems.value);
  // console.log(selectedSellers.value);

  const result = await createOrder(orders);
  if (result) {
    alertStore.addToast("Order placed successfully", "PlaceOrder", "success");
    selectedItems.value.forEach((key) => {
      const [id, sellerId] = key.split("-");
      cartStore.removeFromCart(id, sellerId);
    });
    selectedItems.value = [];
    selectedSellers.value = [];
  }
};

//-------------------- address -----------------------
const getAddressKey = (userId) => `address_${userId}`;

const isLoadingAddress = ref(false);

  watch(selectedAddress, (val) => {
  // console.log("Selected address:", val);
  // save last selectedAddress
  const user = auth.getAuthData();
  if (user && user.id && val) {
    localStorage.setItem(`selectedAddress_${user.id}`, val);
  }
});

watch(
  address,
  () => {
    if (isLoadingAddress.value) return;
    saveAddresses();
  },
  { deep: true }
);

// add new address
const addAddress = () => {
  const trimmed = newAddress.value.trim();
  if (trimmed === "") return;

  // if address not already in the list
  if (!address.value.includes(trimmed)) {
    address.value.push(trimmed);
  }
  newAddress.value = "";
  selectedAddress.value = trimmed; // auto select
};

// save addresses to localStorage
const saveAddresses = () => {
  const user = auth.getAuthData();
  if (user && user.id) {
    localStorage.setItem(getAddressKey(user.id), JSON.stringify(address.value));
  }
};

// -------------------- onMounted --------------------
  onMounted(async () => {
  cartStore.loadCart();

  //address in localstorage
  const user = auth.getAuthData();
  // console.log(user.id);

  //get seller
  const sellerIds = [...new Set(cartStore.cart.map((item) => item.sellerId))];
  const sellersData = await fetchSellers(sellerIds);
  sellersData.forEach((s) => {
    sellerMap.value = {
      ...sellerMap.value,
      [s.id]: s.userName,
    };
  });

  cartStore.updateQuantity();

  //img
  try {
    for (const item of cartStore.cart) {
      if (!item.images || item.images.length === 0) {
        imagesMap.value[item.id] = [placeholder];
      } else {
        const sorted = [...item.images].sort((a, b) => a.imageViewOrder - b.imageViewOrder);

        const urls = await Promise.all(
          sorted.map(async (img) => {
            if (img.fileName != null) {
              const url = await getImageByImageName(img.fileName);
              return url || placeholder;
            } else {
              return placeholder;
            }
          })
        );

        imagesMap.value[item.id] = urls;
      }
    }
  } catch (e) {
    console.error("Something wrong when loading images:", e);
  }

  // console.log(imagesMap.value);

  if (user && user.id) {
    isLoadingAddress.value = true;
    const savedAddress = localStorage.getItem(getAddressKey(user.id));
  // console.log(savedAddress);

    if (savedAddress) {
      address.value = JSON.parse(savedAddress);

      // load last selected address
      const lastSelected = localStorage.getItem(`selectedAddress_${user.id}`);

      if (lastSelected && address.value.includes(lastSelected)) {
        // if exists and still in array -> use it
        selectedAddress.value = lastSelected;
      } else {
        // if not exists or removed -> use first
        selectedAddress.value = address.value[0] || "";
      }
    }
    isLoadingAddress.value = false;
  }
});

const toggleItemSelection = (item) => {
  const key = item.id + "-" + item.sellerId;
  const index = selectedItems.value.indexOf(key);
  if (index > -1) {
    selectedItems.value.splice(index, 1); // Deselect
  } else {
    selectedItems.value.push(key); // Select
  }
};
</script>

<template>
  <div class="p-6 bg-gradient-to-br from-blue-50 via-white to-blue-100 min-h-screen">
    <Breadcrumb :class="'mb-6'" :pathForBreadcrumb="[{ text: 'Home', name: 'Home' }, { text: 'SaleItem', name: 'Products' }, { text: 'Cart' }]" />
    <h1 class="text-3xl font-bold mb-8 text-gray-800">YOUR CART</h1>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">
        <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center text-gray-500 text-center py-20 border-1 rounded-2xl bg-white/60">
          <Store :size="64" color="#454545" :stroke-width="1.5" class="mb-4" />

          <h2 class="text-2xl font-semibold text-gray-700 mb-2">Your Cart is Currently Empty</h2>
          <p class="mb-6">Start adding some items to see them here.</p>

          <router-link to="/sale-items" class="px-6 py-2 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700 transition-colors duration-300"> Continue Shopping </router-link>
        </div>
        <div v-else>
          <ConfirmDelete v-if="showConfirmModal" :show="showConfirmModal" :message="confirmMessage" @confirm="confirmYes" @cancel="confirmNo" />

          <div v-else>
            <div class="bg-white p-4 rounded-lg shadow-md border border-gray-100 mb-4 flex items-center justify-between gap-2">
              <div class="flex items-center gap-3 cursor-pointer" @click="toggleSelectAll">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  @click.stop
                  class="itbms-select-all w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400"
                />
                <label class="font-semibold text-lg text-gray-700">Select All</label>
              </div>
              <button
                @click="deleteselected"
                :disabled="selectedItems.length === 0"
                class="px-4 py-2 rounded-lg shadow-sm transition text-black font-medium border hover:bg-blue-600 hover:text-white disabled:bg-gray-300 disabled:border-0 disabled:text-gray-500 disabled:cursor-not-allowed text-sm cursor-pointer"
              >
                Delete Selected
              </button>
            </div>

            <div v-for="(items, sellerId) in groupedCart" :key="sellerId" class="mb-6 bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden transition-all duration-300 hover:shadow-md">
              <!-- Seller Header -->
              <div class="flex items-center gap-3 p-4 bg-gray-50 border-b border-gray-100 cursor-pointer hover:bg-gray-100 transition-all" @click="toggleSeller(sellerId)">
                <input
                  type="checkbox"
                  :checked="isSellerSelected(sellerId)"
                  @change="toggleSeller(sellerId)"
                  @click.stop
                  class="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400"
                />
                <label class="font-semibold text-gray-800 tracking-tight pointer-events-none">
                  {{ sellerMap[Number(sellerId)] || "Unknown Seller" }}
                </label>
              </div>

              <!-- Column Headers -->
              <div class="grid grid-cols-12 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wider px-5 py-3 bg-white/60 border-b border-gray-100">
                <div class="col-span-5">Product</div>
                <div class="col-span-2 text-right">Price</div>
                <div class="col-span-3 text-center">Quantity</div>
                <div class="col-span-2 text-right">Total</div>
              </div>

              <!-- Items -->
              <div
                v-for="item in items"
                :key="item.id + '-' + item.sellerId"
                class="grid grid-cols-12 gap-4 items-center px-5 py-4 border-b border-gray-100 last:border-none hover:bg-gray-50 transition-colors cursor-pointer"
                @click="toggleItemSelection(item)"
              >
                <!-- Product Info -->
                <div class="col-span-5 flex items-center gap-4">
                  <input
                    type="checkbox"
                    :value="item.id + '-' + item.sellerId"
                    v-model="selectedItems"
                    @click.stop
                    class="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-400 flex-shrink-0"
                  />

                  <div v-if="item.images && item.images.length > 0" class="flex-shrink-0 pointer-events-none">
                    <img :src="imagesMap[item.id]?.[0] || placeholder" alt="Product Image" class="w-14 h-14 object-cover rounded-lg border border-gray-200" />
                  </div>

                  <div class="truncate pointer-events-none">
                    <p class="text-gray-800 font-medium text-sm leading-snug truncate">
                      {{ getDescription(item) }}
                    </p>
                  </div>
                </div>

                <!-- Price -->
                <div class="col-span-2 text-right text-gray-600 text-sm pointer-events-none">฿{{ unitPrice(item.price) }}</div>

                <!-- Quantity Controls -->
                <div class="col-span-3 flex items-center justify-center gap-2">
                  <button @click.stop="decrement(item)" class="w-8 h-8 flex items-center justify-center rounded-md border border-gray-300 text-gray-600 font-bold hover:bg-gray-100 transition">
                    –
                  </button>

                  <span class="w-10 text-center text-gray-800 text-sm font-medium select-none pointer-events-none">
                    {{ item.quantity }}
                  </span>

                  <button @click.stop="increment(item)" class="w-8 h-8 flex items-center justify-center rounded-md border border-gray-300 text-gray-600 font-bold hover:bg-gray-100 transition">
                    +
                  </button>
                </div>

                <!-- Total & Remove -->
                <div class="col-span-2 text-right">
                  <div class="flex items-center justify-end gap-2">
                    <span class="font-semibold text-gray-800 text-sm pointer-events-none"> ฿{{ unitPrice(item.price * item.quantity) }} </span>
                    <button @click.stop="removeFromCartConfirmed(item)" class="text-gray-400 hover:text-red-500 transition text-xl leading-none" title="Remove item">&times;</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ========================================== Order Summary ========================================== -->
      <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-xl h-fit sticky top-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-800 border-b pb-3">ORDER SUMMARY</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-600 mb-1">
            Shipping Address
            <span v-if="!selectedAddress" class="text-red-500">*</span>
          </label>
          <select v-model="selectedAddress" class="w-full border rounded-md p-2 mb-2 focus:ring focus:ring-blue-200 focus:border-blue-400 bg-gray-50">
            <option disabled value="">-- Select address --</option>
            <option v-for="(addr, index) in address" :key="index" :value="addr">
              {{ addr }}
            </option>
          </select>
          <div class="flex gap-2">
            <input type="text" v-model="newAddress" placeholder="Add new address" class="w-full border rounded-md p-2 focus:ring focus:ring-blue-200 focus:border-blue-400" />
            <button @click="addAddress" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Add</button>
          </div>
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-600 mb-1">Note</label>
          <input type="text" v-model="note" placeholder="Add a note" class="itbms-order-note w-full border rounded-md p-2 focus:ring focus:ring-blue-200 focus:border-blue-400" />
        </div>

        <div class="border-t pt-4 text-gray-700 space-y-2">
          <p class="flex justify-between">
            <span>Total items:</span>
            <span class="itbms-total-order-items font-medium">{{ selectedSummary.totalQty }} items</span>
          </p>

          <p class="itbms-total-total-price flex justify-between font-medium text-lg">
            <span>Cart subtotal:</span>
            <span>฿{{ unitPrice(selectedSummary.totalPrice) }}</span>
          </p>

          <p class="flex justify-between text-sm text-gray-500">
            <span>Shipping & Handling:</span>
            <span>Calculated at checkout</span>
          </p>

          <p class="font-bold text-2xl text-blue-700 pt-3 border-t mt-3 flex justify-between">
            <span>GRAND TOTAL:</span>
            <span>฿{{ unitPrice(selectedSummary.totalPrice) }}</span>
          </p>
        </div>

        <div class="mt-6">
          <button
            @click="PlaceOrder()"
            :disabled="!isValid"
            class="itbms-place-order-button w-full px-6 py-3 text-white font-bold rounded-lg shadow-lg transition"
            :class="isValid ? 'bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 hover:scale-[1.02]' : 'bg-blue-300 cursor-not-allowed opacity-60'"
          >
            Place Order
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

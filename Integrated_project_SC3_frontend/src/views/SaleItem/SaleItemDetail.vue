<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getSaleItemByIdV2, deleteSaleItemByIdV2, createOrder } from "@/libs/callAPI/apiSaleItem.js";
import { unitPrice, nullCatching } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import ImageUploader from "@/components/Common/ImageUploader.vue";
import Loading from "@/components/Common/Loading.vue";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";
import { useCartStore } from "@/stores/cartStore";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const product = ref({});
const loading = ref(true);
const quantity = ref(1);
const alertStore = useAlertStore();
const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);
const cartStore = useCartStore();
const auth = useAuthStore()

const confirmDeleteProduct = async () => {
  try {
    await deleteSaleItemByIdV2(pendingDeleteId.value);
    alertStore.addToast("The sale item has been deleted.", "Delete success", "success");
    sessionStorage.setItem("item-just-deleted", "true");
    router.push("/sale-items");
  } catch (error) {
    alertStore.addToast("The requested sale item does not exist.", "Delete failed", "error");
    sessionStorage.setItem("item-just-deleted", "true");
    router.push("/sale-items");
  } finally {
    showDeleteModal.value = false;
  }
};

const deleteProduct = (id) => {
  pendingDeleteId.value = id;
  showDeleteModal.value = true;
};

onMounted(async () => {
  try {
    loading.value = true;
    const data = await getSaleItemByIdV2(route.params.id);
    if (data == undefined) {
      product.value = "404_not_found";
      // console.log("product.value: " + product.value);
      // console.log("product.value: " + product.value.sellerId);
      setTimeout(() => {
        router.push("/sale-items");
      }, 2000);
    } else {
      product.value = data;
      // console.log(product.value);
      // console.log(product.value.sellerId);
      // console.log(product.value.storageGb);
      // console.log(data);
    }
  } catch (error) {
    alertStore.addToast(error.message || "Something Wrong", "This feature is currently experiencing a bug. We will fix it soon.", "error");
    alertStore.addToast("Load order failed.", "error");
    router.push("/sale-items");
  } finally {
    loading.value = false;
  }
  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});

const decrementQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--;
  }
};

const incrementQuantity = () => {
  if (quantity.value < product.value.quantity) {
    quantity.value++;
  }
};

const addItem = async () => {
  if (!product.value || !product.value.id) return;

  const checkRole = localStorage.getItem("role")
  // console.log(checkRole);
  if (!checkRole) {
    router.push({ name: 'Login' });
  }

  const accSellerId = auth.getAuthData().sellerId
  // console.log(accSellerId);
  if (product.value.sellerId === accSellerId) {
    alertStore.addToast("Seller cant't add order that their owner", "Can't add your order", "error");
    return;
  }

  const payload = {
    id: product.value.id,
    sellerId: product.value.sellerId,
    brandName: product.value.brandName,
    model: product.value.model,
    price: product.value.price,
    color: product.value.color,
    images: product.value.saleItemImage,
    stock: product.value.quantity,
    storageGb: product.value.storageGb,
  };
  // console.log(payload);
  const result = cartStore.addToCart(payload, quantity.value);
    if (result.success) {
    // แจ้ง success — ใช้ alertStore ของคุณได้เลย
    alertStore.addToast(`Add your order in to your cart amount (${result.added} )`, "Add to cart", "success");
    // console.log("add success");
  } else {
    // แจ้ง error / ข้อจำกัดสต็อก
        alertStore.addToast(result.message, "","error");
    // console.log("add failed ");
  }
};
</script>
<template>
  <!-- Loading state -->
  <div v-if="loading" class="flex items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 to-white">
    <Loading />
  </div>

  <!-- 404 Not Found -->
  <div
    v-else-if="product == '404_not_found'"
    class="flex flex-col items-center justify-center text-center py-20 space-y-8 min-h-[60vh] bg-blue-50"
  >
    <div class="bg-white p-10 rounded-2xl shadow-xl max-w-md w-full border border-blue-100">
      <img
        src="https://static.thenounproject.com/png/4019366-200.png"
        alt="404 Icon"
        class="w-28 h-28 mx-auto opacity-80"
      />
      <p class="text-slate-600 mt-4 text-lg">The requested product could not be found.</p>
    </div>
  </div>

  <!-- Product detail -->
  <div v-else class="min-h-screen bg-white">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <!-- Breadcrumb -->
      <Breadcrumb
        class="mb-8"
        :pathForBreadcrumb="[
          { text: 'Home', name: 'Home' },
          { text: 'Products', name: 'Products' },
          { text: `${product.brandName} ${product.model}`, name: 'ProductDetail' },
        ]"
      />

      <div class="grid md:grid-cols-2 gap-12">
        <!-- Product Image -->
        <div class="space-y-4">
            <ImageUploader
              :fileImageOrganize="product.fileImageOrganize"
              :param="route.params.id"
              class="w-full h-full object-cover"
            />
        </div>

        <!-- Product Info -->
        <div class="space-y-8">
          <!-- Title -->
          <div>
            <h1 class="text-4xl font-bold text-blue-800 mb-2">{{ product.brandName }}</h1>
            <p class="text-2xl text-slate-600">{{ product.model }}</p>
          </div>

          <!-- Price & Stock -->
          <div class="py-6 border-y border-blue-100">
            <div class="flex items-baseline gap-3 mb-3">
              <span class="text-3xl font-bold text-blue-600">{{ unitPrice(product.price) }}</span>
              <span class="text-lg text-slate-500">Baht</span>
            </div>
            <div class="text-sm text-slate-600">
              Stock:
              <span class="font-semibold text-slate-800">{{ product.quantity }}</span> pcs
            </div>
          </div>

          <!-- Color -->
          <div>
            <h3 class="text-xl font-semibold text-slate-700">
              Color:
              <span class="font-normal">{{ nullCatching(product.color) }}</span>
            </h3>
          </div>

          <!-- Description -->
          <div>
            <h3 class="text-xl font-semibold text-slate-800 mb-2">Description</h3>
            <p class="text-slate-600 leading-relaxed">{{ product.description }}</p>
          </div>

          <!-- Quantity Selector -->
          <div class="flex items-center gap-4">
            <h3 class="text-sm font-medium text-slate-700">Quantity</h3>
            <div class="flex items-center border border-blue-200 rounded-lg overflow-hidden">
              <button
                @click="decrementQuantity"
                class="px-3 py-2 flex items-center justify-center hover:bg-blue-100 transition disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
                :disabled="quantity <= 1"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>

              <span class="w-12 text-center font-bold text-blue-900 text-lg">{{ quantity }}</span>

              <button
                @click="incrementQuantity"
                class="px-3 py-2 flex items-center justify-center hover:bg-blue-100 transition disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
                :disabled="quantity >= product.quantity"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Add to Cart -->
          <div class="flex gap-3 pt-2">
            <button
              @click="addItem"
              class="flex-1 py-3 px-6 text-base font-medium border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-600 hover:text-white transition-all duration-150 shadow-sm cursor-pointer"
            >
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Delete Modal -->
  <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
    <div class="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md border border-blue-100">
      <h2 class="text-xl font-bold text-blue-800 mb-4">Confirm Deletion</h2>
      <p class="text-slate-600 mb-6">Are you sure you want to delete this product?</p>
      <div class="flex justify-end space-x-3">
        <button
          @click="showDeleteModal = false"
          class="px-5 py-2 text-blue-700 border border-blue-300 rounded-lg hover:bg-blue-50 transition"
        >
          Cancel
        </button>
        <button
          @click="confirmDeleteProduct"
          class="px-5 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>

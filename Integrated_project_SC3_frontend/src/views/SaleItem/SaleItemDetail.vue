<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getSaleItemByIdV2,
  deleteSaleItemByIdV2,
} from "@/libs/callAPI/apiSaleItem.js";
import { unitPrice, nullCatching } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import ImageUploader from "@/components/Common/ImageUploader.vue";
import { addToCart } from "@/composables/useCart";

const route = useRoute();
const router = useRouter();
const product = ref({});
const loading = ref(true);
const quantity = ref(1);
const alertStore = useAlertStore();
const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);

const confirmDeleteProduct = async () => {
  try {
    await deleteSaleItemByIdV2(pendingDeleteId.value);
    alertStore.addToast(
      "The sale item has been deleted.",
      "Delete success",
      "success"
    );
    sessionStorage.setItem("item-just-deleted", "true");
    router.push("/sale-items");
  } catch (error) {
    alertStore.addToast(
      "The requested sale item does not exist.",
      "Delete failed",
      "error"
    );
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
      console.log("product.value: " + product.value);
      setTimeout(() => {
        router.push("/sale-items");
      }, 2000);
    } else {
      product.value = data;
      console.log(product.value);
      console.log(data);
    }
  } catch (error) {
    console.log("โหลดข้อมูลสินค้าไม่สำเร็จ:", error.message);
  } finally {
    loading.value = false;
  }

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});

// gen มา
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

const addItem = () => {
  if (!product.value || !product.value.id) return;

  // เตรียมข้อมูลที่เราต้องการเก็บใน cart
  const payload = {
    id: product.value.id,
    sellerId: product.value.sellerId ?? product.value.seller?.id ?? null,
    brandName: product.value.brandName,
    model: product.value.model,
    price: product.value.price,
    color: product.value.color,
    image: product.value.fileImageOrganize?.[0] || null,
    stock: product.value.quantity, // สต็อกจาก backend
  };

  const result = addToCart(payload, quantity.value);

  if (result.success) {
    // แจ้ง success — ใช้ alertStore ของคุณได้เลย
    // alertStore.addToast(
    //   `เพิ่มสินค้าในตะกร้า (${result.added} ชิ้น)`,
    //   "Add to cart",
    //   "success"
    // );
    console.log("add success");
    
  } else {
    // แจ้ง error / ข้อจำกัดสต็อก
    // alertStore.addToast(result.message || "ไม่สามารถเพิ่มสินค้าได้", "Error", "error");
    console.log("add failed");
    
  }
};

</script>

<template>
  <!-- Loading Spinner -->
  <div v-if="loading" class="flex items-center justify-center min-h-[60vh]">
    <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-blue-600"></div>
  </div>

  <!-- 404 Error Page -->
  <div v-else-if="product == '404_not_found'"
    class="flex flex-col items-center justify-center text-center py-20 space-y-8 min-h-[60vh] bg-gray-50">
    <div class="bg-white p-8 rounded-xl shadow-lg max-w-md w-full">
      <!-- 404 Icon -->
      <img src="https://static.thenounproject.com/png/4019366-200.png" alt="404 Icon"
        class="w-24 h-24 mx-auto opacity-80" />

      <!-- Error Message -->
      <!-- <h1 class="itbms-message text-2xl font-bold text-gray-800 mt-6">ไม่พบสินค้าที่ค้นหา</h1> -->
      <p class="itbms-message text-gray-600 mt-2">
        The requested sale item does not exist.
      </p>
    </div>
  </div>

  <!-- Product Detail Page -->
  <div v-else class="itbms-row bg-gray-50 min-h-screen pb-12">
    <!-- Breadcrumb -->
    <div class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
        <div class="flex items-center space-x-2 text-sm text-gray-600">
          <RouterLink to="/sale-items" class="itbms-home-button hover:text-blue-600 transition">Home</RouterLink>
          <span class="text-gray-400">/</span>
          <span class="text-gray-900 font-medium">{{ product.brandName }} {{ product.model }}</span>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
      <!-- Product Overview Section -->
      <div class="bg-white rounded-xl shadow-md overflow-hidden">
        <div class="md:flex">
          <!-- Product Image Section -->
          <div class="md:w-1/2 p-6 md:p-8 bg-gray-50">
            <ImageUploader :fileImageOrganize="product.fileImageOrganize" :param="route.params.id" />
          </div>

          <!-- Product Info Section -->
          <div class="md:w-1/2 p-6 md:p-8 bg-white flex flex-col">
            <!-- Brand & Model -->
            <div class="border-b pb-4">
              <h1 class="itbms-brand text-3xl font-bold text-gray-800">
                {{ product.brandName }}
              </h1>
              <p class="itbms-model text-xl text-gray-700 mt-1">
                {{ product.model }}
              </p>
              <div class="mt-2 flex items-center"></div>
            </div>

            <!-- Price and Stock -->
            <div class="py-4 border-b">
              <div class="flex items-baseline">
                <span class="itbms-price text-3xl font-bold text-blue-600">{{
                  unitPrice(product.price)
                }}</span>
                <span class="itbms-price-unit ml-1 text-lg text-gray-500">Baht</span>
              </div>

              <div class="mt-2 flex items-center space-x-2">
                <span class="text-sm text-gray-500">
                  สินค้าคงเหลือ:
                  <span class="itbms-quantity font-medium">{{
                    product.quantity
                  }}</span>
                  ชิ้น
                </span>
              </div>
            </div>

            <!-- Color -->
            <div class="py-4 border-b">
              <h3 class="text-sm font-medium text-gray-700">
                สี:
                <span class="itbms-color font-semibold">{{
                  nullCatching(product.color)
                }}</span>
              </h3>
              <div class="mt-2 flex items-center space-x-2">
                <div class="w-8 h-8 rounded-full border-2 border-white shadow-sm ring-2 ring-blue-600" :style="`background-color: ${product.color?.toLowerCase() || 'gray'
                  }`"></div>
              </div>
            </div>

            <!-- Quantity Selector -->
            <div class="py-4 border-b flex flex-row justify-center items-center gap-5">
              <h3 class="text-sm font-medium text-gray-700">จำนวน</h3>
              <div class="mt-2 flex items-center space-x-2">
                <button @click="decrementQuantity"
                  class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition"
                  :disabled="quantity <= 1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path>
                  </svg>
                </button>
                <span class="w-12 text-center font-medium">{{ quantity }}</span>
                <button @click="incrementQuantity"
                  class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition"
                  :disabled="quantity >= product.quantity">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                  </svg>
                </button>
              </div>

              <div>
                <button 
                class="w-[100px] h-[35px] bg-blue-600"
                @click="addItem">
                  add to cart
                </button>
              </div>

            </div>

            <!-- Action Buttons -->
            <!-- <div class="py-4 mt-2 flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-2">
              <RouterLink :to="{ name: 'Edit', params: { id: product.id } }"
                class="itbms-edit-button w-full sm:w-1/2 bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition flex items-center justify-center text-center">
                Edit
              </RouterLink>

              <button @click="deleteProduct(product.id)"
                class="itbms-delete-button w-full sm:w-1/2 border border-red-600 text-red-600 py-3 rounded-lg hover:bg-red-50 transition flex items-center justify-center">
                Delete
              </button>
            </div> -->


          </div>
        </div>
      </div>

      <!------------------------------------------------------------------------------------------------------------------------ -->


      <!-- Back Button -->
      <div class="mt-8 flex justify-between items-center">
        <RouterLink to="/sale-items">
          <button class="flex items-center text-blue-600 hover:text-blue-800 transition font-medium">
            <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18">
              </path>
            </svg>
            กลับไปหน้ารายการสินค้า
          </button>
        </RouterLink>
      </div>
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
    <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">ยืนยันการลบ</h2>
      <p class="itbms-message text-gray-600 mb-6">
        Do you want to delete this sale item?
      </p>
      <div class="flex justify-end space-x-2">
        <button @click="showDeleteModal = false"
          class="itbms-cancel-button px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition">
          ยกเลิก
        </button>
        <button @click="confirmDeleteProduct"
          class="itbms-confirm-button px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition">
          ยืนยัน
        </button>
      </div>
    </div>
  </div>

  <!-- Alert Message -->
  <!-- <div
    v-if="alertStore.message"
    :class="`itbms-message px-4 py-2 rounded mb-4 ${
      alertStore.type === 'error'
        ? 'bg-red-100 text-red-700'
        : 'bg-green-100 text-green-700'
    }`"
  >
    {{ alertStore.message }}
  </div> -->
</template>

<style scoped>
/* Additional custom styles can be added here */
</style>

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
      console.log("product.value: " + product.value);
      console.log("product.value: " + product.value.sellerId);
      setTimeout(() => {
        router.push("/sale-items");
      }, 2000);
    } else {
      product.value = data;
      console.log(product.value);
      console.log(product.value.sellerId);
      console.log(product.value.storageGb);
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
  console.log(checkRole);
  if (!checkRole) {
    router.push({ name: 'Login' });
  }

  const accSellerId = auth.getAuthData().sellerId
  console.log(accSellerId);
  if (product.value.sellerId === accSellerId) {
    alertStore.addToast("ไม่สามารถเพิ่มสินค้าได้", "Error", "error");
    return;
  }







  // let allImages = [];
  // if (product.value.saleItemImage && product.value.saleItemImage.length > 0) {
  //   const sortedImages = [...product.value.saleItemImage].sort(
  //     (a, b) => a.imageViewOrder - b.imageViewOrder
  //   );

  //   allImages = await Promise.all(
  //     sortedImages.map(img => getImageByImageName(img.fileName))
  //   );
  // }

  // console.log(allImages); // จะได้ URL ของแต่ละรูป

  // เตรียมข้อมูลที่เราต้องการเก็บใน cart
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
  console.log(payload);
  const result = cartStore.addToCart(payload, quantity.value);
  if (result.success) {
    // แจ้ง success — ใช้ alertStore ของคุณได้เลย
    alertStore.addToast(`เพิ่มสินค้าในตะกร้า (${result.added} ชิ้น)`, "Add to cart", "success");
    console.log("add success");
  } else {
    // แจ้ง error / ข้อจำกัดสต็อก
    alertStore.addToast(result.message || "ไม่สามารถเพิ่มสินค้าได้", "Error", "error");
    console.log("add failed ");
  }
};
</script>

<template>
  <!-- Loading Spinner -->
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <Loading />
  </div>

  <!-- 404 Error Page -->
  <div v-else-if="product == '404_not_found'"
    class="flex flex-col items-center justify-center text-center py-20 space-y-8 min-h-[60vh] bg-gray-50">
    <div class="bg-white p-8 rounded-xl shadow-lg max-w-md w-full">
      <!-- 404 Icon -->
      <img src="https://static.thenounproject.com/png/4019366-200.png" alt="404 Icon"
        class="w-24 h-24 mx-auto opacity-80" />

      <!-- Error Message -->
      <p class="itbms-message text-gray-600 mt-2">The requested sale item does not exist.</p>
    </div>
  </div>

  <!-- Product Detail Page -->
  <div v-else class="itbms-row bg-gray-50 min-h-screen pb-12">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
      <Breadcrumb :class="'mb-6'" :pathForBreadcrumb="[
        { text: 'Home', name: 'Home' },
        { text: 'SaleItem', name: 'Products' },
        { text: `${product.brandName} ${product.model}`, name: 'UserProfile' },
      ]" />
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
                <span class="itbms-price text-3xl font-bold text-blue-600">{{ unitPrice(product.price) }}</span>
                <span class="itbms-price-unit ml-1 text-lg text-gray-500">Baht</span>
              </div>

              <div class="mt-2 flex items-center space-x-2">
                <span class="text-sm text-gray-500">
                  สินค้าคงเหลือ:
                  <span class="itbms-quantity font-medium">{{ product.quantity }}</span>
                  ชิ้น
                </span>
              </div>
            </div>

            <!-- Color -->
            <div class="py-4 border-b">
              <h3 class="text-sm font-medium text-gray-700">
                สี:
                <span class="itbms-color font-semibold">{{ nullCatching(product.color) }}</span>
              </h3>
              <div class="mt-2 flex items-center space-x-2">
                <div class="w-8 h-8 rounded-full border-2 border-white shadow-sm ring-2 ring-blue-600"
                  :style="`background-color: ${product.color?.toLowerCase() || 'gray'}`"></div>
              </div>
            </div>

            <!-- Quantity Selector + Add to Cart -->
            <div class="py-6 border-b flex flex-col sm:flex-row items-center gap-6">

              <!-- ส่วนเลือกจำนวน -->
              <div class="flex flex-row items-center gap-4">
                <h3 class="text-sm font-medium text-gray-700">จำนวน</h3>
                <div class="flex items-center space-x-2">
                  <button @click="decrementQuantity"
                    class="itbms-dec-qty-button w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="quantity <= 1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path>
                    </svg>
                  </button>

                  <span class="itbms-add-to-cart-quantity w-12 text-center font-medium text-gray-800">{{ quantity }}</span>

                  <button @click="incrementQuantity"
                    class="itbms-inc-qty-button w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="quantity >= product.quantity">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- ปุ่ม Add to Cart -->
              <button @click="addItem"
                class="itbms-add-to-cart-button w-[180px] h-[46px] bg-gradient-to-r from-blue-600 to-blue-500 text-white font-semibold rounded-xl shadow-md hover:from-blue-700 hover:to-blue-600 hover:shadow-lg hover:scale-[1.05] active:scale-[0.98] transition-all duration-300 ease-out">
                Add to Cart
              </button>

            </div>


          </div>
        </div>
      </div>

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
      <p class="itbms-message text-gray-600 mb-6">Do you want to delete this sale item?</p>
      <div class="flex justify-end space-x-2">
        <button @click="showDeleteModal = false"
          class="itbms-cancel-button px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition">ยกเลิก</button>
        <button @click="confirmDeleteProduct"
          class="itbms-confirm-button px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition">ยืนยัน</button>
      </div>
    </div>



    <div>

    </div>
  </div>
</template>

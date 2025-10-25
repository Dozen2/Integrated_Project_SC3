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
  <div v-if="loading" class="flex items-center justify-center min-h-screen">
    <Loading />
  </div>

  <div v-else-if="product == '404_not_found'"
    class="flex flex-col items-center justify-center text-center py-20 space-y-8 min-h-[60vh] bg-blue-50">
    <div class="bg-white p-8 rounded-xl shadow-lg max-w-md w-full border border-blue-100">
      <img src="https://static.thenounproject.com/png/4019366-200.png" alt="404 Icon"
        class="w-24 h-24 mx-auto opacity-80" />

      <p class="itbms-message text-slate-600 mt-2">The requested sale item does not exist.</p>
    </div>
  </div>

  <div v-else class="itbms-row min-h-screen">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Breadcrumb :class="'mb-6'" :pathForBreadcrumb="[
        { text: 'Home', name: 'Home' },
        { text: 'SaleItem', name: 'Products' },
        { text: `${product.brandName} ${product.model}`, name: 'UserProfile' },
      ]" />

      <div class="grid md:grid-cols-2 gap-12">

        <div class="space-y-4">
          <div class="relative aspect-square   rounded-2xl overflow-hidden">
            <ImageUploader :fileImageOrganize="product.fileImageOrganize" :param="route.params.id"
              class="w-full h-full object-cover" />
          </div>
        </div>

        <div class="space-y-6">

          <div>
            <h1 class="itbms-brand text-3xl font-bold text-blue-800 mb-4">
              {{ product.brandName }}
            </h1>
            <p class="itbms-model text-xl text-slate-600 mt-1">
              {{ product.model }}
            </p>
            <div class="mt-2 flex items-center"></div>
          </div>

          <div class="py-6 border-y border-blue-200">
            <div class="flex items-baseline gap-3 mb-2">
              <span class="itbms-price text-4xl font-extrabold text-blue-600">{{ unitPrice(product.price) }}</span>
              <span class="itbms-price-unit ml-1 text-lg text-slate-500">Baht</span>
            </div>

            <div class="mt-2 flex items-center space-x-2">
              <span class="text-sm text-slate-500">
                สินค้าคงเหลือ:
                <span class="itbms-quantity font-medium text-slate-700">{{ product.quantity }}</span>
                ชิ้น
              </span>
            </div>
          </div>

          <div>
            <h3 class="text-xl font-semibold text-slate-700 mb-3">
              สี:
              <span class="itbms-color font-normal">{{ nullCatching(product.color) }}</span>
            </h3>
            <div class="mt-2 flex items-center space-x-2">
              <div class="w-8 h-8 rounded-full border-2 border-white shadow-sm ring-2 ring-blue-500"
                :style="`background-color: ${product.color?.toLowerCase() || 'gray'}`"></div>
            </div>
          </div>

          <div>
            <h3 class="text-xl font-semibold text-slate-800 mb-3">
              Description:
            </h3>
            <p class="itbms-description text-slate-600 leading-relaxed">
              {{ product.description }}
            </p>
          </div>

          <div class="flex items-center gap-4">
            <h3 class="text-sm font-medium text-slate-700">จำนวน</h3>
            <div class="flex items-center border border-blue-200 rounded-lg">
              <button @click="decrementQuantity"
                class="itbms-dec-qty-button px-3 py-1 flex items-center justify-center hover:bg-blue-100 transition disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="quantity <= 1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path>
                </svg>
              </button>

              <span class="itbms-add-to-cart-quantity w-12 text-center font-bold text-blue-900">{{ quantity }}</span>

              <button @click="incrementQuantity"
                class="itbms-inc-qty-button px-3 py-1 flex items-center justify-center hover:bg-blue-100 transition disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="quantity >= product.quantity">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="flex gap-3">
            <button @click="addItem"
              class="itbms-add-to-cart-button flex-1 py-3 px-6 text-base font-medium border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 active:bg-blue-100 transition-all duration-150">
              Add to Cart
            </button>
          </div>

        </div>
      </div>

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

  <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
    <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
      <h2 class="text-lg font-semibold text-blue-800 mb-4">ยืนยันการลบ</h2>
      <p class="itbms-message text-slate-600 mb-6">Do you want to delete this sale item?</p>
      <div class="flex justify-end space-x-2">
        <button @click="showDeleteModal = false"
          class="itbms-cancel-button px-4 py-2 bg-white text-blue-700 border border-blue-300 rounded hover:bg-blue-50 transition">ยกเลิก</button>
        <button @click="confirmDeleteProduct"
          class="itbms-confirm-button px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition">ยืนยัน</button>
      </div>
    </div>
    <div>
    </div>
  </div>
</template>
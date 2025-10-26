<script setup>
import { nullCatching, unitPrice } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import { onMounted, ref } from "vue";
import { getImageByImageName } from "@/libs/callAPI/apiSaleItem.js";
import { useCartStore } from "@/stores/cartStore";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { ShoppingCart } from "lucide-vue-next";

const alertStore = useAlertStore();
const cartStore = useCartStore();
const auth = useAuthStore();
const router = useRouter();

const props = defineProps({
  product: Array,
  default: () => [],
  imageUrl: Array,
});

onMounted(async () => {
  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});

//===================== add to cart =======================
const addItem = (item) => {
  const checkRole = localStorage.getItem("role");
  console.log(checkRole);
  if (!checkRole) {
    router.push({ name: "Login" });
  }
  const accSellerId = auth.getAuthData().sellerId;
  console.log(accSellerId);
  if (item.sellerId === accSellerId) {
    alertStore.addToast("ไม่สามารถเพิ่มสินค้าได้", "Error", "error");
    return;
  }

  const payload = {
    id: item.id,
    sellerId: item.sellerId,
    brandName: item.brandName,
    model: item.model,
    price: item.price,
    color: item.color,
    images: [
      {
        fileName: item.mainImageFileName,
        imageViewOrder: 0,
      },
    ],
    stock: item.quantity,
    storageGb: item.storageGb,
  };
  console.log(payload);

  const result = cartStore.addToCart(payload);

  if (result.success) {
    alertStore.addToast(`เพิ่มสินค้าในตะกร้า (${result.added} ชิ้น)`, "Add to cart", "success");
    console.log("add success");
  } else {
    alertStore.addToast(result.message || "ไม่สามารถเพิ่มสินค้าได้", "Error", "error");
    console.log("add failed ");
  }
};
</script>

<template>
  <div class="py-6 max-w-7xl mx-auto">
    <div v-if="product.length === 0" class="text-center text-gray-500 text-xl">no sale item</div>

    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
      <div v-for="(item, index) in product" :key="index" class="itbms-row bg-white p-4 rounded-2xl shadow-md hover:shadow-lg transition-all hover:scale-[1.02] space-x-20">
        <RouterLink :to="`/sale-items/${item.id}`">
          <img :src="imageUrl[index]" alt="product image" class="w-full h-40 object-contain" />
          <div class="mt-3 space-y-1">
            <h2 class="itbms-brand text-lg font-bold text-gray-800">
              {{ item.brandName }}
            </h2>
            <p class="itbms-model text-sm text-gray-600">
              {{ item.model }}
            </p>
            <span class="itbms-ramGb text-sm text-gray-600"> {{ nullCatching(item.ramGb) }} / </span>
            <span class="itbms-storageGb text-sm text-gray-600">
              {{ nullCatching(item.storageGb) }}
              <span class="itbms-storageGb-unit">GB</span>
            </span>
            <p class="itbms-color text-sm text-gray-600">
              {{ unitPrice(item.color) }}
            </p>
            <div class="justify-between flex">
              <p class="itbms-price text-blue-600 font-semibold mt-3 text-lg">
                {{ unitPrice(item.price) }}
                <span class="itbms-price-unit">Baht</span>
              </p>
              <div
                @click="addItem(item)"
                class="itbms-add-to-cart-button cursor-pointer select-none bg-blue-600 text-white p-3 rounded-full shadow-md transition-all duration-200 hover:bg-blue-700 hover:scale-110 active:scale-95 flex items-center"
              >
                <ShoppingCart /> +
              </div>
            </div>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

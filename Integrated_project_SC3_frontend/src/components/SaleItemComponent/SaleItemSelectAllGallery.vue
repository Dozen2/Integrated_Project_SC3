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
  // console.log(checkRole);
  if (!checkRole) {
    router.push({ name: "Login" });
  }
  const accSellerId = auth.getAuthData().sellerId;
  // console.log(accSellerId);
  // console.log(item.quantity);
  if (item.sellerId === accSellerId) {
    alertStore.addToast("Seller cant't add order that their owner", "Can't add your order", "error");
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
  // console.log(payload);

  const result = cartStore.addToCart(payload);

  if (result.success) {
    alertStore.addToast(`Add your order in to your cart amount (${result.added} )`, "Add to cart", "success");
  } else {
    alertStore.addToast(result.message, "", "error");
  }
};
</script>
<template>
  <div class="py-6 max-w-7xl mx-auto">
    <div v-if="product.length === 0" class="text-center text-gray-500 text-xl">no sale item</div>

    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
      <RouterLink
        v-for="(item, index) in product"
        :key="index"
        :to="`/sale-items/${item.id}`"
        class="block itbms-row bg-white p-4 rounded-2xl shadow-md hover:shadow-lg transition-all hover:scale-[1.02]"
      >
        <img :src="imageUrl[index]" alt="product image" class="w-full h-40 object-contain" />

        <div class="mt-3 space-y-1">
          <h2 class="itbms-brand text-lg font-bold text-gray-800">
            {{ item.brandName }}
          </h2>
          <p class="itbms-model text-sm text-gray-600">{{ item.model }}</p>
          <span class="itbms-ramGb text-sm text-gray-600"> {{ nullCatching(item.ramGb) }} / </span>
          <span class="itbms-storageGb text-sm text-gray-600">
            {{ nullCatching(item.storageGb) }}
            <span class="itbms-storageGb-unit">GB</span>
          </span>
          <p class="itbms-color text-sm text-gray-600">
            {{ unitPrice(item.color) }}
          </p>
        </div>

        <div class="flex justify-between items-center mt-3">
          <p class="itbms-price text-blue-600 font-semibold text-lg">
            {{ unitPrice(item.price) }}
            <span class="itbms-price-unit">Baht</span>
          </p>

          <button
            @click.stop.prevent="addItem(item)"
            :disabled="item.quantity == 0"
            class="itbms-add-to-cart-button relative cursor-pointer select-none px-5 py-2 rounded-full shadow-md transition-all duration-300 flex items-center justify-center gap-2 bg-blue-600 text-white hover:bg-blue-700 hover:scale-105 active:scale-95 disabled:bg-gray-300 disabled:text-gray-500 disabled:cursor-not-allowed disabled:scale-100 disabled:shadow-none"
          >
            <!-- Icon (shown on md and up) -->
            <span class="hidden md:flex items-center">
              <ShoppingCart class="w-5 h-5" />
            </span>

            <!-- Text (shown below md) -->
            <span class="md:hidden text-sm font-semibold tracking-wide"> Add to Cart </span>
          </button>
        </div>
      </RouterLink>
    </div>
  </div>
</template>

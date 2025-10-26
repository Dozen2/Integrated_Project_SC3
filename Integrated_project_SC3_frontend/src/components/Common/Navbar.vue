<script setup>
import { ClipboardList, LogOut, ShoppingCart, Smartphone, UserRound } from "lucide-vue-next";
import { useAuthStore } from '@/stores/auth'
import { RouterLink, useRouter } from "vue-router";
import DropDownManagement from "./DropDownManagement.vue";
import { ref, onMounted, computed  } from "vue";
import { useCartStore } from "@/stores/cartStore";
// import { cartItemCount, updateCartCount } from "@/composables/useCart.js";


const router = useRouter();
const auth = useAuthStore();
const cartStore = useCartStore();

const logOut = async () => {
  await auth.logout();
  router.push({ name: 'Products' });

};

const refresh = async () => {
  await auth.refreshToken()
  console.log("refresh pass");
  
}

const cartCount = computed(() => cartStore.cartItemCount);

// เรียกตอน mounted เพื่อให้แสดงจำนวนตั้งแต่โหลดหน้าแรก
onMounted(() => {
  if(auth.role){
    cartStore.loadCart();
    cartStore.updateQuantity();
  }
});

</script>

<template>
  <div>
    <div
      class="sticky top-0 z-50 bg-white shadow-md h-16 flex items-center justify-between px-4 sm:px-8 border-b border-gray-100 text-gray-700 font-sans transition-all duration-300"
    >
      <RouterLink to="/" class="flex items-center gap-2 group">
        <Smartphone class="text-sky-500 w-6 h-6" />
        <h1
          class="font-extrabold text-xl tracking-wide text-sky-700 group-hover:text-sky-500 transition-colors duration-200 sm:block"
        >
          ITBMS_SHOP
        </h1>
      </RouterLink>

      <div class="flex items-center gap-3 sm:gap-6">
        <div v-if="auth.role" class="flex items-center gap-3 sm:gap-6">
          
          <div v-if='auth.role == "ROLE_SELLER"' class="hidden sm:flex items-center justify-between">
            <DropDownManagement :isMobile="false" />
          </div>

          <div class="relative flex">
            <RouterLink :to="{ name: 'UserProfile' }" class="itbms-profile">
              <button
                class="p-2 rounded-full cursor-pointer bg-blue-50 hover:bg-blue-100 transition-colors duration-200 flex border border-blue-200"
              >
                <UserRound color="#000000" class="w-5 h-5" />
              </button>
            </RouterLink>
          </div>

          <div class="relative cursor-pointer">
            <RouterLink :to="{ name: 'Cart' }">
              <ShoppingCart color="#3b82f6" class="w-6 h-6 hover:text-sky-600 transition-colors" />
              <span
                v-if="cartCount > 0"
                class="itbms-cart-quantity absolute -top-2 -right-2 bg-sky-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold ring-2 ring-white"
              >
                {{ cartCount }}
              </span>
            </RouterLink>
          </div>

          <div class="relative cursor-pointer">
            <RouterLink :to="{ name: 'PlaceOrder' }">
              <ClipboardList color="#3b82f6" class="w-6 h-6 hover:text-sky-600 transition-colors" />
              <span
                v-if="orderPlaceItemCount > 0"
                class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold ring-2 ring-white"
              >
                {{ orderPlaceItemCount }}
              </span>
            </RouterLink>
          </div>

          <div class="relative hidden sm:block">
            <button
              class="itbms-logout flex items-center gap-1 border border-sky-500 rounded-full px-3 py-1 cursor-pointer transition-colors text-sky-500 font-semibold hover:bg-sky-500 hover:text-white text-sm"
              @click="logOut"
            >
              <span>Log Out</span>
              <LogOut class="w-4 h-4" />
            </button>
          </div>
        </div>

        <div v-else class="flex items-center gap-3 sm:gap-4 text-sm font-medium">
          <RouterLink :to="{ name: 'Login' }">
            <span
              class="cursor-pointer text-gray-600 hover:text-sky-500 transition-colors duration-200"
            >
              Login
            </span>
          </RouterLink>

          <RouterLink :to="{ name: 'Register' }">
            <span
              class="cursor-pointer bg-sky-500 text-white font-semibold px-3 py-1.5 rounded-full hover:bg-sky-600 transition-all duration-200 shadow-md shadow-sky-500/30"
            >
              Register
            </span>
          </RouterLink>
        </div>
      </div>
    </div>
    
    <div v-if="auth.role === 'ROLE_SELLER'" class="block sm:hidden border-b border-gray-100 bg-white shadow-sm">
      <DropDownManagement :isMobile="true" />
    </div>
  </div>
</template>
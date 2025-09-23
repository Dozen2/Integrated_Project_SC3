<script setup>
import { LogOut, ShoppingCart, Smartphone, UserRound } from "lucide-vue-next";
import { useAuthStore } from '@/stores/auth'
import { RouterLink, useRouter } from "vue-router";
import DropDownManagement from "./DropDownManagement.vue";

const router = useRouter();
const auth = useAuthStore();

const logOut = async () => {
  await auth.logout();
  router.push({ name: 'Home' });

};

</script>

<template>
  <div
    class="sticky top-0 z-50 bg-blue-800 h-20 flex items-center justify-between px-8 border-b border-slate-600 text-slate-300 font-sans">
    <RouterLink to="/" class="flex items-center gap-3 group">
      <Smartphone />
      <h1 class="font-bold text-2xl tracking-wide text-white group-hover:text-sky-400 transition-colors duration-300">
        ITBMS_SHOP
      </h1>
    </RouterLink>

    <!-- <div class="flex-grow max-w-lg mx-8">
      <div class="relative">
        <input
          type="text"
          placeholder="🔍 Search for products..."
          class="w-full bg-blue-300 border border-slate-700 rounded-full py-2 pl-5 pr-4 text-black focus:outline-none focus:border-sky-500 transition-colors"
        />
      </div>
    </div> -->

    <!-- =======================Option=========================     -->

    <div class="flex items-center gap-8">
      <div v-if="auth.role" class="flex items-center gap-6">


        <div v-if='auth.role == "ROLE_SELLER"' class="flex items-center justify-between gap-4">
          <DropDownManagement />
          <!-- <RouterLink
            :to="{ name: 'ProductManage' }"
            class="inline-flex items-center gap-2 bg-blue-500 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all"
          >
            <span class="itbms-manage-brand tracking-wide"
              >Manage Sale Items</span
            >
          </RouterLink> -->
        </div>


        <div class="relative flex">
          <!-- ปุ่มหลัก -->
          <RouterLink 
          :to="{ name: 'UserProfile' }"
          class="itbms-profile">
            <button class="p-2 rounded-full cursor-pointer bg-blue-100 hover:bg-blue-200 transition-colors flex">
              <UserRound color="#000000" />
            </button>
          </RouterLink>
        </div>

        <div class="relative">
          <ShoppingCart color="#ffffff" />
          <span v-if="cartItemCount > 0"
            class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
            {{ cartItemCount }}
          </span>
        </div>

        <div class="relative">
          <button
            class="itbms-logout flex boader border-white rounded-full px-4 py-2 cursor-pointer transition-colors text-white font-semibold hover:bg-blue-600 "
            @click="logOut">
            <span class="mr-[7px]">Log Out</span>
            <LogOut color="#ffffff" />
          </button>
        </div>

      </div>

      <div v-else class="flex items-center gap-6 text-lg font-medium">
        <RouterLink :to="{ name: 'Login' }">
          <span class="cursor-pointer hover:text-sky-400 transition-colors duration-300">Login</span>
        </RouterLink>

        <RouterLink :to="{ name: 'Register' }">
          <span
            class="cursor-pointer bg-sky-500 text-white font-semibold px-4 py-2 rounded-md hover:bg-sky-600 transition-all duration-300 shadow-lg shadow-sky-500/20">
            Register
          </span>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
// import Search from './Query/Search.vue'; // สมมติว่ามีไฟล์นี้

const cartItemCount = ref(3) // ตัวแปรสมมติสำหรับจำนวนสินค้าในตะกร้า

const handleSearch = (keyword) => {
  console.log("ค้นหา:", keyword)
}
</script>

<template>
  <div class="bg-blue-800 h-20 flex items-center justify-between px-8 border-b border-slate-600 text-slate-300 font-sans">

    <RouterLink to="/" class="flex items-center gap-3 group">
      <svg xmlns="http://www.w3.org/2000/svg" 
           class="h-8 w-8 text-sky-400 group-hover:animate-pulse" 
           fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
      </svg>
      <h1 class="font-bold text-2xl tracking-wide text-white group-hover:text-sky-400 transition-colors duration-300">
        ITBMS_SHOP
      </h1>
    </RouterLink>
    
    <div class="flex-grow max-w-lg mx-8">
       <div class="relative">
         <input type="text" placeholder="Search for products..." 
                class="w-full bg-blue-300 border border-slate-700 rounded-full py-2 pl-10 pr-4 text-black focus:outline-none focus:border-sky-500 transition-colors">
         <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
         </svg>
       </div>
    </div>

    <div class="flex items-center gap-8">
      <div class="flex items-center gap-6 text-lg font-medium">
        <span class="cursor-pointer hover:text-sky-400 transition-colors duration-300">SignIn</span>
        
        <span class="cursor-pointer bg-sky-500 text-white font-semibold px-4 py-2 rounded-md hover:bg-sky-600 transition-all duration-300 shadow-lg shadow-sky-500/20">
          Register
        </span>
      </div>

      <div class="flex items-center gap-6">
        <svg xmlns="http://www.w3.org/2000/svg" width="28px" height="28px" fill="currentColor"
          class="cursor-pointer hover:text-sky-400 transition-colors duration-300"
          viewBox="0 0 16 16">
          <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
        </svg>

        <div class="relative">
          <svg xmlns="http://www.w3.org/2000/svg" height="28px" width="28px" viewBox="0 -960 960 960"
            fill="currentColor" class="cursor-pointer hover:text-sky-400 transition-colors duration-300">
            <path
              d="M280-80q-33 0-56.5-23.5T200-160q0-33 23.5-56.5T280-240q33 0 56.5 23.5T360-160q0 33-23.5 56.5T280-80Zm400 0q-33 0-56.5-23.5T600-160q0-33 23.5-56.5T680-240q33 0 56.5 23.5T760-160q0 33-23.5 56.5T680-80ZM246-720l96 200h280l110-200H246Zm-38-80h590q23 0 35 20.5t1 41.5L692-482q-11 20-29.5 31T622-440H324l-44 80h480v80H280q-45 0-68-39.5t-2-78.5l54-98-144-304H40v-80h130l38 80Zm134 280h280-280Z" />
          </svg>
          <span v-if="cartItemCount > 0" class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
            {{ cartItemCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* คุณสามารถเพิ่ม custom styles ที่นี่ได้ถ้าต้องการ */
</style>
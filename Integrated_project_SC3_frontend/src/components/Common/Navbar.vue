<script setup>
import { ref } from "vue"

const showMenu = ref(false)
const toggleMenu = () => (showMenu.value = !showMenu.value)
</script>

<template>
  <div class="bg-white h-20 flex flex-row items-center px-6 justify-between">
    <!-- Logo -->
    <RouterLink to="/" class="flex flex-row items-center text-blue-500 hover:text-blue-900 transition-colors">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
      </svg>
      <h1 class="font-mono font-bold text-lg">ITBMS_SHOP</h1>
    </RouterLink>

    <!-- Icons -->
    <div class="flex flex-row gap-6 items-center">
      <!-- User Menu -->
      <div class="relative inline-block">
        <!-- ปุ่มหลัก -->
        <button
          @click="toggleMenu"
          class="p-2 rounded-full bg-blue-100 hover:bg-blue-200 transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" height="28" viewBox="0 -960 960 960" width="28" fill="#1d4ed8">
            <path
              d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Z" />
          </svg>
        </button>

        <!-- เมนู Login/Signup -->
        <div
          v-if="showMenu"
          class="absolute right-0 mt-3 w-40 bg-white border border-gray-100 rounded-xl shadow-lg overflow-hidden z-50"
        >
          <RouterLink 
            :to="{name: 'Login'}"
            class="flex items-center gap-2 w-full px-4 py-2 text-blue-600 hover:bg-blue-50 transition-colors"
            @click="toggleMenu"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 -960 960 960" fill="currentColor">
              <path
                d="M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Zm-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z" />
            </svg>
            <span class="font-medium">Login</span>
          </RouterLink>

          <RouterLink
            :to="{name: 'Register'}"
            class="flex items-center gap-2 w-full px-4 py-2 text-blue-600 hover:bg-blue-50 transition-colors"
            @click="toggleMenu"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 -960 960 960" fill="currentColor">
              <path
                d="M240-160q-33 0-56.5-23.5T160-240q0-33 23.5-56.5T240-320q33 0 56.5 23.5T320-240q0 33-23.5 56.5T240-160Zm0-240q-33 0-56.5-23.5T160-480q0-33 23.5-56.5T240-560q33 0 56.5 23.5T320-480q0 33-23.5 56.5T240-400Zm0-240q-33 0-56.5-23.5T160-720q0-33 23.5-56.5T240-800q33 0 56.5 23.5T320-720q0 33-23.5 56.5T240-640Zm240 0q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Zm240 0q-33 0-56.5-23.5T640-720q0-33 23.5-56.5T720-800q33 0 56.5 23.5T800-720q0 33-23.5 56.5T720-640ZM480-400q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm40 240v-123l221-220q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T863-380L643-160H520Z" />
            </svg>
            <span class="font-medium">Signup</span>
          </RouterLink>
          
        </div>
      </div>

      <!-- Cart -->
      <button class="p-2 rounded-full bg-blue-100 hover:bg-blue-200 transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7" viewBox="0 -960 960 960" fill="#1d4ed8">
          <path
            d="M280-80q-33 0-56.5-23.5T200-160q0-33 23.5-56.5T280-240q33 0 56.5 23.5T360-160q0 33-23.5 56.5T280-80Zm400 0q-33 0-56.5-23.5T600-160q0-33 23.5-56.5T680-240q33 0 56.5 23.5T760-160q0 33-23.5 56.5T680-80ZM246-720l96 200h280l110-200H246Zm-38-80h590q23 0 35 20.5t1 41.5L692-482q-11 20-29.5 31T622-440H324l-44 80h480v80H280q-45 0-68-39.5t-2-78.5l54-98-144-304H40v-80h130l38 80Z" />
        </svg>
      </button>
    </div>
  </div>
</template>

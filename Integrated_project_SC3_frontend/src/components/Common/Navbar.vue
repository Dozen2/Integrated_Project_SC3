<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import BrandDropdown from './../BrandComponents/BrandDropdown.vue'

const isMenuOpen = ref(false)
</script>

<template>
  <header class="bg-white shadow-lg sticky top-0 z-50 border-b border-gray-100">
    <div class="w-full max-w-screen-2xl mx-auto px-3 sm:px-4 lg:px-8 py-3 sm:py-4 flex items-center justify-between">
      <!-- Logo -->
      <RouterLink to="/"
        class="text-xl sm:text-2xl lg:text-3xl font-bold text-blue-600 hover:text-blue-700 transition duration-200 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 sm:h-6 sm:w-6 lg:h-7 lg:w-7 mr-1 sm:mr-2" fill="none"
          viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
        </svg>
        <span class="hidden xs:inline">ITBMS-SHOP</span>
        <!-- <span class="xs:hidden">SS</span> -->
      </RouterLink>

      <!-- Hamburger Button -->
      <button @click="isMenuOpen = !isMenuOpen"
        class="ml-1 sm:ml-0 lg:hidden rounded-lg p-1 sm:p-2 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-300 transition duration-200">
        <svg v-if="!isMenuOpen" class="w-5 h-5 sm:w-6 sm:h-6 text-gray-700" fill="none" stroke="currentColor"
          viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="w-5 h-5 sm:w-6 sm:h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- Full Menu (desktop) -->
      <div class="hidden lg:flex items-center space-x-4 xl:space-x-8">
        <!-- Search -->
        <div class="relative w-48 xl:w-96">
          <input type="text" placeholder="Search smartphones..."
            class="pl-8 sm:pl-10 pr-3 sm:pr-4 py-1.5 sm:py-2 text-sm border border-gray-300 rounded-full w-full focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300 transition duration-200" />
          <svg class="absolute left-2 sm:left-3 top-2 text-gray-400 w-4 h-4 sm:w-5 sm:h-5" fill="none"
            stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 21l-4.35-4.35m0 0A7.5 7.5 0 1110.5 3a7.5 7.5 0 016.15 13.65z" />
          </svg>
        </div>

        <div class="border-l border-gray-200 h-8 mx-1 xl:mx-2 hidden lg:block"></div>



        <!-- Nav Links -->
        <div class="hidden lg:flex space-x-4 xl:space-x-6">
          <a href="#" class="text-gray-600 hover:text-blue-600 font-medium transition duration-200">Deals</a>
          <RouterLink :to="{ name: 'ProuctCreate' }"
            class=" text-gray-600 hover:text-blue-600 font-medium transition duration-200">
            New
          </RouterLink>
          <a href="#"
            class="hidden xl:block text-gray-600 hover:text-blue-600 font-medium transition duration-200">Support</a>
        </div>

        <!-- Cart Icon with Badge -->
        <div class="relative">
          <svg class="w-5 h-5 sm:w-6 sm:h-6 text-gray-600 hover:text-blue-600 cursor-pointer transition duration-200"
            fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M3 3h2l.4 2M7 13h10l1.4-7H6.6M7 13L5.2 6H3m4 7l1.5 6h9l1.5-6M10 17a2 2 0 100 4 2 2 0 000-4zm7 0a2 2 0 100 4 2 2 0 000-4z" />
          </svg>
          <span
            class="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full h-4 w-4 sm:h-5 sm:w-5 flex items-center justify-center">2</span>
        </div>

        <!-- Auth Buttons -->
        <div class="flex items-center space-x-2 xl:space-x-4">
          <button
            class="border border-gray-300 px-3 sm:px-4 xl:px-5 py-1 sm:py-2 rounded-full text-xs sm:text-sm font-medium hover:bg-gray-50 transition duration-200 focus:outline-none focus:ring-2 focus:ring-blue-300">Login</button>
          <button
            class="bg-blue-600 text-white px-3 sm:px-4 xl:px-5 py-1 sm:py-2 rounded-full text-xs sm:text-sm font-medium hover:bg-blue-700 transition duration-200 shadow-sm hover:shadow focus:outline-none focus:ring-2 focus:ring-blue-500">Sign
            Up</button>
        </div>
      </div>

      <!-- Mini Search for Small/Medium Screens -->
      <div class="flex lg:hidden items-center ml-auto mr-2 sm:mr-4">
        <div class="relative w-full max-w-[140px] sm:max-w-[200px] md:max-w-[250px]">
          <input type="text" placeholder="Search..."
            class="pl-7 pr-2 py-1 text-sm border border-gray-300 rounded-full w-full focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300 transition duration-200" />
          <svg class="absolute left-2 top-1.5 text-gray-400 w-3.5 h-3.5" fill="none" stroke="currentColor"
            viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 21l-4.35-4.35m0 0A7.5 7.5 0 1110.5 3a7.5 7.5 0 016.15 13.65z" />
          </svg>

        </div>

        <!-- Mini Cart -->
        <div class="relative ml-2 sm:ml-3">
          <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M3 3h2l.4 2M7 13h10l1.4-7H6.6M7 13L5.2 6H3m4 7l1.5 6h9l1.5-6M10 17a2 2 0 100 4 2 2 0 000-4zm7 0a2 2 0 100 4 2 2 0 000-4z" />
          </svg>
          <span
            class="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full h-4 w-4 flex items-center justify-center">2</span>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <div v-if="isMenuOpen"
      class="lg:hidden px-3 sm:px-4 pb-4 pt-2 space-y-3 border-t border-gray-200 animate-fadeIn bg-white">
      <BrandDropdown />

      <!-- Mobile Nav Links -->
      <div class="space-y-1 py-1">
        <a href="#" class="block py-2 px-3 rounded-lg hover:bg-gray-50 text-gray-600 font-medium">Deals</a>

        <a href="#" class="block py-2 px-3 rounded-lg hover:bg-gray-50 text-gray-600 font-medium">Support</a>
        <RouterLink :to="{ name: 'ProuctCreate' }"
          class="text-gray-600 hover:text-blue-600 font-medium transition duration-200">
          New
        </RouterLink>
      </div>

      <!-- Cart in Mobile Menu -->
      <div class="flex items-center py-2 px-3 rounded-lg hover:bg-gray-50">
        <svg class="w-4 h-4 sm:w-5 sm:h-5 mr-2 sm:mr-3 text-gray-600" fill="none" stroke="currentColor"
          viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 3h2l.4 2M7 13h10l1.4-7H6.6M7 13L5.2 6H3m4 7l1.5 6h9l1.5-6M10 17a2 2 0 100 4 2 2 0 000-4zm7 0a2 2 0 100 4 2 2 0 000-4z" />
        </svg>
        <span class="text-sm sm:text-base">Cart (2)</span>
      </div>

      <!-- Mobile Auth Buttons -->
      <div class="grid grid-cols-2 gap-2 pt-1">
        <button
          class="border border-gray-300 w-full px-3 py-2 rounded-lg text-sm font-medium hover:bg-gray-50 transition duration-200 focus:outline-none focus:ring-2 focus:ring-blue-300">Login</button>
        <button
          class="bg-blue-600 text-white w-full px-3 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition duration-200 shadow-sm hover:shadow focus:outline-none focus:ring-2 focus:ring-blue-500">Sign
          Up</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out forwards;
}

/* Custom breakpoint */
@media (min-width: 480px) {
  .xs\:inline {
    display: inline;
  }

  .xs\:hidden {
    display: none;
  }
}
</style>
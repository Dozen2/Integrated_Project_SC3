<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import BrandDropdown from './../BrandComponents/BrandDropdown.vue'

const isMenuOpen = ref(false)
</script>

<template>
  <div class="bg-white h-20 flex flex-row items-center px-4 justify-between">

    <!-- Logo -->
    <RouterLink to="/" class="flex flex-row items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
      </svg>
      <h1 class="font-mono font-bold">ITBMS_SHOP</h1>
    </RouterLink>

    <!-- Search -->
    <div class="relative">
      <input type="text" placeholder="Search ..." class="w-[400px] px-4 py-2 border rounded-full focus:outline-none">
      <button class="absolute inset-y-0 right-2 flex items-center text-gray-500">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </button>
    </div>

    <!-- Menu -->
    <div class="font-mono flex flex-row gap-5">
      <span>shop</span>
      <span>new</span>
      <span>phon</span>
      <span>IT</span>
      <span>about</span>
    </div>

    <!-- Icons -->
    <div class="flex flex-row gap-4">
      <svg xmlns="http://www.w3.org/2000/svg" width="35px" height="35px" fill="currentColor" class="bi bi-person-fill"
        viewBox="0 0 16 16">
        <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
      </svg>
      <svg xmlns="http://www.w3.org/2000/svg" height="35px" width="35px" viewBox="0 -960 960 960" fill="#000000">
        <path
          d="M280-80q-33 0-56.5-23.5T200-160q0-33 23.5-56.5T280-240q33 0 56.5 23.5T360-160q0 33-23.5 56.5T280-80Zm400 0q-33 0-56.5-23.5T600-160q0-33 23.5-56.5T680-240q33 0 56.5 23.5T760-160q0 33-23.5 56.5T680-80ZM246-720l96 200h280l110-200H246Zm-38-80h590q23 0 35 20.5t1 41.5L692-482q-11 20-29.5 31T622-440H324l-44 80h480v80H280q-45 0-68-39.5t-2-78.5l54-98-144-304H40v-80h130l38 80Zm134 280h280-280Z" />
      </svg>
    </div>

  </div>
</template>


<style scoped></style>
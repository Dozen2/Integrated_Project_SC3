<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import Search from './Query/Search.vue';

const handleSearch = (keyword) => {
  console.log("ค้นหา:", keyword)
}

let showMenu = ref(false)
const toggleMenu = () => {
    showMenu.value = true
}

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

    <!-- Icons -->
    <div class="flex flex-row gap-4">

      <div class="relative inline-block">
        <!-- ปุ่มหลัก -->
        <button @click="toggleMenu" class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700">
          Menu
        </button>

        <!-- เมนู Login/Signup -->
        <div v-if="showMenu" class="absolute mt-2 w-40 bg-white border rounded-lg shadow-lg p-2 flex flex-col gap-2">
          <button class="w-full px-4 py-2 rounded-lg bg-green-500 text-white hover:bg-green-600">
            Login
          </button>
          <button class="w-full px-4 py-2 rounded-lg bg-indigo-500 text-white hover:bg-indigo-600">
            Signup
          </button>
        </div>
      </div>

      <svg xmlns="http://www.w3.org/2000/svg" height="35px" width="35px" viewBox="0 -960 960 960" fill="#000000">
        <path
          d="M280-80q-33 0-56.5-23.5T200-160q0-33 23.5-56.5T280-240q33 0 56.5 23.5T360-160q0 33-23.5 56.5T280-80Zm400 0q-33 0-56.5-23.5T600-160q0-33 23.5-56.5T680-240q33 0 56.5 23.5T760-160q0 33-23.5 56.5T680-80ZM246-720l96 200h280l110-200H246Zm-38-80h590q23 0 35 20.5t1 41.5L692-482q-11 20-29.5 31T622-440H324l-44 80h480v80H280q-45 0-68-39.5t-2-78.5l54-98-144-304H40v-80h130l38 80Zm134 280h280-280Z" />
      </svg>
    </div>

  </div>
</template>


<style scoped></style>
<script setup>
import { ref } from "vue"

const searchQuery = ref("")   // state เก็บค่าที่พิมพ์

const emit = defineEmits(["search"]) // event ส่งค่าออกไป

const handleSearch = () => {
  emit("search", searchQuery.value.trim())
}
</script>

<template>
  <div class="relative">
    <input
      v-model="searchQuery"
      type="text"
      placeholder="Search ..."
      class="w-[400px] px-4 py-2 border rounded-full focus:outline-none"
    />
    <button
      class="absolute inset-y-0 right-2 flex items-center text-gray-500"
      @click="handleSearch"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
        viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
    </button>
  </div>
</template>

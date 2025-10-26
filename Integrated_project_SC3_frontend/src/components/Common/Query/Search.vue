<script setup>
import { ref, watch } from "vue"



const props = defineProps({
  initialValue: { type: String, default: "" }
})

const emit = defineEmits(["search"])

const searchQuery = ref(props.initialValue)

watch(() => props.initialValue, (newVal) => {
  searchQuery.value = newVal
})

const handleSearch = () => {
  emit("search", searchQuery.value.trim())
}

const clearSearch = () => {
  searchQuery.value = ""
  emit("search", "") 
}
</script>

<template>
  <div class="flex items-center w-full md:w-[400px] relative">
    <input 
      v-model="searchQuery" 
      type="text" 
      placeholder="Search ..."
      @keyup.enter="handleSearch"
      class="w-full pr-20 pl-4 py-2 border rounded-full focus:outline-none"
    />

    <button 
      v-if="searchQuery" 
      class="absolute right-10 text-red-600 hover:text-gray-500"
      @click="clearSearch">
      ✕
    </button>

    <button 
      class="absolute right-3 text-gray-500 hover:text-gray-700"
      @click="handleSearch">
      <svg xmlns="http://www.w3.org/2000/svg" 
        class="h-5 w-5" fill="none" 
        viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
    </button>
  </div>
</template>


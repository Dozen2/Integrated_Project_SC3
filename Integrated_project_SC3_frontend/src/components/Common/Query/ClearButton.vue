<script setup>
import { defineProps, defineEmits } from "vue";

const props = defineProps({
  sessionKeys: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(["cleared"]);

function clearFilters() {
  props.sessionKeys.forEach(key => {
    sessionStorage.removeItem(key);
  });
  emit("cleared"); // ให้ parent ไปรัน fetch ใหม่
}
</script>

<template>
  <button
    @click="clearFilters"
    class="px-3 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors duration-200 whitespace-nowrap"
    title="Clear all filters"
  >
    Clear All
  </button>
</template>

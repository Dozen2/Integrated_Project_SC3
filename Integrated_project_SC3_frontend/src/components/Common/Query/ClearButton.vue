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
    class="px-3 py-2 text-white bg-red-500 hover:bg-red-600 cursor-pointer rounded-lg transition-colors duration-200 whitespace-nowrap"
    title="Clear all filters"
  >
    Clear all fiter
  </button>
</template>

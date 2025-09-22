<script setup>
import { BrushCleaning } from "lucide-vue-next";
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
    class="px-3 py-2 text-white bg-blue-500 hover:bg-blue-600 cursor-pointer rounded-lg transition-colors duration-200 whitespace-nowrap"
    title="Clear all filters"
  >
     <span class="flex"><BrushCleaning strokeWidth={1.5} />&nbsp;Clear all </span>
  </button>
</template>

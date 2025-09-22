<script setup>
import { RouterLink } from "vue-router";

const props = defineProps({
  pathForBreadcrumb: {
    type: Array,
    required: true,
    // ตัวอย่างแต่ละอัน: { text: "SaleItem Manage", name: "ProductManage" }
  }
});
</script>

<template>
  <nav class="flex items-center space-x-2 text-gray-600 text-sm">
    <template v-for="(bc, index) in props.pathForBreadcrumb" :key="bc.name">
      <RouterLink
        v-if="index !== props.pathForBreadcrumb.length - 1"
        :to="{ name: bc.name }"
        class="hover:underline"
      >
        {{ bc.text }}
      </RouterLink>

      <span
        v-else
        class="font-semibold text-gray-900"
      >
        {{ bc.text }}
      </span>

      <span v-if="index !== props.pathForBreadcrumb.length - 1">›</span>
    </template>
  </nav>
</template>

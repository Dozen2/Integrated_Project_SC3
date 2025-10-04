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
  <nav
  >
    <template v-for="(bc, index) in props.pathForBreadcrumb" :key="bc.name">
      <RouterLink
        v-if="index !== props.pathForBreadcrumb.length - 1"
        :to="{ name: bc.name }"
        class="hover:text-blue-800 text-2xl transition duration-200"
      >
        {{ bc.text }}
      </RouterLink>

      <span
        v-else
        class="text-blue-900 font-bold text-2xl"
      >
        {{ bc.text }}
      </span>

      <span
        v-if="index !== props.pathForBreadcrumb.length - 1"
        class="text-blue-400 text-2xl px-1"
      >
        ›
      </span>
    </template>
  </nav>
</template>

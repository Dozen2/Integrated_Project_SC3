<script setup>
import SelectAllBrandList from "@/components/BrandComponents/BrandSelectAllList.vue";
import { ref, onMounted,onBeforeUnmount } from "vue";
import {getAllBrand} from "@/libs/callAPI/apiBrand.js"

const brand = ref([]);

const fetchBrands = async () => {
  try {
    const brandData = await getAllBrand()
    brand.value = brandData;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

function onStorageChange(event) {
  if (event.key === 'brand-updated') {
    console.log('Brand data changed in another tab');
    fetchBrands(); // โหลดข้อมูลใหม่
  }
}

onMounted(() => {
  fetchBrands();
  window.addEventListener('storage', onStorageChange);
});
onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorageChange);
});
</script>

<template>
  <SelectAllBrandList :brand="brand" @brandDeleted="fetchBrands" />
</template>

<script setup>
import { computed, defineEmits, defineProps, ref, watch } from "vue";
import FilterBrand from "./Skeleton/FilterBrand.vue";
import SizeAndSort from "./Skeleton/SizeAndSort.vue";
import Pagination from "./Skeleton/Pagination.vue";

const props = defineProps({
  productTotalPages: Number,
  initialPage: Number,
  initialSize: Number,
  initialFilterBrands: String,
  initialSortField: String,
  initialSortDirection: String,
  showFilter: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true },
  showSizeAndSort: { type: Boolean, default: true },
});

const emit = defineEmits(["urlSetting"]);

// State variables
const filterBrands = ref(props.initialFilterBrands || "");
const page = ref(props.initialPage || 1);
const itbmPage = ref((props.initialPage || 1) - 1);
const size = ref(props.initialSize || 10);
const sortField = ref(props.initialSortField || "id");
const sortDirection = ref(props.initialSortDirection || "asc");

// Watch for prop changes to keep state in sync
watch(
  [() => props.initialFilterBrands, () => props.initialPage, () => props.initialSize, () => props.initialSortField, () => props.initialSortDirection],
  ([newFilterBrands, newPage, newSize, newSortField, newSortDirection]) => {
    if (newFilterBrands !== undefined) filterBrands.value = newFilterBrands || "";
    if (newPage !== undefined) {
      page.value = newPage || 1;
      itbmPage.value = (newPage || 1) - 1;
    }
    if (newSize !== undefined) size.value = newSize || 10;
    if (newSortField !== undefined) sortField.value = newSortField || "id";
    if (newSortDirection !== undefined) sortDirection.value = newSortDirection || "asc";
  }
);

const settings = computed(() => ({
  filterBrands: filterBrands.value,
  page: itbmPage.value,
  size: size.value,
  sortField: sortField.value,
  sortDirection: sortDirection.value,
}));

const emitUrlSetting = () => {
  emit("urlSetting", settings.value);
};

// Handle brand filter changes
const handleBrandFilterChanged = (newFilterBrands) => {
  filterBrands.value = newFilterBrands;
  // Reset to first page when filter changes
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

// Handle size changes
const handleSizeChanged = (newSize) => {
  size.value = newSize;
  // Reset to first page when size changes
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

// Handle sort changes
const handleSortChanged = (sortData) => {
  sortField.value = sortData.sortField;
  sortDirection.value = sortData.sortDirection;
  // Reset to first page when sort changes
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

// Handle page changes
const handlePageChanged = (pageData) => {
  itbmPage.value = pageData.page;
  page.value = pageData.page + 1;

  // Update all settings if provided (for sessionStorage compatibility)
  if (pageData.sortField) {
    sortField.value = pageData.sortField;
  }
  if (pageData.sortDirection) {
    sortDirection.value = pageData.sortDirection;
  }
  if (pageData.filterBrands !== undefined) {
    filterBrands.value = pageData.filterBrands;
  }
  if (pageData.size) {
    size.value = pageData.size;
  }

  emitUrlSetting();
};
</script>

<template>
  <div v-if="showFilter || showSizeAndSort" class="p-4 space-y-6 text-sm text-gray-800 max-w-6xl mx-auto">
    <!-- Filter Section - Horizontal Layout -->
    <div
      class="flex items-center justify-between bg-white p-4 rounded-lg shadow-sm border"
    >
      <div v-if="showFilter">
        <!-- Left Side - Brand Filter -->
        <FilterBrand
          :initialFilterBrands="initialFilterBrands"
          @brandFilterChanged="handleBrandFilterChanged"
        />
      </div>

      <!-- Right Side - Size and Sort Controls -->
      <div v-if="showSizeAndSort">
        <SizeAndSort
          :initialSize="initialSize"
          :initialSortField="initialSortField"
          :initialSortDirection="initialSortDirection"
          @sizeChanged="handleSizeChanged"
          @sortChanged="handleSortChanged"
        />
      </div>
    </div>
  </div>

  <!-- Pagination Section -->
  <div v-if="showPagination">
    <Pagination
      :productTotalPages="productTotalPages"
      :initialPage="initialPage"
      @pageChanged="handlePageChanged"
    />
  </div>
</template>
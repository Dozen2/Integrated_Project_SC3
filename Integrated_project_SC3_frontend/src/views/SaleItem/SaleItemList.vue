<script setup>
import { ref, onBeforeUnmount, onBeforeMount } from "vue";
import {
  getAllSaleItemV1,
  getAllSaleItemV2,
} from "@/libs/callAPI/apiSaleItem.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand.js";
import SelectAllSaleItemGallery from "@/components/SaleItemComponent/SaleItemSelectAllGallery.vue";
import { useAlertStore } from "@/stores/alertStore.js";
import Filter from "@/components/Common/Query/Filter.vue";
import SizeAndSort from "@/components/Common/Query/SizeAndSort.vue";
import Pagination from "@/components/Common/Query/Pagination.vue";

const product = ref([]);
const brand = ref([]);
const initialTotalPages = ref(0);
const alertStore = useAlertStore();

// Session storage keys
const SESSION_KEYS = {
  FILTER_BRAND: "SaleItem-FilterBrand",
  PAGE: "SaleItem-Page",
  SIZE: "SaleItem-Size",
  SORT_DIRECTION: "SaleItem-SortDirection",
  SORT_FIELD: "SaleItem-SortField"
};

// Get individual session storage values
const getSessionValue = (key, defaultValue = null) => {
  const raw = sessionStorage.getItem(key);
  if (raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      return raw; // Return as string if not JSON
    }
  }
  return defaultValue;
};

// Set individual session storage values
const setSessionValue = (key, value) => {
  if (typeof value === 'object') {
    sessionStorage.setItem(key, JSON.stringify(value));
  } else {
    sessionStorage.setItem(key, value);
  }
};

// Get current settings from session storage
const getCurrentSettings = () => {
  return {
    filterBrands: getSessionValue(SESSION_KEYS.FILTER_BRAND, ""),
    page: getSessionValue(SESSION_KEYS.PAGE, 0),
    size: getSessionValue(SESSION_KEYS.SIZE, 10),
    sortDirection: getSessionValue(SESSION_KEYS.SORT_DIRECTION, "desc"),
    sortField: getSessionValue(SESSION_KEYS.SORT_FIELD, "createdOn")
  };
};

const fetchBrandData = async () => {
  try {
    const brandData = await getAllBrand();
    brand.value = brandData.sort((a, b) => a.name.localeCompare(b.name));
    console.log("Fetched brand data:", brandData);
  } catch (error) {
    console.error("Error fetching brand data:", error);
  }
};

const fetchProduct = async () => {
  try {
    const productData = await getAllSaleItemV2([], "createdOn", "desc", 10, 0);

    product.value = productData;
    initialTotalPages.value = productData.totalPages;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const fetchProductWithSettings = async (settings) => {
  try {
    let filterBrands = [];
    if (settings.filterBrands && settings.filterBrands.trim() !== "") {
      filterBrands = settings.filterBrands
        .split(",")
        .map((brand) => brand.trim());
    }

    const productData = await getAllSaleItemV2(
      filterBrands,
      settings.sortField || "createdOn",
      settings.sortDirection || "desc",
      settings.size || 10,
      settings.page || 0
    );

    console.log("Fetched product data:", productData);

    if (productData && productData.content && filterBrands.length > 0) {
      const brandOrder = filterBrands;
      console.log("Brand order:", brandOrder);

      productData.content.sort((a, b) => {
        const brandA = a.brandName?.trim() || a.brand?.name?.trim() || "";
        const brandB = b.brandName?.trim() || b.brand?.name?.trim() || "";

        const indexA = brandOrder.indexOf(brandA);
        const indexB = brandOrder.indexOf(brandB);

        if (indexA !== -1 && indexB !== -1) {
          return indexA - indexB;
        }
        if (indexA !== -1 && indexB === -1) return -1;
        if (indexA === -1 && indexB !== -1) return 1;
        return brandA.localeCompare(brandB);
      });

      console.log(
        "Sorted products:",
        productData.content.map((p) => p.brandName || p.brand?.name)
      );
    }

    product.value = productData;
    initialTotalPages.value = productData.totalPages;
  } catch (error) {
    console.error("Error fetching product data:", error);
  }
};

// Handle brand filter changes
const handleBrandFilterChanged = async (newFilterBrands) => {
  setSessionValue(SESSION_KEYS.FILTER_BRAND, newFilterBrands);
  setSessionValue(SESSION_KEYS.PAGE, 0); // Reset to first page
  
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// Handle size changes
const handleSizeChanged = async (newSize) => {
  setSessionValue(SESSION_KEYS.SIZE, newSize);
  setSessionValue(SESSION_KEYS.PAGE, 0); // Reset to first page
  
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// Handle sort changes
const handleSortChanged = async (sortData) => {
  setSessionValue(SESSION_KEYS.SORT_FIELD, sortData.sortField);
  setSessionValue(SESSION_KEYS.SORT_DIRECTION, sortData.sortDirection);
  setSessionValue(SESSION_KEYS.PAGE, 0); // Reset to first page
  
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// Handle page changes
const handlePageChanged = async (pageData) => {
  setSessionValue(SESSION_KEYS.PAGE, pageData.page);
  
  // Update other settings if provided
  if (pageData.sortField) {
    setSessionValue(SESSION_KEYS.SORT_FIELD, pageData.sortField);
  }
  if (pageData.sortDirection) {
    setSessionValue(SESSION_KEYS.SORT_DIRECTION, pageData.sortDirection);
  }
  if (pageData.filterBrands !== undefined) {
    setSessionValue(SESSION_KEYS.FILTER_BRAND, pageData.filterBrands);
  }
  if (pageData.size) {
    setSessionValue(SESSION_KEYS.SIZE, pageData.size);
  }
  
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

const onStorageChange = (event) => {
  if (event.key === "product-updated") {
    fetchProduct();
    fetchBrandData(); // Also refresh brand data if needed
  }
};

onBeforeMount(async () => {
  // Load brand data first
  await fetchBrandData();
  
  const loadedSettings = getCurrentSettings();
  
  // Check if any settings exist, if not use defaults
  const hasSettings = Object.values(loadedSettings).some(value => 
    value !== null && value !== "" && value !== 0
  );
  
  if (hasSettings) {
    await fetchProductWithSettings(loadedSettings);
  } else {
    await fetchProduct();
  }
  
  window.addEventListener("storage", onStorageChange);
});

onBeforeUnmount(() => {
  window.removeEventListener("storage", onStorageChange);
});
</script>

<template>
  <div class="flex flex-col gap-4 mx-[225px] mt-[50px]">
    <div v-if="alertStore.message">
      <div
        :class="`itbms-message px-4 py-2 rounded ${
          alertStore.type === 'error'
            ? 'bg-red-100 text-red-700'
            : 'bg-green-100 text-green-700'
        }`"
      >
        {{ alertStore.message }}
      </div>
    </div>

    <div class="flex items-center justify-between gap-4">
      <RouterLink
        :to="{ name: 'ProuctCreate' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        <span class="itbms-sale-item-add tracking-wide">New Product</span>
      </RouterLink>

      <RouterLink
        :to="{ name: 'ProductManage' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        <span class="itbms-manage-brand tracking-wide">Manage Sale Items</span>
      </RouterLink>
    </div>

    <!-- Brand Filter Component -->
    <Filter
      :initialFilterValues="getSessionValue(SESSION_KEYS.FILTER_BRAND, '')"
      :options="brand"
      label="Filter by brand(s)"
      placeholder="-- เลือกแบรนด์ --"
      :sessionKey="SESSION_KEYS.FILTER_BRAND"
      valueField="name"
      displayField="name"
      @filterChanged="handleBrandFilterChanged"
    />

    <!-- Size and Sort Component -->
    <SizeAndSort
      :initialSize="getSessionValue(SESSION_KEYS.SIZE, 10)"
      :initialSortField="getSessionValue(SESSION_KEYS.SORT_FIELD, '')"
      :initialSortDirection="getSessionValue(SESSION_KEYS.SORT_DIRECTION, '')"
      @sizeChanged="handleSizeChanged"
      @sortChanged="handleSortChanged"
    />

    <!-- Product Gallery -->
    <SelectAllSaleItemGallery
      v-if="product?.content"
      :product="product.content"
    />

    <!-- Pagination Component -->
    <Pagination
      :initialTotalPages="initialTotalPages"
      :initialPage="getSessionValue(SESSION_KEYS.PAGE, 0) + 1"
      @pageChanged="handlePageChanged"
    />
  </div>
</template>
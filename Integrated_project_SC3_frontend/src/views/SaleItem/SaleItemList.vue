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
import ClearButton from "@/components/Common/Query/ClearButton.vue";

// ======================== Reactive States ========================
const product = ref([]);
const brand = ref([]);
const totalPages = ref(0);
const alertStore = useAlertStore();

// ======================== Configuration ========================
const STORAGE_OPTIONS = [
  { id: 1, name: "32GB", value: "32" },
  { id: 2, name: "64GB", value: "64" },
  { id: 3, name: "128GB", value: "128" },
  { id: 4, name: "256GB", value: "256" },
  { id: 5, name: "512GB", value: "512" },
  { id: 6, name: "1TB", value: "1024" },
  { id: 7, name: "Not specified", value: "-1" },
];

const PRICE_OPTIONS = [
  { id: 1, name: "0 – 5,000 Baht", value: "0-5000" },
  { id: 2, name: "5,001-10,000 Baht", value: "5001-10000" },
  { id: 3, name: "10,001-20,000 Baht", value: "10001-20000" },
  { id: 4, name: "20,001-30,000 Baht", value: "20001-30000" },
  { id: 5, name: "30,001-40,000 Baht", value: "30001-40000" },
  { id: 6, name: "40,001-50,000 Baht", value: "40001-50000" },
];

const SESSION_KEYS = {
  BRAND: "SaleItem-FilterBrand",
  STORAGE: "SaleItem-FilterStorage",
  PRICE: "SaleItem-FilterPrice",
  PAGE: "SaleItem-Page",
  SIZE: "SaleItem-Size",
  SORT_DIRECTION: "SaleItem-SortDirection",
  SORT_FIELD: "SaleItem-SortField",
};

const DEFAULT_VALUES = {
  page: 0,
  size: 10,
  sortDirection: "desc",
  sortField: "createdOn",
};

// ======================== Session Storage Helpers ========================
const getSessionArray = (key) => {
  try {
    const value = sessionStorage.getItem(key);
    if (!value) return [];

    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) {
      return parsed.filter((item) => item && item.toString().trim() !== "");
    }
    return [];
  } catch {
    return [];
  }
};

const getSessionValue = (key, defaultValue) => {
  try {
    const value = sessionStorage.getItem(key);
    return value ? JSON.parse(value) : defaultValue;
  } catch {
    return defaultValue;
  }
};

const setSession = (key, value) => {
  sessionStorage.setItem(key, JSON.stringify(value));
};

const getCurrentFilters = () => ({
  brands: getSessionArray(SESSION_KEYS.BRAND),
  storages: getSessionArray(SESSION_KEYS.STORAGE),
  prices: getSessionArray(SESSION_KEYS.PRICE),
  page: getSessionValue(SESSION_KEYS.PAGE, DEFAULT_VALUES.page),
  size: getSessionValue(SESSION_KEYS.SIZE, DEFAULT_VALUES.size),
  sortDirection: getSessionValue(
    SESSION_KEYS.SORT_DIRECTION,
    DEFAULT_VALUES.sortDirection
  ),
  sortField: getSessionValue(SESSION_KEYS.SORT_FIELD, DEFAULT_VALUES.sortField),
});

// ======================== Data Processing Helpers ========================
const convertStorageValues = (storageNames) => {
  return storageNames.map((name) => {
    const option = STORAGE_OPTIONS.find((opt) => opt.name === name);
    return option ? option.value : name;
  });
};

// const convertPriceValues = (priceNames) => {
//   return priceNames.map((name) => {
//     const option = PRICE_OPTIONS.find((opt) => opt.name === name);
//     return option ? option.value : name;
//   });
// };

// const parsePriceRange = (priceValues) => {
//   if (!priceValues.length) return { min: null, max: null };

//   let min = null;
//   let max = null;

//   priceValues.forEach((range) => {
//     const [lower, upper] = range.split("-").map(Number);
//     if (!isNaN(lower)) min = min === null ? lower : Math.min(min, lower);
//     if (!isNaN(upper)) max = max === null ? upper : Math.max(max, upper);
//   });

//   return { min, max };
// };

// แก้ไขฟังก์ชัน convertPriceValues
const convertPriceValues = (priceNames) => {
  return priceNames.map((name) => {
    // จัดการ custom price
    if (name.includes('custom:')) {
      return name.replace('custom:', '');
    }
    
    const option = PRICE_OPTIONS.find((opt) => opt.name === name);
    return option ? option.value : name;
  });
};

// แก้ไขฟังก์ชัน parsePriceRange
const parsePriceRange = (priceValues) => {
  if (!priceValues.length) return { min: null, max: null };

  let min = null;
  let max = null;

  // priceValues.forEach((range) => {
  //   const [lower, upper] = range.split("-").map(Number);
  //   if (!isNaN(lower)) min = min === null ? lower : Math.min(min, lower);
  //   if (!isNaN(upper)) max = max === null ? upper : Math.max(max, upper);

  // });

    priceValues.forEach((range) => {
    // ตรวจสอบว่ามี "-" หรือไม่
    if (range.includes('-')) {
      // กรณี range เช่น "1000-2000"
      const [lower, upper] = range.split("-").map(Number);
      if (!isNaN(lower)) min = min === null ? lower : Math.min(min, lower);
      if (!isNaN(upper)) max = max === null ? upper : Math.max(max, upper);
    } else {
      // กรณีค่าเดี่ยว เช่น "1000"
      const singleValue = Number(range);
      if (!isNaN(singleValue)) {
        min = min === null ? singleValue : Math.min(min, singleValue);
        max = max === null ? singleValue : Math.max(max, singleValue);
      }
    }
  });

  return { min, max };
};

const sortProductsByBrand = (products, brandOrder) => {
  return products.sort((a, b) => {
    const brandA = a.brandName?.trim() || a.brand?.name?.trim() || "";
    const brandB = b.brandName?.trim() || b.brand?.name?.trim() || "";

    const indexA = brandOrder.indexOf(brandA);
    const indexB = brandOrder.indexOf(brandB);

    if (indexA !== -1 && indexB !== -1) return indexA - indexB;
    if (indexA !== -1) return -1;
    if (indexB !== -1) return 1;
    return brandA.localeCompare(brandB);
  });
};

// ======================== API Functions ========================
const loadBrands = async () => {
  try {
    const data = await getAllBrand();
    brand.value = data.sort((a, b) => a.name.localeCompare(b.name));
  } catch (error) {
    console.error("Error loading brands:", error);
  }
};

const loadProductsDefault = async () => {
  try {
    const data = await getAllSaleItemV2([], "createdOn", "desc", 10, 0);
    product.value = data;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Error loading products:", error);
  }
};

const loadProductsWithFilters = async (filters) => {
  try {
    // Convert filter values
    const storageValues = convertStorageValues(filters.storages);
    const priceValues = convertPriceValues(filters.prices);
    const { min: minPrice, max: maxPrice } = parsePriceRange(priceValues);

    // Fetch data
    const data = await getAllSaleItemV2(
      filters.brands,
      filters.sortField,
      filters.sortDirection,
      filters.size,
      filters.page,
      storageValues,
      minPrice,
      maxPrice
    );

    // Sort by brand order if brands are filtered
    if (data?.content && filters.brands.length > 0) {
      data.content = sortProductsByBrand(data.content, filters.brands);
    }

    product.value = data;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Error loading filtered products:", error);
  }
};

// ======================== Filter Event Handlers ========================
const handleBrandFilter = async (newBrands) => {
  setSession(SESSION_KEYS.BRAND, newBrands);
  setSession(SESSION_KEYS.PAGE, 0);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handleStorageFilter = async (newStorages) => {
  setSession(SESSION_KEYS.STORAGE, newStorages);
  setSession(SESSION_KEYS.PAGE, 0);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handlePriceFilter = async (newPrices) => {
  setSession(SESSION_KEYS.PRICE, newPrices);
  setSession(SESSION_KEYS.PAGE, 0);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

// ======================== Other Event Handlers ========================
const handleSizeChange = async (newSize) => {
  setSession(SESSION_KEYS.SIZE, newSize);
  setSession(SESSION_KEYS.PAGE, 0);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handleSortChange = async (sortData) => {
  setSession(SESSION_KEYS.SORT_FIELD, sortData.sortField);
  setSession(SESSION_KEYS.SORT_DIRECTION, sortData.sortDirection);
  setSession(SESSION_KEYS.PAGE, 0);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handlePageChange = async (pageData) => {
  setSession(SESSION_KEYS.PAGE, pageData.page);

  // Update other settings if provided
  if (pageData.sortField)
    setSession(SESSION_KEYS.SORT_FIELD, pageData.sortField);
  if (pageData.sortDirection)
    setSession(SESSION_KEYS.SORT_DIRECTION, pageData.sortDirection);
  if (pageData.filterBrands !== undefined)
    setSession(SESSION_KEYS.BRAND, pageData.filterBrands);
  if (pageData.size) setSession(SESSION_KEYS.SIZE, pageData.size);

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handleClear = async () => {
  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

// ======================== Storage Event Handler ========================
const onStorageChange = (event) => {
  if (event.key === "product-updated") {
    loadProductsDefault();
    loadBrands();
  }
};

// ======================== Utility Functions ========================
const hasActiveFilters = (filters) => {
  return (
    filters.brands.length > 0 ||
    filters.storages.length > 0 ||
    filters.prices.length > 0 ||
    filters.page > 0 ||
    filters.sortField !== DEFAULT_VALUES.sortField ||
    filters.sortDirection !== DEFAULT_VALUES.sortDirection ||
    filters.size !== DEFAULT_VALUES.size
  );
};

// ======================== Lifecycle ========================
onBeforeMount(async () => {
  await loadBrands();

  const filters = getCurrentFilters();

  if (hasActiveFilters(filters)) {
    await loadProductsWithFilters(filters);
  } else {
    await loadProductsDefault();
  }

  window.addEventListener("storage", onStorageChange);
});

onBeforeUnmount(() => {
  window.removeEventListener("storage", onStorageChange);
});
</script>

<template>
  <div class="flex flex-col gap-4 mx-[225px] mt-[50px]">
    <!-- Alert Message -->
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

    <!-- Action Buttons -->
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

    <!-- Filters -->
    <Filter
      :initialFilterValues="getSessionArray(SESSION_KEYS.BRAND)"
      :options="brand"
      label="Filter by brands"
      placeholder="Select brands"
      :sessionKey="SESSION_KEYS.BRAND"
      valueField="name"
      displayField="name"
      @filterChanged="handleBrandFilter"
    />

    <Filter
      :initialFilterValues="getSessionArray(SESSION_KEYS.STORAGE)"
      :options="STORAGE_OPTIONS"
      label="Filter by Storages"
      placeholder="Select StoragesGB"
      :sessionKey="SESSION_KEYS.STORAGE"
      valueField="name"
      displayField="name"
      @filterChanged="handleStorageFilter"
    />

    <!-- <Filter
      :initialFilterValues="getSessionArray(SESSION_KEYS.PRICE)"
      :options="PRICE_OPTIONS"
      label="Filter by Price"
      placeholder="Select Price Range"
      :sessionKey="SESSION_KEYS.PRICE"
      valueField="value"
      displayField="name"
      @filterChanged="handlePriceFilter"
      ><template #InputPrice></template> ></Filter
    > -->

    <!-- Filter by Price -->
    <Filter
      :initialFilterValues="getSessionArray(SESSION_KEYS.PRICE)"
      :options="PRICE_OPTIONS"
      label="Filter by Price"
      placeholder="Select Price Range"
      :sessionKey="SESSION_KEYS.PRICE"
      valueField="value"
      displayField="name"
      :isPriceFilter="true"
      @filterChanged="handlePriceFilter"
/>

    <!-- Clear Button -->
    <div class="flex justify-end mb-4">
      <ClearButton
        :sessionKeys="[
          SESSION_KEYS.BRAND,
          SESSION_KEYS.STORAGE,
          SESSION_KEYS.PRICE,
          SESSION_KEYS.PAGE,
        ]"
        @cleared="handleClear"
      />
    </div>

    <!-- Size and Sort -->
    <SizeAndSort
      :initialSize="getSessionValue(SESSION_KEYS.SIZE, DEFAULT_VALUES.size)"
      :initialSortField="getSessionValue(SESSION_KEYS.SORT_FIELD, '')"
      :initialSortDirection="getSessionValue(SESSION_KEYS.SORT_DIRECTION, '')"
      @sizeChanged="handleSizeChange"
      @sortChanged="handleSortChange"
    />

    <!-- Product Gallery -->
    <SelectAllSaleItemGallery
      v-if="product?.content"
      :product="product.content"
    />

    <!-- Pagination -->
    <Pagination
      :initialTotalPages="totalPages"
      :initialPage="getSessionValue(SESSION_KEYS.PAGE, DEFAULT_VALUES.page) + 1"
      @pageChanged="handlePageChange"
    />
  </div>
</template>

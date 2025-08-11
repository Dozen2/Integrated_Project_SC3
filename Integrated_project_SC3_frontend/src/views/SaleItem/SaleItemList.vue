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

// ======================== State Management ========================
const product = ref([]);
const brand = ref([]);
const initialTotalPages = ref(0);
const alertStore = useAlertStore();

// ======================== Configuration Data ========================
const storageOptions = [
  { id: 1, name: "32GB", value: "32" },
  { id: 2, name: "64GB", value: "64" },
  { id: 3, name: "128GB", value: "128" },
  { id: 4, name: "256GB", value: "256" },
  { id: 5, name: "512GB", value: "512" },
  { id: 6, name: "1TB", value: "1024" },
  { id: 7, name: "Not specified", value: "0" },
];

const priceOptions = [
  { id: 1, name: "0 – 5,000 Baht", value: "0-5000" },
  { id: 2, name: "5,001-10,000 Baht", value: "5001-10000" },
  { id: 3, name: "10,001-20,000 Baht", value: "10001-20000" },
  { id: 4, name: "20,001-30,000 Baht", value: "20001-30000" },
  { id: 5, name: "30,001-40,000 Baht", value: "30001-40000" },
  { id: 6, name: "40,001-50,000 Baht", value: "40001-50000" },
];

// ======================== Session Storage Management ========================
const SESSION_KEYS = {
  FILTER_BRAND: "SaleItem-FilterBrand",
  FILTER_STORAGE: "SaleItem-FilterStorage",
  FILTER_PRICE: "SaleItem-FilterPrice",
  PAGE: "SaleItem-Page",
  SIZE: "SaleItem-Size",
  SORT_DIRECTION: "SaleItem-SortDirection",
  SORT_FIELD: "SaleItem-SortField",
};

// Helper function to normalize values from session storage
const normalizeToArray = (value) => {
  if (Array.isArray(value)) {
    return value.filter(item => item && item.toString().trim() !== "");
  }
  if (typeof value === "string" && value.trim() !== "") {
    return value.split(",").map(item => item.trim()).filter(item => item !== "");
  }
  return [];
};

// อ่านค่าจาก session storage พร้อม error handling
const getSessionValue = (key, defaultValue = null) => {
  const raw = sessionStorage.getItem(key);
  if (raw) {
    try {
      const parsed = JSON.parse(raw);
      // If it's an array or string for filter keys, normalize to array
      if (key.includes("Filter")) {
        return normalizeToArray(parsed);
      }
      return parsed;
    } catch (e) {
      // If parsing fails and it's a filter key, try to normalize the raw string
      if (key.includes("Filter")) {
        return normalizeToArray(raw);
      }
      return raw;
    }
  }
  return defaultValue;
};

// บันทึกค่าลง session storage
const setSessionValue = (key, value) => {
  sessionStorage.setItem(key, JSON.stringify(value));
};

// ดึงการตั้งค่าปัจจุบันจาก session storage
const getCurrentSettings = () => {
  return {
    filterBrands: getSessionValue(SESSION_KEYS.FILTER_BRAND, []),
    filterStorages: getSessionValue(SESSION_KEYS.FILTER_STORAGE, []),
    filterPrice: getSessionValue(SESSION_KEYS.FILTER_PRICE, []),
    page: getSessionValue(SESSION_KEYS.PAGE, 0),
    size: getSessionValue(SESSION_KEYS.SIZE, 10),
    sortDirection: getSessionValue(SESSION_KEYS.SORT_DIRECTION, "desc"),
    sortField: getSessionValue(SESSION_KEYS.SORT_FIELD, "createdOn"),
  };
};

// ======================== Helper Functions ========================
// แยก price range จาก price filter array
const parsePriceFilters = (priceFilters) => {
  if (!Array.isArray(priceFilters) || priceFilters.length === 0) {
    return { filterPriceLower: null, filterPriceUpper: null };
  }

  // รวม price ranges ทั้งหมดแล้วหา min/max
  let minPrice = null;
  let maxPrice = null;

  priceFilters.forEach(priceRange => {
    const [lower, upper] = priceRange.split('-').map(p => parseInt(p));
    if (!isNaN(lower)) {
      minPrice = minPrice === null ? lower : Math.min(minPrice, lower);
    }
    if (!isNaN(upper)) {
      maxPrice = maxPrice === null ? upper : Math.max(maxPrice, upper);
    }
  });

  return {
    filterPriceLower: minPrice,
    filterPriceUpper: maxPrice
  };
};

// ======================== API Data Fetching ========================
// ดึงข้อมูล brand และเรียงตามชื่อ
const fetchBrandData = async () => {
  try {
    const brandData = await getAllBrand();
    brand.value = brandData.sort((a, b) => a.name.localeCompare(b.name));
  } catch (error) {
    console.error("Error fetching brand data:", error);
  }
};

// ดึงข้อมูลสินค้าแบบ default (ไม่มี filter)
const fetchProduct = async () => {
  try {
    const productData = await getAllSaleItemV2([], "createdOn", "desc", 10, 0);
    product.value = productData;
    initialTotalPages.value = productData.totalPages;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

// ดึงข้อมูลสินค้าตามการตั้งค่าที่กำหนด (มี filter, sort, pagination)
const fetchProductWithSettings = async (settings) => {
  try {
    // Convert storage filter values to match API expectation
    const filterStorageValues = Array.isArray(settings.filterStorages) 
      ? settings.filterStorages.map(storage => {
          const storageOption = storageOptions.find(opt => opt.name === storage);
          return storageOption ? storageOption.value : storage;
        })
      : [];

    // Parse price filters
    const priceFilters = Array.isArray(settings.filterPrice) 
      ? settings.filterPrice.map(price => {
          const priceOption = priceOptions.find(opt => opt.name === price);
          return priceOption ? priceOption.value : price;
        })
      : [];
    
    const { filterPriceLower, filterPriceUpper } = parsePriceFilters(priceFilters);

    // เรียก API พร้อมการตั้งค่า
    const productData = await getAllSaleItemV2(
      Array.isArray(settings.filterBrands) ? settings.filterBrands : [],
      settings.sortField || "createdOn",
      settings.sortDirection || "desc",
      settings.size || 10,
      settings.page || 0,
      filterStorageValues,
      filterPriceLower,
      filterPriceUpper
    );

    // เรียงลำดับสินค้าตาม brand filter ถ้ามี
    if (productData && productData.content && Array.isArray(settings.filterBrands) && settings.filterBrands.length > 0) {
      const brandOrder = settings.filterBrands;

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
    }

    product.value = productData;
    initialTotalPages.value = productData.totalPages;
  } catch (error) {
    console.error("Error fetching product data:", error);
  }
};

// ======================== Event Handlers ========================
// จัดการการเปลี่ยน brand filter - รับค่าเป็น array
const handleBrandFilterChanged = async (newFilterBrands) => {
  setSessionValue(SESSION_KEYS.FILTER_BRAND, newFilterBrands);
  setSessionValue(SESSION_KEYS.PAGE, 0);
  console.log("New Filter Brands (Array):", newFilterBrands);
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการเปลี่ยน storage filter - รับค่าเป็น array
const handleStorageFilterChanged = async (newFilterStorages) => {
  setSessionValue(SESSION_KEYS.FILTER_STORAGE, newFilterStorages);
  setSessionValue(SESSION_KEYS.PAGE, 0);
  console.log("New Filter Storages (Array):", newFilterStorages);

  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการเปลี่ยน price filter - รับค่าเป็น array
const handlePriceFilterChanged = async (newFilterPrice) => {
  setSessionValue(SESSION_KEYS.FILTER_PRICE, newFilterPrice);
  setSessionValue(SESSION_KEYS.PAGE, 0);
  console.log("New Filter Price (Array):", newFilterPrice);

  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการเปลี่ยนจำนวนรายการต่อหน้า
const handleSizeChanged = async (newSize) => {
  setSessionValue(SESSION_KEYS.SIZE, newSize);
  setSessionValue(SESSION_KEYS.PAGE, 0);

  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการล้าง filter ทั้งหมด
const handleClear = async () => {
  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการเปลี่ยนการเรียงลำดับ
const handleSortChanged = async (sortData) => {
  setSessionValue(SESSION_KEYS.SORT_FIELD, sortData.sortField);
  setSessionValue(SESSION_KEYS.SORT_DIRECTION, sortData.sortDirection);
  setSessionValue(SESSION_KEYS.PAGE, 0);

  const settings = getCurrentSettings();
  await fetchProductWithSettings(settings);
};

// จัดการการเปลี่ยนหน้า
const handlePageChanged = async (pageData) => {
  setSessionValue(SESSION_KEYS.PAGE, pageData.page);

  // อัพเดทการตั้งค่าอื่นๆ ถ้ามี
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

// ฟังการเปลี่ยนแปลงใน storage (สำหรับ sync ระหว่าง tab)
const onStorageChange = (event) => {
  if (event.key === "product-updated") {
    fetchProduct();
    fetchBrandData();
  }
};

// ======================== Lifecycle Hooks ========================
onBeforeMount(async () => {
  // โหลดข้อมูล brand ก่อน
  await fetchBrandData();

  // ดึงการตั้งค่าจาก session storage
  const loadedSettings = getCurrentSettings();

  // ตรวจสอบว่ามีการตั้งค่าอยู่หรือไม่
  const hasSettings = 
    (Array.isArray(loadedSettings.filterBrands) && loadedSettings.filterBrands.length > 0) ||
    (Array.isArray(loadedSettings.filterStorages) && loadedSettings.filterStorages.length > 0) ||
    (Array.isArray(loadedSettings.filterPrice) && loadedSettings.filterPrice.length > 0) ||
    loadedSettings.page > 0 ||
    (loadedSettings.sortField && loadedSettings.sortField !== "createdOn") ||
    (loadedSettings.sortDirection && loadedSettings.sortDirection !== "desc") ||
    (loadedSettings.size && loadedSettings.size !== 10);

  // ถ้ามีการตั้งค่า ใช้การตั้งค่านั้น ถ้าไม่มี ใช้ค่า default
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
    <!-- แสดง Alert Message -->
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
      <!-- ปุ่มเพิ่มสินค้าใหม่ -->
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

      <!-- ปุ่มจัดการสินค้า -->
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

    <!-- Brand Filter -->
    <Filter
      :initialFilterValues="getSessionValue(SESSION_KEYS.FILTER_BRAND, [])"
      :options="brand"
      label="Filter by brands"
      placeholder="Select brands"
      :sessionKey="SESSION_KEYS.FILTER_BRAND"
      valueField="name"
      displayField="name"
      @filterChanged="handleBrandFilterChanged"
    />

    <!-- Storage Filters -->
    <Filter
      :initialFilterValues="getSessionValue(SESSION_KEYS.FILTER_STORAGE, [])"
      :options="storageOptions"
      label="Filter by Storages"
      placeholder="Select StoragesGB"
      :sessionKey="SESSION_KEYS.FILTER_STORAGE"
      valueField="name"
      displayField="name"
      @filterChanged="handleStorageFilterChanged"
    />

    <!-- Price Filters -->
    <Filter
      :initialFilterValues="getSessionValue(SESSION_KEYS.FILTER_PRICE, [])"
      :options="priceOptions"
      label="Filter by Price"
      placeholder="Select Price Range"
      :sessionKey="SESSION_KEYS.FILTER_PRICE"
      valueField="name"
      displayField="name"
      @filterChanged="handlePriceFilterChanged"
    />

    <div class="flex justify-end mb-4">
      <ClearButton
        :sessionKeys="[
          SESSION_KEYS.FILTER_BRAND,
          SESSION_KEYS.FILTER_STORAGE,
          SESSION_KEYS.FILTER_PRICE,
          SESSION_KEYS.PAGE,
        ]"
        @cleared="handleClear"
      />
    </div>

    <!-- Size and Sort Controls -->
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

    <!-- Pagination -->
    <Pagination
      :initialTotalPages="initialTotalPages"
      :initialPage="getSessionValue(SESSION_KEYS.PAGE, 0) + 1"
      @pageChanged="handlePageChanged"
    />
  </div>
</template>
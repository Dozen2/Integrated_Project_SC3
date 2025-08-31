<script setup>
import { ref, onBeforeUnmount, onBeforeMount } from "vue";
import {
  getAllSaleItemV2,
  getImageByImageName,
  getViewStorageForSelect,
} from "@/libs/callAPI/apiSaleItem.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand.js";
import SelectAllSaleItemGallery from "@/components/SaleItemComponent/SaleItemSelectAllGallery.vue";
import { useAlertStore } from "@/stores/alertStore.js";
import Filter from "@/components/Common/Query/Filter.vue";
import SizeAndSort from "@/components/Common/Query/SizeAndSort.vue";
import Pagination from "@/components/Common/Query/Pagination.vue";
import ClearButton from "@/components/Common/Query/ClearButton.vue";
import Search from "@/components/Common/Query/Search.vue";
import { Funnel } from "lucide-vue-next";

// ======================== Reactive States ========================
const product = ref([]);
const brand = ref([]);
const totalPages = ref(0);
const alertStore = useAlertStore();

// Custom price range state
const customPriceRange = ref({ min: null, max: null });

// ======================== Configuration ========================

const STORAGE_OPTIONS = ref([]);
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
  SEARCH: "SaleItem-Search",
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

// Get custom price from session storage
const getSessionCustomPrice = () => {
  try {
    const min = sessionStorage.getItem(SESSION_KEYS.CUSTOM_PRICE_MIN);
    const max = sessionStorage.getItem(SESSION_KEYS.CUSTOM_PRICE_MAX);
    return {
      min: min && min !== "" ? parseFloat(min) : null,
      max: max && max !== "" ? parseFloat(max) : null,
    };
  } catch {
    return { min: null, max: null };
  }
};

const getCurrentFilters = () => ({
  brands: getSessionArray(SESSION_KEYS.BRAND),
  storages: getSessionArray(SESSION_KEYS.STORAGE),
  prices: getSessionArray(SESSION_KEYS.PRICE),
  customPrice: getSessionCustomPrice(),
  page: getSessionValue(SESSION_KEYS.PAGE, DEFAULT_VALUES.page),
  size: getSessionValue(SESSION_KEYS.SIZE, DEFAULT_VALUES.size),
  sortDirection: getSessionValue(
    SESSION_KEYS.SORT_DIRECTION,
    DEFAULT_VALUES.sortDirection
  ),
  sortField: getSessionValue(SESSION_KEYS.SORT_FIELD, DEFAULT_VALUES.sortField),
  search: getSessionValue(SESSION_KEYS.SEARCH),
});

// ======================== Data Processing Helpers ========================
const convertStorageValues = (storageNames) => {
  return storageNames.map((name) => {
    const option = STORAGE_OPTIONS.value.find((opt) => opt.name === name);
    if (!option) return null;

    return option.value === "-1" ? -1 : Number(option.value);
  });
};

const convertPriceValues = (priceNames) => {
  return priceNames.map((name) => {
    const option = PRICE_OPTIONS.find((opt) => opt.name === name);
    return option ? option.value : name;
  });
};

const parsePriceRange = (priceValues, customPrice = null) => {
  // If custom price is set, prioritize it over predefined ranges
  if (customPrice && (customPrice.min !== null || customPrice.max !== null)) {
    return {
      min: customPrice.min,
      max: customPrice.max,
    };
  }

  if (!priceValues.length) return { min: null, max: null };

  let min = null;
  let max = null;

  priceValues.forEach((range) => {
    // ตรวจสอบว่ามี "-" หรือไม่
    if (range.includes("-")) {
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

// ======================== API Functions ========================

const loadImageUrl = async () => {
  imageUrl.value = [];
  for (const item of product.value.content) {
    if (item.mainImageFileName) {
      const image = await getImageByImageName(item.mainImageFileName);
      imageUrl.value.push(image);
    } else {
      imageUrl.value.push(
        "https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg"
      );
    }
  }

  console.log("Image URLs loaded:", imageUrl.value);
};

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
    loadImageUrl();
  } catch (error) {
    console.error("Error loading products:", error);
  }
};

const loadProductsWithFilters = async (filters) => {
  try {
    // Convert filter values
    const storageValues = convertStorageValues(filters.storages);
    const priceValues = convertPriceValues(filters.prices);
    const { min: minPrice, max: maxPrice } = parsePriceRange(
      priceValues,
      filters.customPrice
    );

    console.log("Loading products with filters:", {
      brands: filters.brands,
      sortField: filters.sortField,
      sortDirection: filters.sortDirection,
      size: filters.size,
      page: filters.page,
      storageValues,
      minPrice,
      maxPrice,
      customPrice: filters.customPrice,
    });

    const data = await getAllSaleItemV2(
      filters.brands,
      filters.sortField,
      filters.sortDirection,
      filters.size,
      filters.page,
      storageValues,
      minPrice,
      maxPrice,
      filters.search
    );
    product.value = data;
    totalPages.value = data.totalPages;
    loadImageUrl();
  } catch (error) {
    console.error("Error loading filtered products:", error);
  }
};

const loadStroage = async () => {
  try {
    const data = await getViewStorageForSelect();

    // ถ้าเจอ error จาก API ให้ default เป็น array ว่าง
    if (data.error) {
      STORAGE_OPTIONS.value = [];
      return;
    }

    STORAGE_OPTIONS.value = data.map((item) => {
      if (item == null) {
        return { name: "Not specified", value: "-1" };
      }
      return {
        name: `${item.storageGb} GB`,
        value: String(item.storageGb),
      };
    });

    STORAGE_OPTIONS.value.sort((a, b) => {
      if (a.value === "-1") return 1;
      if (b.value === "-1") return -1;
      return Number(a.value) - Number(b.value);
    });
  } catch (error) {
    console.error("Failed to load storage:", error);
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

  // Clear custom price when predefined price ranges are selected
  if (newPrices.length > 0) {
    sessionStorage.setItem(SESSION_KEYS.CUSTOM_PRICE_MIN, "");
    sessionStorage.setItem(SESSION_KEYS.CUSTOM_PRICE_MAX, "");
    customPriceRange.value = { min: null, max: null };
  }

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

const handleSearch = async (keyword) => {
  console.log(keyword);
  setSession(SESSION_KEYS.SEARCH, keyword);
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
  // Clear custom price as well
  customPriceRange.value = { min: null, max: null };
  sessionStorage.setItem(SESSION_KEYS.CUSTOM_PRICE_MIN, "");
  sessionStorage.setItem(SESSION_KEYS.CUSTOM_PRICE_MAX, "");

  const filters = getCurrentFilters();
  await loadProductsWithFilters(filters);
};

let priceOption = ref([...PRICE_OPTIONS]);
let min_price = ref(null);
let max_price = ref(null);

const applyCustomPrice = () => {
  const min = min_price.value ?? "";
  const max = max_price.value ?? "";

  let customPriceRange = "";
  let customName = "";

  if (min !== "" && max !== "") {
    customPriceRange = `${min}-${max}`;
    customName = `${min} – ${max} Baht`;
  } else if (min !== "") {
    customPriceRange = `${min}-${min}`;
    customName = `${min} Baht`;
  } else if (max !== "") {
    customPriceRange = `${max}-${max}`;
    customName = `${max} Baht`;
  } else {
    setSession(SESSION_KEYS.PRICE, []);
    loadProductsWithFilters(getCurrentFilters());
    return;
  }

  const maxId =
    priceOption.value.length > 0
      ? Math.max(...priceOption.value.map((opt) => Number(opt.id)))
      : 0;

  const customOption = {
    id: maxId + 1,
    name: customName,
    value: customPriceRange,
  };

  priceOption.value.push(customOption);
  const prevSelected = getSessionArray(SESSION_KEYS.PRICE) || [];
  const updatedSelected = [...prevSelected, customOption.value];

  setSession(SESSION_KEYS.PRICE, updatedSelected);
  loadProductsWithFilters(getCurrentFilters());
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
    (filters.customPrice &&
      (filters.customPrice.min !== null || filters.customPrice.max !== null)) ||
    filters.page > 0 ||
    filters.sortField !== DEFAULT_VALUES.sortField ||
    filters.sortDirection !== DEFAULT_VALUES.sortDirection ||
    filters.size !== DEFAULT_VALUES.size ||
    !! filters.search  
  );
};

// ======================== Lifecycle ========================
const imageUrl = ref([]);
onBeforeMount(async () => {
  await loadBrands();
  await loadStroage();

  // Load custom price from session storage
  customPriceRange.value = getSessionCustomPrice();

  const filters = getCurrentFilters();
  console.log("Initial filters:", filters);

  console.log(hasActiveFilters(filters));
  
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
  <div class="flex flex-col gap-6 m-10">
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
      <!-- New Product -->
      <RouterLink
        :to="{ name: 'ProuctCreate' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all"
      >
        <span class="itbms-sale-item-add tracking-wide">New Product</span>
      </RouterLink>

      <!-- Manage Items -->
      <RouterLink
        :to="{ name: 'ProductManage' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all"
      >
        <span class="itbms-manage-brand tracking-wide">Manage Sale Items</span>
      </RouterLink>
    </div>

    <!-- 🔹 Search + SizeAndSort แนวนอน -->
    <div class="flex justify-between items-center gap-4">
      <SizeAndSort
        :initialSize="getSessionValue(SESSION_KEYS.SIZE, DEFAULT_VALUES.size)"
        :initialSortField="getSessionValue(SESSION_KEYS.SORT_FIELD, '')"
        :initialSortDirection="getSessionValue(SESSION_KEYS.SORT_DIRECTION, '')"
        @sizeChanged="handleSizeChange"
        @sortChanged="handleSortChange" 
      />
      <Search 
      :key="getSessionValue(SESSION_KEYS.SEARCH, '')"
      :initialValue="getSessionValue(SESSION_KEYS.SEARCH, '')"
      @search="handleSearch" />
    </div>

    <!-- 🔹 Filters + Gallery ระนาบเดียวกัน -->
    <div class="flex gap-6">
      <!-- Filters -->
      <div class="w-1/6 flex flex-col gap-4 mt-8 bg-gray-100 rounded py-[20px]">
        <div class="text-xl font-semibold text-gray-700 mb-2 ml-[20px]">
          <span class="flex"><Funnel /> &nbsp; Filter by:</span>
        </div>
        <Filter
          :initialFilterValues="getSessionArray(SESSION_KEYS.BRAND)"
          :options="brand"
          label="Brands"
          placeholder="Select brands"
          :sessionKey="SESSION_KEYS.BRAND"
          valueField="name"
          displayField="name"
          mode="brand"
          @filterChanged="handleBrandFilter"
        />

        <Filter
          :initialFilterValues="getSessionArray(SESSION_KEYS.STORAGE)"
          :options="STORAGE_OPTIONS"
          label="Storages"
          placeholder="Select StoragesGB"
          :sessionKey="SESSION_KEYS.STORAGE"
          valueField="name"
          displayField="name"
          mode="Storages"
          @filterChanged="handleStorageFilter"
        />

        <!-- Price Filter -->
        <Filter
          :initialFilterValues="getSessionArray(SESSION_KEYS.PRICE)"
          :options="priceOption"
          label="Price"
          placeholder="Select Price Range"
          :sessionKey="SESSION_KEYS.PRICE"
          valueField="value"
          displayField="name"
          :enableCustomPriceInput="true"
          mode="price"
          @filterChanged="handlePriceFilter"
        >
          <template #InputPrice>
            <div class="items-center px-4 py-3 border-t border-gray-100 gap-2">
              <div class="mb-[7px] text-gray-800 font-semibold">
                Custom Price
              </div>
              <div class="flex mb-[8px]">
                <span class="mx-1">Min</span>
                <input
                  type="number"
                  class="border rounded px-2 py-1 flex-1 min-w-0"
                  placeholder="Min"
                  v-model.number="min_price"
                />
              </div>
              <div class="flex mb-[8px]">
                <span class="mx-1">Max</span>
                <input
                  :disabled="min_price === null"
                  type="number"
                  class="border rounded px-2 py-1 flex-1 min-w-0"
                  placeholder="Max"
                  v-model.number="max_price"
                />
              </div>
              <div v-if="min_price > max_price && max_price != 0">
                <p class="text-red-500 text-sm">
                  * Min price must be least than max price
                </p>
              </div>
              <div class="flex justify-end">
                <button
                  class="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 whitespace-nowrap block"
                  @click="applyCustomPrice"
                >
                  Apply
                </button>
              </div>
            </div>
          </template>
        </Filter>

        <!-- Clear Button -->
        <div class="flex justify-end mr-[25px]">
          <ClearButton
            :sessionKeys="[
              SESSION_KEYS.BRAND,
              SESSION_KEYS.STORAGE,
              SESSION_KEYS.PRICE,
              SESSION_KEYS.CUSTOM_PRICE_MIN,
              SESSION_KEYS.CUSTOM_PRICE_MAX,
              SESSION_KEYS.PAGE,
              SESSION_KEYS.SEARCH,
            ]"
            @cleared="handleClear"
          />
        </div>
      </div>

      <!-- Product Gallery -->
      <div class="flex-1">
        <SelectAllSaleItemGallery
          v-if="product?.content"
          :product="product.content"
          :imageUrl="imageUrl"
        />
      </div>
    </div>

    <!-- 🔹 Pagination ด้านล่าง -->
    <div class="mt-4">
      <Pagination
        :initialTotalPages="totalPages"
        :initialPage="
          getSessionValue(SESSION_KEYS.PAGE, DEFAULT_VALUES.page) + 1
        "
        @pageChanged="handlePageChange"
      />
    </div>
  </div>
</template>

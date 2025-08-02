<script setup>
import {
  computed,
  defineEmits,
  defineProps,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";
import { getAllBrand } from "../libs/callAPI/apiBrand";

const props = defineProps({
  productTotalPages: Number,
  initialPage: Number,
  initialSize: Number,
  initialFilterBrands: String,
  initialSortField: String,
  initialSortDirection: String,
  showFilter: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true },
});

const emit = defineEmits(["urlSetting"]);

// Initialize reactive variables with props
const filterBrands = ref(props.initialFilterBrands || "");
const page = ref(props.initialPage || 1);
const itbmPage = ref((props.initialPage || 1) - 1);
const size = ref(props.initialSize || 10);
const sortField = ref(props.initialSortField || "id");
const sortDirection = ref(props.initialSortDirection || "asc");
const selectedBrandList = ref([]);
const brandDropdown = ref(null);
const options = ref([]);
const selected = ref(null);
const dropdownOpen = ref(false);
const dropdownRef = ref(null);
const currentSort = ref("none");

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});

const handleClickOutside = (event) => {
  if (!dropdownRef.value || !(dropdownRef.value instanceof HTMLElement)) return;
  if (!dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false;
  }
};

const totalPage = computed(() => props.productTotalPages);

// Watch for prop changes
watch(
  () => props.initialFilterBrands,
  (newVal) => {
    filterBrands.value = newVal || "";
    if (newVal) {
      selectedBrandList.value = newVal
        .split(",")
        .filter((brand) => brand.trim() !== "");
    } else {
      selectedBrandList.value = [];
    }
  }
);

watch(
  () => props.initialPage,
  (newVal) => {
    if (newVal) {
      page.value = newVal;
      itbmPage.value = newVal - 1;
    }
  }
);

watch(
  () => props.initialSize,
  (newVal) => {
    if (newVal) {
      size.value = newVal;
    }
  }
);

const settings = computed(() => ({
  filterBrands: selectedBrandList.value.join(","),
  page: itbmPage.value, // Convert back to 1-based index for display
  size: size.value,
  sortField: sortField.value,
  sortDirection: sortDirection.value,
}));

const emitUrlSetting = () => {
  emit("urlSetting", settings.value);
};

const goToPage = async (pageNumber) => {
  page.value = pageNumber;
  itbmPage.value = pageNumber - 1;

  const sessionStorageRaw = sessionStorage.getItem("product-page-settings");
  let sessionStorageValue = {};
  try {
    if (sessionStorageRaw) {
      sessionStorageValue = JSON.parse(sessionStorageRaw);
    }
  } catch (error) {
    console.error("Error parsing sessionStorage:", error);
  }

  sortDirection.value = sessionStorageValue.sortDirection || "desc";
  sortField.value = sessionStorageValue.sortField || "createdOn";

  emitUrlSetting();
};

const setSize = (newsize) => {
  size.value = newsize;
  page.value = 1;
  itbmPage.value = 0;
  // Save pagination size to sessionStorage
  sessionStorage.setItem("pagination-size", newsize.toString());
  emitUrlSetting();
};
const sortAsc = () => {
  sortDirection.value = "asc";
  sortField.value = "brand.name";
  currentSort.value = "asc"; // เพิ่มบรรทัดนี้
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

const sortDesc = () => {
  sortDirection.value = "desc";
  sortField.value = "brand.name";
  currentSort.value = "desc"; // เพิ่มบรรทัดนี้
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

const resetSort = () => {
  sortDirection.value = "asc";
  sortField.value = "id";
  currentSort.value = "none"; // เพิ่มบรรทัดนี้
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
};

function onBrandSelected(brandName) {
  const brandObj = options.value.find((b) => b.name === brandName);
  selected.value = brandObj;
  if (brandObj && !selectedBrandList.value.includes(brandObj.name)) {
    selectedBrandList.value.push(brandObj.name);
  }
  filterBrands.value = selectedBrandList.value.join(",");

  // รีเซ็ตหน้าเมื่อเลือก brand ใหม่
  page.value = 1;
  itbmPage.value = 0;

  emitUrlSetting();
  // dropdownOpen.value = false; // ปิด dropdown หลังเลือก
}

function removeBrand(index) {
  selectedBrandList.value.splice(index, 1);
  filterBrands.value = selectedBrandList.value.join(",");
  if (brandDropdown.value && brandDropdown.value.resetSelection) {
    brandDropdown.value.resetSelection();
  }

  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
}

function clearBrand() {
  selectedBrandList.value = [];
  filterBrands.value = "";
  if (brandDropdown.value && brandDropdown.value.resetSelection) {
    brandDropdown.value.resetSelection();
  }
  page.value = 1;
  itbmPage.value = 0;
  emitUrlSetting();
}

// Toggle dropdown function
const toggleDropdown = (event) => {
  event.stopPropagation();
  dropdownOpen.value = !dropdownOpen.value;
};

const handlePostDelete = () => {
  const wasDeleted = sessionStorage.getItem("item-just-deleted");
  if (wasDeleted === "true") {
    // เคลียร์ flag
    sessionStorage.removeItem("item-just-deleted");
    // ใช้ setTimeout เพื่อรอให้ข้อมูลโหลดเสร็จก่อน
    setTimeout(() => {
      if (page.value > 1 && page.value > totalPage.value) {
        console.log(
          "Current page is empty after delete, going to previous page"
        );
        page.value = totalPage.value || 1;
        itbmPage.value = (totalPage.value || 1) - 1;
        emitUrlSetting();
      } else {
        console.log("Current page still has data, staying here");
        emitUrlSetting();
      }
    }, 200);
  }
};

onMounted(async () => {
  // เช็คการลบหลังจากโหลดข้อมูลเสร็จ
  handlePostDelete();

  // โค้ดเดิม...
  if (filterBrands.value) {
    selectedBrandList.value = filterBrands.value
      .split(",")
      .filter((brand) => brand.trim() !== "");
  }

  const savedSize = sessionStorage.getItem("pagination-size");
  if (savedSize && !props.initialSize) {
    size.value = parseInt(savedSize, 10);
  }

  document.addEventListener("click", handleClickOutside);

  try {
    const data = await getAllBrand();
    options.value = data.sort((a, b) => a.name.localeCompare(b.name));
  } catch (error) {
    console.error("โหลดแบรนด์ล้มเหลว:", error.message);
  }
});
</script>

<template>
  <div class="p-4 space-y-6 text-sm text-gray-800 max-w-6xl mx-auto">
    <div v-if="showFilter" class="filter">
      <!-- Filter Section - Horizontal Layout -->
      <div
        class="flex items-center justify-between bg-white p-4 rounded-lg shadow-sm border"
      >
        <!-- Left Side - Brand Filter -->
        <div class="flex items-center gap-4">
          <span class="text-gray-700 font-medium">Filter by brand(s)</span>

          <!-- Brand Dropdown and Selected Brands Inline -->
          <div
            ref="dropdownRef"
            class="itbms-brand-filter relative flex items-center gap-4"
          >
            <!-- Dropdown Toggle Button -->
            <div
              class="itbms-brand-filter itbms-brand-filter-button px-3 py-2 border border-gray-300 rounded cursor-pointer bg-white min-w-32 text-left hover:bg-gray-50 transition"
              @click="toggleDropdown"
              data-cy="brand-dropdown-toggle"
              role="button"
              tabindex="0"
            >
              {{ selected?.name || "-- เลือกแบรนด์ --" }}
              <span class="float-right">▼</span>
            </div>

            <!-- Dropdown Options -->
            <div
              class="absolute z-10 mt-1 w-full bg-white border rounded shadow-lg max-h-60 overflow-y-auto"
              v-if="dropdownOpen"
              data-cy="brand-options"
              :data-dropdown-open="dropdownOpen"
            >
              <div
                v-for="opt in options"
                :key="opt.id"
                class="itbms-filter-item px-3 py-2 hover:bg-gray-100 cursor-pointer"
                @click="onBrandSelected(opt.name)"
                @mousedown.prevent
                data-cy="brand-option"
                :data-brand-name="opt.name"
              >
                {{ opt.name }}
              </div>
            </div>
          </div>
          <!-- Selected Brands Display - now on the right of the dropdown -->
          <div v-if="selectedBrandList.length > 0" class="flex flex-wrap gap-2">
            <span
              v-for="(brand, i) in selectedBrandList"
              :key="i"
              class="flex items-center bg-blue-50 border border-blue-300 rounded-full px-3 py-1 text-sm text-blue-800 shadow-sm"
            >
              {{ brand }}
              <button
                @click="removeBrand(i)"
                class="itbms-filter-item-clear ml-2 text-blue-600 hover:text-blue-800 focus:outline-none font-bold"
                aria-label="ลบแบรนด์"
              >
                ×
              </button>
            </span>
          </div>

          <!-- Clear Button -->
          <button
            @click="clearBrand"
            class="itbms-brand-filter-clear px-3 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded transition"
          >
            Clear
          </button>
        </div>

        <!-- Right Side - Shows and Controls -->
        <div class="flex items-center gap-4">
          <span class="text-gray-700">Shows:</span>

          <!-- Page Size Dropdown -->
          <select
            id="size"
            v-model="size"
            @change="setSize(size)"
            class="itbms-page-size border border-gray-300 rounded px-3 py-2 bg-purple-100 text-purple-800 focus:ring-purple-400 focus:ring-2 min-w-16"
          >
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>

          <!-- Sort Controls -->
          <div class="flex gap-1">
            <button
              @click="sortAsc"
              :class="[
                'itbms-brand-asc p-2 border border-gray-300 rounded transition',
                currentSort === 'asc'
                  ? 'bg-purple-500 text-white'
                  : 'hover:bg-purple-100',
              ]"
              title="Sort Ascending"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 6h16M4 12h8m-8 6h4"
                ></path>
              </svg>
            </button>
            <button
              @click="sortDesc"
              :class="[
                'itbms-brand-desc p-2 border border-gray-300 rounded transition',
                currentSort === 'desc'
                  ? 'bg-purple-500 text-white'
                  : 'hover:bg-purple-100',
              ]"
              title="Sort Descending"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 18h16M4 12h8m-8 6h4"
                ></path>
              </svg>
            </button>
            <button
              @click="resetSort"
              :class="[
                'itbms-brand-none p-2 border border-gray-300 rounded transition',
                currentSort === 'none'
                  ? 'bg-purple-500 text-white'
                  : 'hover:bg-purple-100',
              ]"
              title="Reset Sort"
            >
              <svg
                class="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 6h16M10 12H4m0 6h16"
                ></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- Pagination Section -->
    <div v-show="showPagination && totalPage > 1" class="Pagination">
      <div class="flex justify-center">
        <div
          class="flex gap-1 items-center bg-white rounded-lg shadow-sm border p-2"
        >
          <button
            @click="goToPage(1)"
            :disabled="page === 1"
            class="itbms-page-first px-3 py-2 rounded text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            First
          </button>
          <button
            @click="goToPage(Math.max(1, page - 1))"
            :disabled="page === 1"
            class="itbms-page-prev px-3 py-2 rounded text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            Prev
          </button>

          <template v-for="(p, index) in totalPage" :key="p">
            <button
              @click="goToPage(p)"
              :class="[
                `itbms-page-${index}`,
                'px-3 py-2 rounded transition min-w-10',
                page === p
                  ? 'bg-gray-800 text-white'
                  : 'text-gray-600 hover:bg-gray-100',
              ]"
            >
              {{ p }}
            </button>
          </template>

          <button
            @click="goToPage(Math.min(totalPage, page + 1))"
            :disabled="page === totalPage"
            class="itbms-page-next px-3 py-2 rounded text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            Next
          </button>
          <button
            @click="goToPage(totalPage)"
            :disabled="page === totalPage"
            class="itbms-page-last px-3 py-2 rounded text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            Last
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

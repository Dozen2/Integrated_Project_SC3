<script setup>
import {
  computed,
  defineEmits,
  defineProps,
  onMounted,
  ref,
  watch,
} from "vue";


const props = defineProps({
  initialTotalPages: Number,
  initialPage: Number,
});

const emit = defineEmits(["pageChanged"]);

const SESSION_KEYS = {
  PAGE: "SaleItem-Page",
  SORT_FIELD: "SaleItem-SortField",
  SORT_DIRECTION: "SaleItem-SortDirection",
  FILTER_BRAND: "SaleItem-FilterBrand",
  SIZE: "SaleItem-Size"
};

const page = ref(1);
const itbmPage = ref(0);

// Get session storage value
const getSessionValue = (key, defaultValue) => {
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

// Set session storage value
const setSessionValue = (key, value) => {
  if (typeof value === 'object') {
    sessionStorage.setItem(key, JSON.stringify(value));
  } else {
    sessionStorage.setItem(key, value.toString());
  }
};

// Watch for prop changes
watch(
  () => props.initialPage,
  (newVal) => {
    if (newVal !== undefined) {
      page.value = newVal;
      itbmPage.value = newVal - 1;
    }
  }
);

const totalPage = computed(() => props.initialTotalPages);

const goToPage = async (pageNumber) => {
  page.value = pageNumber;
  itbmPage.value = pageNumber - 1;
  
  setSessionValue(SESSION_KEYS.PAGE, itbmPage.value);

  // Get current session values for all settings
  const sortDirection = getSessionValue(SESSION_KEYS.SORT_DIRECTION, "desc");
  const sortField = getSessionValue(SESSION_KEYS.SORT_FIELD, "createdOn");
  const filterBrands = getSessionValue(SESSION_KEYS.FILTER_BRAND, "");
  const size = getSessionValue(SESSION_KEYS.SIZE, 10);

  emit("pageChanged", {
    page: itbmPage.value,
    sortField,
    sortDirection,
    filterBrands,
    size,
  });
};

const handlePostDelete = () => {
  const wasDeleted = sessionStorage.getItem("item-just-deleted");
  if (wasDeleted === "true") {
    // เคลียร์ flag
    sessionStorage.removeItem("item-just-deleted");
    // ใช้ setTimeout เพื่อรอให้ข้อมูลโหลดเสร็จก่อน
    setTimeout(() => {
      const sortDirection = getSessionValue(SESSION_KEYS.SORT_DIRECTION, "desc");
      const sortField = getSessionValue(SESSION_KEYS.SORT_FIELD, "createdOn");
      const filterBrands = getSessionValue(SESSION_KEYS.FILTER_BRAND, "");
      const size = getSessionValue(SESSION_KEYS.SIZE, 10);

      if (page.value > 1 && page.value > totalPage.value) {
        console.log(
          "Current page is empty after delete, going to previous page"
        );
        page.value = totalPage.value || 1;
        itbmPage.value = (totalPage.value || 1) - 1;
        
        setSessionValue(SESSION_KEYS.PAGE, itbmPage.value);
        
        emit("pageChanged", {
          page: itbmPage.value,
          sortField,
          sortDirection,
          filterBrands,
          size,
        });
      } else {
        console.log("Current page still has data, staying here");
        emit("pageChanged", {
          page: itbmPage.value,
          sortField,
          sortDirection,
          filterBrands,
          size,
        });
      }
    }, 200);
  }
};

onMounted(() => {
  // Load from session storage first, then from props as fallback
  const sessionPage = getSessionValue(SESSION_KEYS.PAGE, null);
  
  if (sessionPage !== null) {
    itbmPage.value = sessionPage;
    page.value = sessionPage + 1;
  } else if (props.initialPage !== undefined) {
    page.value = props.initialPage;
    itbmPage.value = props.initialPage - 1;
  }
  
  // เช็คการลบหลังจากโหลดข้อมูลเสร็จ
  handlePostDelete();
});
</script>

<template>
  <div v-show="totalPage > 1" class="Pagination">
    <div class="flex justify-center">
      <div
        class="flex gap-1 items-center bg-white rounded-lg shadow-sm border p-2"
      >
        <button
          @click="goToPage(1)"
          :disabled="page === 1"
          class="cursor-pointer itbms-page-first px-3 py-2 rounded text-gray-600 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          First
        </button>
        <button
          @click="goToPage(Math.max(1, page - 1))"
          :disabled="page === 1"
          class="cursor-pointer itbms-page-prev px-3 py-2 rounded text-gray-600 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition"
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
                : 'text-gray-600 hover:bg-gray-300 cursor-pointer ',
            ]"
          >
            {{ p }}
          </button>
        </template>

        <button
          @click="goToPage(Math.min(totalPage, page + 1))"
          :disabled="page === totalPage"
          class="itbms-page-next px-3 py-2 rounded text-gray-600 hover:bg-gray-300 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          Next
        </button>
        <button
          @click="goToPage(totalPage)"
          :disabled="page === totalPage"
          class="itbms-page-last px-3 py-2 rounded text-gray-600 hover:bg-gray-300 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          Last
        </button>
      </div>
    </div>
  </div>
</template>
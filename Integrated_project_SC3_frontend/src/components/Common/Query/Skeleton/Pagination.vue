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

const page = ref(props.initialPage || 1);
const itbmPage = ref((props.initialPage || 1) - 1);

// Watch for prop changes
watch(
  () => props.initialPage,
  (newVal) => {
    if (newVal) {
      page.value = newVal;
      itbmPage.value = newVal - 1;
    }
  }
);

const totalPage = computed(() => props.initialTotalPages);

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

  // ใช้ค่าปัจจุบันจาก sessionStorage แทนการ hardcode
  const sortDirection = sessionStorageValue.sortDirection || "desc";
  const sortField = sessionStorageValue.sortField || "createdOn";
  const filterBrands = sessionStorageValue.filterBrands || "";
  const size = sessionStorageValue.size || 10;

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
      const sessionStorageRaw = sessionStorage.getItem("product-page-settings");
      let sessionStorageValue = {};
      try {
        if (sessionStorageRaw) {
          sessionStorageValue = JSON.parse(sessionStorageRaw);
        }
      } catch (error) {
        console.error("Error parsing sessionStorage:", error);
      }

      if (page.value > 1 && page.value > totalPage.value) {
        console.log(
          "Current page is empty after delete, going to previous page"
        );
        page.value = totalPage.value || 1;
        itbmPage.value = (totalPage.value || 1) - 1;
        
        emit("pageChanged", {
          page: itbmPage.value,
          sortField: sessionStorageValue.sortField || "createdOn",
          sortDirection: sessionStorageValue.sortDirection || "desc",
          filterBrands: sessionStorageValue.filterBrands || "",
          size: sessionStorageValue.size || 10,
        });
      } else {
        console.log("Current page still has data, staying here");
        emit("pageChanged", {
          page: itbmPage.value,
          sortField: sessionStorageValue.sortField || "createdOn",
          sortDirection: sessionStorageValue.sortDirection || "desc",
          filterBrands: sessionStorageValue.filterBrands || "",
          size: sessionStorageValue.size || 10,
        });
      }
    }, 200);
  }
};

onMounted(() => {
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
</template>
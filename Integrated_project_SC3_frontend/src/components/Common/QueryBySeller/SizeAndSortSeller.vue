<script setup>
import { ref, onBeforeMount, watch } from "vue";
import {
  ArrowDownWideNarrow,
  ArrowUpWideNarrow,
  ListFilter,
} from "lucide-vue-next";

// Props
const props = defineProps({
  modelSize: { type: Number, default: 10 },
  modelSort: { type: String, default: "createdOn" },
     modelPage: { type: Number, default: 0 },
  storageKeySize: { type: String, default: "seller_size" },
  storageKeySort: { type: String, default: "seller_sort" },
  resetStorage: { type: String, default: "seller_pagination" },
});

// Emits
const emit = defineEmits(["update:modelSize", "update:modelSort", "update:modelPage"]);

// Local state
const size = ref(props.modelSize);
const sort = ref(props.modelSort);

// โหลดค่าจาก sessionStorage ตอน mount
onBeforeMount(() => {
  const savedSize = sessionStorage.getItem(props.storageKeySize);
  if (savedSize) size.value = parseInt(savedSize, 10);

  const savedSort = sessionStorage.getItem(props.storageKeySort);
  if (savedSort) sort.value = savedSort;

  // Emit ค่าตอนแรก
  emit("update:modelSize", size.value);
  emit("update:modelSort", sort.value);
});

// Watch size & sort → save + emit
watch(size, (newSize) => {
  sessionStorage.setItem(props.storageKeySize, newSize);
  emit("update:modelSize", newSize);
});

watch(sort, (newSort) => {
  sessionStorage.setItem(props.storageKeySort, newSort);
  emit("update:modelSort", newSort);
});

// ฟังก์ชัน sort buttons
function sortAsc() {
  sort.value = "asc";
}
function sortDesc() {
  sort.value = "desc";
}
function resetSort() {
  sort.value = "createdOn";
}

function setSize(newSize) {
  size.value = newSize;
  sessionStorage.removeItem(props.resetStorage);
    sessionStorage.setItem(props.resetStorage, "0"); 
     emit("update:modelPage", 0);
}
</script>

<template>
  <div class="flex items-center gap-4">
    <!-- Page Size -->
    <select
      v-model="size"
      @change="setSize(size)"
      class="cursor-pointer border border-blue-300 rounded-lg px-5 py-2 bg-white text-blue-800 font-medium shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition duration-200 ease-in-out min-w-[4rem]"
    >
      <option :value="5">5</option>
      <option :value="10">10</option>
      <option :value="20">20</option>
      <option :value="50">50</option>
    </select>

    <!-- Sort Controls -->
    <!-- <div class="flex gap-1">
      <button
        @click="sortAsc"
        :class="[
          'p-2 border border-gray-300 rounded transition cursor-pointer',
          sort === 'asc' ? 'bg-blue-500 text-white' : 'hover:bg-blue-100',
        ]"
        title="Sort Ascending"
      >
        <ArrowDownWideNarrow />
      </button>
      <button
        @click="sortDesc"
        :class="[
          'p-2 border border-gray-300 rounded transition cursor-pointer',
          sort === 'desc' ? 'bg-blue-500 text-white' : 'hover:bg-blue-100',
        ]"
        title="Sort Descending"
      >
        <ArrowUpWideNarrow />
      </button>
      <button
        @click="resetSort"
        :class="[
          'p-2 border border-gray-300 rounded transition cursor-pointer',
          sort === props.modelSort
            ? 'bg-blue-500 text-white'
            : 'hover:bg-blue-100',
        ]"
        title="Reset Sort"
      >
        <ListFilter />
      </button>
    </div> -->
  </div>
</template>

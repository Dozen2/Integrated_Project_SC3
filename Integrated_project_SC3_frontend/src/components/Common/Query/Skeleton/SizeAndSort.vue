<script setup>
import {
  defineEmits,
  defineProps,
  onMounted,
  ref,
  watch,
} from "vue";

const props = defineProps({
  initialSize: Number,
  initialSortField: String,
  initialSortDirection: String,
});

const emit = defineEmits(["sizeChanged", "sortChanged"]);

const size = ref(props.initialSize || 10);
const sortField = ref(props.initialSortField || "id");
const sortDirection = ref(props.initialSortDirection || "asc");
const currentSort = ref("none");

// Watch for prop changes
watch(
  () => props.initialSize,
  (newVal) => {
    if (newVal) {
      size.value = newVal;
    }
  }
);

// Determine current sort based on sortField and sortDirection
watch(
  [() => props.initialSortField, () => props.initialSortDirection],
  ([newSortField, newSortDirection]) => {
    sortField.value = newSortField || "id";
    sortDirection.value = newSortDirection || "asc";
    
    if (newSortField === "brand.name") {
      currentSort.value = newSortDirection === "asc" ? "asc" : "desc";
    } else {
      currentSort.value = "none";
    }
  }
);

const setSize = (newsize) => {
  size.value = newsize;
  // Save pagination size to sessionStorage
  // sessionStorage.setItem("pagination-size", newsize.toString());
  emit("sizeChanged", newsize);
};

const sortAsc = () => {
  sortDirection.value = "asc";
  sortField.value = "brand.name";
  currentSort.value = "asc";
  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

const sortDesc = () => {
  sortDirection.value = "desc";
  sortField.value = "brand.name";
  currentSort.value = "desc";
  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

const resetSort = () => {
  sortDirection.value = "asc";
  sortField.value = "id";
  currentSort.value = "none";
  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

onMounted(() => {
  // const savedSize = sessionStorage.getItem("pagination-size");
  // if (savedSize && !props.initialSize) {
  //   size.value = parseInt(savedSize, 10);
  // }
  
  // Set initial sort state
  if (props.initialSortField === "brand.name") {
    currentSort.value = props.initialSortDirection === "asc" ? "asc" : "desc";
  } else {
    currentSort.value = "none";
  }
});
</script>

<template>
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
    <SortAscendingOutlined class="text-[25px]" />
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
        <SortDescendingOutlined  class="text-[25px]" />
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
        <MenuOutlined class="text-[17px]"/>
      </button>
    </div>
  </div>
</template>
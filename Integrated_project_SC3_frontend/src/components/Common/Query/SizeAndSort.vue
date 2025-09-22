<script setup>
import {
  ArrowDownWideNarrow,
  ArrowUpWideNarrow,
  ListFilter,
} from "lucide-vue-next";
import { defineEmits, defineProps, onMounted, ref, watch } from "vue";


const props = defineProps({
  initialSize: Number,
  initialSortField: String,
  initialSortDirection: String,
});

const emit = defineEmits(["sizeChanged", "sortChanged"]);

const SESSION_KEYS = {
  SIZE: "SaleItem-Size",
  SORT_FIELD: "SaleItem-SortField",
  SORT_DIRECTION: "SaleItem-SortDirection",
};

const size = ref(10);
const sortField = ref("id");
const sortDirection = ref("asc");
const currentSort = ref("none");

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
  if (typeof value === "object") {
    sessionStorage.setItem(key, JSON.stringify(value));
  } else {
    sessionStorage.setItem(key, value.toString());
  }
};

// Watch for prop changes
watch(
  () => props.initialSize,
  (newVal) => {
    if (newVal !== undefined) {
      size.value = newVal;
    }
  }
);

// Determine current sort based on sortField and sortDirection
watch(
  [() => props.initialSortField, () => props.initialSortDirection],
  ([newSortField, newSortDirection]) => {
    if (newSortField !== undefined) {
      sortField.value = newSortField || "id";
    }
    if (newSortDirection !== undefined) {
      sortDirection.value = newSortDirection || "asc";
    }

    if (sortField.value === "brand.name") {
      currentSort.value = sortDirection.value === "asc" ? "asc" : "desc";
    } else {
      currentSort.value = "none";
    }
  }
);

const setSize = (newsize) => {
  size.value = newsize;
  setSessionValue(SESSION_KEYS.SIZE, newsize);
  emit("sizeChanged", newsize);
};

const sortAsc = () => {
  sortDirection.value = "asc";
  sortField.value = "brand.name";
  currentSort.value = "asc";

  setSessionValue(SESSION_KEYS.SORT_FIELD, sortField.value);
  setSessionValue(SESSION_KEYS.SORT_DIRECTION, sortDirection.value);

  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

const sortDesc = () => {
  sortDirection.value = "desc";
  sortField.value = "brand.name";
  currentSort.value = "desc";

  setSessionValue(SESSION_KEYS.SORT_FIELD, sortField.value);
  setSessionValue(SESSION_KEYS.SORT_DIRECTION, sortDirection.value);

  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

const resetSort = () => {
  sortDirection.value = "asc";
  sortField.value = "id";
  currentSort.value = "none";

  setSessionValue(SESSION_KEYS.SORT_FIELD, sortField.value);
  setSessionValue(SESSION_KEYS.SORT_DIRECTION, sortDirection.value);

  emit("sortChanged", {
    sortField: sortField.value,
    sortDirection: sortDirection.value,
  });
};

onMounted(() => {
  // Load from session storage first, then from props as fallback
  const sessionSize = getSessionValue(SESSION_KEYS.SIZE, null);
  const sessionSortField = getSessionValue(SESSION_KEYS.SORT_FIELD, null);
  const sessionSortDirection = getSessionValue(
    SESSION_KEYS.SORT_DIRECTION,
    null
  );

  size.value = sessionSize !== null ? sessionSize : props.initialSize || 10;
  sortField.value =
    sessionSortField !== null
      ? sessionSortField
      : props.initialSortField || "id";
  sortDirection.value =
    sessionSortDirection !== null
      ? sessionSortDirection
      : props.initialSortDirection || "asc";

  // Set initial sort state
  if (sortField.value === "brand.name") {
    currentSort.value = sortDirection.value === "asc" ? "asc" : "desc";
  } else {
    currentSort.value = "none";
  }
});
</script>

<template>
  <div class="flex items-center gap-4">
    <select
      id="size"
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
    <div class="flex gap-1">
      <button
        @click="sortAsc"
        :class="[
          'itbms-brand-asc p-2 border border-gray-300 rounded transition cursor-pointer',
          currentSort === 'asc'
            ? 'bg-blue-500 text-white'
            : 'hover:bg-blue-100',
        ]"
        title="Sort Ascending"
      >
        <ArrowDownWideNarrow />
      </button>
      <button
        @click="sortDesc"
        :class="[
          'itbms-brand-desc p-2 border border-gray-300 rounded transition cursor-pointer',
          currentSort === 'desc'
            ? 'bg-blue-500 text-white'
            : 'hover:bg-blue-100',
        ]"
        title="Sort Descending"
      >
        <ArrowUpWideNarrow />
      </button>
      <button
        @click="resetSort"
        :class="[
          'itbms-brand-none p-2 border border-gray-300 rounded transition cursor-pointer',
          currentSort === 'none'
            ? 'bg-blue-500 text-white'
            : 'hover:bg-blue-100',
        ]"
        title="Reset Sort"
      >
        <ListFilter />
      </button>
    </div>
  </div>
</template>

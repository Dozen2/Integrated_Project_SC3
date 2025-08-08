<script setup>
import {
  defineEmits,
  defineProps,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";
import { getAllBrand } from "@/libs/callAPI/apiBrand";

const props = defineProps({
  initialFilterBrands: String,
});

const emit = defineEmits(["brandFilterChanged"]);

const filterBrands = ref(props.initialFilterBrands || "");
const selectedBrandList = ref([]);
const brandDropdown = ref(null);
const options = ref([]);
const selected = ref(null);
const dropdownOpen = ref(false);
const dropdownRef = ref(null);

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});

const handleClickOutside = (event) => {
  if (!dropdownRef.value || !(dropdownRef.value instanceof HTMLElement)) return;
  if (!dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false;
  }
};

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

const emitBrandFilter = () => {
  const filterValue = selectedBrandList.value.join(",");
  emit("brandFilterChanged", filterValue);
};

function onBrandSelected(brandName) {
  const brandObj = options.value.find((b) => b.name === brandName);
  selected.value = brandObj;
  if (brandObj && !selectedBrandList.value.includes(brandObj.name)) {
    selectedBrandList.value.push(brandObj.name);
  }
  filterBrands.value = selectedBrandList.value.join(",");
  emitBrandFilter();
}

function removeBrand(index) {
  selectedBrandList.value.splice(index, 1);
  filterBrands.value = selectedBrandList.value.join(",");
  if (brandDropdown.value && brandDropdown.value.resetSelection) {
    brandDropdown.value.resetSelection();
  }
  emitBrandFilter();
}

function clearBrand() {
  selectedBrandList.value = [];
  filterBrands.value = "";
  if (brandDropdown.value && brandDropdown.value.resetSelection) {
    brandDropdown.value.resetSelection();
  }
  emitBrandFilter();
}

// Toggle dropdown function
const toggleDropdown = (event) => {
  event.stopPropagation();
  dropdownOpen.value = !dropdownOpen.value;
};

onMounted(async () => {
  if (filterBrands.value) {
    selectedBrandList.value = filterBrands.value
      .split(",")
      .filter((brand) => brand.trim() !== "");
  }

  document.addEventListener("click", handleClickOutside);

  try {
    const data = await getAllBrand();
    options.value = data.sort((a, b) => a.name.localeCompare(b.name));
    console.log("แบรนด์ที่โหลด:", options.value);
  } catch (error) {
    console.error("โหลดแบรนด์ล้มเหลว:", error.message);
  }
});
</script>

<template>
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
</template>
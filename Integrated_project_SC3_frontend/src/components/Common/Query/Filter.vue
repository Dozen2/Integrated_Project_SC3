<script setup>
import {
  defineEmits,
  defineProps,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";

const props = defineProps({
  initialFilterValues: String,
  options: {
    type: Array,
    default: () => [],
  },
  label: {
    type: String,
    default: "Filter",
  },
  placeholder: {
    type: String,
    default: "-- เลือกตัวเลือก --",
  },
  sessionKey: {
    type: String,
    required: true,
  },
  valueField: {
    type: String,
    default: "name", // field to use for the value (e.g., 'name', 'id')
  },
  displayField: {
    type: String,
    default: "name", // field to use for display (e.g., 'name', 'title')
  },
});

const emit = defineEmits(["filterChanged"]);

const filterValues = ref("");
const selectedValueList = ref([]);
const dropdownOpen = ref(false);
const dropdownRef = ref(null);
const selected = ref(null);

// Get session storage value
const getSessionFilterValues = () => {
  const raw = sessionStorage.getItem(props.sessionKey);
  return raw || "";
};

// Set session storage value
const setSessionFilterValues = (value) => {
  sessionStorage.setItem(props.sessionKey, value);
};

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
  () => props.initialFilterValues,
  (newVal) => {
    if (newVal !== undefined) {
      filterValues.value = newVal || "";
      if (newVal) {
        selectedValueList.value = newVal
          .split(",")
          .filter((value) => value.trim() !== "");
      } else {
        selectedValueList.value = [];
      }
    }
  }
);

const emitFilter = () => {
  const filterValue = selectedValueList.value.join(",");
  filterValues.value = filterValue;
  setSessionFilterValues(filterValue);
  emit("filterChanged", filterValue);
};

function onOptionSelected(optionValue) {
  const optionObj = props.options.find((opt) => opt[props.valueField] === optionValue);
  
  if (optionObj) {
    const valueIndex = selectedValueList.value.indexOf(optionObj[props.valueField]);
    
    if (valueIndex === -1) {
      // Add if not selected
      selectedValueList.value.push(optionObj[props.valueField]);
    } else {
      // Remove if already selected
      selectedValueList.value.splice(valueIndex, 1);
    }
    
    emitFilter();
    // Don't close dropdown - keep it open for multiple selections
  }
}

function removeValue(index) {
  selectedValueList.value.splice(index, 1);
  emitFilter();
}

function clearFilter() {
  selectedValueList.value = [];
  selected.value = null;
  emitFilter();
}

// Toggle dropdown function
const toggleDropdown = (event) => {
  event.stopPropagation();
  dropdownOpen.value = !dropdownOpen.value;
};

// Get display text for selected value
const getDisplayText = (value) => {
  const option = props.options.find((opt) => opt[props.valueField] === value);
  return option ? option[props.displayField] : value;
};

onMounted(() => {
  // Load from session storage first, then from props as fallback
  const sessionValue = getSessionFilterValues();
  const initialValue = sessionValue || props.initialFilterValues || "";
  
  if (initialValue) {
    filterValues.value = initialValue;
    selectedValueList.value = initialValue
      .split(",")
      .filter((value) => value.trim() !== "");
  }

  document.addEventListener("click", handleClickOutside);
});
</script>

<template>
  <div class="flex items-center gap-4">
    <span class="text-gray-700 font-medium whitespace-nowrap">{{ label }}</span>

    <!-- Filter Dropdown -->
    <div
      ref="dropdownRef"
      class="itbms-filter relative"
    >
      <!-- Dropdown Toggle Button -->
      <div
        class="itbms-filter itbms-filter-button px-4 py-3 border border-gray-300 rounded-lg cursor-pointer bg-white min-w-48 text-left hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 shadow-sm"
        @click="toggleDropdown"
        data-cy="filter-dropdown-toggle"
        role="button"
        tabindex="0"
      >
        <span class="text-gray-700">
          {{ selectedValueList.length > 0 ? `${selectedValueList.length} selected` : placeholder }}
        </span>
        <span class="float-right text-gray-500 transition-transform duration-200" :class="{ 'rotate-180': dropdownOpen }">
          <svg class="w-4 h-4 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </span>
      </div>

      <!-- Dropdown Options -->
      <div
        class="absolute top-full left-0 z-20 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-64 overflow-y-auto"
        v-if="dropdownOpen"
        data-cy="filter-options"
        :data-dropdown-open="dropdownOpen"
      >
        <div
          v-for="opt in options"
          :key="opt.id || opt[valueField]"
          class="itbms-filter-item flex items-center px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
          @click="onOptionSelected(opt[valueField])"
          @mousedown.prevent
          data-cy="filter-option"
          :data-option-value="opt[valueField]"
        >
          <!-- Checkbox -->
          <div class="flex items-center">
            <div class="relative">
              <input
                type="checkbox"
                :checked="selectedValueList.includes(opt[valueField])"
                class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2 cursor-pointer"
                @change.stop
                readonly
              />
            </div>
            <span class="ml-3 text-gray-700 select-none">{{ opt[displayField] }}</span>
          </div>
        </div>
        
        <!-- No options message -->
        <div v-if="options.length === 0" class="px-4 py-3 text-gray-500 text-center">
          No options available
        </div>
      </div>
      </div>
    
    <!-- Selected Values Display -->
    <div v-if="selectedValueList.length > 0" class="flex flex-wrap gap-2 flex-1">
      <span
        v-for="(value, i) in selectedValueList"
        :key="i"
        class="flex items-center bg-blue-50 border border-blue-300 rounded-full px-3 py-1 text-sm text-blue-800 shadow-sm"
      >
        {{ getDisplayText(value) }}
        <button
          @click="removeValue(i)"
          class="itbms-filter-item-clear ml-2 text-blue-600 hover:text-blue-800 focus:outline-none font-bold text-lg leading-none"
          aria-label="ลบตัวเลือก"
          title="Remove filter"
        >
          ×
        </button>
      </span>
    </div>

    <!-- Clear Button -->
    <button
      v-if="selectedValueList.length > 0"
      @click="clearFilter"
      class="itbms-filter-clear px-3 py-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors duration-200 whitespace-nowrap"
      title="Clear all filters"
    >
      Clear All
    </button>
  </div>
 
</template>
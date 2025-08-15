<script setup>
import {
  defineEmits,
  defineProps,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  computed
} from "vue";

const props = defineProps({
  initialFilterValues: [String, Array],
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
    default: "name",
  },
  displayField: {
    type: String,
    default: "name",
  },
  // เพิ่ม prop สำหรับแยกว่าเป็น price filter หรือไม่
  isPriceFilter: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["filterChanged"]);

const selectedValueList = ref([]);
const dropdownOpen = ref(false);
const dropdownRef = ref(null);

// เพิ่ม state สำหรับ custom price input
const customMinPrice = ref("");
const customMaxPrice = ref("");
const showCustomInput = ref(false);

// Helper function to convert various input types to array
const normalizeToArray = (value) => {
  if (Array.isArray(value)) {
    return value.filter((item) => item && item.toString().trim() !== "");
  }
  if (typeof value === "string" && value.trim() !== "") {
    return value
      .split(",")
      .map((item) => item.trim())
      .filter((item) => item !== "");
  }
  return [];
};

// Get session storage value and normalize to array
const getSessionFilterValues = () => {
  const raw = sessionStorage.getItem(props.sessionKey);
  if (!raw) return [];

  try {
    const parsed = JSON.parse(raw);
    return normalizeToArray(parsed);
  } catch {
    return normalizeToArray(raw);
  }
};

// Set session storage value as JSON array
const setSessionFilterValues = (arrayValue) => {
  sessionStorage.setItem(props.sessionKey, JSON.stringify(arrayValue));
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
      const normalizedArray = normalizeToArray(newVal);
      selectedValueList.value = [...normalizedArray];
    }
  }
);

const emitFilter = () => {
  const filterArray = [...selectedValueList.value];
  setSessionFilterValues(filterArray);
  emit("filterChanged", filterArray);
};

function onOptionSelected(optionValue) {
  const optionObj = props.options.find(
    (opt) => opt[props.valueField] === optionValue
  );

  if (optionObj) {
    const valueIndex = selectedValueList.value.indexOf(
      optionObj[props.valueField]
    );

    if (valueIndex === -1) {
      selectedValueList.value.push(optionObj[props.valueField]);
    } else {
      selectedValueList.value.splice(valueIndex, 1);
    }

    emitFilter();
  }
}

function removeValue(index) {
  selectedValueList.value.splice(index, 1);
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

// ฟังก์ชันสำหรับจัดการ custom price input
const toggleCustomInput = () => {
  showCustomInput.value = !showCustomInput.value;
  if (!showCustomInput.value) {
    // Clear custom inputs when hiding
    customMinPrice.value = "";
    customMaxPrice.value = "";
    // Remove custom price from selected values
    selectedValueList.value = selectedValueList.value.filter(
      (item) => !item.includes("custom:")
    );
    emitFilter();
  }
};

const applyCustomPrice = () => {
  const minPrice = parseFloat(customMinPrice.value) || 0;
  const maxPrice = parseFloat(customMaxPrice.value) || 999999;
  
  if (minPrice >= 0 && maxPrice > minPrice) {
    // Remove any existing custom price
    selectedValueList.value = selectedValueList.value.filter(
      (item) => !item.includes("custom:")
    );
    
    // Add new custom price range
    const customRange = `custom:${minPrice}-${maxPrice}`;
    selectedValueList.value.push(customRange);
    
    emitFilter();
    showCustomInput.value = false;
  }
};

const clearCustomPrice = () => {
  customMinPrice.value = "";
  customMaxPrice.value = "";
  // Remove custom price from selected values
  selectedValueList.value = selectedValueList.value.filter(
    (item) => !item.includes("custom:")
  );
  emitFilter();
};

// Format custom price for display
const formatCustomPrice = (value) => {
  if (value.includes("custom:")) {
    const range = value.replace("custom:", "");
    const [min, max] = range.split("-");
    return `Custom: ${Number(min).toLocaleString()} - ${Number(max).toLocaleString()} Baht`;
  }
  return value;
};

onMounted(async() => {
  const sessionValue = getSessionFilterValues();
  const initialValue =
    sessionValue.length > 0
      ? sessionValue
      : normalizeToArray(props.initialFilterValues);

  selectedValueList.value = [...initialValue];
  document.addEventListener("click", handleClickOutside);
});
</script>

<template>
  <div class="flex items-center gap-4">
    <span class="text-gray-700 font-medium whitespace-nowrap">{{ label }}</span>

    <!-- Filter Dropdown -->
    <div ref="dropdownRef" class="itbms-filter relative">
      <!-- Dropdown Toggle Button -->
      <div
        class="itbms-filter itbms-filter-button px-4 py-3 border border-gray-300 rounded-lg cursor-pointer bg-white min-w-48 text-left hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 shadow-sm"
        @click="toggleDropdown"
        data-cy="filter-dropdown-toggle"
        role="button"
        tabindex="0"
      >
        <span class="text-gray-700">
          {{
            selectedValueList.length > 0
              ? `${selectedValueList.length} selected`
              : placeholder
          }}
        </span>
        <span
          class="float-right text-gray-500 transition-transform duration-200"
          :class="{ 'rotate-180': dropdownOpen }"
        >
          <svg
            class="w-4 h-4 mt-0.5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </span>
      </div>

      <!-- Dropdown Options -->
      <div
        class="absolute top-full left-0 z-20 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-96 overflow-y-auto"
        v-if="dropdownOpen"
        data-cy="filter-options"
        :data-dropdown-open="dropdownOpen"
      >
        <!-- Regular Options -->
        <div
          v-for="(opt, index) in options"
          :key="opt.id || opt[valueField]"
          class="itbms-filter-item flex items-center px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100"
          @click="onOptionSelected(opt[valueField])"
          @mousedown.prevent
          data-cy="filter-option"
          :data-option-value="opt[valueField]"
        >
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
            <span class="ml-3 text-gray-700 select-none">{{
              opt[displayField]
            }}</span>
          </div>
        </div>

        <!-- Custom Price Input Section (only for price filter) -->
        <div v-if="isPriceFilter" class="border-t border-gray-200">
          <!-- Toggle Custom Input Button -->
          <div
            class="flex items-center px-4 py-3 hover:bg-gray-50 cursor-pointer"
            @click="toggleCustomInput"
            @mousedown.prevent
          >
            <div class="flex items-center">
              <svg
                class="w-4 h-4 text-blue-600 mr-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 4v16m8-8H4"
                />
              </svg>
              <span class="text-blue-600 select-none font-medium">
                {{ showCustomInput ? 'Hide Custom Range' : 'Custom Price Range' }}
              </span>
            </div>
          </div>

          <!-- Custom Input Fields -->
          <div v-if="showCustomInput" class="px-4 py-3 bg-gray-50 border-t border-gray-100">
            <div class="space-y-3">
              <div class="flex gap-2 items-center">
                <div class="flex-1">
                  <label class="block text-xs text-gray-600 mb-1">Min Price (Baht)</label>
                  <input
                    v-model="customMinPrice"
                    type="number"
                    min="0"
                    placeholder="0"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    @click.stop
                    @mousedown.stop
                  />
                </div>
                <span class="text-gray-400 mt-5">-</span>
                <div class="flex-1">
                  <label class="block text-xs text-gray-600 mb-1">Max Price (Baht)</label>
                  <input
                    v-model="customMaxPrice"
                    type="number"
                    min="0"
                    placeholder="999999"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    @click.stop
                    @mousedown.stop
                  />
                </div>
              </div>
              <div class="flex gap-2">
                <button
                  @click.stop="applyCustomPrice"
                  class="flex-1 bg-blue-600 text-white py-2 px-3 rounded-md text-sm hover:bg-blue-700 transition-colors"
                >
                  Apply
                </button>
                <button
                  @click.stop="clearCustomPrice"
                  class="flex-1 bg-gray-300 text-gray-700 py-2 px-3 rounded-md text-sm hover:bg-gray-400 transition-colors"
                >
                  Clear
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- No options message -->
        <div
          v-if="options.length === 0"
          class="px-4 py-3 text-gray-500 text-center"
        >
          No options available
        </div>
      </div>
    </div>

    <!-- Selected Values Display -->
    <div
      v-if="selectedValueList.length > 0"
      class="flex flex-wrap gap-2 flex-1"
    >
      <span
        v-for="(value, i) in selectedValueList"
        :key="i"
        class="flex items-center bg-blue-50 border border-blue-300 rounded-full px-3 py-1 text-sm text-blue-800 shadow-sm"
      >
        {{ value.includes('custom:') ? formatCustomPrice(value) : getDisplayText(value) }}
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
  </div>
</template>
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
  initialFilterValues: [String, Array], // Support both string and array
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
  mode:{
    type: String,
    default: "price"
  }
});

const emit = defineEmits(["filterChanged"]);

const selectedValueList = ref([]);
const dropdownOpen = ref(false);
const dropdownRef = ref(null);

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
    // Try parsing as JSON first (for array)
    const parsed = JSON.parse(raw);
    return normalizeToArray(parsed);
  } catch {
    // If parsing fails, treat as string
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
  emit("filterChanged", filterArray); // Emit array instead of string
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

onMounted(async() => {
  // Load from session storage first, then from props as fallback
  const sessionValue = getSessionFilterValues();
  const initialValue =
    sessionValue.length > 0
      ? sessionValue
      : normalizeToArray(props.initialFilterValues);

  selectedValueList.value = [...initialValue];
  document.addEventListener("click", handleClickOutside);
});

const filteredOptions = computed(() => {
  // ถ้า type เป็น priceOption ให้ตัดตัวสุดท้ายออก
  if (props.mode === "price") {
    return props.options.slice(0,6);
  }
  // ถ้าไม่ใช่ก็คืน options ตามเดิม
  return props.options;
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
        class="absolute top-full left-0 z-20 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-64 overflow-y-auto"
        v-if="dropdownOpen"
        data-cy="filter-options"
        :data-dropdown-open="dropdownOpen"
      >
        <div
          v-for="(opt, index) in filteredOptions"
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

            <!-- ชื่อ option -->
            <span class="ml-3 text-gray-700 select-none">{{
              opt[displayField]
            }}</span>

            <!-- ใส่ slot เฉพาะตัวสุดท้าย -->
            <span
              v-if="index === options.length - 1"
              class="ml-3 text-gray-700 select-none"
            >
            </span>
          </div>
        </div>

        <!-- Custom slot for InputPrice -->
        <slot name="InputPrice"></slot>
        

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
  </div>

</template>
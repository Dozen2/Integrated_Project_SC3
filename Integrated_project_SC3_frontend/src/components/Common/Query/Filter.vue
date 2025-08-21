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
  mode: {
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

onMounted(async () => {
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
    return props.options.slice(0, 6);
  }
  // ถ้าไม่ใช่ก็คืน options ตามเดิม
  return props.options;
});
</script>

<template>
  <div class="flex items-start gap-4">
    <!-- Label -->
    <span class="text-gray-800 font-semibold whitespace-nowrap text-sm">{{ label }}</span>

    <!-- Filter Dropdown -->
    <div ref="dropdownRef" class="itbms-filter relative flex flex-col flex-1 max-w-md">
      <!-- Dropdown Toggle Button -->
      <div
        class="itbms-filter-button group relative px-4 py-3 border-2 border-gray-200 rounded-xl cursor-pointer bg-white min-w-48 text-left hover:border-blue-300 hover:shadow-md transition-all duration-300 ease-out"
        @click="toggleDropdown" data-cy="filter-dropdown-toggle" role="button" tabindex="0" :class="{
          'border-blue-400 shadow-lg ring-2 ring-blue-100': dropdownOpen,
          'border-green-300 bg-green-50': selectedValueList.length > 0 && !dropdownOpen
        }">
        <div class="flex items-center justify-between">
          <span class="text-gray-700 font-medium" :class="{
            'text-green-700': selectedValueList.length > 0 && !dropdownOpen,
            'text-blue-700': dropdownOpen
          }">
            {{
              selectedValueList.length > 0
                ? `${selectedValueList.length} รายการที่เลือก`
                : placeholder || 'เลือกตัวกรอง...'
            }}
          </span>

          <!-- Badge แสดงจำนวน -->
          <div class="flex items-center gap-2">
            <span v-if="selectedValueList.length > 0"
              class="inline-flex items-center justify-center w-6 h-6 text-xs font-bold text-white bg-green-500 rounded-full">
              {{ selectedValueList.length }}
            </span>

            <!-- Arrow Icon -->
            <svg class="w-5 h-5 text-gray-400 transition-all duration-300 ease-out" :class="{
              'rotate-180 text-blue-500': dropdownOpen,
              'text-green-500': selectedValueList.length > 0 && !dropdownOpen
            }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Selected Values Display (Tags) -->
      <div v-if="selectedValueList.length > 0"
        class="flex flex-wrap gap-2 mt-3 p-3 bg-gray-50 rounded-lg border border-gray-100">
        <div class="w-full mb-1">
          <span class="text-xs font-medium text-gray-600 uppercase tracking-wide">รายการที่เลือก:</span>
        </div>
        <span v-for="(value, i) in selectedValueList" :key="i"
          class="group inline-flex items-center bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-full px-3 py-1.5 text-sm font-medium shadow-sm hover:from-blue-600 hover:to-blue-700 hover:shadow-md transition-all duration-200">
          <span class="mr-2">{{ getDisplayText(value) }}</span>
          <button @click="removeValue(i)"
            class="flex items-center justify-center w-5 h-5 bg-white/20 rounded-full hover:bg-white/30 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-white/50"
            aria-label="ลบตัวเลือก" title="ลบตัวเลือกนี้">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </span>

      </div>

      <!-- Dropdown Options -->
      <traโnsition enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 scale-95 translate-y-2" enter-to-class="opacity-100 scale-100 translate-y-0"
        leave-active-class="transition ease-in duration-150" leave-from-class="opacity-100 scale-100 translate-y-0"
        leave-to-class="opacity-0 scale-95 translate-y-2">
        <div v-if="dropdownOpen"
          class="absolute top-full left-0 z-30 w-full mt-2 bg-white border border-gray-200 rounded-xl shadow-xl max-h-80 overflow-hidden"
          data-cy="filter-options" :data-dropdown-open="dropdownOpen">
          <!-- Header -->
          <div class="sticky top-0 bg-gray-50 px-4 py-3 border-b border-gray-100">
            <div class="flex items-center justify-between">
              <span class="text-sm font-semibold text-gray-700">เลือกตัวกรอง</span>
              <span class="text-xs text-gray-500">
                {{ selectedValueList.length }} / {{ filteredOptions.length }} รายการ
              </span>
            </div>
          </div>

          <!-- Options List -->
          <div class="overflow-y-auto max-h-56">
            <div v-for="(opt, index) in filteredOptions" :key="opt.id || opt[valueField]"
              class="group flex items-center px-4 py-3 hover:bg-blue-50 cursor-pointer border-b border-gray-50 last:border-b-0 transition-colors duration-150"
              @click="onOptionSelected(opt[valueField])" @mousedown.prevent data-cy="filter-option"
              :data-option-value="opt[valueField]"
              :class="{ 'bg-blue-50 border-l-4 border-l-blue-500': selectedValueList.includes(opt[valueField]) }">
              <!-- Custom Checkbox -->
              <div class="relative">
                <input type="checkbox" :checked="selectedValueList.includes(opt[valueField])" class="sr-only"
                  readonly />
                <div class="w-5 h-5 border-2 rounded-md transition-all duration-200 flex items-center justify-center"
                  :class="selectedValueList.includes(opt[valueField])
                    ? 'bg-blue-500 border-blue-500'
                    : 'border-gray-300 group-hover:border-blue-400'">
                  <svg v-if="selectedValueList.includes(opt[valueField])" class="w-3 h-3 text-white" fill="none"
                    stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </div>

              <span
                class="ml-3 text-gray-700 select-none font-medium group-hover:text-blue-700 transition-colors duration-150"
                :class="{ 'text-blue-700': selectedValueList.includes(opt[valueField]) }">
                {{ opt[displayField] }}
              </span>
            </div>

            <slot name="InputPrice"></slot>
          </div>

          <!-- Custom slot for additional inputs -->
          <!-- <div v-if="$slots.InputPrice" class="border-t border-gray-100">
            <slot name="InputPrice"></slot>
          </div> -->
          

          <!-- Empty State -->
          <div v-if="options.length === 0" class="flex flex-col items-center justify-center py-8 px-4">
            <svg class="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.47-.881-6.08-2.33" />
            </svg>
            <span class="text-gray-500 text-sm font-medium">ไม่มีตัวเลือกที่สามารถเลือกได้</span>
            <span class="text-gray-400 text-xs mt-1">กรุณาลองค้นหาหรือโหลดข้อมูลใหม่</span>
          </div>
        </div>
      </traโnsition>
    </div>
  </div>
</template>
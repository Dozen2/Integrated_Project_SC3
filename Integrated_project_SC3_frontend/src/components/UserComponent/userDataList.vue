<script setup>
import { ref } from "vue";

const props = defineProps({
  label: String,
  placeholder: String,
  type: {
    type: String,
    default: "text",
  },
  modelValue: String,
  value: String,
  isValid: {
    type: Boolean,
    default: true,
  },
  isFirstInput: {
    type: Boolean,
    default: true,
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
  errorText: String,
});
</script>

<template>
  <div class="grid grid-cols-12 gap-4 items-center py-2 border-b border-blue-100">
    <!-- Label -->
    <span class="col-span-4 text-lg text-blue-700 font-medium">
      {{ props.label }}
    </span>

    <!-- Input (Edit mode) -->
    <div class="col-span-8" v-if="isEditMode">
      <input
        :value="props.value"
        :type="type"
        @input="(e) => { updateValue(e); validateValue(); }"
        @blur="handleBlur"
        :class="[
          'w-full rounded-lg px-3 py-2 border transition-colors focus:outline-none focus:ring-2',
          isValid || isFirstInput
            ? 'border-blue-300 focus:ring-blue-400'
            : 'border-red-400 focus:ring-red-300'
        ]"
      />
    </div>

    <!-- Value (View mode) -->
    <div class="col-span-8" v-else>
      <span class="text-lg text-gray-800">
        {{ props.value }}
      </span>
    </div>
  </div>
</template>

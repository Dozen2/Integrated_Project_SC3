<script setup>
import { onMounted, ref } from "vue";
const props = defineProps({
  label: String,
  placeholder: String,
  type: {
    type: String,
    default: "text"
  },
  modelValue: String,
  isValid: {
    type: Boolean,
    default: true
  },
  isFirstInput: {
    type: Boolean,
    default: true
  }
});

const emits = defineEmits(["update:modelValue"]);

function updateValue(e) {
  emits("update:modelValue", e.target.value);
}
function validateValue() {
  emits("validateValue");
}

</script>

<template>
  <div class="flex flex-col space-y-1">
    <label class="text-sm font-medium text-gray-600">{{ label }}</label>
    <input
      :type="type"
      :placeholder="placeholder"
      :value="modelValue"
      @input="updateValue"
      @blur="validateValue"
      :class="[
        'w-full rounded-lg px-3 py-2 focus:outline-none focus:ring-2',
        isValid || isFirstInput
          ? 'border focus:ring-blue-400' 
          : 'border border-red-500 focus:ring-red-400'
      ]"
    />
  </div>
</template>

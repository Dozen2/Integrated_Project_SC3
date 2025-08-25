<script setup>
import { onMounted, ref } from "vue";
const props = defineProps({
  label: String,
  placeholder: String,
  type: {
    type: String,
    default: "text",
  },
  modelValue: String,
  isValid: {
    type: Boolean,
    default: true,
  },
  isFirstInput: {
    type: Boolean,
    default: true,
  },
  errorText: String,
});

const emits = defineEmits(["update:modelValue", "validateValue"]);


function updateValue(e) {
  emits("update:modelValue", e.target.value);
}
function validateValue() {
  emits("validateValue");
}

function handleBlur(e) {
  let trimmed = e.target.value?.trim() ?? "";
  emits("update:modelValue", trimmed);   // อัปเดต v-model เป็นค่าที่ trim แล้ว
  emits("validateValue");                // validate ต่อได้เลย
}

</script>

<template>
  <div class="flex flex-col space-y-1">
    <label class="text-sm font-medium text-blue-600"
      >{{ label }}
      <span class="text-sm text-red-500" v-if="!isValid && !isFirstInput"
        >* {{ errorText }}</span
      ></label
    >

    <input
      :type="type"
      :placeholder="placeholder"
      :value="modelValue" 
       @input="(e) => { updateValue(e); validateValue(); }"
       @blur="handleBlur"
      :class="[
        'w-full rounded-lg px-3 py-2 focus:outline-none focus:ring-2',
        isValid || isFirstInput
          ? 'border focus:ring-blue-400'
          : 'border border-red-500 focus:ring-red-400',
      ]"
    />
  </div>
</template>

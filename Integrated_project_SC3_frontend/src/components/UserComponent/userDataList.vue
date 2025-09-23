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


const emits = defineEmits(["update:modelValue", "validateValue"]);

// เก็บ type จริงที่ input ใช้
const inputType = ref(props.type);

function updateValue(e) {
  emits("update:modelValue", e.target.value);
}
function validateValue() {
  emits("validateValue");
}

function handleBlur(e) {
  if(inputType.value != "password"){  
    let trimmed = e.target.value?.trim() ?? "";
    emits("update:modelValue", trimmed);   // อัปเดต v-model เป็นค่าที่ trim แล้ว
    emits("validateValue");                // validate ต่อได้เลย
  }
}
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
        :value="props.modelValue"
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
      <span class="text-sm text-red-500" v-show="!isValid && !isFirstInput"
        >* {{ errorText }}</span
      >
    </div>

    <!-- Value (View mode) -->
    <div class="col-span-8" v-else>
      <span class="itbms-nickname text-lg text-gray-800">
        {{ props.modelValue }}
      </span>
    </div>
  </div>
</template>

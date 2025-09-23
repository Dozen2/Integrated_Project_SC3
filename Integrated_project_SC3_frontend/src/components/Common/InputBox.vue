<script setup>
import { Eye, EyeClosed, EyeOff } from "lucide-vue-next";
import { Password } from "primevue";
import { onMounted, ref } from "vue";

const props = defineProps({
  label: String,
  placeholder: String,
  modelValue: String,
  errorText: String,
  type: {
    type: String,
    default: "text",
  },
  isValid: {
    type: Boolean,
    default: true,
  },
  isFirstInput: {
    type: Boolean,
    default: true,
  },
  inputClass: {
    type: String,
    default: "",
  }
});


const emits = defineEmits(["update:modelValue", "validateValue"]);

const showPassword = ref(false);

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

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value;
  inputType.value = showPassword.value ? "text" : "password";
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
<div class="relative">
    <input
      class="itbms-email"
      :type="inputType"
      :placeholder="placeholder"
      :value="modelValue" 
       @input="(e) => { updateValue(e); validateValue(); }"
       @blur="handleBlur"
      :class="[
        'w-full rounded-lg px-3 py-2 focus:outline-none focus:ring-2 itbms-email itbms-password',
        inputClass,
        isValid || isFirstInput
          ? 'border focus:ring-blue-400'
          : 'border border-red-500 focus:ring-red-400',
      ]"
    />
    <button
        v-if="type === 'password'"
        type="button"
        class="absolute inset-y-0 right-3 flex items-center text-gray-500"
        @click="togglePasswordVisibility"
      >
        <span v-if="showPassword"><Eye /></span>
        <span v-else><EyeOff /></span>
      </button>
  </div>
  </div>
</template>

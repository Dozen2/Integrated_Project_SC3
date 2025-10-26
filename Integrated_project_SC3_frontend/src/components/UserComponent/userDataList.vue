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
  classname: {
    type: String,
    default: "",
  },
    disabled: {               // 👈 เพิ่มตรงนี้
    type: Boolean,
    default: false,
  }
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
  <!-- 🔹 ปรับ Container ให้เป็น Stacked (flex-col) ในจอเล็ก, และเป็น Grid ในจอใหญ่ (sm:grid) -->
  <div class="flex flex-col sm:grid sm:grid-cols-12 gap-1 sm:gap-4 py-1 sm:py-2 border-b border-blue-100">
    
    <!-- Label: ใช้ w-full ในจอเล็ก (Stacking), ใช้ col-span-4 ในจอใหญ่ (Grid) -->
    <!-- เพิ่ม mb-1 ในจอเล็กเพื่อให้มีระยะห่างจาก Input/Value -->
    <span class="w-full sm:col-span-4 text-sm sm:text-lg text-blue-700 font-medium mb-1 sm:mb-0">
      {{ props.label }}
    </span>
    

    <!-- Input (Edit mode): ใช้ w-full ในจอเล็ก (Stacking), ใช้ col-span-8 ในจอใหญ่ (Grid) -->
    <div class="w-full sm:col-span-8" v-if="isEditMode">
      
      <input
        :value="props.modelValue"
        :type="type"
        :disabled="props.disabled"
        @input="(e) => { updateValue(e); validateValue(); }"
        @blur="handleBlur"
        :class="[
          props.classname,
          // ใช้ w-full และลดขนาด text
          'w-full rounded-lg px-2 py-1 sm:px-3 sm:py-2 border transition-colors focus:outline-none focus:ring-2 text-sm sm:text-base', 
          isValid || isFirstInput
            ? 'border-blue-300 focus:ring-blue-400'
            : 'border-red-400 focus:ring-red-300'
        ]"
      />
      <span class="text-xs sm:text-sm text-red-500" v-show="!isValid && !isFirstInput"
        >* {{ errorText }}</span
      >
    </div>

    <!-- Value (View mode): ใช้ w-full ในจอเล็ก (Stacking), ใช้ col-span-8 ในจอใหญ่ (Grid) -->
    <div class="w-full sm:col-span-8" v-else>
      <span :class="[props.classname,'text-sm sm:text-lg text-gray-800 break-words']">
        {{ props.modelValue }}
      </span>
    </div>
  </div>
</template>
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
  <div class="mb-3 justify-between items-center grid grid-cols-3 gap-4 space-y-4">
    <span class="text-xl text-gray-600 pl-[150px] col-start-1">{{ props.label }}</span>
    <div class="col-start-3" v-if="isEditMode" >
      <input
      :value="props.value"
      :type="type" 
       @input="(e) => { updateValue(e); validateValue(); }"
       @blur="handleBlur"
      :class="[
        'flex w-full rounded-lg px-3 py-2 focus:outline-none focus:ring-2 ',
        isValid || isFirstInput
          ? 'border focus:ring-blue-400'
          : 'border border-red-500 focus:ring-red-400',
      ]"
    />
    </div>
    <div class="col-start-3" v-else>
      <span class="text-xl font-medium text-gray-800 pr-[30px] ">{{props.value}}</span>
    </div>
  </div>
</template>
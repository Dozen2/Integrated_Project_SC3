<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  type: {
    type: String,
    default: 'text', // text, number, textarea
    validator: (value) => ['text', 'number', 'textarea'].includes(value)
  },
  modelValue: {
    type: [String, Number, null],
    default: ''
  },
  label: {
    type: String,
    required: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  // text / textarea
  maxLength: {
    type: Number,
    default: null
  },
  // number
  min: {
    type: Number,
    default: null
  },
  step: {
    type: [Number, String],
    default: 1
  },
  prefix: {
    type: String,
    default: ''
  },
  suffix: {
    type: String,
    default: ''
  },
  clearOnFocus: {
    type: Boolean,
    default: false
  },
  // textarea
  rows: {
    type: Number,
    default: 3
  },
  error: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
    inputClass: { 
      type: String, 
      default: '' }, // รับค่ามา
});

const emit = defineEmits(['update:modelValue', 'focus', 'blur']);



const inputClass = computed(() => {
  // ถ้ามี inputClass ส่งมาจากภายนอก ให้ใช้อันนั้น
  if (props.inputClass && props.inputClass.trim() !== '') {
    return props.inputClass;
  }

  // ไม่เช่นนั้นใช้ระบบเดิม
  const baseClass =
    'block w-full rounded-md shadow-sm focus:outline-none transition-all duration-200 sm:text-sm';
  const paddingClass = props.prefix ? 'pl-7' : props.suffix ? 'pr-12' : '';

  if (props.error) {
    return `${baseClass} ${paddingClass} border-2 border-red-400 focus:border-red-500 focus:ring-2 focus:ring-red-500`;
  }
  return `${baseClass} ${paddingClass} border border-gray-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-500`;
});

const handleInput = (event) => {
  let value = event.target.value;

  if (props.type === 'number') {
    if (value === '' || value === null) {
      emit('update:modelValue', null);
      return;
    }
    const numValue = Number(value);
    emit('update:modelValue', isNaN(numValue) ? null : numValue);
  } else {
    emit('update:modelValue', value);
  }
};

const handleFocus = (event) => {
  if (props.type === 'number' && props.clearOnFocus) {
    emit('update:modelValue', null);
  }
  emit('focus', event);
};

const handleBlur = (event) => {
  if (props.type !== 'number' && typeof props.modelValue === 'string') {
    emit('update:modelValue', props.modelValue.trim());
  }
  emit('blur', event);
};
</script>

<template>
  <div>
    <!-- Label -->
    <label :for="label" class="block text-sm font-medium text-gray-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div v-if="type === 'number'" class="mt-1 relative rounded-md shadow-sm">
      <div v-if="prefix" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
        <span class="text-gray-500 sm:text-sm">{{ prefix }}</span>
      </div>

      <input 
      :id="label" 
      type="number" 
      :value="modelValue" 
      @input="handleInput" 
      @focus="handleFocus" 
      @blur="handleBlur"
      :class="inputClass" 
      :placeholder="placeholder" 
      :disabled="disabled" 
      :min="min" 
      :step="step" />

      <div v-if="suffix" class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
        <span class="text-gray-500 sm:text-sm">{{ suffix }}</span>
      </div>
    </div>

    <div v-else-if="type === 'textarea'" class="mt-1">
      <textarea :id="label" :value="modelValue" @input="handleInput" @blur="handleBlur" :rows="rows" :class="inputClass"
        :placeholder="placeholder" :disabled="disabled" :maxlength="maxLength"></textarea>
    </div>

    <div v-else class="mt-1">
      <input :id="label" type="text" :value="modelValue" @input="handleInput" @blur="handleBlur" :class="inputClass"
        :placeholder="placeholder" :disabled="disabled" :maxlength="maxLength" />
    </div>

    <!-- Error messages -->
    <p v-if="error && errorMessage" class="mt-2 text-sm text-red-600">
      {{ errorMessage }}
    </p>

    <p v-if="maxLength && typeof modelValue === 'string' && modelValue.length > maxLength"
      class="mt-2 text-sm text-red-600">
      {{ label }} must be 1-{{ maxLength }} characters long.
    </p>

    <p v-if="type === 'number' && min !== null && modelValue !== null && modelValue < min"
      class="mt-2 text-sm text-red-600">
      {{ label }} must be {{ min }} or greater.
    </p>
  </div>
</template>

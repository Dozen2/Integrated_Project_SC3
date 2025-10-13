<template>
  <div class="min-h-[calc(100vh-80px)] flex items-center justify-center bg-gradient-to-br from-blue-100 via-blue-50 to-blue-200">
    <div
      class="relative bg-white/80 backdrop-blur-lg shadow-2xl rounded-[2rem] p-12 pb-30 w-full max-w-5xl flex flex-col md:flex-row gap-12 transition-all hover:shadow-blue-200"
    >
      <!-- Left section -->
      <div class="flex-1 flex flex-col justify-center">
        <div
          class="flex items-center gap-3 mb-6 border-b border-blue-100 pb-4"
        >
          <Smartphone class="w-12 h-12 text-blue-700" />
          <h1 class="text-4xl text-blue-800 tracking-tight">
            {{ label }}
          </h1>
        </div>
        <p class="text-gray-600 text-lg leading-relaxed">
          {{ description }}
        </p>
      </div>

      <!-- Right section (slot for form) -->
      <div
        class="flex-1 flex flex-col justify-center p-8 "
      >
        <slot name="form" />
      </div>

      <!-- Decorative glowing ring -->
      <div
        class="absolute -top-10 -right-10 w-40 h-40 bg-blue-300/30 rounded-full blur-3xl pointer-events-none"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { Smartphone } from "lucide-vue-next";

defineProps({
  label: {
    type: String,
    default: "Sign in",
  },
  description: {
    type: String,
    default:
      "with your Google Account. This account will be available to other Google apps in the browser.",
  },
});
</script>

<script setup>
import { RouterLink } from "vue-router";
</script>

<template>
  <div class="min-h-[calc(100vh-80px)] flex flex-col items-center justify-center bg-gray-100 px-6 py-12">
    <div
      class="bg-white shadow-xl rounded-2xl p-10 w-full max-w-2xl text-center border border-gray-200"
    >
      <h1 class="text-3xl font-semibold text-blue-700 mb-4">
        Please Verify Your Email
      </h1>

      <p class="text-gray-600 text-lg leading-relaxed mb-8">
        We’ve sent a verification link to your registered email address.  
        <span class="block mt-2">
          Please check your inbox and verify your email before proceeding to
          change your password.
        </span>
      </p>

      <div class="flex flex-col items-center gap-4">
        <RouterLink
          :to="{ name: 'Home' }"
          class="bg-blue-600 text-white px-8 py-3 rounded-lg font-medium hover:bg-blue-700 transition duration-200"
        >
          Back to Home Page
        </RouterLink>
      </div>
    </div>

    <p class="text-xs text-gray-400 mt-10">© 2025 ITBMS Team</p>
  </div>
</template>

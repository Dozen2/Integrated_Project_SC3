<script setup>
import { ref } from "vue";
import InputBox from "./../Common/InputBox.vue";

const nickname = ref("");
const fullName = ref("");
const email = ref("");
const password = ref("");

// เก็บสถานะ validate แต่ละช่อง
const valid = ref({
  nickname: true,
  fullName: true,
  email: true,
  password: true,
});

// เก็บสถานะว่าเป็นการกรอกครั้งแรกหรือยัง
const isFirstInput = ref({
  nickname: true,
  fullName: true,
  email: true,
  password: true,
});

// ฟังก์ชัน validate
function validateNickname(value) {
  valid.value.nickname = value.trim().length > 0;
}
function validateFullName(value) {
  valid.value.fullName = value.trim().length > 0;
}
function validateEmail(value) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  valid.value.email = regex.test(value);
}
function validatePassword(value) {
  valid.value.password = value.length >= 6;
}
</script>
<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 via-white to-blue-200"
  >
    <div class="bg-white shadow-lg rounded-2xl p-8 w-full max-w-md">
      <!-- Title -->
      <h2 class="text-2xl font-bold text-blue-700 text-center mb-6">
        Buyer Signup
      </h2>

      <!-- Form -->
      <form class="flex flex-col gap-4">
        <!-- Nickname -->
        <InputBox
          label="Nickname"
          type="text"
          placeholder="Enter your nickname"
          v-model="nickname"
          :isValid="valid.nickname"
          :isFirstInput="isFirstInput.nickname"
          @validateValue="validateNickname"
        />

        <InputBox
          label="Full Name"
          type="text"
          placeholder="Enter your full name"
          v-model="fullName"
          :isValid="valid.fullName"
          :isFirstInput="isFirstInput.fullName"
          @validateValue="validateFullName"
        />

        <InputBox
          label="Email"
          type="email"
          placeholder="Enter your email"
          v-model="email"
          :isValid="valid.email"
          :isFirstInput="isFirstInput.email"
          @validateValue="validateEmail"
        />

        <InputBox
          label="Password"
          type="password"
          placeholder="Enter your password"
          v-model="password"
          :isValid="valid.password"
          :isFirstInput="isFirstInput.password"
          @validateValue="validatePassword"
        />

        <!-- Buttons -->
        <div class="mt-4 flex flex-col gap-3">
          <button
            type="submit"
            class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Sign Up
          </button>
          <RouterLink
            :to="{ name: 'Products' }"
            class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition"
          >
            Cancel
          </RouterLink>
        </div>

        <!-- Already have account -->
        <p class="text-center text-sm text-gray-600 mt-6">
          Already have an account?
          <RouterLink
            :to="{ name: 'Login' }"
            class="text-blue-600 font-medium hover:underline"
          >
            Login here
          </RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>

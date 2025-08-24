<script setup>
import { ref } from "vue"
import { RouterLink } from "vue-router"

const nickname = ref("")
const fullname = ref("")
const email = ref("")
const password = ref("")

// emit event ออกไปให้ parent ใช้ได้
const emit = defineEmits(["signup"])

const handleSignUp = () => {
  const formData = {
    nickname: nickname.value,
    fullname: fullname.value,
    email: email.value,
    password: password.value
  }
  // ส่งข้อมูลออกไปให้ parent ใช้
  emit("signup", formData)
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 via-white to-blue-200">
    <div class="bg-white shadow-lg rounded-2xl p-8 w-full max-w-md">
      <!-- Title -->
      <h2 class="text-2xl font-bold text-blue-700 text-center mb-6">Buyer Signup</h2>

      <!-- Form -->
      <form class="flex flex-col gap-4" @submit.prevent="handleSignUp">
        <!-- Nickname -->
        <div>
          <label class="block text-sm font-medium text-blue-800 mb-1">Nickname</label>
          <input 
            v-model="nickname"
            type="text" 
            placeholder="Enter your nickname"
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- Full Name -->
        <div>
          <label class="block text-sm font-medium text-blue-800 mb-1">Full Name</label>
          <input 
            v-model="fullname"
            type="text" 
            placeholder="Enter your full name"
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- Email -->
        <div>
          <label class="block text-sm font-medium text-blue-800 mb-1">Email</label>
          <input 
            v-model="email"
            type="email" 
            placeholder="Enter your email"
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- Password -->
        <div>
          <label class="block text-sm font-medium text-blue-800 mb-1">Password</label>
          <input 
            v-model="password"
            type="password" 
            placeholder="Enter your password"
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- Buttons -->
        <div class="mt-4 flex flex-col gap-3">
          <button 
            type="submit"
            class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
            Sign Up
          </button>
          <RouterLink 
            :to="{ name: 'Home' }"
            class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition">
            Cancel
          </RouterLink>
        </div>

        <!-- Already have account -->
        <p class="text-center text-sm text-gray-600 mt-6">
          Already have an account? 
          <RouterLink 
            :to="{ name: 'Login' }" 
            class="text-blue-600 font-medium hover:underline">
            Login here
          </RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>

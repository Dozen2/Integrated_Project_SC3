<script setup>
import InputBox from "@/components/Common/InputBox.vue";
import { ref, reactive, computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { useAuthStore } from "../../stores/auth";


const alertStore = useAlertStore();
const route = useRouter();

const email = ref("");
const password = ref("");

const form = reactive({
  email: {
    errorText: "Invalid email format",
    isValid: false,
    isFirstInput: true,
  },
  password: {
    errorText:
      "Password must be less than 40 characters",
    isValid: false,
    isFirstInput: true,
  },
});

const isFormValid = computed(() => {
  const results = Object.entries(form).map(([key, f]) => ({
    field: key,
    isValid: f.isValid ?? null, 
    isFirstInput: f.isFirstInput ?? null,
  }));

  console.table(results);

  return results.filter((r) => r.isValid !== null).every((r) => r.isValid);
});

// =================== Validation ===================
const validateEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  form.email.isValid = regex.test(email.value);
  updateIsFirstInput("email", email.value);
};

const validatePassword = () => {
  form.password.isValid = /^.{0,39}$/.test(password.value)
  updateIsFirstInput("password", password.value);
};

// Update isFirstInput
const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

const loading = ref(false);
const authStore = useAuthStore();

const summitForm = async () => {
  try {
    loading.value = true;
    // const res = await registerUser(formData);
    await authStore.login(email.value, password.value);
    loading.value = false;
    alertStore.addToast(
      "The user account has been successfully registered.",
      "Create buyer successful.",
      "success",
      5000
    );
    console.log(authStore.role);
    route.push("/sale-items");
  } catch (err) {
    loading.value = false;
    alertStore.addToast("Email or Password incorrect.", err.message, "error");
  }
};
</script>

<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 via-white to-blue-200"
  >
    <!-- กล่องหลัก -->
    <div
      class="bg-white shadow-xl rounded-2xl w-full max-w-4xl flex overflow-hidden"
    >
      <!-- ฝั่งซ้าย: Login Form -->
      <div class="w-1/2 p-10 flex flex-col justify-center">
        <!-- Title -->
        <h2 class="text-2xl font-bold text-blue-700 mb-6">
          Login to Your Account
        </h2>

        <!-- Form -->
        <form class="flex flex-col gap-4" @submit.prevent="summitForm">
          <InputBox
            label="Email"
            placeholder="Enter email"
            v-model="email"
            :isValid="form.email.isValid"
            :isFirstInput="form.email.isFirstInput"
            :errorText="form.email.errorText"
            @validateValue="validateEmail"
          />

          <InputBox
            label="Password"
            type="password"
            placeholder="Enter password"
            v-model="password"
            :isValid="form.password.isValid"
            :isFirstInput="form.password.isFirstInput"
            :errorText="form.password.errorText"
            @validateValue="validatePassword"
          />

          <!-- Buttons -->
          <div class="flex flex-col gap-3 mt-4">
            <button
              type="submit"
              :disabled="loading || !isFormValid"
              :class="[
                'w-full py-2 rounded-lg transition',
                loading || !isFormValid
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-blue-600 text-white hover:bg-blue-700',
              ]"
            >
              {{ loading ? "Loading..." : "Login" }}
            </button>

            <RouterLink
              :to="{ name: 'Products' }"
              class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition"
            >
              Cancel
            </RouterLink>

            <RouterLink
              :to="{ name: 'Register' }"
              class="w-full text-center text-blue-700 py-2 rounded-lg hover:bg-blue-200 transition"
            >
              Create Account
            </RouterLink>
          </div>
        </form>
      </div>

      <!-- ฝั่งขวา: Logo -->
      <div class="w-1/2 bg-blue-50 flex items-center justify-center p-10">
        <div class="flex flex-col items-center">
          <!-- ใช้ svg/รูปแทน logo -->
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-28 w-28 text-blue-600 mb-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
            />
          </svg>
          <h1 class="text-2xl font-bold text-blue-700">ITBMS_SHOP</h1>
        </div>
      </div>
    </div>
  </div>
</template>

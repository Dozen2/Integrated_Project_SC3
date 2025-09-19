<script setup>
import { computed, reactive, ref } from "vue";
import InputBox from "./../Common/InputBox.vue";
import { registerUser } from "@/libs/callAPI/apiAuth";
import { RouterLink, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";

const alertStore = useAlertStore();
const route = useRouter();

const nickname = ref("");
const fullname = ref("");
const email = ref("");
const password = ref("");

const form = reactive({
  nickname: {
    errorText: "nickname is required",
    isValid: false,
    isFirstInput: true,
  },
  fullname: {
    errorText: "Full name must be 4–40 characters",
    isValid: false,
    isFirstInput: true,
  },
  email: {
    errorText: "Invalid email format",
    isValid: false,
    isFirstInput: true,
  },
  password: {
    errorText:
      "Password must be 8+ chars with uppercase, lowercase, number, special char",
    isValid: false,
    isFirstInput: true,
  },
});

const isFormValid = computed(() => {
  const results = Object.entries(form).map(([key, f]) => ({
    field: key,
    isValid: f.isValid ?? null, // ถ้าไม่มี isValid จะได้ null
    isFirstInput: f.isFirstInput ?? null,
  }));

  console.table(results); // log สวยๆ ดูได้ว่าฟิลด์ไหนผ่านไม่ผ่าน

  return results.filter((r) => r.isValid !== null).every((r) => r.isValid);
});

// =================== Validation ===================
const validatenickname = () => {
  form.nickname.isValid = nickname.value.trim().length > 0;
  updateIsFirstInput("nickname", nickname.value);
};

const validateFullname = () => {
  const len = fullname.value.trim().length;
  form.fullname.isValid = len >= 4 && len <= 40;
  updateIsFirstInput("fullname", fullname.value);
};

const validateEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  form.email.isValid = regex.test(email.value);
  updateIsFirstInput("email", email.value);
};

const validatePassword = () => {
  form.password.isValid =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$_!%*?&#/\-+^()=\[\]{}><])[A-Za-z\d@$!%*_?&#/\-+^()=\[\]{}><]{8,}$/.test(
      password.value
    );
  updateIsFirstInput("password", password.value);
};

// Update isFirstInput
const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

const loading = ref(false);

const summitForm = async () => {
  try {
    const formData = {
      nickName: nickname.value,
      fullName: fullname.value,
      email: email.value,
      passwords: password.value,
      role: "BUYER",
      mobileNumber: "",
      bankAccountNumber: "",
      bankName: "",
      nationalId: "",
    };

    loading.value = true;
    const res = await registerUser(formData);
    loading.value = false;
    console.log("✅ Register success:", res);
    alertStore.addToast(
      "The user account has been successfully registered.",
      "Create buyer successful.",
      "success",
      5000
    );
    route.push({ name: "Products" });
  } catch (err) {
        loading.value = false;
    alertStore.addToast(err.message, "Register failed", "error");
  }
};
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
      <form class="flex flex-col space-y-2" @submit.prevent="summitForm">
        <InputBox
          label="nickname"
          placeholder="Enter nickname"
          v-model="nickname"
          :isValid="form.nickname.isValid"
          :isFirstInput="form.nickname.isFirstInput"
          :errorText="form.nickname.errorText"
          @validateValue="validatenickname"
        />

        <InputBox
          label="Full Name"
          placeholder="Enter full name"
          v-model="fullname"
          :isValid="form.fullname.isValid"
          :isFirstInput="form.fullname.isFirstInput"
          :errorText="form.fullname.errorText"
          @validateValue="validateFullname"
        />
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
        <div class="flex flex-col space-y-3 mt-4">
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
            {{ loading ? "Loading..." : "Sign Up" }}
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

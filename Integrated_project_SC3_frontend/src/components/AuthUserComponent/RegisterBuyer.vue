<script setup>
import { computed, reactive, ref } from "vue";
import InputBox from "./../Common/InputBox.vue";

const submittedForms = ref([]); // เก็บ submission หลายชุด


const nickname = ref("");
const fullname = ref("");
const email = ref("");
const password = ref("");

const form = reactive({
  nickname: {
    errorText: "Nickname is required",
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
  }});

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
const validateNickname = () => {
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
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(
      password.value
    );
  updateIsFirstInput("password", password.value);
};


// Update isFirstInput
const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};const summitForm = () => {
  if (isFormValid.value) {
    // รวมค่าแต่ละ field เป็น object
    const formData = {
      nickname: nickname.value,
      fullname: fullname.value,
      email: email.value,
      password: password.value,
    };

    // push เข้า array เก็บ submission
    submittedForms.value.push(formData);

    console.log("Form is valid. Submitting...", formData);
  } else {
    console.log("Form has errors. Please fix them.");
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
          label="Nickname"
          placeholder="Enter nickname"
          v-model="nickname"
          :isValid="form.nickname.isValid"
          :isFirstInput="form.nickname.isFirstInput"
          :errorText="form.nickname.errorText"
          @validateValue="validateNickname"
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
          type="email"
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
            :disabled="!isFormValid"
            :class="[
              'w-full py-2 rounded-lg transition',
              isFormValid
                ? 'bg-blue-600 text-white hover:bg-blue-700'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed',
            ]"
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

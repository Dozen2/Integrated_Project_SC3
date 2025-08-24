<script setup>
import { ref, reactive, computed } from "vue";
import InputBox from "./../Common/InputBox.vue";

const nickname = ref("");
const fullname = ref("");
const email = ref("");
const password = ref("");
const shopName = ref("");
const bankAccount = ref("");
const nationalId = ref("");
const phoneNumber = ref("");

// เก็บ state ของ validation
const valid = reactive({
  nickname: false,
  fullname: false,
  email: false,
  password: false,
  shopName: false,
  bankAccount: false,
  nationalId: false,
  phoneNumber: false,
});
const isFirstInput = reactive({
  nickname: true,
  fullname: true,
  email: true,
  password: true,
  shopName: true,
  bankAccount: true,
  nationalId: true,
  phoneNumber: true,
});

// Computed property เพื่อเช็คว่าฟอร์มทั้งหมด valid หรือไม่
const isFormValid = computed(() => {
  console.log(Object.values(valid).every((v) => v));
  return Object.values(valid).every((v) => v);
});

// Validation functions สำหรับแต่ละ field
const validateNickname = () => {
  valid.nickname =
    nickname.value.trim().length > 0 && nickname.value.trim().length <= 2;
  isFirstInput.nickname =
    isFirstInput.nickname && nickname.value == "" ? true : false;
};

const validateFullname = () => {
  valid.fullname = fullname.value.trim().length > 0;
  isFirstInput.fullname =
    isFirstInput.fullname && fullname.value == "" ? true : false;
};

const validateEmail = () => {
  valid.email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value);

  isFirstInput.email = isFirstInput.email && email.value == "" ? true : false;
};

const validatePassword = () => {
  valid.password = password.value.length >= 6;
  isFirstInput.password =
    isFirstInput.password && password.value == "" ? true : false;
};

const validateShopName = () => {
  valid.shopName = shopName.value.trim().length > 0;
  isFirstInput.shopName =
    isFirstInput.shopName && shopName.value == "" ? true : false;
};

const validateBankAccount = () => {
  valid.bankAccount =
    /^[0-9]+$/.test(bankAccount.value) && bankAccount.value.length >= 6;
  isFirstInput.bankAccount =
    isFirstInput.bankAccount && bankAccount.value == "" ? true : false;
};

const validateNationalId = () => {
  valid.nationalId = /^[0-9]{13}$/.test(nationalId.value);
  isFirstInput.nationalId =
    isFirstInput.nationalId && nationalId.value == "" ? true : false;
};

const validatePhoneNumber = () => {
  valid.phoneNumber = phoneNumber.value.trim().length > 0;
  isFirstInput.phoneNumber =
    isFirstInput.phoneNumber && phoneNumber.value == "" ? true : false;
};

const summitForm = () => {
  if (isFormValid.value) {
    // Submit form
    console.log("Form is valid. Submitting...");
  } else {
    console.log("Form has errors. Please fix them.");
  }
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-blue-50">
    <div class="bg-white shadow-xl rounded-2xl p-6 w-full max-w-lg m-10">
      <!-- Title -->
      <h2 class="text-2xl font-bold text-center text-blue-700 mb-4">
        Seller Sign Up
      </h2>

      <!-- Form -->
      <form class="flex flex-col space-y-2" @submit.prevent="summitForm">
        <InputBox
          label="Nickname"
          placeholder="Enter nickname"
          v-model="nickname"
          :isValid="valid.nickname"
          :isFirstInput="isFirstInput.nickname"
          @validateValue="validateNickname"
        />
        {{ nickname }}
        <InputBox
          label="Full Name"
          placeholder="Enter full name"
          v-model="fullname"
          :isValid="valid.fullname"
          :isFirstInput="isFirstInput.fullname"
          @validateValue="validateFullname"
        />

        <InputBox
          label="Email"
          type="email"
          placeholder="Enter email"
          v-model="email"
          :isValid="valid.email"
          :isFirstInput="isFirstInput.email"
          @validateValue="validateEmail"
        />

        <InputBox
          label="Password"
          type="password"
          placeholder="Enter password"
          v-model="password"
          :isValid="valid.password"
          :isFirstInput="isFirstInput.password"
          @validateValue="validatePassword"
        />

        <InputBox
          label="Shop Name"
          placeholder="Enter shop name"
          v-model="shopName"
          :isValid="valid.shopName"
          :isFirstInput="isFirstInput.shopName"
          @validateValue="validateShopName"
        />

        <!-- Phone -->
        <InputBox
          label="Phone"
          type="tel"
          placeholder="Enter phone number"
          v-model="phoneNumber"
          :isValid="valid.phoneNumber"
          :isFirstInput="isFirstInput.phoneNumber"
          @validateValue="validatePhoneNumber"
        />

        <!-- Bank -->
        <div class="flex flex-col space-y-1">
          <label class="text-sm font-medium text-gray-600">Bank</label>
          <select
            class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          >
            <option value="">Select bank</option>
            <option value="kbank">Kasikorn Bank</option>
            <option value="scb">Siam Commercial Bank</option>
            <option value="bbl">Bangkok Bank</option>
            <option value="ktb">Krungthai Bank</option>
          </select>
        </div>

        <InputBox
          label="Bank Account"
          placeholder="Enter bank account number"
          v-model="bankAccount"
          :isValid="valid.bankAccount"
          :isFirstInput="isFirstInput.bankAccount"
          @validateValue="validateBankAccount"
        />

        <InputBox
          label="National ID"
          placeholder="Enter national ID"
          v-model="nationalId"
          :isValid="valid.nationalId"
          :isFirstInput="isFirstInput.nationalId"
          @validateValue="validateNationalId"
        />

        <!-- Upload National ID card -->
        <div class="flex flex-col space-y-1">
          <label class="text-sm font-medium text-gray-600"
            >National ID Card - Front</label
          >
          <input
            type="file"
            accept="image/*"
            class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
        </div>

        <div class="flex flex-col space-y-1">
          <label class="text-sm font-medium text-gray-600"
            >National ID Card - Back</label
          >
          <input
            type="file"
            accept="image/*"
            class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
        </div>

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
        <p class="text-center text-sm text-gray-600 mt-4">
          Already have an account?
          <RouterLink
            :to="{ name: 'Login' }"
            class="text-blue-600 font-medium hover:underline"
          >
            Login
          </RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>

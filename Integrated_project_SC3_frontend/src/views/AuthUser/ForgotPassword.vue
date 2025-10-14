<script setup>
import { computed, reactive, ref } from "vue";
import InputBox from "@/components/Common/InputBox.vue";
import { registerUser, sendResetPasswordEmail } from "@/libs/callAPI/apiAuth";
import { RouterLink, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import FormInputBox from "@/components/Common/FormInputBox.vue";

const alertStore = useAlertStore();
const route = useRouter();
const email = ref("");

const form = reactive({
  email: {
    errorText: "Invalid email format",
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

const validateEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  form.email.isValid = regex.test(email.value);
  updateIsFirstInput("email", email.value);
};

const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

const loading = ref(false);

const summitForm = async () => {
  try {
    loading.value = true;
    const res = await sendResetPasswordEmail(email.value);
    loading.value = false;
    console.log("✅ Register success:", res);
    alertStore.addToast("Please check your email inbox to verify your password reset request.", "Email Sent Successfully", "success");

    route.push({ name: "CheckEmail" });
  } catch (err) {
    loading.value = false;
    alertStore.addToast(err.message, "Email Sent Error", "error");
  }
};
</script>
<template>
  <FormInputBox label="Forgot Password" description="Please enter your email so we can verify your account and help you reset your password.">
    <template #form>
      <form class="flex flex-col space-y-2" @submit.prevent="summitForm">
        <InputBox
          label="Email"
          placeholder="Enter email"
          v-model="email"
          :isValid="form.email.isValid"
          :isFirstInput="form.email.isFirstInput"
          :errorText="form.email.errorText"
          @validateValue="validateEmail"
        />

        <div class="fixed bottom-15 right-20 flex justify-end items-center gap-4 z-50">
          <!-- ปุ่ม Cancel -->
          <RouterLink :to="{ name: 'Products' }" class="px-6 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-100 hover:text-gray-800 transition-all duration-200 shadow-sm">
            Cancel
          </RouterLink>

          <!-- ปุ่ม Verify -->
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            :class="[
              'px-6 py-2 rounded-lg font-medium shadow-md transition-all duration-200',
              loading || !isFormValid ? 'bg-gray-300 text-gray-500 cursor-not-allowed' : 'bg-blue-600 text-white hover:bg-blue-700',
            ]"
          >
            {{ loading ? "Loading..." : "Verify" }}
          </button>
        </div>
      </form>
    </template>
  </FormInputBox>
</template>

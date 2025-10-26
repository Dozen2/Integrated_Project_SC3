<script setup>
import InputBox from "@/components/Common/InputBox.vue";
import { ref, reactive, computed, onMounted } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { useAuthStore } from "../../stores/auth";
import { resetPassword, verifyEmail } from "@/libs/callAPI/apiAuth";
import { decodeAccessToken } from "@/libs/utils";
import FormInputBox from "@/components/Common/FormInputBox.vue";


const newPassword = ref("");
const confirmPassword = ref("");
const isLoading = ref("");
const alertStore = useAlertStore();
const token = ref("");
const route = useRouter();

onMounted(async () => {
  isLoading.value = true;
  const urlParams = new URLSearchParams(window.location.search);
  token.value = urlParams.get("token");
  try {
    // console.log("decodeAccessToken(token): ", decodeAccessToken(token.value).sub);
  } catch (error) {
    route.push({ name: "PageNotFound" });
  }
  isLoading.value = false;
});

const form = reactive({
  newPassword: {
    errorText: "Password must be less than 40 characters",
    isValid: false,
    isFirstInput: true,
  },
  confirmPassword: {
    errorText: "Password must be less than 40 characters",
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

const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

const validateNewPassword = () => {
  form.newPassword.isValid = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$_!%*?&#/\-+^()=\[\]{}><])[A-Za-z\d@$!%*_?&#/\-+^()=\[\]{}><]{8,}$/.test(newPassword.value);
  updateIsFirstInput("newPassword", newPassword.value);
};

const validateConfirmPassword = () => {
  form.confirmPassword.isValid = /^.{1,39}$/.test(confirmPassword.value);
  updateIsFirstInput("confirmPassword", confirmPassword.value);
};

const summitForm = async () => {
  try {
    if (newPassword.value != confirmPassword.value) {
      alertStore.addToast("Passwords do not match", "New password and confirmation password do not match.", "error");
      return;
    }
    await resetPassword(token.value, newPassword.value, confirmPassword.value);
    alertStore.addToast("Your password has been changed successfully. Please log in with your new credentials.", "Password Changed Successfully", "success");

    route.push({ name: "Login" });
  } catch (err) {
    loading.value = false;
    alertStore.addToast(err.message, "Reset Password Error", "error");
  }
};
</script>
<template>
  <FormInputBox label="Reset Password" description="Please enter your new password below. Make sure it’s strong and easy for you to remember.">
    <template #form>
      <form class="flex flex-col gap-4" @submit.prevent="summitForm">
        <InputBox
          label="New Password"
          type="password"
          placeholder="Enter new password"
          v-model="newPassword"
          :isValid="form.newPassword.isValid"
          :isFirstInput="form.newPassword.isFirstInput"
          :errorText="form.newPassword.errorText"
          @validateValue="validateNewPassword"
        />

        <InputBox
          label="Confirm Password"
          type="password"
          placeholder="Enter confirm password"
          v-model="confirmPassword"
          :isValid="form.confirmPassword.isValid"
          :isFirstInput="form.confirmPassword.isFirstInput"
          :errorText="form.confirmPassword.errorText"
          @validateValue="validateConfirmPassword"
        />

        <button
          class="itbms-signin-button fixed bottom-15 right-20 shadow-lg px-6 py-2 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700"
          type="submit"
          :disabled="isLoading || !isFormValid"
        >
          {{ isLoading ? "Loading..." : "Reset Password" }}
        </button>
      </form>
    </template>
  </FormInputBox>
</template>

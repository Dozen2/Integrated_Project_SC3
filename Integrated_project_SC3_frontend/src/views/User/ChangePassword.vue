<script setup>
import InputBox from "@/components/Common/InputBox.vue";
import { ref, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { useAuthStore } from "@/stores/auth";
import FormInputBox from "@/components/Common/FormInputBox.vue";


const auth = useAuthStore();
const router = useRouter();
const alertStore = useAlertStore();

const newPassword = ref("");
const confirmPassword = ref("");
const isLoading = ref(false);

const form = reactive({
  newPassword: {
    errorText: "Password must include upper, lower, number and special character.",
    isValid: false,
    isFirstInput: true,
  },
  confirmPassword: {
    errorText: "Passwords do not match.",
    isValid: false,
    isFirstInput: true,
  },
});

// ✅ ตรวจสอบฟอร์มทั้งหมดถูกต้องหรือไม่
const isFormValid = computed(() => {
  return Object.values(form).every((f) => f.isValid);
});

// ✅ อัปเดต state เมื่อยังไม่เคยพิมพ์
const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

// ✅ validate password ตาม regex
const validateNewPassword = () => {
  form.newPassword.isValid = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$_!%*?&#/\-+^()=\[\]{}><])[A-Za-z\d@$!%*_?&#/\-+^()=\[\]{}><]{8,}$/.test(
    newPassword.value
  );
  updateIsFirstInput("newPassword", newPassword.value);
};

// ✅ confirm password ต้องตรงกับ new password
const validateConfirmPassword = () => {
  form.confirmPassword.isValid = confirmPassword.value === newPassword.value && confirmPassword.value.length > 0;
  updateIsFirstInput("confirmPassword", confirmPassword.value);
};

// ✅ submit ฟอร์มเปลี่ยนรหัสผ่าน
const summitForm = async () => {
  try {
    if (!isFormValid.value) {
      alertStore.addToast("Please fill all required fields correctly.", "Form Error", "error");
      return;
    }
    if (newPassword.value !== confirmPassword.value) {
      alertStore.addToast("Passwords do not match", "Confirm password mismatch", "error");
      return;
    }

    isLoading.value = true;
    await auth.apiChangePassword(newPassword.value);

    alertStore.addToast(
      "Your password has been changed successfully.",
      "Password Changed Successfully",
      "success"
    );

    router.push({ name: "UserProfile" }); // หรือเปลี่ยนเป็นหน้าอื่นได้ เช่น Login
  } catch (err) {
  alertStore.addToast(err.message, "Change Password Error", "error");
  // console.log(err.message);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <FormInputBox
    label="Change Password"
    description="Please enter your new password below. Make sure it’s strong and easy for you to remember."
  >
    <template #form>
      <form class="flex flex-col gap-4" @submit.prevent="summitForm">
        <!-- New Password -->
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

        <!-- Confirm Password -->
        <InputBox
          label="Confirm Password"
          type="password"
          placeholder="Confirm new password"
          v-model="confirmPassword"
          :isValid="form.confirmPassword.isValid"
          :isFirstInput="form.confirmPassword.isFirstInput"
          :errorText="form.confirmPassword.errorText"
          @validateValue="validateConfirmPassword"
        />

        <!-- Submit -->
        <button
          class="itbms-signin-button fixed bottom-15 right-20 shadow-lg px-6 py-2 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 text-white hover:bg-blue-700"
          type="submit"
          :disabled="isLoading || !isFormValid"
        >
          {{ isLoading ? "Loading..." : "Change Password" }}
        </button>
      </form>
    </template>
  </FormInputBox>
</template>

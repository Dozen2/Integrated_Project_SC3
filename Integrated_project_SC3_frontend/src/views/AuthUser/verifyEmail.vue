<script setup>
import { verifyEmail, refreshEmail } from "@/libs/callAPI/apiAuth";
import { CircleAlert, CircleCheck, CircleX, Loader2 } from "lucide-vue-next";
import { ref, onMounted } from "vue";
import { useAlertStore } from "@/stores/alertStore.js";
const alertStore = useAlertStore();

const header = ref("");
const message = ref("");
const pointerStatus = ref("");
const isLoading = ref(false);
const isLoadingButton = ref(false);
const tailwindComponent =
  "flex flex-col items-center justify-center min-h-screen text-3xl font-bold ";

onMounted(async () => {
  isLoading.value = true;
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  const status = await verifyEmail(token);
  pointerStatus.value = status;
  if (status === 200) {
    header.value = "Successfully";
    message.value = "Email verified successfully. You can now log in.";
  } else if (status === 403) {
    header.value = "Invalid token";
    message.value = `The verification link is invalid or somethig wrong status: ${status}`;
  } else if (status === 418) {
    header.value = "Token expired";
    message.value = `The verification link has expired. Please request a new one.`;
  } else {
    header.value = "Error";
    message.value = `An error occurred during verification. Please try again later. status: ${status}`;
  }
  isLoading.value = false;
});

const resendVerificationEmail = async () => {
  try {
    isLoadingButton.value = true;
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get("token");
    await refreshEmail(token);
    alertStore.addToast("Please check your indox email.", "Resend success", "success");
    isLoadingButton.value = false;
  } catch (error) {
    console.error("Error checking pointerStatus:", error);
    alertStore.addToast("Please resend or again.", "Something wrong", "error");
    isLoadingButton.value = true;
  }
};
</script>
<template>
  <div class="flex items-center justify-center min-h-screen">
    <div v-if="isLoading" :class="`${tailwindComponent} text-gray-700`">
      <Loader2 class="w-16 h-16 animate-spin text-blue-500 mb-5" />
      Verify your email, please wait...
    </div>

    <div
      v-else-if="pointerStatus == 200"
      :class="`${tailwindComponent} text-green-600`"
    >
      <CircleCheck size="120" color="#55e62d" class="mb-5" />
      <h3>{{ header }}</h3>
      <p class="text-lg my-3">{{ message }}</p>
    </div>

    <div
      v-else-if="pointerStatus == 403"
      :class="`${tailwindComponent} text-red-600`"
    >
      <CircleX size="120" color="#ff0000" class="mb-5" />
      <h3>{{ header }}</h3>
      <p class="text-lg my-3">{{ message }}</p>
    </div>

    <div
      v-else-if="pointerStatus == 418"
      :class="`${tailwindComponent} text-yellow-600`"
    >
      <CircleAlert size="120" color="#e6c72d" class="mb-5" />
      <h3>{{ header }}</h3>
      <p class="text-lg my-3">{{ message }}</p>
      <button
        v-if="!isLoadingButton"
        class="text-lg mt-3 px-4 py-2 bg-black text-white rounded hover:bg-gray-500 cursor-pointer"
        @click="resendVerificationEmail"
      >
        Resend Verification Email
      </button>
      <button
        v-else
        class="flex text-lg mt-3 px-4 py-2 bg-gray-500 text-white rounded items-center justify-center"
      >
        <Loader2
          class="w-4 h-4 animate-spin text-white mr-[10px]"
        />Resending...
      </button>
    </div>

    <div v-else :class="`${tailwindComponent} text-red-600`">
      <CircleX size="120" color="#ff0000" class="mb-5" />
      <h3>{{ header }}</h3>
      <p class="text-lg my-3">{{ message }}</p>
    </div>
  </div>
</template>

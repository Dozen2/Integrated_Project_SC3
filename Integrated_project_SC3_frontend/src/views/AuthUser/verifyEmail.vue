<script setup>
import { verifyEmail } from "@/libs/callAPI/apiAuth";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const header = ref("");
const message = ref("");
const isLoading = ref(false);
const router = useRouter();

onMounted(async () => {
  isLoading.value = true;
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  if (token) {
    const status = await verifyEmail(token); // รับแค่ status code
    console.log("Verification status:", status);
    if (status === 200) {
      header.value = "Successfully";
      message.value = "Email verified successfully. You can now log in.";
    } else if (status === 400) {
      header.value = "Invalid token";
      message.value = "The verification link is invalid.";
    } else {
      header.value = "Somethig wrong";
      message.value = `Unexpected status: ${status}`;
    }
    isLoading.value = false;
  } else {
    header.value = "Error";
    message.value = "Token not found in URL";
    isLoading.value = false;
  }
});
</script>

<template>
  <div>
    <h2>Email Verification</h2>
    <p v-if="isLoading">Verifying...</p>
    <p v-else>{{ message }}</p>
  </div>
</template>

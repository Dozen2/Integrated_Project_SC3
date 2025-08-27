<script setup>
import { verifyEmail } from "@/libs/callAPI/apiAuth";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const message = ref("");
const isLoading = ref(true);
const router = useRouter();

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");

  if (token) {
    try {
      const status = await verifyEmail(token); // รับแค่ status code

      if (status === 200) {
        message.value = "Successfully";
        setTimeout(() => {
          router.push({ name: "Login" });
        }, 2000);
      } else if (status === 403) {
        message.value = "ยืนยันล้มเหลว กรุณาลองใหม่อีกครั้ง";
      } else {
        message.value = `Unexpected status: ${status}`;
      }
    } catch (error) {
      message.value = error.message || "Error occurred";
    } finally {
      isLoading.value = false;
    }
  } else {
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

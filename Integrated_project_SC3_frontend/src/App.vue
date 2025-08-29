<script setup>
import navbar  from "./components/Common/Navbar.vue";
import Toast from './components/Common/Toast.vue';

// import { useAlertStore } from "@/stores/alertStore.js";
// const alertStore = useAlertStore();
// alertStore.addToast("The sale item has been updated.", "Success", "error");

</script>
<template>
  <navbar />
    <Toast />
  <RouterView />
</template>
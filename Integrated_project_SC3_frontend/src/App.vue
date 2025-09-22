<script setup>
import { useRoute } from 'vue-router'

import navbar  from "./components/Common/Navbar.vue";
import Toast from './components/Common/Toast.vue';

// import { useAlertStore } from "@/stores/alertStore.js";
// const alertStore = useAlertStore();
// alertStore.addToast("The sale item has been updated.", "Success", "error");
const route = useRoute()

const hideNavbarOn = ['/login', '/verify-email']


</script>
<template>
  <div
    class=" bg-gradient-to-br from-blue-100 via-white to-blue-200 min-h-screen"
  >
  <navbar v-if="!hideNavbarOn.includes(route.path)" />
  <Toast />
  <RouterView />
</div>
</template>
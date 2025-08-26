<script setup>
import navbar  from "./components/Common/Navbar.vue";
// import { useToast } from "primevue/usetoast";
// import Toast from 'primevue/toast';
import Toast from './components/Common/Toast.vue';
import { useAlertStore } from "@/stores/alertStore.js";
const alertStore = useAlertStore();

const click = () => {
alertStore.addToast("The sale item has been updated.", "Success", "error");
};
</script>
<template>
  <navbar />
    <!-- <div class="card flex justify-center">
        <Toast />
        <Button label="Show" @click="show()" />
        <p></p>
    </div> -->
    <!-- <button @click="click">Click</button> -->
    <Toast />

  <RouterView />
</template>
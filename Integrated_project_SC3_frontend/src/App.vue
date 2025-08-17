<script setup>
import navbar  from "./components/Common/Navbar.vue";
import { useToast } from "primevue/usetoast";
import Toast from 'primevue/toast';
import Button from 'primevue/button';
const toast = useToast();
const show = () => {
    toast.add({ severity: 'info', summary: 'Info', detail: 'Message Content', life: 3000 });
};
</script>
<template>
  <navbar />
    <!-- <div class="card flex justify-center">
        <Toast />
        <Button label="Show" @click="show()" />
        <p></p>
    </div> -->
  <RouterView />
</template>
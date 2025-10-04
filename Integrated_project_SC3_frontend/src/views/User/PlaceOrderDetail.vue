<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getOrderById } from "@/libs/callAPI/apiUser.js";

const route = useRoute(); // ใช้สำหรับเข้าถึงข้อมูลใน route ปัจจุบัน
const orderParam = ref(null);
const orders = ref([]);

onMounted(async () => {
  orderParam.value = route.params.id;
  orders.value = await getOrderById(orderParam.value);
  console.log("orders.value: ", orders.value);
});
</script>

<template>
  <div class="p-6 text-center text-lg font-semibold text-blue-700">Hello 👋 Order ID: {{ orderParam }}</div>
</template>

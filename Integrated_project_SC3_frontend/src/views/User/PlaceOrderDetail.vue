<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getOrderById } from "@/libs/callAPI/apiUser.js";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";
import { PackageOpen } from "lucide-vue-next";
import Loading from "@/components/Common/Loading.vue";

const route = useRoute(); // ใช้สำหรับเข้าถึงข้อมูลใน route ปัจจุบัน
const orderParam = ref(null);
const orders = ref([]);
const totalPrice = ref(0);
const isLoading = ref(false);

onMounted(async () => {
  isLoading.value = true;
  orderParam.value = route.params.id;
  orders.value = await getOrderById(orderParam.value);
  console.log("orders.value: ", orders.value);
  totalPrice.value = orders.value.orderItems
    .map((item) => item.price * item.quantity)
    .reduce((a, b) => a + b, 0);
  isLoading.value = false;
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat("th-TH", {
    style: "currency",
    currency: "THB",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  })
    .format(value)
    .replace("฿", ""); 
};
</script>

<template>
  <div v-if="isLoading" class="flex items-center justify-center h-screen bg-blue-50">
    <Loading/>
  </div>

<div v-else class="font-sans max-w-7xl mx-auto min-h-screen p-8">
  <Breadcrumb
  :class="'mb-6'"
  :pathForBreadcrumb="[
    { text: 'Home', name: 'Home' },
    { text: 'SaleItem', name: 'Products' },
    { text: 'Place Order', name: 'PlaceOrder' },
    { text: `Place Order Details`, name: 'PlaceOrderId' }
  ]" />
  <div class="flex items-center">
      <h1 class="text-5xl text-blue-500 flex mb-5">
        <span class="mr-2"><PackageOpen size="50" color="#6678ff" /></span>YOUR ORDERS DETAIL
      </h1>
    </div>
  <div class="max-w-7xl mx-auto bg-white shadow-md rounded-lg p-6 mt-8 border border-blue-100">
    <div class="grid md:grid-cols-2 gap-4 text-sm mb-4">
      <div>
        <p><strong class="text-gray-700">Order No:</strong> {{ orders.id }}</p>
        <p><strong class="text-gray-700">Order Date:</strong> {{ orders.orderDate || "-" }}</p>
        <p>
          <strong class="text-gray-700">Total:</strong>
          <span class="font-bold text-blue-700">{{ formatCurrency(totalPrice) }}</span>
        </p>
        <p class="mt-2">
          <strong class="text-gray-700">Shipped To:</strong>
          {{ orders.shippingAddress }}
        </p>
      </div>

      <div>
        <p><strong class="text-gray-700">Seller:</strong> {{ orders.sellerName || "Somsuan" }}</p>
        <p><strong class="text-gray-700">Payment Date:</strong> {{ orders.paymentDate || "-" }}</p>
        <p>
          <strong class="text-gray-700">Status:</strong>
          <span class="ml-1 px-2 py-1 text-xs rounded-md font-semibold" :class="orders.orderStatus === 'Completed' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'">
            {{ orders.orderStatus }}
          </span>
        </p>
        <p><strong class="text-gray-700">Note:</strong> {{ orders.orderNote || "-" }}</p>
      </div>
    </div>

    <hr class="my-4" />

    <!-- Items Section -->
    <div v-for="item in orders.orderItems" :key="item.id" class="flex items-center justify-between py-3 border-b last:border-none text-sm">
      <div class="flex items-center space-x-4">
        <img :src="item.imageUrl || 'https://cdn-icons-png.freepik.com/512/9280/9280762.png'" alt="Product image" class="w-16 h-16 rounded-md object-cover border border-gray-200" />
        <div>
          <p class="font-semibold text-gray-800">
            {{ item.productName || item.description }}
          </p>
          <p class="text-gray-500 text-xs">Unit Price: {{ formatCurrency(item.price) }}</p>
        </div>
      </div>

      <div class="flex items-center space-x-8">
        <p class="text-gray-700">Qty: {{ item.quantity }}</p>
        <p class="font-bold text-blue-700">Price: {{ formatCurrency(item.price * item.quantity) }}</p>
      </div>
    </div>
    </div>
  </div>
</template>

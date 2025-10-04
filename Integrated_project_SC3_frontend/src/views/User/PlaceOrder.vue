<script setup>
import { ref, onMounted } from "vue";
import { getAllOrderByUserId } from "@/libs/callAPI/apiUser.js";
import { useAuthStore } from "@/stores/auth";
import { PackageOpen } from "lucide-vue-next";
import { RouterLink } from "vue-router";

const auth = useAuthStore();
const orders = ref([]);
const userData = auth.getAuthData();
const isLoading = ref(false);
const totalPrice = ref([]);
const currentTotalPrice = ref(0);

onMounted(async () => {
  isLoading.value = true;
  console.log("userData: ", userData.nickname);
  orders.value = await getAllOrderByUserId();
  console.log("ordersById: ", orders.value.content);

  orders.value.content.forEach((order) => {
    const orderTotal = order.orderItems.map((item) => item.price).reduce((a, b) => a + b, 0);
    totalPrice.value.push(orderTotal);
  });
  currentTotalPrice.value = totalPrice.value.reduce((a, b) => a + b, 0);
  console.log("currentTotalPrice.value: ", currentTotalPrice.value);
  console.log("totalPrice.value: ", totalPrice.value);

  isLoading.value = false;
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat("th-TH", {
    style: "currency",
    currency: "THB",
    minimumFractionDigits: 0, // ไม่แสดงทศนิยม
    maximumFractionDigits: 0,
  })
    .format(value)
    .replace("฿", ""); // ตัดสัญลักษณ์ ฿ ออกเพื่อให้เหมือนในรูป
};
</script>
<template>
  <div v-if="isLoading" class="flex items-center justify-center h-screen bg-blue-50">
    <div class="text-blue-600 font-semibold text-4xl animate-pulse">Loading...</div>
  </div>

  <div v-else class="font-sans max-w-4xl mx-auto min-h-screen p-8">
    <div class="flex items-center">
      <h1 class="text-5xl text-blue-500 flex mb-5">
        <span class="mr-2"><PackageOpen size="50" color="#6678ff" /></span>YOUR ORDERS
      </h1>
    </div>
    <RouterLink
      v-for="(order, index) in orders.content"
      :key="order.orderNo"
       :to="{ name: 'PlaceOrderId', params: { id: order.id } }"
      class="block max-w-4xl mx-auto bg-white rounded-2xl shadow-md p-6 mb-6 border border-blue-100 transition transform hover:scale-[1.02] hover:shadow-xl cursor-pointer"
    >
      <!-- Header -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 text-sm mb-4">
        <div>
          <div class="flex items-center mb-2">
            <span class="font-bold text-blue-700 text-base ">{{ userData.nickname }}</span>
          </div>
          <p>
            <strong class="text-gray-500">Order No:</strong>
            <span class="text-gray-800 ml-1">{{ order.id }}</span>
          </p>
          <p>
            <strong class="text-gray-500">Status:</strong>
            <span class="font-semibold ml-1 px-2 py-1 rounded-md text-xs" :class="order.orderStatus === 'Completed' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'">
              {{ order.orderStatus }}
            </span>
          </p>
        </div>
        <div>
          <p><strong class="text-gray-500">Order Date:</strong><br />{{ order.orderDate || "-" }}</p>
        </div>
        <div>
          <p><strong class="text-gray-500">Payment Date:</strong><br />{{ order.paymentDate || "-" }}</p>
        </div>
        <div class="md:text-right">
          <p class="text-gray-500">Total:</p>
          <p class="text-2xl font-bold text-blue-700">{{ formatCurrency(totalPrice[index]) }}</p>
        </div>
      </div>

      <!-- Address + Note -->
      <div class="bg-blue-50 p-4 rounded-lg text-sm mb-4">
        <p><strong class="text-gray-600">Shipped To:</strong> {{ order.shippingAddress }}</p>
        <p v-if="order.orderNote"><strong class="text-gray-600">Note:</strong> {{ order.orderNote }}</p>
      </div>

      <hr class="my-4" />

      <!-- Items -->
      <div class="space-y-4">
        <div v-for="item in order.orderItems" :key="item.id" class="flex items-center space-x-4 text-sm border-b pb-4 last:border-none">
          <img :src="item.imageUrl || 'https://cdn-icons-png.freepik.com/512/9280/9280762.png'" :alt="item.productName" class="w-20 h-20 object-cover rounded-lg border border-gray-200 shadow-sm" />
          <div class="flex-grow">
            <p class="font-semibold text-gray-800">{{ item.productName }}</p>
            <p class="text-gray-500">Qty {{ item.quantity }}</p>
          </div>
          <div class="text-right font-bold text-blue-700 w-28">
            {{ formatCurrency(item.price) }}
          </div>
        </div>
      </div>
    </RouterLink>
  </div>
</template>

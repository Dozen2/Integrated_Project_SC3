<script setup>
import { ref, onMounted } from "vue";
import { getAllOrderByUserId } from "@/libs/callAPI/apiUser.js";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const orders = ref([]);
const userData = auth.getAuthData();

onMounted(async () => {
  console.log("userData: ", userData.nickname);
  orders.value = await getAllOrderByUserId();
  console.log("orders.value: ", orders.value.content);
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
  <div class="font-sans bg-gray-100 p-8">
    <div v-for="order in orders.content" :key="order.orderNo" class="max-w-4xl mx-auto bg-white rounded-lg shadow-sm p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 text-sm mb-4">
        <div>
          <div class="flex items-center mb-2">
            <span class="font-bold text-gray-800 text-base">{{ userData.nickname }}</span>
          </div>
          <p><strong class="font-medium text-gray-500">Order No:</strong> {{ order.orderNo }}</p>
          <p>
            <strong class="font-medium text-gray-500">Status:</strong> <span class="font-semibold text-green-600">{{ order.orderStatus }}</span>
          </p>
        </div>
        <div>
          <p><strong class="font-medium text-gray-500">Order Date:</strong> <br />{{ order.orderDate }}</p>
        </div>
        <div>
          <p><strong class="font-medium text-gray-500">Payment Date:</strong> <br />{{ order.paymentDate }}</p>
        </div>
        <div class="md:text-right">
          <p class="text-gray-500">Total:</p>
          <p class="text-2xl font-bold text-gray-900">{{ formatCurrency(order.totalPrice) }}</p>
        </div>
      </div>
      <div>
        <p class="mt-2"><strong class="font-medium text-gray-500">Shipped To:</strong> {{ order.shippingAddress }}</p>
        <p v-if="order.orderNote"><strong class="font-medium text-gray-500">Note:</strong> {{ order.orderNote }}</p>
      </div>
      <hr class="my-4" />

      <div class="space-y-4">
        <div v-for="item in order.orderItems" :key="item.id" class="flex items-center space-x-4 text-sm">
          <img :src="item.imageUrl" :alt="item.productName" class="w-20 h-20 object-cover rounded-md border border-gray-200" />
          <div class="flex-grow">
            <p class="font-semibold text-gray-800">{{ item.productName }}</p>
          </div>
          <div class="w-20 text-center text-gray-600">Qty {{ item.quantity }}</div>
          <div class="w-28 text-right font-semibold text-gray-800">
            {{ formatCurrency(item.price) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getOrderById } from "@/libs/callAPI/apiUser.js";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";
import { PackageOpen } from "lucide-vue-next";
import Loading from "@/components/Common/Loading.vue";
import { getImageByImageName, setOrderStatus } from "@/libs/callAPI/apiSaleItem";

const route = useRoute();
const orderParam = ref(null);
const orders = ref([]);
const totalPrice = ref(0);
const isLoading = ref(false);

onMounted(async () => {
  isLoading.value = true;
  orderParam.value = route.params.id;

  const updatedOrder = await setOrderStatus(orderParam.value);
  console.log("Updated Order:", updatedOrder);

  orders.value = await getOrderById(orderParam.value);
  console.log("orders.value: ", orders.value);

  totalPrice.value = orders.value.orderItems
    .map((item) => item.price * item.quantity)
    .reduce((a, b) => a + b, 0);
  loadImageUrl()
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

const imageMap = ref([]);
const loadImageUrl = async () => {
  imageMap.value = {};
  for (const item of orders.value.orderItems) {
    if (item.mainImageFileName) {
      const image = await getImageByImageName(item.mainImageFileName);
      imageMap.value[item.no] = image;
    } else {
      imageMap.value[item.no] = "https://cdn-icons-png.freepik.com/512/9280/9280762.png";
    }
  }
};

const formatDate = (isoString) => {
  if (!isoString) return "-"; // กันค่า null หรือ undefined
  const date = new Date(isoString);
  return new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(date);
};

const formatOrderStatus = (status) => {
  switch (status) {
    case "new_complete":
      return "Complete";
    case "new_cancelled":
      return "Cancelled";
    default:
      return status
  }
}
</script>
<template>
  <div v-if="isLoading"
    class="flex items-center justify-center min-h-[calc(100vh-80px)] bg-gradient-to-b from-blue-50 to-white">
    <Loading />
  </div>

  <div v-else class="font-sans max-w-7xl mx-auto min-h-[calc(100vh-80px)] p-8 text-gray-800">
    <Breadcrumb :class="'itbms-your-orders-button mb-6'" :pathForBreadcrumb="[
      { text: 'Home', name: 'Home' },
      { text: 'SaleItem', name: 'Products' },
      { text: 'Place Order', name: 'PlaceOrder' },
      { text: 'Order Details', name: 'PlaceOrderId' }
    ]" />

    <!-- หัวข้อ -->
    <div class="flex items-center">
      <h1 class="text-5xl text-blue-500 flex mb-5">
        <span class="mr-2">
          <PackageOpen size="50" color="#6678ff" />
        </span>YOUR ORDERS DETAILS
      </h1>
    </div>

    <!-- กล่องหลัก -->
    <div class="relative bg-white rounded-3xl shadow-xl border border-blue-100 p-8 overflow-hidden">
      <!-- แสง gradient เบาๆ -->
      <div
        class="absolute inset-0 bg-gradient-to-tr from-blue-50 via-transparent to-transparent opacity-70 pointer-events-none">
      </div>

      <!-- ข้อมูลคำสั่งซื้อ -->
      <div class="grid md:grid-cols-2 gap-8 relative z-10">
        <div class="space-y-2">
          <p><strong class="itbms-order-id text-blue-700">Order No:</strong> {{ orders.id }}</p>
          <p><strong class="itbms-order-date text-blue-700">Order Date:</strong> {{ formatDate(orders.orderDate) || "-"
          }}</p>
          <p>
            <strong class="text-blue-700">Total:</strong>
            <span class="itbms-total-order-price font-bold text-blue-600 text-lg ml-1">{{ formatCurrency(totalPrice) }}
              Bath</span>
          </p>
          <p class="mt-2">
            <strong class="text-blue-700">Shipped To:</strong>
            <span class="itbms-shipping-address text-gray-600 ml-1">{{ orders.shippingAddress }}</span>
          </p>
        </div>

        <div class="space-y-2">
          <p><strong class="itbms-nickname text-blue-700">Seller:</strong> {{ orders.sellerName || "unknow" }}</p>
          <p><strong class="itbms-payment-date text-blue-700">Payment Date:</strong> {{ formatDate(orders.paymentDate)
            || "-" }}</p>
          <p>
            <strong class="text-gray-500">Status:</strong>
            <span class="itbms-order-status font-semibold ml-1 px-2 py-1 rounded-md text-xs" :class="[
              order.orderStatus === 'new_complete'
                ? 'bg-green-100 text-green-700'
                : order.orderStatus === 'new_cancelled'
                  ? 'bg-red-100 text-red-700'
                  : 'bg-yellow-100 text-yellow-700'
            ]">
              {{ formatOrderStatus(orders.orderStatus) }}
            </span>
          </p>
          <p><strong class="itbms-order-note text-blue-700">Note:</strong> {{ orders.orderNote || "-" }}</p>
        </div>
      </div>

      <hr class="my-6 border-blue-100" />

      <!-- รายการสินค้า -->
      <div class="space-y-4 relative z-10">
        <h2 class="text-xl font-semibold text-blue-600 mb-4">Ordered Items</h2>

        <div v-for="item in orders.orderItems" :key="item.id"
          class="itbms-item-row flex items-center justify-between p-4 rounded-2xl border border-blue-100 bg-blue-50/40 hover:bg-blue-100/50 transition-all duration-200">
          <div class="flex items-center space-x-4">
            <img :src="imageMap[item.no]" :alt="item.productName"
              class="w-16 h-16 rounded-xl object-cover border border-gray-200 shadow-sm" />
            <div>
              <p class="itbms-item-description font-semibold text-gray-800">{{ item.productName || item.description }}
              </p>
              <p class="itbms-item-price text-gray-500 text-xs">Unit Price: {{ formatCurrency(item.price) }} Bath</p>
            </div>
          </div>

          <div class="flex items-center space-x-8 text-sm">
            <p class="text-gray-600">Qty: <span class="itbms-item-quantity font-medium text-gray-800">{{ item.quantity
            }}</span></p>
            <p class="itbms-item-total-price font-bold text-blue-700 text-base">{{ formatCurrency(item.price *
              item.quantity) }} Bath</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

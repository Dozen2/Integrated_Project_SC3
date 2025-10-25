<script setup>
import { ref, onMounted, computed } from "vue";
import { getAllOrderByUserId } from "@/libs/callAPI/apiUser.js";
import { useAuthStore } from "@/stores/auth";
import { PackageOpen, ShoppingCart } from "lucide-vue-next";
import { RouterLink } from "vue-router";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";
import Loading from "@/components/Common/Loading.vue";
import { getImageByImageName } from "@/libs/callAPI/apiSaleItem";
import PaginationSeller from "@/components/Common/QueryBySeller/PaginationSeller.vue";
import SizeAndSortSeller from "@/components/Common/QueryBySeller/SizeAndSortSeller.vue";

const auth = useAuthStore();
const orders = ref([]);
const userData = auth.getAuthData();
const isLoading = ref(false);
const totalPrice = ref([]);
const currentTotalPrice = ref(0);
const imageMap = ref([]);
const pagination = ref({
  page: 0,
  size: 50,
  sort: "asc",
  totalPages: 0,
  totalElements: 0,
});

const fetchselect = async () => {
  try {
    isLoading.value = true;
    // reset Image Map and totalPrice
    imageMap.value = {};
    totalPrice.value = [];

    const page = parseInt(sessionStorage.getItem("order_pagination") ?? "0", 10);
    let size = sessionStorage.getItem("order_size") ? parseInt(sessionStorage.getItem("order_size"), 10) : 10;
    console.log("Fetching page:", page);
    console.log("Fetching size:", size);
    pagination.value.page = page;
    pagination.value.size = size;
    const ordersData = await getAllOrderByUserId(size, page);
    orders.value = ordersData;
    console.log("Fetched order:", ordersData);
    pagination.value = {
      page: ordersData.page,
      size: ordersData.size,
      totalPages: ordersData.totalPages,
      totalElements: ordersData.totalElements,
    };
    sessionStorage.setItem("order_pagination", ordersData.page);
    sessionStorage.setItem("order_size", ordersData.size);

    //------------------------- Price Calculation -------------------------
    orders.value.content.forEach((order) => {
      const orderTotal = order.orderItems.map((item) => item.price * item.quantity).reduce((a, b) => a + b, 0);
      totalPrice.value.push(orderTotal);
    });
    currentTotalPrice.value = totalPrice.value.reduce((a, b) => a + b, 0);
    console.log("currentTotalPrice.value: ", currentTotalPrice.value);
    console.log("totalPrice.value: ", totalPrice.value);
    //------------------------- Image Showing -------------------------
    await loadImageUrl();
    console.log("imageMap.value mounted: ", imageMap.value);
    console.log("imageMap.value page changed: ", imageMap.value);
    isLoading.value = false;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const loadImageUrl = async () => {
  imageMap.value = {};
  for (const order of orders.value.content) {
    for (const item of order.orderItems) {
      if (item.mainImageFileName) {
        const image = await getImageByImageName(item.mainImageFileName);
        imageMap.value[item.no] = image;
      } else {
        imageMap.value[item.no] = "https://cdn-icons-png.freepik.com/512/9280/9280762.png";
      }
    }
  }
};

const handlePageChange = (newPage) => {
  pagination.value.page = newPage;
  fetchselect();
};

onMounted(async () => {
  isLoading.value = true;
  await fetchselect();
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

const formatDate = (isoString) => {
  if (!isoString) return "-"; // กันค่า null หรือ undefined
  const date = new Date(isoString);
  return new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(date);
};


const activeTab = ref("new_complete"); // ค่าเริ่มต้นเป็น complete

// ✅ computed: แสดงเฉพาะ order ตามแท็บที่เลือก
const filteredOrders = computed(() => {
  if (!orders.value.content) return [];
  return orders.value.content.filter((order) => order.orderStatus === activeTab.value);
});

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
  <div v-if="isLoading" class="flex items-center justify-center h-screen bg-blue-50">
    <Loading />
  </div>

  <div v-else class="font-sans max-w-7xl mx-auto min-h-screen p-8">
    <Breadcrumb :class="'mb-6'" :pathForBreadcrumb="[
      { text: 'Home', name: 'Home' },
      { text: 'SaleItem', name: 'Products' },
      { text: 'PlaceOrder', name: 'PlaceOrder' },
    ]" />

    <div class="flex items-center">
      <h1 class="text-5xl text-blue-500 flex mb-5">
        <span class="mr-2">
          <PackageOpen size="50" color="#3B82F6" />
        </span>
        YOUR ORDERS
      </h1>
    </div>

    <div class="flex justify-center gap-6 mb-8">
      <button @click="activeTab = 'new_complete'" :class="[
        'px-6 py-2 rounded-full font-semibold transition-all duration-200',
        activeTab === 'new_complete'
          ? 'bg-blue-500 text-white shadow-md'
          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
      ]">
        COMPLETE
      </button>
      <button @click="activeTab = 'new_cancelled'" :class="[
        'px-6 py-2 rounded-full font-semibold transition-all duration-200',
        activeTab === 'new_cancelled'
          ? 'bg-blue-500 text-white shadow-md'
          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
      ]">
        CANCELLED
      </button>
    </div>
  <div v-if="filteredOrders.length === 0 " class="flex flex-col items-center justify-center min-h-screen text-gray-500 gap-7 mt-[-20%]">
        <ShoppingCart size="140" color="#3B82F6" strokeWidth="{1.30}"/>
        <p class="text-4xl text-blue-500">You haven’t placed any orders yet.</p>
      </div>
  
      <div v-else>
    <!-- ✅ ใช้ filteredOrders แทน orders.content -->
    <RouterLink v-for="(order, index) in filteredOrders" :key="order.orderNo"
      :to="{ name: 'PlaceOrderId', params: { id: order.id } }"
      class="itbms-row block max-w-7xl mx-auto bg-white rounded-2xl shadow-md p-6 mb-6 border border-blue-100 transition transform hover:scale-[1.02] hover:shadow-xl cursor-pointer">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 text-sm mb-4">
        <div>
          <div class="flex items-center mb-2">
            <span class="itbms-nickname font-bold text-blue-700 text-base">{{ order.seller.userName }}</span>
          </div>
          <p>
            <strong class="text-gray-500">Order No:</strong>
            <span class="itbms-order-id text-gray-800 ml-1">{{ order.id }}</span>
          </p>
          <p>
            <strong class="text-gray-500">Status:</strong>
            <span class="itbms-order-status font-semibold ml-1 px-2 py-1 rounded-md text-xs" :class="[
              order.orderStatus === 'new_complete'
                ? 'bg-green-100 text-green-700'
                : order.orderStatus === 'new_cancelled'
                  ? 'bg-red-100 text-red-700'
                  : 'bg-yellow-100 text-yellow-700'
            ]">
              {{ formatOrderStatus(order.orderStatus) }}
            </span>
          </p>
        </div>

        <div>
          <p><strong class="itbms-order-date text-gray-500">Order Date:</strong><br />{{ formatDate(order.orderDate) ||
            "-" }}</p>
        </div>
        <div>
          <p><strong class="itbms-payment-date text-gray-500">Payment Date:</strong><br />{{
            formatDate(order.paymentDate) || "-" }}</p>
        </div>
        <div class="md:text-right">
          <p class="text-gray-500">Total:</p>
          <p class="itbms-total-order-price text-2xl font-bold text-blue-700">{{ formatCurrency(totalPrice[index]) }}
            Bath</p>
        </div>
      </div>

      <div class="bg-blue-50 p-4 rounded-lg text-sm mb-4">
        <p><strong class="itbms-shipping-address text-gray-600">Shipped To:</strong> {{ order.shippingAddress }}</p>
        <p v-if="order.orderNote"><strong class="itbms-order-note text-gray-600">Note:</strong> {{ order.orderNote }}
        </p>
      </div>

      <hr class="my-4" />

      <div class="space-y-4">
        <div v-for="(item, index) in order.orderItems" :key="item.id"
          class="itbms-item-row flex items-center space-x-4 text-sm border-b pb-4 last:border-none">
          <img :src="imageMap[item.no]" :alt="item.productName"
            class="w-20 h-20 object-cover rounded-lg border border-gray-200 shadow-sm" />
          <div class="flex-grow">
            <p class="itbms-item-description font-semibold text-gray-800">{{ item.productName }}</p>
            <p class="itbms-item-quantity text-gray-500">Qty {{ item.quantity }}</p>
          </div>

          <div>
            <p><strong class="itbms-order-date text-gray-500">Order Date:</strong><br />{{ formatDate(order.orderDate) || "-" }}</p>
          </div>
          <div>
            <p><strong class="itbms-payment-date text-gray-500">Payment Date:</strong><br />{{ formatDate(order.paymentDate) || "-" }}</p>
          </div>
          <div class="md:text-right">
            <p class="text-gray-500">Total:</p>
            <p class="itbms-total-order-price text-2xl font-bold text-blue-700">{{ formatCurrency(totalPrice[index]) }} Bath</p>
          </div>
        </div>

        <div class="bg-blue-50 p-4 rounded-lg text-sm mb-4">
          <p><strong class="itbms-shipping-address text-gray-600">Shipped To:</strong> {{ order.shippingAddress }}</p>
          <p v-if="order.orderNote"><strong class="itbms-order-note text-gray-600">Note:</strong> {{ order.orderNote }}</p>
        </div>

        <hr class="my-4" />

        <div class="space-y-4">
          <div v-for="(item, index) in order.orderItems" :key="item.id" class="itbms-item-row flex items-center space-x-4 text-sm border-b pb-4 last:border-none">
            <img :src="imageMap[item.no]" :alt="item.productName" class="w-20 h-20 object-cover rounded-lg border border-gray-200 shadow-sm" />
            <div class="flex-grow">
              <p class="itbms-item-description font-semibold text-gray-800">{{ item.productName }}</p>
              <p class="itbms-item-quantity text-gray-500">Qty {{ item.quantity }}</p>
            </div>
            <div class="itbms-item-total-price text-right font-bold text-blue-700 w-28">{{ formatCurrency(item.price * item.quantity) }} Bath</div>
          </div>
        </div>
        </div>
      </RouterLink>

      <div class="flex gap-4 justify-center pb-10">
        <PaginationSeller v-model="pagination.page" :total-page="pagination.totalPages" storage-key="order_pagination" @update:modelValue="fetchselect" />
        <div v-show="pagination.totalPages > 1">
          <SizeAndSortSeller
          v-model:modelSize="pagination.size"
          v-model:modelSort="pagination.sort"
          v-model:modelPage="pagination.page"
          storage-key-size="order_size"
          storage-key-sort="order_sort"
          reset-storage="order_pagination"
          @update:modelPage="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>
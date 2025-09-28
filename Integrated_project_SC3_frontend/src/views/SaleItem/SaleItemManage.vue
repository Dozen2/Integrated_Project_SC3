<script setup>
import { onBeforeMount, onMounted, reactive, ref, watch } from "vue"; // เพิ่ม watch
import { unitPrice, nullCatching } from "@/libs/utils.js"
import { useAlertStore } from "@/stores/alertStore.js"
import { deleteUserById } from "@/libs/api.js";
import { getAllSaleItemSeller} from "@/libs/callAPI/apiSaleItem.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand.js";
import PaginationSeller from "@/components/Common/QueryBySeller/PaginationSeller.vue";
import SizeAndSortSeller from "@/components/Common/QueryBySeller/SizeAndSortSeller.vue";
import { Pencil, Trash2 } from "lucide-vue-next";
import { useAuthStore } from '@/stores/auth'


const alertStore = useAlertStore();
const auth = useAuthStore();
const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;

const saleItem = ref([]);
const brand = ref([]);

const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);

const pagination = ref({
  page: 0,
  size: 50,
  sort: "asc",
  totalPages: 0,
  totalElements: 0,
});


onBeforeMount(async () => {
  await fetchselect();
  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
  console.log(auth.getAuthData().nickname)
});

watch(() => pagination.value.page, (newPage) => {
  sessionStorage.setItem("seller_pagination", newPage.toString());
});

const fetchselect = async () => {
  try {
    // ดึงค่าจาก sessionStorage แทน localStorage
    const page = parseInt(sessionStorage.getItem("seller_pagination") ?? "0", 10);
    const size = sessionStorage.getItem("seller_size") 
      ? parseInt(sessionStorage.getItem("seller_size"), 10) 
      : 10;

    console.log("Fetching page:", page);
    pagination.value.page = page;
    pagination.value.size = size;

    // เรียก API
    const saleItemData = await getAllSaleItemSeller(size, page);
    saleItem.value = saleItemData;
    console.log("Fetched sale items:", saleItemData);

    // อัปเดต pagination state
    pagination.value = {
      page: saleItemData.page,
      size: saleItemData.size,
      totalPages: saleItemData.totalPages,
      totalElements: saleItemData.totalElements,
    };

    // เก็บค่าลง sessionStorage
    sessionStorage.setItem("seller_pagination", saleItemData.page);
    sessionStorage.setItem("seller_size", saleItemData.size);

    const brandData = await getAllBrand();
    brand.value = brandData;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const handlePageChange = (newPage) => {
  pagination.value.page = newPage;
  fetchselect();
};

//============================================================================

const confirmDeleteProduct = async () => {
  try {
    await deleteUserById(`${VITE_ROOT_API_URL}/itb-mshop/v1/sale-items`, pendingDeleteId.value);
    alertStore.addToast('The sale item has been deleted.', 'Delete success', 'success');
    await fetchselect(); // Refresh data after delete
  } catch (error) {
    if (error.status === 404) {
      alertStore.addToast('The requested sale item does not exist.', 'Delete failed', 'error');
    } else {
      alertStore.addToast('The requested sale item does not exist.', 'Delete failed', 'error');
    }
    await fetchselect(); // Refresh data even on error
  } finally {
    showDeleteModal.value = false;
  }
};

const deleteProduct = (id) => {
  pendingDeleteId.value = id;
  showDeleteModal.value = true;
};

</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
  <h3 class="itbms-nickname text-3xl font-bold text-blue-700 mb-[-15px]">Wellcome {{ auth.getAuthData().nickname }}</h3>
<div class="flex justify-between items-center gap-4  mt-[20px]">
  <div>
    <h1 class="text-4xl font-bold text-blue-700 flex items-center">SaleItem Management</h1>
  </div>
    <div class=" items-center gap-4"> 
    </div>
        <RouterLink
        :to="{ name: 'ProuctCreate' }"
        class="inline-flex items-center gap-2 border-blue-400 border bg-gray-100 text-blue-700 hover:bg-gray-200 text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
      >
        <span class="itbms-sale-item-add tracking-wide flex"
          >Add New SaleItem</span
          >
      </RouterLink>
  </div>


    <!-- Alert Message -->
    <div v-if="alertStore.message" :class="`itbms-message px-4 py-2 rounded mb-4 ${alertStore.type === 'error'
      ? 'bg-red-100 text-red-700'
      : 'bg-blue-100 text-blue-700'}`">
      {{ alertStore.message }}
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">ยืนยันการลบ</h2>
        <p class="itbms-message text-gray-600 mb-6">Do you want to delete this sale item?</p>
        <div class="flex justify-end space-x-2">
          <button @click="showDeleteModal = false"
            class="itbms-cancel-button px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition">ยกเลิก</button>
          <button @click="confirmDeleteProduct"
            class="itbms-confirm-button px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition">ยืนยัน</button>
        </div>
      </div>
    </div>

    <div class="flex justify-between items-center mb-4">
    </div>

    <!-- Product Table -->
    <div class="overflow-x-auto shadow mb-7">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-blue-700 text-white">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Id</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Brand</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Model</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Ram</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Storage</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Color</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Price</th>
            <th scope="col" class="px-6 py-3 text-left text-base font-medium uppercase tracking-wider">Action</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="(saleItem, index) in saleItem.content" :key="saleItem.id" class="itbms-row hover:bg-blue-50"
            :class="{ 'bg-blue-50': index % 2 === 0 }">
            <td class="px-6 py-4 whitespace-nowrap text-base font-medium text-gray-900 itbms-id">{{ saleItem.id }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-brand">{{ saleItem.brandName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-model">{{ saleItem.model }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-ramGb">{{ nullCatching(saleItem.ramGb)
              }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-storageGb">{{
              nullCatching(saleItem.storageGb) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-color">{{ nullCatching(saleItem.color)
              }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-price">{{ unitPrice(saleItem.price) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500">
              <div class="flex space-x-2">
                <RouterLink :to="{ name: 'Edit', params: { id: saleItem.id } }">
                  <button
                    class="itbms-edit-button bg-blue-700 hover:bg-blue-800 text-white w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                    <Pencil size=20 strokeWidth=1.5 />
                  </button>
                </RouterLink>
                <button @click="deleteProduct(saleItem.id)"
                  class="itbms-delete-button bg-white hover:bg-red-500 border border-gray-300 text-gray-700 w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  <Trash2 size=20 strokeWidth=1.5 />
                </button>
              </div>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-if="!saleItem.content || saleItem.content.length === 0">
            <td colspan="8" class="px-6 py-4 text-center text-gray-500 itbms-no">
              No sale items available
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex gap-4 justify-center">

      <PaginationSeller
      v-model="pagination.page"
      :total-page="pagination.totalPages"
      storage-key="seller_pagination"
      @update:modelValue="fetchselect"
    />
    <SizeAndSortSeller
      v-model:modelSize="pagination.size"
      v-model:modelSort="pagination.sort"
      v-model:modelPage="pagination.page"
      storage-key-size="seller_size"
      storage-key-sort="seller_sort"
      reset-storage="seller_pagination"
      @update:modelPage="handlePageChange"
      />
    </div>

  </div>
</template>
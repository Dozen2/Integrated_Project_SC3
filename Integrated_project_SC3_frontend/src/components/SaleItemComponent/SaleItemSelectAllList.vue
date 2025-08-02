<script setup>
import { onMounted, ref } from "vue";
import { unitPrice, nullCatching } from "@/libs/utils.js"
import { useAlertStore } from "@/stores/alertStore.js"
import { useRouter } from "vue-router";
import { deleteUserById } from "@/libs/api.js";

const alertStore = useAlertStore();
const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;

const emit = defineEmits(["selestDeleted"]);

const props = defineProps({
  product: {
    type: Array,
    default: () => []
  }
});

const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);

const confirmDeleteProduct = async () => {
  try {
    await deleteUserById(`${VITE_ROOT_API_URL}/itb-mshop/v1/sale-items`, pendingDeleteId.value);
    alertStore.setMessage('The sale item has been deleted.');
    // router.push('/sale-items/list');
    emit("selestDeleted");
  } catch (error) {
    if (error.status === 404) {
      alertStore.setMessage('The requested sale item does not exist.', 'error')
      // router.push('/sale-items/list');
      emit("selestDeleted");
    } else {
      alertStore.setMessage('The requested sale item does not exist.', 'error')
      // router.push('/sale-items/list');
      emit("selestDeleted");
    }
  } finally {
    showDeleteModal.value = false;
    localStorage.setItem('product-updated', Date.now().toString());
  }
};

const deleteProduct = (id) => {
  pendingDeleteId.value = id;
  showDeleteModal.value = true;
};

onMounted(() => {
  console.log('ProductList mounted');
  console.log(props.product);

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <!-- Alert Message -->
    <div v-if="alertStore.message" :class="`itbms-message px-4 py-2 rounded mb-4 ${alertStore.type === 'error'
      ? 'bg-red-100 text-red-700'
      : 'bg-blue-100 text-blue-700'}`">
      {{ alertStore.message }}
    </div>

    <!-- Empty State -->
    <div v-if="!product || product.length === 0" class="itbms-no text-center text-gray-500 text-xl">
      no sale item
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
    <div class="overflow-x-auto shadow">
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
          <tr v-for="(product, index) in props.product" :key="product.id" class="itbms-row hover:bg-blue-50"
            :class="{ 'bg-blue-50': index % 2 === 0 }">
            <td class="px-6 py-4 whitespace-nowrap text-base font-medium text-gray-900 itbms-id">{{ product.id }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-brand">{{ product.brandName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-model">{{ product.model }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-ramGb">{{ nullCatching(product.ramGb)
              }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-storageGb">{{
              nullCatching(product.storageGb) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-color">{{ nullCatching(product.color)
              }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500 itbms-price">{{ unitPrice(product.price) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-base text-gray-500">
              <div class="flex space-x-2">
                <RouterLink :to="{ name: 'Edit', params: { id: product.id } }">
                  <button
                    class="itbms-edit-button bg-blue-700 hover:bg-blue-800 text-white w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                    🖋️
                  </button>
                </RouterLink>
                <button @click="deleteProduct(product.id)"
                  class="itbms-delete-button bg-white hover:bg-red-500 border border-gray-300 text-gray-700 w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  🗑️
                </button>
              </div>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-if="!props.product || props.product.length === 0">
            <td colspan="8" class="px-6 py-4 text-center text-gray-500 itbms-no">
              No sale items available
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
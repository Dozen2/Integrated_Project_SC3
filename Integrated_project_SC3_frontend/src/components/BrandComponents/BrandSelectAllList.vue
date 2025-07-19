<script setup>
import { onMounted, ref } from "vue";
import { unitPrice, nullCatching } from "@/libs/utils.js"
import { useAlertStore } from "@/stores/alertStore.js"
import { useRouter } from "vue-router";
import BrandCreate from "@/views/Brand/BrandCreate.vue";
import { deleteUserById, getAllData } from "@/libs/api.js";

const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const alertStore = useAlertStore();
const router = useRouter();
const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);
const brandToDeleteName = ref("");
const cannotdelete = ref(false)

const emit = defineEmits(["brandDeleted"]);


const props = defineProps({
  brand: {
    type: Array,
    default: () => []
  }
});

const goToEdit = (id) => {
  router.push({ name: "BrandEdit", params: { id } })
}

const confirmDeleteProduct = async () => {
  try {
    await deleteUserById(`${VITE_ROOT_API_URL}/itb-mshop/v1/brands`, pendingDeleteId.value);
    alertStore.setMessage('The brand has been deleted.');
    // router.push('/brands');
    emit("brandDeleted");
  } catch (error) {
    if (error.status === 400) {
      const data = await error.json?.();
      alertStore.setMessage(data?.message || 'Cannot delete brand.', 'error');
    }
    else if (error.status === 404) {
      alertStore.setMessage('An error has occurred, the brand does not exist.', 'error')
      router.push('/brands');
    } else {
      alertStore.setMessage('The requested sale item does not exists.', 'error')
      router.push('/brands');
    }
  } finally {
    showDeleteModal.value = false;
    localStorage.setItem('brand-updated', Date.now().toString());
  }
};



const deleteBrand = (id, name, noOfSaleItems) => {
  console.log(noOfSaleItems);
  if (noOfSaleItems > 0) {
    cannotdelete.value = true
    brandToDeleteName.value = name;
    // alertStore.setMessage(`Delete ${name} is not allowed. There are sale items with ${name} brand.`, 'error');

    // setTimeout(() => {
    //   alertStore.clearMessage();
    //   router.push('/brands'); // หรือ router.push('/brands')
    // }, 2500);

    return;
  }
  pendingDeleteId.value = id;
  showDeleteModal.value = true;
  brandToDeleteName.value = name

};

onMounted(() => {
  console.log('brandList mounted');
  console.log(props.brand);

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});

 localStorage.setItem('brand-updated', Date.now().toString());

</script>

<template>
  <div class="max-w-7xl mx-auto mt-[40px]">

    <!-- Navigation Links -->
    <div class="flex mb-4 space-x-4">
      <RouterLink :to="{ name: 'ProductManage' }"
        class="px-4 py-2 bg-gray-100 text-gray-700 hover:bg-gray-200 rounded-md shadow-sm transition">
        🛒 Sale Item List
      </RouterLink>

      <RouterLink :to="{ name: 'BrandCreate' }"
        class="itbms-add-button px-4 py-2 bg-blue-600 text-white hover:bg-blue-700 rounded-md shadow-sm transition">
        ➕ Add New Brand
      </RouterLink>
    </div>


    <!-- Alert Message -->
    <div v-if="alertStore.message" :class="`itbms-message px-4 py-2 rounded mb-4 ${alertStore.type === 'error'
      ? 'bg-red-100 text-red-700'
      : 'bg-blue-100 text-blue-700'}`">
      {{ alertStore.message }}
    </div>

    <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">ยืนยันการลบ</h2>
        <p class="itbms-message text-gray-600 mb-6">Do you want to delete <strong>{{ brandToDeleteName }}</strong>
          brand?</p>
        <div class="flex justify-end space-x-2">
          <button @click="showDeleteModal = false"
            class="itbms-cancel-button px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition">ยกเลิก</button>
          <button @click="confirmDeleteProduct"
            class="itbms-confirm-button px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition">ยืนยัน</button>
        </div>
      </div>
    </div>

    <!-- Modal: Cannot Delete Brand -->
    <div v-if="cannotdelete" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <h2 class="text-lg font-semibold text-red-700 mb-4">Delete Not Allowed</h2>
        <p class="itbms-message text-gray-700 mb-6">
          Delete <strong>{{ brandToDeleteName }}</strong> is not allowed. There are sale items with <strong>{{
            brandToDeleteName }}</strong> brand.
        </p>
        <div class="flex justify-end">
          <button @click="cannotdelete = false"
            class="itbms-cancel-button px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition">
            Cancel
          </button>
        </div>
      </div>
    </div>


    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="min-w-full border-collapse mb-[80px]">
        <thead>
          <tr>
            <th class="bg-blue-700 text-white py-3 px-4 text-center w-1/12">Id</th>
            <th class="bg-blue-700 text-white py-3 px-4 text-center">Name</th>
            <th class="bg-blue-700 text-white py-3 px-4 text-center w-2/12">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in props.brand" :key="item.id" :class="index % 2 === 0 ? 'bg-blue-50' : 'bg-white'"
            class="itbms-row border border-gray-200">
            <td class="itbms-id text-black py-2 px-4 text-center border-r border-gray-200">{{ item.id }}</td>
            <td class="itbms-name text-black py-2 px-4 text-center border-r border-gray-200">{{ item.name }}</td>
            <td class="py-2 px-4 text-center">
              <div class="flex justify-center space-x-2">
                <button @click="goToEdit(item.id)"
                  class="itbms-edit-button bg-blue-700 hover:bg-blue-800 text-white w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  🖋️
                </button>
                <button @click="deleteBrand(item.id, item.name, item.noOfSaleItems)"
                  class="itbms-delete-button bg-white hover:bg-red-500 border border-gray-300 text-gray-700 w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  🗑️
                </button>
              </div>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-if="!props.brand || props.brand.length === 0">
            <td colspan="3" class="px-6 py-4 text-center text-gray-500 itbms-no">
              No sale items available
            </td>
          </tr>
        </tbody>
      </table>
    </div>



  </div>
</template>
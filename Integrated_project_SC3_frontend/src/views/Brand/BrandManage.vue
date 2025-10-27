<script setup>
import { onMounted, ref, onBeforeUnmount } from "vue";
import { unitPrice, nullCatching } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import { useRouter } from "vue-router";
import BrandCreate from "@/views/Brand/BrandCreate.vue";
import { deleteUserById, getAllData } from "@/libs/api.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand.js";
import { MoveLeft, Pencil, Trash2 } from "lucide-vue-next";
import { useAuthStore } from "@/stores/auth";
import ConfirmDelete from "@/components/Common/ConfirmDelete.vue";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";

const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const alertStore = useAlertStore();
const router = useRouter();
const showDeleteModal = ref(false);
const pendingDeleteId = ref(null);
const brandToDeleteName = ref("");
const cannotdelete = ref(false);
const brand = ref([]);

const auth = useAuthStore();

const fetchBrands = async () => {
  try {
    const brandData = await getAllBrand();
    brand.value = brandData;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

function onStorageChange(event) {
  if (event.key === "brand-updated") {
    // console.log("Brand data changed in another tab");
    fetchBrands(); // โหลดข้อมูลใหม่
  }
}

const goToEdit = (id) => {
  router.push({ name: "BrandEdit", params: { id } });
};

const confirmDeleteProduct = async () => {
  try {
    await deleteUserById(`${VITE_ROOT_API_URL}/itb-mshop/v1/brands`, pendingDeleteId.value);
    alertStore.addToast("The brand has been deleted.", "Delete success", "success");
    await fetchBrands(); // รีเฟรชข้อมูลหลังลบ
  } catch (error) {
    if (error.status === 400) {
      const data = await error.json?.();
      alertStore.addToast(data?.message || "Cannot delete brand.", "Delete failed", "error");
    } else if (error.status === 404) {
      alertStore.addToast("An error has occurred, the brand does not exist.", "Delete failed", "error");
      router.push("/brands");
    } else {
      alertStore.addToast("The requested sale item does not exists.", "Delete failed", "error");
      router.push("/brands");
    }
  } finally {
    showDeleteModal.value = false;
    localStorage.setItem("brand-updated", Date.now().toString());
  }
};

const deleteBrand = (id, name, noOfSaleItems) => {
  // console.log(noOfSaleItems);
  if (noOfSaleItems > 0) {
    cannotdelete.value = true;
    brandToDeleteName.value = name;
    return;
  }
  pendingDeleteId.value = id;
  showDeleteModal.value = true;
  brandToDeleteName.value = name;
};

onMounted(async () => {
  // console.log("Brand management component mounted");

  // โหลดข้อมูล brand
  await fetchBrands();

  // เพิ่ม event listener สำหรับ storage change
  window.addEventListener("storage", onStorageChange);

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }

  localStorage.setItem("brand-updated", Date.now().toString());
});

onBeforeUnmount(() => {
  window.removeEventListener("storage", onStorageChange);
});
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto mt-[40px]">
    <Breadcrumb :class="'mb-6'"
      :pathForBreadcrumb="[{ text: 'Home', name: 'Home' }, { text: 'SaleItem', name: 'Products' }, { text: 'BrandManage' }]" />
    <h3 class="text-3xl font-bold text-blue-700">Wellcome {{ auth.getAuthData().nickname }}</h3>
    <div class="flex justify-between items-center gap-4 mb-4">
      <h1 class="text-4xl font-bold text-blue-700 flex items-center">Brand Management</h1>
      <RouterLink :to="{ name: 'BrandCreate' }" class="inline-flex items-center justify-center 
         bg-sky-500 text-white 
         hover:bg-sky-600 hover:shadow-lg 
         shadow-md shadow-sky-500/50 
         transition duration-300 ease-in-out 
         focus:outline-none focus:ring-4 focus:ring-sky-500/50 
         rounded-full group 
         
         /* === MOBILE (Circle) === */
         w-12 h-12 flex-shrink-0 
         
         /* === DESKTOP (Full Button) === */
         sm:w-auto sm:h-auto 
         ">
        <!-- 🟢 ข้อความเต็ม (แสดงเมื่อจอใหญ่กว่า 640px) -->
        <span
          class="tracking-wide hidden sm:flex items-center gap-2 text-base font-semibold px-6 py-2.5 whitespace-nowrap">
          Add New Brand
        </span>

        <!-- 🔴 Icon "+" (แสดงเมื่อจอเล็กกว่า 640px) -->
        <!-- ใช้ h-full w-full เพื่อให้ไอคอนอยู่ตรงกลางวงกลม -->
        <span class="flex sm:hidden items-center justify-center h-full w-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"
            stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
        </span>
      </RouterLink>
    </div>
    <ConfirmDelete :show="showDeleteModal" :message="`Do you want to delete brand '${brandToDeleteName}' ?`"
      @confirm="confirmDeleteProduct" @cancel="() => (showDeleteModal = false)" />
    <ConfirmDelete :show="cannotdelete"
      :message="`Cannot delete brand '${brandToDeleteName}'. There are sale items associated with this brand.`"
      confirm-text="OK" hide-cancel @confirm="() => (cannotdelete = false)" />

    <!-- Brand Table -->
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
          <tr v-for="(item, index) in brand" :key="item.id" :class="index % 2 === 0 ? 'bg-blue-50' : 'bg-white'"
            class="itbms-row border border-gray-200">
            <td class="itbms-id text-black py-2 px-4 text-center border-r border-gray-200">{{ item.id }}</td>
            <td class="itbms-name text-black py-2 px-4 text-center border-r border-gray-200">{{ item.name }}</td>
            <td class="py-2 px-4 text-center">
              <div class="flex justify-center space-x-2">
                <button @click="goToEdit(item.id)"
                  class="itbms-edit-button bg-blue-700 hover:bg-blue-800 text-white w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  <Pencil size="20" strokeWidth="1.5" />
                </button>
                <button @click="deleteBrand(item.id, item.name, item.noOfSaleItems)"
                  class="itbms-delete-button bg-white hover:bg-red-500 border border-gray-300 text-gray-700 w-8 h-8 flex items-center justify-center rounded transition duration-150 hover:cursor-pointer">
                  <Trash2 size="20" strokeWidth="1.5" />
                </button>
              </div>
            </td>
          </tr>

          <!-- Empty state -->
          <tr v-if="!brand || brand.length === 0">
            <td colspan="3" class="px-6 py-4 text-center text-gray-500 itbms-no">No brands available</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

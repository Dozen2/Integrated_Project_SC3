<script setup>
import { ref, onBeforeUnmount, onBeforeMount } from "vue";
import {
  getAllSaleItemV1,
  getAllSaleItemV2,
} from "@/libs/callAPI/apiSaleItem.js";
import SelectAllSaleItemGallery from "@/components/SaleItemComponent/SaleItemSelectAllGallery.vue";
import { useAlertStore } from "@/stores/alertStore.js";
import QueryComponent from "@/components/Common/Query/QueryComponent.vue";

const product = ref([]);
const brand = ref([]);
const initialTotalPages = ref(0);
const savedSettings = ref(null);
const alertStore = useAlertStore();

const saveSettingsToSession = (settings) => {
  sessionStorage.setItem("product-page-settings", JSON.stringify(settings));
  savedSettings.value = settings;
};

const loadSettingsFromSession = () => {
  const raw = sessionStorage.getItem("product-page-settings");
  if (raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      console.error("Error parsing saved settings:", e);
      return null;
    }
  }
  return null;
};

const fetchProduct = async () => {
  try {
    const productData = await getAllSaleItemV2([], "createdOn", "desc", 10, 0);

    product.value = productData;
    initialTotalPages.value = productData.totalPages;

    const brandData = await getAllSaleItemV1();
    brand.value = brandData;
    console.log("Fetched brand data:", brandData);
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

const fetchProductWithSettings = async (settings) => {
  try {
    let filterBrands = [];
    if (settings.filterBrands && settings.filterBrands.trim() !== "") {
      filterBrands = settings.filterBrands
        .split(",")
        .map((brand) => brand.trim());
    }

    const productData = await getAllSaleItemV2(
      filterBrands,
      settings.sortField || "createdOn",
      settings.sortDirection || "desc",
      settings.size || 10,
      settings.page || 0
    );

    console.log("Fetched product data:", productData);

    if (productData && productData.content && filterBrands.length > 0) {
      const brandOrder = filterBrands;
      console.log("Brand order:", brandOrder);

      productData.content.sort((a, b) => {
        const brandA = a.brandName?.trim() || a.brand?.name?.trim() || "";
        const brandB = b.brandName?.trim() || b.brand?.name?.trim() || "";

        const indexA = brandOrder.indexOf(brandA);
        const indexB = brandOrder.indexOf(brandB);

        if (indexA !== -1 && indexB !== -1) {
          return indexA - indexB;
        }
        if (indexA !== -1 && indexB === -1) return -1;
        if (indexA === -1 && indexB !== -1) return 1;
        return brandA.localeCompare(brandB);
      });

      console.log(
        "Sorted products:",
        productData.content.map((p) => p.brandName || p.brand?.name)
      );
    }

    product.value = productData;
    initialTotalPages.value = productData.totalPages;
  } catch (error) {
    console.error("Error fetching product data:", error);
  }
};

const handleUserInteraction = async (newSettings) => {
  saveSettingsToSession(newSettings);
  await fetchProductWithSettings(newSettings);
};

const onStorageChange = (event) => {
  if (event.key === "product-updated") {
    fetchProduct();
  }
};

onBeforeMount(async () => {
  const loadedSettings = loadSettingsFromSession();
  savedSettings.value = loadedSettings;

  if (loadedSettings) {
    await fetchProductWithSettings(loadedSettings);
  } else {
    await fetchProduct();
  }
  window.addEventListener("storage", onStorageChange);
});

onBeforeUnmount(() => {
  window.removeEventListener("storage", onStorageChange);
});
</script>

<template>
  <div class="flex flex-col gap-4 mx-[225px] mt-[50px]">
    <div v-if="alertStore.message">
      <div
        :class="`itbms-message px-4 py-2 rounded ${
          alertStore.type === 'error'
            ? 'bg-red-100 text-red-700'
            : 'bg-green-100 text-green-700'
        }`"
      >
        {{ alertStore.message }}
      </div>
    </div>

    <div class="flex items-center justify-between gap-4">
      <RouterLink
        :to="{ name: 'ProuctCreate' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        <span class="itbms-sale-item-add tracking-wide">New Product</span>
      </RouterLink>

      <RouterLink
        :to="{ name: 'ProductManage' }"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-m font-medium px-5 py-2.5 rounded-full shadow-md hover:shadow-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-4 h-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
        <span class="itbms-manage-brand tracking-wide">Manage Sale Items</span>
      </RouterLink>
    </div>

    <QueryComponent
      @urlSetting="handleUserInteraction"
      :initialSize="
        savedSettings?.size !== undefined ? Number(savedSettings.size) : 10
      "
      :initialFilterBrands="savedSettings?.filterBrands || ''"
      :initialSortField="savedSettings?.sortField || ''"
      :initialSortDirection="savedSettings?.sortDirection || ''"
      :showSizeAndSort="true"
      :showFilter="true"
    />

    <SelectAllSaleItemGallery
      v-if="product?.content"
      :product="product.content"
    />

    <QueryComponent
      @urlSetting="handleUserInteraction"
      :initialTotalPages="initialTotalPages"
      :initialPage="
        savedSettings?.page !== undefined ? Number(savedSettings.page) + 1 : 1
      "
      :showPagination="true"
    />
  </div>
  
</template>

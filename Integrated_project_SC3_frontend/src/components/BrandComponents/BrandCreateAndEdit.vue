<script setup>
import { computed, ref, reactive, onBeforeMount, watch } from "vue";
import { addData, getDataById, updateData } from "@/libs/api";
import { useRoute, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import Breadcrumb from "../Common/Breadcrumb.vue";
import { Smartphone } from "lucide-vue-next";
import { useAuthStore } from "@/stores/auth";

const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;

const router = useRouter();
const route = useRoute();
const alertStore = useAlertStore();

const blockTailwind = "block w-full pl-5 pr-3 py-2.5 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500";
const blockTailwindError = "block w-full pl-5 pr-3 py-2.5 border border-red-400 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-blue-500";

const blockTailwindName = ref(blockTailwind);
const blockTailwindWebsiteURL = ref(blockTailwind);
const blockTailwindCountryOfOrigin = ref(blockTailwind);

const prop = defineProps({
  mode: {
    type: String,
  },
  brandId: [String, Number],
});

const isEdit = prop.brandId;
const brand = reactive({
  id: null,
  name: "",
  websiteUrl: "",
  countryOfOrigin: "",
  isActive: false,
});

onBeforeMount(async () => {
  if (prop.mode === "edit" && isEdit) {
    try {
      const data = await getDataById(VITE_ROOT_API_URL + `/itb-mshop/v1/brands`, isEdit);
      brand.id = data.id;
      brand.name = data.name;
      brand.websiteUrl = data.websiteUrl;
      brand.countryOfOrigin = data.countryOfOrigin;
      brand.isActive = data.isActive;
      Object.assign(originalBrand, JSON.parse(JSON.stringify(brand)));
      console.log("Brand data loaded for editing:", data);
    } catch (err) {
      console.log(err);
    }
  }
});

const trimField = (field) => {
  if (typeof brand[field] === "string") brand[field] = brand[field].trim();
};

const originalBrand = reactive({});
const handleCancel = () => {
  router.push({ name: "BrandManage" });
};

const compareProduct = (a, b) => {
  if (a === b) return true;
  if (typeof a !== "object" || typeof b !== "object" || a === null || b === null) return false;

  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  if (keysA.length !== keysB.length) return false;

  return keysA.every((key) => compareProduct(a[key], b[key]));
};

const isBrandChanged = computed(() => !compareProduct(brand, originalBrand));

const isSaving = ref(true);

const isFormValid = computed(() => {
  return (
    brand.name !== null && brand.name.trim() !== ""
    // brand.websiteUrl.trim() !== "" &&
    // brand.countryOfOrigin.trim() !== ""
  );
});

const normalizeEmptyStringsToNull = (obj) => {
  for (const key in obj) {
    if (typeof obj[key] === "string" && obj[key].trim() === "") {
      obj[key] = null;
    } else if (typeof obj[key] === "object" && obj[key] !== null) {
      normalizeEmptyStringsToNull(obj[key]);
    }
  }
};

watch(
  brand,
  () => {
    validationBrandForm();
  },
  { deep: true }
);

const maxLength = {
  name: 30,
  webSiteUrl: 40,
  countryOfOrigin: 80,
};

const isValidUrl = (url) => {
  if (!url) return true; // not specified = valid
  try {
    new URL(url);
    return true;
  } catch (_) {
    return false;
  }
};

const validationBrandForm = () => {
  let isValid = true;
  if (!brand.name || brand.name.length > maxLength.name) {
    blockTailwindName.value = blockTailwindError;
    isValid = false;
  } else {
    blockTailwindName.value = blockTailwind;
  }

  if (!isValidUrl(brand.websiteUrl)) {
    blockTailwindWebsiteURL.value = blockTailwindError;
    isValid = false;
  } else {
    blockTailwindWebsiteURL.value = blockTailwind;
  }

  if ((brand.countryOfOrigin?.length ?? 0) > maxLength.countryOfOrigin) {
    blockTailwindCountryOfOrigin.value = blockTailwindError;
    isValid = false;
  } else {
    blockTailwindCountryOfOrigin.value = blockTailwind;
  }

  isSaving.value = isValid;
};

const handleSave = async () => {
  const newBrand = {
    name: brand.name,
    websiteUrl: brand.websiteUrl,
    isActive: brand.isActive,
    countryOfOrigin: brand.countryOfOrigin,
  };
  isSaving.value = false;
  if (!isFormValid.value) {
    isSaving.value = true;
    return;
  }
  normalizeEmptyStringsToNull(newBrand);
  try {
    if (prop.mode === "edit" && isEdit) {
      await updateData(VITE_ROOT_API_URL + `/itb-mshop/v1/brands`, isEdit, newBrand);
      alertStore.addToast("The sale item has been updated.", "Update success", "success");
      router.push({ name: "BrandManage" });
      alertStore.addToast("The brand has been updated.", "Update success", "success");
    } else {
      await addData(VITE_ROOT_API_URL + `/itb-mshop/v1/brands`, newBrand);
      alertStore.addToast("The brand has been added.", "Add success", "success");
      router.push({ name: "BrandManage" });
      alertStore.addToast("The brand has been added.", "Add success", "success");
    }
  } catch (err) {
    console.log(err);
  } finally {
    isSaving.value = true;
    localStorage.setItem("brand-updated", Date.now().toString());
  }
};
const auth = useAuthStore();

</script>

<template>

  <div class="p-6">
    <div class="max-w-4xl mx-auto">

     
      <!-- Header -->
      <div class="mb-6 ">
        <div class="flex items-center mt-[40px]">
        
      </div>

        <span v-if="isEdit">
          <Breadcrumb
          :class="'mb-6'"
          :pathForBreadcrumb="[
            { text: 'Home', name: 'Home' },
            { text: 'SaleItem', name: 'Products' },
            { text: 'Edit Brand' },
          ]"
    />
  </span>
  <span v-else>
<Breadcrumb
      :class="'mb-6'"
      :pathForBreadcrumb="[
        { text: 'Home', name: 'Home' },
        { text: 'SaleItem', name: 'Products' },
        { text: 'Create Brand' },
      ]"
    />
  </span>
      </div>

 <h3 class="text-3xl font-bold text-blue-700">Wellcome {{ auth.getAuthData().nickname }}</h3>
   <h1 class="text-4xl font-bold text-blue-700 flex items-center mb-4">Brand Management</h1>

      <!-- Form Card -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden border border-blue-100">
        <!-- Card Header -->
        <div class="bg-blue-600 px-6 py-4">
          <h2 class="text-xl font-semibold text-white flex items-center">
            {{ isEdit ? "Edit Brand" : "New Brand" }}
          </h2>
        </div>

        <!-- Form Content -->
        <div class="p-6">
          <div class="space-y-6">
            <!-- Name Field -->
            <div>
              <label for="itbms-name" class="block text-sm font-medium text-gray-700 mb-1"> Brand Name <span class="text-red-500">*</span> </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"></div>
                <input v-model="brand.name" type="text" required @blur="trimField('name')" :class="`itbms-name ${blockTailwindName}`" placeholder="Enter brand name" />
              </div>
              <p v-show="!brand.name || brand.name.length > maxLength.name" class="itbms-message mt-1 text-sm text-red-500">Brand name must be 1-30 characters long.</p>
            </div>

            <!-- Website URL Field -->
            <div>
              <label for="itbms-websiteUrl" class="block text-sm font-medium text-gray-700 mb-1"> Website URL </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"></div>
                <input v-model="brand.websiteUrl" type="text" @blur="trimField('websiteUrl')" :class="`itbms-websiteUrl ${blockTailwindWebsiteURL}`" placeholder="https://example.com" />
              </div>
              <p v-if="!isValidUrl(brand.websiteUrl)" class="itbms-message mt-1 text-sm text-red-500">Brand URL must be a valid URL or not specified.</p>
            </div>

            <!-- Country of Origin Field -->
            <div>
              <label for="itbms-countryOfOrigin" class="block text-sm font-medium text-gray-700 mb-1"> Country of Origin </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"></div>
                <input
                  v-model="brand.countryOfOrigin"
                  type="text"
                  @blur="trimField('countryOfOrigin')"
                  :class="`itbms-countryOfOrigin ${blockTailwindCountryOfOrigin}`"
                  placeholder="Enter country of origin"
                />
              </div>
              <p v-if="(brand.countryOfOrigin?.length ?? 0) > maxLength.countryOfOrigin" class="itbms-message mt-1 text-sm text-red-500">
                Brand country of origin must be 1-80 characters long or not specified.
              </p>
            </div>
            <div class="flex items-center">
              <div class="relative inline-block w-12 mr-3 align-middle select-none transition duration-200 ease-in">
                <input
                  id="itbms-isActive"
                  type="checkbox"
                  name="toggle"
                  v-model="brand.isActive"
                  class="peer absolute w-6 h-6 bg-white border-4 rounded-full appearance-none cursor-pointer transition-all duration-300 ease-in-out checked:translate-x-6"
                />
                <label for="itbms-isActive" class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer transition-all duration-300 ease-in-out peer-checked:bg-blue-500"></label>
              </div>

              <div class="ml-2 text-sm">
                <label for="itbms-isActive" class="font-medium text-gray-700">Active</label>
              </div>
            </div>
          </div>
          <div class="mt-8 flex flex-col sm:flex-row sm:space-x-4 space-y-3 sm:space-y-0">
            <button
              @click="handleSave"
              :disabled="!isSaving || !isFormValid || (prop.mode === 'edit' && !isBrandChanged)"
              type="submit"
              class="itbms-save-button w-full sm:w-auto flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-lg shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              Save
            </button>
            <button
              type="button"
              @click="handleCancel"
              class="itbms-cancel-button w-full sm:w-auto flex justify-center items-center px-6 py-3 border border-blue-300 text-base font-medium rounded-lg shadow-sm text-blue-700 bg-white hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200 cursor-pointer"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  defineProps,
  onBeforeMount,
  computed,
  watch,
} from "vue";
import {
  addSaleItem,
  getSaleItemById,
  updateSaleItem,
} from "@/libs/callAPI/apiSaleItem.js";
import BrandDropdown from "./../BrandComponents/BrandDropdown.vue";
import { useRoute, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand";

const boxTextTailwind =
  "w-full p-3 border border-blue-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white shadow-sm transition-all duration-200";
const boxTextTailwindError =
  "w-full p-3 border-2 border-red-400 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none bg-white shadow-sm transition-all duration-200";

const brandError = ref(false);
const boxTextTailwindModel = ref(boxTextTailwind);
const boxTextTailwindPrice = ref(boxTextTailwind);
const boxTextTailwindQuantity = ref(boxTextTailwind);
const boxTextTailwindDesc = ref(boxTextTailwind);
const boxTextTailwindRamGB = ref(boxTextTailwind);
const boxTextTailwindStorageGB = ref(boxTextTailwind);
const boxTextTailwindColor = ref(boxTextTailwind);
const boxTextTailwindScreenSizeInch = ref(boxTextTailwind);

const reloadData = ref(0);
const isSaving = ref(true);
const route = useRoute();
const router = useRouter();
const alertStore = useAlertStore();

const prop = defineProps({
  mode: String,
  productId: [String, Number],
});

const product = reactive({
  id: null,
  model: "",
  brand: { id: null, name: "" },
  description: "",
  price: null,
  ramGb: null,
  screenSizeInch: null,
  quantity: 1,
  storageGb: null,
  color: "",
});

const originalProduct = reactive({});

onBeforeMount(async () => {
  if (prop.mode === "Edit") {
    try {
      const data = await getSaleItemById(prop.productId);
      if (!data) {
        product.model = "404_not_found";
        setTimeout(() => router.push("/sale-items"), 2000);
        return;
      }
      product.id = data.id;
      product.model = data.model;
      product.brand.name = data.brandName;
      product.description = data.description;
      product.price = data.price;
      product.ramGb = data.ramGb;
      product.screenSizeInch = data.screenSizeInch;
      product.quantity = data.quantity;
      product.storageGb = data.storageGb;
      product.color = data.color;

      await getBrandIdByName(data.brandName);
      Object.assign(originalProduct, JSON.parse(JSON.stringify(product)));
    } catch (error) {
      console.error("Error loading product:", error);
    }
  }
});

const getBrandIdByName = async (brandName) => {
  const data = await getAllBrand();
  const brand = data.find((b) => b.name === brandName);
  if (brand) {
    product.brand.id = brand.id;
  } else {
    console.error("Brand not found");
  }
};

const handleBrandId = (id) => (product.brand.id = id);
const handleBrandName = (name) => (product.brand.name = name);

const trimField = (field) => {
  if (typeof product[field] === "string")
    product[field] = product[field].trim();
};

// ตรวจสอบว่า product ถูกเปลี่ยนแปลงหรือไม่ (ใช้สำหรับ Edit mode)
const compareProduct = (a, b) => {
  if (a === b) return true;
  if (
    typeof a !== "object" ||
    typeof b !== "object" ||
    a === null ||
    b === null
  )
    return false;

  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  if (keysA.length !== keysB.length) return false;

  return keysA.every((key) => compareProduct(a[key], b[key]));
};

const isProductChanged = computed(
  () => !compareProduct(product, originalProduct)
);

const maxLength = {
  model: 60,
  description: 65535,
  color: 40,
};
watch(
  product,
  () => {
    validationProductForm();
  },
  { deep: true }
);

const checkDecimal = (num) => {
  return !(Math.floor(num * 100) === num * 100);
};

const validationProductForm = () => {
  let isValid = true;
  if (!product.model || (product.model?.length ?? 0) > maxLength.model) {
    boxTextTailwindModel.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindModel.value = boxTextTailwind;
  }

  // Price
  if (product.price < 0) {
    boxTextTailwindPrice.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindPrice.value = boxTextTailwind;
  }

  // RAM
  if (typeof product.ramGb === "number" && product.ramGb <= 0) {
    boxTextTailwindRamGB.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindRamGB.value = boxTextTailwind;
  }

  // Storage
  if (typeof product.storageGb === "number" && product.storageGb <= 0) {
    boxTextTailwindStorageGB.value = boxTextTailwindError;
    console.log(typeof product.storageGb);
    isValid = false;
  } else {
    boxTextTailwindStorageGB.value = boxTextTailwind;
  }

  // Description
  if (
    !product.description ||
    product.description.length > maxLength.description
  ) {
    boxTextTailwindDesc.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindDesc.value = boxTextTailwind;
  }

  // Quantity
  if (product.quantity === null || product.quantity < 0) {
    boxTextTailwindQuantity.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindQuantity.value = boxTextTailwind;
  }

  // Color
  if ((product.color?.length ?? 0) > maxLength.color) {
    boxTextTailwindColor.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindColor.value = boxTextTailwind;
  }

  // Screen Size
  if (
    (typeof product.screenSizeInch === "number" &&
      product.screenSizeInch <= 0) ||
    checkDecimal(product.screenSizeInch)
  ) {
    boxTextTailwindScreenSizeInch.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindScreenSizeInch.value = boxTextTailwind;
  }

  // Brand
  if (!product.brand.id || !product.brand.name) {
    brandError.value = true;
    isValid = false;
  } else {
    brandError.value = false;
  }

  isSaving.value = isValid;
};

const isFormValid = computed(() => {
  return (
    product.brand.id !== null &&
    product.brand.name.trim() !== "" &&
    product.model.trim() !== "" &&
    product.price >= 0 &&
    product.quantity >= 0 &&
    product.description.trim() !== ""
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

const saveProduct = async () => {
  isSaving.value = false;

  // reset styles
  boxTextTailwindModel.value = boxTextTailwind;
  boxTextTailwindPrice.value = boxTextTailwind;
  boxTextTailwindQuantity.value = boxTextTailwind;
  boxTextTailwindDesc.value = boxTextTailwind;
  brandError.value = false;

  // Validate fields
  if (!product.brand.id || !product.brand.name) brandError.value = true;
  if (!product.model) boxTextTailwindModel.value = boxTextTailwindError;
  if (!product.price || product.price < 0)
    boxTextTailwindPrice.value = boxTextTailwindError;
  if (!product.quantity || product.quantity < 0)
    boxTextTailwindQuantity.value = boxTextTailwindError;
  if (!product.description) boxTextTailwindDesc.value = boxTextTailwindError;
  if (!isFormValid.value) {
    isSaving.value = true;
    return;
  }

  normalizeEmptyStringsToNull(product);

  try {
    if (product.id) {
      await updateSaleItem(product.id, product);
      alertStore.setMessage("The sale item has been updated.");
      router.go(-1);
    } else if (prop.mode === "Edit") {
      await updateSaleItem(product.id, product);

      alertStore.setMessage("The sale item has been updated.");
      router.go(-1);
    } else {
      await addSaleItem(product);
      setSessionStorage();
      alertStore.setMessage("The sale item has been successfully added.");
      router.go(-1);
    }
  } catch (err) {
    console.error("เกิดข้อผิดพลาดระหว่างบันทึก:", err.message);
    alert(err.message);
    router.push(`/sale-items`);
  } finally {
    isSaving.value = true;
    reloadData.value++;
    sessionStorage.setItem("product-updated", Date.now().toString());
  }
};

const setSessionStorage = () => {
  const raw = sessionStorage.getItem("product-page-settings");

  if (raw) {
    const settings = JSON.parse(raw);
    settings.page = 0;
    sessionStorage.setItem("product-page-settings", JSON.stringify(settings));
    console.log("✔️ page updated to 0 and saved back:", settings);
  } else {
    console.log("⚠️ No settings found in sessionStorage.");
  }
};
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-b from-blue-50 to-white py-12 px-4 sm:px-6"
  >
    <div class="max-w-3xl mx-auto">
      <!-- Header with decorative elements -->
      <div class="relative mb-8">
        <div class="absolute inset-0 flex items-center" aria-hidden="true">
          <div class="w-full border-t border-blue-300"></div>
        </div>
        <div class="relative flex justify-center">
          <span
            class="bg-gradient-to-r from-blue-100 via-white to-blue-100 px-6 py-2 rounded-full"
          >
            <h1 class="text-2xl font-bold text-blue-700 flex items-center">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6 mr-2"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                <line x1="12" y1="18" x2="12" y2="18"></line>
              </svg>
              {{
                prop.mode === "Edit" ? "Edit Phone Details" : "Add New Phone"
              }}
            </h1>
          </span>
        </div>
      </div>

      <!-- Main Form Card -->
      <div
        class="bg-white rounded-2xl shadow-xl overflow-hidden border border-blue-200"
      >
        <!-- Card Header -->
        <div class="bg-gradient-to-r from-blue-600 to-blue-500 px-6 py-4">
          <h2 class="text-xl font-semibold text-white flex items-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5 mr-2"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path
                d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z"
              ></path>
            </svg>
            Phone Specifications
          </h2>
        </div>

        <!-- Form Content -->
        <div class="p-6">
          <div class="space-y-6">
            <!-- Brand Selection -->
            <div class="bg-blue-50 p-4 rounded-lg border border-blue-100">
              <div class="flex flex-col sm:flex-row sm:items-center gap-3">
                <label
                  for="brand"
                  class="font-medium text-blue-800 sm:w-40 flex items-center"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-5 w-5 mr-2"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"
                    ></path>
                    <line x1="7" y1="7" x2="7.01" y2="7"></line>
                  </svg>
                  Brand
                  <span class="text-red-500 ml-1">*</span>
                </label>
                <div class="flex-1">
                  <BrandDropdown
                    :brandError="brandError"
                    :brandName="product.brand.name"
                    :reloadData="reloadData"
                    @sendBrandId="handleBrandId"
                    @sendBrandName="handleBrandName"
                  />
                  <p
                    v-show="brandError"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Brand must be selected.
                  </p>
                </div>
              </div>
            </div>

            <!-- Main Form Fields -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Left Column -->
              <div class="space-y-6">
                <!-- Model -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <rect
                        x="2"
                        y="3"
                        width="20"
                        height="14"
                        rx="2"
                        ry="2"
                      ></rect>
                      <line x1="8" y1="21" x2="16" y2="21"></line>
                      <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                    Model
                    <span class="text-red-500 ml-1">*</span>
                  </label>
                  <input
                    type="text"
                    v-model="product.model"
                    @blur="trimField('model')"
                    :class="`itbms-model ${boxTextTailwindModel}`"
                    placeholder="e.g. iPhone 13 Pro"
                  />
                  <p
                    v-show="product.model.length > maxLength.model"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Model must be 1-60 characters long.
                  </p>
                </div>

                <!-- Price -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <line x1="12" y1="1" x2="12" y2="23"></line>
                      <path
                        d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"
                      ></path>
                    </svg>
                    Price (Baht)
                    <span class="text-red-500 ml-1">*</span>
                  </label>
                  <div class="relative">
                    <div
                      class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
                    >
                      <span class="text-gray-500">฿</span>
                    </div>
                    <input
                      type="number"
                      v-model.number="product.price"
                      :class="`itbms-price pl-8 ${boxTextTailwindPrice}`"
                      placeholder="e.g. 29900"
                    />
                  </div>
                  <p
                    v-show="product.price < 0"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Price must be non-negative integer.
                  </p>
                </div>

                <!-- RAM -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <rect
                        x="2"
                        y="2"
                        width="20"
                        height="8"
                        rx="2"
                        ry="2"
                      ></rect>
                      <rect
                        x="2"
                        y="14"
                        width="20"
                        height="8"
                        rx="2"
                        ry="2"
                      ></rect>
                    </svg>
                    RAM (GB)
                  </label>
                  <input
                    type="number"
                    v-model.number="product.ramGb"
                    :class="`itbms-ramGb ${boxTextTailwindRamGB}`"
                    placeholder="e.g. 8"
                  />
                  <p
                    v-show="boxTextTailwindRamGB === boxTextTailwindError"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    RAM size must be positive integer or not specified.
                  </p>
                </div>

                <!-- Storage -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path
                        d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"
                      ></path>
                    </svg>
                    Storage (GB)
                  </label>
                  <input
                    type="number"
                    v-model.number="product.storageGb"
                    :class="`itbms-storageGb ${boxTextTailwindStorageGB}`"
                    placeholder="e.g. 128"
                  />
                  <p
                    v-show="boxTextTailwindStorageGB === boxTextTailwindError"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Storage size must be positive integer or not specified.
                  </p>
                </div>
              </div>

              <!-- Right Column -->
              <div class="space-y-6">
                <!-- Screen Size -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <rect
                        x="2"
                        y="3"
                        width="20"
                        height="14"
                        rx="2"
                        ry="2"
                      ></rect>
                      <line x1="8" y1="21" x2="16" y2="21"></line>
                      <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                    Screen Size (Inches)
                  </label>
                  <input
                    type="number"
                    v-model.number="product.screenSizeInch"
                    step="0.1"
                    :class="`itbms-screenSizeInch ${boxTextTailwindScreenSizeInch}`"
                    placeholder="e.g. 6.1"
                  />
                  <p
                    v-show="
                      boxTextTailwindScreenSizeInch === boxTextTailwindError
                    "
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Screen size must be positive number with at most 2 decimal
                    points or not specified.
                  </p>
                </div>

                <!-- Color -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <circle cx="13.5" cy="6.5" r="2.5"></circle>
                      <circle cx="19" cy="13" r="2.5"></circle>
                      <circle cx="6" cy="12" r="2.5"></circle>
                      <circle cx="10" cy="20" r="2.5"></circle>
                    </svg>
                    Color
                  </label>
                  <input
                    type="text"
                    v-model="product.color"
                    @blur="trimField('color')"
                    :class="`itbms-color ${boxTextTailwindColor}`"
                    placeholder="e.g. Midnight Blue"
                  />
                  <p
                    v-show="(product.color?.length ?? 0) > maxLength.color"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Color must be 1-40 characters long or not specified.
                  </p>
                </div>

                <!-- Quantity -->
                <div>
                  <label
                    class="block font-medium text-gray-700 mb-1 flex items-center"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-5 w-5 mr-2 text-blue-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path
                        d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"
                      ></path>
                      <line x1="3" y1="6" x2="21" y2="6"></line>
                      <path d="M16 10a4 4 0 0 1-8 0"></path>
                    </svg>
                    Quantity
                    <span class="text-red-500 ml-1">*</span>
                  </label>
                  <input
                    type="number"
                    v-model.number="product.quantity"
                    @blur=""
                    @focus="product.quantity = null"
                    :class="`itbms-quantity ${boxTextTailwindQuantity}`"
                    placeholder="e.g. 10"
                  />
                  <p
                    v-show="boxTextTailwindQuantity === boxTextTailwindError"
                    class="itbms-message mt-1 text-sm text-red-500"
                  >
                    Quantity must be non-negative integer.
                  </p>
                </div>
              </div>
            </div>

            <!-- Description (Full Width) -->
            <div>
              <label
                class="block font-medium text-gray-700 mb-1 flex items-center"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5 mr-2 text-blue-500"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <line x1="8" y1="6" x2="21" y2="6"></line>
                  <line x1="8" y1="12" x2="21" y2="12"></line>
                  <line x1="8" y1="18" x2="21" y2="18"></line>
                  <line x1="3" y1="6" x2="3.01" y2="6"></line>
                  <line x1="3" y1="12" x2="3.01" y2="12"></line>
                  <line x1="3" y1="18" x2="3.01" y2="18"></line>
                </svg>
                Description
                <span class="text-red-500 ml-1">*</span>
              </label>
              <textarea
                v-model="product.description"
                rows="4"
                @blur="trimField('description')"
                :class="`itbms-description ${boxTextTailwindDesc}`"
                placeholder="Enter product description..."
              ></textarea>
              <p
                v-show="product.description.length > maxLength.description"
                class="itbms-message mt-1 text-sm text-red-500"
              >
                Description must be 1-65,535 characters long.
              </p>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mt-8 flex flex-col sm:flex-row sm:justify-end gap-4">
            <button
              type="button"
              class="itbms-cancel-button order-2 sm:order-1 flex items-center justify-center px-6 py-3 border border-blue-300 text-blue-700 font-medium rounded-lg shadow-sm hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
              @click="router.go(-1)"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 mr-2"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              Cancel
            </button>

            <button
              type="submit"
              class="itbms-save-button order-1 sm:order-2 flex items-center justify-center px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-500 text-white font-medium rounded-lg shadow-sm hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="
                !isSaving ||
                !isFormValid ||
                (prop.mode === 'Edit' && !isProductChanged)
              "
              @click="saveProduct"
            >
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>

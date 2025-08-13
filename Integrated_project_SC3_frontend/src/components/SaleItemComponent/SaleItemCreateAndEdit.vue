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
  "w-[600px] p-3 border border-blue-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white shadow-sm transition-all duration-200";
const boxTextTailwindError =
  "w-[600px] p-3 border-2 border-red-400 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none bg-white shadow-sm transition-all duration-200";

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

const files = ref([]);

// เมื่อเลือกไฟล์
// แก้ไข handleFileChange เพื่อเชื่อมกับ images
const handleFileChange = (event) => {
  const selectedFiles = Array.from(event.target.files);
  files.value.push(...selectedFiles);
  console.log(files);
  // แปลงไฟล์เป็น URL สำหรับแสดงผล
  selectedFiles.forEach(file => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const url = e.target.result
      console.log("File URL (Base64):", url);
      images.value.push(url);
    };
    reader.readAsDataURL(file);
  });
  
  
  event.target.value = "";
};

// ลบไฟล์
// แก้ไข removeFile เพื่อลบทั้งใน files และ images
const removeFile = (index) => {
  files.value.splice(index, 1);
  images.value.splice(index, 1);
  
  // ปรับ currentIndex ถ้าจำเป็น
  if (currentIndex.value >= images.value.length && images.value.length > 0) {
    currentIndex.value = images.value.length - 1;
  } else if (images.value.length === 0) {
    currentIndex.value = 0;
  }
};

const moveUp = (index) => {
  if (index > 0) {
    const temp = files.value[index];
    files.value[index] = files.value[index - 1];
    files.value[index - 1] = temp;
  }
};

const moveDown = (index) => {
  if (index < files.value.length - 1) {
    const temp = files.value[index];
    files.value[index] = files.value[index + 1];
    files.value[index + 1] = temp;
  }
};

// เพิ่มตัวแปรที่ขาดหาย
const images = ref([]); // default image
const currentIndex = ref(0);

// เพิ่มฟังก์ชันที่ขาดหาย
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  } else {
    currentIndex.value = images.value.length - 1;
  }
};

const nextImage = () => {
  if (currentIndex.value < images.value.length - 1) {
    currentIndex.value++;
  } else {
    currentIndex.value = 0;
  }
};

</script>

<template>
  <div class="bg-white h-screen w-auto">
    <!-- Header with decorative elements -->
    <div class="relative mb-5 mt-6">
      <div class="absolute inset-0 flex items-center" aria-hidden="true">
        <div class="w-2/3 mx-auto border-t border-blue-300"></div>
      </div>
      <div class="relative flex justify-center">
        <span class="bg-gradient-to-r from-blue-100 via-white to-blue-100 px-6 py-2 rounded-full">
          <h1 class="text-2xl font-bold text-blue-700 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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

    <div class="h-full m-5 flex flex-row justify-center items-center">

<div class="grid grid-rows-[auto_auto] gap-4 p-4 flex-1/2 relative">
    
    <!-- แสดงเฉพาะเมื่อมีรูป -->
    <div v-if="images.length > 0">
      <!-- ปุ่มซ้าย -->
      <button @click="prevImage" v-show="images.length > 1"
              class="absolute left-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
        &lt;
      </button>

      <!-- ช่องใหญ่ด้านบน แสดงรูป -->
      <div class="bg-gray-100 rounded border border-blue-400 h-96 flex items-center justify-center">
        <img :src="images[currentIndex]" alt="main image" class="max-h-full max-w-full object-contain" />
      </div>

      <!-- ปุ่มขวา -->
      <button @click="nextImage" v-show="images.length > 1"
              class="absolute right-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
        &gt;
      </button>

      <!-- ช่องเล็ก 4 ช่องด้านล่าง -->
      <div class="grid grid-cols-4 gap-4 mt-3" v-if="images.length > 1">
        <div v-for="(img, idx) in images.slice(0, 4)" :key="idx"
             @click="currentIndex = idx"
             :class="['rounded border', idx === currentIndex ? 'border-blue-600' : 'border-blue-400', 'h-32 cursor-pointer']">
          <img :src="img" alt="thumbnail" class="w-full h-full object-cover rounded" />
        </div>
      </div>
    </div>

    <!-- แสดงเมื่อไม่มีรูป -->
    <div v-else class="bg-gray-100 rounded border-2 border-dashed border-gray-300 h-96 flex items-center justify-center">
      <div class="text-center text-gray-500">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v14a2 2 0 002 2z" />
        </svg>
        <p class="text-lg font-medium">No images uploaded</p>
        <p class="text-sm">Upload images to preview them here</p>
      </div>
    </div>
  </div>



      <div class="m-3 p-6 h-[600px] w-[600px] rounded-2xl bg-white flex-1/2">

        <!-- Brand Selection -->
        <div class="w-full max-w-[500px] mb-6">
          <div class="flex flex-col sm:flex-row sm:items-center gap-4">
            <label for="brand" class="font-medium text-blue-800 w-24 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"></path>
                <line x1="7" y1="7" x2="7.01" y2="7"></line>
              </svg>
              Brand
              <span class="text-red-500 ml-1">*</span>
            </label>
            <div class="flex-1">
              <BrandDropdown :brandError="brandError" :brandName="product.brand.name" :reloadData="reloadData"
                @sendBrandId="handleBrandId" @sendBrandName="handleBrandName" />
              <p v-show="brandError" class="itbms-message mt-1 text-sm text-red-500">Brand must be selected.</p>
            </div>
          </div>
        </div>

        <!-- Model -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
            Model
            <span class="text-red-500 ml-1">*</span>
          </label>
          <div class="flex-1">
            <input type="text" v-model="product.model" @blur="trimField('model')"
              :class="`itbms-model ${boxTextTailwindModel} w-full`" placeholder="e.g. iPhone 13 Pro" />
            <p v-show="product.model.length > maxLength.model" class="itbms-message mt-1 text-sm text-red-500">
              Model must be 1-60 characters long.
            </p>
          </div>
        </div>

        <!-- Price -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            Price (Baht)
            <span class="text-red-500 ml-1">*</span>
          </label>
          <div class="flex-1 relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <span class="text-gray-500">฿</span>
            </div>
            <input type="number" v-model.number="product.price"
              :class="`itbms-price pl-8 ${boxTextTailwindPrice} w-full`" placeholder="e.g. 29900" />
          </div>
          <p v-show="product.price < 0" class="itbms-message mt-1 text-sm text-red-500 ml-24">
            Price must be non-negative integer.
          </p>
        </div>

        <!-- RAM -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
              <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
            </svg>
            RAM (GB)
          </label>
          <div class="flex-1">
            <input type="number" v-model.number="product.ramGb" :class="`itbms-ramGb ${boxTextTailwindRamGB} w-full`"
              placeholder="e.g. 8" />
            <p v-show="boxTextTailwindRamGB === boxTextTailwindError" class="itbms-message mt-1 text-sm text-red-500">
              RAM size must be positive integer or not specified.
            </p>
          </div>
        </div>

        <!-- Storage -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path
                d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z">
              </path>
            </svg>
            Storage (GB)
          </label>
          <div class="flex-1">
            <input type="number" v-model.number="product.storageGb"
              :class="`itbms-storageGb ${boxTextTailwindStorageGB} w-full`" placeholder="e.g. 128" />
            <p v-show="boxTextTailwindStorageGB === boxTextTailwindError"
              class="itbms-message mt-1 text-sm text-red-500">
              Storage size must be positive integer or not specified.
            </p>
          </div>
        </div>

        <!-- Screen Size -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
            Screen Size (Inches)
          </label>
          <div class="flex-1">
            <input type="number" v-model.number="product.screenSizeInch" step="0.1"
              :class="`itbms-screenSizeInch ${boxTextTailwindScreenSizeInch} w-full`" placeholder="e.g. 6.1" />
            <p v-show="boxTextTailwindScreenSizeInch === boxTextTailwindError"
              class="itbms-message mt-1 text-sm text-red-500">
              Screen size must be positive number with at most 2 decimal
              points or not specified.
            </p>
          </div>
        </div>

        <!-- Color -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="13.5" cy="6.5" r="2.5"></circle>
              <circle cx="19" cy="13" r="2.5"></circle>
              <circle cx="6" cy="12" r="2.5"></circle>
              <circle cx="10" cy="20" r="2.5"></circle>
            </svg>
            Color
          </label>
          <div class="flex-1">
            <input type="text" v-model="product.color" @blur="trimField('color')"
              :class="`itbms-color ${boxTextTailwindColor} w-full`" placeholder="e.g. Midnight Blue" />
            <p v-show="(product.color?.length ?? 0) > maxLength.color" class="itbms-message mt-1 text-sm text-red-500">
              Color must be 1-40 characters long or not specified.
            </p>
          </div>
        </div>

        <!-- Quantity -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            Quantity
            <span class="text-red-500 ml-1">*</span>
          </label>
          <div class="flex-1">
            <input type="number" v-model.number="product.quantity" @focus="product.quantity = null"
              :class="`itbms-quantity ${boxTextTailwindQuantity} w-full`" placeholder="e.g. 10" />
            <p v-show="boxTextTailwindQuantity === boxTextTailwindError"
              class="itbms-message mt-1 text-sm text-red-500">
              Quantity must be non-negative integer.
            </p>
          </div>
        </div>

        <!-- Description (Full Width) -->
        <div class="max-w-[600px] mb-6">
          <label class="block font-medium text-gray-700 mb-1 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
          <textarea v-model="product.description" rows="4" @blur="trimField('description')"
            :class="`itbms-description ${boxTextTailwindDesc} w-full`"
            placeholder="Enter product description..."></textarea>
          <p v-show="product.description.length > maxLength.description"
            class="itbms-message mt-1 text-sm text-red-500">
            Description must be 1-65,535 characters long.
          </p>
        </div>

        <!-- input img -->
        <div class="mb-6 max-w-[500px]">
          <label for="file-upload"
            class="cursor-pointer inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition">
            <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#000000">
              <path
                d="M440-320v-326L336-542l-56-58 200-200 200 200-56 58-104-104v326h-80ZM240-160q-33 0-56.5-23.5T160-240v-120h80v120h480v-120h80v120q0 33-23.5 56.5T720-160H240Z" />
            </svg>
          </label>
          <input id="file-upload" type="file" class="hidden" multiple @change="handleFileChange" />
        </div>
        <!-- รายการไฟล์ -->
        <ul v-if="files.length > 0" class="space-y-2">
          <li v-for="(file, index) in files" :key="index"
            class="flex items-center justify-between bg-gray-100 p-2 rounded w-125">
            <!-- ชื่อไฟล์ -->
            <span class="truncate max-w-[200px]">{{ file.name }}</span>

            <!-- ปุ่ม action -->
            <div class="flex gap-2">
              <button @click="moveUp(index)" :disabled="index === 0"
                class="bg-gray-300 px-2 py-1 rounded disabled:opacity-50">
                ⬆️
              </button>
              <button @click="moveDown(index)" :disabled="index === files.length - 1"
                class="bg-gray-300 px-2 py-1 rounded disabled:opacity-50">
                ⬇️
              </button>
              <button @click="removeFile(index)"
                class="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600 transition">
                ลบ
              </button>
            </div>
          </li>
        </ul>

        <!-- Action Buttons -->
        <div class="flex flex-col sm:flex-row sm:justify-end gap-4 max-w-[500px]">
          <button type="button"
            class="itbms-cancel-button order-2 sm:order-1 flex items-center justify-center px-6 py-3 border border-blue-300 text-blue-700 font-medium rounded-lg shadow-sm hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
            @click="router.go(-1)">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
            Cancel
          </button>

          <button type="submit"
            class="itbms-save-button order-1 sm:order-2 flex items-center justify-center px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-500 text-white font-medium rounded-lg shadow-sm hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!isSaving || !isFormValid || (prop.mode === 'Edit' && !isProductChanged)" @click="saveProduct">
            Save
          </button>
        </div>
      </div>


    </div>



  </div>
</template>

<style scoped></style>

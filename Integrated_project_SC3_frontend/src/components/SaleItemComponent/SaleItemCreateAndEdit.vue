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
  addSaleItemV2,
  getSaleItemByIdV2,
  updateSaleItem,
  getImageByImageName
} from "@/libs/callAPI/apiSaleItem.js";
import BrandDropdown from "./../BrandComponents/BrandDropdown.vue";
import { useRoute, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand";
import { ChevronLeft,
  ChevronRight } from 'lucide-vue-next'; 


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

// โครงสร้างใหม่ของ saleItem ตามที่ต้องการ
const saleItem = reactive({
  id: null,
  model: "",
  brand: { id: null },
  description: "",
  price: null,
  ramGb: null,
  screenSizeInch: null,
  quantity: 1,
  storageGb: null,
  color: "",
  saleItemImage: [],
});


const originalSaleItem = reactive({});

// ตัวแปรสำหรับการจัดการไฟล์
const files = ref([]);
const currentIndex = ref(0);

onBeforeMount(async () => {
  if (prop.mode === "Edit") {
    try {
      const data = await getSaleItemByIdV2(prop.productId);
      if (!data) {
        saleItem.model = "404_not_found";
        setTimeout(() => router.push("/sale-items"), 2000);
        return;
      }
      
      // อัปเดตข้อมูลตามโครงสร้างใหม่
      saleItem.id = data.id;
      saleItem.model = data.model;
      saleItem.description = data.description;
      saleItem.price = data.price;
      saleItem.ramGb = data.ramGb;
      saleItem.screenSizeInch = data.screenSizeInch;
      saleItem.quantity = data.quantity;
      saleItem.storageGb = data.storageGb;
      saleItem.color = data.color;
      
      // ตั้งค่า brand
      if (data.brandName) {
        await getBrandIdByName(data.brandName);
      }
      
      // ✅ แก้ไข: ย้ายการ push รูปมาไว้หลัง fetch ข้อมูล
      if (data.saleItemImage && Array.isArray(data.saleItemImage)) {
        saleItem.saleItemImage = [...data.saleItemImage];
        fileImageFirstResponse.length = 0; // clear array ก่อน
        fileImageFirstResponse.push(...data.saleItemImage);
        console.log("saleItem.saleItemImage", saleItem.saleItemImage);
        await organizeAndFetchImages();
      }

      Object.assign(originalSaleItem, JSON.parse(JSON.stringify(saleItem)));
    } catch (error) {
      console.error("Error loading product:", error);
    }
  }
});

const getBrandIdByName = async (brandName) => {
  const data = await getAllBrand();
  const brand = data.find((b) => b.name === brandName);
  if (brand) {
    saleItem.brand.id = brand.id;
    saleItem.brandName = brand.name; // เก็บไว้สำหรับ backward compatibility
  } else {
    console.error("Brand not found");
  }
};

// อัปเดตฟังก์ชันจัดการ brand
const handleBrandId = (id) => {
  saleItem.brand.id = id;
  // เก็บไว้สำหรับ backward compatibility
  // saleItem.brandId = id; 
};

const handleBrandName = (name) => {
  saleItem.brandName = name; // เก็บไว้สำหรับ backward compatibility
};

const trimField = (field) => {
  if (typeof saleItem[field] === "string")
    saleItem[field] = saleItem[field].trim();
};

// ตรวจสอบว่า saleItem ถูกเปลี่ยนแปลงหรือไม่ (ใช้สำหรับ Edit mode)
const compareSaleItem = (a, b) => {
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

  return keysA.every((key) => compareSaleItem(a[key], b[key]));
};

const isSaleItemChanged = computed(
  () => !compareSaleItem(saleItem, originalSaleItem)
);

const maxLength = {
  model: 60,
  description: 65535,
  color: 40,
};

watch(
  saleItem,
  () => {
    validationSaleItemForm();
  },
  { deep: true }
);

const checkDecimal = (num) => {
  return !(Math.floor(num * 100) === num * 100);
};

const validationSaleItemForm = () => {
  let isValid = true;
  
  // Model validation
  if (!saleItem.model || (saleItem.model?.length ?? 0) > maxLength.model) {
    boxTextTailwindModel.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindModel.value = boxTextTailwind;
  }

  // Price validation
  if (saleItem.price < 0) {
    boxTextTailwindPrice.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindPrice.value = boxTextTailwind;
  }

  // RAM validation
  if (typeof saleItem.ramGb === "number" && saleItem.ramGb <= 0) {
    boxTextTailwindRamGB.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindRamGB.value = boxTextTailwind;
  }

  // Storage validation
  if (typeof saleItem.storageGb === "number" && saleItem.storageGb <= 0) {
    boxTextTailwindStorageGB.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindStorageGB.value = boxTextTailwind;
  }

  // Description validation
  if (
    !saleItem.description ||
    saleItem.description.length > maxLength.description
  ) {
    boxTextTailwindDesc.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindDesc.value = boxTextTailwind;
  }

  // Quantity validation
  if (saleItem.quantity === null || saleItem.quantity < 0) {
    boxTextTailwindQuantity.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindQuantity.value = boxTextTailwind;
  }

  // Color validation
  if ((saleItem.color?.length ?? 0) > maxLength.color) {
    boxTextTailwindColor.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindColor.value = boxTextTailwind;
  }

  // Screen Size validation
  if (
    (typeof saleItem.screenSizeInch === "number" &&
      saleItem.screenSizeInch <= 0) ||
    checkDecimal(saleItem.screenSizeInch)
  ) {
    boxTextTailwindScreenSizeInch.value = boxTextTailwindError;
    isValid = false;
  } else {
    boxTextTailwindScreenSizeInch.value = boxTextTailwind;
  }

  // Brand validation - ใช้ saleItem.brand.id แทน
  if (!saleItem.brand.id || !saleItem.brandName) {
    brandError.value = true;
    isValid = false;
  } else {
    brandError.value = false;
  }

  isSaving.value = isValid;
};

const isFormValid = computed(() => {
  return (
    saleItem.brand.id !== null &&
    saleItem.brandName?.trim() !== "" &&
    saleItem.model.trim() !== "" &&
    saleItem.price >= 0 &&
    saleItem.quantity >= 0 &&
    saleItem.description.trim() !== ""
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
const saveSaleItem = async () => {
  isSaving.value = false;

  // Reset styles
  boxTextTailwindModel.value = boxTextTailwind;
  boxTextTailwindPrice.value = boxTextTailwind;
  boxTextTailwindQuantity.value = boxTextTailwind;
  boxTextTailwindDesc.value = boxTextTailwind;
  brandError.value = false;

  // Validate fields
  if (!saleItem.brand.id || !saleItem.brandName) brandError.value = true;
  if (!saleItem.model) boxTextTailwindModel.value = boxTextTailwindError;
  if (!saleItem.price || saleItem.price < 0)
    boxTextTailwindPrice.value = boxTextTailwindError;
  if (!saleItem.quantity || saleItem.quantity < 0)
    boxTextTailwindQuantity.value = boxTextTailwindError;
  if (!saleItem.description) boxTextTailwindDesc.value = boxTextTailwindError;
  
  if (!isFormValid.value) {
    isSaving.value = true;
    return;
  }

  // ========================================== สร้าง FormData ========================================== 
  const formData = new FormData();
  
  // สำเนาข้อมูลและทำ normalize
  const saleItemCopy = JSON.parse(JSON.stringify(saleItem));
  normalizeEmptyStringsToNull(saleItemCopy);

  // เพิ่มข้อมูลลงใน FormData
  for (const field in saleItemCopy) {
    if (field === 'brand' && saleItemCopy[field]?.id) {
      formData.append('brand.id', saleItemCopy[field].id);
    } else if (field === 'saleItemImage') {
      // ข้าม saleItemImage ในการ loop นี้
      continue;
    } else if (saleItemCopy[field] !== null && saleItemCopy[field] !== undefined) {
      formData.append(field, saleItemCopy[field]);
    }
  }

  // ✅ แก้ไข: เพิ่มไฟล์รูปภาพลงใน FormData
  console.log("fileImageOrganize before sending:", fileImageOrganize.value);
  console.log("files array before sending:", files.value);
  
  // วิธีที่ง่ายกว่า: ส่งไฟล์ใหม่ทั้งหมด
  let newFileIndex = 0;
  fileImageOrganize.value.forEach((item, index) => {
    if (item.fileName === null) {
      // หาไฟล์ใหม่ที่ตรงกับ index นี้
      const actualFileIndex = fileImageOrganize.value
        .slice(0, index + 1)
        .filter(img => img.fileName === null).length - 1;
      
      if (files.value[actualFileIndex]) {
        console.log(`Adding new file at index ${actualFileIndex}:`, files.value[actualFileIndex].name);
        formData.append(`images`, files.value[actualFileIndex]);
        formData.append(`imageSequence[${newFileIndex}]`, item.imageViewOrder);
        newFileIndex++;
      }
    } else if (item.fileName !== null) {
      // สำหรับไฟล์เดิมที่ต้องการเก็บตำแหน่ง
      console.log(`Keeping existing file: ${item.fileName} at order: ${item.imageViewOrder}`);
      formData.append(`existingImageOrder[${item.fileName}]`, item.imageViewOrder);
    }
  });

  // Debug: แสดงข้อมูลใน FormData
  console.log("FormData entries:");
  for (let [key, value] of formData.entries()) {
    console.log(key, value);
  }

  try {
    if (saleItem.id) {
      await updateSaleItem(saleItem.id, formData);
      alertStore.setMessage("The sale item has been updated.");
      router.go(-1);
    } else if (prop.mode === "Edit") {
      await updateSaleItem(saleItem.id, formData);
      alertStore.setMessage("The sale item has been updated.");
      router.go(-1);
    } else {
      // ✅ Create mode - ตรวจสอบว่ามีรูปหรือไม่
      console.log("Creating new sale item with", newFileIndex, "images");
      await addSaleItemV2(formData);
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

//----------------------------------------------File Management-------------------------------------------------------------------------------------
const fileImageOrganize = ref([]);
// File size limits ตาม backend spec
const FILE_SIZE_LIMITS = {
  MAX_FILE_SIZE: 2 * 1024 * 1024, // 2MB
  MAX_REQUEST_SIZE: 5 * 1024 * 1024, // 5MB
};

// ฟังก์ชันแปลงขนาดไฟล์เป็น string ที่อ่านง่าย
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// เมื่อเลือกไฟล์ - รีโลจิกใหม่เพื่อใช้กับ fileImageOrganize.value (แบบเรียบง่าย)
// ✅ แก้ไข: เพิ่ม debug ใน handleFileChange
const handleFileChange = (event) => {
  const selectedFiles = Array.from(event.target.files);
  const MAX_IMAGES = 4;
  const MAX_FILENAME_LENGTH = 50;
  

  console.log("Selected files:", selectedFiles.map(f => f.name));
  console.log("Selected files all:", selectedFiles);
  
  // ตรวจสอบความยาวของชื่อไฟล์
  const longFilenames = selectedFiles.filter(file => file.name.length > MAX_FILENAME_LENGTH);
  if (longFilenames.length > 0) {
    const fileList = longFilenames.map(file => `"${file.name}" (${file.name.length} ตัวอักษร)`).join('\n');
    alert(`ชื่อไฟล์ต่อไปนี้ยาวเกิน ${MAX_FILENAME_LENGTH} ตัวอักษร:\n${fileList}\n\nกรุณาเปลี่ยนชื่อไฟล์ให้สั้นลงก่อนอัปโหลด`);
    event.target.value = "";
    return;
  }
  
  // ตรวจสอบจำนวนรูปทั้งหมด
  const currentImageCount = fileImageOrganize.value.length;
  const totalAfterUpload = currentImageCount + selectedFiles.length;
  
  let filesToProcess = selectedFiles;
  let warningMessage = "";
  
  if (totalAfterUpload > MAX_IMAGES) {
    const remainingSlots = MAX_IMAGES - currentImageCount;
    
    if (remainingSlots <= 0) {
      alert(`คุณสามารถอัปโหลดได้สูงสุด ${MAX_IMAGES} รูปเท่านั้น\nปัจจุบันมีรูปครบ ${MAX_IMAGES} รูปแล้ว`);
      event.target.value = "";
      return;
    }
    
    filesToProcess = selectedFiles.slice(0, remainingSlots);
    warningMessage = `คุณเลือกรูป ${selectedFiles.length} รูป แต่สามารถอัปโหลดได้อีกเพียง ${remainingSlots} รูป\nจึงจะอัปโหลดเฉพาะ ${remainingSlots} รูปแรกเท่านั้น`;
  }
  
  // ตรวจสอบขนาดไฟล์
  const validation = validateFileSize(filesToProcess);
  
  if (!validation.isValid) {
    alert(validation.errors.join('\n'));
    event.target.value = "";
    return;
  }
  
  if (warningMessage) {
    alert(warningMessage);
  }

  console.log("filesToProcess: ",filesToProcess)
  
  // เพิ่มไฟล์ใหม่ลงใน fileImageOrganize และ files array
  const filesProcessed = [];
  filesToProcess.forEach((file, index) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const imageUrl = e.target.result;
      const nextOrder = fileImageOrganize.value.length;
      
      fileImageOrganize.value.push({
        fileName: null, // ไฟล์ใหม่
        orgFileName: file.name,
        imageUrl: imageUrl,
        imageViewOrder: nextOrder
      });
      
      console.log("Added new image to fileImageOrganize:", {
        fileName: null,
        orgFileName: file.name,
        imageViewOrder: nextOrder
      });
    };
    reader.readAsDataURL(file);
    filesProcessed.push(file);
  });
  
  // เพิ่มไฟล์ลงใน files array
  files.value.push(...filesProcessed);
  
  console.log("Updated files array:", files.value);
  console.log("Updated filesProcessed array:", filesProcessed);
  console.log("Updated filesName array:", files.value.map(f => f.name));
  console.log("Updated fileImageOrganize:", fileImageOrganize.value);
  
  event.target.value = "";
};

const validateFileSize = (selectedFiles) => {
  const errors = [];
  let totalSize = 0;
  
  // คำนวณขนาดรวมของไฟล์เดิมที่เป็นไฟล์ใหม่ (fileName === null)
  fileImageOrganize.value.forEach((item, index) => {
    if (item.fileName === null && files.value[index]) {
      totalSize += files.value[index].size;
    }
  });
  
  // ตรวจสอบขนาดของไฟล์ใหม่แต่ละไฟล์
  selectedFiles.forEach((file, index) => {
    if (file.size > FILE_SIZE_LIMITS.MAX_FILE_SIZE) {
      errors.push(`File "${file.name}" (${formatFileSize(file.size)}) exceeds maximum file size of ${formatFileSize(FILE_SIZE_LIMITS.MAX_FILE_SIZE)}`);
    }
    totalSize += file.size;
  });
  
  // ตรวจสอบขนาดรวมทั้งหมด
  if (totalSize > FILE_SIZE_LIMITS.MAX_REQUEST_SIZE) {
    errors.push(`Total file size (${formatFileSize(totalSize)}) exceeds maximum request size of ${formatFileSize(FILE_SIZE_LIMITS.MAX_REQUEST_SIZE)}`);
  }
  
  return {
    isValid: errors.length === 0,
    errors: errors
  };
};
const removeFile = (index) => {
  // ลบจาก fileImageOrganize
  fileImageOrganize.value.splice(index, 1);
  
  // ลบจาก files array
  files.value.splice(index, 1);
  
  // อัปเดต imageViewOrder ใหม่
  fileImageOrganize.value.forEach((item, idx) => {
    item.imageViewOrder = idx;
  });
  
  // ปรับ currentIndex ถ้าจำเป็น
  if (currentIndex.value >= fileImageOrganize.value.length && fileImageOrganize.value.length > 0) {
    currentIndex.value = fileImageOrganize.value.length - 1;
  } else if (fileImageOrganize.value.length === 0) {
    currentIndex.value = 0;
  }
};

// ฟังก์ชันเลื่อนขึ้น
const moveUp = (index) => {
  if (index > 0) {
    // สลับตำแหน่งใน fileImageOrganize array
    const temp = fileImageOrganize.value[index];
    fileImageOrganize.value[index] = fileImageOrganize.value[index - 1];
    fileImageOrganize.value[index - 1] = temp;
    
    // อัปเดต imageViewOrder
    fileImageOrganize.value[index].imageViewOrder = index;
    fileImageOrganize.value[index - 1].imageViewOrder = index - 1;
    
    // สลับใน files array
    const tempFile = files.value[index];
    files.value[index] = files.value[index - 1];
    files.value[index - 1] = tempFile;
  }
};

// ฟังก์ชันเลื่อนลง
const moveDown = (index) => {
  if (index < fileImageOrganize.value.length - 1) {
    // สลับตำแหน่งใน fileImageOrganize array
    const temp = fileImageOrganize.value[index];
    fileImageOrganize.value[index] = fileImageOrganize.value[index + 1];
    fileImageOrganize.value[index + 1] = temp;
    
    // อัปเดต imageViewOrder
    fileImageOrganize.value[index].imageViewOrder = index;
    fileImageOrganize.value[index + 1].imageViewOrder = index + 1;
    
    // สลับใน files array
    const tempFile = files.value[index];
    files.value[index] = files.value[index + 1];
    files.value[index + 1] = tempFile;
  }
};

// ฟังก์ชันสำหรับการนำทางรูปภาพ
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  } else {
    currentIndex.value = fileImageOrganize.value.length - 1;
  }
};

const nextImage = () => {
  if (currentIndex.value < fileImageOrganize.value.length - 1) {
    currentIndex.value++;
  } else {
    currentIndex.value = 0;
  }
};


//===================================File image called=============================


const fileImageFirstResponse = [];

// Method สำหรับจัดการข้อมูล
const organizeAndFetchImages = async () => {
  try {
    // 1. จัดเรียงข้อมูลตาม imageViewOrder
    // ใช้ .sort() เพื่อจัดเรียง array
    const fileImageSorted = [...fileImageFirstResponse].sort(
      (a, b) => a.imageViewOrder - b.imageViewOrder
    );

    // 2. Loop เพื่อ fetch API และจัดเก็บข้อมูล
    for (const item of fileImageSorted) {
      // เรียกใช้ฟังก์ชัน getImageByImageName() เพื่อดึงรูป
      const imageUrl = await getImageByImageName(item.fileName);

      // 3. push ข้อมูลที่ได้ลงใน fileImageOrganize
      fileImageOrganize.value.push({
        fileName: item.fileName,
        orgFileName: item.originalFileName,
        imageUrl: imageUrl, // เก็บ url ของรูปที่ได้จากการ fetch
        imageViewOrder: item.imageViewOrder,
      });
    }
    console.log("After sorted Sale Item:",fileImageOrganize.value);
    console.log("After sorted Sale Item:",saleItem.saleItemImage.length);

  } catch (error) {
    console.error("Error organizing and fetching saleItemImage:", error);
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

    <div class=" m-5 ml-[120px] flex flex-row justify-center items-center ">
      <!-- Image Preview Section - ใช้ saleItem.saleItemImage แทน saleItemImage -->
      <div class="grid grid-rows-[auto_auto] gap-4 p-4 flex-1/2 relative ">
        
        <!-- แสดงเฉพาะเมื่อมีรูป -->
        <div v-if="fileImageOrganize.length > 0">
          <!-- ปุ่มซ้าย -->
          <button @click="prevImage" v-show="fileImageOrganize.length > 1"
                  class="absolute left-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
            <ChevronLeft/>
          </button>
          

          <!-- ช่องใหญ่ด้านบน แสดงรูป -->
          <div class="bg-gray-100 rounded border border-blue-400 h-96 flex items-center justify-center">
            <img :src="fileImageOrganize[currentIndex].imageUrl" alt="main image" class="max-h-full max-w-full object-contain" />
          </div>

          <!-- ปุ่มขวา -->
          <button @click="nextImage" v-show="fileImageOrganize.length > 1"
                  class="absolute right-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
            <ChevronRight/>
          </button>

          <!-- ช่องเล็ก 4 ช่องด้านล่าง -->
          <div class="grid grid-cols-4 gap-4 mt-3" v-if="fileImageOrganize.length > 1">
            <div v-for="(idx) in 4 " :key="idx"
                 :class="['rounded border', (idx-1) === currentIndex ? 'border-blue-600' : 'border-blue-400', 'h-32 cursor-pointer']">
                <span v-if="fileImageOrganize.length > (idx-1)" @click="currentIndex = (idx-1)">
                  <img :src="fileImageOrganize[idx-1].imageUrl" alt="thumbnail" class="w-full h-full object-cover rounded" />
                </span>
                <span v-else class="w-full h-full flex items-center justify-center text-gray-500">
                  No Image
                </span>
            </div>
          </div>
        </div>

        <!-- แสดงเมื่อไม่มีรูป -->
        <div v-else class="bg-gray-100 rounded border-2 border-dashed border-gray-300 h-96 flex items-center justify-center">
          <div class="text-center text-gray-500">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
            <p class="text-lg font-medium">No saleItemImage uploaded</p>
            <p class="text-sm">Upload saleItemImage to preview them here</p>
          </div>
        </div>
         <!-- File Upload -->
        <div class="mb-6 max-w-[500px]">
          <label for="file-upload"
            class="cursor-pointer inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition">
            <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#ffffff">
              <path
                d="M440-320v-326L336-542l-56-58 200-200 200 200-56 58-104-104v326h-80ZM240-160q-33 0-56.5-23.5T160-240v-120h80v120h480v-120h80v120q0 33-23.5 56.5T720-160H240Z" />
            </svg>
            Upload saleItemImage
          </label>
          <input id="file-upload" type="file" class="hidden" multiple accept="image/*" @change="handleFileChange" />
          <div class="mt-2 text-sm text-gray-500">
            <p>Maximum file size: 2MB per image</p>
            <p>Maximum total size: 5MB for all saleItemImage</p>
          </div>
        </div>

        <!-- File List -->
        <ul v-if="fileImageOrganize.length > 0" class="space-y-2 mb-6">
          <li v-for="(file, index) in fileImageOrganize" :key="index"
            class="flex items-center justify-between bg-gray-100 p-2 rounded w-125">
            <!-- ชื่อไฟล์ -->
            <span class="truncate max-w-[200px]">{{ file.orgFileName }}</span>

            <!-- ปุ่ม action -->
            <div class="flex gap-2">
              <button @click="moveUp(index)" :disabled="index === 0"
                class="bg-gray-300 px-2 py-1 rounded disabled:opacity-50 hover:bg-gray-400 transition">
                ⬆️
              </button>
              <button @click="moveDown(index)" :disabled="index === fileImageOrganize.length - 1"
                class="bg-gray-300 px-2 py-1 rounded disabled:opacity-50 hover:bg-gray-400 transition">
                ⬇️
              </button>
              <button @click="removeFile(index)"
                class="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600 transition">
                ลบ
              </button>
            </div>
          </li>
        </ul>
      </div>

      <!-- ====================================================End of Image Uploader==================================================== -->

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
              <BrandDropdown :brandError="brandError" :brandName="saleItem.brandName" :reloadData="reloadData"
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
            <input type="text" v-model="saleItem.model" @blur="trimField('model')"
              :class="`itbms-model ${boxTextTailwindModel} w-full`" placeholder="e.g. iPhone 13 Pro" />
            <p v-show="saleItem.model.length > maxLength.model" class="itbms-message mt-1 text-sm text-red-500">
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
            <input type="number" v-model.number="saleItem.price"
              :class="`itbms-price pl-8 ${boxTextTailwindPrice} w-full`" placeholder="e.g. 29900" />
          </div>
          <p v-show="saleItem.price < 0" class="itbms-message mt-1 text-sm text-red-500 ml-24">
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
            <input type="number" v-model.number="saleItem.ramGb" :class="`itbms-ramGb ${boxTextTailwindRamGB} w-full`"
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
            <input type="number" v-model.number="saleItem.storageGb"
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
            <input type="number" v-model.number="saleItem.screenSizeInch" step="0.1"
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
            <input type="text" v-model="saleItem.color" @blur="trimField('color')"
              :class="`itbms-color ${boxTextTailwindColor} w-full`" placeholder="e.g. Midnight Blue" />
            <p v-show="(saleItem.color?.length ?? 0) > maxLength.color" class="itbms-message mt-1 text-sm text-red-500">
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
            <input type="number" v-model.number="saleItem.quantity" @focus="saleItem.quantity = null"
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
          <textarea v-model="saleItem.description" rows="4" @blur="trimField('description')"
            :class="`itbms-description ${boxTextTailwindDesc} w-full`"
            placeholder="Enter product description..."></textarea>
          <p v-show="saleItem.description.length > maxLength.description"
            class="itbms-message mt-1 text-sm text-red-500">
            Description must be 1-65,535 characters long.
          </p>
        </div>

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
            class="itbms-save-button order-1 sm:order-2 flex items-center  justify-center px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-500 text-white font-medium rounded-lg shadow-sm hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!isSaving || !isFormValid || (prop.mode === 'Edit' && !isSaleItemChanged)" @click="saveSaleItem">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
              <polyline points="17,21 17,13 7,13 7,21"></polyline>
              <polyline points="7,3 7,8 15,8"></polyline>
            </svg>
            Save
          </button>
        </div>
        <br>
        <br>
      </div>
    </div>
  </div>
</template>
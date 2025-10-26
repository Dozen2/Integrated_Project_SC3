<script setup>
import { ref, reactive, defineProps, onBeforeMount, computed, watch } from "vue";
import { addSaleItemV2, getSaleItemByIdV2, updateSaleItem, updateSaleItemV2, getImageByImageName, createSaleItem, updateSaleItemSeller } from "@/libs/callAPI/apiSaleItem.js";
import BrandDropdown from "./../BrandComponents/BrandDropdown.vue";
import { useRoute, useRouter } from "vue-router";
import { useAlertStore } from "@/stores/alertStore.js";
import { getAllBrand } from "@/libs/callAPI/apiBrand";
import { ChevronLeft, ChevronRight, Upload, X } from "lucide-vue-next";
import { ChevronDown, ChevronUp } from "lucide-vue-next";
import InputBox from "../Common/InputBox.vue";

const boxTextTailwind = "w-[600px] p-3 border border-blue-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white shadow-sm transition-all duration-200";
const boxTextTailwindError = "w-[600px] p-3 border-2 border-red-400 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none bg-white shadow-sm transition-all duration-200";

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

      // ย้ายการ push รูปมาไว้หลัง fetch ข้อมูล
      if (data.saleItemImage && Array.isArray(data.saleItemImage)) {
        saleItem.saleItemImage = [...data.saleItemImage];
        fileImageFirstResponse.length = 0; // clear array ก่อน
    fileImageFirstResponse.push(...data.saleItemImage);
  // console.log("saleItem.saleItemImage", saleItem.saleItemImage);
        await organizeAndFetchImages();
      }

      Object.assign(originalSaleItem, JSON.parse(JSON.stringify(saleItem)));

  // console.log("Original Sale Item:", originalSaleItem);
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
};

const handleBrandName = (name) => {
  saleItem.brandName = name; // เก็บไว้สำหรับ backward compatibility
};

const trimField = (field) => {
  if (typeof saleItem[field] === "string") saleItem[field] = saleItem[field].trim();
};

// ตรวจสอบว่า saleItem ถูกเปลี่ยนแปลงหรือไม่ (ใช้สำหรับ Edit mode)
const compareSaleItem = (a, b) => {
  if (a === b) return true;
  if (typeof a !== "object" || typeof b !== "object" || a === null || b === null) return false;
  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  if (keysA.length !== keysB.length) return false;

  return keysA.every((key) => compareSaleItem(a[key], b[key]));
};

const isSaleItemChanged = computed(() => !compareSaleItem(saleItem, originalSaleItem));

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
  if (!saleItem.description || saleItem.description.length > maxLength.description) {
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
  if ((typeof saleItem.screenSizeInch === "number" && saleItem.screenSizeInch <= 0) || checkDecimal(saleItem.screenSizeInch)) {
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
  return saleItem.brand.id !== null && saleItem.brandName?.trim() !== "" && saleItem.model.trim() !== "" && saleItem.price >= 0 && saleItem.quantity >= 0 && saleItem.description.trim() !== "";
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

const setSessionStorage = () => {
  const raw = sessionStorage.getItem("product-page-settings");

  if (raw) {
    const settings = JSON.parse(raw);
    settings.page = 0;
    sessionStorage.setItem("product-page-settings", JSON.stringify(settings));
  // console.log("✔️ page updated to 0 and saved back:", settings);
  } else {
  // console.log("⚠️ No settings found in sessionStorage.");
  }
};

//----------------------------------------------File Management-------------------------------------------------------------------------------------
const fileImageOrganize = ref([]);
const FILE_SIZE_LIMITS = {
  MAX_FILE_SIZE: 2 * 1024 * 1024, // 2MB
  MAX_REQUEST_SIZE: 5 * 1024 * 1024, // 5MB
};

const formatFileSize = (bytes) => {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

const handleFileChange = async (event) => {
  const selectedFiles = Array.from(event.target.files);
  const MAX_IMAGES = 4;
  const MAX_FILENAME_LENGTH = 50;

  // console.log(
  //   "Selected files:",
  //   selectedFiles.map((f) => f.name)
  // );
  // console.log("Selected files all:", selectedFiles);

  // ตรวจสอบความยาวของชื่อไฟล์
  const longFilenames = selectedFiles.filter((file) => file.name.length > MAX_FILENAME_LENGTH);
  if (longFilenames.length > 0) {
    const fileList = longFilenames.map((file) => `"${file.name}" (${file.name.length} ตัวอักษร)`).join("\n");
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
      alertStore.addToast(`You've reached the upload limit of ${MAX_IMAGES} images.`, "Image upload limit exceeded.", "warning", 8000);

      event.target.value = "";
      return;
    }

    filesToProcess = selectedFiles.slice(0, remainingSlots);
    warningMessage = `You can only upload ${remainingSlots} more images. Your selection of ${selectedFiles.length} will be trimmed.`;
  }

  // ตรวจสอบขนาดไฟล์
  const validation = validateFileSize(filesToProcess);

  if (!validation.isValid) {
    alertStore.addToast(validation.errors.join("\n"), "Image upload limit exceeded.", "warning", 8000);
  }

  filesToProcess = validation.validFiles;

  if (warningMessage) {
    alertStore.addToast(warningMessage, "Image upload limit exceeded.", "warning", 8000);
  }

  // console.log("filesToProcess: ", filesToProcess);

  const filesProcessed = [];
  for (const [index, file] of filesToProcess.entries()) {
    const reader = new FileReader();

    const imageUrl = await new Promise((resolve) => {
      reader.onload = (e) => resolve(e.target.result);
      reader.readAsDataURL(file);
    });

    const nextOrder = fileImageOrganize.value.length;

  // console.log("filesToProcess: ", filesToProcess[index].name);
  // console.log("filesToProcess: ", filesToProcess);

    fileImageOrganize.value.push({
      fileName: null, // ไฟล์ใหม่
      orgFileName: file.name,
      imageUrl: imageUrl,
      imageViewOrder: nextOrder + 1,
    });

    saleItem.saleItemImage.push({
      imageFile: file,
      imageUrl: imageUrl,
      orgFileName: file.name,
      imageViewOrder: nextOrder + 1,
    });

    filesProcessed.push(file);
  }

  // console.log("fileImageOrganize.value:", fileImageOrganize.value);

  // เพิ่มไฟล์ลงใน files array
  files.value.push(...filesProcessed);
  event.target.value = "";
};

const validateFileSize = (selectedFiles) => {
  // console.log("Validating file sizes...", selectedFiles);

  const errors = [];
  let totalSize = 0;

  // คำนวณขนาดรวมของไฟล์เดิมที่เป็นไฟล์ใหม่ (fileName === null)
  fileImageOrganize.value.forEach((item, index) => {
    if (item.fileName === null && files.value[index]) {
      totalSize += files.value[index].size;
    }
  });

  // ตรวจสอบไฟล์ใหม่ทีละไฟล์ และกรองไฟล์ที่ไม่เกินออกมา
  const validFiles = selectedFiles.filter((file) => {
    if (file.size > FILE_SIZE_LIMITS.MAX_FILE_SIZE) {
      errors.push(`File ${file.name} (${formatFileSize(file.size)}) exceeds the maximum size of ${formatFileSize(FILE_SIZE_LIMITS.MAX_FILE_SIZE)} and will be removed.`);
      return false; // ตัดไฟล์นี้ออก
    }
    totalSize += file.size;
    return true; // เก็บไฟล์นี้ไว้
  });

  // ตรวจสอบขนาดรวมทั้งหมด
  if (totalSize > FILE_SIZE_LIMITS.MAX_REQUEST_SIZE) {
    errors.push(`The total file size (${formatFileSize(totalSize)}) exceeds the maximum allowed size of ${formatFileSize(FILE_SIZE_LIMITS.MAX_REQUEST_SIZE)}.`);
  }

  return {
    isValid: errors.length === 0,
    errors: errors,
    validFiles: validFiles, // เอาไว้ใช้อัปเดต selectedFiles ต่อ
  };
};

const removeFile = (index) => {
  //push name deleted to deletedImage
  if (fileImageOrganize.value[index].fileName) {
    deletedImage.value.push(fileImageOrganize.value[index].fileName);

    let findIndexSaleItemImage = saleItem.saleItemImage.findIndex((item) => item.fileName === fileImageOrganize.value[index].fileName);
  // console.log("findIndexSaleItemImage:", findIndexSaleItemImage);
    if (findIndexSaleItemImage !== -1) {
      saleItem.saleItemImage.splice(findIndexSaleItemImage, 1);
    }
  } else {
    // ถ้าเป็นไฟล์ใหม่ที่ยังไม่มี fileName ให้ลบจาก saleItem.saleItemImage โดยใช้ orgFileName แทน
    let findIndexSaleItemImage = saleItem.saleItemImage.findIndex((item) => item.orgFileName === fileImageOrganize.value[index].orgFileName);
  // console.log("findIndexSaleItemImage:", findIndexSaleItemImage);

    if (findIndexSaleItemImage !== -1) {
      saleItem.saleItemImage.splice(findIndexSaleItemImage, 1);
    }
  }

  // console.log("fileImageOrganize.value[index].fileName: " + fileImageOrganize.value[index].fileName);
  // console.log("index: " + index);

  // console.log("deletedImage:", deletedImage.value);
  fileImageOrganize.value.splice(index, 1);
  files.value.splice(index, 1);

  // อัปเดต imageViewOrder ใหม่
  fileImageOrganize.value.forEach((item, idx) => {
    item.imageViewOrder = idx;
  });

  // อัปเดต
  saleItem.saleItemImage.forEach((img) => {
    img.imageViewOrder = img.imageViewOrder - 1; // เริ่มนับจาก 1
  });

  // console.log("After removal, fileImageOrganize:", fileImageOrganize.value);
  // console.log("After removal, saleItem.saleItemImage:", saleItem.saleItemImage);
  // console.log("After removal, files array:", files.value);
};

const deletedImage = ref([]);
//===================================Action image change=============================

const positionImageChange = () => {
  // อัปเดต imageViewOrder ใน saleItem.saleItemImage
  // imageFile != imageURL
  saleItem.saleItemImage.forEach((img) => {
    const found = fileImageOrganize.value.find((f) => f.fileName === img.fileName || f.imageUrl === img.imageUrl);
    if (found) {
      img.imageViewOrder = found.imageViewOrder;
    }
  });
};

// ฟังก์ชันเลื่อนขึ้น
const moveUp = (index) => {
  if (index > 0) {
    // สลับตำแหน่งใน fileImageOrganize array
    const temp = fileImageOrganize.value[index];
    fileImageOrganize.value[index] = fileImageOrganize.value[index - 1];
    fileImageOrganize.value[index - 1] = temp;

    // อัปเดต imageViewOrder
    fileImageOrganize.value[index].imageViewOrder = index + 1;
    fileImageOrganize.value[index - 1].imageViewOrder = index;

    // สลับใน files array
    const tempFile = files.value[index];
    files.value[index] = files.value[index - 1];
    files.value[index - 1] = tempFile;

    positionImageChange();

    //log saleItem.saleItemImage after move
  // console.log("After move up saleItem.saleItemImage:", saleItem.saleItemImage);
  // console.log("After move up fileImageOrganize:", fileImageOrganize.value);
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

    positionImageChange();
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
    const fileImageSorted = [...fileImageFirstResponse].sort((a, b) => a.imageViewOrder - b.imageViewOrder);

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
  // console.log("After sorted Sale Item:", fileImageOrganize.value);
  // console.log("After sorted Sale Item:", saleItem.saleItemImage.length);
  } catch (error) {
    console.error("Error organizing and fetching saleItemImage:", error);
  }
};

//===================================Save form data=============================

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
  if (!saleItem.price || saleItem.price < 0) boxTextTailwindPrice.value = boxTextTailwindError;
  if (!saleItem.quantity || saleItem.quantity < 0) boxTextTailwindQuantity.value = boxTextTailwindError;
  if (!saleItem.description) boxTextTailwindDesc.value = boxTextTailwindError;

  if (!isFormValid.value) {
    isSaving.value = true;
    return;
  }

  // ------------------ สร้าง FormData ------------------
  const formData = new FormData();

  // สำเนาข้อมูลและทำ normalize
  const saleItemCopy = JSON.parse(JSON.stringify(saleItem));
  normalizeEmptyStringsToNull(saleItemCopy);
  // ดึง imageFile จาก saleItem.saleItemImage มาใส่ใน saleItemCopy.saleItemImage
  if (Array.isArray(saleItem.saleItemImage)) {
    saleItemCopy.saleItemImage = saleItem.saleItemImage.map((img, idx) => {
      const copied = { ...img };
      if (img.imageFile) {
        copied.imageFile = img.imageFile; // copy File object ด้วย
      }
      return copied;
    });
  }
  // console.log("saleItemCopy:", saleItemCopy);
  // console.log("saleItem:", saleItem);

  let appendControlByMode = "";
  if (prop.mode === "Edit") {
    appendControlByMode = "saleItem.";
  }
  // console.log("appendControlByMode: ", appendControlByMode);

  // เพิ่มข้อมูลลงใน FormData
  for (const field in saleItemCopy) {
    if (field === "brand" && saleItemCopy[field]?.id) {
      formData.append(`${appendControlByMode}brand.id`, saleItemCopy[field].id);
    } else if (field === "saleItemImage") {
      //ส่ง saleItemImage.fileName กับ imageViewOrder
      saleItemCopy[field].forEach((image, index) => {
        if (image.fileName) {
          formData.append(`saleItemImages[${index}].fileName`, image.fileName);
          formData.append(`saleItemImages[${index}].imageViewOrder`, image.imageViewOrder);
        }
        if (image.imageFile) {
          formData.append(`saleItemImages[${index}].imageFile`, image.imageFile);
          // console.log(`Appending imageFile for index ${index}:`, image.imageFile);
          formData.append(`saleItemImages[${index}].imageViewOrder`, image.imageViewOrder);
        }
      });
    } else if (saleItemCopy[field] !== null && saleItemCopy[field] !== undefined) {
      formData.append(`${appendControlByMode}` + field, saleItemCopy[field]);
    }
  }

  // console.log("fileImageOrganize before sending:", fileImageOrganize.value);
  // console.log("files array before sending:", files.value);

  // if(prop.mode != 'Edit' ){
  fileImageOrganize.value.forEach((item, index) => {
    if (item.fileName === null) {
      // หาไฟล์ใหม่ที่ตรงกับ index นี้
      const actualFileIndex = fileImageOrganize.value.slice(0, index + 1).filter((img) => img.fileName === null).length - 1;
      if (files.value[actualFileIndex]) {
  formData.append(`images`, files.value[actualFileIndex]);
  // console.log(`Appending new file: ${files.value[actualFileIndex]}`);
      }
    }
  });

  //loop deletedImage and append to formData
  deletedImage.value.forEach((fileName) => {
    formData.append(`deletedImage[]`, fileName);
  });

  // Debug: แสดงข้อมูลใน FormData
  // console.log("FormData entries:");
  // for (let [key, value] of formData.entries()) {
  //   console.log(key, value);
  // }

  try {
    if (saleItem.id || prop.mode === "Edit") {
  await updateSaleItemSeller(saleItem.id, formData);
  // console.log("FormData after update:", formData);
      alertStore.addToast("The sale item has been updated.", "Update success", "success");
      router.go(-1);
    } else {
      // ✅ Create mode - ตรวจสอบว่ามีรูปหรือไม่
      await createSaleItem(formData);
      setSessionStorage();
      alertStore.addToast("The sale item has been successfully added.", "Add success", "success");
      router.go(-1);
    }
  } catch (err) {
    console.error("Something wrong: ", err.message);
    alert(err.message);
    router.push(`/sale-items`);
  } finally {
    isSaving.value = true;
    reloadData.value++;
  }
};
</script>

<template>
  <div class="bg-white min-h-screen w-auto pt-1">
    <Title :label="prop.mode === 'Edit' ? 'Edit Phone Details' : 'Add New Phone'" />

    <!-- Alert Message -->
    <div v-if="alertStore.message" :class="`itbms-message px-4 py-2 rounded mb-4 ${alertStore.type === 'error' ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700'}`" class="itbms-message">
      {{ alertStore.message }}
    </div>

    <div class="m-5 ml-[120px] flex flex-row justify-center">
      <!-- Image Preview Section - ใช้ saleItem.saleItemImage แทน saleItemImage -->
      <div class="grid grid-rows-[auto_auto] gap-4 p-4 flex-1/2 relative">
        <!-- แสดงเฉพาะเมื่อมีรูป -->
        <div v-if="fileImageOrganize.length > 0">
          <!-- ปุ่มซ้าย -->
          <button @click="prevImage" v-show="fileImageOrganize.length > 1" class="absolute left-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
            <ChevronLeft />
          </button>

          <!-- ช่องใหญ่ด้านบน แสดงรูป -->
          <div class="bg-gray-100 rounded border border-blue-400 h-96 flex items-center justify-center">
            <img :src="fileImageOrganize[currentIndex].imageUrl" alt="main image" class="max-h-full max-w-full object-contain" />
          </div>

          <!-- ปุ่มขวา -->
          <button @click="nextImage" v-show="fileImageOrganize.length > 1" class="absolute right-2 top-50 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200 z-10">
            <ChevronRight />
          </button>

          <!-- ช่องเล็ก 4 ช่องด้านล่าง -->
          <div class="grid grid-cols-4 gap-4 mt-3" v-if="fileImageOrganize.length > 1">
            <div v-for="idx in 4" :key="idx" :class="['rounded border', idx - 1 === currentIndex ? 'border-blue-600' : 'border-blue-400', 'h-32 cursor-pointer']">
              <span v-if="fileImageOrganize.length > idx - 1" @click="currentIndex = idx - 1">
                <img :src="fileImageOrganize[idx - 1].imageUrl" alt="thumbnail" class="w-full h-full object-cover rounded" />
              </span>
              <span v-else class="w-full h-full flex items-center justify-center text-gray-500"> No Image </span>
            </div>
          </div>
        </div>

        <!-- แสดงเมื่อไม่มีรูป -->
        <div v-else class="bg-gray-100 rounded border-2 border-dashed border-gray-300 h-96 flex items-center justify-center">
          <div class="text-center text-gray-500">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v14a2 2 0 002 2z"
              />
            </svg>
            <p class="text-lg font-medium">No saleItemImage uploaded</p>
            <p class="text-sm">Upload saleItemImage to preview them here</p>
          </div>
        </div>
        <!-- File Upload -->
        <div class="mb-6 max-w-[500px]">
          <label for="file-upload" class="flex cursor-pointer bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 w-[200px] transition justify-center">
            <Upload class="mr-[7px]" />
            <span class="font-semibold">Upload Image</span>
          </label>
          <input id="file-upload" type="file" class="hidden" multiple accept="image/*" @change="handleFileChange" />
          <div class="mt-2 text-sm text-gray-500">
            <p>* Maximum file size: 2MB per image</p>
            <p>* Maximum total size: 5MB for all saleItemImage</p>
          </div>
        </div>

        <!-- File List -->
        <ul v-if="fileImageOrganize.length > 0" class="space-y-2 mb-6">
          <li v-for="(file, index) in fileImageOrganize" :key="index" class="flex items-center justify-between bg-gray-100 p-2 rounded w-125">
            <!-- ชื่อไฟล์ -->
            <span class="truncate max-w-[200px]">{{ file.orgFileName }}</span>

            <!-- ปุ่ม action -->
            <div class="flex">
              <button @click="removeFile(index)" class="px-2 py-1 mr-2 text-red-500 hover:text-red-700 transition">
                <X />
              </button>
              <button @click="moveUp(index)" :disabled="index === 0" class="py-1 disabled:opacity-50 transition hover:text-blue-400">
                <ChevronUp />
              </button>
              <button @click="moveDown(index)" :disabled="index === fileImageOrganize.length - 1" class="pr-2 py-1 disabled:opacity-50 transition hover:text-blue-400">
                <ChevronDown />
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
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"></path>
                <line x1="7" y1="7" x2="7.01" y2="7"></line>
              </svg>
              Brand
              <span class="text-red-500 ml-1">*</span>
            </label>
            <div class="flex-1">
              <BrandDropdown :brandError="brandError" :brandName="saleItem.brandName" :reloadData="reloadData" @sendBrandId="handleBrandId" @sendBrandName="handleBrandName" />
              <p v-show="brandError" class="itbms-message mt-1 text-sm text-red-500">Brand must be selected.</p>
            </div>
          </div>
        </div>

        <!-- Model -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
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
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
            Model
            <span class="text-red-500 ml-1">*</span>
          </label>

          <InputBox v-model="saleItem.model" placeholder="e.g. iPhone 13 Pro" :inputClass="`${boxTextTailwindModel} itbms-model`" />
          <p v-show="saleItem.model.length > maxLength.model" class="itbms-message mt-1 text-sm text-red-500">Model must be 1-60 characters long.</p>
        </div>

        <!-- Price -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
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
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            Price (Baht)
            <span class="text-red-500 ml-1">*</span>
          </label>

          <InputBox v-model="saleItem.price" placeholder="e.g. 29900" :inputClass="`${boxTextTailwindPrice} itbms-price`" />
          <div class="flex-1">
            <p v-show="saleItem.price < 0" class="itbms-message mt-1 text-sm text-red-500 ml-24">Price must be non-negative integer.</p>
          </div>
        </div>

        <!-- RAM -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
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
              <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
              <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
            </svg>
            RAM (GB)
          </label>
          <InputBox type="number" v-model.number="saleItem.ramGb" placeholder="e.g. 8" :inputClass="`${boxTextTailwindRamGB} itbms-ramGb`" />
          <div class="flex-1">
            <p v-show="boxTextTailwindRamGB === boxTextTailwindError" class="itbms-message mt-1 text-sm text-red-500">RAM size must be positive integer or not specified.</p>
          </div>
        </div>

        <!-- Storage -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
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
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            </svg>
            Storage (GB)
          </label>
          <InputBox type="number" v-model.number="saleItem.storageGb" placeholder="e.g. 128" :inputClass="`${boxTextTailwindStorageGB} itbms-storageGb`" />
          <div class="flex-1">
            <p v-show="boxTextTailwindStorageGB === boxTextTailwindError" class="itbms-message mt-1 text-sm text-red-500">Storage size must be positive integer or not specified.</p>
          </div>
        </div>

        <!-- Screen Size -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 flex items-center">
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
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
            Screen Size (Inches)
          </label>
          <InputBox type="number" v-model.number="saleItem.screenSizeInch" placeholder="e.g. 6.1" :inputClass="`${boxTextTailwindScreenSizeInch} itbms-screenSizeInch`" />
          <div class="flex-1">
            <p v-show="boxTextTailwindScreenSizeInch === boxTextTailwindError" class="itbms-message mt-1 text-sm text-red-500">
              Screen size must be positive number with at most 2 decimal points or not specified.
            </p>
          </div>
        </div>

        <!-- Color -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
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
          <InputBox v-model="saleItem.color" placeholder="e.g. Midnight Blue" :inputClass="`${boxTextTailwindColor} itbms-color`" />
          <div class="flex-1">
            <p v-show="(saleItem.color?.length ?? 0) > maxLength.color" class="itbms-message mt-1 text-sm text-red-500">Color must be 1-40 characters long or not specified.</p>
          </div>
        </div>

        <!-- Quantity -->
        <div class="flex items-center mb-6 max-w-[600px]">
          <label class="font-medium text-gray-700 w-24 flex items-center">
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
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            Quantity
            <span class="text-red-500 ml-1">*</span>
          </label>
          <InputBox type="number" v-model="saleItem.quantity" placeholder="e.g. 10" :inputClass="`${boxTextTailwindQuantity} itbms-quantity`" />
          <div class="flex-1">
            <p v-show="boxTextTailwindQuantity === boxTextTailwindError" class="itbms-message mt-1 text-sm text-red-500">Quantity must be non-negative integer.</p>
          </div>
        </div>

        <!-- Description (Full Width) -->
        <div class="max-w-[600px] mb-6">
          <label class="block font-medium text-gray-700 mb-1 flex items-center">
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
            v-model="saleItem.description"
            rows="4"
            @blur="trimField('description')"
            :class="`itbms-description ${boxTextTailwindDesc} w-full`"
            placeholder="Enter product description..."
          ></textarea>
          <p v-show="saleItem.description.length > maxLength.description" class="itbms-message mt-1 text-sm text-red-500">Description must be 1-65,535 characters long.</p>
        </div>

        <!-- Action Buttons -->
        <div class="flex flex-col sm:flex-row sm:justify-end gap-4 max-w-[500px]">
          <button
            type="button"
            class="itbms-cancel-button order-2 sm:order-1 flex items-center justify-center px-6 py-3 border border-blue-300 text-blue-700 font-medium rounded-lg shadow-sm hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
            @click="router.go(-1)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
            Cancel
          </button>

          <button
            type="submit"
            class="itbms-save-button order-1 sm:order-2 flex items-center justify-center px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-500 text-white font-medium rounded-lg shadow-sm hover:from-blue-700 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!isSaving || !isFormValid || (prop.mode === 'Edit' && !isSaleItemChanged)"
            @click="saveSaleItem"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
              <polyline points="17,21 17,13 7,13 7,21"></polyline>
              <polyline points="7,3 7,8 15,8"></polyline>
            </svg>
            Save
          </button>
        </div>

        <br />
        <br />
      </div>
    </div>
  </div>
</template>

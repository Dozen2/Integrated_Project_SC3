<script setup>
import {
  getImageByImageName,
  getSaleItemByIdV2,
} from "@/libs/callAPI/apiSaleItem.js";
import { onBeforeMount, ref } from "vue";
import {
  ChevronLeft,
  ChevronRight,
  CircleChevronLeft,
  CircleChevronRight,
} from "lucide-vue-next";

//-------------------------------------------Mock-----------------------------------------------
// const fileImageFirstResponse = [
//   { fileName: "img1.jpg", imageViewOrder: 3 },
//   { fileName: "img3.jpg", imageViewOrder: 2 },
//   { fileName: "img2.jpg", imageViewOrder: 1 },
//   { fileName: "img0.jpg", imageViewOrder: 0 },
// ];
const fileImageFirstResponse = [];

const fileImageOrganize = ref([]);
const currentViewImage = ref(0);

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
        imageUrl: imageUrl, // เก็บ url ของรูปที่ได้จากการ fetch
        imageViewOrder: item.imageViewOrder,
      });
    }
    console.log(fileImageOrganize.value);
  } catch (error) {
    console.error("Error organizing and fetching images:", error);
  }
};

//definePropParam
const props = defineProps({
  param: {
    type: Number,
  },
});

onBeforeMount(async () => {
  const saleItemIdParam = props.param; // ตัวอย่าง ID ของ Sale Item
  const saleItem = await getSaleItemByIdV2(saleItemIdParam);
  console.log("Sale Item:", saleItem.saleItemImage);
  fileImageFirstResponse.push(...saleItem.saleItemImage);
  await organizeAndFetchImages();
});

//----------------------------------------------File-------------------------------------------------------------------------------------

const files = ref([]);

// เมื่อเลือกไฟล์
// แก้ไข handleFileChange เพื่อเชื่อมกับ images
const handleFileChange = (event) => {
  const selectedFiles = Array.from(event.target.files);
  files.value.push(...selectedFiles);
  console.log(files);
  // แปลงไฟล์เป็น URL สำหรับแสดงผล
  selectedFiles.forEach((file) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const url = e.target.result;
      console.log("File URL (Base64):", url);
      images.value.push(url);
    };
    reader.readAsDataURL(file);
  });

  event.target.value = "";
};

// เพิ่มตัวแปรที่ขาดหาย
const images = ref([]); // default image

// เพิ่มฟังก์ชันที่ขาดหาย
const prevImage = () => {
  currentViewImage.value--;
};

const nextImage = () => {
  currentViewImage.value++;
};
</script>

<template>
  <div class="flex-1">
    <div v-if="fileImageOrganize.length > 0" class="relative">
      <div
        class="bg-white rounded-lg shadow-md h-96 overflow-hidden flex items-center justify-center"
      >
        <img
          :src="fileImageOrganize[currentViewImage].imageUrl"
          alt="main image"
          class="max-h-full max-w-full object-contain"
        />
      </div>
      <button
        @click="prevImage"
        v-show="currentViewImage > 0"
        class="absolute left-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200"
      >
        <ChevronLeft />
      </button>
      <button
        @click="nextImage"
        v-show="currentViewImage < fileImageOrganize.length - 1"
        class="absolute right-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200"
      >
        <ChevronRight />
      </button>
    </div>
    <div
      class="grid grid-cols-4 gap-4 mt-3"
      v-if="fileImageOrganize.length > 1"
    >
      <div
        v-for="(image, index) in fileImageOrganize"
        :key="index"
        @click="currentViewImage = image.imageViewOrder - 1"
        :class="[
          'rounded border',
          image.imageViewOrder === currentViewImage
            ? 'border-blue-600'
            : 'border-blue-400',
          'h-32 cursor-pointer',
        ]"
      >
        <img
          :src="image.imageUrl"
          alt="thumbnail"
          class="w-full h-full object-cover rounded"
        />
      </div>
    </div>

    <div
      v-else
      class="bg-white rounded-lg shadow-md h-96 flex items-center justify-center border-2 border-dashed border-gray-300"
    >
      <div class="text-center text-gray-500">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-16 w-16 mx-auto mb-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v14a2 2 0 002 2z"
          />
        </svg>
      </div>
    </div>
  </div>
</template>

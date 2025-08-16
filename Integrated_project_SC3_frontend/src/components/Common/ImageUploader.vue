<script setup>
import {
  getImageByImageName,
  getSaleItemByIdV2
} from "@/libs/callAPI/apiSaleItem.js";
import {
  onBeforeMount,
  ref
} from "vue";
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
    type: Number
  }
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
  if (currentViewImage.value >= images.value.length && images.value.length > 0) {
    currentViewImage.value = images.value.length - 1;
  } else if (images.value.length === 0) {
    currentViewImage.value = 0;
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
            <p>currentViewImage: {{ currentViewImage }}</p>
          </div>
          <button
            @click="prevImage"
            v-show="currentViewImage > 0"
            class="absolute left-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200"
          >
          <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
          <button
            @click="nextImage"
            v-show="currentViewImage < fileImageOrganize.length - 1"
            class="absolute right-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </div>
        <div class="grid grid-cols-4 gap-4 mt-3" v-if="fileImageOrganize.length > 1">
          <div
            v-for="(image, index) in fileImageOrganize"
            :key="index"
            @click="currentViewImage = image.imageViewOrder-1"
            :class="[
              'rounded border',
              image.imageViewOrder === currentViewImage ? 'border-blue-600' : 'border-blue-400',
              'h-32 cursor-pointer',
            ]"
          >
            <img
              :src="image.imageUrl"
              alt="thumbnail"
              class="w-full h-full object-cover rounded"
            />
            <p>imageName {{ image.fileName }}</p>
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
            <p class="text-lg font-medium">No images uploaded</p>
          </div>
        </div>
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
</template>
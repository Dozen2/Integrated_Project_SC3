<script setup>
import { getImageByImageName, getSaleItemByIdV2 } from "@/libs/callAPI/apiSaleItem.js";
import { onBeforeMount, ref } from "vue";
import { ChevronLeft, ChevronRight } from "lucide-vue-next";

const fileImageFirstResponse = [];

const fileImageOrganize = ref([]);
const currentViewImage = ref(0);

const organizeAndFetchImages = async () => {
  try {
    const fileImageSorted = [...fileImageFirstResponse].sort((a, b) => a.imageViewOrder - b.imageViewOrder);
    for (const item of fileImageSorted) {
      const imageUrl = await getImageByImageName(item.fileName);
      fileImageOrganize.value.push({
        fileName: item.fileName,
        imageUrl: imageUrl,
        imageViewOrder: item.imageViewOrder,
      });
    }
    console.log(fileImageOrganize.value);
  } catch (error) {
    console.error("Error organizing and fetching images:", error);
  }
};
const props = defineProps({
  param: {
    type: Number,
  },
});

onBeforeMount(async () => {
  const saleItemIdParam = props.param;
  const saleItem = await getSaleItemByIdV2(saleItemIdParam);
  console.log("Sale Item:", saleItem.saleItemImage);
  fileImageFirstResponse.push(...saleItem.saleItemImage);
  await organizeAndFetchImages();
});
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
      <div class="bg-white rounded-lg shadow-md h-96 overflow-hidden flex items-center justify-center">
        <img :src="fileImageOrganize[currentViewImage].imageUrl" alt="main image" class="max-h-full max-w-full object-contain" />
      </div>
      <button @click="prevImage" v-show="currentViewImage > 0" class="absolute left-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200">
        <ChevronLeft />
      </button>
      <button @click="nextImage" v-show="currentViewImage < fileImageOrganize.length - 1" class="absolute right-2 top-1/2 -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-200">
        <ChevronRight />
      </button>
    </div>
    <div class="grid grid-cols-4 gap-4 mt-3" v-if="fileImageOrganize.length > 0">
      <div
        v-for="(image, index) in fileImageOrganize"
        :key="index"
        @click="currentViewImage = image.imageViewOrder - 1"
        :class="['rounded border', image.imageViewOrder === currentViewImage ? 'border-blue-600' : 'border-blue-400', 'h-32 cursor-pointer']"
      >
        <img :src="image.imageUrl" alt="thumbnail" class="w-full h-full object-cover rounded" />
      </div>
    </div>

    <div v-else class="bg-white rounded-2xl shadow-md h-96 flex flex-col items-center justify-center border border-gray-200 p-6">
      <img
        src="https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg"
        alt="No Image Available"
        class="w-40 h-40 object-contain mb-4 opacity-80"
      />
    </div>
  </div>
</template>

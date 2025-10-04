<script setup>
import { nullCatching, unitPrice } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import { onMounted, ref } from "vue";
import { getImageByImageName } from "@/libs/callAPI/apiSaleItem.js";

const alertStore = useAlertStore();

const props = defineProps({
  product: Array,
  default: () => [],
  imageUrl: Array,
});

// const imageUrl = ref([]);

onMounted(async () => {
  // console.log("ProductGallery mounted");
  // for (const item of props.product) {
  //   if (item.mainImageFileName) {
  //     const image = await getImageByImageName(item.mainImageFileName);
  //     imageUrl.value.push(image);
  //   } else {
  //     imageUrl.value.push(
  //       "https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg"
  //     );
  //   }
  // }
  console.log("Image URLs loaded:", props.imageUrl);

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage();
    }, 3000);
  }
});
</script>

<template>
  <div class=" py-6 max-w-7xl mx-auto">
    <div v-if="product.length === 0" class="text-center text-gray-500 text-xl">
      no sale item
    </div>

    <div
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6"
    >
      <RouterLink
        v-for="(item, index) in product"
        :key="index"
        :to="`/sale-items/${item.id}`"
        class="itbms-row bg-white p-4 rounded-2xl shadow-md hover:shadow-lg transition-all hover:scale-[1.02]"
      >
        <img
          :src="imageUrl[index]"
          alt="product image"
          class="w-full h-40 object-contain"
        />
        <div class="mt-3 space-y-1">
          <h2 class="itbms-brand text-lg font-bold text-gray-800">
            {{ item.brandName }}
          </h2>
          <p class="itbms-model text-sm text-gray-600">
            {{ item.model }}
          </p>
          <span class="itbms-ramGb text-sm text-gray-600">
            {{ nullCatching(item.ramGb) }} / 
          </span>
          <span class="itbms-storageGb text-sm text-gray-600">
            {{ nullCatching(item.storageGb) }} 
            <span class="itbms-storageGb-unit">GB</span>
          </span>
          <p class="itbms-price text-blue-600 font-semibold mt-3 text-lg">
            {{ unitPrice(item.price) }}
            <span class="itbms-price-unit">Baht</span>
          </p>
        </div>
      </RouterLink>
    </div>
  </div>
</template>

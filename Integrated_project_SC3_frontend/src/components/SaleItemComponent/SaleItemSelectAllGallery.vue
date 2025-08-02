<script setup>
import { nullCatching, unitPrice } from "@/libs/utils.js";
import { useAlertStore } from "@/stores/alertStore.js";
import { onMounted } from "vue";

const alertStore = useAlertStore()

const props = defineProps({
  product: Array,
  default: () => []
})

onMounted(() => {
  console.log('ProductGallery mounted')
  console.log(props.product)

  if (alertStore.message) {
    setTimeout(() => {
      alertStore.clearMessage()
    }, 3000)
  }
})

</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">

    <div v-if="product.length === 0" class="text-center text-gray-500 text-xl">
      no sale item
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
      <RouterLink v-for="(item, index) in product" :key="index" :to="`/sale-items/${item.id}`"
        class="itbms-row bg-white p-4 rounded-2xl shadow-md hover:shadow-lg transition-all hover:scale-[1.02]">
        <img :src="item.imageUrl || 'https://app-area.riointernational.com.bd/productImages/1738403480BRk6I.png'"
          alt="product image" class="w-full h-40 object-contain" /> 

        <div class="mt-3 space-y-1">
          <h2 class="itbms-brand text-lg font-bold text-gray-800">
            {{ item.brandName }}
          </h2>
          <p class="itbms-model text-sm text-gray-600">
            {{ item.model }}
          </p>
          <p class="itbms-ramGb text-sm text-gray-600">
            {{ nullCatching(item.ramGb) }}
          </p>
          <p class="itbms-storageGb text-sm text-gray-600">
            {{ nullCatching(item.storageGb) }} <span class="itbms-storageGb-unit">GB</span>
          </p>
          <p class="itbms-price text-blue-600 text-base font-semibold">
            {{ unitPrice(item.price) }} <span class="itbms-price-unit">Baht</span>
          </p>
        </div>
      </RouterLink>
    </div>
   </div>
</template>
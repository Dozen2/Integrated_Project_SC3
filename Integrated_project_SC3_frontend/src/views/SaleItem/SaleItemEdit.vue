<script setup>
import ProductCreateAndEdit from "@/components/SaleItemComponent/SaleItemCreateAndEdit.vue";
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getSaleItemById } from "@/libs/callAPI/apiSaleItem.js";

const route = useRoute();
const router = useRouter();
const product = ref({});

onMounted(async () => {
  try {
    const data = await getSaleItemById(route.params.id);
    if (data == undefined) {
      product.value = "404_not_found";
      setTimeout(() => {
        router.push("/sale-items");
      }, 2000);
    } else {
      product.value = data;
    }
  } catch (error) {
    console.log("โหลดข้อมูลสินค้าไม่สำเร็จ:", error.message);
  }
});
</script>
<template>
  <ProductCreateAndEdit mode="Edit" :product-id="route.params.id" />
</template>

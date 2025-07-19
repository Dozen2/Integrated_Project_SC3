<script setup>
import {
  ref,
  defineProps,
  watchEffect,
  onBeforeMount,
  onMounted,
  watch,
  defineExpose,
} from "vue";
import { getAllData } from "@/libs/api";
import { useRouter } from "vue-router";

// Props
const prop = defineProps({
  brandError: Boolean,
  brandName: {
    type: String,
    default: "All Brands",
  },
  reloadData: {
    type: Number,
  },
  modelvalue: String,
});

const emit = defineEmits(
  ["sendBrandId", "sendBrandName"],
  ["update:modelValue"]
);

// States
const errorColor = ref("border-gray-300");
const selectedId = ref(prop.modelvalue || "");
const options = ref([]);
const brand = ref("");
const URL = import.meta.env.VITE_ROOT_API_URL;
const router = useRouter();

// Update border color on brandError
watchEffect(() => {
  errorColor.value = prop.brandError ? "border-red-300" : "border-gray-300";
});

async function fetchBrands() {
  try {
    const data = await getAllData(`${URL}/itb-mshop/v1/brands`);
    //console.log("response from API", data);
    if (data?.error === "not_found") {
      brand.value = "404_not_found";
      setTimeout(() => router.push("/sale-items"), 2000);
      return;
    }

    data.sort((a, b) => a.name.localeCompare(b.name));
    options.value = data;

    // กำหนด selectedId จาก brandName ที่รับมา
    const found = data.find((b) => b.name === prop.brandName);
    if (found) {
      selectedId.value = found.id;
      emit("sendBrandId", found.id);
      emit("sendBrandName", found.name);
    }
  } catch (error) {
    console.error("โหลดข้อมูลแบรนด์ไม่สำเร็จ:", error.message);
    alert("เกิดข้อผิดพลาดในการโหลดแบรนด์");
  }
}

onBeforeMount(() => {
  fetchBrands();
  // console.log(options.value);
  // console.log(data);
});

watch(
  () => prop.reloadData,
  () => {
    fetchBrands();
  }
);

// เมื่อเลือกแบรนด์จาก select
function handleChange() {
  const brandObj = options.value.find((b) => b.id === selectedId.value);
  if (brandObj) {
    emit("sendBrandId", brandObj.id);
    emit("sendBrandName", brandObj.name);
    emit("update:modelValue", brandObj.name);
  }
}

function resetSelection() {
  selectedId.value = "";
}
defineExpose({
  resetSelection,
});
</script>

<template>
  <!-- แจ้งเตือน 404 -->
  <div
    v-if="brand === '404_not_found'"
    class="fixed top-5 left-1/2 transform -translate-x-1/2 bg-red-50 border border-red-300 text-red-800 text-sm px-6 py-3 rounded-md shadow-lg z-50 flex items-center space-x-2"
  >
    <svg
      class="w-5 h-5 text-red-600"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M12 9v2m0 4h.01M12 5a7 7 0 11-7 7 7 7 0 017-7z"
      />
    </svg>
    <span>ไม่พบข้อมูลแบรนด์ที่คุณเลือก</span>
  </div>

  <!-- เปลี่ยนเป็น <select> -->
  <div class="w-48">
    <select
      v-model="selectedId"
      @change="handleChange"
      :class="`itbms-brand w-full px-4 py-2 text-sm border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${errorColor}`"
    >
      <option value="">-- เลือกแบรนด์ --</option>
      <option
        v-for="option in options"
        :key="option.id"
        :value="option.id"
        class=""
      >
        {{ option.name }}
      </option>
    </select>
  </div>
</template>

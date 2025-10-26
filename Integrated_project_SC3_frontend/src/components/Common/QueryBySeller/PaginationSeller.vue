<script setup>
import { ref, watch, onMounted } from "vue";

const props = defineProps({
  totalPage: {
    type: Number,
    required: true,
  },
  modelValue: {
    type: Number,
    default: 0, // เปลี่ยนเป็น 0 เพื่อให้ตรงกับ parent
  },
  storageKey: {
    type: String,
    default: "pagination_page",
  },
});

const emit = defineEmits(["update:modelValue"]);

// ใช้ค่าจาก props เป็น initial value
const page = ref(props.modelValue + 1); // แปลงเป็น 1-based สำหรับ UI

// เมื่อ props.modelValue เปลี่ยน (จาก parent) ให้อัปเดต page
watch(() => props.modelValue, (newValue) => {
  console.log("PaginationSeller received new modelValue:", newValue);
  page.value = newValue + 1; // แปลงเป็น 1-based
});

// โหลดค่าจาก sessionStorage ตอน mount แต่ให้ props มี priority
onMounted(() => {
  // ถ้า props.modelValue มีค่า (ไม่ใช่ default) ให้ใช้ props
  if (props.modelValue >= 0) {
    page.value = props.modelValue + 1;
    console.log("Mounted, using props page:", props.modelValue);
  } else {
    // ถ้าไม่มี ค่อยอ่านจาก sessionStorage
    const savedPage = sessionStorage.getItem(props.storageKey);
    if (savedPage) {
      const p = parseInt(savedPage, 10);
      if (p >= 0 && p < props.totalPage) { 
        page.value = p + 1;
        emit("update:modelValue", p);
      }
    }
    console.log("Mounted, page:", page.value);
  }
});

// sync page → sessionStorage + emit
watch(page, (newPage) => {
  const zeroBased = newPage - 1;
  sessionStorage.setItem(props.storageKey, zeroBased.toString());
  emit("update:modelValue", zeroBased); 
});

// ฟังก์ชันเปลี่ยนหน้า
function goToPage(p) {
  if (p >= 1 && p <= props.totalPage) {
    page.value = p;
    console.log("Go to page:", p);
  }
}
</script>

<template>
  <div v-show="totalPage > 1" class="Pagination">
    <!-- ปรับขนาดตัวอักษรและ padding โดยรวมให้เล็กลงในจอเล็ก -->
    <div class="flex justify-center text-xs sm:text-sm md:text-base">
      <div
        class="flex gap-0.5 items-center bg-white rounded-lg shadow-sm border p-0.5 md:p-2"
      >
        <!-- ปุ่ม First และ Prev (แสดงเสมอ แต่ขนาดลดลง) -->
        <button
          @click="goToPage(1)"
          :disabled="page === 1"
          class="cursor-pointer itbms-page-first px-1.5 py-0.5 md:px-3 md:py-2 rounded text-gray-600 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
        >
          First
        </button>
        <button
          @click="goToPage(Math.max(1, page - 1))"
          :disabled="page === 1"
          class="cursor-pointer itbms-page-prev px-1.5 py-0.5 md:px-3 md:py-2 rounded text-gray-600 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
        >
          Prev
        </button>

        <!-- 🔴 Dropdown/Box View (แสดงเมื่อจอเล็กกว่า 768px) -->
        <div class="block md:hidden mx-1">
          <select
            :value="page"
            @change="goToPage(Number($event.target.value))"
            class="h-full border border-gray-300 rounded-lg bg-white text-sm py-1 px-1 focus:ring-sky-500 focus:border-sky-500 min-w-[3rem] shadow-sm"
          >
            <option v-for="p in totalPage" :key="p" :value="p">
              {{ p }}
            </option>
          </select>
        </div>

        <!-- 🟢 List View (แสดงเมื่อจอใหญ่กว่าหรือเท่ากับ 768px) -->
        <div class="hidden md:flex gap-1 items-center">
          <template v-for="(p, index) in totalPage" :key="p">
            <button
              @click="goToPage(p)"
              :class="[
                `itbms-page-${index}`,
                'px-1.5 py-0.5 md:px-3 md:py-2 rounded transition min-w-8',
                page === p
                  ? 'bg-gray-800 text-white'
                  : 'text-gray-600 hover:bg-gray-300 cursor-pointer ',
              ]"
            >
              {{ p }}
            </button>
          </template>
        </div>

        <!-- ปุ่ม Next และ Last (แสดงเสมอ แต่ขนาดลดลง) -->
        <button
          @click="goToPage(Math.min(totalPage, page + 1))"
          :disabled="page === totalPage"
          class="itbms-page-next px-1.5 py-0.5 md:px-3 md:py-2 rounded text-gray-600 hover:bg-gray-300 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
        >
          Next
        </button>
        <button
          @click="goToPage(totalPage)"
          :disabled="page === totalPage"
          class="itbms-page-last px-1.5 py-0.5 md:px-3 md:py-2 rounded text-gray-600 hover:bg-gray-300 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
        >
          Last
        </button>
      </div>
    </div>
  </div>
</template>

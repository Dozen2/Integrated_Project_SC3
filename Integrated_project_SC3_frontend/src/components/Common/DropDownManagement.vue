<script setup>
import { ChevronDown } from "lucide-vue-next";
import { ref, computed, onMounted, onUnmounted, reactive, watch } from "vue";
import { useRoute, RouterLink } from "vue-router";

const isOpen = ref(false);
const route = useRoute();
const currentPath = ref(route.path);

const menuItems = reactive([
  { text: "SaleItem Manage", name: "ProductManage", path: "/sale-items/list" },
  { text: "Brand Manage", name: "BrandManage", path: "/brands" },
  { text: "Order Manage", name: "OrderManage", path: "/seller-order" },
]);

watch(route, (newRoute) => {
  currentPath.value = newRoute.path;
});

const displayText = computed(() => {
  const path = currentPath.value;
  if (path === "/sale-items/list") {
    return menuItems[0].text;
  }
  if (path === "/brands") {
    return menuItems[1].text;
  }
  if (path === "/seller-order") {
    return menuItems[2].text;
  }
  return "Management";
});

const isActiveMenuItem = (itemPath) => {
  return currentPath.value === itemPath;
};

const dropdown = ref(null);

const handleClickOutside = (event) => {
  if (isOpen.value && dropdown.value && !dropdown.value.contains(event.target)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  console.log("Current path: ", currentPath.value);
  document.addEventListener("mousedown", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("mousedown", handleClickOutside);
});

const closeDropdown = () => {
  isOpen.value = false;
};
</script>

<template>
  <div ref="dropdown" class="relative inline-block text-left">
    <div>
      <button
        @click="isOpen = !isOpen"
        type="button"
        class="inline-flex justify-between items-center gap-2 w-full sm:w-50 border border-sky-300 bg-sky-50 text-sky-700 hover:bg-sky-100 text-sm font-medium px-4 py-2 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-400 cursor-pointer"
        id="menu-button"
        aria-expanded="true"
        aria-haspopup="true"
      >
        <span class="truncate">{{ displayText }}</span>
        <span
          class="-mr-1 h-5 w-5 text-sky-400 transition-transform duration-200"
          :class="{ 'transform rotate-180': isOpen }"
          viewBox="0 0 20 20"
          fill="currentColor"
          aria-hidden="true"
        >
          <ChevronDown color="#3b82f6" strokeWidth="2" />
        </span>
      </button>
    </div>

    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="absolute right-0 z-10 mt-2 w-48 sm:w-50 origin-top-right rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none overflow-hidden"
        role="menu"
        aria-orientation="vertical"
        aria-labelledby="menu-button"
        tabindex="-1"
      >
        <div class="py-1" role="none">
          <RouterLink
            v-for="(item, index) in menuItems"
            :key="item.text"
            :to="item.path"
            @click="closeDropdown"
            :class="{
              'bg-blue-50 border-l-4 border-sky-500': isActiveMenuItem(item.path),
              'hover:bg-gray-50 border-l-4 border-transparent': !isActiveMenuItem(item.path),
            }"
            class="block px-4 py-2 text-sm transition-colors duration-150"
            role="menuitem"
            tabindex="-1"
          >
            <span
              :class="{
                'text-sky-700 font-semibold': isActiveMenuItem(item.path),
                'text-gray-700': !isActiveMenuItem(item.path),
              }"
            >
              {{ item.text }}
            </span>
          </RouterLink>
        </div>
      </div>
    </transition>
  </div>
</template>
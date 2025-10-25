<script setup>
import { defineProps, defineEmits } from "vue";
import { AlertTriangle } from "lucide-vue-next"; // ใช้ไอคอนจาก Lucide

const props = defineProps({
  show: { type: Boolean, default: false },
  message: { type: [String], default: "Are you sure you want to continue?" },
  title: { type: String, default: "Confirmation" },
  confirmText: { type: String, default: "Confirm" },
  cancelText: { type: String, default: "Cancel" },
  hideCancel: { type: Boolean, default: false },
  type: { type: String, default: "warning" }, // warning | success | error
});

const emit = defineEmits(["confirm", "cancel"]);

const onConfirm = () => emit("confirm");
const onCancel = () => emit("cancel");
</script>

<template>
  <transition name="fade">
    <div
      v-if="show"
      class="fixed inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-50 transition-opacity duration-300"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl p-6 w-80 sm:w-96 transform transition-all duration-300 scale-100 hover:scale-[1.01]"
      >
        <!-- Icon -->
        <div class="flex justify-center mb-4">
          <component
            :is="AlertTriangle"
            class='w-14 h-14 p-3 rounded-full text-red-500 bg-red-50'
          />
        </div>

        <!-- Title -->
        <h3 class="text-xl font-bold text-gray-800 mb-3 text-center">
          {{ title }}
        </h3>

        <!-- Message -->
        <p
          class="text-gray-600 mb-6 text-center leading-relaxed"
          v-html="message"
        ></p>

        <!-- Buttons -->
        <div class="flex justify-center gap-3">
          <button
            v-if="!hideCancel"
            @click="onCancel"
            class="px-5 py-2.5 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100 transition cursor-pointer"
          >
            {{ cancelText }}
          </button>
          <button
            @click="onConfirm"
            :class="[
              'px-5 py-2.5 rounded-lg text-white shadow-md transition bg-red-500 hover:bg-red-600 cursor-pointer'
            ]"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

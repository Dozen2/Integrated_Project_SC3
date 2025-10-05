<script setup>
import { useAlertStore } from "@/stores/alertStore.js";
import { nextTick, watch, onMounted } from "vue";
import { gsap } from "gsap";

const alertStore = useAlertStore();

// Watch for new toasts and animate them
watch(
  () => alertStore.toasts.length,
  async () => {
    await nextTick();

    // Animate in new toasts
    const toastElements = document.querySelectorAll(".toast-item");
    const newToasts = Array.from(toastElements).slice(-1); // Get the last toast (newest)

    newToasts.forEach((toast) => {
      // Set initial state (slide from bottom right)
      gsap.set(toast, {
        x: 100,
        y: 50,
        opacity: 0,
        scale: 0.5,
        rotation: 15,
      });

      // Animate in with bounce effect
      gsap.to(toast, {
        x: 0,
        y: 0,
        opacity: 1,
        scale: 1,
        rotation: 0,
        duration: 0.8,
        ease: "elastic.out(1, 0.3)",
      });
    });
  }
);

// Auto remove toasts after delay with animation
const setupAutoRemove = (id, delay = 5000) => {
  setTimeout(() => {
    if (alertStore.toasts.find((t) => t.id === id)) {
      removeToast(id);
    }
  }, delay);
};

// Watch for new toasts to setup auto remove
watch(
  () => alertStore.toasts,
  (newToasts, oldToasts) => {
    if (newToasts.length > (oldToasts?.length || 0)) {
      const newToast = newToasts[newToasts.length - 1];
      setupAutoRemove(newToast.id);
    }
  },
  { deep: true }
);

const removeToast = (id) => {
  const toastElement = document.querySelector(`[data-toast-id="${id}"]`);

  if (toastElement) {
    // Animate out with slide and fade
    gsap.to(toastElement, {
      x: 100,
      y: 50,
      opacity: 0,
      scale: 0.3,
      rotation: -10,
      duration: 0.5,
      ease: "back.in(2)",
      onComplete: () => {
        alertStore.removeToast(id);
      },
    });
  } else {
    alertStore.removeToast(id);
  }
};
</script>

<template>
  <div class="fixed bottom-4 right-4 space-y-3 z-50">
    <div
      v-for="toast in alertStore.toasts"
      :key="toast.id"
      :data-toast-id="toast.id"
      :class="[
        'toast-item px-5 py-4 rounded-xl shadow-2xl flex justify-between items-center min-w-[280px] cursor-pointer transition-all duration-300 hover:shadow-2xl hover:scale-105 relative overflow-hidden bg-white',
      ]"
    >
      <div class="flex items-center z-10">
        <!-- Icon with colored background -->
        <div
          :class="[
            'mr-4 text-2xl rounded-full w-8 h-8 flex items-center justify-center backdrop-blur-sm border',
            toast.type === 'success' ? 'bg-green-100 border-green-200 text-green-700' : '',
            toast.type === 'error' ? 'bg-red-100 border-red-200 text-red-700' : '',
            toast.type === 'warning' ? 'bg-yellow-100 border-yellow-200 text-yellow-700' : '',
            toast.type === 'info' ? 'bg-blue-100 border-blue-200 text-blue-700' : '',
          ]"
        >
          <span v-if="toast.type === 'success'">✓</span>
          <span v-else-if="toast.type === 'error'">⚠</span>
          <span v-else-if="toast.type === 'warning'">⚠</span>
          <span v-else-if="toast.type === 'info'">ℹ</span>
        </div>
        <div>
          <div
            v-if="toast.header"
            :class="[
              'font-bold text-base mb-1 drop-shadow-sm px-2 py-1 rounded-md inline-block',
              toast.type === 'success' ? 'text-green-800 bg-green-100/90' : '',
              toast.type === 'error' ? 'text-red-800 bg-red-100/90' : '',
              toast.type === 'warning' ? 'text-yellow-800 bg-yellow-100/90' : '',
              toast.type === 'info' ? 'text-blue-800 bg-blue-100/90' : '',
            ]"
          >
            {{ toast.header }}
          </div>
          <div class="itbms-message text-sm drop-shadow-sm text-black mt-2">{{ toast.message }}</div>
        </div>
      </div>
      <button
        @click="removeToast(toast.id)"
        class="ml-4 font-bold text-xl hover:scale-125 transition-all duration-200 flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-full text-gray-600 hover:text-gray-800 z-10"
      >
        ×
      </button>

      <!-- Subtle accent overlay -->
      <div
        :class="[
          'absolute inset-0 opacity-20',
          toast.type === 'success' ? 'bg-gradient-to-br from-green-200 to-green-500' : '',
          toast.type === 'error' ? 'bg-gradient-to-br from-red-200 to-red-500' : '',
          toast.type === 'warning' ? 'bg-gradient-to-br from-yellow-200 to-yellow-500' : '',
          toast.type === 'info' ? 'bg-gradient-to-br from-blue-200 to-blue-500' : '',
        ]"
      ></div>
      <div
        :class="[
          'absolute top-0 left-0 w-full h-0.5',
          toast.type === 'success' ? 'bg-green-300' : '',
          toast.type === 'error' ? 'bg-red-300' : '',
          toast.type === 'warning' ? 'bg-yellow-300' : '',
          toast.type === 'info' ? 'bg-blue-300' : '',
        ]"
      ></div>
    </div>
  </div>
</template>

<style scoped>
.toast-item {
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 10px 10px -5px rgba(0, 0, 0, 0.03), inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.toast-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, transparent 50%);
  pointer-events: none;
  border-radius: inherit;
}

.toast-item:hover::before {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4) 0%, transparent 50%);
}
</style>

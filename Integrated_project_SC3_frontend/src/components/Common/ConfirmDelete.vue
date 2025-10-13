<script setup>
import { defineProps, defineEmits } from "vue";

const props = defineProps({
    show: { type: Boolean, default: false },
    message: { type: [String], default: "Are you sure?" },
    title: { type: String, default: "Confirm" },
    confirmText: { type: String, default: "Confirm" },
    cancelText: { type: String, default: "Cancel" },
    hideCancel: { type: Boolean, default: false },
});

const emit = defineEmits(["confirm", "cancel"]);

const onConfirm = () => emit("confirm");
const onCancel = () => emit("cancel");
</script>
<template>
    <div v-if="show" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
        <div class="bg-white rounded-xl shadow-lg p-6 w-80">
            <h3 class="text-lg font-semibold mb-4">{{ title }}</h3>
            <p class="text-gray-600 mb-6" v-html="message"></p>
            <div class="flex justify-end gap-3">
                <button v-if="!hideCancel" @click="onCancel"
                    class="px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-100">
                    {{ cancelText }}
                </button>
                <button @click="onConfirm" class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600">
                    {{ confirmText }}
                </button>
            </div>
        </div>
    </div>
</template>
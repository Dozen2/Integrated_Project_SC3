<!-- src/views/Cart.vue -->
<script setup>
import { cart, updateQuantity, removeFromCart } from "@/composables/useCart.js";

const increment = (item) => {
  updateQuantity(item.id, item.sellerId, item.quantity + 1);
};

const decrement = (item) => {
  if (item.quantity - 1 <= 0) {
    // ถ้าเหลือ 0 → ลบออก
    removeFromCart(item.id, item.sellerId);
  } else {
    updateQuantity(item.id, item.sellerId, item.quantity - 1);
  }
};
</script>

<template>
  <div class="p-4">
    <h1 class="text-xl font-bold mb-4">🛒 Your Cart</h1>

    <div v-if="cart.length === 0" class="text-gray-500">
      ไม่มีสินค้าในตะกร้า
    </div>

    <div v-else class="space-y-4">
      <div v-for="item in cart" :key="item.id + '-' + item.sellerId"
           class="flex items-center justify-between border p-3 rounded">
        <div class="flex items-center gap-3">
          <img v-if="item.image" :src="item.image" alt="img" class="w-16 h-16 object-cover rounded" />
          <div>
            <p class="font-semibold">{{ item.brandName }} {{ item.model }}</p>
            <p class="text-sm text-gray-500">฿{{ item.price }}</p>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <button @click="decrement(item)" class="px-2 py-1 bg-gray-200 rounded">-</button>
          <span>{{ item.quantity }}</span>
          <button @click="increment(item)" class="px-2 py-1 bg-gray-200 rounded">+</button>
        </div>

        <div class="font-bold text-green-600">
          ฿{{ item.price * item.quantity }}
        </div>
      </div>

      <!-- Cart Summary -->
      <div class="border-t pt-4 text-right">
        <p class="font-bold">
          รวมทั้งหมด:
          ฿{{ cart.reduce((sum, it) => sum + it.price * it.quantity, 0) }}
        </p>
      </div>
    </div>
  </div>
</template>

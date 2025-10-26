import { defineStore } from "pinia";
import { useAuthStore } from "./auth";

const CART_KEY = "cart";

export const useCartStore = defineStore("cart", {
  state: () => ({
    cart: [],
  }),

  getters: {
    cartItemCount: (state) =>
      state.cart.reduce((sum, it) => sum + Number(it.quantity || 0), 0),
  },

  actions: {
    getCartKey() {
      const auth = useAuthStore();
      const user = auth.getAuthData();
      if (user && user.id) {
        return `cart_${user.id}`
      }
      return "cart_guest"
    },

    loadCart() {
      try {
        const key = this.getCartKey();
        const raw = localStorage.getItem(key);
        this.cart = raw ? JSON.parse(raw) : [];
      } catch (e) {
        console.error("loadCart error", e);
        this.cart = [];
      }
    },

    saveCart() {
      try {
        const key = this.getCartKey();
        localStorage.setItem(key, JSON.stringify(this.cart));
      } catch (e) {
        console.error("saveCart error", e);
      }
    },

    findIndex(id, sellerId) {
      return this.cart.findIndex(
        (i) =>
          String(i.id) === String(id) && String(i.sellerId) === String(sellerId)
      );
    },

    addToCart(itemPayload, qty = 1) {
      qty = Number(qty) || 1;
      if (!itemPayload || !itemPayload.id) {
        return { success: false, message: "Invalid item" };
      }

      const idx = this.findIndex(itemPayload.id, itemPayload.sellerId);
      const stock = Number(itemPayload.stock ?? itemPayload.quantity ?? Infinity);

      if (idx !== -1) {
        const current = Number(this.cart[idx].quantity || 0);
        const willBe = current + qty;
        if (willBe > stock) {
          const available = stock - current;
          if (available <= 0) {
            return { success: false, message: "You've already added the maximum available quantity of this item to your cart." };
          }
          this.cart[idx].quantity = current + available;
          this.saveCart();
          return {
            success: true,
            added: available,
            message: `You can only add ${available} due to stock limit.`,
          };
        } else {
          this.cart[idx].quantity = willBe;
          this.saveCart();
          return { success: true, added: qty };
        }
      } else {
        const toAdd = Math.min(qty, stock);
        if (toAdd <= 0) return { success: false, message: "This saleitem out of stock" };

        this.cart.push({
          id: itemPayload.id,
          sellerId: itemPayload.sellerId,
          brandName: itemPayload.brandName ?? null,
          model: itemPayload.model ?? null,
          price: itemPayload.price ?? 0,
          quantity: toAdd,
          color: itemPayload.color ?? null,
          images: itemPayload.images.map(img => ({
            fileName: img.fileName,
            imageViewOrder: img.imageViewOrder,
          })),
          stock: stock,
          storageGb: itemPayload.storageGb
        });
        this.saveCart();
        return { success: true, added: toAdd };
      }
    },

    removeFromCart(id, sellerId) {
      const idx = this.findIndex(id, sellerId);
      if (idx !== -1) {
        this.cart.splice(idx, 1);
        this.saveCart();
        return true;
      }
      return false;
    },

    updateQuantity(id, sellerId, newQty) {
      newQty = Number(newQty);
      const idx = this.findIndex(id, sellerId);
      if (idx === -1) return { success: false, message: "item not found" };
      const stock = Number(this.cart[idx].stock ?? Infinity);
      if (newQty <= 0) {
        this.cart.splice(idx, 1);
        this.saveCart();
        return { success: true, removed: true };
      }
      const finalQty = Math.min(newQty, stock);
      this.cart[idx].quantity = finalQty;
      this.saveCart();
      return { success: true, quantity: finalQty };
    },

    clearCart() {
      this.cart = [];
      this.saveCart();
    },
  },
});

import { ref } from "vue";

const CART_KEY = "cart";
const cart = ref([]);
const cartItemCount = ref(0);

function loadCart() {
  try {
    const raw = localStorage.getItem(CART_KEY);
    cart.value = raw ? JSON.parse(raw) : [];
  } catch (e) {
    cart.value = [];
  }
  updateCartCount();
}

function saveCart() {
  try {
    localStorage.setItem(CART_KEY, JSON.stringify(cart.value));
  } catch (e) {
    console.error("saveCart error", e);
  }
  updateCartCount();
}

function updateCartCount() {
  cartItemCount.value = cart.value.reduce(
    (sum, it) => sum + Number(it.quantity || 0),
    0
  );
}

function findIndex(id, sellerId) {
  return cart.value.findIndex(
    (i) => String(i.id) === String(id) && String(i.sellerId) === String(sellerId)
  );
}

function addToCart(itemPayload, qty = 1) {
  qty = Number(qty) || 1;
  if (!itemPayload || !itemPayload.id) {
    return { success: false, message: "Invalid item" };
  }

  const idx = findIndex(itemPayload.id, itemPayload.sellerId);
  const stock = Number(itemPayload.stock ?? itemPayload.quantity ?? Infinity);

  if (idx !== -1) {
    const current = Number(cart.value[idx].quantity || 0);
    const willBe = current + qty;
    if (willBe > stock) {
      const available = stock - current;
      if (available <= 0) {
        return { success: false, message: "สินค้าเกินสต็อก" };
      }
      cart.value[idx].quantity = current + available;
      saveCart();
      return {
        success: true,
        added: available,
        message: `เพิ่มได้เพียง ${available} เนื่องจากจำกัดสต็อก`,
      };
    } else {
      cart.value[idx].quantity = willBe;
      saveCart();
      return { success: true, added: qty };
    }
  } else {
    const toAdd = Math.min(qty, stock);
    if (toAdd <= 0) return { success: false, message: "สินค้าหมดสต็อก" };

    cart.value.push({
      id: itemPayload.id,
      sellerId: itemPayload.sellerId,
      brandName: itemPayload.brandName ?? null,
      model: itemPayload.model ?? null,
      price: itemPayload.price ?? 0,
      quantity: toAdd,
      color: itemPayload.color ?? null,
      image: itemPayload.image ?? null,
      stock: stock,
    });
    saveCart();
    return { success: true, added: toAdd };
  }
}

function removeFromCart(id, sellerId) {
  const idx = findIndex(id, sellerId);
  if (idx !== -1) {
    cart.value.splice(idx, 1);
    saveCart();
    return true;
  }
  return false;
}

function updateQuantity(id, sellerId, newQty) {
  newQty = Number(newQty);
  const idx = findIndex(id, sellerId);
  if (idx === -1) return { success: false, message: "item not found" };
  const stock = Number(cart.value[idx].stock ?? Infinity);
  if (newQty <= 0) {
    // remove
    cart.value.splice(idx, 1);
    saveCart();
    return { success: true, removed: true };
  }
  const finalQty = Math.min(newQty, stock);
  cart.value[idx].quantity = finalQty;
  saveCart();
  return { success: true, quantity: finalQty };
}

function getCart() {
  return cart.value;
}

function clearCart() {
  cart.value = [];
  saveCart();
}

/* Sync across tabs */
window.addEventListener("storage", (e) => {
  if (e.key === CART_KEY) {
    loadCart();
  }
});

loadCart();

/* exports */
export {
  cartItemCount,
  cart,
  addToCart,
  removeFromCart,
  updateQuantity,
  getCart,
  clearCart,
  updateCartCount,
};

export default function useCart() {
  return {
    cartItemCount,
    cart,
    addToCart,
    removeFromCart,
    updateQuantity,
    getCart,
    clearCart,
    updateCartCount,
  };
}


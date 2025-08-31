import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    role: null, // 'buyer' | 'seller'
    token: null,
  }),
  actions: {
    loginAsBuyer() {
      this.isLoggedIn = true
      this.role = 'buyer'
      this.token = 'FAKE_BUYER_TOKEN'
    },
    loginAsSeller() {
      this.isLoggedIn = true
      this.role = 'seller'
      this.token = 'FAKE_SELLER_TOKEN'
    },
    logout() {
      this.isLoggedIn = false
      this.role = null
      this.token = null
    }
  }
})

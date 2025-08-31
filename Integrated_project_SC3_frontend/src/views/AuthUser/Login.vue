<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

function loginAsBuyer() {
  auth.loginAsBuyer()
  redirectAfterLogin()
}

function loginAsSeller() {
  auth.loginAsSeller()
  redirectAfterLogin()
}

function redirectAfterLogin() {
  const redirect = route.query.redirect
  if (redirect) {
    router.replace(redirect)
  } else {
    if (auth.role === 'seller') {
      router.replace({ name: 'ProductManage' })
    } else {
      router.replace({ name: 'Home' })
    }
  }
}
</script>

<template>
  <div>
    <h1>Login (Mock)</h1>
    <button 
    @click="loginAsBuyer"
    class="bg-red-700"
    >
        Login as Buyer
    </button>
    <button 
    @click="loginAsSeller"
    class="bg-blue-700"
    >
        Login as Seller
    </button>
  </div>
</template>

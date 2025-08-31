
import { createRouter, createWebHistory } from 'vue-router'
import ProductList from "../views/SaleItem/SaleItemList.vue"
import HomePage from '@/views/HomePage.vue'
import ProductDetail from '@/views/SaleItem/SaleItemDetail.vue'
import ProuctEdit from '@/views/SaleItem/SaleItemEdit.vue'
import ProuctCreate from '@/views/SaleItem/SaleItemCreate.vue'
import BrandCreate from '@/views/Brand/BrandCreate.vue'
import ProductManage from '@/views/SaleItem/SaleItemManage.vue'
import BrandManage from '@/views/Brand/BrandManage.vue'
import BrandEdit from '@/views/Brand/BrandEdit.vue'
import Register from '@/views/AuthUser/Register.vue'
import Login from '@/views/AuthUser/Login.vue'
import VerifyEmail from '@/views/AuthUser/verifyEmail.vue'
import { useAuthStore } from '@/stores/auth'


// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes: [
//     {
//       path: '/',
//       name: 'Home',
//       component: HomePage
//     },
//     {
//       path: '/sale-items',
//       name: 'Products',
//       component: ProductList
//     },
//     {
//       path: '/sale-items/:id',
//       name: 'Detail',
//       component: ProductDetail
//     },
//     {
//       path: '/sale-items/:id/edit',
//       name: 'Edit',
//       component: ProuctEdit,
//       props: true
//     },
//     {
//       path: '/sale-items/create',
//       name: 'ProuctCreate',
//       component: ProuctCreate

//     },
//     {
//       path: '/sale-items/list',
//       name: 'ProductManage',
//       component: ProductManage
//     },
//     {
//       path: '/brands/add',
//       name: 'BrandCreate',
//       component: BrandCreate
//     },
//     {
//       path: '/brands',
//       name: 'BrandManage',
//       component: BrandManage

//     },
//     {
//       path: '/brands/:id/edit',
//       name: 'BrandEdit',
//       component: BrandEdit
//     },
//     {
//       path: '/sale-items/register',
//       name: 'Register',
//       component: Register
//     },
//     {
//       path: '/sale-items/login',
//       name: 'Login',
//       component: Login
//     },
//     {
//       path: '/verify-email',
//       name: 'VerifyeMail',
//       component: VerifyEmail
//     },
//   ],
// })





// import { createRouter, createWebHistory } from 'vue-router'
// import HomePage from '@/pages/HomePage.vue'
// import ProductList from '@/pages/ProductList.vue'
// import ProductDetail from '@/pages/ProductDetail.vue'
// import ProuctEdit from '@/pages/ProductEdit.vue'
// import ProuctCreate from '@/pages/ProductCreate.vue'
// import ProductManage from '@/pages/ProductManage.vue'
// import BrandCreate from '@/pages/BrandCreate.vue'
// import BrandManage from '@/pages/BrandManage.vue'
// import BrandEdit from '@/pages/BrandEdit.vue'
// import Register from '@/pages/Register.vue'
// import Login from '@/pages/Login.vue'
// import VerifyEmail from '@/pages/VerifyEmail.vue'




const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Buyer หรือ Public
    { path: '/', name: 'Home', component: HomePage },
    { path: '/sale-items', name: 'Products', component: ProductList },
    { path: '/sale-items/:id', name: 'Detail', component: ProductDetail },

    // Seller
    { path: '/sale-items/:id/edit', name: 'Edit', component: ProuctEdit, meta: { requiresAuth: true, roles: ['seller'] } },
    { path: '/sale-items/create', name: 'ProuctCreate', component: ProuctCreate, meta: { requiresAuth: true, roles: ['seller'] } },
    { path: '/sale-items/list', name: 'ProductManage', component: ProductManage, meta: { requiresAuth: true, roles: ['seller'] } },
    { path: '/brands/add', name: 'BrandCreate', component: BrandCreate, meta: { requiresAuth: true, roles: ['seller'] } },
    { path: '/brands', name: 'BrandManage', component: BrandManage, meta: { requiresAuth: true, roles: ['seller'] } },
    { path: '/brands/:id/edit', name: 'BrandEdit', component: BrandEdit, meta: { requiresAuth: true, roles: ['seller'] } },

    // Auth
    { path: '/sale-items/register', name: 'Register', component: Register },
    { path: '/sale-items/login', name: 'Login', component: Login },
    { path: '/verify-email', name: 'VerifyEmail', component: VerifyEmail },
  ],
})

export default router

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth) {
    if (!auth.isLoggedIn) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }

    const allowedRoles = to.meta.roles
    if (allowedRoles && !allowedRoles.includes(auth.role)) {
      return { name: 'Home' }
    }
  }

  return true
})


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
import VerifyEmail from '@/views/AuthUser/VerifyEmail.vue'
import { useAuthStore } from '@/stores/auth'


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

router.beforeEach(async (to) => {
  const auth = useAuthStore();

  if (to.meta.requiresAuth) {
    if (!auth.accessToken) {
      const refreshed = await auth.refreshToken();
      if (!refreshed) {
        return { name: 'Login', query: { redirect: to.fullPath } };
      }
    }

    const allowedRoles = to.meta.roles;
    if (allowedRoles && !allowedRoles.includes(auth.role)) {
      return { name: 'Home' };
    }
  }

  return true;
});

export default router


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
import VerifyEmail from '@/views/AuthUser/VerlfyEmail.vue'
import UserProfile from '@/views/User/UserProfile.vue'
import { useAuthStore } from '@/stores/auth'




const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Buyer หรือ Public
    { path: '/', name: 'Home', component: HomePage },
    { path: '/sale-items', name: 'Products', component: ProductList },
    { path: '/sale-items/:id', name: 'Detail', component: ProductDetail },

    // Seller
    { path: '/sale-items/:id/edit', name: 'Edit', component: ProuctEdit, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },
    { path: '/sale-items/create', name: 'ProuctCreate', component: ProuctCreate, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },
    { path: '/sale-items/list', name: 'ProductManage', component: ProductManage, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },
    { path: '/brands', name: 'BrandManage', component: BrandManage, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },
    { path: '/brands/add', name: 'BrandCreate', component: BrandCreate, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },
    { path: '/brands/:id/edit', name: 'BrandEdit', component: BrandEdit, meta: { requiresAuth: true, roles: ['ROLE_SELLER'] } },

    // Auth
    { path: '/register', name: 'Register', component: Register },
    { path: '/signin', name: 'Login', component: Login },
    { path: '/verify-email', name: 'VerifyEmail', component: VerifyEmail },

    //User
    {path: '/profile', name: 'UserProfile', component: UserProfile , meta: { requiresAuth: true, roles: ['ROLE_BUYER', 'ROLE_SELLER'] }},
    {path: "/profile/edit", name: "UserProfileEdit", component: UserProfile, meta: { requiresAuth: true, roles: ["ROLE_BUYER", "ROLE_SELLER"] }},

    // Unknow-Path -> Home Page
    { path: '/:pathMatch(.*)*', redirect: { name: 'Products' } },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  const guestOnlyRoutes = ['Login', 'Register', 'VerifyEmail'];
  if (auth.accessToken && guestOnlyRoutes.includes(to.name)) {
    return { name: 'Products' }; // redirect ไปหน้า Home (หรือจะไปหน้า Profile ก็ได้)
  }

  // ถ้าต้อง auth
  if (to.meta.requiresAuth) {
    // ไม่มี accessToken -> ลอง refresh
    if (!auth.accessToken) {
      const refreshed = await auth.refreshToken();
      if (!refreshed) {
        return { name: 'Login', query: { redirect: to.fullPath } };
      }
    }

    // ตรวจสอบ role
    const allowedRoles = to.meta.roles || [];
    if (allowedRoles.length > 0 && !allowedRoles.includes(auth.role)) {
        // ไม่ตรง role -> redirect ไปหน้า Home
      return { name: 'Login' };
    }
  }
  return true;
});

export default router
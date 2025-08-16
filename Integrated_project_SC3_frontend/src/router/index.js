
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
import Test_SaleItem_createEdit from '@/components/SaleItemComponent/Test_SaleItem_create&edit.vue'
import TestImageUploader from '@/components/SaleItemComponent/Test-image-uploader.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomePage
    },
    {
      path: '/sale-items',
      name: 'Products',
      component: ProductList
    },
    {
      path: '/sale-items/:id',
      name: 'Detail',
      component: ProductDetail
    },
    {
      path: '/sale-items/:id/edit',
      name: 'Edit',
      component: ProuctEdit,
      props: true
    },
    {
      path: '/sale-items/create',
      name: 'ProuctCreate',
      component: ProuctCreate

    },
    {
      path: '/sale-items/list',
      name: 'ProductManage',
      component: ProductManage
    },
    {
      path: '/brands/add',
      name: 'BrandCreate',
      component: BrandCreate
    },
    {
      path: '/brands',
      name: 'BrandManage',
      component: BrandManage

    },
    {
      path: '/brands/:id/edit',
      name: 'BrandEdit',
      component: BrandEdit
    },
    {
      path: '/sale-items/test',
      name: 'test_BrandEdit',
      component: Test_SaleItem_createEdit
    },
    {
      path: '/sale-items/testImage',
      name: 'test_image_uploader',
      component: TestImageUploader
    }
  ],
})

export default router

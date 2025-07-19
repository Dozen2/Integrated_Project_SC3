<script setup>
import BrandCreateAndEdit from '@/components/BrandComponents/BrandCreateAndEdit.vue';
import {useRoute} from 'vue-router';
const route = useRoute()
</script>
<template>
    <BrandCreateAndEdit mode="edit" :brand-id="route.params.id"></BrandCreateAndEdit>
</template>
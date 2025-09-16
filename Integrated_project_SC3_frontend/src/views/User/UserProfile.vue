<script setup>
import { ref, onMounted } from "vue";
import userDataList from '@/components/UserComponent/userDataList.vue'
import { useAuthStore } from '@/stores/auth'
import { UserRound, UserRoundPen } from "lucide-vue-next";

const auth = useAuthStore();
console.log('Current Role:', auth.role);
const userProfile = ref({});
const isEditMode = ref(false);

const mockUserProfile = {
  userType: auth.role.slice(5),
  nickName: "Sky Angel",
  fullName: "Jenny Wilson",
  email: "nutchanon@gmail.com",
     phoneNumber: "123-456-7890",
     bankName: "Bangkok Bank",
     bankAccount: "123-456-7890"
};

const summitForm = () => {
  // Logic to handle form submission
  console.log("Form submitted with data:", userProfile.value);
  isEditMode.value = false; // Exit edit mode after submission
};

const editUserProfile = () => {
     // ...
  isEditMode.value = true;
};

onMounted(async() => {
  // await userProfile.value = fetchUserProfile();
});

</script>
<template>
<div class="bg-gradient-to-br from-blue-100 via-white to-blue-200 pt-[50px] min-h-screen pb-10">
    <div class="max-w-md mx-auto bg-white rounded-xl shadow-md overflow-hidden md:max-w-4xl">
      <div class="p-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-6 flex justify-center">
          User Profile
        </h2>
       <div class="grid place-items-center">
  <div class="w-36 h-36 flex items-center justify-center rounded-full bg-blue-100 hover:bg-blue-200 transition-colors mb-[40px]">
    <UserRoundPen size="100px" color="white" />
  </div>
</div>
        <form class="flex flex-col space-y-2" @submit.prevent="summitForm">
        <userDataList label="UserType" :value="auth.role.slice(5)" />
        <userDataList label="NickName" :value="mockUserProfile.nickName" :isEditMode="isEditMode"/>
        <userDataList label="FullName" :value="mockUserProfile.fullName" :isEditMode="isEditMode"/>
        <userDataList label="Email" :value="mockUserProfile.email" :isEditMode="isEditMode"/>
     <div v-if="auth.role === 'ROLE_SELLER'" :isEditMode="isEditMode">
        <userDataList label="PhoneNumber" :value="mockUserProfile.phoneNumber" :isEditMode="isEditMode"/>
        <userDataList label="BankName" :value="mockUserProfile.bankName" :isEditMode="isEditMode"/>
        <userDataList label="BackAccount" :value="mockUserProfile.bankAccount" :isEditMode="isEditMode"/>
     </div>
     </form>
</div>
    </div>
     <div v-if="!isEditMode" class="flex mt-4 justify-center">
  <button @click="isEditMode=true" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded cursor-pointer">
    Edit
  </button>
</div>
     <div v-else class="flex mt-4 space-x-5 justify-center">
  <button @click="editUserProfile" class="bg-blue-500 hover:bg-blue-700 px-[25px] text-white font-bold py-2 rounded cursor-pointer">
    Save
  </button>
  <button @click="isEditMode=false" class=" text-gray-700 hover:bg-gray-200 border border-gray-400 py-2 px-4 rounded cursor-pointer">
    Cancel
  </button>
</div>
     
  </div>
</template>
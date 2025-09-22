<script setup>
import { ref, onMounted } from "vue";
import userDataList from '@/components/UserComponent/userDataList.vue'
import { useAuthStore } from '@/stores/auth'
import { UserRound, UserRoundPen } from "lucide-vue-next";

const auth = useAuthStore();
console.log('Current Role:', auth.role);
const userProfile = ref({});
const isEditMode = ref(false);
const phoneNumber = ref()
const bankAccount = ref()

// const mockUserProfile = {
//   userType: auth.role.slice(5),
//   nickName: "Sky Angel",
//   fullName: "Jenny Wilson",
//   email: "nutchanon@gmail.com",
//   phoneNumber: "123-456-7890",
//   bankName: "Bangkok Bank",
//   bankAccount: "123-456-7890"
// };

const maskphoneNumber = () => {
  let phomNum = userProfile.value.phoneNumber;
  // console.log("Original:", phomNum);

  let maskPhon = '';
  let count = 0; // นับตัวเลขที่เพิ่มเข้า mask

  for (let i = phomNum.length - 1; i >= 0; i--) {
    // ถ้ายังไม่ครบ 4 ตัว ให้เพิ่มตัวเลขจริง
    if (count < 4) {
      maskPhon = phomNum[i] + maskPhon; // เพิ่มด้านหน้า
      count++;
    } else {
      maskPhon = 'X' + maskPhon; // ตัวที่เหลือเป็น X
    }
  }

  // console.log("Masked:", maskPhon);
  return phoneNumber.value = maskPhon
}

const maskBankAccount = () => {
  let bankAcc = userProfile.value.bankAccount;
  // console.log("Original:", bankAcc);

  let maskbank = '';
  let count = 0; // นับตัวเลขที่เพิ่มเข้า mask

  for (let i = bankAcc.length - 1; i >= 0; i--) {
    // ถ้ายังไม่ครบ 4 ตัว ให้เพิ่มตัวเลขจริง
    if (count < 4) {
      maskbank = bankAcc[i] + maskbank; // เพิ่มด้านหน้า
      count++;
    } else {
      maskbank = 'X' + maskbank; // ตัวที่เหลือเป็น X
    }
  }

  // console.log("Masked:", maskbank);
  return bankAccount.value = maskbank
}




const summitForm = () => {
  // Logic to handle form submission

  console.log("Form submitted with data:", userProfile.value);
  isEditMode.value = false; // Exit edit mode after submission
};

const editUserProfile = async () => {
  try {
    // เรียก API update profile ผ่าน store
    await auth.updateProfile({
      fullName: userProfile.value.fullName,
      nickName: userProfile.value.nickName,
    });
    auth.refreshToken()
    isEditMode.value = false; // กลับไป view mode
    // alert("Profile updated successfully!");
  } catch (err) {
    console.error("Update profile error:", err);
    alert("Error: " + err.message);
  }
};

onMounted(async () => {
  // await userProfile.value = fetchUserProfile();
  try {
    // ดึง userId จาก token decode หรือจาก BE ตอน login
    const decoded = JSON.parse(atob(auth.accessToken.split(".")[1]));
    const userId = decoded.id;

    userProfile.value = await auth.loadUserProfile(userId);

  } catch (err) {
    console.error("Load profile error:", err);
  }
  maskphoneNumber();
  maskBankAccount();
});

</script>

<template>
  <div class="bg-gradient-to-br from-blue-50 via-white to-blue-100 min-h-screen py-10">
    <div class="max-w-2xl mx-auto bg-white rounded-2xl shadow-sm border border-blue-100">
      <div class="p-8">
        <!-- Profile Title -->
        <h2 class="text-2xl font-semibold text-blue-800 mb-8 text-center">
          User Profile
        </h2>

        <!-- Avatar -->
        <div class="flex justify-center mb-8">
          <div
            class="w-32 h-32 flex items-center justify-center rounded-full bg-gradient-to-br from-blue-400 to-blue-600 shadow-md hover:scale-105 transition-transform">
            <UserRoundPen size="80px" color="white" />
          </div>
        </div>

        <!-- User Data -->
        <form @submit.prevent="summitForm" class="space-y-4">
          <userDataList label="NickName" v-model="userProfile.nickName" :isEditMode="isEditMode" />
          <userDataList label="FullName" v-model="userProfile.fullName" :isEditMode="isEditMode" />
          <userDataList label="Email" v-model="userProfile.email" />
          <div v-if="auth.role === 'ROLE_SELLER'" class="space-y-4">
            <userDataList label="PhoneNumber" v-model="phoneNumber" />
            <userDataList label="BankName" v-model="userProfile.bankName" />
            <userDataList label="BankAccount" v-model="bankAccount" />
          </div>
        </form>
      </div>
    </div>

    <!-- Buttons -->
    <div class="flex mt-6 justify-center space-x-4">
      <template v-if="!isEditMode">
        <button @click="isEditMode = true"
          class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-6 rounded-full transition-colors">
          Edit
        </button>
      </template>
      <template v-else>
        <button @click="editUserProfile"
          class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-8 rounded-full transition-colors">
          Save
        </button>
        <button @click="isEditMode = false"
          class="bg-white text-blue-600 border border-blue-300 hover:bg-blue-50 font-medium py-2 px-6 rounded-full transition-colors">
          Cancel
        </button>
      </template>
    </div>
  </div>
</template>

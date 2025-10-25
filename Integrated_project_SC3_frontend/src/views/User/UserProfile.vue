<script setup>
import { ref, onMounted, reactive, computed } from "vue";
import userDataList from "@/components/UserComponent/userDataList.vue";
import { useAuthStore } from "@/stores/auth";
import { UserRound, UserRoundPen } from "lucide-vue-next";
import { useAlertStore } from "@/stores/alertStore";
import { useRouter } from "vue-router";
import Breadcrumb from "@/components/Common/Breadcrumb.vue";
import Loading from "@/components/Common/Loading.vue";

const route = useRouter();
const auth = useAuthStore();
const toast = useAlertStore();

const userProfile = ref({
  email: "",
  fullName: "",
  nickName: "",
  phoneNumber: "",
  bankAccount: "",
  bankName: "",
});
const originalProfile = ref({});

const isEditMode = ref(false);
const phoneNumber = ref("");
const bankAccount = ref("");
const userType = ref("");

const form = reactive({
  nickname: {
    errorText: "Nickname is required",
    isValid: false,
    isFirstInput: true,
  },
  fullname: {
    errorText: "Full name must be 4–40 characters",
    isValid: false,
    isFirstInput: true,
  },
});
const isLoading = ref(false);


onMounted(async () => {
  isLoading.value = true;
  const role = await auth.getAuthData().authorities[auth.getAuthData().authorities.length - 1].role;
  console.log("User role:", role);
  try {
    if (auth.accessToken) {
      const userId = auth.getAuthData().id;
      userType.value = role.slice(5);
      userType.value = userType.value.charAt(0).toUpperCase() + userType.value.slice(1).toLowerCase();
      userProfile.value = await auth.loadUserProfile(userId);
      originalProfile.value = {
        nickName: userProfile.value.nickName,
        fullName: userProfile.value.fullName,
      };
    }
  } catch (err) {
    console.error("Load profile error:", err);
  }
  if (role == 'ROLE_SELLER') {
    maskPhoneNumber();
    maskBankAccount();
  }

  isLoading.value = false;
});

const validateNickname = () => {
  const value = userProfile.value.nickName.trim();
  form.nickname.isValid = value.length > 0;
  updateIsFirstInput("nickname", value);
};

const validateFullname = () => {
  console.log("Validating fullname:", userProfile.value.fullName);
  const value = userProfile.value.fullName.trim();
  form.fullname.isValid = value.length >= 4 && value.length <= 40;
  updateIsFirstInput("fullname", value);
};

const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};

const summitForm = () => {
  console.log("Form submitted with data:", userProfile.value);
  isEditMode.value = false;
};

const editUserProfile = async () => {
  try {
    await auth.updateProfile({
      fullName: userProfile.value.fullName,
      nickName: userProfile.value.nickName,
    });
    await auth.refreshToken();
    originalProfile.value = {
      nickName: userProfile.value.nickName,
      fullName: userProfile.value.fullName,
    };
    changeToViewMode()
    toast.addToast(
      "The user profile has been successfully updated.",
      "Update profile successful.",
      "success",
      5000
    );
  } catch (err) {
    console.error("Update profile error:", err);
    alert("Error: " + err.message);
  }
};

const isDataChanged = computed(() => {
  return (
    userProfile.value.nickName !== originalProfile.value.nickName ||
    userProfile.value.fullName !== originalProfile.value.fullName
  );
});

const isFormValid = computed(() => {
  if (!isDataChanged.value) {
    console.log("No data changed - cannot save");
    return false;
  }
  let changedFieldsValid = true;

  if (userProfile.value.nickName !== originalProfile.value.nickName) {
    changedFieldsValid = changedFieldsValid && form.nickname.isValid;
  }
  if (userProfile.value.fullName !== originalProfile.value.fullName) {
    changedFieldsValid = changedFieldsValid && form.fullname.isValid;
  }
  return changedFieldsValid;
});

const maskValue = (value) => {
  const firstPart = "x".repeat(value.length - 4);
  const middle = value.slice(-4, -1);
  return firstPart + middle + "x";
};

const maskPhoneNumber = () => {
  phoneNumber.value = maskValue(userProfile.value.phoneNumber);
};

const maskBankAccount = () => {
  bankAccount.value = maskValue(userProfile.value.bankAccount);
};

const cancelButton = () => {
  userProfile.value.nickName = originalProfile.value.nickName;
  userProfile.value.fullName = originalProfile.value.fullName;
  form.nickname.isValid = false;
  form.nickname.isFirstInput = true;
  form.fullname.isValid = false;
  form.fullname.isFirstInput = true;
  changeToViewMode()

};

const changeToEditMode = () => {
  route.push({ name: "UserProfileEdit" });
  isEditMode.value = true
};

const changeToViewMode = () => {
  route.push({ name: "UserProfile" });
  isEditMode.value = false;
}
</script>

<template>
  <div v-if="isLoading" class="flex items-center justify-center min-h-[calc(100vh-80px)]">
    <Loading/>  
  </div>
  <div v-else
    class=" max-w-2xl mx-auto min-h-[calc(100vh-80px)] py-10"
  >
  <Breadcrumb
  :class="'mb-6'"
  :pathForBreadcrumb="[
    { text: 'Home', name: 'Home' },
    { text: 'SaleItem', name: 'Products' },
    { text: 'Profile', name: 'UserProfile' }
  ]" />
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

          <userDataList classname="itbms-nickname" label="NickName" v-model="userProfile.nickName" :isEditMode="isEditMode"
            :isValid="form.nickname.isValid" :isFirstInput="form.nickname.isFirstInput"
            :errorText="form.nickname.errorText" @validateValue="validateNickname" />
            
          <userDataList 
            classname="itbms-email" 
            label="Email" 
            v-model="userProfile.email"/>

          <userDataList classname="itbms-fullname" label="FullName" v-model="userProfile.fullName" :isEditMode="isEditMode"
            :isValid="form.fullname.isValid" :isFirstInput="form.fullname.isFirstInput"
            :errorText="form.fullname.errorText" @validateValue="validateFullname" />

          <userDataList classname="itbms-type" label="Type" v-model="userType" />

          <div v-if="auth.role === 'ROLE_SELLER'" class="space-y-4">
            <userDataList classname="itbms-mobile" label="Mobile" v-model="phoneNumber"/>

            <userDataList classname="itbms-bankAccount" label="Bank Account No" v-model="bankAccount"/>

            <userDataList classname="itbms-bankName" label="Bank Name" v-model="userProfile.bankName"/>
          </div>
        </form>
      </div>
    </div>

    <!-- Buttons -->
    <div class="flex mt-6 justify-center space-x-4">
      <template v-if="!isEditMode">

        <button @click="changeToEditMode"
          class="itbms-profile-button bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-6 rounded-full transition-colors cursor-pointer">
          Edit profile
        </button>

        <RouterLink :to="{name: 'chang-password'}">
          chang-password
        </RouterLink>

      </template>
      <template v-else>

        <button @click="editUserProfile" :disabled="!isFormValid" :class="[
          'py-2 rounded-lg transition',
          !isFormValid
            ? 'bg-gray-300 hover:bg-gray-500 text-white font-medium py-2 px-8 rounded-full transition-colors cursor-not-allowed'
            : 'bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-8 rounded-full transition-colors cursor-pointer',
        ]" class="itbms-save-button">
          Save
        </button>

        <button @click="cancelButton"
          class="itbms-cancel-button bg-white text-blue-600 border border-blue-300 hover:bg-blue-50 font-medium py-2 px-6 rounded-full transition-colors cursor-pointer">
          Cancel
        </button>
      </template>
    </div>
  </div>
</template>

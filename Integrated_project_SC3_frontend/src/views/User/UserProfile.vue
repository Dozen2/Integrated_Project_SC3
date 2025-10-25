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
    <Loading />
  </div>

  <div v-else class="py-8 max-w-4xl mx-auto">
    <Breadcrumb :class="'mb-6'" :pathForBreadcrumb="[
      { text: 'Home', name: 'Home' },
      { text: 'SaleItem', name: 'Products' },
      { text: 'Profile', name: 'UserProfile' }
    ]" />

    <div class="grid md:grid-cols-3 gap-8">

      <div class="md:col-span-1 space-y-4">

        <div class="bg-card border rounded-xl p-6 text-center">
          <div class="flex justify-center mb-4">
            <div
              class="w-24 h-24 flex items-center justify-center rounded-full bg-gradient-to-br from-blue-400 to-blue-600 shadow-md hover:scale-105 transition-transform">
              <UserRoundPen size="60px" color="white" />
            </div>
          </div>
          <h3>{{ userProfile.fullName || userProfile.nickName }}</h3>
          <p class="text-sm text-muted-foreground mt-1">{{ userProfile.email }}</p>
        </div>

        <div class="bg-card border rounded-xl p-4 space-y-2">
          <RouterLink :to="{ name: 'chang-password' }" class="block">
            <button
              class="w-full justify-start text-left py-2 px-4 hover:bg-accent hover:text-accent-foreground rounded-md">
              Change Password
            </button>
          </RouterLink>
        </div>
      </div>

      <div class="md:col-span-2">

        <div class="bg-card border rounded-xl p-6">

          <div class="flex items-center justify-between mb-6">
            <h2>Personal Information</h2>

            <template v-if="!isEditMode">
              <button @click="changeToEditMode"
                class="itbms-profile-button bg-primary text-primary-foreground hover:bg-primary/90 py-2 px-4 rounded-md transition-colors cursor-pointer">
                Edit
              </button>
            </template>
            <template v-else>
              <div class="flex gap-2">
                <button @click="cancelButton"
                  class="itbms-cancel-button bg-transparent border border-input hover:bg-accent hover:text-accent-foreground py-2 px-4 rounded-md transition-colors cursor-pointer">
                  Cancel
                </button>
                <button @click="editUserProfile" :disabled="!isFormValid" :class="[
                  'itbms-save-button py-2 px-4 rounded-md transition-colors',
                  !isFormValid
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-primary text-primary-foreground hover:bg-primary/90 cursor-pointer',
                ]">
                  Save
                </button>
              </div>
            </template>
          </div>

          <form @submit.prevent="summitForm" class="space-y-4">
            <userDataList classname="itbms-nickname" label="NickName" v-model="userProfile.nickName"
              :isEditMode="isEditMode" :isValid="form.nickname.isValid" :isFirstInput="form.nickname.isFirstInput"
              :errorText="form.nickname.errorText" @validateValue="validateNickname" />

            <userDataList classname="itbms-email" label="Email" :isEditMode="isEditMode" v-model="userProfile.email"
              :disabled="true" />

            <userDataList classname="itbms-fullname" label="FullName" v-model="userProfile.fullName"
              :isEditMode="isEditMode" :isValid="form.fullname.isValid" :isFirstInput="form.fullname.isFirstInput"
              :errorText="form.fullname.errorText" @validateValue="validateFullname" />

            <userDataList classname="itbms-type" label="Type" v-model="userType" />

            <div v-if="auth.role === 'ROLE_SELLER'" class="space-y-4">
              <userDataList classname="itbms-mobile" label="Mobile" v-model="phoneNumber" :isEditMode="isEditMode"
                :disabled="true" />

              <userDataList classname="itbms-bankAccount" label="Bank Account No" v-model="bankAccount"
                :isEditMode="isEditMode" :disabled="true" />

              <userDataList classname="itbms-bankName" label="Bank Name" v-model="userProfile.bankName"
                :isEditMode="isEditMode" :disabled="true" />
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>
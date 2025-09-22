<script setup>
import { ref, onMounted, reactive, computed } from "vue";
import userDataList from "@/components/UserComponent/userDataList.vue";
import { useAuthStore } from "@/stores/auth";
import { UserRound, UserRoundPen } from "lucide-vue-next";

const auth = useAuthStore();

const userProfile = ref({});
const originalProfile = ref({});

const isEditMode = ref(false);
const phoneNumber = ref("");
const bankAccount = ref("");

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

onMounted(async () => {
  try {
    if (auth.accessToken) {
      const decoded = JSON.parse(atob(auth.accessToken.split(".")[1]));
      const userId = decoded.id;
      userProfile.value = await auth.loadUserProfile(userId);
      originalProfile.value = {
        nickName: userProfile.value.nickName,
        fullName: userProfile.value.fullName,
      };
    }
  } catch (err) {
    console.error("Load profile error:", err);
  }
  maskPhoneNumber();
  maskBankAccount();
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
    isEditMode.value = false;
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
    console.log("Nickname changed, valid:", form.nickname.isValid);
  }
  if (userProfile.value.fullName !== originalProfile.value.fullName) {
    changedFieldsValid = changedFieldsValid && form.fullname.isValid;
    console.log("Fullname changed, valid:", form.fullname.isValid);
  }
  return changedFieldsValid;
});

const maskPhoneNumber = () => {
  if (!userProfile.value.phoneNumber) return;
  let raw = userProfile.value.phoneNumber;
  let masked = "";
  let count = 0;
  for (let i = raw.length - 1; i >= 0; i--) {
    if (count < 4) {
      masked = raw[i] + masked;
      count++;
    } else {
      masked = "X" + masked;
    }
  }
  phoneNumber.value = masked;
};

const maskBankAccount = () => {
  if (!userProfile.value.bankAccount) return;
  let raw = userProfile.value.bankAccount;
  let masked = "";
  let count = 0;
  for (let i = raw.length - 1; i >= 0; i--) {
    if (count < 4) {
      masked = raw[i] + masked;
      count++;
    } else {
      masked = "X" + masked;
    }
  }
  bankAccount.value = masked;
};
</script>

<template>
  <div
    class="bg-gradient-to-br from-blue-50 via-white to-blue-100 min-h-screen py-10"
  >
    <div
      class="max-w-2xl mx-auto bg-white rounded-2xl shadow-sm border border-blue-100"
    >
      <div class="p-8">
        <!-- Profile Title -->
        <h2 class="text-2xl font-semibold text-blue-800 mb-8 text-center">
          User Profile
        </h2>

        <!-- Avatar -->
        <div class="flex justify-center mb-8">
          <div
            class="w-32 h-32 flex items-center justify-center rounded-full bg-gradient-to-br from-blue-400 to-blue-600 shadow-md hover:scale-105 transition-transform"
          >
            <UserRoundPen size="80px" color="white" />
          </div>
        </div>

        <!-- User Data -->
        <form @submit.prevent="summitForm" class="space-y-4">
          <userDataList
            label="NickName"
            v-model="userProfile.nickName"
            :isEditMode="isEditMode"
            :isValid="form.nickname.isValid"
            :isFirstInput="form.nickname.isFirstInput"
            :errorText="form.nickname.errorText"
            @validateValue="validateNickname"
          />
          <userDataList
            label="FullName"
            v-model="userProfile.fullName"
            :isEditMode="isEditMode"
            :isValid="form.fullname.isValid"
            :isFirstInput="form.fullname.isFirstInput"
            :errorText="form.fullname.errorText"
            @validateValue="validateFullname"
          />
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
        <button
          @click="isEditMode = true"
          class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-6 rounded-full transition-colors cursor-pointer"
        >
          Edit
        </button>
      </template>
      <template v-else>
        <button
          @click="editUserProfile"
          :disabled="!isFormValid"
          :class="[
            'py-2 rounded-lg transition',
            !isFormValid
              ? 'bg-gray-300 hover:bg-gray-500 text-white font-medium py-2 px-8 rounded-full transition-colors cursor-not-allowed'
              : 'bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-8 rounded-full transition-colors cursor-pointer',
          ]"
        >
          Save
        </button>
        <button
          @click="isEditMode = false"
          class="bg-white text-blue-600 border border-blue-300 hover:bg-blue-50 font-medium py-2 px-6 rounded-full transition-colors cursor-pointer"
        >
          Cancel
        </button>
      </template>
    </div>
  </div>
</template>

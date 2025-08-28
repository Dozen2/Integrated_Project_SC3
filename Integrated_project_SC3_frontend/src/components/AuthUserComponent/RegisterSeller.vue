<script setup>
import { ref, reactive, computed } from "vue";
import InputBox from "./../Common/InputBox.vue";
import { RouterLink, useRouter } from "vue-router";
import { registerUser } from "@/libs/callAPI/apiAuth";
import { useAlertStore } from "@/stores/alertStore.js";

const alertStore = useAlertStore();
const route = useRouter();


const nickName = ref("");
const fullName = ref("");
const email = ref("");
const password = ref("");
const bankAccount = ref("");
const nationalId = ref("");
const phoneNumber = ref("");
const nationalIdFront = ref(null);
const nationalIdBack = ref(null);
const bank = ref("");

const nationalIdFrontPreview = ref(null);
const nationalIdBackPreview = ref(null);

// รวมทุก state ไว้ใน form
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
  email: {
    errorText: "Invalid email format",
    isValid: false,
    isFirstInput: true,
  },
  password: {
    errorText:
      "Password must be 8+ chars with uppercase, lowercase, number, special char",
    isValid: false,
    isFirstInput: true,
  },
  bankAccount: {
    errorText: "Bank account must be at least 6 digits",
    isValid: false,
    isFirstInput: true,
  },
  nationalId: {
    errorText: "National ID must be 13 digits",
    isValid: false,
    isFirstInput: true,
  },
  phoneNumber: {
    errorText: "Phone number is required",
    isValid: false,
    isFirstInput: true,
  },
  bank: { errorText: "Hello eiei", isValid: false, isFirstInput: true },
  nationalIdFront: {
    errorText: "Hello eiei",
    isValid: false,
    isFirstInput: true,
  },
  nationalIdBack: {
    errorText: "Hello eiei",
    isValid: false,
    isFirstInput: true,
  },
});

const isFormValid = computed(() => {
  const results = Object.entries(form).map(([key, f]) => ({
    field: key,
    isValid: f.isValid ?? null, // ถ้าไม่มี isValid จะได้ null
    isFirstInput: f.isFirstInput ?? null,
  }));

  console.table(results); 

  return results.filter((r) => r.isValid !== null).every((r) => r.isValid);
});

// =================== Validation ===================
const validateNickname = () => {
  form.nickname.isValid = nickName.value.trim().length > 0;
  updateIsFirstInput("nickname", nickName.value);
};

const validateFullname = () => {
  const len = fullName.value.trim().length;
  form.fullname.isValid = len >= 4 && len <= 40;
  updateIsFirstInput("fullname", fullName.value);
};

const validateEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  form.email.isValid = regex.test(email.value);
  updateIsFirstInput("email", email.value);
};

const validatePassword = () => {
  form.password.isValid =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(
      password.value
    );
  updateIsFirstInput("password", password.value);
};

const validateBankAccount = () => {
  form.bankAccount.isValid =
    /^[0-9]+$/.test(bankAccount.value) && bankAccount.value.length >= 6;
  updateIsFirstInput("bankAccount", bankAccount.value);
};

const validateNationalId = () => {
  form.nationalId.isValid = /^[0-9]{13}$/.test(nationalId.value);
  updateIsFirstInput("nationalId", nationalId.value);
};

const validatePhoneNumber = () => {
  form.phoneNumber.isValid = phoneNumber.value.trim().length > 0;
  updateIsFirstInput("phoneNumber", phoneNumber.value);
};


// =================== Summit Form ===================
// Update isFirstInput
const updateIsFirstInput = (field, value) => {
  form[field].isFirstInput = form[field].isFirstInput && value === "";
};


const loading = ref(false);

// Submit
const summitForm = async () => {
    try {
      const userData = {
        nickName: nickName.value,
        fullName: fullName.value,
        email: email.value,
        passwords: password.value,
        role: "SELLER",
        mobileNumber: phoneNumber.value,
        bankAccountNumber: bankAccount.value,
        bankName: bank.value,
        nationalId: nationalId.value,
      }

      loading.value = true
      const res = await registerUser(
        userData, 
        nationalIdFront.value, 
        nationalIdBack.value
      )
      loading.value = false

      console.log("✅ Registered success:", res)
      alertStore.addToast("The user account has been successfully registered.", "Create seller successful.", "success",5000);
      route.push({ name: "Products" });
    } catch (err) {
      alertStore.addToast(err.message, "Register failed", "error");
  }
}

function handleFileChangeFront(e) {
  const file = e.target.files[0]
  if (file) {
    nationalIdFront.value = file
    form.nationalIdFront.isValid = true
    nationalIdFrontPreview.value = URL.createObjectURL(file);
  }
}
function handleFileChangeBack(e) {
  const file = e.target.files[0]
  if (file) {
    nationalIdBack.value = file
    form.nationalIdBack.isValid = true
    nationalIdBackPreview.value = URL.createObjectURL(file);
  }
}
const frontInput = ref(null);
const removeFrontImage = () => {
  nationalIdFront.value = null;
  form.nationalIdFront.isValid = false
  nationalIdFrontPreview.value = null;

  if (frontInput.value) frontInput.value.value = null;
};

const backInput = ref(null);
const removeBackImage = () => {
  nationalIdBack.value = null;
  form.nationalIdBack.isValid = false
  nationalIdBackPreview.value = null;

  if (backInput.value) backInput.value.value = null;
};


</script>

  <template>
    <div class="min-h-screen flex items-center justify-center bg-blue-50">
      <div class="bg-white shadow-xl rounded-2xl p-6 w-full max-w-lg m-10">
        <!-- Title -->
        <h2 class="text-2xl font-bold text-center text-blue-700 mb-4">
          Seller Sign Up
        </h2>

        <!-- Form -->
        <form class="flex flex-col space-y-2" @submit.prevent="summitForm">
          <InputBox
            label="Nickname"
            placeholder="Enter nickname"
            v-model="nickName"
            :isValid="form.nickname.isValid"
            :isFirstInput="form.nickname.isFirstInput"
            :errorText="form.nickname.errorText"
            @validateValue="validateNickname"
          />

          <InputBox
            label="Full Name"
            placeholder="Enter full name"
            v-model="fullName"
            :isValid="form.fullname.isValid"
            :isFirstInput="form.fullname.isFirstInput"
            :errorText="form.fullname.errorText"
            @validateValue="validateFullname"
          />
          <InputBox
            label="Email"
            placeholder="Enter email"
            v-model="email"
            :isValid="form.email.isValid"
            :isFirstInput="form.email.isFirstInput"
            :errorText="form.email.errorText"
            @validateValue="validateEmail"
          />

          <InputBox
            label="Password"
            type="password"
            placeholder="Enter password"
            v-model="password"
            :isValid="form.password.isValid"
            :isFirstInput="form.password.isFirstInput"
            :errorText="form.password.errorText"
            @validateValue="validatePassword"
          />

          <InputBox
            label="Phone"
            type="tel"
            placeholder="Enter phone number"
            v-model="phoneNumber"
            :isValid="form.phoneNumber.isValid"
            :isFirstInput="form.phoneNumber.isFirstInput"
            :errorText="form.phoneNumber.errorText"
            @validateValue="validatePhoneNumber"
          />

          <div class="flex flex-col space-y-1">
            <label class="text-sm font-medium text-blue-600">Bank</label>
            <select
              class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
              v-model="bank"
              @change="
                form.bank.isValid = bank !== '';
                updateIsFirstInput('bank', bank);
              "
            >
              <option value="" disabled selected hidden>Select Bank</option>
              <option value="kbank">Kasikorn Bank</option>
              <option value="scb">Siam Commercial Bank</option>
              <option value="bbl">Bangkok Bank</option>
              <option value="ktb">Krungthai Bank</option>
            </select>
          </div>

          <InputBox
            label="Bank Account"
            placeholder="Enter bank account number"
            v-model="bankAccount"
            :isValid="form.bankAccount.isValid"
            :isFirstInput="form.bankAccount.isFirstInput"
            :errorText="form.bankAccount.errorText"
            @validateValue="validateBankAccount"
          />

          <InputBox
            label="National ID"
            placeholder="Enter national ID"
            v-model="nationalId"
            :isValid="form.nationalId.isValid"
            :isFirstInput="form.nationalId.isFirstInput"
            :errorText="form.nationalId.errorText"
            @validateValue="validateNationalId"
          />

          <!-- Upload National ID card -->
          <div class="flex flex-col space-y-1">
            <label class="text-sm font-medium text-gray-600">National ID Card - Front</label>
            <input
              type="file"
              accept="image/*"
              class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
              @change="handleFileChangeFront"
              ref="frontInput"
            />

            <div v-if="nationalIdFrontPreview" class="mt-2 relative">
              <img :src="nationalIdFrontPreview" alt="Front Preview" class="w-40 rounded-lg border" />
              <button
                type="button"
                @click="removeFrontImage"
                class="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs"
              >
                ✕
              </button>
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <label class="text-sm font-medium text-gray-600">National ID Card - Back</label>
            <input
              type="file"
              accept="image/*"
              class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
              @change="handleFileChangeBack"
              ref="backInput"
            />
          
            <div v-if="nationalIdBackPreview" class="mt-2 relative">
              <img :src="nationalIdBackPreview" alt="Back Preview" class="w-40 rounded-lg border" />
              <button
                type="button"
                @click="removeBackImage"
                class="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs"
              >
                ✕
              </button>
            </div>
          </div>


          <!-- Buttons -->
          <div class="flex flex-col space-y-3 mt-4">
            <button
            type="submit"
            :disabled="loading || !isFormValid"
            :class="[
              'w-full py-2 rounded-lg transition',
              loading || !isFormValid
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700',
            ]"
          >
            {{ loading ? "Loading..." : "Sign Up" }}
          </button>
            <RouterLink
              :to="{ name: 'Products' }"
              class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition"
            >
              Cancel
            </RouterLink>
          </div>

          <!-- Already have account -->
          <p class="text-center text-sm text-gray-600 mt-4">
            Already have an account?
            <RouterLink
              :to="{ name: 'Login' }"
              class="text-blue-600 font-medium hover:underline"
            >
              Login
            </RouterLink>
          </p>
        </form>
      </div>
    </div>
  </template>

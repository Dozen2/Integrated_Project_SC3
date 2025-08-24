<script setup>
import { ref } from "vue"

const form = ref({
    nickname: "",
    fullname: "",
    email: "",
    password: "",
    shopName: "",
    phone: "",
    bank: "",
    bankAccount: "",
    nationalId: "",
    nationalIdFront: null,
    nationalIdBack: null,
})

const previewFront = ref(null)
const previewBack = ref(null)

const submittedData = ref(null)

const handleFileChange = (event, type) => {
    const file = event.target.files[0]
    if (file) {
        if (type === "front") {
            form.value.nationalIdFront = file
            previewFront.value = URL.createObjectURL(file)
        } else if (type === "back") {
            form.value.nationalIdBack = file
            previewBack.value = URL.createObjectURL(file)
        }
    }
}

const handleSignUp = () => {
    submittedData.value = { ...form.value }
}


</script>

<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-blue-50">
        <div class="bg-white shadow-xl rounded-2xl p-6 w-full max-w-lg m-10">
            <!-- Title -->
            <h2 class="text-2xl font-bold text-center text-blue-700 mb-4">
                Seller Sign Up
            </h2>

            <!-- Form -->
            <form class="flex flex-col space-y-2" @submit.prevent="handleSignUp">
                <!-- Nickname -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Nickname</label>
                    <input v-model="form.nickname" type="text" placeholder="Enter nickname"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Fullname -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Full Name</label>
                    <input v-model="form.fullname" type="text" placeholder="Enter full name"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Email -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Email</label>
                    <input v-model="form.email" type="email" placeholder="Enter email"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Password -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Password</label>
                    <input v-model="form.password" type="password" placeholder="Enter password"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Shop name -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Shop Name</label>
                    <input v-model="form.shopName" type="text" placeholder="Enter shop name"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Phone -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Phone</label>
                    <input v-model="form.phone" type="tel" placeholder="Enter phone number"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Bank -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Bank</label>
                    <select v-model="form.bank"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400">
                        <option value="">Select bank</option>
                        <option value="kbank">Kasikorn Bank</option>
                        <option value="scb">Siam Commercial Bank</option>
                        <option value="bbl">Bangkok Bank</option>
                        <option value="ktb">Krungthai Bank</option>
                    </select>
                </div>

                <!-- Bank Account -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Bank Account</label>
                    <input v-model="form.bankAccount" type="text" placeholder="Enter bank account number"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- National ID -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID</label>
                    <input v-model="form.nationalId" type="text" placeholder="Enter national ID"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- National ID Card - Front -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID Card - Front</label>
                    <input type="file" accept="image/*" @change="handleFileChange($event, 'front')"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                    <img v-if="previewFront" :src="previewFront" alt="Front Preview"
                        class="w-48 h-32 object-cover rounded-lg mt-2 border" />
                </div>

                <!-- National ID Card - Back -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID Card - Back</label>
                    <input type="file" accept="image/*" @change="handleFileChange($event, 'back')"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                    <img v-if="previewBack" :src="previewBack" alt="Back Preview"
                        class="w-48 h-32 object-cover rounded-lg mt-2 border" />
                </div>

                <!-- Buttons -->
                <div class="flex flex-col space-y-3 mt-4">
                    <button type="submit"
                        class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
                        Sign Up
                    </button>
                    <RouterLink :to="{ name: 'Home' }"
                        class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition">
                        Cancel
                    </RouterLink>
                </div>
            </form>
        </div>

        <!-- แสดงผลข้อมูลหลังจาก Sign Up -->
        <div v-if="submittedData" class="bg-white shadow-lg rounded-xl p-6 w-full max-w-lg mt-6">
            <h3 class="text-lg font-bold mb-4 text-blue-700">Submitted Data</h3>
            <pre class="bg-gray-100 p-3 rounded-lg text-sm overflow-x-auto">
        {{ submittedData }}
      </pre>
        </div>
    </div>
</template>

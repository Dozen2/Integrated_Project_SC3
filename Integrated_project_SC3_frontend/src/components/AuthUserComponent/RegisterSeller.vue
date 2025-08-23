<script setup>
</script>
<template>
    <div class="min-h-screen flex items-center justify-center bg-blue-50">
        <div class="bg-white shadow-xl rounded-2xl p-6 w-full max-w-lg m-10">
            <!-- Title -->
            <h2 class="text-2xl font-bold text-center text-blue-700 mb-4">
                Seller Sign Up
            </h2>

            <!-- Form -->
            <form class="flex flex-col space-y-2">
                <!-- Nickname -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Nickname</label>
                    <input type="text" placeholder="Enter nickname"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Fullname -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Full Name</label>
                    <input type="text" placeholder="Enter full name"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Email -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Email</label>
                    <input type="email" placeholder="Enter email"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Password -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Password</label>
                    <input type="password" placeholder="Enter password"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Shop name -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Shop Name</label>
                    <input type="text" placeholder="Enter shop name"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Phone -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Phone</label>
                    <input type="tel" placeholder="Enter phone number"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Bank -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">Bank</label>
                    <select
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
                    <input type="text" placeholder="Enter bank account number"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- National ID -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID</label>
                    <input type="text" placeholder="Enter national ID"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <!-- Upload National ID card -->
                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID Card - Front</label>
                    <input type="file" accept="image/*"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
                </div>

                <div class="flex flex-col space-y-1">
                    <label class="text-sm font-medium text-gray-600">National ID Card - Back</label>
                    <input type="file" accept="image/*"
                        class="w-full border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400" />
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

                <!-- Already have account -->
                <p class="text-center text-sm text-gray-600 mt-4">
                    Already have an account?
                    <RouterLink :to="{name:'Login'}" class="text-blue-600 font-medium hover:underline">
                        Login
                    </RouterLink>
                </p>
            </form>
        </div>
    </div>
</template>

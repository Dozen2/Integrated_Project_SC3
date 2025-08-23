<script setup>

</script>
<template>
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 via-white to-blue-200">
        <!-- กล่องหลัก -->
        <div class="bg-white shadow-xl rounded-2xl w-full max-w-4xl flex overflow-hidden">
            <!-- ฝั่งซ้าย: Login Form -->
            <div class="w-1/2 p-10 flex flex-col justify-center">
                <!-- Title -->
                <h2 class="text-2xl font-bold text-blue-700 mb-6">Login to Your Account</h2>

                <!-- Form -->
                <form class="flex flex-col gap-4">
                    <!-- Email -->
                    <div>
                        <label class="block text-sm font-medium text-blue-800 mb-1">Email</label>
                        <input 
                        type="email" 
                        placeholder="Enter your email"
                        class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <!-- Password -->
                    <div>
                        <label class="block text-sm font-medium text-blue-800 mb-1">Password</label>
                        <input 
                        type="password" 
                        placeholder="Enter your password"
                        class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <!-- Buttons -->
                    <div class="flex flex-col gap-3 mt-4">
                        <button 
                            type="submit"
                            class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
                            Login
                        </button>

                        <RouterLink :to="{ name: 'Products' }"
                            class="w-full text-center border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-100 transition">
                            Cancel
                        </RouterLink>

                        <RouterLink :to="{ name: 'Register' }"
                            class="w-full text-center  text-blue-700 py-2 rounded-lg hover:bg-blue-200 transition">
                            Create Account
                        </RouterLink>
                    </div>
                </form>
            </div>

            <!-- ฝั่งขวา: Logo -->
            <div class="w-1/2 bg-blue-50 flex items-center justify-center p-10">
                <div class="flex flex-col items-center">
                    <!-- ใช้ svg/รูปแทน logo -->
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-28 w-28 text-blue-600 mb-4" fill="none"
                        viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
                    </svg>
                    <h1 class="text-2xl font-bold text-blue-700">ITBMS_SHOP</h1>
                </div>
            </div>
        </div>
    </div>
</template>

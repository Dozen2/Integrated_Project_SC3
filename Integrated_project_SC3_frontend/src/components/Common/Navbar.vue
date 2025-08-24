<script setup>
import { Smartphone } from "lucide-vue-next";
import { ref } from "vue";

const showMenu = ref(false);
const toggleMenu = () => (showMenu.value = !showMenu.value);

</script>

<template>
  <div
    class="bg-blue-800 h-20 flex items-center justify-between px-8 border-b border-slate-600 text-slate-300 font-sans"
  >
    <RouterLink to="/" class="flex items-center gap-3 group">
     <Smartphone />
      <h1
        class="font-bold text-2xl tracking-wide text-white group-hover:text-sky-400 transition-colors duration-300"
      >
        ITBMS_SHOP
      </h1>
    </RouterLink>

    <div class="flex-grow max-w-lg mx-8">
      <div class="relative">
        <input
          type="text"
          placeholder="Search for products..."
          class="w-full bg-blue-300 border border-slate-700 rounded-full py-2 pl-10 pr-4 text-black focus:outline-none focus:border-sky-500 transition-colors"
        />
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-500"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"
          />
        </svg>
      </div>
    </div>

    <div class="flex items-center gap-8">
      <div class="flex items-center gap-6 text-lg font-medium">
        <RouterLink :to="{ name: 'Login' }">
          <span
            class="cursor-pointer hover:text-sky-400 transition-colors duration-300"
            >Login</span
          >
        </RouterLink>

        <RouterLink :to="{ name: 'Register' }">
          <span
            class="cursor-pointer bg-sky-500 text-white font-semibold px-4 py-2 rounded-md hover:bg-sky-600 transition-all duration-300 shadow-lg shadow-sky-500/20"
          >
            Register
          </span></RouterLink
        >
      </div>

      <div class="flex items-center gap-6">
        <div class="relative inline-block">
          <!-- ปุ่มหลัก -->
          <button
            @click="toggleMenu"
            class="p-2 rounded-full   bg-blue-100 hover:bg-blue-200 transition-colors"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              height="28"
              viewBox="0 -960 960 960"
              width="28"
              fill="#1d4ed8"
            >
              <path
                d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Z"
              />
            </svg>
          </button>

          <!-- เมนู Login/Signup -->
          <div
            v-if="showMenu"
            class="absolute right-0 mt-3 w-40 bg-white border border-gray-100 rounded-xl shadow-lg overflow-hidden z-50"
          >
            <RouterLink
              :to="{ name: 'Login' }"
              class="flex items-center gap-2 w-full px-4 py-2 text-blue-600 hover:bg-blue-50 transition-colors"
              @click="toggleMenu"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5"
                viewBox="0 -960 960 960"
                fill="currentColor"
              >
                <path
                  d="M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Zm-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z"
                />
              </svg>
              <span class="font-medium">Login</span>
            </RouterLink>

            <RouterLink
              :to="{ name: 'Register' }"
              class="flex items-center gap-2 w-full px-4 py-2 text-blue-600 hover:bg-blue-50 transition-colors"
              @click="toggleMenu"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5"
                viewBox="0 -960 960 960"
                fill="currentColor"
              >
                <path
                  d="M240-160q-33 0-56.5-23.5T160-240q0-33 23.5-56.5T240-320q33 0 56.5 23.5T320-240q0 33-23.5 56.5T240-160Zm0-240q-33 0-56.5-23.5T160-480q0-33 23.5-56.5T240-560q33 0 56.5 23.5T320-480q0 33-23.5 56.5T240-400Zm0-240q-33 0-56.5-23.5T160-720q0-33 23.5-56.5T240-800q33 0 56.5 23.5T320-720q0 33-23.5 56.5T240-640Zm240 0q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Zm240 0q-33 0-56.5-23.5T640-720q0-33 23.5-56.5T720-800q33 0 56.5 23.5T800-720q0 33-23.5 56.5T720-640ZM480-400q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm40 240v-123l221-220q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T863-380L643-160H520Z"
                />
              </svg>
              <span class="font-medium">Signup</span>
            </RouterLink>
          </div>
        </div>

        <div class="relative">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            height="28px"
            width="28px"
            viewBox="0 -960 960 960"
            fill="currentColor"
            class="cursor-pointer hover:text-sky-400 transition-colors duration-300"
          >
            <path
              d="M280-80q-33 0-56.5-23.5T200-160q0-33 23.5-56.5T280-240q33 0 56.5 23.5T360-160q0 33-23.5 56.5T280-80Zm400 0q-33 0-56.5-23.5T600-160q0-33 23.5-56.5T680-240q33 0 56.5 23.5T760-160q0 33-23.5 56.5T680-80ZM246-720l96 200h280l110-200H246Zm-38-80h590q23 0 35 20.5t1 41.5L692-482q-11 20-29.5 31T622-440H324l-44 80h480v80H280q-45 0-68-39.5t-2-78.5l54-98-144-304H40v-80h130l38 80Zm134 280h280-280Z"
            />
          </svg>
          <span
            v-if="cartItemCount > 0"
            class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold"
          >
            {{ cartItemCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

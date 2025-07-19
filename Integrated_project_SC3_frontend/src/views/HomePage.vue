<script setup>
import { RouterLink } from "vue-router";
import { ref, onMounted } from "vue";
import { gsap } from "gsap";

// DOM references
const heroText = ref(null);
const heroImage = ref(null);
const buttons = ref([]);
const cards = ref([]);
const features = ref([]);

onMounted(() => {
  // Hero animations
  gsap.from(heroText.value, {
    opacity: 0,
    y: 60,
    duration: 1.4,
    ease: "power3.out",
  });

  gsap.from(heroImage.value, {
    opacity: 0,
    scale: 0.9,
    duration: 1.6,
    delay: 0.3,
    ease: "power3.out",
  });

  // Buttons with stagger
  gsap.from(buttons.value, {
    opacity: 0,
    y: 30,
    stagger: 0.15,
    delay: 0.8,
    duration: 1,
    ease: "power2.out",
  });

  // Cards animation
  gsap.from(cards.value, {
    opacity: 0,
    y: 80,
    stagger: 0.2,
    delay: 0.5,
    duration: 1.2,
    ease: "power3.out",
  });

  // Features animation
  gsap.from(features.value, {
    opacity: 0,
    x: -50,
    stagger: 0.1,
    delay: 1,
    duration: 0.8,
    ease: "power2.out",
  });
});
</script>

<template>
  <div class="min-h-screen bg-white">
    
    <!-- Hero Section -->
    <section class="relative min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
      <!-- Background Pattern -->
      <div class="absolute inset-0 opacity-5">
        <div class="absolute top-20 left-20 w-96 h-96 bg-blue-600 rounded-full blur-3xl"></div>
        <div class="absolute bottom-20 right-20 w-96 h-96 bg-purple-600 rounded-full blur-3xl"></div>
      </div>
      
      <div class="container mx-auto px-6 grid lg:grid-cols-2 gap-12 items-center relative z-10">
        <!-- Text Content -->
        <div ref="heroText" class="ml-[100px] space-y-8">
          <div class="space-y-6">
            <h1 class="text-5xl lg:text-7xl font-light text-gray-900 leading-tight">
              Galaxy
              <span class="block font-normal text-blue-600">S25 Ultra</span>
            </h1>
            <p class="text-2xl lg:text-3xl text-gray-600 font-light leading-relaxed max-w-2xl">
              เปิดประสบการณ์ใหม่แห่งนวัตกรรม<br>
              <span class="text-blue-600 font-medium">พร้อมฟีเจอร์ AI ที่ล้ำสมัยที่สุด</span>
            </p>
          </div>
          
          <!-- Feature Highlights -->
          <div class="space-y-6 mt-8">
            <div ref="el => features.value.push(el)" class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-blue-600 to-purple-600 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-xl lg:text-2xl text-gray-800 font-medium group-hover:text-blue-600 transition-colors">200MP Ultra Camera</span>
            </div>
            <div ref="el => features.value.push(el)" class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-xl lg:text-2xl text-gray-800 font-medium group-hover:text-purple-600 transition-colors">S Pen Built-in</span>
            </div>
            <div ref="el => features.value.push(el)" class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-green-600 to-blue-600 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-xl lg:text-2xl text-gray-800 font-medium group-hover:text-green-600 transition-colors">Galaxy AI</span>
            </div>
          </div>

          <!-- CTA Buttons -->
          <div class="flex flex-col sm:flex-row gap-4 pt-4">
            <RouterLink to="/sale-items">
              <button
                ref="el => buttons.value.push(el)"
                class="group px-8 py-4 bg-blue-600 text-white rounded-full font-medium text-lg hover:bg-blue-700 transition-all duration-300 hover:shadow-lg hover:shadow-blue-600/25 transform hover:-translate-y-1"
              >
                <span class="flex items-center justify-center space-x-2">
                  <span>Shop Now</span>
                  <svg class="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"></path>
                  </svg>
                </span>
              </button>
            </RouterLink>
            <RouterLink to="/sale-items/list">
              <button
                ref="el => buttons.value.push(el)"
                class="px-8 py-4 border-2 border-gray-300 text-gray-700 rounded-full font-medium text-lg hover:border-gray-400 hover:bg-gray-50 transition-all duration-300"
              >
                Become Seller
              </button>
            </RouterLink>
          </div>
        </div>

        <!-- Hero Image -->
        <div ref="heroImage" class="mr-[100px] flex justify-center lg:justify-end">
          <div class="relative">
            <div class="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20 rounded-3xl blur-2xl scale-110"></div>
            <img
              src="https://th.bing.com/th/id/OIP.KODyoJuJYVP9s2PtToRRIgHaFQ?rs=1&pid=ImgDetMain&cb=idpwebpc2"
              alt="Galaxy S25 Ultra"
              class="relative z-10 max-w-md lg:max-w-lg xl:max-w-xl w-full h-auto object-contain drop-shadow-2xl"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- Products Section -->
    <section class="py-20 px-6 bg-white">
      <div class="container mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl lg:text-5xl font-light text-gray-900 mb-4">Best Sellers</h2>
          <p class="text-xl text-gray-600 font-light">เลือกสมาร์ทโฟนที่ดีที่สุดสำหรับคุณ</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div
            v-for="(product, index) in [
              { name: 'Galaxy S25 Ultra', price: '฿42,900', color: 'Titanium Black' },
              { name: 'Galaxy S25+', price: '฿35,900', color: 'Icy Blue' },
              { name: 'Galaxy S25', price: '฿28,900', color: 'Silver Shadow' },
              { name: 'Galaxy Z Fold6', price: '฿62,900', color: 'Navy' }
            ]"
            :key="index"
            ref="el => cards.value.push(el)"
            class="group bg-white rounded-3xl p-6 hover:shadow-2xl transition-all duration-500 border border-gray-100 hover:border-gray-200 cursor-pointer transform hover:-translate-y-2"
          >
            <div class="aspect-square mb-6 relative overflow-hidden rounded-2xl bg-gradient-to-br from-gray-50 to-gray-100">
              <img
                src="https://th.bing.com/th/id/OIP.KODyoJuJYVP9s2PtToRRIgHaFQ?rs=1&pid=ImgDetMain&cb=idpwebpc2"
                :alt="product.name"
                class="w-full h-full object-contain p-4 group-hover:scale-110 transition-transform duration-500"
              />
            </div>
            
            <div class="space-y-3">
              <h3 class="text-xl font-medium text-gray-900">{{ product.name }}</h3>
              <p class="text-sm text-gray-500">{{ product.color }}</p>
              <p class="text-2xl font-light text-gray-900">{{ product.price }}</p>
              
              <button class="w-full mt-4 py-3 px-6 bg-gray-900 text-white rounded-full font-medium hover:bg-gray-800 transition-colors duration-300 group-hover:bg-blue-600">
                View Details
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Innovation Section -->
    <section class="py-20 px-6 bg-gradient-to-br from-gray-900 to-black text-white">
      <div class="container mx-auto text-center">
        <h2 class="text-4xl lg:text-5xl font-light mb-8">Innovation Never Stops</h2>
        <p class="text-xl font-light text-gray-300 mb-12 max-w-3xl mx-auto">
          ค้นพบเทคโนโลยีที่เปลี่ยนโลกไปกับ Samsung Galaxy พร้อมฟีเจอร์ AI ที่จะทำให้ชีวิตของคุณง่ายขึ้น
        </p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-16">
          <div class="text-center space-y-4">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Ultra Camera</h3>
            <p class="text-gray-400">200MP resolution with AI enhancement</p>
          </div>
          
          <div class="text-center space-y-4">
            <div class="w-16 h-16 bg-purple-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Fast Charging</h3>
            <p class="text-gray-400">45W super fast charging technology</p>
          </div>
          
          <div class="text-center space-y-4">
            <div class="w-16 h-16 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Galaxy AI</h3>
            <p class="text-gray-400">Smart features powered by artificial intelligence</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
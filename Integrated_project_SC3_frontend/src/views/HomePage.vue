<script setup>
import { RouterLink } from "vue-router";
import { ref, onMounted } from "vue";
import { gsap } from "gsap";
import MobiePhone from "@/assets/imageLogo/mobiePhone.png";
import { ScrollTrigger } from "gsap/ScrollTrigger";

gsap.registerPlugin(ScrollTrigger);

const heroText = ref(null);
const heroImage = ref(null);
const buttons = ref([]);

onMounted(() => {
  // Fade-in Text
  gsap.from(heroText.value.children, {
    y: 60,
    opacity: 0,
    duration: 1,
    stagger: 0.2,
    ease: "power3.out",
  });

  // Hero Image float
  gsap.from(heroImage.value, {
    scale: 0.8,
    opacity: 0,
    duration: 1.2,
    ease: "power3.out",
    delay: 0.5,
  });

  // Floating animation loop for phone
  gsap.to(heroImage.value, {
    y: 20,
    duration: 4,
    ease: "sine.inOut",
    repeat: -1,
    yoyo: true,
  });

  // CTA Button subtle pop
  gsap.from(buttons.value, {
    scale: 0.9,
    opacity: 0,
    duration: 0.8,
    stagger: 0.15,
    ease: "back.out(1.7)",
    delay: 0.8,
  });

  // Scroll effect (fade/slide up whole section)
  gsap.from(".hero-section", {
    scrollTrigger: {
      trigger: ".hero-section",
      start: "top 80%",
      toggleActions: "play none none reverse",
    },
    y: 100,
    opacity: 0,
    duration: 1,
    ease: "power2.out",
  });
});
</script>

<template>
  <div class="min-h-screen bg-white">
    <!-- Hero Section -->
    <section
  class="hero-section relative min-h-screen flex items-center justify-center overflow-hidden bg-gradient-to-br from-gray-50 via-white to-gray-100 "
>
  <!-- Floating Background Glow -->
  <div class="absolute inset-0">
    <div class="absolute bottom-0 right-0 w-[28rem] h-[28rem] bg-purple-500/30 rounded-full blur-[160px]"></div>
  </div>

  <!-- Content Grid -->
  <div class="container relative z-10 mx-auto px-8 grid lg:grid-cols-2 gap-16 items-center">
    
    <!-- Text Section -->
    <div ref="heroText" class="ml-[60px] mt-[-50px] space-y-10">
      <!-- Heading -->
      <div class="space-y-6">
        <h1 class="text-5xl lg:text-7xl font-extralight text-gray-900 leading-tight tracking-tight">
          The Future of  
          <span class="block font-semibold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            Mobile Shopping
          </span>
        </h1>
        <p class="text-xl lg:text-2xl text-gray-600 font-light leading-relaxed max-w-2xl">
          Explore every smartphone from the world’s top brands.  
          Designed for <span class="font-medium text-blue-600">every style, every budget, every you.</span>
        </p>
      </div>

      <!-- Highlights -->
      <div class="space-y-6 pt-4">
        <div class="flex items-center space-x-4 group">
          <div class="w-3 h-3 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-full group-hover:scale-125 transition-transform"></div>
          <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-blue-600 transition-colors">
            From cutting-edge flagships to everyday essentials
          </span>
        </div>
        <div class="flex items-center space-x-4 group">
          <div class="w-3 h-3 bg-gradient-to-r from-purple-600 to-pink-500 rounded-full group-hover:scale-125 transition-transform"></div>
          <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-purple-600 transition-colors">
            100% authentic with full official warranties
          </span>
        </div>
        <div class="flex items-center space-x-4 group">
          <div class="w-3 h-3 bg-gradient-to-r from-green-500 to-blue-500 rounded-full group-hover:scale-125 transition-transform"></div>
          <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-green-600 transition-colors">
            Lightning-fast delivery with expert support
          </span>
        </div>
      </div>

      <!-- CTA Buttons -->
      <div class="flex flex-col sm:flex-row gap-4 pt-8 mt-[-40px]">
        <!-- Primary CTA -->
        <RouterLink to="/sale-items">
          <button
            ref="el => buttons.value.push(el)"
            class="cursor-pointer group px-10 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-full font-semibold text-lg shadow-md hover:shadow-xl hover:shadow-blue-600/25 transition-all duration-300 transform hover:-translate-y-1"
          >
            <span class="flex items-center justify-center space-x-2">
              <span>Start Shopping</span>
            </span>
          </button>
        </RouterLink>

        <!-- Secondary CTA -->
        <RouterLink to="">
          <button
            ref="el => buttons.value.push(el)"
            class="cursor-pointer px-15 py-4 border-2 border-gray-300 text-gray-700 rounded-full font-semibold text-lg hover:border-gray-400 hover:bg-gray-100 transition-all duration-300 transform hover:-translate-y-1 backdrop-blur-sm"
          >
            About Us
          </button>
        </RouterLink>
      </div>
    </div>

    <!-- Hero Image -->
    <div ref="heroImage" class="mr-[60px] flex justify-center lg:justify-end">
      <div class="relative">
        <!-- Glow behind image -->
        <div class="absolute inset-0 bg-gradient-to-r from-blue-500/20 to-purple-500/20 rounded-3xl blur-3xl scale-110"></div>
        
        <img
          :src="MobiePhone"
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
          <h2 class="text-4xl lg:text-5xl font-light text-gray-900 mb-4">
            Best Sellers
          </h2>
          <p class="text-xl text-gray-600 font-light">
            เลือกสมาร์ทโฟนที่ดีที่สุดสำหรับคุณ
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div
            v-for="(product, index) in [
              {
                name: 'Galaxy S25 Ultra',
                price: '฿42,900',
                color: 'Titanium Black',
              },
              { name: 'Galaxy S25+', price: '฿35,900', color: 'Icy Blue' },
              { name: 'Galaxy S25', price: '฿28,900', color: 'Silver Shadow' },
              { name: 'Galaxy Z Fold6', price: '฿62,900', color: 'Navy' },
            ]"
            :key="index"
            ref="el => cards.value.push(el)"
            class="group bg-white rounded-3xl p-6 hover:shadow-2xl transition-all duration-500 border border-gray-100 hover:border-gray-200 cursor-pointer transform hover:-translate-y-2"
          >
            <div
              class="aspect-square mb-6 relative overflow-hidden rounded-2xl bg-gradient-to-br from-gray-50 to-gray-100"
            >
              <img
                src="https://th.bing.com/th/id/OIP.KODyoJuJYVP9s2PtToRRIgHaFQ?rs=1&pid=ImgDetMain&cb=idpwebpc2"
                :alt="product.name"
                class="w-full h-full object-contain p-4 group-hover:scale-110 transition-transform duration-500"
              />
            </div>

            <div class="space-y-3">
              <h3 class="text-xl font-medium text-gray-900">
                {{ product.name }}
              </h3>
              <p class="text-sm text-gray-500">{{ product.color }}</p>
              <p class="text-2xl font-light text-gray-900">
                {{ product.price }}
              </p>

              <button
                class="w-full mt-4 py-3 px-6 bg-gray-900 text-white rounded-full font-medium hover:bg-gray-800 transition-colors duration-300 group-hover:bg-blue-600"
              >
                View Details
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Innovation Section -->
    <section
      class="py-20 px-6 bg-gradient-to-br from-gray-900 to-black text-white"
    >
      <div class="container mx-auto text-center">
        <h2 class="text-4xl lg:text-5xl font-light mb-8">
          Innovation Never Stops
        </h2>
        <p class="text-xl font-light text-gray-300 mb-12 max-w-3xl mx-auto">
          ค้นพบเทคโนโลยีที่เปลี่ยนโลกไปกับ Samsung Galaxy พร้อมฟีเจอร์ AI
          ที่จะทำให้ชีวิตของคุณง่ายขึ้น
        </p>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-16">
          <div class="text-center space-y-4">
            <div
              class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
                ></path>
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"
                ></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Ultra Camera</h3>
            <p class="text-gray-400">200MP resolution with AI enhancement</p>
          </div>

          <div class="text-center space-y-4">
            <div
              class="w-16 h-16 bg-purple-600 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                ></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Fast Charging</h3>
            <p class="text-gray-400">45W super fast charging technology</p>
          </div>

          <div class="text-center space-y-4">
            <div
              class="w-16 h-16 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                ></path>
              </svg>
            </div>
            <h3 class="text-xl font-medium">Galaxy AI</h3>
            <p class="text-gray-400">
              Smart features powered by artificial intelligence
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

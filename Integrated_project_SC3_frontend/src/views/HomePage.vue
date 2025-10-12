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
  <div class=" min-h-[calc(100vh-80px)]">
    <!-- Hero Section -->
    <section class="pt-[35px] hero-section relative min-h-[calc(100vh-95px)]  flex items-center justify-center overflow-hidden bg-gradient-to-br from-gray-50 via-white to-gray-100">
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
              <span class="block font-semibold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent"> Mobile Shopping </span>
            </h1>
            <p class="text-xl lg:text-2xl text-gray-600 font-light leading-relaxed max-w-2xl">
              Explore every smartphone from the world’s top brands. Designed for <span class="font-medium text-blue-600">every style, every budget, every you.</span>
            </p>
          </div>

          <!-- Highlights -->
          <div class="space-y-6 pt-4">
            <div class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-blue-600 transition-colors"> From cutting-edge flagships to everyday essentials </span>
            </div>
            <div class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-purple-600 to-pink-500 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-purple-600 transition-colors"> 100% authentic with full official warranties </span>
            </div>
            <div class="flex items-center space-x-4 group">
              <div class="w-3 h-3 bg-gradient-to-r from-green-500 to-blue-500 rounded-full group-hover:scale-125 transition-transform"></div>
              <span class="text-lg lg:text-xl text-gray-800 font-medium group-hover:text-green-600 transition-colors"> Lightning-fast delivery with expert support </span>
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

            <img :src="MobiePhone" class="relative z-10 max-w-md lg:max-w-lg xl:max-w-xl w-full h-auto object-contain drop-shadow-2xl" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import gsap from "gsap";

const title = ref(null);
const subtitle = ref(null);
const btn = ref(null);
const circles = ref(null);

onMounted(() => {
  // Animate title
  gsap.from(title.value, {
    opacity: 0,
    y: -100,
    duration: 1.2,
    ease: "bounce.out",
  });

  // Animate subtitle
  gsap.from(subtitle.value, {
    opacity: 0,
    y: 50,
    delay: 0.6,
    duration: 1,
    ease: "power3.out",
  });

  // Animate button
  gsap.from(btn.value, {
    opacity: 0,
    scale: 0.5,
    delay: 1,
    duration: 0.8,
    ease: "back.out(1.7)",
  });

  // Floating circles background
  const circleEls = circles.value.children;
  [...circleEls].forEach((el, i) => {
    const size = Math.random() * 120 + 40;
    el.style.width = `${size}px`;
    el.style.height = `${size}px`;
    el.style.left = `${Math.random() * 100}%`;
    el.style.top = `${Math.random() * 100}%`;

    gsap.to(el, {
      y: `random(-50, 50)`,
      x: `random(-50, 50)`,
      repeat: -1,
      duration: Math.random() * 4 + 3,
      yoyo: true,
      ease: "sine.inOut",
      delay: i * 0.3,
    });
  });
});
</script>

<template>
  <div class=" min-h-[calc(100vh-80px)] relative flex flex-col items-center justify-center bg-gradient-to-b from-blue-900 via-sky-800 to-sky-600 overflow-hidden">
    <!-- Background floating circles -->
    <div ref="circles" class="absolute inset-0 -z-10">
      <div v-for="i in 6" :key="i" class="absolute bg-white/10 rounded-full"></div>
    </div>

    <!-- 404 Text -->
    <h1 ref="title" class="text-[10rem] font-extrabold text-white drop-shadow-lg select-none">404</h1>

    <!-- Subtitle -->
    <p ref="subtitle" class="mt-4 text-2xl text-slate-200 font-light tracking-wide text-center">Oops! The page you are looking for doesn’t exist.</p>

    <!-- Button -->
    <RouterLink to="/" ref="btn" class="mt-8 px-6 py-3 bg-sky-500 hover:bg-sky-600 text-white font-semibold rounded-full shadow-lg shadow-sky-700/30 transition-all"> Back to Home </RouterLink>
  </div>
</template>

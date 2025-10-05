<template>
  <div
    ref="loadingContainer"
    class="flex flex-col items-center justify-center h-screen text-blue-800 bg-transparent overflow-hidden"
  >
    <!-- แหวนหมุนซ้อนกัน -->
    <div class="relative w-40 h-40 flex items-center justify-center">
      <!-- outer -->
      <div
        ref="outerRing"
        class="absolute inset-0 border-[3px] border-blue-200 rounded-full"
      ></div>

      <!-- glowing orbit -->
      <div
        ref="glowOrbit"
        class="absolute w-24 h-24 border-[3px] border-blue-500 border-t-transparent rounded-full shadow-[0_0_15px_rgba(59,130,246,0.5)]"
      ></div>

      <!-- particle orbit -->
      <div
        ref="particle"
        class="absolute w-4 h-4 bg-blue-600 rounded-full shadow-[0_0_10px_rgba(59,130,246,0.8)]"
      ></div>

      <!-- center core -->
      <div class="w-5 h-5 bg-blue-700 rounded-full shadow-[0_0_20px_rgba(37,99,235,0.7)] animate-pulse"></div>
    </div>

    <!-- ข้อความ -->
    <div ref="textContainer" class="mt-10 text-center select-none">
      <p
        ref="loadingText"
        class="text-4xl font-bold tracking-widest text-blue-600"
      >
        Loading<span class="inline-block text-blue-400">...</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { gsap } from "gsap";

const loadingContainer = ref(null);
const outerRing = ref(null);
const glowOrbit = ref(null);
const particle = ref(null);
const loadingText = ref(null);
const textContainer = ref(null);

onMounted(() => {
  // fade in ทั้งหน้า
  gsap.from(loadingContainer.value, {
    opacity: 0,
    duration: 1.2,
    ease: "power2.out",
  });

  // วงแหวนรอบนอกหมุนช้า
  gsap.to(outerRing.value, {
    rotate: 360,
    duration: 5,
    repeat: -1,
    ease: "linear",
  });

  // วงแหวนในเรืองแสงหมุนเร็วกว่า
  gsap.to(glowOrbit.value, {
    rotate: -360,
    duration: 2.5,
    repeat: -1,
    ease: "linear",
  });

  // จุด particle หมุนรอบศูนย์กลาง
  gsap.to(particle.value, {
    rotation: 360,
    duration: 2,
    repeat: -1,
    ease: "linear",
    transformOrigin: "40px 40px", // ให้หมุนรอบวงใหญ่
  });

  // ตัวอักษรเด้งขึ้นเล็กน้อยตอนเข้า
  gsap.from(textContainer.value, {
    y: 20,
    opacity: 0,
    duration: 1,
    delay: 0.5,
    ease: "back.out(1.7)",
  });

  // pulse effect กับข้อความ
  gsap.to(loadingText.value, {
    opacity: 0.3,
    duration: 0.8,
    repeat: -1,
    yoyo: true,
    ease: "power1.inOut",
  });
});
</script>

<style scoped>
/* ทำให้วงดูเนียนขึ้น */
.border-blue-200 {
  box-shadow: 0 0 20px rgba(147, 197, 253, 0.4);
}
</style>

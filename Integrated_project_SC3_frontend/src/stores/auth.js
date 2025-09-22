// stores/auth.js
import { defineStore } from "pinia";
import { jwtDecode } from "jwt-decode";
import {
  loginUser,
  refreshToken as apiRefreshToken,
  fetchUserProfile,
  logout as apiLogout,
  editUserProfile as apiEditUserProfile
} from "@/libs/callAPI/apiAuth";
import router from "@/router";


export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: localStorage.getItem("accessToken") || null,
    role: localStorage.getItem("role") || null,
    isLoggedIn: !!localStorage.getItem("accessToken"),
  }),

  actions: {
    // ฟังก์ชัน login
    async login(username, password) {
      try {
        const { accessToken, refreshToken, role } = await loginUser(username, password);

        this.accessToken = accessToken;
        this.role = role;
        this.isLoggedIn = true;

        // เก็บลง localStorage
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("role", role);

        return true;
      } catch (err) {
        this.logout();
        throw err;
      }
    },

    // ฟังก์ชัน refresh token
    async refreshToken() {
      try {
        const { accessToken, role } = await apiRefreshToken();

        console.log("✅ AccessToken ใหม่:", accessToken);
        console.log("new role = ", role);


        this.accessToken = accessToken;
        this.role = role;
        this.isLoggedIn = true;

        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("role", role);

        return true;
      } catch (err) {
        this.logout();
        router.push("/login");
        return false;
      }
    },

    // ฟังก์ชัน logout
    async logout() {
      try {
        await apiLogout();
      } catch {
        console.error("Logout API error:", err);
      }

      this.accessToken = null;
      this.role = null;
      this.isLoggedIn = false;

      localStorage.removeItem("accessToken");
      localStorage.removeItem("role");
      // Cookies.remove("refreshToken");
      // optional: clear refresh token cookie
    },

    getAuthData() {
      const accessToken = localStorage.getItem("accessToken"); // ดึงจาก localStorage
      const decoded = jwtDecode(accessToken);
      return decoded;
    },

    async loadUserProfile(userId) {
      try {
        this.profile = await fetchUserProfile(userId)
        return this.profile
      } catch (err) {
        console.error("Error loading profile:", err);
        throw err;
      }
    },

    async updateProfile(userData) {
      try {
        const updatedProfile = await apiEditUserProfile(userData);
        this.profile = updatedProfile; // อัปเดตค่าใน store ด้วย
        return updatedProfile;
      } catch (err) {
        console.error("Update profile failed:", err);
        throw err;
      }
    },
  },
});

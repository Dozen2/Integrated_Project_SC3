// stores/auth.js
import { defineStore } from "pinia";
import { loginUser, refreshToken as apiRefreshToken } from "@/libs/callAPI/apiAuth";


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

        this.accessToken = accessToken;
        this.role = role;
        this.isLoggedIn = true;

        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("role", role);

        return true;
      } catch (err) {
        this.logout();
        return false;
      }
    },

    // ฟังก์ชัน logout
    logout() {
      this.accessToken = null;
      this.role = null;
      this.isLoggedIn = false;

      localStorage.removeItem("accessToken");
      localStorage.removeItem("role");
      // optional: clear refresh token cookie
    }
  }
});

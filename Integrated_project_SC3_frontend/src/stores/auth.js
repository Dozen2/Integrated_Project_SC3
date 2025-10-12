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
        // const { accessToken, refreshToken, role } = await loginUser(username, password);
       const { accessToken, role } = await loginUser(username, password);
        this.accessToken = accessToken;
        this.role = role;
        this.isLoggedIn = true;
        if(accessToken === "Unverified"){
          console.log("Unverified user cannot login");
          this.logout();
          return false
        }

        // เก็บลง localStorage
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("role", role);

        return true;
      } catch (err) {
        this.logout();
        throw "Hello123456:  "+err;
      }
    },

    // ฟังก์ชัน refresh token
    async refreshToken() {
      try {
        const { accessToken, role } = await apiRefreshToken();

        console.log("New AccessToken:", accessToken);
        console.log("new role = ", role);


        this.accessToken = accessToken;
        this.role = role;
        this.isLoggedIn = true;

        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("role", role);

        return true;
      } catch (err) {
        // this.logout();
        // router.push("/signin"); 
        console.log("refresh not finis");
        
        return false;
      }
    },

    // ฟังก์ชัน logout
    async logout() {
      console.log("logout");
      
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
      localStorage.removeItem("cart")
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

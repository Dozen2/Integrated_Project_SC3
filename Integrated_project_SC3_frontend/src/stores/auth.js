import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { loginUser, refreshToken as apiRefreshToken } from "@/libs/callAPI/apiAuth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: null,
    role: null,
  }),
  actions: {
    async login(username, password) {
      const { accessToken, role } = await loginUser(username, password);
      this.accessToken = accessToken;
      this.role = role;
    },
    async refreshToken() {
      try {
        const { accessToken, role } = await apiRefreshToken();
        this.accessToken = accessToken;
        this.role = role;
        return true;
      } catch {
        this.logout();
        return false;
      }
    },
    logout() {
      this.accessToken = null;
      this.role = null;
      Cookies.remove("refreshToken");
    },
  },
});


import router from "@/router";
import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";



const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
// const userUrlV2 = `${VITE_ROOT_API_URL}/itb-mshop/v2/user/register`;

// ฟังก์ชัน register user
async function registerUser(
  userData,
  nationalIdPhotoFront,
  nationalIdPhotoBack
) {
  const formData = new FormData();

  // field ที่ตรงกับ UserDTO
  formData.append("nickName", userData.nickName);
  formData.append("email", userData.email);
  formData.append("passwords", userData.passwords);
  formData.append("fullName", userData.fullName);
  formData.append("role", userData.role);
  formData.append("mobileNumber", userData.mobileNumber);
  formData.append("bankAccountNumber", userData.bankAccountNumber);
  formData.append("bankName", userData.bankName);
  formData.append("nationalId", userData.nationalId);

  // file (optional)
  if (nationalIdPhotoFront) {
    formData.append("nationalIdPhotoFront", nationalIdPhotoFront);
  }
  if (nationalIdPhotoBack) {
    formData.append("nationalIdPhotoBack", nationalIdPhotoBack);
  }

  const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/register`, {
    method: "POST",
    body: formData,
  });

  if (!res.ok) {
    const errorData = await res.json();
    throw new Error(`${errorData.message}`);
  }

  return res.json();
}

async function verifyEmail(token) {
  const url = `${VITE_ROOT_API_URL}/itb-mshop/v2/auth/verify-email?token=${token}`;
  console.log("fetching:", url);

  const response = await fetch(url, { method: "POST" });

  return response.status; // ส่งข้อความกลับไปให้ caller
}


async function refreshEmail(token) {
  const url = `${VITE_ROOT_API_URL}/itb-mshop/v2/auth/refresh-email-token?token=${token}`;
  console.log("refrech fetching:", url);
  try {
    const response = await fetch(url, { method: "POST" });
    return response.status; // ส่งข้อความกลับไปให้ caller
  } catch (error) {
    console.log(error);
  }
}


async function loginUser(username, password) {
  try {
    const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: username, password: password }),
      credentials: "include"
    });
    console.log("Login fetch response:", res.status);
    //Checkpoint!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    if (!res.ok) throw new Error("Login failed");

    const data = await res.json();
    console.log("Login response:", data);
    const accessToken = data.access_token;   // token ที่ BE ส่งกลับมา
    // const refreshToken = data.refresh_token;
    // Cookies.set("refreshToken", refreshToken, { expires: 7, secure: true, sameSite: "Strict" });

    const decoded = jwtDecode(accessToken);
    const authorities = decoded.authorities || [];
    console.log("Decoded JWT:", decoded);
    console.log("Authorities:", authorities);

    let role = "UNKNOWN";
    if (authorities.some(auth => auth.role === "ROLE_SELLER")) {
      role = "ROLE_SELLER";
    } else if (authorities.some(auth => auth.role === "ROLE_BUYER")) {
      role = "ROLE_BUYER";
    }
    console.log(role);
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("role", role);
    return { accessToken, refreshToken, role };
  } catch (err) {
    console.error(err);
    throw new Error(err.message || "Login failed");
  }
}

async function refreshToken() {
  const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/refresh`, {
    method: "POST",
    credentials: "include" // refresh token จะส่งมากับ cookie
  });

  if (!res.ok) throw new Error("Refresh failed");

  const data = await res.json();
  const decoded = jwtDecode(data.access_token); // สังเกต key ให้ตรงกับ BE

  const authorities = decoded.authorities || [];

  let decode_role = 'UNKNOWN'
  if (authorities.some(auth => auth.role === "ROLE_SELLER")) {
    decode_role = "ROLE_SELLER";
  } else if (authorities.some(auth => auth.role === "ROLE_BUYER")) {
    decode_role = "ROLE_BUYER";
  }


  return { accessToken: data.access_token, role: decode_role};
}

async function authFetch(url, options = {}) {
  const auth = useAuthStore();
  let accessToken = localStorage.getItem("accessToken");

  const headers = {
    ...(options.headers || {}),
    Authorization: `Bearer ${accessToken}`,
  };

  let res = await fetch(url, { ...options, headers, credentials: "include" });

  // ถ้า token หมดอายุ → ลอง refresh
  if (res.status === 401) {
    const success = await auth.refreshToken();
    console.log("do is finis!!!!");


    if (success) {
      accessToken = localStorage.getItem("accessToken");
      headers.Authorization = `Bearer ${accessToken}`;
      console.log("do is finis!!!! again");

      // retry ใหม่
      res = await fetch(url, { ...options, headers, credentials: "include" });
    } else {
      // refresh ไม่สำเร็จ → เด้งไป login
      auth.logout();
      router.push("/login");
      throw new Error("Session expired. Please login again.");
    }
  }

  return res;
}


async function fetchUserProfile(userId) {
  const accessToken = localStorage.getItem("accessToken")
  if (!accessToken) throw new Error("No access token")

  const res = await authFetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/user/${userId}`, {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${accessToken}`,  // ใส่ JWT ไปด้วย
    },
  })
  if (!res.ok) throw new Error("Failed to fetch profile")

  return res.json();
}

async function logout() {
  const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/logout`, {
    method: "POST",
    credentials: "include"
  });

  if (!res.ok && res.status !== 204) {
    throw new Error("Logout failed");
  }
  return true;
}

async function editUserProfile(userData) {
  const accessToken = localStorage.getItem("accessToken");
  if (!accessToken) throw new Error("No access token");

  const formData = new FormData();
  formData.append("fullName", userData.fullName);
  formData.append("nickName", userData.nickName);

  const res = await authFetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/auth/user/profile/all`, {
    method: "PUT",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
    },
    body: formData,
  });

  if (!res.ok) {
    const errorData = await res.json();
    throw new Error(errorData.message || "Update failed");
  }

  return res.json()
}



export {
  registerUser,
  verifyEmail,
  refreshEmail,
  loginUser,
  refreshToken,
  fetchUserProfile,
  logout,
  editUserProfile
};

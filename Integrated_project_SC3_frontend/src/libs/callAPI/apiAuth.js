import Cookies from "js-cookie";
import { jwtDecode } from "jwt-decode";

const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const userUrlV2 = `${VITE_ROOT_API_URL}/itb-mshop/v2/user/register`;




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

  const res = await fetch(userUrlV2, {
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
  const url = `${VITE_ROOT_API_URL}/itb-mshop/v2/user/verify-email?token=${token}`;
  console.log("fetching:", url);

  const response = await fetch(url, { method: "POST" });

  return response.status; // ส่งข้อความกลับไปให้ caller
}

async function refreshEmail(token) {
  const url = `${VITE_ROOT_API_URL}/itb-mshop/v2/user/refresh-email-token?token=${token}`;
  console.log("refrech fetching:", url);
  try {
    const response = await fetch(url, { method: "POST" });
    return response.status; // ส่งข้อความกลับไปให้ caller
  } catch (error) {
    console.log(error);
  }
}

async function loginUser(username, password) {
  const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/user/authentications`,{
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, passwords: password }),
  })

  if (!res.ok) {
    throw new Error('login failed')
  }
  const data = await res.json();

  Cookies.set("refreshToken", data.refreshToken, { expires: 7, secure: true });
  const decoded = jwtDecode(data.accessToken);
  return { accessToken: data.accessToken, role: decoded.role }
}

// refresh access token
async function refreshToken() {
  const refreshToken = Cookies.get("refreshToken");
  if (!refreshToken) throw new Error("No refresh token");

  const res = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/user/refresh`, {
    method: "POST",
    headers: { "x-refresh-token": refreshToken },
  });

  if (!res.ok) throw new Error("Refresh failed");
  const data = await res.json();
  const decoded = jwtDecode(data.accessToken);
  return { accessToken: data.accessToken, role: decoded.role };
}



export { registerUser, verifyEmail, refreshEmail, loginUser, refreshToken };

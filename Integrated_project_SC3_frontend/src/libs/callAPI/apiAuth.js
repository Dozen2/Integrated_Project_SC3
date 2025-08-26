const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const userUrlV2 = `${VITE_ROOT_API_URL}/itb-mshop/v2/user/register`;

// ฟังก์ชัน register user
export async function registerUser(userData, nationalIdPhotoFront, nationalIdPhotoBack) {
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

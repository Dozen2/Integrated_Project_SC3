import { authFetch } from "@/libs/callAPI/apiAuth";
import { jwtDecode } from "jwt-decode";

const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;

async function getAllOrderByUserId(size = 10, page = 0) {
  const accessToken = localStorage.getItem("accessToken");
  const decoded = jwtDecode(accessToken);
  console.log("decoded: ", decoded);
  console.log("decoded.id: ", decoded.id);
  if (!accessToken) throw new Error("No access token");

  const params = new URLSearchParams();
  params.append("size", size);
  params.append("page", page);

  const res = await authFetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/user/${decoded.id}/orders?${params.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
  if (!res.ok) throw new Error("Failed to fetch profile");
  return res.json();
}


async function getOrderById(id) {
  const accessToken = localStorage.getItem("accessToken");
  if (!accessToken) throw new Error("No access token");

  const res = await authFetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/orders/${id}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`, // ใส่ JWT ไปด้วย
    },
  });
  if (!res.ok) throw new Error("Failed to fetch profile");

  return res.json();
}


async function getSellerOrderBySellerId(size = 10, page = 0) {
  const accessToken = localStorage.getItem("accessToken");
  const decoded = jwtDecode(accessToken);
  console.log("decoded: ", decoded);
  console.log("decoded.id: ", decoded.id);
  if (!accessToken) throw new Error("No access token");

   const params = new URLSearchParams();
  params.append("size", size);
  params.append("page", page);

  const res = await authFetch(`${VITE_ROOT_API_URL}/itb-mshop/v2/sellers/${decoded.sellerId}/orders?${params.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
  if (!res.ok) throw new Error("Failed to fetch profile");
  return res.json();
}


export { getAllOrderByUserId, getOrderById,getSellerOrderBySellerId };

import { jwtDecode } from "jwt-decode";

function unitPrice(price) {
     try {
          return price.toLocaleString();
     } catch (error) {
          return "error"
     }
 }

 function nullCatching(data) {
     return (data === null || data === undefined || data === "") ? "-" : data;
 }

 const decodeAccessToken = (accessToken) => {
     return jwtDecode(accessToken);
 }

 export { unitPrice ,nullCatching, decodeAccessToken};
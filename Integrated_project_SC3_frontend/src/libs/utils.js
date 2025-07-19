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

 export { unitPrice ,nullCatching};
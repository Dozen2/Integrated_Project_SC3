const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const urlV1 = `${VITE_ROOT_API_URL}/itb-mshop/v1/sale-items`;
const urlV2 = `${VITE_ROOT_API_URL}/itb-mshop/v2/sale-items`;
// const test = `${VITE_ROOT_API_URL}/api/files`
const test = `${VITE_ROOT_API_URL}/itb-mshop/v2/sale-items/file`

async function testSendImage(imgName) {
  try {
    // แก้ไขตรงนี้: เพิ่ม imgName เข้าไปใน URL
    const res = await fetch(`${test}/${imgName}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) {
      if (res.status === 404) {
        // สามารถจัดการ error 404 ให้เฉพาะเจาะจงได้
        return { error: "Image not found" }; 
      }
      throw new Error(`HTTP error! status: ${res.status}`);
    }

    const imageBlob = await res.blob();
    const imageUrl = URL.createObjectURL(imageBlob);
    return imageUrl;

  } catch (error) {
    // ปรับปรุงการจัดการ error ให้แสดงข้อความที่เข้าใจง่ายขึ้น
    throw new Error(`Failed to fetch image: ${error.message}`);
  }
}


async function getAllSaleItemV1() {
  try {
    const res = await fetch(urlV1);
    if (!res.ok) {
      if (res.status === 404) {
        return { error: "not_found" };
      }
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    const SaleItem = await res.json();
    return SaleItem;
  } catch (error) {
    throw new Error(`Fetch failed: ${error.message}`);
  }
}

// const getAllSaleItemV2 = async (
//   filterBrand,
//   sortField,
//   sortDirection,
//   size,
//   page
// ) => {
//   filterBrand = filterBrand || [];

//   const params = new URLSearchParams();
//   filterBrand.forEach((brand) => {
//     params.append("filterBrands", brand || "");
//   });
//   params.append("sortField", sortField || "createdOn");
//   params.append("sortDirection", sortDirection || "desc");
//   params.append("size", size || 10);
//   params.append("page", page || 0);
//   const pathInput = params.toString();

//   try {
//     const res = await fetch(`${urlV2}?${pathInput}`);
//     if (!res.ok) {
//       if (res.status === 404) {
//         return { error: "not_found" };
//       }
//       throw new Error(`HTTP error! status: ${res.status}`);
//     }
//     const SaleItem = await res.json();
//     return SaleItem;
//   } catch (error) {
//     throw new Error(`Fetch failed: ${error.message}`);
//   }
// };

const getAllSaleItemV2 = async (
  filterBrand,
  sortField,
  sortDirection,
  size,
  page,
  filterStorages = [],
  filterPriceLower = null,
  filterPriceUpper = null
) => {
  filterBrand = filterBrand || [];
  filterStorages = filterStorages || [];

  const params = new URLSearchParams();
  
  // Filter brands
  filterBrand.forEach((brand) => {
    params.append("filterBrands", brand || "");
  });
  
  // Filter storages
  filterStorages.forEach((storage) => {
    params.append("filterStorages", storage || "");
  });
  
  // Price range filters
  if (filterPriceLower !== null && filterPriceLower !== "") {
    params.append("filterPriceLower", filterPriceLower);
  }
  if (filterPriceUpper !== null && filterPriceUpper !== "") {
    params.append("filterPriceUpper", filterPriceUpper);
  }
  
  // Sorting and pagination
  params.append("sortField", sortField || "createdOn");
  params.append("sortDirection", sortDirection || "desc");
  params.append("size", size || 10);
  params.append("page", page || 0);
  
  const pathInput = params.toString();
  console.log("API Call URL:", `${urlV2}?${pathInput}`);

  try {
    const res = await fetch(`${urlV2}?${pathInput}`);
    if (!res.ok) {
      if (res.status === 404) {
        return { error: "not_found" };
      }
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    const SaleItem = await res.json();
    return SaleItem;
  } catch (error) {
    throw new Error(`Fetch failed: ${error.message}`);
  }
};

async function getSaleItemById(id) {
  try {
    const SaleItem = await fetch(`${urlV1}/${id}`);
    if (SaleItem.status === 404) return undefined;
    const finalSaleItem = await SaleItem.json();
    return finalSaleItem;
  } catch (error) {
    throw new Error(error);
  }
}

async function addSaleItem(newSaleItem) {
  try {
    const res = await fetch(urlV1, {
      method: "POST",
      headers: {
        "content-type": "application/json",
      },
      body: JSON.stringify({
        ...newSaleItem,
      }),
    });
    const addedSaleItem = await res.json();
    return addedSaleItem;
  } catch (error) {
    throw new Error("can not add your SaleItem");
  }
}

async function updateSaleItem(id, updatedSaleItem) {
  try {
    const res = await fetch(`${urlV1}/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(updatedSaleItem),
    });
    if (!res.ok) {
      throw new Error("Failed to update SaleItem");
    }
    const updatedResponse = await res.json();
    return updatedResponse;
  } catch (error) {
    throw new Error("Cannot update your SaleItem");
  }
}

const deleteSaleItemById = async (id) => {
  const res = await fetch(`${urlV1}/${id}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    const error = new Error("Request failed");
    error.status = res.status;
    error.json = () => res.json();
    throw error;
  }

  return res.status === 204;
};

// ดึงข้อมูลแบบมี pagination/filter/sort ผ่าน query
// const getAllSaleItemPage = async ({
//   filterBrands = [],
//   page = 0,
//   size = 10,
//   sortField = null,
//   sortDirection = "desc",
// } = {}) => {
//   const params = new URLSearchParams();

//   if (filterBrands.length > 0) {
//     filterBrands.forEach((brand) => params.append("filterBrands", brand));
//   }
//   params.append("page", page);
//   params.append("size", size);
//   if (sortField) {
//     params.append("sortField", sortField);
//     params.append("sortDirection", sortDirection);
//   }

//   const fullUrl = `${urlV1}?${params.toString()}`;
//   console.log(fullUrl);

//   try {
//     const res = await fetch(fullUrl);
//     if (!res.ok) {
//       throw new Error(`HTTP error! status: ${res.status}`);
//     }
//     const SaleItem = await res.json();
//     return SaleItem;
//   } catch (error) {
//     throw new Error(`Fetch failed: ${error.message}`);
//   }
// };

const getAllSaleItemPage = async ({
  filterBrands = [],
  filterStorages = [],
  filterPriceLower = null,
  filterPriceUpper = null,
  page = 0,
  size = 10,
  sortField = null,
  sortDirection = "desc",
} = {}) => {
  const params = new URLSearchParams();

  // Filter brands
  if (filterBrands.length > 0) {
    filterBrands.forEach((brand) => params.append("filterBrands", brand));
  }

  // Filter storages
  if (filterStorages.length > 0) {
    filterStorages.forEach((storage) => params.append("filterStorages", storage));
  }

  // Price range filters
  if (filterPriceLower !== null) {
    params.append("filterPriceLower", filterPriceLower);
  }
  if (filterPriceUpper !== null) {
    params.append("filterPriceUpper", filterPriceUpper);
  }

  // Pagination
  params.append("page", page);
  params.append("size", size);

  // Sorting
  if (sortField) {
    params.append("sortField", sortField);
    params.append("sortDirection", sortDirection);
  }

  const fullUrl = `${urlV1}?${params.toString()}`;
  console.log(fullUrl);

  try {
    const res = await fetch(fullUrl);
    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    const SaleItem = await res.json();
    return SaleItem;
  } catch (error) {
    throw new Error(`Fetch failed: ${error.message}`);
  }
};

export {
  getAllSaleItemV1,
  getAllSaleItemV2,
  getSaleItemById,
  addSaleItem,
  updateSaleItem,
  deleteSaleItemById,
  // getAllSaleItemPage,
  testSendImage
};

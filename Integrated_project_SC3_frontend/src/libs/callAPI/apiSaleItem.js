const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const urlV1 = `${VITE_ROOT_API_URL}/itb-mshop/v1/sale-items`;
const urlV2 = `${VITE_ROOT_API_URL}/itb-mshop/v2/sale-items`;

async function getImageByImageName(imgName) {
  try {
    // แก้ไขตรงนี้: เพิ่ม imgName เข้าไปใน URL
    const res = await fetch(`${urlV2}/file/${imgName}`, {
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


async function getViewStorageForSelect() {
  try {
    const res = await fetch(`${urlV2}/storages`);
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

const getAllSaleItemV2 = async (
  filterBrand,
  sortField,
  sortDirection,
  size,
  page,
  filterStorages = [],
  filterPriceLower = null,
  filterPriceUpper = null,
  search = ""
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

  // เพิ่ม search keyword
  if (search && search.trim() !== "") {
    params.append("searchParam", search.trim());
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


//SaleItem Seller
//********************************************************************************************/


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

async function getSaleItemByIdV2(id) {
  try {
    const SaleItem = await fetch(`${urlV2}/${id}`);
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

//addSaleItemV2 that add to be form data
async function addSaleItemV2(newSaleItem) {
  try {
    const response = await fetch(`${VITE_ROOT_API_URL}/itb-mshop/v2`, {
      method: "POST",
      body: newSaleItem, 
    });

    // ตรวจสอบสถานะการตอบกลับ
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json(); // หรือ return response ตามที่ต้องการ
  } catch (error) {
    console.error("Error adding sale item:", error);
    throw new Error(error.message || "Cannot add your SaleItem");
  }
}

//updateSaleItemV2 function to update sale item by id
async function updateSaleItemV2(id, updatedSaleItem) {
  try {
    const res = await fetch(`${urlV2}/${id}`, {
     method: "PUT",
      body: updatedSaleItem,
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
const deleteSaleItemByIdV2 = async (id) => {
  const res = await fetch(`${urlV2}/${id}`, {
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

export {
  getAllSaleItemV1,
  getAllSaleItemV2,
  getSaleItemById,
  addSaleItem,
  updateSaleItem,
  deleteSaleItemById,
  deleteSaleItemByIdV2,
  getSaleItemByIdV2,
  getImageByImageName,
  addSaleItemV2,
  getViewStorageForSelect,
  updateSaleItemV2
};

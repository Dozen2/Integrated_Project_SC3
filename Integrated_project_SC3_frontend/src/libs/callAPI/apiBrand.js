const VITE_ROOT_API_URL = import.meta.env.VITE_ROOT_API_URL;
const urlV1 = `${VITE_ROOT_API_URL}/itb-mshop/v1/brands`;

async function getAllBrand() {
  try {
    const res = await fetch(urlV1);
    if (!res.ok) {
      if (res.status === 404) {
        return { error: "not_found" };
      }
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    const data = await res.json();  
    return data;
  } catch (error) {
    throw new Error(`Fetch failed: ${error.message}`);
  }
}



async function getBrandById(id) {
    try{
        const data = await fetch(`${urlV1}/${id}`);
        if (data.status === 404) return undefined;
        const finalBrand = await data.json();
        return finalBrand
    }catch(error){
        throw new Error(error)
    }
}

async function addBrand(newBrand) {
    try {
      const res = await fetch(urlV1, {
        method: 'POST',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({
          ...newBrand
        })
      })
      const addedBrand = await res.json()
      return addedBrand
    } catch (error) {
      throw new Error('can not add your Brand')
    }
  }


async function updateBrand(id, updatedBrand) {
  try {
    const res = await fetch(`${urlV1}/${id}`, {
      method: 'PUT', 
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(updatedBrand),
    });
    if (!res.ok) {
      throw new Error('Failed to update data');
    }
    const updatedResponse = await res.json();
    return updatedResponse;
  } catch (error) {
    throw new Error('Cannot update your data');
  }
}


const deleteBrandById = async ( id) => {
  const res = await fetch(`${urlV1}/${id}`, {
    method: 'DELETE'
  });

  if (!res.ok) {
    const error = new Error('Request failed')
    error.status = res.status
    error.json = () => res.json();
    throw error
  }

  return res.status === 204;
}

export {
    getAllBrand,
    getBrandById,
    addBrand,
    updateBrand,
    deleteBrandById
  };
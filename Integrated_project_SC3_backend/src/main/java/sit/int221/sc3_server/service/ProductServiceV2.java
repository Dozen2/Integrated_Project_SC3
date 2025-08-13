package sit.int221.sc3_server.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sit.int221.sc3_server.entity.Product;
import sit.int221.sc3_server.exception.PageNotFoundException;
import sit.int221.sc3_server.repository.BrandRepository;
import sit.int221.sc3_server.repository.ProductRepository;

import java.util.List;

@Service
public class ProductServiceV2 {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ModelMapper modelMapper;

    
    public Page<Product> getAllProduct(List<String> filterBrands,List<Integer> filterStorages,Integer filterPriceLower,Integer filterPriceUpper, Integer page, Integer size, String sortField, String sortDirection) {
        if(page == null){
            throw new PageNotFoundException("Required parameter 'page' is not present.");
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort.Direction directionId = Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField).and(Sort.by(directionId, "id")));

        filterBrands = (filterBrands == null || filterBrands.isEmpty())? null :filterBrands;
        filterStorages = (filterStorages == null || filterStorages.isEmpty())? null : filterStorages;

        filterPriceLower = (filterPriceLower == null)? 0 :filterPriceLower;
        filterPriceUpper = (filterPriceUpper == null)? 9999999 :filterPriceUpper;


//        if(filterPriceLower != null && filterPriceUpper == null){
//            return productRepository.findFilteredProductAndNullStorageGbAndMinPrice(filterBrands,filterStorages,filterPriceLower,pageable);
//        }

        if (filterStorages != null && filterStorages.contains(-1)) {
            return productRepository.findFilteredProductAndNullStorageGb(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,pageable);
        }

        if(filterPriceLower > filterPriceUpper){
            throw new RuntimeException("Min should be less than Max");
        }


        //No filter
//        if (filterBrands == null || filterBrands.isEmpty()) {
//            System.out.println("Non Filter By brand Id");
//            return productRepository.findAll(PageRequest.of(page, size,Sort.by(direction, sortField).and(Sort.by(directionId, "id"))));
//        }
//
//        //Filter by BrandName
//        else {
//            System.out.println("Filter By brand Id filterBrands: "+filterBrands+ storageGb);
//            return productRepository.findByBrand_NameInAndRamGbIn(filterBrands,storageGb,PageRequest.of(page, size, Sort.by(direction, sortField).and(Sort.by(directionId, "id"))));
//        }
        return productRepository.findFilteredProduct(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,pageable);
    }

}

package sit.int221.sc3_server.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.SaleItemCreateDTO;
import sit.int221.sc3_server.configuration.FileStorageProperties;
import sit.int221.sc3_server.entity.Brand;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.SaleItemImage;
import sit.int221.sc3_server.entity.StorageGbView;
import sit.int221.sc3_server.exception.CreateFailedException;
import sit.int221.sc3_server.exception.ItemNotFoundException;
import sit.int221.sc3_server.exception.PageNotFoundException;
import sit.int221.sc3_server.repository.BrandRepository;
import sit.int221.sc3_server.repository.SaleItemImageRepository;
import sit.int221.sc3_server.repository.SaleitemRepository;
import sit.int221.sc3_server.repository.StorageGbViewRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class SaleItemServiceV2 {
    @Autowired
    private SaleitemRepository saleitemRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private StorageGbViewRepository storageGbViewRepository;

    public Page<SaleItem> getAllProduct(List<String> filterBrands, List<Integer> filterStorages, Integer filterPriceLower, Integer filterPriceUpper, Integer page, Integer size, String sortField, String sortDirection) {
        if(page == null){
            throw new PageNotFoundException("Required parameter 'page' is not present.");
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort.Direction directionId = Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField).and(Sort.by(directionId, "id")));
//        boolean isNullStorage = filterStorages.contains(-1);
//        List<Integer> storageFilter = isNullStorage
//                ? null // ไม่ต้องส่งค่าจริงเพราะเราหา null อยู่แล้ว
//                : filterStorages;
        filterBrands = (filterBrands == null || filterBrands.isEmpty())? null :filterBrands;
        filterStorages = (filterStorages == null || filterStorages.isEmpty())? null : filterStorages;

//================Ter====================
        filterPriceLower = (filterPriceLower == null)? 0 :filterPriceLower;
        filterPriceUpper = (filterPriceUpper == null)? 9999999 :filterPriceUpper;
//================Ter====================
//        if(filterPriceLower != null && filterPriceUpper == null ){
//            return productRepository.findFilteredProductAndNullStorageGbAndMinPrice(filterBrands,filterStorages,filterPriceLower,pageable);
//        }
//        if(filterPriceLower == null ){
//            return productRepository.findFilterProductNoPrice(filterBrands,filterStorages,pageable);
//            }




//        if(filterPriceLower != null && filterPriceUpper == null){
//            return productRepository.findFilteredProductAndNullStorageGbAndMinPrice(filterBrands,filterStorages,filterPriceLower,pageable);
//        }

        if (filterStorages != null && filterStorages.contains(-1)) {
            return saleitemRepository.findFilteredProductAndNullStorageGb(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,pageable);

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
        return saleitemRepository.findFilteredProduct(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,pageable);
    }

    public List<StorageGbView> getStorageView(){
        return storageGbViewRepository.findAll();
    }

    @Transactional
    public SaleItem createSaleItem(SaleItemCreateDTO saleItemCreateDTO, List<MultipartFile> images){
        // 1. หา brand
        int brandId = saleItemCreateDTO.getBrand().getId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ItemNotFoundException("Brand with ID " + brandId + " not found."));

        // 2. เช็ค duplicate model
        String model = saleItemCreateDTO.getModel().trim();
        if(saleitemRepository.existsByModelIgnoreCase(model)){
            throw new CreateFailedException("Cannot create SaleItem: model '" + model + "' already exists.");
        }
        // 3. Map DTO → Entity
        SaleItem saleItem = modelMapper.map(saleItemCreateDTO, SaleItem.class);
        saleItem.setBrand(brand);
        // 4. Save SaleItem ก่อน เพื่อให้ได้ id
//        SaleItem saveItem = saleitemRepository.saveAndFlush(saleItem);

        // 6. จัดการไฟล์รูปภาพ
        if(images != null && !images.isEmpty()){
//            List<String> storedFileNames = fileService.storeList(images);
            int sequence = 1;
            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                //แยกนามสกุลไฟล์
                String keepFileSurname = "";
                int keepIndexFileName = originalFilename.lastIndexOf('.');
                if(keepIndexFileName > 0){
                    keepFileSurname = originalFilename.substring(keepIndexFileName);
                }

                //สุ่มชื่อใหม่
                String newFileName = UUID.randomUUID().toString() + keepFileSurname;
                fileService.store(image,newFileName);
                SaleItemImage saleItemImage = new SaleItemImage();
                saleItemImage.setSaleItem(saleItem);
                saleItemImage.setFileName(newFileName);         // ชื่อใหม่
                saleItemImage.setOriginalFileName(originalFilename); // ชื่อเก่า
                saleItemImage.setImageViewOrder(sequence++);
                System.out.println(saleItemImage);

                saleItem.getSaleItemImage().add(saleItemImage);
            }
        }
//        SaleItem saveItem = saleitemRepository.saveAndFlush(saleItem);

    return saleitemRepository.saveAndFlush(saleItem);
    }



    public SaleItem getProductById(int id) {
        SaleItem saleitem = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
        if (saleitem.getDescription() != null) {

            String cleaned = saleitem.getDescription().replaceAll("[\\n\\r\\u00A0\\u200B]", "").trim();
            saleitem.setDescription(cleaned);
        }
        System.out.println(saleitem);
        return saleitem;
    }

}

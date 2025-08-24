package sit.int221.sc3_server.service.saleItem;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.saleItem.SaleItemCreateDTO;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemImageRequest;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemWithImageInfo;
import sit.int221.sc3_server.entity.Brand;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.SaleItemImage;
import sit.int221.sc3_server.entity.StorageGbView;
import sit.int221.sc3_server.exception.*;
import sit.int221.sc3_server.exception.crudException.CreateFailedException;
import sit.int221.sc3_server.exception.crudException.DeleteFailedException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.exception.crudException.UpdateFailedException;
import sit.int221.sc3_server.repository.brand.BrandRepository;
import sit.int221.sc3_server.repository.saleItem.SaleItemImageRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;
import sit.int221.sc3_server.repository.saleItem.StorageGbViewRepository;
import sit.int221.sc3_server.service.FileService;

import java.util.*;

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

    public Page<SaleItem> getAllProduct(List<String> filterBrands, List<Integer> filterStorages, Integer filterPriceLower, Integer filterPriceUpper,String searchValue, Integer page, Integer size, String sortField, String sortDirection) {
        if(page == null){
            throw new PageNotFoundException("Required parameter 'page' is not present.");
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort.Direction directionId = Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField).and(Sort.by(directionId, "id")));
        filterBrands = (filterBrands == null || filterBrands.isEmpty())? null :filterBrands;
        filterStorages = (filterStorages == null || filterStorages.isEmpty())? null : filterStorages;
        String keyword = (searchValue == null || searchValue.isBlank()) ? null : searchValue.toLowerCase();

        if(filterPriceLower != null && filterPriceUpper == null  ){
            return saleitemRepository.findFilteredProductAndNullStorageGbAndMinPrice(filterBrands,filterStorages,filterPriceLower,keyword,pageable);
        }
        if(filterPriceUpper != null && filterPriceLower == null){
            return saleitemRepository.findFilteredProductAndNullStorageGbAndMinPrice(filterBrands,filterStorages,filterPriceUpper,keyword,pageable);
        }


        if (filterStorages != null && filterStorages.contains(-1)) {
            return saleitemRepository.findFilteredProductAndNullStorageGb(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,keyword,pageable);

        }


        return saleitemRepository.findFilteredProduct(filterBrands,filterStorages,filterPriceLower,filterPriceUpper,keyword,pageable);
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
                fileService.store(image,newFileName,"salitem");
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


    @Transactional
    public SaleItem updateSaleItem(int id, SaleItemWithImageInfo newProduct) {
        SaleItem existing = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Sale Item Not Found by Id"));

        Brand brand = brandRepository.findById(newProduct.getSaleItem().getBrand().getId())
                .orElseThrow(() -> new ItemNotFoundException("Brand not found with ID."));

        SaleItemCreateDTO saleItem = newProduct.getSaleItem();
        if (saleItem.getQuantity() == null || saleItem.getQuantity() < 0) {
            saleItem.setQuantity(1);
        }

        try {
            // -------- update main fields ----------
            existing.setModel(saleItem.getModel());
            existing.setBrand(brand);
            existing.setDescription(saleItem.getDescription());
            existing.setPrice(saleItem.getPrice());
            existing.setRamGb(saleItem.getRamGb());
            existing.setScreenSizeInch(saleItem.getScreenSizeInch());
            existing.setQuantity(saleItem.getQuantity());
            existing.setStorageGb(saleItem.getStorageGb());
            existing.setColor(saleItem.getColor());


            try {
                // -------- STEP 1: ลบรูปที่อยู่ใน deletedImage ----------
                if (newProduct.getDeletedImage() != null && !newProduct.getDeletedImage().isEmpty()) {

                    List<String> names = newProduct.getDeletedImage();
                    List<SaleItemImage> images = saleItemImageRepository
                            .findAllBySaleItemAndFileNameIn(existing, names);

                    for (SaleItemImage img : images) {
                        saleItemImageRepository.delete(img);
                        fileService.removeFile(img.getFileName(),"salitem");
                    }

                }
            }catch (Exception e){
                throw new DeleteFailedException("Cannot delete image because image cannot exists in both 'saleItemImage' list and 'deletedImage' list.");
            }


            // -------- STEP 2: จัดการรูปจาก imageInfos ----------
            if (newProduct.getSaleItemImages() != null) {
                for (SaleItemImageRequest imgReq : newProduct.getSaleItemImages()) {
                    if (imgReq.getImageFile() != null && !imgReq.getImageFile().isEmpty()) {
                        // 🔹 กรณีอัปโหลดไฟล์ใหม่
                        String originalName = imgReq.getImageFile().getOriginalFilename();
                        String fileExt = "";
                        int dotIndex = originalName.lastIndexOf(".");
                        if (dotIndex > 0) {
                            fileExt = originalName.substring(dotIndex);
                        }

                        String newFileName = UUID.randomUUID().toString() + fileExt;
                        fileService.store(imgReq.getImageFile(), newFileName,"salitem");

                        SaleItemImage newImage = new SaleItemImage();
                        newImage.setSaleItem(existing);
                        newImage.setFileName(newFileName);
                        newImage.setOriginalFileName(originalName);
                        newImage.setImageViewOrder(imgReq.getImageViewOrder()); // ✅ set order ที่ client ส่งมา
                        saleItemImageRepository.save(newImage);

                    } else if (imgReq.getFileName() != null) {
                        // 🔹 กรณีอัปเดต order ของรูปเก่า
                        SaleItemImage oldImage = saleItemImageRepository
                                .findByFileNameAndSaleItem(imgReq.getFileName(), existing)
                                .orElseThrow(() -> new ItemNotFoundException("Old image not found: " + imgReq.getFileName()));

                        if (imgReq.getImageViewOrder() != null) {
                            oldImage.setImageViewOrder(imgReq.getImageViewOrder()); // ✅ update ค่าใหม่
                        }
                        saleItemImageRepository.save(oldImage);
                    }
                }
            }

// -------- STEP 3: Normalize order ----------
            List<SaleItemImage> images = saleItemImageRepository.findBySaleItem(existing);

// sort ตามค่าที่ client ส่งมา (ใช้ค่า imageViewOrder ใหม่ล่าสุด)
            images.sort(Comparator.comparing(
                    img -> Optional.ofNullable(img.getImageViewOrder()).orElse(Integer.MAX_VALUE)
            ));

            int order = 1;
            for (SaleItemImage img : images) {
                img.setImageViewOrder(order++); // ✅ normalize เป็น 1..n
            }
            saleItemImageRepository.saveAll(images);

            return saleitemRepository.saveAndFlush(existing);

        } catch (Exception e) {
            throw new UpdateFailedException("SaleItem " + id + " not updated: " + e.getMessage());
        }
    }




    public SaleItem deleteSaleItem(int id) {
        SaleItem saleitem = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Product ID not found"));

        // ลบไฟล์รูปจาก disk
        if(saleitem.getSaleItemImage() != null){
            for (SaleItemImage image: saleitem.getSaleItemImage()) {
                fileService.removeFile(image.getFileName(),"salitem");// ลบไฟล์จาก disk
            }
        }
        // ลบ record รูปจาก DB (ถ้าไม่ได้ตั้ง orphanRemoval = true)
        for (SaleItemImage img : saleitem.getSaleItemImage()) {
            saleItemImageRepository.delete(img);
        }
        // ลบ product
        saleitemRepository.deleteById(id);
        return saleitem;
    }
}

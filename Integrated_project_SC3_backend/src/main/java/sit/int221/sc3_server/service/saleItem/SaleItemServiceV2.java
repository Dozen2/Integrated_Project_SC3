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
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.*;
import sit.int221.sc3_server.exception.crudException.CreateFailedException;
import sit.int221.sc3_server.exception.crudException.DeleteFailedException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.exception.crudException.UpdateFailedException;
import sit.int221.sc3_server.repository.brand.BrandRepository;
import sit.int221.sc3_server.repository.saleItem.SaleItemImageRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;
import sit.int221.sc3_server.repository.saleItem.StorageGbViewRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
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
    @Autowired
    private SellerRepository sellerRepository;

    public Page<SaleItem> getAllProduct(Integer sellerId, List<String> filterBrands, List<Integer> filterStorages, Integer filterPriceLower, Integer filterPriceUpper,String searchValue, Integer page, Integer size, String sortField, String sortDirection) {
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
            return saleitemRepository.findFilteredProductAndNullStorageGbAndMinPrice(sellerId, filterBrands,filterStorages,filterPriceLower,keyword,pageable);
        }
        if(filterPriceUpper != null && filterPriceLower == null){
            return saleitemRepository.findFilteredProductAndNullStorageGbAndMinPrice(sellerId, filterBrands,filterStorages,filterPriceUpper,keyword,pageable);
        }


        if (filterStorages != null && filterStorages.contains(-1)) {
            return saleitemRepository.findFilteredProductAndNullStorageGb(sellerId, filterBrands,filterStorages,filterPriceLower,filterPriceUpper,keyword,pageable);

        }


        return saleitemRepository.findFilteredProduct(sellerId, filterBrands,filterStorages,filterPriceLower,filterPriceUpper,keyword,pageable);
    }

    public List<StorageGbView> getStorageView(){
        return storageGbViewRepository.findAll();
    }

    @Transactional
    public SaleItem createSaleItem(SaleItemCreateDTO saleItemCreateDTO, List<MultipartFile> images){
        int brandId = saleItemCreateDTO.getBrand().getId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ItemNotFoundException("Brand with ID " + brandId + " not found."));

        String model = saleItemCreateDTO.getModel().trim();
        if(saleitemRepository.existsByModelIgnoreCase(model)){
            throw new CreateFailedException("Cannot create SaleItem: model '" + model + "' already exists.");
        }
        SaleItem saleItem = modelMapper.map(saleItemCreateDTO, SaleItem.class);
        saleItem.setBrand(brand);

        if(images != null && !images.isEmpty()){
            int sequence = 1;
            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                String keepFileSurname = "";
                int keepIndexFileName = originalFilename.lastIndexOf('.');
                if(keepIndexFileName > 0){
                    keepFileSurname = originalFilename.substring(keepIndexFileName);
                }

                String newFileName = UUID.randomUUID().toString() + keepFileSurname;
                fileService.store(image,newFileName,"saleitem");
                SaleItemImage saleItemImage = new SaleItemImage();
                saleItemImage.setSaleItem(saleItem);
                saleItemImage.setFileName(newFileName);
                saleItemImage.setOriginalFileName(originalFilename);
                saleItemImage.setImageViewOrder(sequence++);
                System.out.println(saleItemImage);

                saleItem.getSaleItemImage().add(saleItemImage);
            }
        }

    return saleitemRepository.saveAndFlush(saleItem);
    }



    public SaleItem getProductById(Integer saleItemId) {

        SaleItem saleItem = saleitemRepository.findById(saleItemId).orElseThrow(()-> new ItemNotFoundException("SaleItem not found for this id :: " + saleItemId));
        cleanDescription(saleItem);

        System.out.println(saleItem);
        return saleItem;
    }
    public SaleItem getProductBySellerId(Integer sellerId,Integer saleItemId){
        boolean exist = sellerRepository.existsById(sellerId);
        if(!exist){
            throw new ForbiddenException("user not found");
        }
        SaleItem saleItem = saleitemRepository.findBySellerId(sellerId,saleItemId);
        return saleItem;
    }

    public void cleanDescription(SaleItem saleItem){
        if(saleItem != null && saleItem.getDescription() != null){
            String clean = saleItem.getDescription().replaceAll("[\\n\\r\\u00A0\\u200B]", "").trim();
            saleItem.setDescription(clean);
        }
    }



@Transactional
public SaleItem updateSaleItem(Integer id, SaleItemWithImageInfo newProduct,Integer sellerId) {
    SaleItem existing = this.getProductBySellerId(sellerId,id);

    Brand brand = brandRepository.findById(newProduct.getSaleItem().getBrand().getId())
            .orElseThrow(() -> new ItemNotFoundException("Brand not found with ID."));

    SaleItemCreateDTO saleItem = newProduct.getSaleItem();
    if (saleItem.getQuantity() == null || saleItem.getQuantity() < 0) {
        saleItem.setQuantity(1);
    }

    try {
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
            if (newProduct.getDeletedImage() != null && !newProduct.getDeletedImage().isEmpty()) {

                List<String> names = newProduct.getDeletedImage();
                List<SaleItemImage> images = saleItemImageRepository
                        .findAllBySaleItemAndFileNameIn(existing, names);

                for (SaleItemImage img : images) {
                    saleItemImageRepository.delete(img);
                    fileService.removeFile(img.getFileName(),"saleitem");
                }

            }
        }catch (Exception e){
            throw new DeleteFailedException("Cannot delete image because image cannot exists in both 'saleItemImage' list and 'deletedImage' list.");
        }


        if (newProduct.getSaleItemImages() != null) {
            for (SaleItemImageRequest imgReq : newProduct.getSaleItemImages()) {
                if (imgReq.getImageFile() != null && !imgReq.getImageFile().isEmpty()) {
                    String originalName = imgReq.getImageFile().getOriginalFilename();
                    String fileExt = "";
                    int dotIndex = originalName.lastIndexOf(".");
                    if (dotIndex > 0) {
                        fileExt = originalName.substring(dotIndex);
                    }

                    String newFileName = UUID.randomUUID().toString() + fileExt;
                    fileService.store(imgReq.getImageFile(), newFileName,"saleitem");

                    SaleItemImage newImage = new SaleItemImage();
                    newImage.setSaleItem(existing);
                    newImage.setFileName(newFileName);
                    newImage.setOriginalFileName(originalName);
                    newImage.setImageViewOrder(imgReq.getImageViewOrder());
                    saleItemImageRepository.save(newImage);

                } else if (imgReq.getFileName() != null) {
                    SaleItemImage oldImage = saleItemImageRepository
                            .findByFileNameAndSaleItem(imgReq.getFileName(), existing)
                            .orElseThrow(() -> new ItemNotFoundException("Old image not found: " + imgReq.getFileName()));

                    if (imgReq.getImageViewOrder() != null) {
                        oldImage.setImageViewOrder(imgReq.getImageViewOrder());
                    }
                    saleItemImageRepository.save(oldImage);
                }
            }
        }

        List<SaleItemImage> images = saleItemImageRepository.findBySaleItem(existing);

        images.sort(Comparator.comparing(
                img -> Optional.ofNullable(img.getImageViewOrder()).orElse(Integer.MAX_VALUE)
        ));

        int order = 1;
        for (SaleItemImage img : images) {
            img.setImageViewOrder(order++);
        }
        saleItemImageRepository.saveAll(images);

        return saleitemRepository.saveAndFlush(existing);

    } catch (Exception e) {
        throw new UpdateFailedException("SaleItem " + id + " not updated: " + e.getMessage());
    }
}




    public void deleteSaleItem(Integer id,Integer sellerId) {
       SaleItem saleItem = this.getProductBySellerId(sellerId,id);


        if(saleItem.getSaleItemImage() != null){
            for (SaleItemImage image: saleItem.getSaleItemImage()) {
                fileService.removeFile(image.getFileName(),"saleitem");// ลบไฟล์จาก disk
            }
        }

        for (SaleItemImage img : saleItem.getSaleItemImage()) {
            saleItemImageRepository.delete(img);
        }
        saleitemRepository.deleteById(id);
    }

    @Transactional
    public SaleItem createSellerSaleItem(Integer sellerId,SaleItemCreateDTO saleItemCreateDTO, List<MultipartFile> images){
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(()->new ForbiddenException("user not found"));
        int brandId = saleItemCreateDTO.getBrand().getId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ItemNotFoundException("Brand with ID " + brandId + " not found."));


        SaleItem saleItem = modelMapper.map(saleItemCreateDTO, SaleItem.class);
        saleItem.setSeller(seller);
        saleItem.setBrand(brand);


        if(images != null && !images.isEmpty()){
            int sequence = 1;
            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                String keepFileSurname = "";
                int keepIndexFileName = originalFilename.lastIndexOf('.');
                if(keepIndexFileName > 0){
                    keepFileSurname = originalFilename.substring(keepIndexFileName);
                }

                String newFileName = UUID.randomUUID().toString() + keepFileSurname;
                System.out.println("Uploading file: " + image.getOriginalFilename() + ", contentType: " + image.getContentType());
                fileService.store(image,newFileName,"saleitem");
                SaleItemImage saleItemImage = new SaleItemImage();
                saleItemImage.setSaleItem(saleItem);
                saleItemImage.setFileName(newFileName);
                saleItemImage.setOriginalFileName(originalFilename);
                saleItemImage.setImageViewOrder(sequence++);
                System.out.println(saleItemImage);

                saleItem.getSaleItemImage().add(saleItemImage);
            }
        }
        SaleItem saveItem = saleitemRepository.saveAndFlush(saleItem);
        System.out.println(saveItem);

        return saveItem;
    }


}

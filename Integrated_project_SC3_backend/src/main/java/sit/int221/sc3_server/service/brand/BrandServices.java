package sit.int221.sc3_server.service.brand;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Brand.CreateBrandDTO;
import sit.int221.sc3_server.DTO.saleItem.UpdateBrandDTO;

import sit.int221.sc3_server.DTO.Brand.BrandDetailDTO;

import sit.int221.sc3_server.entity.Brand;
import sit.int221.sc3_server.exception.crudException.DeleteFailedException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.repository.brand.BrandRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BrandServices {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SaleitemRepository saleitemRepository;

    @Autowired
    private ModelMapper modelMapper;



    public List<BrandDetailDTO> getAllBrand() {
        List<Brand> brandList = brandRepository.findAll();

        return brandList.stream().map(brand -> {
            BrandDetailDTO dto = modelMapper.map(brand, BrandDetailDTO.class);
            dto.setNoOfSaleItems(brand.getSaleItems().size());
            return dto;
        }).collect(Collectors.toList());
    }


    private Brand getBrandById(int id) {
        return brandRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("No Brand found with id: " + id));
    }

    public BrandDetailDTO getBrandDetailById(int id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Brand was found with id: " + id));

        BrandDetailDTO dto = modelMapper.map(brand, BrandDetailDTO.class);
        dto.setNoOfSaleItems(brand.getSaleItems().size());

        return dto;
    }


    public Brand createBrand(CreateBrandDTO dto) {
        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setWebSiteUrl(dto.getWebsiteUrl());
        brand.setCountryOfOrigin(dto.getCountryOfOrigin());
        Boolean isActive = dto.getIsActive();
        brand.setIsActive(isActive != null ? isActive : true);
        return brandRepository.save(brand);
    }


    public BrandDetailDTO updateBrand(int id, UpdateBrandDTO dtos) {

        if (dtos.getName() == null || dtos.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name is required");
        }

        Brand brand = getBrandById(id);
        brand.setName(dtos.getName());
        brand.setWebSiteUrl(dtos.getWebsiteUrl());
        brand.setCountryOfOrigin(dtos.getCountryOfOrigin());

        Boolean isActive = dtos.getIsActive();
        brand.setIsActive(isActive != null ? isActive : true);
        brand = brandRepository.save(brand);
        BrandDetailDTO dto = modelMapper.map(brand, BrandDetailDTO.class);
        dto.setNoOfSaleItems(brand.getSaleItems().size());


        return dto;
    }


    public void deleteBrand(int id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Brand not found"));
        boolean hasProduct = saleitemRepository.existsByBrand_Id(brand.getId());
        if (hasProduct) {
            throw new DeleteFailedException("Cannot delete brand because it has products");
        }
        try {
            brandRepository.deleteById(brand.getId());
        } catch (Exception e) {
            throw new RuntimeException("Brand " + id + " cant be deleted due to " + e.getMessage());
        }
    }
}

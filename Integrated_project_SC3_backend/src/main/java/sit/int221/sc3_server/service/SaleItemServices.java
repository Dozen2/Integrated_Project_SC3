package sit.int221.sc3_server.service;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.sc3_server.DTO.SaleItemCreateDTO;
import sit.int221.sc3_server.entity.Brand;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.exception.CreateFailedException;
import sit.int221.sc3_server.exception.ItemNotFoundException;
import sit.int221.sc3_server.exception.UpdateFailedException;
import sit.int221.sc3_server.repository.BrandRepository;
import sit.int221.sc3_server.repository.SaleitemRepository;

import java.util.List;

@Service
public class SaleItemServices {
    @Autowired
    private SaleitemRepository saleitemRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<SaleItem> getAllProduct() {
        return saleitemRepository.findAll();
//        return productRepository.findAllByOrderByCreatedOnDesc();
    }


    public SaleItem getProductById(int id) {
        SaleItem saleitem = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
        if (saleitem.getDescription() != null) {

            String cleaned = saleitem.getDescription().replaceAll("[\\n\\r\\u00A0\\u200B]", "").trim();
            saleitem.setDescription(cleaned);
        }
        return saleitem;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaleItem createProduct(SaleItemCreateDTO dto) {
        int brandId = dto.getBrand().getId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ItemNotFoundException("Brand with ID " + brandId + " not found."));

        String model = dto.getModel().trim();
        boolean isDuplicate = saleitemRepository.existsByModelIgnoreCase(model);

        if (isDuplicate) {
            throw new CreateFailedException("Cannot create SaleItem: model '" + model + "' already exists.");
        }

        SaleItem saleitem = modelMapper.map(dto, SaleItem.class);
        saleitem.setBrand(brand);

        try {
            return saleitemRepository.saveAndFlush(saleitem);
        } catch (Exception e) {
            throw new CreateFailedException(
                    "Cannot create SaleItem due to: " + e.getMessage()
            );
        }
    }

    public SaleItem updateProduct(int id, SaleItemCreateDTO newProduct) {
        SaleItem existing = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Sale Item Not Found by Id"));

        Brand brand = brandRepository.findById(newProduct.getBrand().getId())
                .orElseThrow(() -> new ItemNotFoundException("Brand not found with ID."));

        if (newProduct.getQuantity() == null || newProduct.getQuantity() < 0) {
            newProduct.setQuantity(1);
        }

        try {
            existing.setModel(newProduct.getModel());
            existing.setBrand(brand);
            existing.setDescription(newProduct.getDescription());
            existing.setPrice(newProduct.getPrice());
            existing.setRamGb(newProduct.getRamGb());
            existing.setScreenSizeInch(newProduct.getScreenSizeInch());
            existing.setQuantity(newProduct.getQuantity());
            existing.setStorageGb(newProduct.getStorageGb());
            existing.setColor(newProduct.getColor());


            return saleitemRepository.save(existing);
        } catch (Exception e) {
            throw new UpdateFailedException("SaleItem " + id + " not updated: " + e.getMessage());

        }
    }

    public SaleItem deleteProduct(int id) {
        SaleItem saleitem = saleitemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Product ID not found"));
        saleitemRepository.deleteById(id);
        return saleitem;
    }

}

package sit.int221.sc3_server.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.sc3_server.DTO.PageDTO;
import sit.int221.sc3_server.DTO.SalesItemDetailDTO;
import sit.int221.sc3_server.entity.Product;
import sit.int221.sc3_server.service.ProductServiceV2;
import sit.int221.sc3_server.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/itb-mshop/v2")
//@CrossOrigin(origins = "${app.cors.allowedOrigins}")

public class ProductControllerV2 {
    @Autowired
    private ProductServiceV2 productServiceV2;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ListMapper listMapper;

    @GetMapping("/sale-items")
    public ResponseEntity<PageDTO<SalesItemDetailDTO>> getAllSaleItem(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer  filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "id",required = false) String sortField,
            @RequestParam(defaultValue = "asc", required = false) String sortDirection
    ) {
        Page<Product> products = productServiceV2.getAllProduct(filterBrands,filterStorages, filterPriceLower,filterPriceUpper, page, size, sortField, sortDirection);
        PageDTO<SalesItemDetailDTO> pageDTO = listMapper.toPageDTO(products, SalesItemDetailDTO.class, modelMapper);
        return ResponseEntity.ok(pageDTO);
    }



}

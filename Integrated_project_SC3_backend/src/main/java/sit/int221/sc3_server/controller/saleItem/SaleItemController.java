package sit.int221.sc3_server.controller.saleItem;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.sc3_server.DTO.saleItem.SaleItemCreateDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDetailDTO;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.service.saleItem.SaleItemServices;
import sit.int221.sc3_server.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/itb-mshop/v1")
//@CrossOrigin(origins = "${app.cors.allowedOrigins}")

public class SaleItemController {
    @Autowired
    private SaleItemServices saleItemServices;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ListMapper listMapper;


    @GetMapping("/sale-items")
    public ResponseEntity<List<SalesItemDTO>> getAllSaleItem() {
        List<SaleItem> productItem = saleItemServices.getAllProduct();
        List<SalesItemDTO> productDto = listMapper.mapList(productItem, SalesItemDTO.class, modelMapper);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/sale-items/{id}")
    public ResponseEntity<SalesItemDetailDTO> getSaleItemById(@PathVariable int id) {
        return ResponseEntity.ok().body(modelMapper.map(saleItemServices.getProductById(id), SalesItemDetailDTO.class));
    }

    @PostMapping("/sale-items")
    public ResponseEntity<SalesItemDetailDTO> createSaleItem(@RequestBody @Valid SaleItemCreateDTO saleItemCreateDTO) {
        SaleItem saleitem = saleItemServices.createProduct(saleItemCreateDTO);
        SalesItemDetailDTO responseDto = modelMapper.map(saleitem, SalesItemDetailDTO.class);
        responseDto.setBrandName(saleitem.getBrand().getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/sale-items/{id}")
    public ResponseEntity<SalesItemDetailDTO> updateSaleItem(@PathVariable int id, @RequestBody @Valid SaleItemCreateDTO productDto) {
       SaleItem saleitem = saleItemServices.updateProduct(id, productDto);

        var dto = modelMapper.map(saleitem, SalesItemDetailDTO.class);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping("/sale-items/{id}")
    public ResponseEntity<SalesItemDetailDTO> deleteSaleItem(@PathVariable int id) {
        saleItemServices.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }


}

package sit.int221.sc3_server.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.PageDTO;
import sit.int221.sc3_server.DTO.SaleItemCreateDTO;
import sit.int221.sc3_server.DTO.SaleItemDetailFileDto;
import sit.int221.sc3_server.DTO.SalesItemDetailDTO;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.SaleItemServiceV2;
import sit.int221.sc3_server.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/itb-mshop/v2")
//@CrossOrigin(origins = "${app.cors.allowedOrigins}")

public class SaleItemControllerV2 {
    @Autowired
    private SaleItemServiceV2 saleItemServiceV2;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private FileService fileService;

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
        Page<SaleItem> saleitems = saleItemServiceV2.getAllProduct(filterBrands,filterStorages, filterPriceLower,filterPriceUpper, page, size, sortField, sortDirection);
        PageDTO<SalesItemDetailDTO> pageDTO = listMapper.toPageDTO(saleitems, SalesItemDetailDTO.class, modelMapper);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping("")
    public ResponseEntity<SaleItemDetailFileDto> createSaleItem(
            @ModelAttribute SaleItemCreateDTO saleItemCreateDTO ,
            @RequestPart List<MultipartFile> images){

        SaleItem saleitem = saleItemServiceV2.createSaleItem(saleItemCreateDTO,images);

        SaleItemDetailFileDto response = modelMapper.map(saleitem, SaleItemDetailFileDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/sale-items/{id}")
    public ResponseEntity<SaleItemDetailFileDto> getSaleItemById(@PathVariable int id) {
        return ResponseEntity.ok().body(modelMapper.map(saleItemServiceV2.getProductById(id), SaleItemDetailFileDto.class));
    }

    @GetMapping("/sale-items/file/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(
            @PathVariable String filename) {
        Resource file = fileService.loadFileAsResource(filename);
        System.out.println(MediaType.valueOf(fileService.getFileType(file)));
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(fileService.getFileType(file))).body(file);
    }


//    @PutMapping("")

}

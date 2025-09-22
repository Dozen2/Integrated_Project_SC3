package sit.int221.sc3_server.controller.saleItem;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.*;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.DTO.saleItem.SaleItemCreateDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDTO;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemDetailFileDto;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemWithImageInfo;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDetailDTO;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SaleItemDetailSeller;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SellerDTO;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.Seller;
import sit.int221.sc3_server.entity.StorageGbView;
import sit.int221.sc3_server.exception.UnAuthenticateException;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.saleItem.SaleItemServiceV2;
import sit.int221.sc3_server.service.user.UserServices;
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
    @Autowired
    private UserServices userServices;

    @GetMapping("/sale-items")//map เป็น SalesItemDetailFileDto เพื่อที่จะได้ส่งรูปไปได้
    public ResponseEntity<PageDTO<SalesItemDetailDTO>> getAllSaleItem(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer  filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = false) String searchParam,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "id",required = false) String sortField,
            @RequestParam(defaultValue = "asc", required = false) String sortDirection
    ) {
        Page<SaleItem> saleitems = saleItemServiceV2.getAllProduct(null,filterBrands,filterStorages, filterPriceLower,filterPriceUpper,searchParam, page, size, sortField, sortDirection);
        PageDTO<SalesItemDetailDTO> pageDTO = listMapper.toPageDTO(saleitems, SalesItemDetailDTO.class, modelMapper);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping("")
    public ResponseEntity<SaleItemDetailFileDto> createSaleItem(
            @ModelAttribute SaleItemCreateDTO saleItemCreateDTO ,
            @RequestPart(required = false) List<MultipartFile> images){

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


    @GetMapping("/sale-items/storages")
    public ResponseEntity<List<StorageGbView>> getStorageView(){
        return ResponseEntity.ok().body(saleItemServiceV2.getStorageView());
    }


    @PutMapping(value = "/sale-items/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemDetailFileDto> updateSaleItem(
            @PathVariable int id,
            @ModelAttribute SaleItemWithImageInfo request,
            @RequestPart(required = false) List<MultipartFile> newImages
            ){
        SaleItem saleItem = saleItemServiceV2.updateSaleItem(id,request);
        SaleItemDetailFileDto response = modelMapper.map(saleItem, SaleItemDetailFileDto.class);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sale-items/{id}")
    public ResponseEntity<SaleItemWithImageInfo> deleteSaleItem(@PathVariable int id){
             saleItemServiceV2.deleteSaleItem(id);
            return ResponseEntity.noContent().build() ;
    }

    @GetMapping("/sellers/{id}/sale-items")
    public ResponseEntity<PageDTO<SaleItemDetailSeller>> getSaleItemBySeller(
            @PathVariable int id,
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer  filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = false) String searchParam,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "id",required = false) String sortField,
            @RequestParam(defaultValue = "asc", required = false) String sortDirection,
            Authentication authentication
    ){
        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();
        Buyer buyer = userServices.findBuyerBySellerId(id);
        if(!authUserDetail.getId().equals(buyer.getId())){
            throw new UnAuthorizeException("request user id not matched with id in access token");
        }
        if(!"ACCESS_TOKEN".equals(authUserDetail.getTokenType())){
            throw new UnAuthorizeException("Invalid token type");
        }

        if(!buyer.getIsActive()){
            throw new UnAuthenticateException("user is not active");
        }
        String authUsername = authUserDetail.getUsername();
        Integer authSellerId = authUserDetail.getSellerId();
        Page<SaleItem> saleItems = saleItemServiceV2.getAllProduct(authSellerId,filterBrands,filterStorages,filterPriceLower,filterPriceUpper,searchParam,page,size,sortField,sortDirection);
        PageDTO<SaleItemDetailSeller> pageDTO = listMapper.toPageDTO(saleItems, SaleItemDetailSeller.class,modelMapper);
        pageDTO.getContent().forEach(dto ->{
            if(dto.getSellerDTO() == null) dto.setSellerDTO(new SellerDTO());
            dto.getSellerDTO().setId(authSellerId);
            dto.getSellerDTO().setUserName(authUsername);

        });
        return ResponseEntity.ok(pageDTO);

    }


    @PostMapping("/sellers/{id}/sale-items")
    public ResponseEntity<SaleItemDetailFileDto> createSaleItemSeller(@ModelAttribute SaleItemCreateDTO saleItemCreateDTO
                                                                        ,@RequestPart(required = false) List<MultipartFile> images
                                                                        ,@PathVariable(value = "id") int id,
                                                                      Authentication authentication){
    AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();
    Buyer buyer = userServices.findBuyerBySellerId(id);
    if(!"ACCESS_TOKEN".equals(authUserDetail.getTokenType())){
        throw new UnAuthorizeException("Invalid token");
    }
    if(!authUserDetail.getId().equals(buyer.getId())){
        throw new UnAuthorizeException("Seller not found");
    }

    if(!buyer.getIsActive()){
        throw new UnAuthenticateException("user is not active");
    }

    SaleItem saleItem = saleItemServiceV2.createSellerSaleItem(authUserDetail.getSellerId(), saleItemCreateDTO,images);
    SaleItemDetailFileDto response = modelMapper.map(saleItem,SaleItemDetailFileDto.class);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

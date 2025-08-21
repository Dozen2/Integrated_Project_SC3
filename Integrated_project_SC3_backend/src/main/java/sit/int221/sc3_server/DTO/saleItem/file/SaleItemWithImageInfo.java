package sit.int221.sc3_server.DTO.saleItem.file;

import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.sc3_server.DTO.saleItem.SaleItemCreateDTO;

import java.util.List;

@Data
@NoArgsConstructor
public class SaleItemWithImageInfo {
    private SaleItemCreateDTO saleItem;
    private List<SaleItemImageRequest> saleItemImages;
    private List<String> deletedImage;

}

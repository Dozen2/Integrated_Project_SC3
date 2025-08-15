package sit.int221.sc3_server.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SaleItemWithImageInfo {
    private SaleItemCreateDTO saleItem;
    private List<SaleItemImageRequest> imageInfos;


}

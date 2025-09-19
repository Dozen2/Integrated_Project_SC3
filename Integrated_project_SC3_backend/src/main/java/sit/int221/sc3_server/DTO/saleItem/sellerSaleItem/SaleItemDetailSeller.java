package sit.int221.sc3_server.DTO.saleItem.sellerSaleItem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.sc3_server.entity.Seller;

@Data
@NoArgsConstructor
public class SaleItemDetailSeller {
    private Integer id;
    private String model;
    private String brandName;
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
    @JsonProperty("seller")
    private SellerDTO sellerDTO;


    public void setColor(String color) {
        if (color != null && color.trim().isEmpty()) {
            this.color = null;
        } else if (color == null || color.trim().isEmpty()) {
            this.color = null;
        } else {
            this.color = color.trim();
        }
    }
}

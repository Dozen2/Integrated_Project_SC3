package sit.int221.sc3_server.DTO.order;

import lombok.Data;

@Data
public class OrderItems {
    private Integer no;
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
    private String mainImageFileName;
}

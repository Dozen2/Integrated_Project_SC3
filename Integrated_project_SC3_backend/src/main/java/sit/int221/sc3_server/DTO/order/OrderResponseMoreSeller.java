package sit.int221.sc3_server.DTO.order;

import lombok.Data;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SellerDTO;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseMoreSeller {
    private Integer id;
    private Integer buyerId;
    private SellerResponseOrder sellerResponseOrder;
    private Instant orderDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItems> orderItems;
    private String orderStatus;
}

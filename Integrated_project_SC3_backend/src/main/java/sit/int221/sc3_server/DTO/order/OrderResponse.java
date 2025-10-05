package sit.int221.sc3_server.DTO.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SellerDTO;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private Integer id;
    private Integer buyerId;
    @JsonProperty("seller")
    private SellerDTO sellerDTO;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItems> orderItems;
    private String orderStatus;
}

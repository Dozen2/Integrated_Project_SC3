package sit.int221.sc3_server.DTO.order;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private Integer id;
    private Integer buyerId;
    private SellerResponseOrder seller;
    private Instant orderDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItems> orderItems;
    private String orderStatus;
}

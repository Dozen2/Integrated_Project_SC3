package sit.int221.sc3_server.DTO.order;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderRequest {
    private Integer buyerId;
    private Integer sellerId;
    private Instant orderDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItems> orderItems;
    private String orderStatus;


}

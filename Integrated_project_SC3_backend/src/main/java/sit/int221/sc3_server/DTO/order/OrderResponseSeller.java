package sit.int221.sc3_server.DTO.order;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseSeller {
    private Integer id;
    private BuyerDTO buyerDTO;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItems> orderItems;
    private String orderStatus;
}

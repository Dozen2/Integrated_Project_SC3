package sit.int221.sc3_server.DTO.order;

import lombok.Data;

@Data
public class SellerResponseOrder {
    private Integer id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;

}

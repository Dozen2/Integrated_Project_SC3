package sit.int221.sc3_server.DTO.user.profile;

import lombok.Data;

@Data
public class SellerProfileDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String userType;
    private String phoneNumber;
    private String bankName;
    private String bankAccount;
    private String nickName;
}

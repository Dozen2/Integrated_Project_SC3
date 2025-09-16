package sit.int221.sc3_server.DTO.user.profile;

import lombok.Data;

@Data
public class BuyerProfileDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;

}

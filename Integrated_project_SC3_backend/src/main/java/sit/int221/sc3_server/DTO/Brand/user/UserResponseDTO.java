package sit.int221.sc3_server.DTO.Brand.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id;
    private String nickName;
    private String email;
//    private String passwords;
    private String fullName;
    private Boolean isActive;
    private String userType;



}

package sit.int221.sc3_server.DTO.Brand.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer id;
    private String nickName;
    private String email;
    private String passwords;
    private String fullName;

    private Integer buyerId;

    private Integer sellerId;
    private String mobileNumber;
    private String bankAccountNumber;
    private String bankName;
    private String nationalId;
    private String nationalIdPhotoFront;
    private String nationalIdPhotoBack;

}

package sit.int221.sc3_server.DTO.Brand.user;

import lombok.Data;

@Data
public class UserDTO {

    private String nickName;
    private String email;
    private String passwords;
    private String fullName;

    private String role;
    private String mobileNumber;
    private String bankAccountNumber;
    private String bankName;
    private String nationalId;
//    private String nationalIdPhotoFront;
//    private String nationalIdPhotoBack;

    public String getEmail(){
        return this.email = email.trim();
    }
}

package sit.int221.sc3_server.DTO.Brand.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    private String nickName;
    @Email
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String passwords;
    private String fullName;

    private String role;
    private String mobileNumber;
    private String bankAccountNumber;
    private String bankName;
    //    @Size(min = 0,max = 13)
    private String nationalId;
//    private String nationalIdPhotoFront;
//    private String nationalIdPhotoBack;

    public String getEmail() {
        return email != null ? email.trim() : null;
    }
}

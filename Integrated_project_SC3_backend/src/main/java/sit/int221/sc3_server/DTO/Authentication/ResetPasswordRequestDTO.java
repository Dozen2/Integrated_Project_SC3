package sit.int221.sc3_server.DTO.Authentication;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String token;
    private String newPassword;
    private String confirmPassword;
}

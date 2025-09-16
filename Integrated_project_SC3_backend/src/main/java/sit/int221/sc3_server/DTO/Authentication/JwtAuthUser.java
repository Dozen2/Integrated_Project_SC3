package sit.int221.sc3_server.DTO.Authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JwtAuthUser {
//    @Email
    @Size(min = 0,max = 50 ,message = "Your email is too long")
    private String username;

    private String password;

}

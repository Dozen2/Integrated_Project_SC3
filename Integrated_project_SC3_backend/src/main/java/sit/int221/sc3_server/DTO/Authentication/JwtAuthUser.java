package sit.int221.sc3_server.DTO.Authentication;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class JwtAuthUser {
    @Email
    private String username;
    private String passwords;

}

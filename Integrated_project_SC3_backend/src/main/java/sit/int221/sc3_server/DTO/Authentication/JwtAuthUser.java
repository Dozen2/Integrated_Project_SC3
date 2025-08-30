package sit.int221.sc3_server.DTO.Authentication;

import lombok.Data;

@Data
public class JwtAuthUser {
    private String fullName;
    private String passwords;

}

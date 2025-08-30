package sit.int221.sc3_server.DTO.Authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccessToken {
    public AccessToken(String token){this.token = token;}

    @JsonIgnore
    private String token;
    public String getAccess_token(){
        return token;
    }
}

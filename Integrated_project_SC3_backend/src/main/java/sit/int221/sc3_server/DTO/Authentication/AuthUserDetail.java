package sit.int221.sc3_server.DTO.Authentication;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
@Getter
public class AuthUserDetail extends org.springframework.security.core.userdetails.User{
//    private Integer id;
//    public AuthUserDetail(Integer id, String username, String password) {
//        this(id, username, password,new ArrayList<GrantedAuthority>());
//    }
//    public AuthUserDetail(Integer id, String username, String password
//            , Collection<? extends GrantedAuthority> authorities) {
//        super(username, password, authorities);
//        this.id = id;
//    }
    private Integer id;
    private String nickName;
    private String email;
    private String tokenType;
    private Integer sellerId;
    // constructor แบบง่าย (authorities ว่าง)
    public AuthUserDetail(Integer id, String username, String password, String nickName, String email, Collection<? extends GrantedAuthority> authorities) {
        this(id, username, password, nickName, email,null, null,new ArrayList<GrantedAuthority>());
    }

    // constructor เต็ม
    public AuthUserDetail(Integer id, String username, String password,
                          String nickName, String email,String tokenType,Integer sellerId,
                          Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.nickName = nickName;
        this.email = email;
        this.tokenType=tokenType;
        this.sellerId =sellerId;
    }
}

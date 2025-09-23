package sit.int221.sc3_server.service.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.utils.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class JwtUserDetailService implements UserDetailsService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Buyer buyer= buyerRepository.findByUserNameOrEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException(username));
        if(!buyer.getIsActive()){
            throw new UnAuthorizeException("User is not verify");
        }
//        String role = (buyer.getSeller() != null)? "SELLER":"BUYER";
        Integer id = (buyer.getSeller()!= null)? buyer.getSeller().getId():buyer.getId();
        return new AuthUserDetail(id,             // id
                buyer.getEmail(),          // username (ใช้ email)
                buyer.getPasswords(),      // password
                buyer.getNickName(),       // nickName
                buyer.getEmail(),
                null,
                getAuthorities(buyer.getRoles()));
    }

    public UserDetails loadUserById(Integer id){
    Buyer buyer = buyerRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User id "+ id + " does not exist"));
//        String role = (buyer.getSeller() != null)? "SELLER":"BUYER";
        Integer userId = (buyer.getSeller()!= null)? buyer.getSeller().getId():buyer.getId();
        return new AuthUserDetail(userId,             // id
                buyer.getEmail(),          // username (ใช้ email)
                buyer.getPasswords(),      // password
                buyer.getNickName(),       // nickName
                buyer.getEmail(),
                null,
                getAuthorities(buyer.getRoles()));
    }
    private static Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

//    private static List<GrantedAuthority> getAuthorities(Set<Role> roles) {
//        return roles.stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
//                .collect(Collectors.toList());
//    }


//    private static GrantedAuthority getAuthority(String role){
//        return new SimpleGrantedAuthority(role);
//    }
}

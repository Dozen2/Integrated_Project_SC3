package sit.int221.sc3_server.service.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.repository.user.BuyerRepository;

import java.util.List;

@Service
public class JwtUserDetailService implements UserDetailsService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Buyer buyer= buyerRepository.findByUserNameOrEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException(username));
        String role = (buyer.getSeller() != null)? "SELLER":"BUYER";
        return new AuthUserDetail(buyer.getId(),buyer.getFullName()
                ,buyer.getPasswords(),List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public UserDetails loadUserById(Integer id){
    Buyer buyer = buyerRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User id "+ id + " does not exist"));
        String role = (buyer.getSeller() != null)? "SELLER":"BUYER";
        return new AuthUserDetail(buyer.getId(),buyer.getFullName()
                ,buyer.getPasswords(),List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private static GrantedAuthority getAuthority(String role){
        return new SimpleGrantedAuthority(role);
    }
}

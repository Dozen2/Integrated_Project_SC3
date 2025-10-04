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
import sit.int221.sc3_server.entity.Seller;
import sit.int221.sc3_server.exception.ForbiddenException;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.utils.Role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtUserDetailService implements UserDetailsService {

    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Buyer buyer= buyerRepository.findByUserNameOrEmail(username)
                .orElseThrow(()-> new ForbiddenException("user does not exist"));
        if(!buyer.getIsActive()){
            throw new UnAuthorizeException("User is not verify");
        }
//        String role = (buyer.getSeller() != null)? "SELLER":"BUYER";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.BUYER);
        Integer sellerId = null;
        if (buyer.getSeller() != null) {
            roles.add(Role.SELLER);
            sellerId = buyer.getSeller().getId();
        }


        return new AuthUserDetail(buyer.getId(),             // id
                buyer.getEmail(),          // username (ใช้ email)
                buyer.getPasswords(),      // password
                buyer.getNickName(),       // nickName
                buyer.getEmail(),
                sellerId,
                null,
                getAuthorities(roles));
    }

    public UserDetails loadUserByBuyerId(Integer buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + buyerId + " does not exist"));

        Set<Role> roles = new HashSet<>();
        roles.add(Role.BUYER);

        Integer sellerId = null;
        if(buyer.getSeller() != null){
            roles.add(Role.SELLER);
            sellerId = buyer.getSeller().getId();
        }

        return new AuthUserDetail(
                buyer.getId(),       // always buyerId
                buyer.getEmail(),
                buyer.getPasswords(),
                buyer.getNickName(),
                buyer.getEmail(),
                sellerId,           // if has seller
                null,               // tokenType, ใส่เมื่อ generate token
                getAuthorities(roles)
        );
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

package sit.int221.sc3_server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.exception.UnAuthenticateException;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;
import sit.int221.sc3_server.utils.JwtUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailService jwtUserDetailService;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        Integer userId = null;
        String jwtToken;
        Map<String, Object> claims = null;
        System.out.println(requestTokenHeader);
        Collections.list(request.getHeaderNames()).forEach(System.out::println);
        if(requestTokenHeader != null){
            if(requestTokenHeader.startsWith("Bearer ")){
                jwtToken = requestTokenHeader.substring(7);
                jwtUtils.verifyToken(jwtToken);
                claims = jwtUtils.getJWTClaimSet(jwtToken);
                if(jwtUtils.isExpired(claims)){
                    throw new UnAuthenticateException("Invalid token");
                }
                if(!jwtUtils.isValidClaims(claims)|| !"ACCESS_TOKEN".equals(claims.get("typ"))){
                    throw new UnAuthenticateException("Invalid token");
                }
                Object idObj = claims.get("id");
                userId = Integer.parseInt(idObj.toString());
            }
            else {
                throw new UnAuthorizeException("Jwt token does not begin with Bearer String");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(userId != null && authentication == null){
        UserDetails userDetails = this.jwtUserDetailService.loadUserById(userId);
            if(userDetails == null || !userDetails.getUsername().equals(claims.get("email"))){
                throw new UnAuthorizeException("Invalid JWT token");
            }
            AuthUserDetail authUserDetail = new AuthUserDetail(
                    ((AuthUserDetail) userDetails).getId(),
                    userDetails.getUsername(),
                    userDetails.getPassword(),
                    ((AuthUserDetail) userDetails).getNickName(),
                    ((AuthUserDetail) userDetails).getEmail(),
                    (String) claims.get("typ"),
                    userDetails.getAuthorities()
            );
            UsernamePasswordAuthenticationToken uPaT =
                    new UsernamePasswordAuthenticationToken(authUserDetail,null,authUserDetail.getAuthorities());
            uPaT.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(uPaT);
            authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(authentication);

        }
        filterChain.doFilter(request,response);

    }

}

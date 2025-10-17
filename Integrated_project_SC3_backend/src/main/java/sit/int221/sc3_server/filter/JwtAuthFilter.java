package sit.int221.sc3_server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.utils.JwtUtils;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;

import java.io.IOException;
import java.util.Map;

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
        Integer buyerId = null;
        Map<String, Object> claims = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            jwtUtils.verifyToken(jwtToken);
            claims = jwtUtils.getJWTClaimSet(jwtToken);

            if (jwtUtils.isExpired(claims)) {
                throw new AuthenticationCredentialsNotFoundException("Token expired");
            }
            if (!jwtUtils.isValidClaims(claims) || !"ACCESS_TOKEN".equals(claims.get("typ"))) {
                throw new AuthenticationCredentialsNotFoundException("Invalid token");
            }

            Object idObj = claims.get("id");
            if (idObj == null) {
                throw new AuthenticationCredentialsNotFoundException("No id in token");
            }
            buyerId = Integer.parseInt(idObj.toString());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (buyerId != null && authentication == null) {
            UserDetails userDetails = jwtUserDetailService.loadUserByBuyerId(buyerId);

            if (userDetails == null || !userDetails.getUsername().equals(claims.get("email"))) {
                throw new AuthenticationCredentialsNotFoundException("Invalid JWT token");
            }

            AuthUserDetail authUserDetail = new AuthUserDetail(
                    ((AuthUserDetail) userDetails).getId(),
                    userDetails.getUsername(),
                    userDetails.getPassword(),
                    ((AuthUserDetail) userDetails).getNickName(),
                    ((AuthUserDetail) userDetails).getEmail(),
                    ((AuthUserDetail) userDetails).getSellerId(),
                    (String) claims.get("typ"),
                    userDetails.getAuthorities()
            );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(authUserDetail, null, authUserDetail.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}

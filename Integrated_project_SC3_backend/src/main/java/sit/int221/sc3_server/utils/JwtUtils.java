package sit.int221.sc3_server.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.entity.Token;

import java.util.Date;


@Component
public class JwtUtils {

    @Value("#{30*1000*60}")
    private long MAX_TOKEN_INTERVAL;

    @Value("${app.security.jwt.key}")
    private String KEY_ID;

    private RSAKey rsaPrivateJWK;

    private RSAKey rsaPublicJWK;


    public JwtUtils(){
        try{
            rsaPrivateJWK = new RSAKeyGenerator(2048).keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
            System.out.println(rsaPublicJWK.toJSONString());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public String generateToken(UserDetails userDetails){
        return generateToken(userDetails,MAX_TOKEN_INTERVAL, TokenType.ACCESS_TOKEN);
    }

    public String generateToken(UserDetails user,Long ageInMinute,TokenType tokenType){
        try{
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername()).issuer("http://localhost:8080")
                    .expirationTime(new Date(new Date().getTime() + ageInMinute))
                    .issueTime(new Date(new Date().getTime()))
                    .claim("authorities",user.getAuthorities())
                    .claim("uid",((AuthUserDetail)user).getId())
                    .claim("typ",tokenType.toString())
                    .build();
            SignedJWT signedJWT =new SignedJWT(new JWSHeader
                    .Builder(JWSAlgorithm.RS256).keyID(rsaPrivateJWK.getKeyID()).build(),claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

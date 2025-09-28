package sit.int221.sc3_server.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.exception.UnAuthorizeException;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;


@Component
public class JwtUtils {

    @Value("30")
    private long MAX_TOKEN_INTERVAL;

    @Value("${app.security.jwt.key-id}")
    private String KEY_ID;
    @Getter
    private RSAKey rsaPrivateJWK;

    private RSAKey rsaPublicJWK;

    public JwtUtils() {
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048).keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
            System.out.println(rsaPublicJWK.toJSONString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, MAX_TOKEN_INTERVAL, TokenType.ACCESS_TOKEN);
    }


    public String generateToken(UserDetails user, Long ageInMinute, TokenType tokenType) {
        try {
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);

            AuthUserDetail authUser = (AuthUserDetail) user;

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + ageInMinute * 60 * 1000);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("https://intproj24.sit.kmutt.ac.th/sc3/")
                    .subject(user.getUsername())
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .claim("nickname", authUser.getNickName())   // หรือ getNickName() ถ้ามี
                    .claim("id", authUser.getId())
                    .claim("sellerId", authUser.getSellerId())
                    .claim("email", authUser.getUsername())
                    .claim("authorities", user.getAuthorities())
                    .claim("typ", tokenType.toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaPrivateJWK.getKeyID())
                            .build(),
                    claimsSet
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        try{
//            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
//            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
//                    .subject(user.getUsername()).issuer("http://localhost:8080")
//                    .expirationTime(new Date(new Date().getTime() + ageInMinute))
//                    .issueTime(new Date(new Date().getTime()))
//                    .claim("authorities",user.getAuthorities())
//                    .claim("uid",((AuthUserDetail)user).getId())
//                    .claim("typ",tokenType.toString())
//                    .build();
//            SignedJWT signedJWT =new SignedJWT(new JWSHeader
//                    .Builder(JWSAlgorithm.RS256).keyID(rsaPrivateJWK.getKeyID()).build(),claimsSet);
//            signedJWT.sign(signer);
//            return signedJWT.serialize();
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
    }


    public boolean verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
            boolean pass = signedJWT.verify(verifier);
            System.out.println("Token verified!!!" + pass);
            if (!pass) {
                throw new UnAuthorizeException("Verified Error, Invalid JWT");
            }
            return true;
        } catch (JOSEException | ParseException p) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Verified Error, Invalid JWT", p);
        }
    }


    public Map<String, Object> getJWTClaimSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT (Can't parsed)", e);
        }
    }


    public boolean isExpired(Map<String, Object> jwtClaims) {
        Date expDate = (Date) jwtClaims.get("exp");
        return expDate.before(new Date());
    }


    public boolean isValidClaims(Map<String, Object> jwtClaims) {
        System.out.println(jwtClaims);
        return jwtClaims.containsKey("iat")
                && "https://intproj24.sit.kmutt.ac.th/sc3/".equals(jwtClaims.get("iss"))
                && jwtClaims.containsKey("id")
                && Long.parseLong(jwtClaims.get("id").toString()) > 0
                && jwtClaims.containsKey("email");
    }

    public String extractUsername(String token) {
        verifyToken(token);
        Map<String, Object> claims = getJWTClaimSet(token);
        if (claims.containsKey("email")) {
            return claims.get("email").toString();
        } else {
            throw new UnAuthorizeException("This user does not exist");
        }
    }

//*************************************** adding ******************************************

    public String generateEmailVerifyToken(String userEmail) throws JOSEException {
        try {
            if (userEmail == null || userEmail.isBlank()) {
                throw new IllegalArgumentException("User email cannot be null or blank");
            }
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userEmail)
                    .claim("purpose", "EMAIL_VERIFY")
//                    .expirationTime(new Date(System.currentTimeMillis() + 15 * 1000))
                    .expirationTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                    .issueTime(new Date())
                    .build();
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaPrivateJWK.getKeyID())
                            .build(),
                    claims
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate email verification token", e);
        }
    }


    public String verifyAndDecodeEmailToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
        System.out.println("isVerifyByPublicKey: " + signedJWT.verify(verifier));
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Invalid signature");
        }
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        // ตรวจสอบหมดอายุ
        if (claims.getExpirationTime().before(new Date())) {
//            throw new RuntimeException("Token expired");
            return "EmailExpire";
        }
        // ตรวจสอบว่า token ใช้สำหรับ verify email เท่านั้น
        if (!"EMAIL_VERIFY".equals(claims.getStringClaim("purpose"))) {
            throw new RuntimeException("Invalid token purpose");
        }
        System.out.println("claims: " + claims);
        return claims.getSubject();
    }


    public String extractEmailFromExpiredAccessToken(String expiredAccessToken) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(expiredAccessToken);

        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Invalid signature for Access Token");
        }
        return signedJWT.getJWTClaimsSet().getSubject();
    }

}

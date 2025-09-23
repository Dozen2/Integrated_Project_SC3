package sit.int221.sc3_server.controller.user;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.Authentication.JwtAuthUser;
import sit.int221.sc3_server.DTO.user.UserDTO;
import sit.int221.sc3_server.DTO.user.UserResponseDTO;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.user.UserServices;
import sit.int221.sc3_server.utils.JwtUtils;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/itb-mshop/v2/auth")
public class AuthController {
    @Autowired
    private UserServices userServices;
    @Autowired
    private FileService fileService;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtUserDetailService jwtUserDetailService;
    @Value("${app.cookie.path:/itb-mshop/v2/auth/refresh}")
    private String cookiePath;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> createUser(@Valid @ModelAttribute UserDTO userDTO
            , @RequestPart(value = "nationalIdPhotoFront", required = false) MultipartFile front
            , @RequestPart(value = "nationalIdPhotoBack", required = false) MultipartFile back) throws MessagingException, UnsupportedEncodingException {


        Buyer buyer = userServices.createUser(userDTO,front,back);
        UserResponseDTO dto = userServices.mapToDTO(buyer);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam(name = "token") String token)  throws MessagingException, UnsupportedEncodingException{
        boolean verify = userServices.verifyEmail(token);
        if(verify){
            return ResponseEntity.ok("Email successfully verify");
        }else{
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("ExpiredToken");
        }
    }

    @PostMapping("/refresh-email-token")
    public ResponseEntity<String> verifyEmailRefresh(@RequestParam(name = "token") String token)  throws MessagingException, UnsupportedEncodingException{
        userServices.emailExpired(token);
        return ResponseEntity.ok("Email successfully refresh");
    }


        @PostMapping("/login")//******
    public ResponseEntity<?> login(@Valid @RequestBody JwtAuthUser jwtAuthUser, HttpServletResponse response){

        if (jwtAuthUser.getUsername().isBlank()){
            throw new UnAuthorizeException("Email or Password is incorrect");
        }
                try {
                    boolean check = userServices.checkPassword(jwtAuthUser.getPassword(), jwtAuthUser.getUsername());
                    if(!check){
                        throw new UnAuthorizeException("Email or Password is incorrect");
                    }
                    Map<String,Object> tokens = userServices.authenticateUser(jwtAuthUser);
                    ResponseCookie cookie =  ResponseCookie.from("refresh_token",(String) tokens.get("refresh_token"))
                            .httpOnly(true)
                            .secure(false)
//                            .path("/itb-mshop/v2/auth/refresh")
                            .path(cookiePath)
                            .maxAge(Duration.ofDays(1))
                            .sameSite("Lax")
//                            .sameSite("Strict")
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());

                    return ResponseEntity.ok(Map.of(
                            "access_token",tokens.get("access_token")
                    ));
                }catch (BadCredentialsException e){
                    return ResponseEntity.status(400).build();
                }


    }
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody JwtAuthUser jwtAuthUser, HttpServletResponse response){
//
//        if (jwtAuthUser.getEmail().isBlank()){
//            throw new UnAuthorizeException("Email or Password is incorrect");
//        }
//        try {
//            boolean check = userServices.checkPassword(jwtAuthUser.getPassword(), jwtAuthUser.getEmail());
//            if(!check){
//                throw new UnAuthorizeException("Email or Password is incorrect");
//            }
//            Map<String,Object> tokens = userServices.authenticateUser(jwtAuthUser);
//            ResponseCookie cookie =  ResponseCookie.from("refresh_token",(String) tokens.get("refresh_token"))
//                    .httpOnly(true)
//                    .secure(false)
////                            .path("/itb-mshop/v2/auth/refresh")
//                    .path(cookiePath)
//                    .maxAge(Duration.ofDays(1))
//                    .sameSite("Lax")
////                            .sameSite("Strict")
//                    .build();
//            response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
//
//            return ResponseEntity.ok(Map.of(
//                    "access_token",tokens.get("access_token")
//            ));
//        }catch (BadCredentialsException e){
//            return ResponseEntity.status(400).build();
//        }
//
//
//    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTheToken(@CookieValue(name = "refresh_token",required = false) String token, HttpServletRequest request){
        System.out.println("======== ALL HEADERS ========");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println(headerName + ": " + request.getHeader(headerName));
        });
        System.out.println("============================");
        System.out.println("========");
        System.out.println(token);
        System.out.println("========");

        if (token == null || token.isEmpty()) {
            // ถ้าไม่มี header → return 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Token");
        }

        return ResponseEntity.ok(userServices.refreshToken(token));
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(HttpServletRequest request,HttpServletResponse response){
//        String authHeader = request.getHeader("Authorization");
//        if(authHeader == null || !authHeader.startsWith("Bearer ")){
//            return ResponseEntity.badRequest().build();
//        }
////        String token = authHeader.substring(7);
////        Map<String,Object> claims;
////        try{
////            claims =jwtUtils.getJWTClaimSet(token);
////        }catch (Exception e){
////            return ResponseEntity.badRequest().build();
////        }
////        Integer userId = Integer.parseInt(claims.get("id").toString());
////         userServices.checkIsActive(userId);
//         ResponseCookie deleteCookie = ResponseCookie.from("refresh_token","")
//                 .httpOnly(true)
////                 .secure(true)
//                 .path("/")
//                 .maxAge(0)  // expire ทันที
//                 .build();
//         response.addHeader("Set-Cookie",deleteCookie.toString());
//         return ResponseEntity.noContent().build();
//
//    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        System.out.println("======== ALL HEADERS ========");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println(headerName + ": " + request.getHeader(headerName));
        });
        System.out.println("============================");
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.noContent().build();
    }
}

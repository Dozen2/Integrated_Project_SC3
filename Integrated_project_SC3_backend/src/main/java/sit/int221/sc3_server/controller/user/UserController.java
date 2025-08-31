package sit.int221.sc3_server.controller.user;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Authentication.JwtAuthUser;
import sit.int221.sc3_server.DTO.Brand.user.UserDTO;
import sit.int221.sc3_server.DTO.Brand.user.UserResponseDTO;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.exception.crudException.CreateFailedException;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.user.UserServices;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/itb-mshop/v2/user")
public class UserController {
    @Autowired
    private UserServices userServices;
    @Autowired
    private FileService fileService;

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


    @PostMapping("/authentications")
    public ResponseEntity<Object> login(@Valid @RequestBody JwtAuthUser jwtAuthUser){
        if (jwtAuthUser.getUsername().isBlank()){
            throw new UnAuthorizeException("Email or Password is incorrect");
        }
                try {
                    boolean check = userServices.checkPassword(jwtAuthUser.getPasswords(), jwtAuthUser.getUsername());
                    if(!check){
                        throw new UnAuthorizeException("Email or Password is incorrect");
                    }
                    return ResponseEntity.ok(userServices.authenticateUser(jwtAuthUser));
                }catch (BadCredentialsException e){
                    return ResponseEntity.status(400).build();
                }


    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshTheToken(@RequestHeader("x-refresh-token") String token){
        return ResponseEntity.ok(userServices.refreshToken(token));
    }
//    @GetMapping("/user/file/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(
//            @PathVariable String filename) {
//        Resource file = fileService.loadFileAsResourceNational(filename);
//        System.out.println(MediaType.valueOf(fileService.getFileType(file)));
//        return ResponseEntity.ok()
//                .contentType(MediaType.valueOf(fileService.getFileType(file))).body(file);
//    }
}

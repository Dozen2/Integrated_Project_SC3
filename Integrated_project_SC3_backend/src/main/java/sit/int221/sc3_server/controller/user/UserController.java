package sit.int221.sc3_server.controller.user;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.Brand.user.UserDTO;
import sit.int221.sc3_server.DTO.Brand.user.UserResponseDTO;
import sit.int221.sc3_server.entity.Buyer;
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
    public ResponseEntity<UserResponseDTO> createUser(@ModelAttribute UserDTO userDTO
            , @RequestPart(value = "nationalIdPhotoFront", required = false) MultipartFile front
            , @RequestPart(value = "nationalIdPhotoBack", required = false) MultipartFile back) throws MessagingException, UnsupportedEncodingException {


        Buyer buyer = userServices.createUser(userDTO,front,back);
        UserResponseDTO dto = userServices.mapToDTO(buyer);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam(name = "token") String token){
        boolean verify = userServices.verifyEmail(token);
        if(verify){
            return ResponseEntity.ok("Email successfully verify");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }
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

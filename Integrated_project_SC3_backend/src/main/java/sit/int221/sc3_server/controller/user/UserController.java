package sit.int221.sc3_server.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sit.int221.sc3_server.DTO.Authentication.AuthUserDetail;
import sit.int221.sc3_server.DTO.user.profile.UserProfileRequestRTO;
import sit.int221.sc3_server.exception.ForbiddenException;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.user.UserServices;
import sit.int221.sc3_server.utils.JwtUtils;

@RestController
@RequestMapping("/itb-mshop/v2/user")
public class UserController {
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



    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable int id , Authentication authentication){
        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();

        if(!authUserDetail.getId().equals(id)){
            throw new ForbiddenException("request user id not matched with id in access token");
        }

        if(!"ACCESS_TOKEN".equals(authUserDetail.getTokenType())){
            throw new ForbiddenException("Invalid token type");
        }
        boolean isSeller = authUserDetail.getAuthorities()
                .stream().anyMatch(auth ->auth.getAuthority().equals("ROLE_SELLER"));
        if(!isSeller){
            return ResponseEntity.ok(userServices.getBuyerById(authUserDetail.getId()));
        }else {
            return ResponseEntity.ok(userServices.getSeller(authUserDetail.getId()));
        }

    }

    @PutMapping("/profile/all")
    public ResponseEntity<?> editUserProfile(@ModelAttribute UserProfileRequestRTO userProfileRequestRTO, Authentication authentication){
        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();

        if(!"ACCESS_TOKEN".equals(authUserDetail.getTokenType())){
            throw new ForbiddenException("Invalid token type");
        }

        boolean isSeller = authentication.getAuthorities()
                .stream().anyMatch(auth ->auth.getAuthority().equals("ROLE_SELLER"));

        if(isSeller){
            return ResponseEntity.ok().body(userServices.updateSeller(userProfileRequestRTO,authUserDetail.getId()));
        }else{
            return ResponseEntity.ok().body(userServices.updateBuyer(userProfileRequestRTO,authUserDetail.getId()));
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
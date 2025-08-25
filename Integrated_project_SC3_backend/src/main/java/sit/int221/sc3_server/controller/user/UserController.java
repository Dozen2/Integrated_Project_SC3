package sit.int221.sc3_server.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.Brand.user.UserDTO;
import sit.int221.sc3_server.DTO.Brand.user.UserResponseDTO;
import sit.int221.sc3_server.entity.User;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.service.user.UserServices;

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
            , @RequestPart(value = "nationalIdPhotoBack", required = false) MultipartFile back){

        User user = userServices.createUser(userDTO,front,back);
        UserResponseDTO dto = userServices.mapToDTO(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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

package sit.int221.sc3_server.service.user;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.Brand.user.UserDTO;
import sit.int221.sc3_server.DTO.Brand.user.UserResponseDTO;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.entity.Seller;
import sit.int221.sc3_server.entity.User;
import sit.int221.sc3_server.exception.DuplicteItemException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.repository.user.UserRepository;
import sit.int221.sc3_server.service.FileService;

import java.util.UUID;

@Service
public class UserServices {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private FileService fileService;
    private Argon2PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public void checkDuplication(UserDTO userDTO){
    if(userRepository.existsUserByEmail(userDTO.getEmail())){
        throw new DuplicteItemException("user already exist");
    }
    if(userDTO.getRole().equalsIgnoreCase("seller")
            && sellerRepository.existsSellerByMobileNumberAndNationalId(userDTO.getMobileNumber(),userDTO.getNationalId())){
        throw new DuplicteItemException("user already exist");
    }
    }

    @Transactional
    public User createUser(UserDTO userDTO, MultipartFile front,MultipartFile back) {
        // ✅ ตรวจสอบข้อมูลซ้ำ (อีเมล, ชื่อเล่น ฯลฯ)
        checkDuplication(userDTO);

        // ✅ สร้าง User ใหม่
        User user = new User();
        user.setNickName(userDTO.getNickName());
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setIsActive(false);

        // ✅ เข้ารหัสรหัสผ่าน
        String hashPassword = passwordEncoder.encode(userDTO.getPasswords());
        user.setPasswords(hashPassword);

        // ✅ ถ้า role = seller
        if ("seller".equalsIgnoreCase(userDTO.getRole())) {
            // 🔹 ตรวจสอบความครบถ้วนของข้อมูล Seller
            if (userDTO.getBankName() == null || userDTO.getBankAccountNumber() == null
                    || userDTO.getNationalId() == null
) {
                throw new IllegalArgumentException("Seller details must not be null for seller role");
            }

            // 🔹 จัดการอัพโหลดไฟล์บัตรประชาชน
            String frontFileName = saveNationalIdFile(front);
            String backFileName = saveNationalIdFile(back);

            // 🔹 สร้าง Seller
            Seller seller = new Seller();
            seller.setBankName(userDTO.getBankName());
            seller.setMobileNumber(userDTO.getMobileNumber());
            seller.setBankAccountNumber(userDTO.getBankAccountNumber());
            seller.setNationalId(userDTO.getNationalId());
            seller.setNationalIdPhotoFront(frontFileName);
            seller.setNationalIdPhotoBack(backFileName);

            // 🔹 บันทึก Seller และเชื่อมกับ User
            sellerRepository.saveAndFlush(seller);
            user.setSeller(seller);
        }

        // ✅ ทุก user เป็น buyer โดย default
        Buyer buyer = new Buyer();
        buyerRepository.saveAndFlush(buyer);
        user.setBuyer(buyer);

        // ✅ บันทึก User
        return userRepository.save(user);
    }
    public UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNickName(user.getNickName());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setIsActive(user.getIsActive());

        if(user.getBuyer() != null){
            dto.setUserType("BUYER");
        }
        if(user.getSeller() != null){
            dto.setUserType("SELLER");
        }

        return dto;
    }
    private String saveNationalIdFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        fileService.store(file, newFileName, "nationalid");
        return newFileName;
    }
}

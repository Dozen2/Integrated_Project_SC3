package sit.int221.sc3_server.service.user;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.sc3_server.DTO.Brand.user.UserDTO;
import sit.int221.sc3_server.DTO.Brand.user.UserResponseDTO;
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.DuplicteItemException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.repository.user.UserRepository;
import sit.int221.sc3_server.repository.user.VerifyTokenRepository;
import sit.int221.sc3_server.service.FileService;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class
UserServices {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerifyTokenRepository verifyTokenRepository;


    private Argon2PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    //สร้าง email กลางเอาไว้เป็นตัวที่จะส่งไปหา user ห้ามใช้ email ตัวเอง
    //ต้องไปตั้ง password ใน manage account --> security --> 2 step email verification
    public void checkDuplication(UserDTO userDTO){
    if(userRepository.existsUserByEmail(userDTO.getEmail())){
        throw new DuplicteItemException("This email already exist");
    }
    if(userDTO.getRole().equalsIgnoreCase("seller")
            && sellerRepository.existsSellerByMobileNumber(userDTO.getMobileNumber())){
        throw new DuplicteItemException("This mobile number already exist");
    }
    if(userDTO.getRole().equalsIgnoreCase("seller") && sellerRepository.existsSellerByNationalId(userDTO.getNationalId())){
        throw new DuplicteItemException("This nationalID already exist");
    }
    }

    @Transactional
    public User createUser(UserDTO userDTO, MultipartFile front,MultipartFile back) throws MessagingException, UnsupportedEncodingException {
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
)
            {
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
        userRepository.save(user);


//        VerifyToken verifyToken = new VerifyToken();
//        verifyToken.setVerifyToken(UUID.randomUUID().toString());
//        verifyToken.setExpiredDate(Instant.now().plus(24, ChronoUnit.HOURS));
//        verifyTokenRepository.saveAndFlush(verifyToken);
//        user.setVerifyTokens(verifyToken);
        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setVerifyToken(UUID.randomUUID().toString());
        verifyToken.setExpiredDate(Instant.now().plus(24, ChronoUnit.HOURS));
        verifyToken.setUser(user);          // 🔥 สำคัญ ต้อง set User ให้ VerifyToken
        verifyTokenRepository.save(verifyToken);
        user.setVerifyTokens(verifyToken);


        userRepository.save(user);

        emailService.sendMailVerification(user.getEmail(),verifyToken.getVerifyToken());
         return user;
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
        if(user.getBuyer() != null && user.getSeller() != null){
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

    //ตรวจสอบ token
    @Transactional
    public boolean verifyEmail(String tokenStr){
        VerifyToken token = userRepository.findVerifyToken(tokenStr).orElse(null);
        if(token == null || token.getExpiredDate().isBefore(Instant.now())){
            return false;
        }

        User user = token.getUser();
        user.setIsActive(true);

        verifyTokenRepository.delete(token);
        user.setVerifyTokens(null);

        userRepository.save(user);
        return true;
    }

//    public String
}

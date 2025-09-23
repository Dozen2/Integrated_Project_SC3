package sit.int221.sc3_server.service.user;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.DTO.Authentication.JwtAuthUser;
import sit.int221.sc3_server.DTO.user.UserDTO;
import sit.int221.sc3_server.DTO.user.UserResponseDTO;
import sit.int221.sc3_server.DTO.user.profile.BuyerProfileDTO;
import sit.int221.sc3_server.DTO.user.profile.SellerProfileDTO;
import sit.int221.sc3_server.DTO.user.profile.UserProfileRequestRTO;
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.DuplicteItemException;
import sit.int221.sc3_server.exception.UnAuthenticateException;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.repository.user.VerifyTokenRepository;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.utils.JwtUtils;
import sit.int221.sc3_server.utils.Role;
import sit.int221.sc3_server.utils.TokenType;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class
UserServices {
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
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private JwtUserDetailService jwtUserDetailService;
    @Autowired
    private AuthenticationManager authenticationManager;



    private Argon2PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    //สร้าง email กลางเอาไว้เป็นตัวที่จะส่งไปหา user ห้ามใช้ email ตัวเอง
    //ต้องไปตั้ง password ใน manage account --> security --> 2 step email verification
    public void checkDuplication(UserDTO userDTO){
    if(buyerRepository.existsBuyerByEmail(userDTO.getEmail())){
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
    public boolean checkIsActive(Integer id){
        Buyer buyer = buyerRepository.findById(id).orElseThrow(()->  new UnAuthorizeException("User not found"));
        if(!buyer.getIsActive()){
            throw new UnAuthenticateException("User is not active");
        }
        return true;
    }
    public Buyer findBuyerBySellerId(Integer id){
       Seller seller =  sellerRepository.findById(id).orElseThrow(()-> new UnAuthorizeException("user not found"));
        Buyer buyer =  seller.getBuyer();
        if(!buyer.getIsActive()){
            throw new UnAuthenticateException("user is not active");
        }
        return buyer;
    }

    @Transactional
    public Buyer createUser(UserDTO userDTO, MultipartFile front,MultipartFile back) throws MessagingException, UnsupportedEncodingException {
        // ✅ ตรวจสอบข้อมูลซ้ำ (อีเมล, ชื่อเล่น ฯลฯ)
        checkDuplication(userDTO);

        // ✅ สร้าง User ใหม่
        Buyer user = new Buyer();
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
            user.getRoles().add(Role.SELLER);
        }

        // ✅ ทุก user เป็น buyer โดย default


        // ✅ บันทึก User
        user.getRoles().add(Role.BUYER);
        buyerRepository.save(user);

        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setVerifyToken(UUID.randomUUID().toString());
        verifyToken.setExpiredDate(Instant.now().plus(24, ChronoUnit.HOURS));
        verifyToken.setBuyer(user);          // 🔥 สำคัญ ต้อง set User ให้ VerifyToken
        verifyTokenRepository.save(verifyToken);
        user.setVerifyToken(verifyToken);


        buyerRepository.save(user);

        emailService.sendMailVerification(user.getEmail(),verifyToken.getVerifyToken());
         return user;
    }
    public UserResponseDTO mapToDTO(Buyer user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNickName(user.getNickName());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setIsActive(user.getIsActive());

        if(user.getSeller() != null){
            dto.setUserType("SELLER");
        }else {
            dto.setUserType("BUYER");
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



    @Transactional
    public boolean verifyEmail(String tokenStr) {
        VerifyToken token = verifyTokenRepository.findByVerifyToken(tokenStr);

        System.out.println("Before check expired");
        if (token.getExpiredDate().isBefore(Instant.now())) {
            return false;
        }


        Buyer user = token.getBuyer();

        System.out.println("After check expired");

        user.setIsActive(true);
        user.setVerifyToken(null);
        verifyTokenRepository.delete(token);// ลบ token ผ่าน orphanRemoval
        buyerRepository.save(user);

        return true;
    }

//    public Map<String,Object> authenticateUser(JwtAuthUser jwtAuthUser){
//        UsernamePasswordAuthenticationToken uToken =
//                new UsernamePasswordAuthenticationToken(jwtAuthUser.getUsername(),jwtAuthUser.getPasswords());
//        authenticationManager.authenticate(uToken);
//        UserDetails userDetails = jwtUserDetailService.loadUserByUsername(jwtAuthUser.getUsername());
//        long refreshTokenAgeInMinute = 24 * 60 * 60 * 1000;
//        return Map.of(
//                "access_token",jwtUtils.generateToken(userDetails),
//                "refresh_token",jwtUtils.generateToken(userDetails,refreshTokenAgeInMinute, TokenType.refresh_token)
//
//        );
//
//    }
//public Map<String,Object> authenticateUser(JwtAuthUser jwtAuthUser){
//    UsernamePasswordAuthenticationToken uToken =
//            new UsernamePasswordAuthenticationToken(jwtAuthUser.getUsername(),jwtAuthUser.getPassword());
//    authenticationManager.authenticate(uToken);
//    UserDetails userDetails = jwtUserDetailService.loadUserByUsername(jwtAuthUser.getUsername());
//    long refreshTokenAgeInMinute = 24*60 ;
//
//    String accessToken = jwtUtils.generateToken(userDetails);
//    String refreshToken = jwtUtils.generateToken(userDetails,refreshTokenAgeInMinute,TokenType.refresh_token);
//    return Map.of(
//            "access_token",accessToken,
//            "refresh_token",refreshToken
//
//    );
//
//}
    public Map<String,Object> authenticateUser(JwtAuthUser jwtAuthUser){
        UsernamePasswordAuthenticationToken uToken =
                new UsernamePasswordAuthenticationToken(jwtAuthUser.getUsername(),jwtAuthUser.getPassword());
        authenticationManager.authenticate(uToken);
        UserDetails userDetails = jwtUserDetailService.loadUserByUsername(jwtAuthUser.getUsername());
        long refreshTokenAgeInMinute = 24*60 ;

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateToken(userDetails,refreshTokenAgeInMinute,TokenType.refresh_token);
        return Map.of(
                "access_token",accessToken,
                "refresh_token",refreshToken

        );

    }

    public Map<String, Object> refreshToken(String refreshToken){
        jwtUtils.verifyToken(refreshToken);
        Map<String,Object> claims = jwtUtils.getJWTClaimSet(refreshToken);
        jwtUtils.isExpired(claims);
    if(!jwtUtils.isValidClaims(claims)){
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                , "Invalid refresh token");
    }
//        System.out.println(claims.get("uid").toString());
        UserDetails userDetails = jwtUserDetailService.loadUserById(Integer.parseInt(claims.get("id").toString()));
    return Map.of("access_token",jwtUtils.generateToken(userDetails));
    }

    public boolean checkPassword(String password,String email){
        Buyer user = buyerRepository.findByUserNameOrEmail(email).orElseThrow(
                ()->new UnAuthorizeException("Email or Password is Incorrect"));
        if(!user.getIsActive()){
            throw new UnAuthenticateException("your account is not active");
        }
        return passwordEncoder.matches(password, user.getPasswords());
    }

    public void emailExpired(String tokenStr) throws MessagingException, UnsupportedEncodingException {
        VerifyToken token = verifyTokenRepository.findByVerifyToken(tokenStr);
        if (token == null) {
            throw new IllegalArgumentException("Token not found: " + tokenStr);
        }

//        token.setVerifyToken(UUID.randomUUID().toString());
        token.setExpiredDate(Instant.now().plus(24, ChronoUnit.HOURS));
        System.out.println(token.getVerifyToken());
        System.out.println(token);
        verifyTokenRepository.save(token);
        emailService.sendMailVerification(token.getBuyer().getEmail(),token.getVerifyToken());
    }

    public BuyerProfileDTO getBuyerById(int id){
        Buyer buyer = buyerRepository.findById(id).orElseThrow(()->new UnAuthorizeException("user not found"));
        return this.mapBuyerDto(buyer);
    }

    public SellerProfileDTO getSeller(int id){
        Buyer buyer = buyerRepository.findById(id).orElseThrow(()->new UnAuthorizeException("user not found"));
        return this.mapSellerDto(buyer);

    }

    public BuyerProfileDTO updateBuyer(UserProfileRequestRTO userProfileRequestRTO,int id){
        Buyer buyer = buyerRepository.findById(id).orElseThrow(()-> new UnAuthorizeException("user not found"));
        buyer.setNickName(userProfileRequestRTO.getNickName());
        buyer.setFullName(userProfileRequestRTO.getFullName());
        Buyer newBuyer = buyerRepository.saveAndFlush(buyer);
        return this.mapBuyerDto(newBuyer);
    }

    public SellerProfileDTO updateSeller(UserProfileRequestRTO userProfileRequestRTO,int id){
        Buyer buyer = buyerRepository.findById(id).orElseThrow(()-> new UnAuthorizeException("user not found"));
        buyer.setNickName(userProfileRequestRTO.getNickName());
        buyer.setFullName(userProfileRequestRTO.getFullName());
        Buyer newBuyer = buyerRepository.saveAndFlush(buyer);
        return this.mapSellerDto(newBuyer);
    }
    public BuyerProfileDTO mapBuyerDto(Buyer buyer){
        BuyerProfileDTO dto = new BuyerProfileDTO();
        dto.setId(buyer.getId());
        dto.setFullName(buyer.getFullName());
        dto.setNickName(buyer.getNickName());
        dto.setEmail(buyer.getEmail());
        dto.setUserType(Role.BUYER.name());

        return dto;
    }

    public SellerProfileDTO mapSellerDto(Buyer buyer){
        SellerProfileDTO dto = new SellerProfileDTO();
        dto.setId(buyer.getId());
        dto.setEmail(buyer.getEmail());
        dto.setFullName(buyer.getFullName());
        dto.setUserType(Role.SELLER.name());
        dto.setPhoneNumber(buyer.getSeller().getMobileNumber());
        dto.setBankName(buyer.getSeller().getBankName());
        dto.setBankAccount(buyer.getSeller().getBankAccountNumber());
        dto.setNickName(buyer.getNickName());
        return dto;

    }

}

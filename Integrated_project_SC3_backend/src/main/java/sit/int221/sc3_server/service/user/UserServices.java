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
import sit.int221.sc3_server.exception.ForbiddenException;
import sit.int221.sc3_server.exception.UnAuthorizeException;
import sit.int221.sc3_server.exception.crudException.CreateFailedException;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;
import sit.int221.sc3_server.service.FileService;
import sit.int221.sc3_server.utils.JwtUtils;
import sit.int221.sc3_server.utils.Role;
import sit.int221.sc3_server.utils.TokenType;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.NoSuchElementException;
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
    private JwtUtils jwtUtils;
    @Autowired
    private JwtUserDetailService jwtUserDetailService;
    @Autowired
    private AuthenticationManager authenticationManager;


    private Argon2PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    //สร้าง email กลางเอาไว้เป็นตัวที่จะส่งไปหา user ห้ามใช้ email ตัวเอง
    //ต้องไปตั้ง password ใน manage account --> security --> 2 step email verification
    public void checkDuplication(UserDTO userDTO) {
        if (buyerRepository.existsBuyerByEmail(userDTO.getEmail())) {
            throw new DuplicteItemException("This email already exist");
        }
        if (userDTO.getRole().equalsIgnoreCase("seller")
                && sellerRepository.existsSellerByMobileNumber(userDTO.getMobileNumber())) {
            throw new DuplicteItemException("This mobile number already exist");
        }
        if (userDTO.getRole().equalsIgnoreCase("seller") && sellerRepository.existsSellerByNationalId(userDTO.getNationalId())) {
            throw new DuplicteItemException("This nationalID already exist");
        }
    }


    public Seller findSellerBySellerId(Integer id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new ForbiddenException("user not found"));
        Buyer buyer = seller.getBuyer();
        if (buyer == null) {
            throw new ForbiddenException("seller has no buyer profile");//should not happen
        }
        if (!buyer.getIsActive()) {
            throw new ForbiddenException("user is not active");
        }
        return seller;

    }

    public Buyer findBuyerByBuyerId(Integer id) {
        return buyerRepository.findById(id).orElseThrow(() -> new ForbiddenException("user not found"));
    }

    public Buyer findBuyerBySellerId(Integer id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new ForbiddenException("user not found"));
        return seller.getBuyer();
    }

    public boolean isValidThaiId(String id) {
        if (id == null || !id.matches("\\d{13}")) return false;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(id.charAt(i)) * (13 - i);
        }
        int checkDigit = (11 - (sum % 11)) % 10;
        return checkDigit == Character.getNumericValue(id.charAt(12));
    }

    @Transactional
    public Buyer createUser(UserDTO userDTO, MultipartFile front, MultipartFile back) throws MessagingException, UnsupportedEncodingException {
        checkDuplication(userDTO);
        if ( userDTO.getNationalId() != null && !isValidThaiId(userDTO.getNationalId())) {
            throw new CreateFailedException("Invalid Thai national ID number");
        }
        Buyer user = new Buyer();
        user.setNickName(userDTO.getNickName());
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setIsActive(false);

        String hashPassword = passwordEncoder.encode(userDTO.getPasswords());
        user.setPasswords(hashPassword);


        if ("seller".equalsIgnoreCase(userDTO.getRole())) {
            if (userDTO.getBankName() == null || userDTO.getBankAccountNumber() == null
                    || userDTO.getNationalId() == null
            ) {
                throw new IllegalArgumentException("Seller details must not be null for seller role");
            }
            String frontFileName = saveNationalIdFile(front);
            String backFileName = saveNationalIdFile(back);
            Seller seller = new Seller();
            seller.setBankName(userDTO.getBankName());
            seller.setMobileNumber(userDTO.getMobileNumber());
            seller.setBankAccountNumber(userDTO.getBankAccountNumber());
            seller.setNationalId(userDTO.getNationalId());
            seller.setNationalIdPhotoFront(frontFileName);
            seller.setNationalIdPhotoBack(backFileName);

            sellerRepository.saveAndFlush(seller);
            user.setSeller(seller);
            user.getRoles().add(Role.SELLER);
        }
        user.getRoles().add(Role.BUYER);
        buyerRepository.save(user);


        buyerRepository.save(user);

        emailService.sendMailVerification(user.getEmail(), user.getEmail());
        return user;
    }


    public UserResponseDTO mapToDTO(Buyer user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNickName(user.getNickName());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setIsActive(user.getIsActive());

        if (user.getSeller() != null) {
            dto.setUserType("SELLER");
        } else {
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
        try {
            String claimEmail = jwtUtils.verifyAndDecodeEmailToken(tokenStr);
            System.out.println("claimEmailInVerifyEmailMethod: " + claimEmail);
            if (claimEmail.equals("EmailExpire")) {
                return false;
            }
            Buyer user = buyerRepository.findByEmailAndNonIsActive(claimEmail);
            user.setIsActive(true);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public Map<String, Object> authenticateUser(JwtAuthUser jwtAuthUser) {
        UsernamePasswordAuthenticationToken uToken =
                new UsernamePasswordAuthenticationToken(jwtAuthUser.getEmail(), jwtAuthUser.getPassword());
        authenticationManager.authenticate(uToken);
        UserDetails userDetails = jwtUserDetailService.loadUserByUsername(jwtAuthUser.getEmail());
//        long refreshTokenAgeInMinute = 24 * 60;
        long refreshTokenAgeInMinute = 5;

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateToken(userDetails, refreshTokenAgeInMinute, TokenType.refresh_token);
        return Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken

        );

    }



    public Map<String, Object> refreshToken(String refreshToken) {
        jwtUtils.verifyToken(refreshToken);
        Map<String, Object> claims = jwtUtils.getJWTClaimSet(refreshToken);
        if (jwtUtils.isExpired(claims)) {
            throw new UnAuthorizeException("token expired");
        }
        if (!jwtUtils.isValidClaims(claims)) {
            throw new UnAuthorizeException("Invalid token");
        }

        Object idObj = claims.get("id");
        if (idObj == null) {
            throw new UnAuthorizeException("dont have id in token");
        }

        Integer buyerId = Integer.parseInt(idObj.toString());
        UserDetails userDetails = jwtUserDetailService.loadUserByBuyerId(buyerId);

        String newAccessToken = jwtUtils.generateToken(userDetails);

        return Map.of("access_token", newAccessToken);
    }


    public boolean checkPassword(String password, String email) {
        Buyer user = buyerRepository.findByUserNameOrEmail(email).orElseThrow(
                () -> new UnAuthorizeException("Email or Password is Incorrect"));
        if (!user.getIsActive()) {
            throw new ForbiddenException("your account is not active");

        }
        return passwordEncoder.matches(password, user.getPasswords());
    }

    public void emailExpired(String tokenStr) throws MessagingException, UnsupportedEncodingException {
        try {
            System.out.println("tokenStr: " + tokenStr);
            String email = jwtUtils.extractEmailFromExpiredAccessToken(tokenStr);
            emailService.sendMailVerification(email, email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public BuyerProfileDTO getBuyerById(int id) {
        Buyer buyer = buyerRepository.findById(id).orElseThrow(() -> new UnAuthorizeException("user not found"));
        return this.mapBuyerDto(buyer);
    }


    public SellerProfileDTO getSeller(int id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new UnAuthorizeException("user not found"));
        Buyer buyer = seller.getBuyer();
        return this.mapSellerDto(buyer);

    }

    public BuyerProfileDTO updateBuyer(UserProfileRequestRTO userProfileRequestRTO, int id) {
        Buyer buyer = buyerRepository.findById(id).orElseThrow(() -> new UnAuthorizeException("user not found"));
        buyer.setNickName(userProfileRequestRTO.getNickName());
        buyer.setFullName(userProfileRequestRTO.getFullName());
        Buyer newBuyer = buyerRepository.saveAndFlush(buyer);
        return this.mapBuyerDto(newBuyer);
    }

    public SellerProfileDTO updateSeller(UserProfileRequestRTO userProfileRequestRTO, int id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new ForbiddenException("user not found"));
        Buyer buyer = seller.getBuyer();
        if (buyer.getSeller() == null) {
            throw new ForbiddenException("user is buyer");
        }
        buyer.setNickName(userProfileRequestRTO.getNickName());
        buyer.setFullName(userProfileRequestRTO.getFullName());
        Buyer newBuyer = buyerRepository.saveAndFlush(buyer);
        return this.mapSellerDto(newBuyer);
    }

    public BuyerProfileDTO mapBuyerDto(Buyer buyer) {
        BuyerProfileDTO dto = new BuyerProfileDTO();
        dto.setId(buyer.getId());
        dto.setFullName(buyer.getFullName());
        dto.setNickName(buyer.getNickName());
        dto.setEmail(buyer.getEmail());
        dto.setUserType(Role.BUYER.name());

        return dto;
    }

    public SellerProfileDTO mapSellerDto(Buyer buyer) {
        if (buyer.getSeller() == null) {
            throw new ForbiddenException("This user has no seller profile");
        }
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


    public void sendResetPasswordEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        boolean exists = buyerRepository.existsByEmailAndIsActive(email.trim().replace("\"", ""),true);
        System.out.println("Check email exists for [" + email + "] = " + exists);

        if (!exists) {
            throw new NoSuchElementException("No user found with email: " + email);
        }


        emailService.sendMailVerityResetPassword(email, email);
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        try {
            //check new and confirm password
            if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("New password and confirm password do not match");
            }

            //decode token and getEmail
            String email = jwtUtils.verifyAndDecodeEmailToken(token);
            System.out.println("Check decode email: " + email);
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email not found in token");
            }
            Buyer buyer = buyerRepository.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("No user found with email: " + email));

            //change password argon
            String hashedPassword = passwordEncoder.encode(newPassword);

            //set new password in repo by email
            buyer.setPasswords(hashedPassword);
            buyerRepository.save(buyer);
            System.out.println("Password has been reset successfully for: " + email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Buyer changePassword(Integer buyerId,String newPassword){
        Buyer buyer = buyerRepository.findById(buyerId).orElseThrow(()->new ForbiddenException("user not found"));
        if(passwordEncoder.matches(newPassword, buyer.getPasswords())){
            throw new ForbiddenException("Your new password is the same as old password");
        }
        buyer.setPasswords(passwordEncoder.encode(newPassword));
        buyerRepository.save(buyer);
        return buyer;
    }

}

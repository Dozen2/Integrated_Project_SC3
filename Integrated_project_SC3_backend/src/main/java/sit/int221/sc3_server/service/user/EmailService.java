package sit.int221.sc3_server.service.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sit.int221.sc3_server.utils.JwtUtils;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender sender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.frontend.url.dev:http://localhost:5173}")
    private String devFrontUrl;
    @Value("${app.frontend.url.prod:http://intproj24.sit.kmutt.ac.th}")
    private String prodFrontendUrl;
    private final Environment environment;

    public EmailService(Environment environment) {
        this.environment = environment;
    }
//    public void sendEmail(String to,String subject,String body) throws MessagingException {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        sender.send(message);
//    }

    public void sendEmail(String to, String subject, String content, boolean isHtml) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, isHtml);
        sender.send(message);
    }


    // ใช้สำหรับ get ชื่อ host #AKA โค้ดจากไอเหย
    private String getHost() {
        String[] activeProfile = environment.getActiveProfiles();
        boolean isDev = activeProfile.length > 0 && activeProfile[0].equals("dev");
        return isDev ? devFrontUrl : prodFrontendUrl;
    }

    // ใช้สำหรับส่ง email verification
    //แก้ path ด้วย
    @Async
    public void sendMailVerification(String to, String token) {
        try {
        String hostPath = getHost();
        String verifyToken = jwtUtils.generateEmailVerifyToken(token);
        String link = hostPath + "/sc3/verify-email?token=" + verifyToken;
        Context context = new Context();
        context.setVariable("verificationLink", link);
        String htmlContent = templateEngine.process("VerifyEmail", context);
        sendEmail(to, "Verify your email", htmlContent, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // ใช้สำหรับส่ง forgot password
//    public void sendForgotPassword(String to,String resetToken) throws MessagingException, UnsupportedEncodingException {
//        String hostPath = getHost();
//        String link = hostPath + "/sc3/verify-email?token=" + resetToken;
//        String body = "Click the link to verify your email:\n\n" + link;
//        MimeMessage message02 = sender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message02, true, "UTF-8");
//        helper.setTo(to);
//        helper.setSubject("Verify your ITBMS account");
//        helper.setFrom("jillterkorn@gmail.com", "ITBMS Team");
//        sendEmail(to,"Reset your password",body);
//    }
}

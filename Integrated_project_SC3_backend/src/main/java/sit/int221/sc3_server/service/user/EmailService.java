package sit.int221.sc3_server.service.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender sender;

    @Value("${app.frontend.url.dev:http://localhost:5173}")
    private String devFrontUrl;
    @Value("${app.frontend.url.prod:http://intproj24.sit.kmutt.ac.th}")
    private String prodFrontendUrl;
    private final Environment environment;

    public EmailService(Environment environment){
        this.environment = environment;
    }
    public void sendEmail(String to,String subject,String body) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }

    // ใช้สำหรับ get ชื่อ host #AKA โค้ดจากไอเหย
    private String getHost(){
        String[] activeProfile =  environment.getActiveProfiles();
        boolean isDev = activeProfile.length > 0 && activeProfile[0].equals("dev");
        return isDev ? devFrontUrl : prodFrontendUrl;
    }
    // ใช้สำหรับส่ง email verification
    //แก้ path ด้วย
    public void sendMailVerification(String to,String token) throws MessagingException, UnsupportedEncodingException {
        String hostPath = getHost();
        String link = hostPath + "/sc3/verify-email?token=" + token;
        String body = "Click the link to verify your email:\n\n" + link;
        MimeMessage message02 = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message02, true, "UTF-8");
        helper.setTo(to);
        sendEmail(to,"Verify your email",body);
    }



    
    // ใช้สำหรับส่ง forgot password
    public void sendForgotPassword(String to,String resetToken) throws MessagingException, UnsupportedEncodingException {
        String hostPath = getHost();
        String link = hostPath + "/sc3/verify-email?token=" + resetToken;
        String body = "Click the link to verify your email:\n\n" + link;
        MimeMessage message02 = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message02, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Verify your ITBMS account");
        helper.setFrom("jillterkorn@gmail.com", "ITBMS Team");
        sendEmail(to,"Reset your password",body);
    }
}

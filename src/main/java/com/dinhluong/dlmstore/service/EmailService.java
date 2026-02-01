package com.dinhluong.dlmstore.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; 
    @Async 
    public void sendVerificationEmail(String to, String name, String link) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String subject = "Xác thực tài khoản - DLM Store";
            
            
            String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>"
                    + "<h2 style='color: #007bff;'>Chào mừng " + name + " đến với DLM Store!</h2>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào nút bên dưới để kích hoạt tài khoản của bạn:</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<a href='" + link + "' style='background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Xác thực ngay</a>"
                    + "</div>"
                    + "<p>Hoặc copy link này vào trình duyệt: <br> <a href='" + link + "'>" + link + "</a></p>"
                    + "<p style='color: #888; font-size: 12px;'>Link này sẽ hết hạn sau 24 giờ.</p>"
                    + "</div>";

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); 

            mailSender.send(mimeMessage);
            System.out.println("Đã gửi mail xác thực tới: " + to);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

  
   
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String subject = "Mã OTP đặt lại mật khẩu - DLM Store";

            String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>"
                    + "<h2 style='color: #dc3545;'>Yêu cầu đặt lại mật khẩu</h2>"
                    + "<p>Bạn vừa yêu cầu đặt lại mật khẩu. Đây là mã OTP của bạn:</p>"
                    + "<div style='text-align: center; margin: 20px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #333; border: 2px dashed #007bff; padding: 10px 20px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p>Mã này sẽ hết hạn sau <strong>5 phút</strong>.</p>"
                    + "<p style='color: red;'>Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "</div>";

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(mimeMessage);
            System.out.println("Đã gửi OTP tới: " + to);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail OTP: " + e.getMessage());
        }
    }
}
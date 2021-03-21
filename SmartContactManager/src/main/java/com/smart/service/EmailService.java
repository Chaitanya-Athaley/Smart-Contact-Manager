package com.smart.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service; 
@Service
public class EmailService {
	
	@Autowired
    private JavaMailSender javaMailSender;
	
	public boolean sendEmail(String subject,String message,String recipient)  
	{     
		boolean flag = true;
		
		//String recipient = "chaitnyaathaley@gmail.com"; 
		// email ID of  Sender. 
		//String sender = "mukundsawalapurkar@gmail.com"; 

		MimeMessage msg = javaMailSender.createMimeMessage();

        // true = multipart message
		
		try {
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(recipient);

        helper.setSubject(subject);

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText("<h1>OTP  "+message+" , Never share your OTP to anyone</h1>", true);

        //FileSystemResource file = new FileSystemResource(new File("classpath:android.png"));

        //Resource resource = new ClassPathResource("android.png");
        //InputStream input = resource.getInputStream();

        //ResourceUtils.getFile("classpath:android.png");

        //helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

        javaMailSender.send(msg);
        
        flag = true;
        
		}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
        
		return flag; 
	} 
}

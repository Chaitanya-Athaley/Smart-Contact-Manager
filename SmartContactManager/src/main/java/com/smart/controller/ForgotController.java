package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	Random random = new Random(1000); 
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//email id form open handler 
	
	
	@GetMapping("/forgot")
	public String openEmail() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession httpSession) {
		
		System.out.println("email:::"+email);
		//Generating 4 digit otp
		
		int otp = random.nextInt(99999);
		System.out.println("otp::"+otp);
		// OTP matching logic
		boolean flag = this.emailService.sendEmail("OTP FROM SCM ", String.valueOf(otp), email);
		
		if(flag==true) {
			
			httpSession.setAttribute("email", email);
			httpSession.setAttribute("myotp", otp);
			return "verify_otp";
		}else {
			httpSession.setAttribute("message", new Message("Check your email id !!","alert-warning"));
			return "forgot_email_form";	
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession  session) {
		int myotp =  (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myotp==otp) {
			//password change form
			
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				//send error message
				session.setAttribute("message", new Message("User does not exist with this email id !!","alert-warning"));
				return "forgot_email_form";	
				
			}else {
				//send change password form
				
				return "password_change_form";
			}
			
		}else {
			session.setAttribute("message", new Message("You have Entered wrong otp","alert-warning"));
			return "verify_otp";
		}
		
	}
	
	//changepassword
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession  session) {
		
		
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		
		String encode = this.bCryptPasswordEncoder.encode(newpassword);
		user.setPassword(encode);
		this.userRepository.save(user);
		
		//session.setAttribute("message", new Message("Your password has changed successfully!!","alert-warning"));
		return "redirect:/signin?change=password changed successfully";
	}
	
	
	
}

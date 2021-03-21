package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		//get the user using usernamne(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);

		model.addAttribute("user",user);

	}


	/// HOME DashBoard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User DashBoard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	//process add Contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			contact.setUser(user);
			// image process upload
			if(file.isEmpty()) {
				//message
				System.out.println("File is Empty");
				contact.setImage("contact.png");
			}else {
				// upload file
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is Uploaded");
			}
			
			user.getContacts().add(contact);
			

			this.userRepository.save(user);

			System.out.println("Data::"+contact);
			System.out.println("Add TO DataBase ");
			
			//Message Success .....
			session.setAttribute("message", new Message("Your contact is Added !! ", "success"));
			
		}catch (Exception e) {
			System.out.println("ERROR:::::::"+e.getMessage());
			e.printStackTrace();
			//Message Error .....
			session.setAttribute("message", new Message("Somthing went wrong !! ", "danger"));

		}
		return"normal/add_contact_form";
	}

	//show contacts
	//per page - 5n
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title", "Show-Contacts");
		/// contact list send kare hai
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		Pageable pageable = PageRequest.of(page, 5); 
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}

	///showing perticuler contact detail
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("CID=="+cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		return "normal/contact_detail";
	}
	
	//delete contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession httpSession,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//check.....
		System.out.println("Contact::"+contact.getcId());
		//contact.setUser(null);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		//remove image
		//contact.getimage()
		
		
		//this.contactRepository.delete(contact);
		System.out.println("DELETED:::");
		httpSession.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model model) {
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		model.addAttribute("title", "Update Contact");
		return "normal/update_form";
	}
	
	//process-update for updating contact
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Model model,HttpSession httpSession,Principal principal) {
		String name = contact.getName();
		int getcId = contact.getcId();
		try {
			//check the image
			// old cont details
			Contact oldcontactDtl = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//file work
				//delete old image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldcontactDtl.getImage());
				file1.delete();
				
				
				
				//update new image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				contact.setImage(oldcontactDtl.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		httpSession.setAttribute("message", new Message("Your Contact Updated Successfully.....", "success"));
		
		
		
		System.out.println("name=="+name);
		System.out.println("getcId=="+getcId);
		model.addAttribute("contact", contact);
		model.addAttribute("title", "Update Contact");
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	
	//your profile handler
	@GetMapping("/profile")
	public String youProfile(Model model,HttpSession httpSession,Principal principal) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//settings
	@GetMapping("/settings")
	public String openSettings(Model model,HttpSession httpSession,Principal principal) {
		
		model.addAttribute("title", "Settings Page");
		return "normal/settings";
	}
	
	//change password 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession httpSession) {
		System.out.println("oldPassword"+oldPassword);
		System.out.println("newPassword"+newPassword);
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		String password = currentUser.getPassword();
		System.out.println("password  :"+password);
		if(this.bCryptPasswordEncoder.matches(oldPassword, password)) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			httpSession.setAttribute("message", new Message("Your Password Successfully Changed","success"));
			
		}
		else {
			//error
			httpSession.setAttribute("message", new Message("Your old Password is Wrong ","danger"));
			return "redirect:/user/settings";
		}
		
		
		
		
		return "redirect:/user/index";
	}



}

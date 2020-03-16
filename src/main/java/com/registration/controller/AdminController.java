package com.registration.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.registration.model.Admin;
import com.registration.model.AdminConfirmationToken;
import com.registration.repository.AdminConfirmationTokenRepository;
import com.registration.repository.AdminRepository;
import com.registration.repository.CollegeConfirmationTokenRepository;
import com.registration.service.EmailService;
import com.registration.service.AdminService;



@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private AdminService adminService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@Autowired
	private AdminConfirmationTokenRepository confirmationTokenRepository;
		
	@Autowired
	public AdminController(AdminService adminService, EmailService emailService) {
		
		this.adminService = adminService;
		this.emailService = emailService;

	}
	
	// Return registration form template
	@RequestMapping(value="/admin_register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, Admin admin){
		modelAndView.addObject("admin", admin);
		modelAndView.setViewName("admin_register");
		return modelAndView;
	}
	
	@RequestMapping(value="/admin_login", method = RequestMethod.GET)
	public ModelAndView showLoginPage(ModelAndView modelAndView, Admin admin) {
		modelAndView.addObject("admin",admin);
		modelAndView.setViewName("admin_login");
		return modelAndView;
	}

	// Process form input data
	@RequestMapping(value = "/admin_register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid Admin admin, BindingResult bindingResult, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		Admin adminExists = adminService.findByEmail(admin.getEmail());
		
		System.out.println(adminExists);
		
		if (adminExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("admin_register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("admin_register");		
		} else { // new instructor so we create instructor and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
		    admin.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    admin.setConfirmationToken(UUID.randomUUID().toString());
		        
		    adminService.saveAdmin(admin);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(admin.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8090/lms/admin/admin_confirm?token=" + admin.getConfirmationToken());
			registrationEmail.setFrom("caramelitservices10@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + admin.getEmail());
			modelAndView.setViewName("admin_register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/admin_confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		Admin admin = adminService.findByConfirmationToken(token);
			
		if (admin == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", admin.getConfirmationToken());
		}
			
		modelAndView.setViewName("admin_confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/admin_confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("admin_confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:instructor_confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		Admin admin = adminService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		admin.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		admin.setEnabled(true);
		
		// Save user
		adminService.saveAdmin(admin);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		modelAndView.setViewName("redirect:/admin/admin_login");
		return modelAndView;		
	}
	
	
	
	
	@RequestMapping(value="/admin_login", method=RequestMethod.POST)
	public ModelAndView loginInstructor(ModelAndView modelAndView, Admin admin, String  password) {
		
		Admin adminExists = adminRepository.findByEmail(admin.getEmail());
		if(adminExists != null) {
			// use encoder.matches to compare raw password with encrypted password

			if (encoder.matches(admin.getPassword(), adminExists.getPassword())){
				// successfully logged in
				modelAndView.addObject("message", "Successfully logged in!");
				modelAndView.setViewName("admin_successLogin");
			} else {
				// wrong password
				modelAndView.addObject("message", "Incorrect password. Try again.");
				modelAndView.setViewName("admin_login");
			}
		} else {	
			modelAndView.addObject("message", "The email provided does not exist!");
			modelAndView.setViewName("admin_login");

		}
		
		return modelAndView;
	}
	
	@RequestMapping(value="/admin_successLogin", method = RequestMethod.GET)
	public String successlogin() {
		return "admin_successLogin";
	}
	
	
		
	
	@RequestMapping(value="/admin_forgot-password", method=RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, Admin admin) {
		modelAndView.addObject("admin", admin);
		modelAndView.setViewName("admin_forgotpassword");
		return modelAndView;
	}

	/**
	 * Receive email of the user, create token and send it via email to the user
	 */
	@RequestMapping(value="/admin_forgot-password", method=RequestMethod.POST)
	public ModelAndView forgotUserPassword(ModelAndView modelAndView, Admin admin) {
		Admin adminExists = adminRepository.findByEmail(admin.getEmail());
		if(adminExists != null) {
			// create token
			AdminConfirmationToken confirmationToken = new AdminConfirmationToken(adminExists);
			
			// save it
			confirmationTokenRepository.save(confirmationToken);
			
			// create the email
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(adminExists.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("caramelitservices10@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here: "
			+"http://103.210.74.133:8090/lms/admin/confirm-reset?token="+confirmationToken.getConfirmationToken());
			
			emailService.sendEmail(mailMessage);

			modelAndView.addObject("message", "Request to reset password received. Check your inbox for the reset link.");
			modelAndView.setViewName("admin_successForgotPassword");

		} else {	
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("admin_error");
		}
		
		return modelAndView;
	}


	@RequestMapping(value="/confirm-reset", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView validateResetToken(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
	{
		AdminConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(token != null) {
			Admin admin = adminRepository.findByEmail(token.getAdmin().getEmail());
			admin.setEnabled(true);
			adminRepository.save(admin);
			modelAndView.addObject("admin", admin);
			modelAndView.addObject("emailId", admin.getEmail());
			modelAndView.setViewName("admin_resetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("admin_error");
		}
		
		return modelAndView;
	}	

	/**
	 * Receive the token from the link sent via email and display form to reset password
	 */
	@RequestMapping(value = "/admin_reset-password", method = RequestMethod.POST)
	public ModelAndView resetAdminPassword(ModelAndView modelAndView, Admin admin) {
		// ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(admin.getEmail() != null) {
			// use email to find user
			Admin tokenInstructor = adminRepository.findByEmail(admin.getEmail());
			tokenInstructor.setEnabled(true);
			tokenInstructor.setPassword(encoder.encode(admin.getPassword()));
			// System.out.println(tokenInstructor.getPassword());
			adminRepository.save(tokenInstructor);
			modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
			modelAndView.setViewName("admin_successResetPassword");
		} else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("admin_error");
		}
		
		return modelAndView;
	}


	
	
	
	
	
	public AdminRepository getAdminRepository() {
		return adminRepository;
	}

	public void setAdminRepository(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public AdminConfirmationTokenRepository getConfirmationTokenRepository() {
		return confirmationTokenRepository;
	}

	public void setConfirmationTokenRepository(AdminConfirmationTokenRepository confirmationTokenRepository) {
		this.confirmationTokenRepository = confirmationTokenRepository;
	}
	
	
}
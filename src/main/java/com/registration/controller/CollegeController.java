package com.registration.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.registration.model.College;
import com.registration.model.CollegeConfirmationToken;
import com.registration.model.ConfirmationToken;
import com.registration.model.Organisation;
import com.registration.model.User;
import com.registration.repository.CollegeConfirmationTokenRepository;
import com.registration.repository.CollegeRepository;
import com.registration.repository.ConfirmationTokenRepository;
import com.registration.repository.UserRepository;
import com.registration.service.CollegeService;
import com.registration.service.EmailService;
import com.registration.service.UserService;

@Controller
@RequestMapping("/college")
public class CollegeController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private CollegeService collegeService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private CollegeRepository collegeRepository;
	@Autowired
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@Autowired
	private CollegeConfirmationTokenRepository collegeConfirmationTokenRepository;
	
	@Autowired
	public CollegeController(CollegeService collegeService, EmailService emailService) {
		
		this.collegeService = collegeService;
		this.emailService = emailService;

	}
	
	// Return registration form template
	@RequestMapping(value="/college_register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, College college){
		modelAndView.addObject("college", college);
		modelAndView.setViewName("college_register");
		return modelAndView;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView saveProduct(@ModelAttribute("college") College college) {
		ModelAndView modelAndView = new ModelAndView();
	
		collegeService.saveCollege(college);
		College colleges = collegeRepository.findByEmail(college.getEmail());
		//List<User> listUsers = userService.listAll();
		//model.addAttribute("listUsers", listUsers);
		modelAndView.addObject("colleges", colleges);	
		
		modelAndView.setViewName("college_successLogin");
	    return modelAndView;
	}
	
	@RequestMapping(value="/college_list", method = RequestMethod.GET)
	public ModelAndView showColleges(ModelAndView modelAndView, College college) {
		List<College> listColleges = collegeService.listAll();
		modelAndView.addObject("listColleges", listColleges);
		modelAndView.setViewName("college_list");
		return modelAndView;
	}
	
	
	@RequestMapping(value="/college_login", method = RequestMethod.GET)
	public ModelAndView showLoginPage(ModelAndView modelAndView, College college) {
		modelAndView.addObject("college",college);
		modelAndView.setViewName("college_login");
		return modelAndView;
	}
	
	

	// Process form input data
	@RequestMapping(value = "/college_register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid College college, BindingResult bindingResult, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		College collegeExists = collegeService.findByEmail(college.getEmail());
		
		System.out.println(collegeExists);
		
		if (collegeExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("college_register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("college_register");		
		} else { // new user so we create user and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
		    college.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    college.setConfirmationToken(UUID.randomUUID().toString());
		        
		    collegeService.saveCollege(college);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(college.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8090/lms/college/college_confirm?token=" + college.getConfirmationToken());
			registrationEmail.setFrom("caramelitservices10@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + college.getEmail());
			modelAndView.setViewName("college_register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/college_confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		College college = collegeService.findByConfirmationToken(token);
			
		if (college == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", college.getConfirmationToken());
		}
			
		modelAndView.setViewName("college_confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/college_confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("college_confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:college_confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		College college = collegeService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		college.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		college.setEnabled(true);
		
		// Save user
		collegeService.saveCollege(college);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		modelAndView.setViewName("redirect:/college/college_login");
		return modelAndView;		
	}
	
	
	
	
	
	
	
	
	
	
	
	@RequestMapping(value="/college_login", method=RequestMethod.POST)
	public ModelAndView loginCollege(ModelAndView modelAndView, College college ,Model model) {
		
		College collegeExists = collegeRepository.findByEmail(college.getEmail());
		if(collegeExists != null) {
			// use encoder.matches to compare raw password with encrypted password

			if (encoder.matches(college.getPassword(), collegeExists.getPassword())){
				// successfully logged in
				College colleges = collegeRepository.findByEmail(college.getEmail());
				List<College> listColleges = collegeService.listAll();
				model.addAttribute("listColleges", listColleges);
				modelAndView.addObject("colleges", colleges);	

				modelAndView.setViewName("college_successLogin");
			} else {
				// wrong password
				modelAndView.addObject("message", "Incorrect password. Try again.");
				modelAndView.setViewName("college_login");
			}
		} else {	
			modelAndView.addObject("message", "The email provided does not exist!");
			modelAndView.setViewName("college_login");

		}
		
		return modelAndView;
	}
	
	
	@RequestMapping(value="/college_forgot-password", method=RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, College college) {
		modelAndView.addObject("college", college);
		modelAndView.setViewName("college_forgot-password");
		return modelAndView;
	}

	/**
	 * Receive email of the user, create token and send it via email to the user
	 */
	@RequestMapping(value="/college_forgot-password", method=RequestMethod.POST)
	public ModelAndView forgotCollegePassword(ModelAndView modelAndView, College college) {
		College collegeExists = collegeRepository.findByEmail(college.getEmail());
		if(collegeExists != null) {
			// create token
			CollegeConfirmationToken confirmationToken = new CollegeConfirmationToken(collegeExists);
			
			// save it
			collegeConfirmationTokenRepository.save(confirmationToken);
			
			// create the email
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(collegeExists.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("caramelitservices10@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here: "
			+"http://103.210.74.133:8090/lms/college/confirm-reset?token="+confirmationToken.getConfirmationToken());
			
			emailService.sendEmail(mailMessage);

			modelAndView.addObject("message", "Request to reset password received. Check your Email inbox for the reset link.");
			modelAndView.setViewName("successForgotPassword");

		} else {	
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}


	@RequestMapping(value="/confirm-reset", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView validateResetToken(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
	{
		CollegeConfirmationToken token = collegeConfirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(token != null) {
			College college = collegeRepository.findByEmail(token.getCollege().getEmail());
			college.setEnabled(true);
			collegeRepository.save(college);
			modelAndView.addObject("college", college);
			modelAndView.addObject("emailId", college.getEmail());
			modelAndView.setViewName("college_resetPassword");
			
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}	

	/**
	 * Receive the token from the link sent via email and display form to reset password
	 */
	@RequestMapping(value = "/college_reset-password", method = RequestMethod.POST)
	public ModelAndView resetUserPassword(ModelAndView modelAndView, College college) {
		// ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(college.getEmail() != null) {
			// use email to find user
			College tokenUser = collegeRepository.findByEmail(college.getEmail());
			tokenUser.setEnabled(true);
			tokenUser.setPassword(encoder.encode(college.getPassword()));
			// System.out.println(tokenUser.getPassword());
			collegeRepository.save(tokenUser);
			modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
			modelAndView.setViewName("college_successResetPassword");
		} else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}


	
	
	
	
	
	public CollegeRepository getCollegeRepository() {
		return collegeRepository;
	}

	public void setCollegeRepository(CollegeRepository collegeRepository) {
		this.collegeRepository = collegeRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public CollegeConfirmationTokenRepository getCollegeConfirmationTokenRepository() {
		return collegeConfirmationTokenRepository;
	}

	public void setCollegeConfirmationTokenRepository(CollegeConfirmationTokenRepository collegeConfirmationTokenRepository) {
		this.collegeConfirmationTokenRepository = collegeConfirmationTokenRepository;
	}
	

}

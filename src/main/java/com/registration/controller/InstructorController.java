package com.registration.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.registration.model.Instructor;
import com.registration.repository.InstructorRepository;
import com.registration.service.EmailService;
import com.registration.service.InstructorService;
import com.registration.model.InstructorConfirmationToken;
import com.registration.model.User;
import com.registration.repository.InstructorConfirmationTokenRepository;


@Controller
@RequestMapping("/instructor")
public class InstructorController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private InstructorService instructorService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private InstructorRepository instructorRepository;
	@Autowired
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@Autowired
	private InstructorConfirmationTokenRepository confirmationTokenRepository;
	
	@Autowired
	public InstructorController(InstructorService instructorService, EmailService emailService) {
		
		this.instructorService = instructorService;
		this.emailService = emailService;

	}
	
	@RequestMapping(value="/instructor_list", method = RequestMethod.GET)
	public ModelAndView showInstructors(ModelAndView modelAndView, Instructor instructor) {
		
		List<Instructor> listInstructors = instructorService.listAll();
		modelAndView.addObject("listInstructors", listInstructors);
		modelAndView.setViewName("instructor_list");
		return modelAndView;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView saveProduct(@ModelAttribute("instructor") Instructor instructor) {
		ModelAndView modelAndView = new ModelAndView();
	
	    instructorService.saveInstructor(instructor);
		Instructor instructors = instructorRepository.findByEmail(instructor.getEmail());
		//List<User> listUsers = userService.listAll();
		//model.addAttribute("listUsers", listUsers);
		modelAndView.addObject("instructors", instructors);	
		
		modelAndView.setViewName("instructor_successLogin");
	    return modelAndView;
	}
	
	// Return registration form template
	@RequestMapping(value="/instructor_register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, Instructor instructor){
		modelAndView.addObject("instructor", instructor);
		modelAndView.setViewName("instructor_register");
		return modelAndView;
	}
	
	@RequestMapping(value="/instructor_login", method = RequestMethod.GET)
	public ModelAndView showLoginPage(ModelAndView modelAndView, Instructor instructor) {
		modelAndView.addObject("instructor",instructor);
		modelAndView.setViewName("instructor_login");
		return modelAndView;
	}

	// Process form input data
	@RequestMapping(value = "/instructor_register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid Instructor instructor, BindingResult bindingResult, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		Instructor instructorExists = instructorService.findByEmail(instructor.getEmail());
		
		System.out.println(instructorExists);
		
		if (instructorExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("instructor_register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("instructor_register");		
		} else { // new instructor so we create instructor and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
		    instructor.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    instructor.setConfirmationToken(UUID.randomUUID().toString());
		        
		    instructorService.saveInstructor(instructor);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(instructor.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8090/lms/instructor/instructor_confirm?token=" + instructor.getConfirmationToken());
			registrationEmail.setFrom("caramelitservices10@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + instructor.getEmail());
			modelAndView.setViewName("instructor_register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/instructor_confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		Instructor instructor = instructorService.findByConfirmationToken(token);
			
		if (instructor == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", instructor.getConfirmationToken());
		}
			
		modelAndView.setViewName("instructor_confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/instructor_confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("instructor_confirm");
		
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
		Instructor instructor = instructorService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		instructor.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		instructor.setEnabled(true);
		
		// Save user
		instructorService.saveInstructor(instructor);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		modelAndView.setViewName("redirect:/instructor/instructor_login");
		return modelAndView;		
	}
	
	
	
	
	@RequestMapping(value="/instructor_login", method=RequestMethod.POST)
	public ModelAndView loginInstructor(ModelAndView modelAndView, Instructor instructor,Model model) {
		
		Instructor instructorExists = instructorRepository.findByEmail(instructor.getEmail());
		if(instructorExists != null) {
			// use encoder.matches to compare raw password with encrypted password

			if (encoder.matches(instructor.getPassword(), instructorExists.getPassword())){
				// successfully logged in
				Instructor instructors = instructorRepository.findByEmail(instructor.getEmail());
				List<Instructor> listInstructors = instructorService.listAll();
				model.addAttribute("listInstructors", listInstructors);
				modelAndView.addObject("instructors", instructors);	
				modelAndView.setViewName("instructor_successLogin");
			} else {
				// wrong password
				modelAndView.addObject("message", "Incorrect password. Try again.");
				modelAndView.setViewName("instructor_login");
			}
		} else {	
			modelAndView.addObject("message", "The email provided does not exist!");
			modelAndView.setViewName("instructor_login");

		}
		
		return modelAndView;
	}
	
	
	@RequestMapping(value="/instructor_forgot-password", method=RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, Instructor instructor) {
		modelAndView.addObject("instructor", instructor);
		modelAndView.setViewName("instructor_forgotPassword");
		return modelAndView;
	}

	/**
	 * Receive email of the user, create token and send it via email to the user
	 */
	@RequestMapping(value="/instructor_forgot-password", method=RequestMethod.POST)
	public ModelAndView forgotUserPassword(ModelAndView modelAndView, Instructor instructor) {
		Instructor instructorExists = instructorRepository.findByEmail(instructor.getEmail());
		if(instructorExists != null) {
			// create token
			InstructorConfirmationToken confirmationToken = new InstructorConfirmationToken(instructorExists);
			
			// save it
			confirmationTokenRepository.save(confirmationToken);
			
			// create the email
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(instructorExists.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("caramelitservices10@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here: "
			+"http://103.210.74.133:8090/lms/instructor/confirm-reset?token="+confirmationToken.getConfirmationToken());
			
			emailService.sendEmail(mailMessage);

			modelAndView.addObject("message", "Request to reset password received. Check your inbox for the reset link.");
			modelAndView.setViewName("instructor_successForgotPassword");

		} else {	
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("instructor_error");
		}
		
		return modelAndView;
	}


	@RequestMapping(value="/confirm-reset", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView validateResetToken(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
	{
		InstructorConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(token != null) {
			Instructor instructor = instructorRepository.findByEmail(token.getInstructor().getEmail());
			instructor.setEnabled(true);
			instructorRepository.save(instructor);
			modelAndView.addObject("instructor", instructor);
			modelAndView.addObject("emailId", instructor.getEmail());
			modelAndView.setViewName("instructor_resetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("instructor_error");
		}
		
		return modelAndView;
	}	

	/**
	 * Receive the token from the link sent via email and display form to reset password
	 */
	@RequestMapping(value = "/instructor_reset-password", method = RequestMethod.POST)
	public ModelAndView resetInstructorPassword(ModelAndView modelAndView, Instructor instructor) {
		// ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(instructor.getEmail() != null) {
			// use email to find user
			Instructor tokenInstructor = instructorRepository.findByEmail(instructor.getEmail());
			tokenInstructor.setEnabled(true);
			tokenInstructor.setPassword(encoder.encode(instructor.getPassword()));
			// System.out.println(tokenInstructor.getPassword());
			instructorRepository.save(tokenInstructor);
			modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
			modelAndView.setViewName("instructor_successResetPassword");
		} else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("instructor_error");
		}
		
		return modelAndView;
	}


	
	
	
	
	
	public InstructorRepository getUserRepository() {
		return instructorRepository;
	}

	public void setUserRepository(InstructorRepository instructorRepository) {
		this.instructorRepository = instructorRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public InstructorConfirmationTokenRepository getConfirmationTokenRepository() {
		return confirmationTokenRepository;
	}

	public void setConfirmationTokenRepository(InstructorConfirmationTokenRepository confirmationTokenRepository) {
		this.confirmationTokenRepository = confirmationTokenRepository;
	}
	
	
}
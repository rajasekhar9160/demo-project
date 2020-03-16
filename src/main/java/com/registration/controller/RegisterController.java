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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.registration.model.User;
import com.registration.repository.UserRepository;
import com.registration.service.EmailService;
import com.registration.service.UserService;
import com.registration.model.ConfirmationToken;
import com.registration.repository.ConfirmationTokenRepository;


@Controller
@SessionAttributes("email")
@RequestMapping("/user")
public class RegisterController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserService userService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	
	
	@Autowired
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@Autowired
	private ConfirmationTokenRepository confirmationTokenRepository;
	
	@Autowired
	public RegisterController(UserService userService, EmailService emailService) {
		
		this.userService = userService;
		this.emailService = emailService;
		
	}
	
	
	@RequestMapping(value="/user_list", method = RequestMethod.GET)
	public ModelAndView showUsers(ModelAndView modelAndView, User user) {
		
		List<User> listUsers = userService.listAll();
		modelAndView.addObject("listUsers", listUsers);
		modelAndView.setViewName("user_list");
		
		return modelAndView;
	}
	
	@RequestMapping("/edit/{email}")
	public ModelAndView showEditProductPage(@PathVariable(name = "email") String email) {
	    ModelAndView mav = new ModelAndView("edit_user");
	    User user = userService.findByEmail(email);
	    mav.addObject("user", user);
	     
	    return mav;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView saveProduct(@ModelAttribute("user") User user) {
		ModelAndView modelAndView = new ModelAndView();
	
	    userService.saveUser(user);
		User users = userRepository.findByEmail(user.getEmail());
		//List<User> listUsers = userService.listAll();
		//model.addAttribute("listUsers", listUsers);
		modelAndView.addObject("users", users);	
		
		modelAndView.setViewName("successLogin");
	    return modelAndView;
	}
	/*
	@RequestMapping(value = "/profile",method = RequestMethod.GET)
	public ModelAndView viewProfile(@ModelAttribute("user") User user) {
		ModelAndView mav = new ModelAndView();
		userService.saveUser(user);
		User users = userRepository.findByEmail(user.getEmail());
		mav.addObject("users", users);
		mav.setViewName("profile");
		return mav;
	}*/
		
	// Return registration form template
	@RequestMapping(value="/register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user){
		modelAndView.addObject("user", user);
		modelAndView.setViewName("register");
		return modelAndView;
	}
	
	@RequestMapping(value="/login", method = RequestMethod.GET)
	public ModelAndView showLoginPage(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user",user);
		modelAndView.setViewName("login");
		return modelAndView;
	}
	
	
	
	
	
	// Process form input data
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user, BindingResult bindingResult, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		User userExists = userService.findByEmail(user.getEmail());
		
		System.out.println(userExists);
		
		if (userExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("register");		
		} else { // new user so we create user and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
		    user.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    user.setConfirmationToken(UUID.randomUUID().toString());
		        
		    userService.saveUser(user);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(user.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8090/lms/user/confirm?token=" + user.getConfirmationToken());
			registrationEmail.setFrom("caramelitservices10@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
			modelAndView.setViewName("register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		User user = userService.findByConfirmationToken(token);
			
		if (user == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", user.getConfirmationToken());
		}
			
		modelAndView.setViewName("confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		User user = userService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		user.setEnabled(true);
		
		// Save user
		userService.saveUser(user);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		modelAndView.setViewName("redirect:/user/login");
		return modelAndView;		
	}
	

	

	@RequestMapping(value="/login", method=RequestMethod.POST)
	public ModelAndView loginUser(@RequestParam String email,User user,Model model) {
		ModelAndView modelAndView = new ModelAndView();
		
		User userExists = userRepository.findByEmail(user.getEmail());
		
		if(userExists != null) {
			// use encoder.matches to compare raw password with encrypted password

			if (encoder.matches(user.getPassword(), userExists.getPassword())){
			
				User users = userRepository.findByEmail(user.getEmail());
				List<User> listUsers = userService.listAll();
				model.addAttribute("listUsers", listUsers);
				modelAndView.addObject("users", users);	
				modelAndView.setViewName("successLogin");
		
			} else {
				// wrong password
				
				modelAndView.addObject("message", "Incorrect password. Try again.");
				modelAndView.setViewName("login");
			}
		} else {	
			modelAndView.addObject("message", "The email provided does not exist!");
			modelAndView.setViewName("login");

		}
		
		return modelAndView;
	}
	


	@RequestMapping(value="/forgot-password", method=RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user", user);
		modelAndView.setViewName("forgotPassword");
		return modelAndView;
	}

	/**
	 * Receive email of the user, create token and send it via email to the user
	 */
	@RequestMapping(value="/forgot-password", method=RequestMethod.POST)
	public ModelAndView forgotUserPassword(ModelAndView modelAndView, User user) {
		User userExists = userRepository.findByEmail(user.getEmail());
		if(userExists != null) {
			// create token
			ConfirmationToken confirmationToken = new ConfirmationToken(userExists);
			
			// save it
			confirmationTokenRepository.save(confirmationToken);
			
			// create the email
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(userExists.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("caramelitservices10@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here: "
			+"http://103.210.74.133:8090/lms/user/confirm-reset?token="+confirmationToken.getConfirmationToken());
			
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
		ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(token != null) {
			User user = userRepository.findByEmail(token.getUser().getEmail());
			user.setEnabled(true);
			userRepository.save(user);
			modelAndView.addObject("user", user);
			modelAndView.addObject("emailId", user.getEmail());
			modelAndView.setViewName("resetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}	

	/**
	 * Receive the token from the link sent via email and display form to reset password
	 */
	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public ModelAndView resetUserPassword(ModelAndView modelAndView, User user) {
		// ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(user.getEmail() != null) {
			// use email to find user
			User tokenUser = userRepository.findByEmail(user.getEmail());
			tokenUser.setEnabled(true);
			tokenUser.setPassword(encoder.encode(user.getPassword()));
			// System.out.println(tokenUser.getPassword());
			userRepository.save(tokenUser);
			modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
			modelAndView.setViewName("successResetPassword");
		} else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}	
	
	
	/*private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
	
	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		DBFile dbFile = userService.StoreFile(file);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/downloadFile/")
				.path(dbFile.getId())
				.toUriString();
		
		return new UploadFileResponse(dbFile.getFileName(),fileDownloadUri,file.getContentType(),file.getSize());
	}
	
	*/
		

	/*
	@RequestMapping("/edit_user_profile/{id}")
	public ModelAndView showEditProductPage(@PathVariable(name = "id") int id) {
	    ModelAndView mav = new ModelAndView("edit_user_profile");
	    User user = userService.get(id);
	    mav.addObject("user", user);
	     
	    return mav;
	}*/
	
	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public ConfirmationTokenRepository getConfirmationTokenRepository() {
		return confirmationTokenRepository;
	}

	public void setConfirmationTokenRepository(ConfirmationTokenRepository confirmationTokenRepository) {
		this.confirmationTokenRepository = confirmationTokenRepository;
	}
	
	
}
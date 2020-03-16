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
import com.registration.model.College;
import com.registration.model.Instructor;
import com.registration.model.Organisation;
import com.registration.repository.OrganisationRepository;
import com.registration.service.EmailService;
import com.registration.service.OrganisationService;
import com.registration.model.organisationConfirmationToken;
import com.registration.repository.OrganisationConfirmationTokenRepository;


@Controller
@RequestMapping("/organisation")
public class OrganisationController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private OrganisationRepository organisationRepository;
	@Autowired
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	@Autowired
	private OrganisationConfirmationTokenRepository confirmationTokenRepository;
	
	@Autowired
	public OrganisationController(OrganisationService organisationService, EmailService emailService) {
		
		this.organisationService = organisationService;
		this.emailService = emailService;

	}
	
	// Return registration form template
	@RequestMapping(value="/organisation_register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, Organisation organisation){
		modelAndView.addObject("organisation", organisation);
		modelAndView.setViewName("organisation_register");
		return modelAndView;
	}
	
	@RequestMapping(value="/organisation_login", method = RequestMethod.GET)
	public ModelAndView showLoginPage(ModelAndView modelAndView, Organisation organisation) {
		modelAndView.addObject("organisation",organisation);
		modelAndView.setViewName("organisation_login");
		return modelAndView;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView saveProduct(@ModelAttribute("organisation") Organisation organisation) {
		ModelAndView modelAndView = new ModelAndView();
	
	    organisationService.saveOrganisation(organisation);
		Organisation organisations = organisationRepository.findByEmail(organisation.getEmail());
		//List<User> listUsers = userService.listAll();
		//model.addAttribute("listUsers", listUsers);
		modelAndView.addObject("organisations", organisations);	
		
		modelAndView.setViewName("organisation_successLogin");
	    return modelAndView;
	}
	
	
	@RequestMapping(value="/organisation_list", method = RequestMethod.GET)
	public ModelAndView showColleges(ModelAndView modelAndView, College college) {
		List<Organisation> listOrganisations = organisationService.listAll();
		modelAndView.addObject("listOrganisations", listOrganisations);
		modelAndView.setViewName("organisation_list");
		return modelAndView;
	}
	
	// Process form input data
	@RequestMapping(value = "/organisation_register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid Organisation organisation, BindingResult bindingResult, HttpServletRequest request) {
				
		// Lookup user in database by e-mail
		Organisation organisationExists = organisationService.findByEmail(organisation.getEmail());
		
		System.out.println(organisationExists);
		
		if (organisationExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("organisation_register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("organisation_register");		
		} else { // new instructor so we create instructor and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
			organisation.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
			organisation.setConfirmationToken(UUID.randomUUID().toString());
		        
			organisationService.saveOrganisation(organisation);
				
			String appUrl = request.getScheme() + "://" + request.getServerName();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(organisation.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + ":8090/lms/organisation/organisation_confirm?token=" + organisation.getConfirmationToken());
			registrationEmail.setFrom("caramelitservices10@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + organisation.getEmail());
			modelAndView.setViewName("organisation_register");
		}
			
		return modelAndView;
	}
	
	// Process confirmation link
	@RequestMapping(value="/organisation_confirm", method = RequestMethod.GET)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, @RequestParam("token") String token) {
			
		Organisation organisation = organisationService.findByConfirmationToken(token);
			
		if (organisation == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", organisation.getConfirmationToken());
		}
			
		modelAndView.setViewName("organisation_confirm");
		return modelAndView;		
	}
	
	// Process confirmation link
	@RequestMapping(value="/organisation_confirm", method = RequestMethod.POST)
	public ModelAndView confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {
				
		modelAndView.setViewName("organisation_confirm");
		
		Zxcvbn passwordCheck = new Zxcvbn();
		
		Strength strength = passwordCheck.measure(requestParams.get("password"));
		
		if (strength.getScore() < 3) {
			//modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
			bindingResult.reject("password");
			
			redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

			modelAndView.setViewName("redirect:organisation_confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}
	
		// Find the user associated with the reset token
		Organisation organisation = organisationService.findByConfirmationToken(requestParams.get("token"));

		// Set new password
		organisation.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

		// Set user to enabled
		organisation.setEnabled(true);
		
		// Save user
		organisationService.saveOrganisation(organisation);
		
		modelAndView.addObject("successMessage", "Your password has been set!");
		modelAndView.setViewName("redirect:/organisation/organisation_login");
		return modelAndView;		
	}
	
	
	
	
	@RequestMapping(value="/organisation_login", method=RequestMethod.POST)
	public ModelAndView loginOrganisation(ModelAndView modelAndView, Organisation organisation, Model model) {
		
		Organisation organisationExists = organisationRepository.findByEmail(organisation.getEmail());
		if(organisationExists != null) {
			// use encoder.matches to compare raw password with encrypted password

			if (encoder.matches(organisation.getPassword(), organisationExists.getPassword())){
				// successfully logged in
				Organisation organisations = organisationRepository.findByEmail(organisation.getEmail());
				List<Organisation> listOrganisations = organisationService.listAll();
				model.addAttribute("listOrganisations", listOrganisations);
				modelAndView.addObject("organisations", organisations);	
				modelAndView.setViewName("organisation_successLogin");
			} else {
				// wrong password
				modelAndView.addObject("message", "Incorrect password. Try again.");
				modelAndView.setViewName("organisation_login");
			}
		} else {	
			modelAndView.addObject("message", "The email provided does not exist!");
			modelAndView.setViewName("organisation_login");

		}
		
		return modelAndView;
	}
	
	
	@RequestMapping(value="/organisation_forgot-password", method=RequestMethod.GET)
	public ModelAndView displayResetPassword(ModelAndView modelAndView, Organisation organisation) {
		modelAndView.addObject("organisation", organisation);
		modelAndView.setViewName("organisation_forgotPassword");
		return modelAndView;
	}

	/**
	 * Receive email of the user, create token and send it via email to the user
	 */
	@RequestMapping(value="/organisation_forgot-password", method=RequestMethod.POST)
	public ModelAndView forgotOrganisationPassword(ModelAndView modelAndView, Organisation organisation) {
		Organisation organisationExists = organisationRepository.findByEmail(organisation.getEmail());
		if(organisationExists != null) {
			// create token
			organisationConfirmationToken confirmationToken = new organisationConfirmationToken(organisationExists);
			
			// save it
			confirmationTokenRepository.save(confirmationToken);
			
			// create the email
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(organisationExists.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom("caramelitservices10@gmail.com");
			mailMessage.setText("To complete the password reset process, please click here: "
			+"http://103.210.74.133:8090/lms/organisation/confirm-reset?token="+confirmationToken.getConfirmationToken());
			
			emailService.sendEmail(mailMessage);

			modelAndView.addObject("message", "Request to reset password received. Check your inbox for the reset link.");
			modelAndView.setViewName("organisation_successForgotPassword");

		} else {	
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("organisation_error");
		}
		
		return modelAndView;
	}


	@RequestMapping(value="/confirm-reset", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView validateResetToken(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
	{
		organisationConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(token != null) {
			Organisation organisation = organisationRepository.findByEmail(token.getOrganisation().getEmail());
			organisation.setEnabled(true);
			organisationRepository.save(organisation);
			modelAndView.addObject("organisation", organisation);
			modelAndView.addObject("emailId", organisation.getEmail());
			modelAndView.setViewName("organisation_resetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("organisation_error");
		}
		
		return modelAndView;
	}	

	/**
	 * Receive the token from the link sent via email and display form to reset password
	 */
	@RequestMapping(value = "/organisation_reset-password", method = RequestMethod.POST)
	public ModelAndView resetOrganisationPassword(ModelAndView modelAndView, Organisation organisation) {
		// ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
		
		if(organisation.getEmail() != null) {
			// use email to find user
			Organisation tokenOrganisation = organisationRepository.findByEmail(organisation.getEmail());
			tokenOrganisation.setEnabled(true);
			tokenOrganisation.setPassword(encoder.encode(organisation.getPassword()));
			// System.out.println(tokenInstructor.getPassword());
			organisationRepository.save(tokenOrganisation);
			modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
			modelAndView.setViewName("organisation_successResetPassword");
		} else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("organisation_error");
		}
		
		return modelAndView;
	}


	
	
	
	
	
	public OrganisationRepository getOrganisationRepository() {
		return organisationRepository;
	}

	public void setOrganisationRepository(OrganisationRepository organisationRepository) {
		this.organisationRepository = organisationRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public OrganisationConfirmationTokenRepository getConfirmationTokenRepository() {
		return confirmationTokenRepository;
	}

	public void setConfirmationTokenRepository(OrganisationConfirmationTokenRepository confirmationTokenRepository) {
		this.confirmationTokenRepository = confirmationTokenRepository;
	}
	
	
}
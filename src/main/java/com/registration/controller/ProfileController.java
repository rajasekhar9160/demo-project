package com.registration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.registration.model.Profile;
import com.registration.service.ProfileService;

@Controller
@RequestMapping("/profile")
public class ProfileController {
	
	@Autowired
	private ProfileService profileService;
	
	@RequestMapping("/")
	public String viewHomePage(Model model) {
	    List<Profile> listProfiles = profileService.listAll();
	    model.addAttribute("listProfiles", listProfiles);
	     
	    return "index";
	}
	
	@RequestMapping("/new_profile")
	public String showNewProductPage(Model model) {
	    Profile profile = new Profile();
	    model.addAttribute("profile", profile);
	     
	    return "new_profile";
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@ModelAttribute("profile") Profile profile) {
	    profileService.save(profile);
	     
	    return "redirect:/";
	}
}

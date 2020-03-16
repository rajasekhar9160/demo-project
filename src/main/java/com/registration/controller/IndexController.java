package com.registration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.registration.model.Course;
import com.registration.service.CourseService;

@Controller
public class IndexController {
	
	@Autowired
	private CourseService courseService;
	
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	public String index(Model model) {
		List<Course> listCourses = courseService.listAll();
		model.addAttribute("listCourses",listCourses);
		return "index";
	}
		
	
}

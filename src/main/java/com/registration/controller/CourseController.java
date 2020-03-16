package com.registration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.registration.model.Course;
import com.registration.service.CourseService;

@Controller
@RequestMapping("/course")
public class CourseController {
	
	@Autowired
	private CourseService courseService;
	
	@RequestMapping(value = "/list_course", method = RequestMethod.GET)
	public String viewHomePage(Model model) {
		List<Course> listCourses = courseService.listAll();
		model.addAttribute("listCourses", listCourses);
		return "courses";
	}
	
	@RequestMapping(value = "/new_course", method = RequestMethod.GET)
	public String showNewCoursePage(Model model) {
		Course course = new Course();
		model.addAttribute("course", course);
		return "new_course";
	}
	
	@RequestMapping(value = "/save_course", method = RequestMethod.POST)
	public String saveCourse(@ModelAttribute("course") Course course) {
		courseService.save(course);
		return "redirect:/course/list_course";
	}
	@RequestMapping("/edit/{id}")
	public ModelAndView showEditProductPage(@PathVariable(name = "id") int id) {
	    ModelAndView mav = new ModelAndView("edit_course");
	    Course course = courseService.get(id);
	    mav.addObject("course", course);
	     
	    return mav;
	}
}

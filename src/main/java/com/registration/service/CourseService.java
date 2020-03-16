package com.registration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.Course;
import com.registration.repository.CourseRepository;

@Service("courseService")
public class CourseService {
	
	@Autowired
	private CourseRepository courseRepository;
	
	public List<Course> listAll(){
		return courseRepository.findAll();
	}
	
	public void save(Course course) {
		courseRepository.save(course);
	}
	
	public Course get(long id) {
		return courseRepository.findById(id).get();
	}
	
	public void delete(long id) {
        courseRepository.deleteById(id);
    }
}

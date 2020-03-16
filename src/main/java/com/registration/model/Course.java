package com.registration.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "course")
public class Course {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "courseName")
	private String courseName;
	
	@Column(name = "courseContent1")
	private String courseContent1;
	
	@Column(name = "courseContent1a")
	private String courseContent1a;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getCourseContent1() {
		return courseContent1;
	}

	public void setCourseContent1(String courseContent1) {
		this.courseContent1 = courseContent1;
	}

	public String getCourseContent1a() {
		return courseContent1a;
	}

	public void setCourseContent1a(String courseContent1a) {
		this.courseContent1a = courseContent1a;
	}
	
	
	
}

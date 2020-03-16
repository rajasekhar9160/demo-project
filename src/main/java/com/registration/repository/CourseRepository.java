package com.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.registration.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long>{

}

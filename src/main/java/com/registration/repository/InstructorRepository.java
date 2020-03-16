package com.registration.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.registration.model.Instructor;

@Repository("instructorRepository")
public interface InstructorRepository extends CrudRepository<Instructor, Long> {
	
	 Instructor findByEmail(String email);
	 Instructor findByConfirmationToken(String confirmationToken);
	
}

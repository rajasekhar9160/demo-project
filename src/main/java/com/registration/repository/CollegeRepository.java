package com.registration.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.registration.model.College;

@Repository("collegeRepository")
public interface CollegeRepository extends CrudRepository<College, Long>{
	College findByEmail(String email);
	College findByConfirmationToken(String confirmationToken);
}

package com.registration.repository;

import org.springframework.data.repository.CrudRepository;

import com.registration.model.CollegeConfirmationToken;


public interface CollegeConfirmationTokenRepository extends CrudRepository<CollegeConfirmationToken, String>{
	CollegeConfirmationToken findByConfirmationToken(String confirmationToken);
}

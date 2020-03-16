package com.registration.repository;

import org.springframework.data.repository.CrudRepository;

import com.registration.model.InstructorConfirmationToken;

public interface InstructorConfirmationTokenRepository extends CrudRepository<InstructorConfirmationToken, String>{
	InstructorConfirmationToken findByConfirmationToken(String confirmationToken);
}

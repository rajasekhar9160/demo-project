package com.registration.repository;

import org.springframework.data.repository.CrudRepository;

import com.registration.model.AdminConfirmationToken;

public interface AdminConfirmationTokenRepository extends CrudRepository<AdminConfirmationToken, String> {
	AdminConfirmationToken findByConfirmationToken(String confirmationToken);
}

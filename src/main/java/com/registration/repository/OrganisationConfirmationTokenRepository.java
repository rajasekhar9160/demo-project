package com.registration.repository;

import org.springframework.data.repository.CrudRepository;

import com.registration.model.organisationConfirmationToken;

public interface OrganisationConfirmationTokenRepository extends CrudRepository<organisationConfirmationToken, String>{
	organisationConfirmationToken findByConfirmationToken(String confirmationToken);
}

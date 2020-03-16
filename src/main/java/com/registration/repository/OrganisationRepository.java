package com.registration.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.registration.model.Organisation;

@Repository("organisationRepository")
public interface OrganisationRepository extends CrudRepository<Organisation, Long> {
	
	 Organisation findByEmail(String email);
	 Organisation findByConfirmationToken(String confirmationToken);
	
}

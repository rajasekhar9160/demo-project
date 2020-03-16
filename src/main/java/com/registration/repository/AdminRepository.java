package com.registration.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.registration.model.Admin;

@Repository("adminRepository")
public interface AdminRepository extends CrudRepository<Admin, Long> {
	
	 Admin findByEmail(String email);
	 
	 Admin findByConfirmationToken(String confirmationToken);
	
}

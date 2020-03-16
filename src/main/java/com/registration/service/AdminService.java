package com.registration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.Admin;
import com.registration.repository.AdminRepository;

@Service("adminService")
public class AdminService {
	@Autowired
	private AdminRepository adminRepository;

	
	public AdminService(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}
	
	public Admin findByEmail(String email) {
		return adminRepository.findByEmail(email);
	}
	
	
	public Admin findByConfirmationToken(String confirmationToken) {
		return adminRepository.findByConfirmationToken(confirmationToken);
	}
	
	
	
	public void saveAdmin(Admin admin) {
		adminRepository.save(admin);
	}


}
package com.registration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.Instructor;
import com.registration.model.Organisation;
import com.registration.repository.OrganisationRepository;

@Service("organisationService")
public class OrganisationService {
	@Autowired
	private OrganisationRepository organisationRepository;

	
	public OrganisationService(OrganisationRepository organisationRepository) {
		this.organisationRepository = organisationRepository;
	}
	
	public Organisation findByEmail(String email) {
		return organisationRepository.findByEmail(email);
	}
	
	
	public Organisation findByConfirmationToken(String confirmationToken) {
		return organisationRepository.findByConfirmationToken(confirmationToken);
	}
	
	
	
	public void saveOrganisation(Organisation organisation) {
		organisationRepository.save(organisation);
	}

	public List<Organisation> listAll(){
		return (List<Organisation>) organisationRepository.findAll();
	}
	public Organisation get(long id) {
        return organisationRepository.findById(id).get();
    }
     
    public void delete(long id) {
       organisationRepository.deleteById(id);
    }
}
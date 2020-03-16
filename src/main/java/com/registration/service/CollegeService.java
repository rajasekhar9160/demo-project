package com.registration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.College;
import com.registration.model.Organisation;
import com.registration.repository.CollegeRepository;


@Service("collegeService")
public class CollegeService {
	@Autowired
	private CollegeRepository collegeRepository;

	
	public CollegeService(CollegeRepository collegeRepository) {
		this.collegeRepository = collegeRepository;
	}
	
	public College findByEmail(String email) {
		return collegeRepository.findByEmail(email);
	}
	
	
	public College findByConfirmationToken(String confirmationToken) {
		return collegeRepository.findByConfirmationToken(confirmationToken);
	}
	
	
	
	public void saveCollege(College college) {
		collegeRepository.save(college);
	}
	
	public List<College> listAll(){
		return (List<College>) collegeRepository.findAll();
	}
	public College get(long id) {
        return collegeRepository.findById(id).get();
    }
     
    public void delete(long id) {
       collegeRepository.deleteById(id);
    }

}

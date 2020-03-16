package com.registration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.Instructor;
import com.registration.model.User;
import com.registration.repository.InstructorRepository;

@Service("instructorService")
public class InstructorService {
	@Autowired
	private InstructorRepository instructorRepository;

	
	public InstructorService(InstructorRepository instructorRepository) {
		this.instructorRepository = instructorRepository;
	}
	
	public Instructor findByEmail(String email) {
		return instructorRepository.findByEmail(email);
	}
	
	
	public Instructor findByConfirmationToken(String confirmationToken) {
		return instructorRepository.findByConfirmationToken(confirmationToken);
	}
	
	
	
	public void saveInstructor(Instructor instructor) {
		instructorRepository.save(instructor);
	}
	
	public List<Instructor> listAll(){
		return (List<Instructor>) instructorRepository.findAll();
	}
	
	 public Instructor get(long id) {
	        return instructorRepository.findById(id).get();
	    }
	     
	    public void delete(long id) {
	        instructorRepository.deleteById(id);
	    }
}
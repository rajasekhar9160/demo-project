package com.registration.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.registration.model.Profile;
import com.registration.repository.ProfileRepository;

@Service
@Transactional
public class ProfileService {
	
	@Autowired
	private ProfileRepository profileRepository;
	
	public List<Profile> listAll(){
		return profileRepository.findAll();
	}
	
	public void save(Profile profile) {
		profileRepository.save(profile);
	}
	
	public Profile get(long id) {
		return profileRepository.findById(id).get();
	}
}

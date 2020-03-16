package com.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.registration.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long>{
	
}

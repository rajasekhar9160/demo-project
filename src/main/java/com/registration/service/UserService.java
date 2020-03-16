package com.registration.service;

import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.registration.model.User;

import com.registration.repository.UserRepository;

@Service("userService")
@Transactional
public class UserService {
	@Autowired
	private UserRepository userRepository;
	

	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	
	public User findByConfirmationToken(String confirmationToken) {
		return userRepository.findByConfirmationToken(confirmationToken);
	}
	
	
	
	public void saveUser(User user) {
		userRepository.save(user);
	}


	
	
	public List<User> listAll() {
        return userRepository.findAll();
    }
     
     
    public User get(long id) {
        return userRepository.findById(id).get();
    }
     
    public void delete(long id) {
        userRepository.deleteById(id);
    }

	
}
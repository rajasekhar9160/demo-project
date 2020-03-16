package com.registration.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class Contact {
    @NotEmpty
    private String name;

    @NotEmpty
    private String message;

    @NotEmpty
    @Email
    private String email;
    
    @NotEmpty
    private String phone;
    
    @NotEmpty
    private String country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	
    
}

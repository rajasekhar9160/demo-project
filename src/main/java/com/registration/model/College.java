package com.registration.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Transient;

@Entity
@Table(name="college")
public class College {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name = "email", nullable = false, unique = true)
	@Email(message = "Please provide a valid e-mail")
	@NotEmpty(message = "Please provide an e-mail")
	private String email;
	
	@Column(name = "password")
	@Transient
	private String password;
	
	@Column(name = "college_name")
	@NotEmpty(message = "Please provide your college name")
	private String collegeName;
	
	@Column(name = "enabled")
	private boolean enabled;
	
	@Column(name = "confirmation_token")
	private String confirmationToken;

	@Column(name="mobile")
	@NotEmpty(message = "Please provide your Mobile Number")
	private String mobile;
	
	@Column(name="country")
	@NotEmpty(message = "Please provide your Country name")
	private String country;
	
	@Column(name="state")
	@NotEmpty(message = "Please provide your State name")
	private String state;
	
	@Column(name="university_name")
	@NotEmpty(message = "Please provide your University name")
	private String universityName;
	
	@Column(name="others")
	private String others;

	@Column(name="skill")
	private String skill;
	
	@Column(name="type_of_university")
	private String typeOfUniversity;

	
	public String getTypeOfUniversity() {
		return typeOfUniversity;
	}

	public void setTypeOfUniversity(String typeOfUniversity) {
		this.typeOfUniversity = typeOfUniversity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCollegeName() {
		return collegeName;
	}

	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getConfirmationToken() {
		return confirmationToken;
	}

	public void setConfirmationToken(String confirmationToken) {
		this.confirmationToken = confirmationToken;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	

	public String getUniversityName() {
		return universityName;
	}

	public void setUniversityName(String universityName) {
		this.universityName = universityName;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}
	
	
	
	
}

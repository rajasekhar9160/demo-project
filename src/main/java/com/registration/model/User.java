package com.registration.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "user")
public class User {

	//Test Commit
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	@Transient
	private String password;
	
	@Column(name = "first_name")

	private String firstName;
	
	@Column(name = "last_name")
	
	private String lastName;
	
	@Column(name = "enabled")
	private boolean enabled;
	
	@Column(name = "confirmation_token")
	private String confirmationToken;
	
	
	@Column(name="mobile")
	private String mobile;
	
	@Column(name="dob")
	@JsonFormat(pattern = "dd-MM-yyyy")
	private String dob;
	
	@Column(name="country")
	private String country;
	
	@Column(name="state")
	private String state;
	
	
	@Column(name="university")
	private String university;
	
	@Column(name="college")
	private String college;
	
	@Column(name="skill")
	private String skill;
	
	@Column(name = "qualification")
	private String qualification;
	
	@Column(name ="roll")
	private String roll;
	
	@Column(name = "specialisation")
	private String specialisation;
	
	@Column(name = "cstate")
	private String cstate;
	
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "photo")
	private byte[] photo;
	
	
	public User() {
		
	}
	




	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getCstate() {
		return cstate;
	}

	public void setCstate(String cstate) {
		this.cstate = cstate;
	}

	public String getUniversity() {
		return university;
	}

	public void setUniversity(String university) {
		this.university = university;
	}



	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getConfirmationToken() {
		return confirmationToken;
	}

	public void setConfirmationToken(String confirmationToken) {
		this.confirmationToken = confirmationToken;
	}

	
	

	

	
	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getRoll() {
		return roll;
	}

	public void setRoll(String roll) {
		this.roll = roll;
	}

	public String getSpecialisation() {
		return specialisation;
	}

	public void setSpecialisation(String specialisation) {
		this.specialisation = specialisation;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		this.enabled = value;
	}



	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
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

	public String getCollege() {
		return college;
	}

	public void setCollege(String college) {
		this.college = college;
	}



	

//	public String getTechnology() {
//		return technology;
//	}
//
//	public void setTechnology(String technology) {
//		this.technology = technology;
//	}
//	

}
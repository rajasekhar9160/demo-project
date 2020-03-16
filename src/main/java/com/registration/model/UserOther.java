package com.registration.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class UserOther {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="user_id")
	private long userid;
	
	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "id")
    private User user;
	
	public UserOther() {
		
	}
	

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

	public UserOther(long userid, User user, String university, String college, String skill, String qualification,
			String roll, String specialisation, String cstate) {
		super();
		this.userid = userid;
		this.user = user;
		this.university = university;
		this.college = college;
		this.skill = skill;
		this.qualification = qualification;
		this.roll = roll;
		this.specialisation = specialisation;
		this.cstate = cstate;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUniversity() {
		return university;
	}

	public void setUniversity(String university) {
		this.university = university;
	}

	public String getCollege() {
		return college;
	}

	public void setCollege(String college) {
		this.college = college;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
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

	public String getCstate() {
		return cstate;
	}

	public void setCstate(String cstate) {
		this.cstate = cstate;
	}
	
	
}

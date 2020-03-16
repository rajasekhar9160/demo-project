package com.registration.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http

		.authorizeRequests()
		/*	.anyRequest()
		.fullyAuthenticated()
		.and()
		.httpBasic();
		http.csrf().disable();*/
			.antMatchers("/register/*").permitAll()
			.antMatchers("/confirm/*").permitAll();			
		 
	}


}
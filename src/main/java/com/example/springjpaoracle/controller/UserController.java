package com.example.springjpaoracle.controller;

import java.util.List;

import com.example.springjpaoracle.model.UserDetails;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserController
{
	private final UserDetailsRepository userDetailsRepository;

	public UserController(final UserDetailsRepository userDetailsRepository)
	{
		this.userDetailsRepository = userDetailsRepository;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<List<UserDetails>> userDetails() {
        
		List<UserDetails> userDetails = userDetailsRepository.findAll();
		return new ResponseEntity<List<UserDetails>>(userDetails, HttpStatus.OK);
	}

}

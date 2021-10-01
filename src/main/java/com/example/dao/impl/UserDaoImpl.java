package com.example.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.dao.UserDao;
import com.example.dao.UserDetailsRepository;
import com.example.model.UserDetails;

@Repository
public class UserDaoImpl implements UserDao {

	@Autowired
	UserDetailsRepository userDetailsRepository;

	public List<UserDetails> getUserDetails() {
		List<UserDetails> output = new ArrayList<>();
		userDetailsRepository.findAll().forEach(output::add);
		return output;
	}
}

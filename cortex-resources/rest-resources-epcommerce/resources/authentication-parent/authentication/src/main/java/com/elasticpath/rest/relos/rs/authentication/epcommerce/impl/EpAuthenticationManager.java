/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.impl;

import java.util.Arrays;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.elasticpath.commons.security.impl.EpUserPasswordEncoder;

/**
 * Provides an {@link AuthenticationManager} that calls CE.
 */
@Component
public class EpAuthenticationManager implements AuthenticationManager {

	@Reference
	private UserDetailsService userDetailsService;

	@Reference
	private MessageSource globalMessageSource;

	private AuthenticationManager authenticationManager;


	/**
	 * Activates the AuthenticationManager by constructing one that matches EP's mechanisms.
	 */
	@Activate
	protected void activate() {
		//Our UserDetailsService produces Users

		EpUserPasswordEncoder passwordEncoder = new EpUserPasswordEncoder();

		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		daoAuthenticationProvider.setUserDetailsService(userDetailsService);
		daoAuthenticationProvider.setMessageSource(globalMessageSource);

		authenticationManager = new ProviderManager(Arrays.asList(daoAuthenticationProvider));
	}

	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		return authenticationManager.authenticate(authentication);
	}
}

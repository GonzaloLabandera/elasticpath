/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.security.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Authenticate using BCrypt or sha1 encoded hash.
 */
public class CmPasswordEncoder extends BCryptPasswordEncoder {

	/**
	 * Constructor.
	 * @param strength the strength.
	 */
	public CmPasswordEncoder(final int strength) {
		super(strength);
	}

	@Override
	public boolean matches(final CharSequence rawPassword, final String encodedPassword) {

		// Try to authenticate using native BCrypt password.
		boolean result = super.matches(rawPassword, encodedPassword);

		if (!result) {
			// Fallback to BCrypt encoded SHA1 password.
			String sha1hash = DigestUtils.sha1Hex(rawPassword.toString());
			result = super.matches(sha1hash, encodedPassword);
		}

		return result;
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.security.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Authenticate using BCrypt or sha1 encoded hash.
 */
public class EpUserPasswordEncoder extends BCryptPasswordEncoder {

	private static final int STRENGTH_256 = 256;

	@Override
	public boolean matches(final CharSequence rawPassword, final String encodedStringData) {

		String encodedPassword = encodedStringData;
		String calculatedPassword = rawPassword.toString();

		try {
			// Still using the old sha encoding. Calculate the password using the salt.
			JSONObject encodedData = new JSONObject(encodedStringData);
			encodedPassword = encodedData.get("password").toString();
			calculatedPassword = getCalculatedPassword(rawPassword.toString(), encodedData.get("salt").toString());
		} catch (JSONException e) {
			// Nothing to do. Native BCrypt password.
		}

		// Authenticate using native BCrypt password.
		return super.matches(calculatedPassword, encodedPassword);
	}

	private String getCalculatedPassword(final String rawPassword, final String salt) {
		ShaPasswordEncoder sha256PasswordEncoder = new ShaPasswordEncoder(STRENGTH_256);
		return sha256PasswordEncoder.encodePassword(rawPassword, salt);
	}
}

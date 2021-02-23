/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.persistence.DataCache;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.PasswordGenerator;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.impl.AbstractLegacyPersistenceImpl;

/**
 * Represents the customer Authentication.
 */
@Entity
@Table(name = CustomerAuthenticationImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CustomerAuthenticationImpl extends AbstractLegacyPersistenceImpl implements CustomerAuthentication {

	private static final long serialVersionUID = 5000000001L;

	private static final int SALT_LENGTH = 128;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCUSTOMERAUTHENTICATION";

	private String password;

	private String salt;

	private String username;

	private String clearTextPassword;

	private long uidPk;

	@Override
	@Basic
	@Column(name = "PASSWORD")
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(final String password) {
		this.password = password;
	}

	@Override
	@Basic
	@Column(name = "SALT", length = SALT_LENGTH)
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(final String salt) {
		this.salt = salt;
	}

	@Override
	@Basic
	@Column(name = "USERNAME")
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public void setClearTextPassword(final String clearTextPassword) {
		this.clearTextPassword = clearTextPassword;
		// encrypt the clearTextPassword and set it into the password field.
		setPasswordUsingPasswordEncoder(clearTextPassword);
	}

	@Override
	@Transient
	public String getClearTextPassword() {
		return clearTextPassword;
	}

	/**
	 * Sets the password using the PasswordEncoder.
	 *
	 * @param clearTextPassword the new clearText password.
	 */
	void setPasswordUsingPasswordEncoder(final String clearTextPassword) {
		if (StringUtils.isBlank(clearTextPassword)) {
			setPassword(null);
		} else {
			final PasswordEncoder passwordEncoder = getSingletonBean(ContextIdNames.PASSWORDENCODER, PasswordEncoder.class);
			setPassword(passwordEncoder.encode(clearTextPassword));
		}
	}

	@Override
	public String resetPassword() {
		final PasswordGenerator passwordGenerator = getSingletonBean(ContextIdNames.PASSWORD_GENERATOR, PasswordGenerator.class);
		final String newPassword = passwordGenerator.getPassword();
		setClearTextPassword(newPassword);
		return newPassword;
	}

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(
			name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME,
			allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

}

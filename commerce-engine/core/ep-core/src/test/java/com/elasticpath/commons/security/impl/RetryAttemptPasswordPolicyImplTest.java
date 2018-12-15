/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.commons.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.elasticpath.commons.security.PasswordHolder;
import com.elasticpath.commons.security.PasswordPolicy;
import com.elasticpath.commons.security.ValidationResult;
import com.elasticpath.settings.test.support.SimpleSettingValueProvider;

/**
 * Test <code>RetryAttemptPasswordPolicyImpl</code>.
 */
public class RetryAttemptPasswordPolicyImplTest {

	private static final int DEFAULT_ACCOUNT_LOCKOUT_THRESHOLD = 4;

	/**
	 * Test method for valid retry attempt.
	 */
	@Test
	public void testValidRetryAttempt() {
		PasswordPolicy passwordPolicy = createRetryAttemptPasswordPolicy(DEFAULT_ACCOUNT_LOCKOUT_THRESHOLD);
		PasswordHolder passwordHolder = createMockPasswordHolder(DEFAULT_ACCOUNT_LOCKOUT_THRESHOLD - 1);

		ValidationResult result = passwordPolicy.validate(passwordHolder);
		assertThat(result.containsError(RetryAttemptPasswordPolicyImpl.PASSWORD_VALIDATION_ERROR_MAXIMUM_RETRY_ATTEMPTS)).isFalse();
		assertThat(result.isValid()).isTrue();
	}

	/**
	 * Test method for invalid retry attempt.
	 */
	@Test
	public void testinValidRetryAttempt() {
		PasswordPolicy passwordPolicy = createRetryAttemptPasswordPolicy(DEFAULT_ACCOUNT_LOCKOUT_THRESHOLD);
		PasswordHolder passwordHolder = createMockPasswordHolder(DEFAULT_ACCOUNT_LOCKOUT_THRESHOLD);

		ValidationResult result = passwordPolicy.validate(passwordHolder);
		assertThat(result.containsError(RetryAttemptPasswordPolicyImpl.PASSWORD_VALIDATION_ERROR_MAXIMUM_RETRY_ATTEMPTS)).isTrue();
		assertThat(result.isValid()).isFalse();
	}

	private RetryAttemptPasswordPolicyImpl createRetryAttemptPasswordPolicy(final int accountLockoutThreshold) {
		final RetryAttemptPasswordPolicyImpl passwordPolicy = new RetryAttemptPasswordPolicyImpl();
		passwordPolicy.setMaximumFailedLoginAttemptsProvider(new SimpleSettingValueProvider<>(accountLockoutThreshold));
		return passwordPolicy;
	}

	private PasswordHolder createMockPasswordHolder(final int failedLoginAttempts) {
		final PasswordHolder mockPasswordHolder = mock(PasswordHolder.class);
		when(mockPasswordHolder.getFailedLoginAttempts()).thenReturn(failedLoginAttempts);
		return mockPasswordHolder;
	}

}

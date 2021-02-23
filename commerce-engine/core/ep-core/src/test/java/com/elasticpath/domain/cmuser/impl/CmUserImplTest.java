/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.cmuser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.EpPasswordValidationException;
import com.elasticpath.commons.security.CmPasswordPolicy;
import com.elasticpath.commons.security.PasswordPolicy;
import com.elasticpath.commons.security.ValidationError;
import com.elasticpath.commons.security.ValidationResult;
import com.elasticpath.domain.cmuser.UserPasswordHistoryItem;
import com.elasticpath.domain.cmuser.UserPermission;
import com.elasticpath.domain.cmuser.UserRole;
import com.elasticpath.service.misc.TimeService;

/**
 * Test of the public API of <code>CmUserImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class CmUserImplTest {

	private CmUserImpl cmUserImpl;

	@Mock
	private PasswordEncoder mockPasswordEncoder;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Prepares for the next test.
	 *
	 * @throws Exception if something goes wrong.
	 */
	@Before
	public void setUp() throws Exception {
		this.cmUserImpl = new CmUserImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(name, clazz);
			}

			@Override
			public <T> T getSingletonBean(final String name, final Class<T> clazz) {
				return beanFactory.getSingletonBean(name, clazz);
			}
		};
		this.cmUserImpl.setUserRoles(new HashSet<>());

		when(beanFactory.getSingletonBean(ContextIdNames.CM_PASSWORDENCODER, PasswordEncoder.class)).thenReturn(mockPasswordEncoder);
		when(beanFactory.getPrototypeBean(ContextIdNames.USER_PASSWORD_HISTORY_ITEM, UserPasswordHistoryItem.class))
				.thenAnswer(invocation -> new UserPasswordHistoryItemImpl());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getUserName()'.
	 */
	@Test
	public void testGetSetUserName() {
		assertThat(cmUserImpl.getUsername()).isNull();

		final String userName = "userName";
		cmUserImpl.setUserName(userName);
		assertThat(cmUserImpl.getUsername()).isEqualTo(userName);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.setEmail(String)'.
	 */
	@Test
	public void testSetEmail() {
		final String[] testData = new String[] { "aaaa@aaa.aaa", "", null };
		for (final String email : testData) {
			cmUserImpl.setEmail(email);
			assertThat(cmUserImpl.getEmail()).isEqualTo(email);
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getFirstName()'.
	 */
	@Test
	public void testGetSetFirstName() {
		assertThat(cmUserImpl.getFirstName()).isNull();

		final String firstName = "first";
		cmUserImpl.setFirstName(firstName);
		assertThat(cmUserImpl.getFirstName()).isEqualTo(firstName);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getLastName()'.
	 */
	@Test
	public void testGetSetLastName() {
		assertThat(cmUserImpl.getLastName()).isNull();

		final String firstName = "first";
		cmUserImpl.setLastName(firstName);
		assertThat(cmUserImpl.getLastName()).isEqualTo(firstName);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.hasUserRole()'.
	 */
	@Test
	public void testHasUserRole() {
		final long userRoleId = 1000L;
		final long invalidUserRoleId = 1001L;
		final String testRoleName = "TestRole";
		final String invalidTestRoleName = "InvalidTestRole";

		final UserRole userRole = new UserRoleImpl();
		userRole.setUidPk(userRoleId);
		userRole.setName(testRoleName);

		cmUserImpl.addUserRole(userRole);
		assertThat(cmUserImpl.hasUserRole(userRoleId)).isTrue();
		assertThat(cmUserImpl.hasUserRole(testRoleName)).isTrue();

		assertThat(cmUserImpl.hasUserRole(invalidTestRoleName)).isFalse();
		assertThat(cmUserImpl.hasUserRole(invalidUserRoleId)).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.hasPermission()'.
	 */
	@Test
	public void testHasPermission() {
		final long userRoleId = 1000L;
		final String testRoleName = "TestRole";
		final String authority = "CSR";
		final String invalidAuthority = "Invalid";

		final UserRole userRole = new UserRoleImpl();
		userRole.setUidPk(userRoleId);
		userRole.setName(testRoleName);

		final UserPermission userPermission = new UserPermissionImpl();
		userPermission.setAuthority(authority);
		Set<UserPermission> userPermissions = new HashSet<>();
		userPermissions.add(userPermission);
		userRole.setUserPermissions(userPermissions);
		cmUserImpl.addUserRole(userRole);

		assertThat(cmUserImpl.hasPermission(authority)).isTrue();
		assertThat(cmUserImpl.hasPermission(invalidAuthority)).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getPassword()'.
	 */
	@Test
	public void testGetPassword() {
		assertThat(cmUserImpl.getPassword()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getPassword()'.
	 */
	@Test
	public void testGetEncryptedPassword() {
		assertThat(cmUserImpl.getPassword()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.setClearTextPassword(String)'.
	 */
	@Test
	public void testSetClearTextPassword() {
		final String[] passwords = new String[] { "AbCdEfGhI", "AbCdEfGhIjKlMnOpQrS", "aA123_$@#^&", "", null };
		final String[] hashedPasswords = new String[] { "d60c7aaba158d8270ec509390438152ca931ec6a", "32a6ea3419c4d9653cf51c6500f3accef2012ab0",
				"e9d1d12fbb45ca95c496f3a33a40956c1a4da1ef", "adc83b19e793491b1c6ea0fd8b46cd9f32e592fc", null };

		for (int i = 0; i < passwords.length; i++) {
			final String password = passwords[i];
			final String hashedPassword = hashedPasswords[i];
			when(mockPasswordEncoder.encode(password)).thenReturn(hashedPassword);
			cmUserImpl.setClearTextPassword(password);
			cmUserImpl.setPasswordUsingPasswordEncoder(password);
			assertThat(cmUserImpl.getPassword()).isEqualTo(hashedPassword);
		}
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.getConfirmClearTextPassword()'.
	 */
	@Test
	public void testGetConfirmClearTextPassword() {
		assertThat(cmUserImpl.getConfirmClearTextPassword()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CmUserImpl.setLastName(String)'.
	 */
	@Test
	public void testSetConfirmClearTextPassword() {
		final String[] passwords = new String[] { "AbCdEfGhI", "AbCdEfGhIjKlMnOpQrS", "aA123_$@#^&", "", null };
		for (final String password : passwords) {
			cmUserImpl.setConfirmClearTextPassword(password);
			assertThat(cmUserImpl.getConfirmClearTextPassword()).isEqualTo(password);
		}
	}

	/** 
	 * Test that isCmAccess returns true when a user has the CMUSER role. 
	 * @throws Exception on error.
	 */
	@Test
	public void testIsCmAccessTrue() {
		UserRole cmUserRole = new UserRoleImpl();
		cmUserRole.setUserPermissions(new HashSet<>());
		cmUserRole.setName(UserRole.CMUSER);
		cmUserImpl.addUserRole(cmUserRole);
		assertThat(cmUserImpl.isCmAccess()).isTrue();
	}
	
	/** 
	 * Test that isCmAccess returns false when a user does not have the CMUSER role. 
	 */
	@Test
	public void testIsCmAccessFalse() {
		UserRole wsUserRole = new UserRoleImpl();
		wsUserRole.setUserPermissions(new HashSet<>());
		wsUserRole.setName(UserRole.WSUSER);
		cmUserImpl.addUserRole(wsUserRole);
		assertThat(cmUserImpl.isCmAccess()).isFalse();
	}
	
	/** 
	 * Test that isWsAccess returns true when a user has the WSUSER role. 
	 */
	@Test
	public void testIsWsAccessTrue() {
		UserRole wsUserRole = new UserRoleImpl();
		wsUserRole.setUserPermissions(new HashSet<>());
		wsUserRole.setName(UserRole.WSUSER);
		cmUserImpl.addUserRole(wsUserRole);
		assertThat(cmUserImpl.isWsAccess()).isTrue();
	}
	
	/** 
	 * Test that isWsAccess returns false when a user does not have the WSUSER role. 
	 */
	@Test
	public void testIsWsAccessFalse() {
		UserRole cmUserRole = new UserRoleImpl();
		cmUserRole.setUserPermissions(new HashSet<>());
		cmUserRole.setName(UserRole.CMUSER);
		cmUserImpl.addUserRole(cmUserRole);
		assertThat(cmUserImpl.isWsAccess()).isFalse();
	}

	@Test
	public void testIsPasswordExpiredTrueWhenMaxAgePasswordPolicyInvalid() {
		final PasswordPolicy maximumAgePasswordPolicy = mock(PasswordPolicy.class, "maxAgePasswordPolicy");

		when(beanFactory.getSingletonBean(ContextIdNames.MAXIMUM_AGE_PASS_WORD_POLICY, PasswordPolicy.class)).thenReturn(maximumAgePasswordPolicy);

		final ValidationResult validationResult = new ValidationResult();
		validationResult.addError(new ValidationError("foo"));

		when(maximumAgePasswordPolicy.validate(cmUserImpl)).thenReturn(validationResult);

		assertThat(cmUserImpl.isPasswordExpired())
			.as("Expected password to be expired when the max age password policy is invalid")
			.isTrue();
		verify(maximumAgePasswordPolicy).validate(cmUserImpl);
	}

	@Test
	public void testIsPasswordExpiredFalseWhenMaxAgePasswordPolicyValid() {
		final PasswordPolicy maximumAgePasswordPolicy = mock(PasswordPolicy.class, "maxAgePasswordPolicy");

		when(beanFactory.getSingletonBean(ContextIdNames.MAXIMUM_AGE_PASS_WORD_POLICY, PasswordPolicy.class)).thenReturn(maximumAgePasswordPolicy);

		when(maximumAgePasswordPolicy.validate(cmUserImpl)).thenReturn(new ValidationResult());

		assertThat(cmUserImpl.isPasswordExpired())
			.as("Expected password not to be expired when the max age password policy is valid")
			.isFalse();
		verify(maximumAgePasswordPolicy).validate(cmUserImpl);
	}

	@Test
	public void verifySetCheckedClearTextPasswordFailsWhenValidatorRejects() throws Exception {
		cmUserImpl.setPasswordHistoryItems(Collections.<UserPasswordHistoryItem>emptyList());

		final CmPasswordPolicy cmPasswordPolicy = mock(CmPasswordPolicy.class);

		when(beanFactory.getSingletonBean(ContextIdNames.CM_PASS_WORD_POLICY, CmPasswordPolicy.class)).thenReturn(cmPasswordPolicy);

		final ValidationResult validationResult = new ValidationResult();
		validationResult.addError(new ValidationError("Invalid"));

		when(cmPasswordPolicy.validate(cmUserImpl)).thenReturn(validationResult);

		assertThatThrownBy(() -> cmUserImpl.setCheckedClearTextPassword("foo"))
			.isInstanceOf(EpPasswordValidationException.class)
			.hasMessage("Password didn't pass validation");
		verify(cmPasswordPolicy).validate(cmUserImpl);
	}

	@Test
	public void verifySetCheckedClearTextPasswordSucceedsWhenValidatorAccepts() throws Exception {
		final String newPassword = "new password";
		final String newPasswordEncoded = "new encoded password";

		cmUserImpl.setPasswordHistoryItems(Collections.<UserPasswordHistoryItem>emptyList());

		final CmPasswordPolicy cmPasswordPolicy = mock(CmPasswordPolicy.class);

		givenTimeServiceReturnsCurrentTime();
		when(beanFactory.getSingletonBean(ContextIdNames.CM_PASS_WORD_POLICY, CmPasswordPolicy.class)).thenReturn(cmPasswordPolicy);

		when(mockPasswordEncoder.encode(newPassword)).thenReturn(newPasswordEncoded);

		when(cmPasswordPolicy.validate(cmUserImpl)).thenReturn(new ValidationResult());

		cmUserImpl.setCheckedClearTextPassword(newPassword);

		assertThat(cmUserImpl.getPassword())
			.as("New password should be set successfully")
			.isEqualTo(newPasswordEncoded);
		verify(cmPasswordPolicy).validate(cmUserImpl);
	}

	/**
	 * Tests that the new password is moved to history right after setting it.
	 */
	@Test
	public void testSetCheckedClearTextPassword3() {
		final String initialPassword = "initialPassword";

		final CmPasswordPolicy cmPasswordPolicy = mock(CmPasswordPolicy.class);

		givenTimeServiceReturnsCurrentTime();
		when(beanFactory.getSingletonBean(ContextIdNames.CM_PASS_WORD_POLICY, CmPasswordPolicy.class)).thenReturn(cmPasswordPolicy);

		when(mockPasswordEncoder.encode(initialPassword)).thenReturn("encodedInitialPassword");
		cmUserImpl.setClearTextPassword(initialPassword);
		cmUserImpl.setPasswordUsingPasswordEncoder(initialPassword);

		assertThat(cmUserImpl.getPasswordHistoryItems())
			.as("There should be no passwords in history after first login.")
			.isEmpty();

		final String oldPassword = cmUserImpl.getPassword();
		final String newPassword = "newPassword1";
		final String encodedNewPassword = "encodedNewPassword1";
		when(mockPasswordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

		when(cmPasswordPolicy.validate(cmUserImpl)).thenReturn(new ValidationResult());

		final int passwordHistoryMinimumLength = 3;
		when(cmPasswordPolicy.getPasswordHistoryLength()).thenReturn(passwordHistoryMinimumLength);
		cmUserImpl.setCheckedClearTextPassword(newPassword);
		assertThat(cmUserImpl.getPasswordHistoryItems())
			.as("After changing password old should be moved to history")
			.hasSize(1);
		assertThat(cmUserImpl.getPasswordHistoryItems().get(0).getOldPassword())
			.as("Password in history should be equal to changed password")
			.isEqualTo(oldPassword);
	}

	private void givenTimeServiceReturnsCurrentTime() {
		final TimeService timeService = mock(TimeService.class);

		when(timeService.getCurrentTime()).thenReturn(new Date());

		when(beanFactory.getSingletonBean(ContextIdNames.TIME_SERVICE, TimeService.class)).thenReturn(timeService);
	}

}
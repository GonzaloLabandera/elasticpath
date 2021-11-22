/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.customer.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.UserAccountAssociationImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * User Account Association Service Implementation.
 */
public class UserAccountAssociationServiceImpl extends AbstractEpPersistenceServiceImpl implements UserAccountAssociationService {

	private CustomerService customerService;
	private RoleToPermissionsMappingService roleToPermissionMappingService;

	@Override
	public UserAccountAssociation add(final UserAccountAssociation userAccountAssociation) {
		return getPersistenceEngine().saveOrUpdate(userAccountAssociation);
	}

	@Override
	public UserAccountAssociation update(final String userGuid, final String accountGuid, final String role) {
		UserAccountAssociation userAccountAssociation = findAssociationForUserAndAccount(userGuid, accountGuid);
		userAccountAssociation.setAccountRole(role);

		return update(userAccountAssociation);
	}

	@Override
	public UserAccountAssociation update(final UserAccountAssociation userAccountAssociation) {
		return getPersistenceEngine().saveOrUpdate(userAccountAssociation);
	}

	@Override
	public void remove(final String userGuid, final String accountGuid) {
		UserAccountAssociation userAccountAssociation = findAssociationForUserAndAccount(userGuid, accountGuid);
		if (userAccountAssociation != null) {
			remove(userAccountAssociation);
		}
	}

	@Override
	public void remove(final UserAccountAssociation userAccountAssociation) {
		getPersistenceEngine().delete(userAccountAssociation);
		getPersistenceEngine().flush();
	}

	@Override
	public Collection<UserAccountAssociation> findAssociationsForUser(final Customer user) throws IllegalArgumentException {
		return findAssociationsForUser(user.getGuid());
	}

	@Override
	public Collection<UserAccountAssociation> findAssociationsForUser(final String userGuid) throws IllegalArgumentException {
		return getPersistenceEngine().retrieveByNamedQuery("USER_ACCOUNT_ASSOCIATIONS_FOR_USER", userGuid);
	}

	@Override
	public Collection<UserAccountAssociation> findAssociationsForAccount(final Customer account) throws IllegalArgumentException {
		return findAssociationsForAccount(account.getGuid());
	}

	@Override
	public Collection<UserAccountAssociation> findAssociationsForAccount(final String accountGuid) throws IllegalArgumentException {
		return getPersistenceEngine().retrieveByNamedQuery("USER_ACCOUNT_ASSOCIATIONS_FOR_ACCOUNT", accountGuid);
	}

	@Override
	public UserAccountAssociation findAssociationForUserAndAccount(final String userGuid, final String accountGuid) throws IllegalArgumentException {
		List<UserAccountAssociation> results = getPersistenceEngine().retrieveByNamedQuery(
				"USER_ACCOUNT_ASSOCIATION_FOR_USER_AND_ACCOUNT", userGuid, accountGuid);
		if (results.size() == 1) {
			return results.get(0);
		}
		return null;
	}

	@Override
	public UserAccountAssociation associateUserToAccount(final Customer user, final Customer account, final String role)
			throws IllegalArgumentException {
		return associateUserToAccount(user.getGuid(), account.getGuid(), role);
	}

	@Override
	public UserAccountAssociation associateUserToAccount(final String userGuid, final String accountGuid, final String role)
			throws IllegalArgumentException {
		UserAccountAssociation userAccountAssociation = createUserAccountAssociation(userGuid, accountGuid, role);
		return add(userAccountAssociation);
	}

	@Override
	public UserAccountAssociation findOrCreateUserAccountAssociation(final String userAccountAssociationGuid,
																	 final String userGuid,
																	 final String accountGuid,
																	 final String role) throws IllegalArgumentException {
		UserAccountAssociation userAccountAssociation = findByGuid(userAccountAssociationGuid);
		if (userAccountAssociation == null) {
			userAccountAssociation = createUserAccountAssociationWithGuid(userAccountAssociationGuid, userGuid, accountGuid, role);
		} else {
			validateUserAccountAssociation(userGuid, accountGuid, role);
			userAccountAssociation.setUserGuid(userGuid);
			userAccountAssociation.setAccountGuid(accountGuid);
			userAccountAssociation.setAccountRole(role);
		}
		return getPersistenceEngine().saveOrUpdate(userAccountAssociation);
	}

	private UserAccountAssociation createUserAccountAssociationWithGuid(final String userAccountAssociationGuid, final String userGuid,
																		final String accountGuid, final String role) {
		UserAccountAssociation userAccountAssociation = createUserAccountAssociation(userGuid, accountGuid, role);
		userAccountAssociation.setGuid(userAccountAssociationGuid);
		return userAccountAssociation;
	}

	@Override
	public int disassociateUserFromAccount(final Customer user, final Customer account) throws IllegalArgumentException {
		return getPersistenceEngine().executeNamedQuery("DELETE_USER_ACCOUNT_ASSOCIATION_BY_USER_AND_ACCOUNT", user.getGuid(), account.getGuid());
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getPersistenceEngine().get(UserAccountAssociationImpl.class, uid);
	}

	@Override
	public Collection<Long> findAllUids() {
		return getPersistenceEngine().retrieveByNamedQuery("USER_ACCOUNT_ASSOCIATION_UIDS_ALL");
	}

	@Override
	public Collection<UserAccountAssociation> findByIDs(final List<Long> uids) {
		return getPersistenceEngine().retrieveByNamedQueryWithList("USER_ACCOUNT_ASSOCIATIONS_BY_UIDS", "list", uids);
	}

	@Override
	public boolean isExistingUserAssociation(final String accountCustomerGuid, final String associatedCustomerGuid) {
		final List<Long> results = getPersistenceEngine().retrieveByNamedQuery("USER_ACCOUNT_EXISTS_BY_ACCOUNT_AND_USER", accountCustomerGuid,
				associatedCustomerGuid);
		return !results.isEmpty() && results.get(0) > 0;
	}

	private UserAccountAssociation createUserAccountAssociation(final String userGuid, final String accountGuid, final String role) {
		validateUserAccountAssociation(userGuid, accountGuid, role);
		UserAccountAssociation userAccountAssociation = getPrototypeBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION, UserAccountAssociationImpl.class);
		userAccountAssociation.setAccountGuid(accountGuid);
		userAccountAssociation.setUserGuid(userGuid);
		userAccountAssociation.setAccountRole(role);
		return userAccountAssociation;
	}

	private void validateUserAccountAssociation(final String userGuid, final String accountGuid, final String role) throws EpServiceException {

		checkArgument(customerService.isCustomerGuidExists(accountGuid),
				"Invalid UserAccountAssociation: The supplied account guid does not exist: " + accountGuid);

		checkArgument(customerService.isCustomerGuidExists(userGuid),
				"Invalid UserAccountAssociation: The supplied user guid does not exist: " + userGuid);

		checkArgument(customerService.getCustomerTypeByGuid(accountGuid).equals(CustomerType.ACCOUNT),
				"Invalid UserAccountAssociation: The supplied Account guid is not a customer of type ACCOUNT: " + accountGuid);

		checkArgument(customerService.getCustomerTypeByGuid(userGuid).equals(CustomerType.REGISTERED_USER)
						|| customerService.getCustomerTypeByGuid(userGuid).equals(CustomerType.SINGLE_SESSION_USER),
				"Invalid UserAccountAssociation: The supplied User guid is not a customer of type REGISTERED USER: " + userGuid);

		checkArgument(StringUtils.isNotBlank(role),
				"Invalid UserAccountAssociation: Missing role.");

		if (!roleToPermissionMappingService.getDefinedRoleKeys().contains(role)) {
			throw new IllegalArgumentException("Invalid UserAccountAssociation: The supplied role " + role + " is not a valid role. ");
		}
	}

	@Override
	public UserAccountAssociation findByGuid(final String guid) {
		List<UserAccountAssociation> userAccountAssociationsByGuid = getPersistenceEngine().retrieveByNamedQuery(
				"USER_ACCOUNT_ASSOCIATIONS_BY_GUID", guid);
		if (userAccountAssociationsByGuid.isEmpty()) {
			return null;
		}
		if (userAccountAssociationsByGuid.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate guid:" + guid);
		}
		return userAccountAssociationsByGuid.get(0);
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	protected RoleToPermissionsMappingService getRoleToPermissionMappingService() {
		return roleToPermissionMappingService;
	}

	public void setRoleToPermissionMappingService(final RoleToPermissionsMappingService roleToPermissionMappingService) {
		this.roleToPermissionMappingService = roleToPermissionMappingService;
	}
}

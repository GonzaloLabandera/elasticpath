/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.service.customer;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.customer.AccountRole;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.service.EpPersistenceService;

/**
 * The User Account Association Service.
 */
public interface UserAccountAssociationService extends EpPersistenceService {

	/**
	 * Adds the given UserAccountAssociation.
	 *
	 * @param userAccountAssociation the UserAccou tAssociation to add
	 * @return the persisted instance of ProductAssociation
	 */
	UserAccountAssociation add(UserAccountAssociation userAccountAssociation);

	/**
	 * Updates the given UserAccountAssociation.
	 *
	 * @param userAccountAssociation the userAccountAssociation to be updated
	 * @return the persisted instance of UserAccountAssociation
	 */
	UserAccountAssociation update(UserAccountAssociation userAccountAssociation);

	/**
	 * Delete the UserAccountAssociation.
	 *
	 * @param userAccountAssociation the UserAccountAssociation to remove
	 */
	void remove(UserAccountAssociation userAccountAssociation);

	/**
	 * Find all associations for a user.
	 *
	 * @param user The user to get all associations for.
	 * @return The associations for the customer.
	 * @throws IllegalArgumentException if the customer is not of type REGISTERED_USER.
	 */
	Collection<UserAccountAssociation> findAssociationsForUser(Customer user) throws IllegalArgumentException;

	/**
	 * Find all associations for a user.
	 *
	 * @param userGuid The user to get all associations for.
	 * @return The associations for the customer.
	 * @throws IllegalArgumentException if the customer is not of type REGISTERED_USER.
	 */
	Collection<UserAccountAssociation> findAssociationsForUser(String userGuid) throws IllegalArgumentException;

	/**
	 * Find all customers associated to an account.
	 *
	 * @param account The account to find associations for.
	 * @return The associations for the account.
	 * @throws IllegalArgumentException if the supplied customer is not of type ACCOUNT.
	 */
	Collection<UserAccountAssociation> findAssociationsForAccount(Customer account) throws IllegalArgumentException;

	/**
	 * Find all customers associated to an account.
	 *
	 * @param userGuid The user to find associations for.
	 * @return The associations for the account.
	 * @throws IllegalArgumentException if the supplied customer is not of type ACCOUNT.
	 */
	Collection<UserAccountAssociation> findAssociationsForAccount(String userGuid) throws IllegalArgumentException;

	/**
	 * Retrieves the user account association record for the passed user and account.
	 * @param userGuid The user to find the association for.
	 * @param accountGuid THe account to find the association for.
	 * @return the association record or null if none is found.
	 * @throws IllegalArgumentException if the supplied customer is not of type ACCOUNT.
	 */
	UserAccountAssociation findAssociationForUserAndAccount(String userGuid, String accountGuid) throws IllegalArgumentException;

	/**
	 * Create and persist a new UserAccountAssociation.
	 *
	 * @param user    The user to associate.
	 * @param account The account to associate the user to.
	 * @param role    The role the user has in this association.
	 * @return The persisted UserAccountAssociation
	 * @throws IllegalArgumentException if the user is not REGISTERED_CUSTOMER or the account is not ACCOUT.
	 */
	UserAccountAssociation associateUserToAccount(Customer user, Customer account, AccountRole role)
			throws IllegalArgumentException;

	/**
	 * Create and persist a new UserAccountAssociation.
	 *
	 * @param userAccountAssociationGuid        The UserAccountAssociation guid
	 * @param userGuid    The user to associate.
	 * @param accountGuid The account to associate the user to.
	 * @param role        The role the user has in this association.
	 * @return The persisted UserAccountAssociation
	 * @throws IllegalArgumentException if the user is not REGISTERED_CUSTOMER or the account is not ACCOUNT.
	 */
	UserAccountAssociation findOrCreateUserAccountAssociation(String userAccountAssociationGuid, String userGuid, String accountGuid, String role)
			throws IllegalArgumentException;

	/**
	 * Create and persist a new UserAccountAssociation.
	 *
	 * @param userGuid    The user to associate.
	 * @param accountGuid The account to associate the user to.
	 * @param role        The role the user has in this association.
	 * @return The persisted UserAccountAssociation
	 * @throws IllegalArgumentException if the user is not REGISTERED_CUSTOMER or the account is not ACCOUNT.
	 */
	UserAccountAssociation associateUserToAccount(String userGuid, String accountGuid, String role)
			throws IllegalArgumentException;

	/**
	 * Disassociate a user from an account..
	 *
	 * @param user    The user to disassociate.
	 * @param account The account to disassociate the user from.
	 * @return number of entities updated.
	 * @throws IllegalArgumentException if the supplied user is not associated to the supplied account.
	 */
	int disassociateUserFromAccount(Customer user, Customer account) throws IllegalArgumentException;

	/**
	 * Find All User Account Associations.
	 *
	 * @return all UserAccountAssociations
	 */
	Collection<Long> findAllUids();

	/**
	 * Find User Account Associations by UIDS.
	 *
	 * @param uids list of UIDs
	 * @return UserAccountAssociations
	 */
	Collection<UserAccountAssociation> findByIDs(List<Long> uids);

	/**
	 * Find User Account Associations by GUIDS.
	 *
	 * @param guid the user account association guid
	 * @return the UserAccountAssociation
	 */
	UserAccountAssociation findByGuid(String guid);
	
	/**
	 * Returns true if there is an existing User Association for the given Account, User, and Role.
	 * 
	 * @param accountCustomerGuid the account GUID
	 * @param associatedCustomerGuid the user GUID
	 * @return true if there is an existing User Association for the given Account, User, and Role.
	 */
	boolean isExistingUserAssociation(String accountCustomerGuid, String associatedCustomerGuid);


}

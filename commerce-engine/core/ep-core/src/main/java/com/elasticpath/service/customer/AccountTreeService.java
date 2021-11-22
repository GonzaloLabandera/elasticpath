/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer;

import java.util.List;
import java.util.Optional;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;

/**
 * The Account Tree Service.
 */
public interface AccountTreeService {
	/**
	 * Add a child to an account.
	 *
	 * @param accountGuid The guid of the child account.
	 * @param parentAccountGuid The guid of the parent account.
	 * @throws EpServiceException       on db error.
	 */
	void insertClosures(String accountGuid, String parentAccountGuid) throws EpServiceException;

	/**
	 * Fetch a list of GUIDs for all members of the subtree under the provided Account. Makes no guarantee as to the ordering of the returned GUIDs.
	 *
	 * @param accountGuid the root account guid of the tree.
	 * @return the list of guids of the tree members.
	 * @throws IllegalArgumentException if the root customer is not of type ACCOUNT.
	 * @throws EpServiceException       on db error.
	 */
	List<String> findDescendantGuids(String accountGuid) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch the path of guids to the root account for this account.
	 *
	 * @param accountGuid the account guid for which we want to fetch ancestors.
	 * @return the list of guids of accounts from the provided account up to and including the root. The list returned is ordered
	 * from direct parent -> root.
	 * @throws IllegalArgumentException if the provided Customer is not of type Account.
	 * @throws EpServiceException       on db error.
	 */
	List<String> findAncestorGuids(String accountGuid) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch the GUIDS of all direct children of the given account.
	 *
	 * @param account the Account for which we want to fetch children for.
	 * @return the list of GUIDS for all direct children of the account.
	 * @throws IllegalArgumentException if the provided Customer is not of type Account.
	 * @throws EpServiceException       on db error.
	 */
	List<String> fetchChildAccountGuids(Customer account) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch the GUIDS of all direct children of the given account.
	 *
	 * @param accountGuid the Account Guid for which we want to fetch children for.
	 * @param pageStartIndex the page start index.
	 * @param pageSize the number of results per page.
	 * @return the list of GUIDS for all direct children of the account.
	 * @throws IllegalArgumentException if the provided Customer is not of type Account.
	 * @throws EpServiceException       on db error.
	 */
	List<String> findChildGuidsPaginated(String accountGuid, int pageStartIndex, int pageSize);

	/**
	 * Fetch the GUID of the parent account for the account specified by the supplied GUID.
	 *
	 * @param accountGuid The guid of the account.
	 * @return The parent guid.
	 */
	Optional<String> fetchParentAccountGuidByChildGuid(String accountGuid);

	/**
	 * Remove the closure by account guid.
	 * @param accountGuid the guid of the account.
	 */
	void remove(String accountGuid);
}

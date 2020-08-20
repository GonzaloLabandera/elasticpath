/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;

/**
 * The Account Tree Service.
 */
public interface AccountTreeService {
	/**
	 * Add a child to an account.
	 *
	 * @param parent the parent account.
	 * @param child  the Customer object of the child to add.
	 * @throws IllegalArgumentException if the parent or child are not of type ACCOUNT, or if adding this child would create a circular relationship
	 *                                  in the tree, or if the child already has a parent.
	 * @throws EpServiceException       on db error.
	 */
	void parent(Customer parent, Customer child) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch a list of GUIDs for all members of the subtree under the provided Account. Makes no guarantee as to the ordering of the returned GUIDs.
	 *
	 * @param root the root of the tree.
	 * @return the list of guids of the tree members.
	 * @throws IllegalArgumentException if the root customer is not of type ACCOUNT.
	 * @throws EpServiceException       on db error.
	 */
	List<String> fetchSubtree(Customer root) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch the path of guids to the root account for this account.
	 *
	 * @param account the account for which we want to fetch root.
	 * @return the list of guids of accounts from the provided account up to and including the root. The list returned is ordered
	 * from direct parent -> root.
	 * @throws IllegalArgumentException if the provided Customer is not of type Account.
	 * @throws EpServiceException       on db error.
	 */
	List<String> fetchPathToRoot(Customer account) throws IllegalArgumentException, EpServiceException;

	/**
	 * Fetch the GUIDS of all direct children of the given account.
	 *
	 * @param account the Account for which we want to fetch children for.
	 * @return the list of GUIDS for all direct children of the account.
	 * @throws IllegalArgumentException if the provided Customer is not of type Account.
	 * @throws EpServiceException       on db error.
	 */
	List<String> fetchChildAccountGuids(Customer account) throws IllegalArgumentException, EpServiceException;
}

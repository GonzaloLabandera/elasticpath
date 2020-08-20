/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;

/**
 * The Account Tree Service Implementation.
 */
public class ParentReferenceAccountTreeServiceImpl implements AccountTreeService {

	private CustomerService customerService;

	@Override
	public void parent(final Customer parent, final Customer child) throws IllegalArgumentException, EpServiceException {
		validateAccounts(parent, child);
		child.setParentGuid(parent.getGuid());
		customerService.update(child);
	}

	@Override
	public List<String> fetchSubtree(final Customer root) throws IllegalArgumentException, EpServiceException {
		checkCustomerType(root);
		final List<String> subtreeGuids = new ArrayList<>();
		fetchSubtree(Collections.singletonList(root.getGuid()), subtreeGuids);
		return subtreeGuids;
	}

	private void fetchSubtree(final List<String> parentGuids, final List<String> subtreeGuids) {
		final List<String> childrenGuids = getChildrenGuids(parentGuids);
		if (!childrenGuids.isEmpty()) {
			subtreeGuids.addAll(childrenGuids);
			fetchSubtree(childrenGuids, subtreeGuids);
		}
	}

	@Override
	public List<String> fetchPathToRoot(final Customer account) throws IllegalArgumentException, EpServiceException {
		checkCustomerType(account);
		final List<String> pathToRoot = new ArrayList<>();
		fetchPathToRoot(account.getGuid(), pathToRoot);
		return pathToRoot;
	}

	private void fetchPathToRoot(final String childGuid, final List<String> pathToRoot) {
		final Optional<String> parentGuid = getParentGuid(childGuid);
		if (parentGuid.isPresent()) {
			pathToRoot.add(parentGuid.get());
			fetchPathToRoot(parentGuid.get(), pathToRoot);
		}
	}

	@Override
	public List<String> fetchChildAccountGuids(final Customer account) throws IllegalArgumentException, EpServiceException {
		checkCustomerType(account);
		return getChildrenGuids(Collections.singletonList(account.getGuid()));
	}

	private void validateAccounts(final Customer parent, final Customer child) throws IllegalArgumentException {
		checkCustomerType(parent);
		checkCustomerType(child);
		if (StringUtils.isNotEmpty(child.getParentGuid())) {
			throw new IllegalArgumentException("The child customer already has a parent.");
		}
		final List<String> pathToRoot = new ArrayList<>();
		fetchPathToRoot(parent.getGuid(), pathToRoot);
		if (isRootOfTree(child.getGuid(), pathToRoot)) {
			throw new IllegalArgumentException("The child customer is a root of the parents' tree");
		}
	}

	private boolean isRootOfTree(final String childGuid, final List<String> pathToRoot) {
		return !pathToRoot.isEmpty() && pathToRoot.get(pathToRoot.size() - 1).equals(childGuid);
	}

	private List<String> getChildrenGuids(final List<String> parentGuids) {
		return customerService.getPersistenceEngine().retrieveByNamedQueryWithList("ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS",
				"guidsThisLevel", parentGuids);
	}

	private Optional<String> getParentGuid(final String childGuid) {
		final List<String> guids = customerService.getPersistenceEngine()
				.retrieveByNamedQuery("ACCOUNT_PARENT_GUID_BY_CHILD_GUID", childGuid);
		validateParentGuid(guids);

		return guids.isEmpty()
				? Optional.empty()
				: Optional.of(guids.get(0));
	}

	private void validateParentGuid(final List<String> parentGuidList) {
		if (parentGuidList.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate GUIDs exist -- " + parentGuidList.get(0));
		}
	}

	private void checkCustomerType(final Customer customer) throws IllegalArgumentException {
		if (!customer.getCustomerType().equals(CustomerType.ACCOUNT)) {
			throw new IllegalArgumentException("This customer is not of type ACCOUNT.");
		}
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}

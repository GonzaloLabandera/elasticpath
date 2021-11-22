/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerClosure;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;

/**
 * The Account Tree Service Implementation.
 */
public class ParentReferenceAccountTreeServiceImpl implements AccountTreeService {

	private static final String ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS = "ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS";
	private static final String LIST_PARAM_NAME = "guidsThisLevel";
	private CustomerService customerService;
	private BeanFactory beanFactory;
	private PersistenceEngine persistenceEngine;

	@Override
	public void insertClosures(final String accountGuid, final String parentAccountGuid) throws EpServiceException {

		validateAccounts(parentAccountGuid, accountGuid);

		duplicateAncestorClosures(accountGuid, parentAccountGuid);
		saveNewClosure(accountGuid, parentAccountGuid);
	}

	private void saveNewClosure(final String accountGuid, final String parentAccountGuid) {
		long ancestorDepth = getAncestorDepth(parentAccountGuid);

		CustomerClosure customerClosure = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_CLOSURE, CustomerClosure.class);
		customerClosure.setAncestorGuid(parentAccountGuid);
		customerClosure.setDescendantGuid(accountGuid);
		customerClosure.setAncestorDepth(ancestorDepth);

		persistenceEngine.save(customerClosure);
	}

	private void duplicateAncestorClosures(final String accountGuid, final String parentAccountGuid) {
		final List<CustomerClosure> customerClosuresForDuplicating = persistenceEngine
				.retrieveByNamedQuery("SELECT_ACCOUNT_CLOSURE_DUPLICATE", parentAccountGuid);

		for (CustomerClosure customerClosure : customerClosuresForDuplicating) {

			CustomerClosure customerClosureNew = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_CLOSURE, CustomerClosure.class);
			customerClosureNew.setDescendantGuid(accountGuid);
			customerClosureNew.setAncestorGuid(customerClosure.getAncestorGuid());
			customerClosureNew.setAncestorDepth(customerClosure.getAncestorDepth());

			persistenceEngine.save(customerClosureNew);
		}
	}

	private long getAncestorDepth(final String parentAccountGuid) {
		final List<Long> depthList = persistenceEngine.retrieveByNamedQuery("SELECT_CUSTOMER_CLOSURE_DEPTH", parentAccountGuid);
		if (!depthList.isEmpty() && depthList.get(0) != null) {
			return depthList.get(0);
		}
		return 0;
	}

	@Override
	public List<String> findDescendantGuids(final String accountGuid) throws IllegalArgumentException, EpServiceException {
		return persistenceEngine.retrieveByNamedQuery("SELECT_DESCENDANT", accountGuid);
	}

	@Override
	public List<String> findAncestorGuids(final String accountGuid) throws IllegalArgumentException, EpServiceException {
		return persistenceEngine.retrieveByNamedQuery("SELECT_ANCESTOR", accountGuid);
	}

	@Override
	public List<String> fetchChildAccountGuids(final Customer account) throws IllegalArgumentException, EpServiceException {
		return getChildrenGuids(Collections.singletonList(account.getGuid()));
	}

	@Override
	public List<String> findChildGuidsPaginated(final String accountGuid, final int pageStartIndex, final int pageSize) {
		return customerService.getPersistenceEngine().retrieveByNamedQueryWithList(ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS,
				LIST_PARAM_NAME,
				Collections.singletonList(accountGuid),
				new Object[]{},
				pageStartIndex, pageSize);
	}

	private void validateAccounts(final String parent, final String child) throws IllegalArgumentException {
		final List<String> pathToRoot = findAncestorGuids(parent);
		if (isRootOfTree(child, pathToRoot)) {
			throw new IllegalArgumentException("The child customer is a root of the parents' tree");
		}
	}

	private boolean isRootOfTree(final String childGuid, final List<String> pathToRoot) {
		return !pathToRoot.isEmpty() && pathToRoot.get(pathToRoot.size() - 1).equals(childGuid);
	}

	private List<String> getChildrenGuids(final List<String> parentGuids) {
		return customerService.getPersistenceEngine().retrieveByNamedQueryWithList(ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS,
				LIST_PARAM_NAME, parentGuids);
	}

	@Override
	public Optional<String> fetchParentAccountGuidByChildGuid(final String childGuid) {
		final List<String> guids = customerService.getPersistenceEngine()
				.retrieveByNamedQuery("ACCOUNT_PARENT_GUID_BY_CHILD_GUID", childGuid);
		validateParentGuid(guids);

		return guids.isEmpty()
				? Optional.empty()
				: Optional.of(guids.get(0));
	}

	@Override
	public void remove(final String accountGuid) {
		persistenceEngine.executeNamedQuery("DELETE_ACCOUNT_CLOSURE_BY_DESCENDANT_GUID", accountGuid);
	}

	private void validateParentGuid(final List<String> parentGuidList) {
		if (parentGuidList.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate GUIDs exist -- " + parentGuidList.get(0));
		}
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}
}

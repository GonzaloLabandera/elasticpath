/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.datapolicy.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.DuplicateKeyException;
import com.elasticpath.domain.datapolicy.ConsentAction;
import com.elasticpath.domain.datapolicy.CustomerConsent;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.domain.datapolicy.impl.CustomerConsentHistoryImpl;
import com.elasticpath.service.datapolicy.CustomerConsentService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Perform CRUD operations with CustomerConsent entity.
 */
public class CustomerConsentServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerConsentService {

	private static final String LIST_STRING = "list";

	@Override
	public CustomerConsent save(final CustomerConsent customerConsent) throws DuplicateKeyException {
		//delete old row(if exists)
		deleteByDataPolicyGuidForCustomer(customerConsent.getDataPolicy().getGuid(), customerConsent.getCustomerGuid());
		getPersistenceEngine().flush();

		CustomerConsent savedConsent = getPersistenceEngine().saveOrUpdate(customerConsent);
		getPersistenceEngine().flush();

		//insert into history table as well
		CustomerConsent history = getPrototypeBean(ContextIdNames.CUSTOMER_CONSENT_HISTORY, CustomerConsentHistoryImpl.class);
		BeanUtils.copyProperties(savedConsent, history);
		getPersistenceEngine().save(history);

		return customerConsent;
	}

	@Override
	public void updateCustomerGuids(final String oldCustomerGuid, final String newCustomerGuid) throws DuplicateKeyException {
		List<CustomerConsent> allCustomerContests = getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMERCONSENT_FIND_BY_CUSTOMER_GUIDS",
				LIST_STRING, Arrays.asList(oldCustomerGuid, newCustomerGuid));

		List<Long> toCleanup = filterConsentUidpksToCleanup(allCustomerContests);

		getPersistenceEngine().executeNamedQueryWithList("CUSTOMERCONSENT_CLEANUP_BY_UIDPKS",
				LIST_STRING, toCleanup);

		getPersistenceEngine().executeNamedQuery("CUSTOMERCONSENT_UPDATE_CUSTOMER_GUID_BY_OLD_CUSTOMER_GUID",
				oldCustomerGuid, newCustomerGuid);

		getPersistenceEngine().executeNamedQuery("CUSTOMERCONSENTHISTORY_UPDATE_CUSTOMER_GUID_BY_OLD_CUSTOMER_GUID",
				oldCustomerGuid, newCustomerGuid);
	}

	private List<Long> filterConsentUidpksToCleanup(final List<CustomerConsent> allCustomerContests) {
		Map<Long, CustomerConsent> policyToNewestConsent = new HashMap<>();
		List<Long> toCleanup = new ArrayList<>();

		for (CustomerConsent customerConsent : allCustomerContests) {
			if (policyToNewestConsent.containsKey(customerConsent.getDataPolicy().getUidPk())) {
				CustomerConsent mappedConsent = policyToNewestConsent.get(customerConsent.getDataPolicy().getUidPk());
				if (mappedConsent.getConsentDate().before(customerConsent.getConsentDate())
						|| mappedConsent.getUidPk() < customerConsent.getUidPk()) {
					toCleanup.add(mappedConsent.getUidPk());
					policyToNewestConsent.put(customerConsent.getDataPolicy().getUidPk(), customerConsent);
				} else {
					toCleanup.add(customerConsent.getUidPk());
					policyToNewestConsent.put(mappedConsent.getDataPolicy().getUidPk(), mappedConsent);
				}
			} else {
				policyToNewestConsent.put(customerConsent.getDataPolicy().getUidPk(), customerConsent);
			}
		}
		return toCleanup;
	}

	@Override
	public List<CustomerConsent> list() throws EpServiceException {
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMERCONSENT_SELECT_ALL");
	}

	@Override
	public List<CustomerConsent> listHistory() throws EpServiceException {
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMERCONSENTHISTORY_SELECT_ALL");
	}

	@Override
	public CustomerConsent load(final long customerConsentUid) throws EpServiceException {
		CustomerConsent customerConsent;
		if (customerConsentUid <= 0) {
			customerConsent = getPrototypeBean(ContextIdNames.CUSTOMER_CONSENT, CustomerConsent.class);
		} else {
			customerConsent = getPersistentBeanFinder().load(ContextIdNames.CUSTOMER_CONSENT, customerConsentUid);
		}
		return customerConsent;
	}

	@Override
	public CustomerConsent get(final long customerConsentUid) throws EpServiceException {
		CustomerConsent customerConsent;
		if (customerConsentUid <= 0) {
			customerConsent = getPrototypeBean(ContextIdNames.CUSTOMER_CONSENT, CustomerConsent.class);
		} else {
			customerConsent = getPersistentBeanFinder().get(ContextIdNames.CUSTOMER_CONSENT, customerConsentUid);
		}
		return customerConsent;
	}

	@Override
	public CustomerConsent findByGuid(final String guid) throws EpServiceException {
		final List<CustomerConsent> customerConsents =
				getPersistenceEngine().retrieveByNamedQuery("CUSTOMERCONSENTHISTORY_FIND_BY_GUID", guid);
		if (customerConsents.isEmpty()) {
			return null;
		}
		if (customerConsents.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate guid:" + guid);
		}
		return customerConsents.get(0);
	}

	@Override
	public List<CustomerConsent> findByGuids(final List<String> guids) throws EpServiceException {
		final List<CustomerConsent> customerConsents =
				getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMERCONSENTHISTORY_FIND_BY_GUIDS", LIST_STRING, guids);
		if (customerConsents.isEmpty()) {
			return null;
		}
		return customerConsents;
	}

	@Override
	public List<CustomerConsent> findActiveConsentsByCustomerGuid(final String customerGuid) throws EpServiceException {
		final List<CustomerConsent> customerConsents =
				getPersistenceEngine().retrieveByNamedQuery("CUSTOMERCONSENT_FIND_BY_CUSTOMER_GUID", customerGuid);
		if (customerConsents.isEmpty()) {
			return null;
		}
		return customerConsents;
	}

	@Override
	public List<CustomerConsent> findHistoryByCustomerGuid(final String customerGuid) throws EpServiceException {
		final List<CustomerConsent> customerConsents =
				getPersistenceEngine().retrieveByNamedQuery("CUSTOMERCONSENTHISTORY_FIND_BY_CUSTOMER_GUID", customerGuid);
		if (customerConsents.isEmpty()) {
			return null;
		}
		return customerConsents;
	}

	@Override
	public CustomerConsent findByDataPolicyGuidForCustomerLatest(final String dataPolicyGuid, final String customerGuid) {
		final List<CustomerConsent> customerConsents =
				getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_CONSENT_FIND_BY_CUSTOMER_AND_DATA_POLICY_GUID_LATEST",
						customerGuid, dataPolicyGuid);
		if (customerConsents.isEmpty()) {
			return null;
		}
		return customerConsents.get(0);
	}

	@Override
	public void deleteByDataPolicyGuidForCustomer(final String dataPolicyGuid, final String customerGuid) {
		getPersistenceEngine().executeNamedQuery("CUSTOMER_CONSENT_DELETE_BY_CUSTOMER_AND_DATA_POLICY_GUID_LATEST",
				customerGuid, dataPolicyGuid);


	}

	@Override
	public void deleteByCustomerUids(final List<Long> customerUids) {
		getPersistenceEngine().executeNamedQueryWithList("CUSTOMER_CONSENT_DELETE_BY_CUSTOMER_UIDS_LATEST", LIST_STRING, customerUids);
		getPersistenceEngine().executeNamedQueryWithList("CUSTOMER_CONSENT_DELETE_BY_CUSTOMER_UIDS", LIST_STRING, customerUids);
		getPersistenceEngine().flush();
	}


	/**
	 * Generic load method for all persistable domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	@Override
	public List<CustomerConsent> findWithActiveDataPoliciesByCustomerGuid(final String customerGuid, final boolean includeDisabledPolicies)
			throws EpServiceException {
		if (includeDisabledPolicies) {
			return getPersistenceEngine()
					.retrieveByNamedQuery("CUSTOMER_CONSENTS_FIND_LATEST_ACTIVE_OR_DISABLED_BY_CUSTOMER", customerGuid);
		}

		return getPersistenceEngine()
				.retrieveByNamedQuery("CUSTOMER_CONSENTS_FIND_LATEST_ACTIVE_BY_CUSTOMER", customerGuid);
	}

	@Override
	public Boolean customerHasGivenConsentForAtLeastOneDataPolicy(final String customerGuid, final Set<DataPolicy> policies) {

		List<CustomerConsent> acceptedConsents = getPersistenceEngine()
				.retrieveByNamedQuery("CUSTOMER_CONSENTS_FIND_LATEST_BY_CUSTOMER", customerGuid, ConsentAction.GRANTED);
		return acceptedConsents.stream()
				.anyMatch(customerConsent -> policies.contains(customerConsent.getDataPolicy()));
	}
}

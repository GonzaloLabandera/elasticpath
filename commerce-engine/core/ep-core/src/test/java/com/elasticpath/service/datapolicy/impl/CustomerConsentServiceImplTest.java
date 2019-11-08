/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.datapolicy.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.datapolicy.CustomerConsent;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.domain.datapolicy.impl.CustomerConsentHistoryImpl;
import com.elasticpath.persistence.api.PersistenceEngine;

@RunWith(MockitoJUnitRunner.class)
public class CustomerConsentServiceImplTest {

	private static final long CUSTOMER_CONSENT_UIDPK = 1L;
	private static final String CUSTOMER_CONSENT_GUID = "GUID1";
	private static final String CUSTOMER_GUID = "CUSTOMER_GUID1";
	private static final String CUSTOMER_GUID_2 = "CUSTOMER_GUID2";
	public static final String LIST = "list";

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private CustomerConsent customerConsent;

	@Mock
	private CustomerConsentHistoryImpl customerConsentHistory;

	@Mock
	private ElasticPath elasticPath;

	@InjectMocks
	private CustomerConsentServiceImpl customerConsentServiceImpl;

	@Before
	public void setUp() {
		Mockito.<Class<CustomerConsent>>when(elasticPath.getBeanImplClass(ContextIdNames.CUSTOMER_CONSENT)).thenReturn(CustomerConsent.class);

		when(elasticPath.getBean(ContextIdNames.CUSTOMER_CONSENT)).thenReturn(customerConsent);
		when(elasticPath.getBean(ContextIdNames.CUSTOMER_CONSENT_HISTORY)).thenReturn(customerConsentHistory);
	}

	@Test
	public void verifyAdd() {
		DataPolicy policy = mock(DataPolicy.class);

		when(customerConsent.getDataPolicy()).thenReturn(policy);
		when(policy.getGuid()).thenReturn("DataPolicyGuid");

		when(persistenceEngine.saveOrUpdate(customerConsent)).thenReturn(customerConsent);

		when(customerConsent.getCustomerGuid()).thenReturn("CustomerGuid");

		assertThat(customerConsentServiceImpl.save(customerConsent))
				.isEqualTo(customerConsent);

		verify(persistenceEngine).saveOrUpdate(customerConsent);
		verify(persistenceEngine).save(customerConsentHistory);
	}

	@Test
	public void verifyUpdate() {
		customerConsentServiceImpl.updateCustomerGuids(CUSTOMER_GUID, CUSTOMER_GUID_2);

		verify(persistenceEngine).retrieveByNamedQueryWithList("CUSTOMERCONSENT_FIND_BY_CUSTOMER_GUIDS",
				LIST, Arrays.asList(CUSTOMER_GUID, CUSTOMER_GUID_2));

		verify(persistenceEngine).executeNamedQueryWithList("CUSTOMERCONSENT_CLEANUP_BY_UIDPKS",
				LIST, Collections.emptyList());

		verify(persistenceEngine).executeNamedQuery("CUSTOMERCONSENT_UPDATE_CUSTOMER_GUID_BY_OLD_CUSTOMER_GUID",
				CUSTOMER_GUID, CUSTOMER_GUID_2);

		verify(persistenceEngine).executeNamedQuery("CUSTOMERCONSENTHISTORY_UPDATE_CUSTOMER_GUID_BY_OLD_CUSTOMER_GUID",
				CUSTOMER_GUID, CUSTOMER_GUID_2);
	}

	@Test
	public void loadWithUidPkOfZeroReturnsNewCustomerConsentFromBean() {
		assertThat(customerConsentServiceImpl.load(0))
				.isEqualTo(customerConsent);

		verify(elasticPath).getBean(ContextIdNames.CUSTOMER_CONSENT);
	}

	@Test
	public void loadWithUidPkGreaterThanZeroReturnsTheCorrespondingCustomerConsentFromThePersistenceEngine() {
		when(persistenceEngine.load(CustomerConsent.class, CUSTOMER_CONSENT_UIDPK)).thenReturn(customerConsent);
		CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl customerConsentService =
				new CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl();

		customerConsentService.setPersistenceEngine(persistenceEngine);
		customerConsentService.setElasticPath(elasticPath);

		assertThat(customerConsentService.load(CUSTOMER_CONSENT_UIDPK))
				.isEqualTo(customerConsent);

		verify(persistenceEngine).load(CustomerConsent.class, CUSTOMER_CONSENT_UIDPK);
	}

	@Test
	public void listDelegatesQueryAndReturnsListOfResultingCustomerConsents() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMERCONSENT_SELECT_ALL")).thenReturn(Collections.singletonList(customerConsent));

		assertThat(customerConsentServiceImpl.list())
				.isEqualTo(Collections.singletonList(customerConsent));
	}

	@Test
	public void listHistoryDelegatesQueryAndReturnsListOfResultingCustomerConsents() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMERCONSENTHISTORY_SELECT_ALL"))
				.thenReturn(Collections.singletonList(customerConsent));

		assertThat(customerConsentServiceImpl.listHistory())
				.isEqualTo(Collections.singletonList(customerConsent));
	}

	@Test
	public void listRetriveByGuidsDelegatesQueryAndReturnsListOfResultingDataCustomerConsents() {
		List<String> guidList = Collections.singletonList(CUSTOMER_CONSENT_GUID);

		when(persistenceEngine.retrieveByNamedQueryWithList("CUSTOMERCONSENTHISTORY_FIND_BY_GUIDS",
				LIST, guidList)).thenReturn(Collections.singletonList(customerConsent));

		assertThat(customerConsentServiceImpl.findByGuids(guidList))
				.isEqualTo(Collections.singletonList(customerConsent));
	}

	@Test
	public void listRetriveByIncorrectGuidsDelegatesQueryAndReturnsNull() {
		List<String> guidList = Collections.singletonList(CUSTOMER_CONSENT_GUID);

		when(persistenceEngine.retrieveByNamedQueryWithList("CUSTOMERCONSENTHISTORY_FIND_BY_GUIDS",
				LIST, guidList)).thenReturn(Collections.emptyList());

		assertThat(customerConsentServiceImpl.findByGuids(guidList))
				.isNull();
	}

	@Test
	public void listRetriveByShopperCustomerGuidDelegatesQueryAndReturnsListOfResultingDataCustomerConsents() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMERCONSENT_FIND_BY_CUSTOMER_GUID", CUSTOMER_GUID))
				.thenReturn(Collections.singletonList(customerConsent));

		assertThat(customerConsentServiceImpl.findActiveConsentsByCustomerGuid(CUSTOMER_GUID))
				.isEqualTo(Collections.singletonList(customerConsent));
	}

	@Test
	public void listRetriveByIncorrectShopperCustomerGuidDelegatesQueryAndReturnsNull() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMERCONSENT_FIND_BY_CUSTOMER_GUID", CUSTOMER_GUID)).
				thenReturn(Collections.emptyList());

		assertThat(customerConsentServiceImpl.findActiveConsentsByCustomerGuid(CUSTOMER_GUID))
				.isNull();
	}

	@Test
	public void nullReturnedWhenCustomerConsentDoesNotExistForGivenUidPk() {
		CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl customerConsentService =
				new CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl();

		customerConsentService.setPersistenceEngine(persistenceEngine);
		customerConsentService.setElasticPath(elasticPath);

		assertThat(customerConsentService.get(CUSTOMER_CONSENT_UIDPK))
				.isEqualTo(null);
	}

	@Test
	public void getWithUidPkOfZeroReturnsNewCustomerConsentFromBean() {
		assertThat(customerConsentServiceImpl.get(0))
				.isEqualTo(customerConsent);

		verify(elasticPath).getBean(ContextIdNames.CUSTOMER_CONSENT);
	}

	@Test
	public void shouldReturnCustomerConsentInstanceForGivenUidPk() {
		CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl customerConsentService =
				new CustomerConsentServiceImplTest.MockCustomerConsentServiceImpl();

		when(persistenceEngine.get(CustomerConsent.class, CUSTOMER_CONSENT_UIDPK)).thenReturn(customerConsent);

		customerConsentService.setPersistenceEngine(persistenceEngine);
		customerConsentService.setElasticPath(elasticPath);

		assertThat(customerConsentService.get(CUSTOMER_CONSENT_UIDPK))
				.isEqualTo(customerConsent);
	}

	@Test
	public void shouldFindCustomerConsentWithActivePolicies() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMER_CONSENTS_FIND_LATEST_ACTIVE_BY_CUSTOMER", CUSTOMER_GUID))
				.thenReturn(Collections.singletonList(customerConsent));

		List<CustomerConsent> actualResult = customerConsentServiceImpl.findWithActiveDataPoliciesByCustomerGuid(CUSTOMER_GUID,
				false);

		assertThat(actualResult)
				.containsExactly(customerConsent);
	}

	@Test
	public void shouldFindCustomerConsentWithActiveAndDisabledPolicies() {
		when(persistenceEngine.retrieveByNamedQuery("CUSTOMER_CONSENTS_FIND_LATEST_ACTIVE_OR_DISABLED_BY_CUSTOMER", CUSTOMER_GUID))
				.thenReturn(Collections.singletonList(customerConsent));

		List<CustomerConsent> actualResult = customerConsentServiceImpl.findWithActiveDataPoliciesByCustomerGuid(CUSTOMER_GUID,
				true);

		assertThat(actualResult)
				.containsExactly(customerConsent);
	}

	private class MockCustomerConsentServiceImpl extends CustomerConsentServiceImpl {
		@Override
		public PersistentBeanFinder getPersistentBeanFinder() {
			return super.getPersistentBeanFinder();
		}

		@Override
		public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
			super.setPersistenceEngine(persistenceEngine);
		}
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.integration.service.shopper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.datapolicy.CustomerConsent;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.datapolicy.CustomerConsentService;
import com.elasticpath.service.datapolicy.DataPolicyService;
import com.elasticpath.service.shopper.impl.CustomerConsentMergerForShopperUpdates;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Test functionality regarding {@link CustomerConsentMergerForShopperUpdates}.
 */
public class CustomerConsentMergerForShopperUpdatesTest extends AbstractDataPolicyTest {

	private static final String CUSTOMER_CONSENT_CODE1 = "CUSTOMER_CONSENT_CODE1";
	private static final String CUSTOMER_CONSENT_CODE2 = "CUSTOMER_CONSENT_CODE2";
	private static final String CUSTOMER_CONSENT_CODE3 = "CUSTOMER_CONSENT_CODE3";
	private static final String CUSTOMER_CONSENT_CODE4 = "CUSTOMER_CONSENT_CODE4";
	private static final String CUSTOMER_CONSENT_CODE5 = "CUSTOMER_CONSENT_CODE5";

	@Autowired
	@Qualifier("customerConsentMergerForShopperUpdates")
	private CustomerConsentMergerForShopperUpdates customerConsentMergerForShopperUpdates;

	private SimpleStoreScenario scenario;

	@Autowired
	private CustomerConsentService customerConsentService;

	@Autowired
	private DataPolicyService dataPolicyService;

	private DataPolicy dataPolicy1;

	private DataPolicy dataPolicy2;


	/**
	 * Setup the tests.
	 */
	@Before
	public void setUp() {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
		dataPolicy1 = createAndSaveDataPolicy(DATA_POLICY_UNIQUE_CODE);
		dataPolicy2 = createAndSaveDataPolicy(DATA_POLICY_UNIQUE_CODE2);

		assertThat(dataPolicyService.list()).hasSize(2);
	}

	/**
	 * Test that the correct customer consents are attached to the registered account after logging in.<br>
	 * In other words, test when an anonymous shopper logs in to a registered account, that the customer consents acknowledged, will,
	 * going forward are now associated with the registered account and not the one created for the anonymous shopper.<br>
	 * Also tests that the anonymous customer consents are no longer available in this case.
	 */
	@DirtiesDatabase
	@Test
	public void testMergingCustomerConsentsFromAnonymouslyAcceptedConsentsToCustomerAccount() {
		Customer registeredCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL, false);
		CustomerSession registeredCustomerSession = createCustomerSession(registeredCustomer, scenario.getCatalog());

		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE1, registeredCustomer, dataPolicy1);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(1);

		Customer anonymousCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL2, true);
		CustomerSession anonymousCustomerSession = createCustomerSession(anonymousCustomer, scenario.getCatalog());
		Shopper anonymousShopper = anonymousCustomerSession.getShopper();
		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE2, anonymousCustomer, dataPolicy2);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.hasSize(1);

		customerConsentMergerForShopperUpdates.invalidateShopper(registeredCustomerSession, anonymousShopper);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(2);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.isNull();
	}


	/**
	 * Test that customer consents if the originating account is already registered.
	 * Also tests that the registered customer consents are available in this case.
	 */
	@DirtiesDatabase
	@Test
	public void testNotMergingCustomerConsentsFromRegisteredAccountAcceptedConsentsToAnotherCustomerAccount() {
		Customer secondRegisteredCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL, false);
		CustomerSession secondRegisteredCustomerSession = createCustomerSession(secondRegisteredCustomer, scenario.getCatalog());

		createAndSaveCustomerConsent(DATA_POLICY_UNIQUE_CODE, secondRegisteredCustomer);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(secondRegisteredCustomer.getGuid()))
				.hasSize(1);

		Customer originalRegisteredCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL2, false);
		CustomerSession originalRegisteredCustomerSession = createCustomerSession(originalRegisteredCustomer, scenario.getCatalog());
		Shopper originalRegisteredShopper = originalRegisteredCustomerSession.getShopper();
		createAndSaveCustomerConsent(DATA_POLICY_UNIQUE_CODE2, originalRegisteredCustomer);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(originalRegisteredCustomer.getGuid()))
				.hasSize(1);

		customerConsentMergerForShopperUpdates.invalidateShopper(secondRegisteredCustomerSession, originalRegisteredShopper);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(secondRegisteredCustomer.getGuid()))
				.hasSize(1);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(originalRegisteredCustomer.getGuid()))
				.hasSize(1);

	}



	/**
	 * Test that the correct customer consents are attached to the registered account after logging in.<br>
	 * In other words, test when an anonymous shopper logs in to a registered account, that the customer consents acknowledged, will,
	 * going forward are now associated with the registered account and not the one created for the anonymous shopper.<br>
	 * Also tests that the anonymous customer consents are no longer available in this case.
	 */
	@DirtiesDatabase
	@Test
	public void testMergingCustomerConsentsFromAnonymouslyAcceptedConsentsToCustomerAccountWithHistory() {
		Customer registeredCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL, false);
		CustomerSession registeredCustomerSession = createCustomerSession(registeredCustomer, scenario.getCatalog());

		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE1, registeredCustomer, dataPolicy1);
		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE2, registeredCustomer, dataPolicy2);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(2);

		Customer anonymousCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL2, true);
		CustomerSession anonymousCustomerSession = createCustomerSession(anonymousCustomer, scenario.getCatalog());
		Shopper anonymousShopper = anonymousCustomerSession.getShopper();

		CustomerConsent consent1 = createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE3, anonymousCustomer, dataPolicy1);
		CustomerConsent consent2 = createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE4, anonymousCustomer, dataPolicy2);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.hasSize(2);

		customerConsentMergerForShopperUpdates.invalidateShopper(registeredCustomerSession, anonymousShopper);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.containsExactlyInAnyOrder(consent1, consent2);

		assertThat(customerConsentService.findHistoryByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(4);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.isNull();
	}


	/**
	 * Test that the correct customer consents are attached to the registered account after logging in.<br>
	 * In other words, test when an anonymous shopper logs in to a registered account, that the customer consents acknowledged, will,
	 * going forward are now associated with the registered account and not the one created for the anonymous shopper.<br>
	 * Also tests that the anonymous customer consents are no longer available in this case.
	 */
	@DirtiesDatabase
	@Test
	public void testMergingCustomerConsentsFromAnonymouslyAcceptedConsentsToCustomerAccountWithHistoryMixedInserts() {
		Customer registeredCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL, false);
		CustomerSession registeredCustomerSession = createCustomerSession(registeredCustomer, scenario.getCatalog());

		Customer anonymousCustomer = createPersistedCustomer(scenario.getStore().getCode(), TEST_EMAIL2, true);
		CustomerSession anonymousCustomerSession = createCustomerSession(anonymousCustomer, scenario.getCatalog());
		Shopper anonymousShopper = anonymousCustomerSession.getShopper();

		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE2, registeredCustomer, dataPolicy2);
		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE3, anonymousCustomer, dataPolicy1);
		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE1, registeredCustomer, dataPolicy1);
		createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE5, registeredCustomer, dataPolicy2);

		CustomerConsent consent1 = createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE4, anonymousCustomer, dataPolicy2);
		CustomerConsent consent2 = createAndSaveCustomerConsent(CUSTOMER_CONSENT_CODE1, anonymousCustomer, dataPolicy1);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.hasSize(2);
		assertThat(customerConsentService.findHistoryByCustomerGuid(anonymousCustomer.getGuid()))
				.hasSize(3);
		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(2);
		assertThat(customerConsentService.findHistoryByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(3);

		customerConsentMergerForShopperUpdates.invalidateShopper(registeredCustomerSession, anonymousShopper);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(registeredCustomer.getGuid()))
				.containsExactlyInAnyOrder(consent1, consent2);

		assertThat(customerConsentService.findHistoryByCustomerGuid(registeredCustomer.getGuid()))
				.hasSize(6);

		assertThat(customerConsentService.findActiveConsentsByCustomerGuid(anonymousCustomer.getGuid()))
				.isNull();
	}

}

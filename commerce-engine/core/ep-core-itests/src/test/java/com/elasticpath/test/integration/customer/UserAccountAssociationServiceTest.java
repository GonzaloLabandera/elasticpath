/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.test.integration.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * User Account Association Service itest.
 */
public class UserAccountAssociationServiceTest extends DbTestCase {

	@Autowired
	private UserAccountAssociationService userAccountAssociationService;

	@Autowired
	private CustomerService customerService;

	private Customer buyer;

	private Customer account;

	private Store store;

	private static final String CUSTOMER_NAME1 = "testcustomer1@UserAccountAssociationService.com";

	private static final String CUSTOMER_NAME2 = "testcustomer2@UserAccountAssociationService.com";

	private static final String BUYER_ROLE = "BUYER";

	@Before
	public void initialize() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		store = scenario.getStore();
		buyer = createCustomer(store.getCode(), CUSTOMER_NAME1, CustomerType.REGISTERED_USER);
		account = createCustomer(store.getCode(), CUSTOMER_NAME2, CustomerType.ACCOUNT);
	}

	@DirtiesDatabase
	@Test
	public void testAssociateUserToAccount() {
		//associate user to account
		UserAccountAssociation userAccountAssociation = doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer,
				account, BUYER_ROLE));
		assertThat(userAccountAssociation)
				.as("user account association does not contain expected fields")
				.hasFieldOrPropertyWithValue("userGuid", buyer.getGuid())
				.hasFieldOrPropertyWithValue("accountGuid", account.getGuid())
				.hasFieldOrPropertyWithValue("role", BUYER_ROLE);

		//verify association can be found by account
		assertThat(userAccountAssociationService.findAssociationsForAccount(account))
				.as("user account association was not found").contains(userAccountAssociation);

		//verify association can be found by user
		assertThat(userAccountAssociationService.findAssociationsForUser(buyer))
				.as("user account association was not found")
				.contains(userAccountAssociation);

		//remove association
		assertThatCode(() -> doInTransaction(status -> userAccountAssociationService.disassociateUserFromAccount(buyer, account)))
				.doesNotThrowAnyException();


		//verify the association can not be found
		assertThat(userAccountAssociationService.findAssociationsForUser(buyer))
				.as("user account association was not found")
				.doesNotContain(userAccountAssociation);
	}

	@DirtiesDatabase
	@Test
	public void testIsExistingUserAssociation() {
		
		// associate user to account
		doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer, account, BUYER_ROLE));

		boolean isExistingAssociation = userAccountAssociationService.isExistingUserAssociation(account.getGuid(), buyer.getGuid());

		assertTrue(isExistingAssociation);

	}
	
	@DirtiesDatabase
	@Test
	public void testAssociateUserToAccountValidation() {
		//associate account to an invalid customer type
		Customer accountWithInvalidType = createCustomer(store.getCode(), "account2@elasticpath.com", CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer.getGuid(),
				accountWithInvalidType.getGuid(), BUYER_ROLE)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageStartingWith("Invalid UserAccountAssociation: The supplied Account guid is not a customer of type ACCOUNT");
	}

	@DirtiesDatabase
	@Test
	public void testAssociateDuplicateValidation() {
		//associate user to account
		UserAccountAssociation userAccountAssociation = doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer,
				account, BUYER_ROLE));

		//attempt to recreate the same association
		assertThatThrownBy(() -> doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer,
				account, BUYER_ROLE)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@DirtiesDatabase
	@Test
	public void testFindAssociationByGuid() {
		//associate user to account
		UserAccountAssociation userAccountAssociation = doInTransaction(status -> userAccountAssociationService.associateUserToAccount(buyer,
				account, BUYER_ROLE));

		//Assert that the association can be found by guid
		assertThat(userAccountAssociationService.findByGuid(userAccountAssociation.getGuid()))
				.as("UserAccountAssociation was not found by guid")
				.isEqualTo(userAccountAssociation);
	}

	private Customer createCustomer(final String storeCode, final String shardId, final CustomerType type) {
		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(shardId);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(storeCode);
		customer.setCustomerType(type);
		customer = customerService.add(customer);
		return customer;
	}


}

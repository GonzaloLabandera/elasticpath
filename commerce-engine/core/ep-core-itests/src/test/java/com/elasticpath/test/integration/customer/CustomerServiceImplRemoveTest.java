/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.test.integration.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shopper.dao.ShopperDao;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Account Service itest
 */
public class CustomerServiceImplRemoveTest extends DbTestCase {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserAccountAssociationService userAccountAssociationService;

	@Autowired
	private ShopperDao shopperDao;

	@Autowired
	private OrderService orderService;

	private Customer accountParent, accountWithOrder;

	private static final String CUSTOMER_CHILD_NAME = "test-delete-account-child@AccountService.com";
	private static final String CUSTOMER_PARENT_NAME = "test-delete-account-parent@AccountService.com";
	private static final String CUSTOMER_WITH_ORDER = "test_delete-account-with-order@AccountService.com";

	private static final String USER_NAME = "test-delete-account-user@AccountService.com";
	private static final String USER_NAME_WITH_ORDER = "test-delete-account-user-with-order@AccountService.com";

	private static final String BUYER_ROLE = "BUYER";

	@Before
	public void setUp() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		Store store = scenario.getStore();

		createAccountWithoutOrder(store);
		createAccountWithOrder(store);
	}

	private void createAccountWithOrder(Store store) {
		accountWithOrder = createCustomer(store, CUSTOMER_WITH_ORDER, CustomerType.ACCOUNT);
		accountWithOrder = customerService.add(accountWithOrder);

		Customer buyerWithOrder = createCustomer(store, USER_NAME_WITH_ORDER, CustomerType.REGISTERED_USER);
		buyerWithOrder = customerService.add(buyerWithOrder);

		userAccountAssociationService.associateUserToAccount(buyerWithOrder, accountWithOrder, BUYER_ROLE);

		orderService.add(createOrder(store, accountWithOrder));
	}

	private void createAccountWithoutOrder(Store store) {
		accountParent = createCustomer(store, CUSTOMER_PARENT_NAME, CustomerType.ACCOUNT);
		accountParent = customerService.add(accountParent);

		Customer buyer = createCustomer(store, USER_NAME, CustomerType.REGISTERED_USER);
		buyer = customerService.add(buyer);

		userAccountAssociationService.associateUserToAccount(buyer, accountParent, BUYER_ROLE);

		Customer accountChild = createCustomer(store, CUSTOMER_CHILD_NAME, CustomerType.ACCOUNT, accountParent.getGuid());
		customerService.add(accountChild);
	}

	@DirtiesDatabase
	@Test
	public void testThatAccountIsDeleted() {
		customerService.remove(accountParent);

		Customer accountByGuid = customerService.findByGuid(accountParent.getGuid());
		List<ShopperMemento> shoppers = shopperDao.findByAccountGuid(accountParent.getGuid());
		Collection<UserAccountAssociation> associationsForAccount = userAccountAssociationService.findAssociationsForAccount(accountParent);

		assertThat(shoppers).isEmpty();
		assertThat(accountByGuid).isNull();
		assertThat(associationsForAccount).isEmpty();
	}

	@DirtiesDatabase
	@Test
	public void testThatAccountIsNotDeleted() {
		assertThatExceptionOfType(EpServiceException.class).isThrownBy(() ->
				customerService.remove(accountWithOrder)
		).withMessage("Account cannot be deleted - the account or its children has associated orders");
	}

	private Customer createCustomer(Store store, String customerName, CustomerType type, String parentGuid) {
		Customer customer = createCustomer(store, customerName, type);
		customer.setParentGuid(parentGuid);

		return customer;
	}

	private Customer createCustomer(Store store, String customerName, CustomerType type) {
		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(customerName);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());
		customer.setCustomerType(type);

		return customer;
	}

	private Order createOrder(Store store, Customer account) {
		Order order = getBeanFactory().getPrototypeBean(ContextIdNames.ORDER, Order.class);
		order.setLocale(Locale.US);
		order.setCreatedDate(new Date());
		order.setStoreCode(store.getCode());
		order.setAccount(account);

		return order;
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */

package com.elasticpath.service.shoppingcart.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.impl.OrderAddressImpl;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.rules.AppliedRule;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.impl.AppliedRuleImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.PromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.impl.ShipmentTypeShoppingCartVisitor;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartMementoImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.plugin.tax.domain.TaxExemption;
import com.elasticpath.plugin.tax.domain.impl.TaxExemptionImpl;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.impl.ShoppingItemHasRecurringPricePredicate;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.shoppingcart.OrderSkuFactory;
import com.elasticpath.test.factory.TestCustomerProfileFactory;
import com.elasticpath.test.factory.TestShopperFactory;
import com.elasticpath.test.factory.TestShoppingCartFactory;

/**
 * Test that {@link OrderFactoryImpl} behaves as expected.
 */
@SuppressWarnings("PMD.ExcessiveImports")
@RunWith(MockitoJUnitRunner.class)
public class OrderFactoryImplTest {

	@org.junit.Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final String CART_ORDER_GUID = "cart_order_guid";
	private static final Locale LOCALE = Locale.CANADA;
	private static final String RULE_DELETED_MESSAGE = "Referenced rule was deleted";

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private OrderService orderService;
	@Mock
	private CartOrderService cartOrderService;
	@Mock
	private TimeService timeService;
	@Mock
	private CustomerService customerService;
	@Mock
	private OrderSkuFactory orderSkuFactory;
	@Mock
	private ProductSkuLookup productSkuLookup;
	@Mock
	private RuleService ruleService;

	private final AppliedRule.Visitor appliedRuleVisitor = appliedRule -> {
	};

	private OrderFactoryImpl orderFactory;
	private CustomerSession customerSession;
	private Store store;
	private Customer mockAccount;

	/**
	 * Setup required for each test.
	 *
	 * @throws Exception in case of error
	 */
	@Before
	public void setUp() throws Exception {
		orderFactory = new OrderFactoryImpl();
		orderFactory.setBeanFactory(beanFactory);
		orderFactory.setOrderService(orderService);
		orderFactory.setCartOrderService(cartOrderService);
		orderFactory.setTimeService(timeService);
		orderFactory.setOrderSkuFactory(orderSkuFactory);
		orderFactory.setRuleService(ruleService);
		orderFactory.setAppliedRuleVisitor(appliedRuleVisitor);

		customerSession = mock(CustomerSession.class);
		store = mock(Store.class);

		final EventOriginatorHelper eventOriginatorHelper = mock(EventOriginatorHelper.class);

		when(eventOriginatorHelper.getCustomerOriginator(any(Customer.class))).thenReturn(mock(EventOriginator.class));

		when(timeService.getCurrentTime()).thenReturn(new Date());
		when(customerSession.getCurrency()).thenReturn(Currency.getInstance("CAD"));
		when(customerSession.getLocale()).thenReturn(LOCALE);
		when(store.getCode()).thenReturn("store");

		final ShoppingItemHasRecurringPricePredicate recurringPricePredicate = new ShoppingItemHasRecurringPricePredicate();
		final ShipmentTypeShoppingCartVisitor shoppingCartVisitor = new ShipmentTypeShoppingCartVisitor(recurringPricePredicate, productSkuLookup);

		mockBeanFactorySingletonExpectation(beanFactory, ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class, eventOriginatorHelper);
		mockBeanFactorySingletonExpectation(beanFactory, ContextIdNames.CUSTOMER_SERVICE, CustomerService.class, customerService);
		mockBeanFactoryPrototypeExpectation(beanFactory, ContextIdNames.SHOPPING_CART_MEMENTO, ShoppingCart.class,
				mock(ShoppingCartMementoImpl.class));
		mockBeanFactoryPrototypeExpectation(beanFactory, ContextIdNames.ORDER_ADDRESS, OrderAddress.class, mock(OrderAddressImpl.class));
		mockBeanFactoryPrototypeExpectation(beanFactory, ContextIdNames.SHIPMENT_TYPE_SHOPPING_CART_VISITOR, ShipmentTypeShoppingCartVisitor.class,
				shoppingCartVisitor);
		mockBeanFactoryPrototypeExpectation(beanFactory, ContextIdNames.APPLIED_RULE, AppliedRule.class, AppliedRuleImpl.class);
	}

	/**
	 * Test that an anonymous customer gets profile information copied from the billing address and tax exemption
	 * information copied from the shopping cart.
	 */
	@Test
	public void testUpdateAnonymousCustomer() {
		final Customer customer = createCustomer();
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);

		final ShoppingCart shoppingCart = createShoppingCart();
		final ShoppingCartTaxSnapshot pricingSnapshot = createShoppingCart();

		addBillingAddressToCart(shoppingCart);
		TaxExemption taxExemption = addTaxExemptionToCart(shoppingCart);

		setupOrder();

		when(ruleService.findByUids(Collections.emptySet())).thenReturn(Collections.emptyList());
		when(cartOrderService.getCartOrderGuidByShoppingCartGuid(shoppingCart.getGuid())).thenReturn(CART_ORDER_GUID);
		when(beanFactory.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class)).thenReturn(customerService);

		Order newOrder = orderFactory.createAndPersistNewEmptyOrder(customer, customerSession, shoppingCart, false, false);
		newOrder = orderFactory.fillInNewOrderFromShoppingCart(newOrder, customer, customerSession, shoppingCart, pricingSnapshot);
		assertEquals("The tax exemption ID should have been set", taxExemption.getExemptionId(), newOrder.getTaxExemption().getExemptionId());
		assertEquals("The tax exemption data should have been set",
				taxExemption.getData("data1Key"), newOrder.getTaxExemption().getData("data1Key"));
	}

	@Test
	public void testUpdateAnonymousCustomerWithNullBillingAddress() {
		final Customer customer = createCustomer();
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);

		final ShoppingCart shoppingCart = createShoppingCart();
		final ShoppingCartTaxSnapshot pricingSnapshot = createShoppingCart();

		setupOrder();

		when(ruleService.findByUids(Collections.emptySet())).thenReturn(Collections.emptyList());
		when(cartOrderService.getCartOrderGuidByShoppingCartGuid(shoppingCart.getGuid())).thenReturn(CART_ORDER_GUID);

		Order newOrder = orderFactory.createAndPersistNewEmptyOrder(customer, customerSession, shoppingCart, false, false);
		orderFactory.fillInNewOrderFromShoppingCart(newOrder, customer, customerSession, shoppingCart, pricingSnapshot);

		verify(customerService, never()).updateCustomerFromAddress(any(), any());
	}

	@Test
	public void verifyOrderIsCreatedWithAccount() throws Exception {
		final ShoppingCart shoppingCart = createShoppingCart();
		final Customer customer = createCustomer();
		createShoppingCart();

		customer.setCustomerType(CustomerType.REGISTERED_USER);
		Order order = setupOrder();

		mockAccount = mock(Customer.class);
		Shopper shopper = customerSession.getShopper();
		shopper.setAccount(mockAccount);

		final Order persistedOrder = orderFactory.createAndPersistNewEmptyOrder(
				customer,
				customerSession,
				shoppingCart,
				false,
				false
		);

		assertEquals("Account is not set on persisted order.", persistedOrder.getAccount(), mockAccount);

		final ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);

		final ShoppingCartPricingSnapshot pricingSnapshot = mock(ShoppingCartPricingSnapshot.class);
		when(taxSnapshot.getShoppingCartPricingSnapshot()).thenReturn(pricingSnapshot);

		final PromotionRecordContainer promotionRecordContainer = mock(PromotionRecordContainer.class);
		when(pricingSnapshot.getPromotionRecordContainer()).thenReturn(promotionRecordContainer);

		final Order filledInOrder = orderFactory.fillInNewOrderFromShoppingCart(
				order,
				customer,
				customerSession,
				shoppingCart,
				taxSnapshot);

		assertEquals("Account is not set on filled in order.", filledInOrder.getAccount(), mockAccount);
	}

	@Test
	public void verifyOrderIsCreatedWithAppliedRules() throws Exception {
		final Customer customer = createCustomer();
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);

		final ShoppingCart shoppingCart = createShoppingCart();
		final ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		final ShoppingCartPricingSnapshot pricingSnapshot = mock(ShoppingCartPricingSnapshot.class);

		final Long ruleId1 = 1L;
		final Long ruleId2 = 2L;
		final Long ruleId3 = 3L; // simulates the scenario where this Rule no longer exists or applies

		final Rule rule1 = createRule(ruleId1);
		final Rule rule2 = createRule(ruleId2);

		final AppliedRule expectedAppliedRule1 = mock(AppliedRule.class, "AppliedRule 1");
		final AppliedRule expectedAppliedRule2 = mock(AppliedRule.class, "AppliedRule 2");
		final AppliedRule expectedAppliedRule3 = mock(AppliedRule.class, "AppliedRule 3");

		setupOrder();

		when(cartOrderService.getCartOrderGuidByShoppingCartGuid(shoppingCart.getGuid())).thenReturn(CART_ORDER_GUID);
		when(taxSnapshot.getShoppingCartPricingSnapshot()).thenReturn(pricingSnapshot);

		final PromotionRecordContainer promotionRecordContainer = mock(PromotionRecordContainer.class);
		when(pricingSnapshot.getPromotionRecordContainer()).thenReturn(promotionRecordContainer);

		final ImmutableSet<Long> ruleIds = ImmutableSet.of(ruleId1, ruleId2, ruleId3);
		when(promotionRecordContainer.getAppliedRules()).thenReturn(ruleIds);

		when(ruleService.findByUids(ruleIds)).thenReturn(ImmutableList.of(rule1, rule2));

		when(beanFactory.getPrototypeBean(ContextIdNames.APPLIED_RULE, AppliedRule.class))
				.thenReturn(expectedAppliedRule1)
				.thenReturn(expectedAppliedRule2)
				.thenReturn(expectedAppliedRule3);

		final Order newOrder = orderFactory.createAndPersistNewEmptyOrder(customer, customerSession, shoppingCart, false, false);
		final Order populatedOrder = orderFactory.fillInNewOrderFromShoppingCart(newOrder, customer, customerSession, shoppingCart, taxSnapshot);

		assertThat(populatedOrder.getAppliedRules())
				.containsExactlyInAnyOrder(expectedAppliedRule1, expectedAppliedRule2, expectedAppliedRule3);

		verify(expectedAppliedRule1, times(1)).initialize(rule1, LOCALE);
		verify(expectedAppliedRule1, times(1)).accept(appliedRuleVisitor);

		verify(expectedAppliedRule2, times(1)).initialize(rule2, LOCALE);
		verify(expectedAppliedRule2, times(1)).accept(appliedRuleVisitor);

		verify(expectedAppliedRule3, times(1)).setRuleUid(ruleId3);
		verify(expectedAppliedRule3, times(1)).setRuleCode(RULE_DELETED_MESSAGE);
		verify(expectedAppliedRule3, times(1)).setRuleName(RULE_DELETED_MESSAGE);
	}

	private Rule createRule(final Long ruleId) {
		final Rule rule = mock(Rule.class, "Rule " + ruleId);
		when(rule.getUidPk()).thenReturn(ruleId);
		return rule;
	}

	private Customer createCustomer() {
		final CustomerImpl customer = new CustomerImpl();
		customer.setCustomerProfileAttributes(new TestCustomerProfileFactory().getProfile());

		return customer;
	}

	private ShoppingCartImpl createShoppingCart() {
		final Shopper shopper = TestShopperFactory.getInstance().createNewShopperWithMemento();
		final ShoppingCartImpl cart = TestShoppingCartFactory.getInstance().createNewCartWithMemento(shopper, store);
		when(customerSession.getShopper()).thenReturn(shopper);
		cart.setCustomerSession(customerSession);
		return cart;
	}

	private CustomerAddress addBillingAddressToCart(final ShoppingCart shoppingCart) {
		final CustomerAddress billingAddress = new CustomerAddressImpl();
		billingAddress.setFirstName("James");
		billingAddress.setLastName("Kirk");
		billingAddress.setPhoneNumber("555-873-5867");
		shoppingCart.setBillingAddress(billingAddress);
		return billingAddress;
	}

	private TaxExemption addTaxExemptionToCart(final ShoppingCart shoppingCart) {
		final TaxExemption taxExemption = new TaxExemptionImpl();
		taxExemption.setExemptionId("EXEMPT123");
		taxExemption.addData("data1Key", "data1Value");
		shoppingCart.setTaxExemption(taxExemption);
		return taxExemption;
	}

	private Order setupOrder() {
		final Order order = new OrderImpl();
		when(beanFactory.getPrototypeBean(ContextIdNames.ORDER, Order.class)).thenReturn(order);
		when(orderService.add(order)).thenReturn(order);
		return order;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void mockBeanFactoryPrototypeExpectation(final BeanFactory beanFactory, final String beanName, final Class interfaceClass,
													 final Object bean) {
		when(beanFactory.getPrototypeBean(beanName, interfaceClass)).thenReturn(bean);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void mockBeanFactoryPrototypeExpectation(final BeanFactory beanFactory, final String beanName, final Class interfaceClass,
													 final Class implClass) {
		when(beanFactory.getPrototypeBean(beanName, interfaceClass)).thenAnswer(invocationOnMock -> implClass.newInstance());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void mockBeanFactorySingletonExpectation(final BeanFactory beanFactory, final String beanName, final Class interfaceClass,
													 final Object bean) {
		when(beanFactory.getSingletonBean(beanName, interfaceClass)).thenReturn(bean);
	}
}

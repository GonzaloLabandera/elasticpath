/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.OrderReturnImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.impl.CartItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderDataPopulator;
import com.elasticpath.xpf.converters.ShoppingCartConverter;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyFields"})
@RunWith(MockitoJUnitRunner.class)
public class PopulateOrderDataCheckoutActionTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ShoppingCart shoppingCart;
	@Mock
	private XPFShoppingCart xpfShoppingCart;
	@Mock
	private ShoppingCartTaxSnapshot taxSnapshot;
	@Mock
	private OrderDataPopulator orderDataPopulator1;
	@Mock
	private OrderDataPopulator orderDataPopulator2;
	@Mock
	private XPFExtensionLookup xpfExtensionLookup;
	@Mock
	private ShoppingCartConverter xpfShoppingCartConverter;
	@Mock
	private CustomerSession customerSession;
	@Mock
	private Shopper shopper;


	List<OrderDataPopulator> orderDataPopulators = new ArrayList<>();

	private static final String STORE_CODE = "storecode";
	private final ModifierFieldsMapWrapper modifierFieldsMapWrapper = new ModifierFieldsMapWrapper();
	private final ProductSku sku = new ProductSkuImpl();
	private final OrderReturnImpl exchange = new OrderReturnImpl();
	private final CartItem item = new ShoppingItemImpl();
	private final Order order = new OrderImpl();

	@InjectMocks
	private PopulateOrderDataCheckoutAction checkoutAction;

	private PreCaptureCheckoutActionContextImpl checkoutContext;

	@Mock
	private BeanFactory beanFactory;
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	@Before
	public void setUp() {
		elasticPath.setBeanFactory(beanFactory);

		sku.initialize();
		sku.setSkuCode("SKU");
		item.setSkuGuid(sku.getGuid());

		final boolean isOrderExchange = false;
		final boolean awaitExchangeCompletion = false;
		checkoutContext = new PreCaptureCheckoutActionContextImpl(shoppingCart,
				taxSnapshot,
				customerSession, isOrderExchange, awaitExchangeCompletion, exchange, null);
		checkoutContext.setOrder(order);

		orderDataPopulators.add(orderDataPopulator1);
		orderDataPopulators.add(orderDataPopulator2);

		when(shoppingCart.getRootShoppingItems()).thenReturn(Collections.singletonList(item));
		when(shoppingCart.getModifierFields()).thenReturn(modifierFieldsMapWrapper);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class))
				.thenAnswer(invocation -> new ModifierFieldsMapWrapper());
		when(xpfExtensionLookup.getMultipleExtensions(eq(OrderDataPopulator.class),
				any(), any())).thenReturn(orderDataPopulators);
		when(shoppingCart.getStore().getCode()).thenReturn(STORE_CODE);
		checkoutAction.setXpfShoppingCartConverter(xpfShoppingCartConverter);
		when(xpfShoppingCartConverter.convert(shoppingCart)).thenReturn(xpfShoppingCart);

	}

	@Test
	public void testRollback() {
		// Given
		final Map<String, String> orderPopulatorOrderData = new HashMap<>();
		orderPopulatorOrderData.put("xpfProperty", "xpfData");
		when(orderDataPopulator1.collectOrderData(any())).thenReturn(orderPopulatorOrderData);

		// When
		checkoutAction.execute(checkoutContext);
		checkoutAction.rollback(checkoutContext);

		// Then
		Map<String, String> orderData = order.getModifierFields().getMap();
		assertEquals("Order Data should have been rolled back", Collections.emptyMap(), orderData);
	}

	@Test
	public void verifyExecuteWithOrderPopulatorPropertiesIntoTheOrderData() {
		// Given
		Map<String, String> expected = new HashMap<>();
		expected.put("xpfProperty", "xpfData");
		when(orderDataPopulator1.collectOrderData(any())).thenReturn(expected);

		// When
		checkoutAction.execute(checkoutContext);

		// Then
		assertEquals("Should have copied the values from the orderDataPopulator into the order data",
				expected, order.getModifierFields().getMap());
	}

	@Test
	public void verifyExecuteWithNoOrderDataPopulators() {
		// Given
		Map<String, String> expected = new HashMap<>();
		List<OrderDataPopulator> emptyOrderDataPopulators = new ArrayList<>();
		when(xpfExtensionLookup.getMultipleExtensions(eq(OrderDataPopulator.class),
				any(), any())).thenReturn(emptyOrderDataPopulators);

		// When
		checkoutAction.execute(checkoutContext);

		// Then
		assertEquals("Order data should be empty",
				expected, order.getModifierFields().getMap());
	}

	@Test
	public void testExecuteWithMinInputs() {
		// Given
		Map<String, String> expected = new HashMap<>();
		when(orderDataPopulator1.collectOrderData(any())).thenReturn(expected);

		// When
		checkoutAction.execute(checkoutContext);

		// Then
		assertEquals("Order data should be empty",
				expected, order.getModifierFields().getMap());
	}

	@Test
	public void verifyHigherPriorityPopulatorsShouldOverrideValuesSetByLowerPriorityPopulators() {
		// Given
		final Map<String, String> orderPopulatorOrderData1 = new HashMap<>();
		orderPopulatorOrderData1.put("xpfProperty1", "expectedData");

		when(orderDataPopulator1.collectOrderData(any())).thenReturn(orderPopulatorOrderData1);

		final Map<String, String> orderPopulatorOrderData2 = new HashMap<>();
		orderPopulatorOrderData2.put("xpfProperty1", "attemptedOverrrideValue");
		orderPopulatorOrderData2.put("xpfProperty2", "expectedData");
		orderPopulatorOrderData2.put("xpfProperty3", "expectedData");

		when(orderDataPopulator2.collectOrderData(any())).thenReturn(orderPopulatorOrderData2);

		// When
		checkoutAction.execute(checkoutContext);

		// Then
		Map<String, String> expected = new HashMap<>();
		expected.put("xpfProperty1", "expectedData");
		expected.put("xpfProperty2", "expectedData");
		expected.put("xpfProperty3", "expectedData");
		assertEquals("OrderDataPopulator with higher priority should not be overwritten",
				expected, order.getModifierFields().getMap());
	}

	@Test
	public void verifyOtherPopulatorsContinueIfAnExceptionIsThrownByAnEarlierExtension() {
		when(orderDataPopulator1.collectOrderData(any())).thenThrow(new RuntimeException());

		final Map<String, String> orderPopulatorOrderData2 = new HashMap<>();
		orderPopulatorOrderData2.put("xpfProperty1", "expectedData");
		orderPopulatorOrderData2.put("xpfProperty2", "expectedData");
		orderPopulatorOrderData2.put("xpfProperty3", "expectedData");

		when(orderDataPopulator2.collectOrderData(any())).thenReturn(orderPopulatorOrderData2);

		// When
		checkoutAction.execute(checkoutContext);

		// Then
		Map<String, String> expected = new HashMap<>();
		expected.put("xpfProperty1", "expectedData");
		expected.put("xpfProperty2", "expectedData");
		expected.put("xpfProperty3", "expectedData");
		assertEquals("OrderDataPopulator with higher priority should not be overwritten",
				expected, order.getModifierFields().getMap());
	}

}

/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.test.integration.extension;

import static com.elasticpath.xpf.extensions.HoldAllOrdersOrderHoldStrategyImpl.ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.impl.ShopperImpl;
import com.elasticpath.domain.shopper.impl.ShopperMementoImpl;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.money.Money;
import com.elasticpath.sellingchannel.ShoppingItemFactory;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;
import com.elasticpath.settings.SettingsService;
import com.elasticpath.settings.domain.SettingValue;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.ShoppingCartSimpleStoreScenario;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.impl.OrderHoldStrategyXPFBridgeImpl;
import com.elasticpath.xpf.extensions.HoldAllOrdersOrderHoldStrategyImpl;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFInMemoryExtensionResolverImpl;

@DirtiesDatabase
public class OrderHoldStrategyXPFBridgeImplTest extends BasicSpringContextTest {

	@Autowired
	private XPFInMemoryExtensionResolverImpl resolver;
	@Autowired
	private SettingsService settingsService;
	@Autowired
	private ShoppingItemFactory shoppingItemFactory;
	@Autowired
	private OrderHoldStrategyXPFBridgeImpl orderHoldStrategyXPFBridge;

	private Store store;
	private ShoppingCart shoppingCart;

	@Before
	public void setUp() {
		SettingValue settingValue = settingsService.getSettingValue("COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStore");
		settingValue.setBooleanValue(true);

		settingsService.updateSettingValue(settingValue);
		ShoppingCartSimpleStoreScenario scenario = getTac().useScenario(ShoppingCartSimpleStoreScenario.class);
		Product physicalProduct = scenario.getShippableProducts().get(0);
		store = scenario.getStore();
		shoppingCart = new ShoppingCartImpl();
		ShoppingItem physicalShoppingItem = shoppingItemFactory.createShoppingItem(physicalProduct.getDefaultSku(), createPrice(), 1, 1, null);


		shoppingCart.addShoppingCartItem(physicalShoppingItem);
	}

	@Test
	public void testEvaluateOrderHoldsWithDefaultExtensions() {
		PreCaptureCheckoutActionContext context = createContext();

		List<OrderHold> result = orderHoldStrategyXPFBridge.evaluateOrderHolds(context);

		assertEquals(1, result.size());
		assertEquals(OrderHoldStatus.ACTIVE, result.get(0).getStatus());
		assertEquals(ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING, result.get(0).getHoldDescription());
		assertEquals("RESOLVE_GENERIC_HOLD", result.get(0).getPermission());
	}

	@Test
	public void testGetPaginatedResultWithMinimalExtensions() {
		resolver.removeExtensionFromSelector(HoldAllOrdersOrderHoldStrategyImpl.class.getName(), null,
				XPFExtensionPointEnum.ORDER_HOLD_STRATEGY, new XPFExtensionSelectorAny());
		PreCaptureCheckoutActionContext context = createContext();

		List<OrderHold> result = orderHoldStrategyXPFBridge.evaluateOrderHolds(context);

		assertEquals(0, result.size());
	}

	public PreCaptureCheckoutActionContext createContext() {
		Customer customer = new CustomerImpl();
		customer.setGuid("guid");
		customer.setSharedId("sharedId");
		customer.setStatus(1);
		customer.setCustomerGroups(Collections.emptyList());

		CustomerSession customerSession = new CustomerSessionImpl();
		customerSession.setCurrency(Currency.getInstance("USD"));
		customerSession.setLocale(Locale.ENGLISH);

		Shopper shopper = new ShopperImpl();
		shopper.setShopperMemento(new ShopperMementoImpl());
		shopper.setStoreCode(store.getCode());
		shopper.setCustomerSession(customerSession);
		shopper.setCustomer(customer);
		shoppingCart.setShopper(shopper);
		return new PreCaptureCheckoutActionContextImpl(shoppingCart, null, null, false, false, null, null);
	}

	private Price createPrice() {
		Currency currency = TestDataPersisterFactory.DEFAULT_CURRENCY;
		Price price = getBeanFactory().getPrototypeBean(ContextIdNames.PRICE, Price.class);
		price.setCurrency(currency);
		price.setListPrice(Money.valueOf(BigDecimal.ONE, currency));

		return price;
	}
}

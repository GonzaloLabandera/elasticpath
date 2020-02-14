/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.orderpaymentapi;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cartorder.impl.CartOrderImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.impl.CartOrderPaymentInstrumentServiceImpl;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link CartOrderPaymentInstrumentServiceImpl}.
 */
public class CartOrderPaymentInstrumentServiceImplTest extends DbTestCase {

	private static String paymentInstrumentGuid;
	private static String cartOrderGuid;

	@Autowired
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Autowired
	private CartOrderService cartOrderService;

	@Autowired
	private ShopperService shopperService;

	@Autowired
	private ShoppingCartService shoppingCartService;

	@Before
	public void setUp() throws Exception {
		paymentInstrumentGuid = Utils.uniqueCode("PAYMENTINSTRUMENT");
		cartOrderGuid = Utils.uniqueCode("CARTORDER");
	}

	@Test
	@DirtiesDatabase
	public void ensureFindByGuidFindsCartOrderPaymentInstrument() {
        CartOrderPaymentInstrument entity = createTestCartOrderPaymentInstrument();
        CartOrder cartOrder = createAndSaveCartOrder();
        entity.setCartOrderUid(cartOrder.getUidPk());

        cartOrderPaymentInstrumentService.saveOrUpdate(entity);

        assertTrue("CartOrderPaymentInstrument was not attached to cart order",
                cartOrderPaymentInstrumentService.hasPaymentInstruments(cartOrder));
        CartOrderPaymentInstrument persistedInstrument = cartOrderPaymentInstrumentService.findByGuid(entity.getGuid());
        assertEquals("Wrong CartOrderPaymentInstrument found by GUID", entity, persistedInstrument);
    }

	@Test
	@DirtiesDatabase
	public void ensureFindByCartOrderFindsCartOrderPaymentInstruments() {
        CartOrderPaymentInstrument entity = createTestCartOrderPaymentInstrument();
        CartOrder cartOrder = createAndSaveCartOrder();
        entity.setCartOrderUid(cartOrder.getUidPk());

        cartOrderPaymentInstrumentService.saveOrUpdate(entity);

        Collection<CartOrderPaymentInstrument> instruments = cartOrderPaymentInstrumentService.findByCartOrder(cartOrder);
        Iterator<CartOrderPaymentInstrument> iterator = instruments.iterator();
        assertTrue("No CartOrderPaymentInstrument entities were found for this CartOrder", iterator.hasNext());
        assertEquals("Wrong CartOrderPaymentInstrument associated with the CartOrder", entity.getUidPk(), iterator.next().getUidPk());
    }

	@Test
	@DirtiesDatabase
	public void removingCartOrderPaymentInstrumentDoesNotRemoveCartOrder() {
        CartOrder cartOrder = createAndSaveCartOrder();

        CartOrderPaymentInstrument paymentInstrument = createTestCartOrderPaymentInstrument();
        paymentInstrument.setCartOrderUid(cartOrder.getUidPk());

        cartOrderPaymentInstrumentService.saveOrUpdate(paymentInstrument);
        cartOrderPaymentInstrumentService.remove(paymentInstrument);

        final CartOrder persistedCartOrder = cartOrderService.findByStoreCodeAndGuid(scenario.getStore().getCode(), cartOrder.getGuid());
        assertNotNull("Cart Order was unexpectedly removed", persistedCartOrder);
        assertFalse("CartOrderPaymentInstrument was not removed", cartOrderPaymentInstrumentService.hasPaymentInstruments(cartOrder));
    }

	private CartOrderPaymentInstrument createTestCartOrderPaymentInstrument() {
		CartOrderPaymentInstrument cartOrderPaymentInstrument =
				getBeanFactory().getPrototypeBean(CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class);
		cartOrderPaymentInstrument.setPaymentInstrumentGuid(paymentInstrumentGuid);
		cartOrderPaymentInstrument.setLimitAmount(BigDecimal.ONE);
		cartOrderPaymentInstrument.setCurrency(Currency.getInstance("USD"));
		return cartOrderPaymentInstrument;
	}

	private ShoppingCart configureAndPersistCart() {
		Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
		Store store = scenario.getStore();
		Customer customer = persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
		shopper.setCustomer(customer);
		shopper.setStoreCode(store.getCode());
		shopper = shopperService.save(shopper);

		CustomerSession customerSession = TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(shopper);
		customerSession.setCurrency(Currency.getInstance("USD"));

		ShoppingCartImpl shoppingCart = new ShoppingCartImpl();
		shoppingCart.setShopper(shopper);
		shoppingCart.setStore(store);
		shoppingCart.getShoppingCartMemento().setGuid(Utils.uniqueCode("CART"));
		shoppingCart.setCustomerSession(customerSession);

		shopper.setCurrentShoppingCart(shoppingCart);

		return shoppingCartService.saveOrUpdate(shoppingCart);
	}

	private CartOrder createAndSaveCartOrder() {
		ShoppingCart shoppingCart = configureAndPersistCart();

		CartOrder cartOrder = new CartOrderImpl();
		cartOrder.setGuid(cartOrderGuid);
		cartOrder.setShoppingCartGuid(shoppingCart.getGuid());

		return cartOrderService.saveOrUpdate(cartOrder);
	}

}

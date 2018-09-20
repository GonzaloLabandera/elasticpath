/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
/**
 *
 */
package com.elasticpath.service.shoppingcart.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.catalog.AvailabilityException;
import com.elasticpath.domain.catalog.InsufficientInventoryException;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.AllocationService;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.impl.StockCheckerCheckoutAction;

/**
 * Tests for the {@code CheckoutServiceImpl} class.
 */
public class CheckoutServiceImplTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	@Mock private ProductSkuLookup productSkuLookup;

	/**
	 * Test that when checkout fails due to an exception thrown during processing of an action,
	 * all previous actions are rolled back.
	 */
	@Test
	public void testCheckoutRollbackMechanism() {
		final Shopper shopper = context.mock(Shopper.class);
		final ShoppingCart shoppingCart = context.mock(ShoppingCart.class);
		final ShoppingCartTaxSnapshot pricingSnapshot = context.mock(ShoppingCartTaxSnapshot.class);
		final ReversibleCheckoutAction action1 = context.mock(ReversibleCheckoutAction.class, "action1");
		final ReversibleCheckoutAction action2 = context.mock(ReversibleCheckoutAction.class, "action2");
		final CustomerSession customerSession = context.mock(CustomerSession.class);

		final CheckoutResults checkoutResults = context.mock(CheckoutResults.class);
		context.checking(new Expectations() { {
			oneOf(action1).execute(with(aNonNull(CheckoutActionContext.class)));
			oneOf(action2).execute(with(aNonNull(CheckoutActionContext.class)));
			will(throwException(new EpSystemException("testing exception handling.")));
			allowing(shopper).getCurrentShoppingCart(); will(returnValue(shoppingCart));
			oneOf(action2).rollback(with(aNonNull(CheckoutActionContext.class)));
			oneOf(action1).rollback(with(aNonNull(CheckoutActionContext.class)));
			allowing(shopper).getUidPk(); will(returnValue(1L));
			oneOf(checkoutResults).setOrder(null);
		} });

		CheckoutServiceImpl service = new CheckoutServiceImpl();
		List<ReversibleCheckoutAction> actionList = new ArrayList<>();
		actionList.add(action1);
		actionList.add(action2);
		service.setReversibleActionList(actionList);

		try {
			service.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, null, true, false, null, checkoutResults);
		} catch (EpSystemException ex) {
			assertNotNull("The EpSystemException should be rethrown after the payments have been rolled back.", ex);
		}
		//The check is performed by expecting the payment service rollBackPayments() method call on the mock object
	}

	/**
	 * TODO: Test that if a payment service throws an exception during the processing of OrderPayments,
	 * all changes to the Order will be rolled back (including any payments already made), and then then EpSystemException
	 * will be re-thrown. The ShoppingCart will still have all of its items.
	 * This test should simply ensure that the PaymentService.initializePayments method throws an exception
	 * and then check that it's handled property.
	 */


	/**
	 * Test that when there is insufficient inventory, checkout will fail with an InsufficientInventoryException
	 * and the shopping cart will not be emptied.
	 */
	@Test(expected = InsufficientInventoryException.class)
	public void testInsufficientInventoryOnCheckout() {
		//If the ShoppingCart were cleared this would fail because the mock shopping cart is not expecting it.

		final Shopper shopper = context.mock(Shopper.class);
		final ShoppingCart shoppingCart = context.mock(ShoppingCart.class);
		final ShoppingCartTaxSnapshot pricingSnapshot = context.mock(ShoppingCartTaxSnapshot.class);
		final Store store = context.mock(Store.class);
		final Warehouse warehouse = context.mock(Warehouse.class);
		final ShoppingItem shoppingItem = context.mock(ShoppingItem.class);
		final ProductSku productSku = createProductSku("skuCode");
		final Locale locale = Locale.CANADA;
		final OrderPayment orderPayment = context.mock(OrderPayment.class);
		final OrderReturn orderReturn = context.mock(OrderReturn.class);
		final CheckoutResults checkoutResults = context.mock(CheckoutResults.class);
		final CustomerSession customerSession = context.mock(CustomerSession.class);
		final List<ShoppingItem> items = new ArrayList<>();
		items.add(shoppingItem);
		final AllocationService allocationService = context.mock(AllocationService.class);
		context.checking(new Expectations() { {
			allowing(store).getWarehouse(); will(returnValue(warehouse));
			allowing(shoppingCart).isExchangeOrderShoppingCart(); will(returnValue(false));
			allowing(shoppingCart).getNumItems(); will(returnValue(1));
			allowing(shoppingCart).getStore(); will(returnValue(store));
			allowing(shoppingCart).getLeafShoppingItems(); will(returnValue(items));
			allowing(shoppingCart).getShopper(); will(returnValue(shopper));
			//Constructing the exception requires the following
			allowing(shoppingItem).getSkuGuid(); will(returnValue(productSku.getGuid()));
			allowing(warehouse).getCode(); will(returnValue("warehouseCode"));
			allowing(warehouse).getUidPk(); will(returnValue(1L));
			allowing(shoppingItem).getQuantity(); will(returnValue(1));
			allowing(shopper).getUidPk(); will(returnValue(1L));
			allowing(allocationService).hasSufficientUnallocatedQty(productSku,
					1L, 1); will(returnValue(false));
			allowing(customerSession).getLocale();
			will(returnValue(locale));
		} });

		StockCheckerCheckoutAction stockCheckerAction = new StockCheckerCheckoutAction() {
			@Override
			protected void setCartItemErrorMessage(final ShoppingItem cartItem, final ErrorMessage errorMessage, final Locale locale) {
				//do nothing
			}
		};
		stockCheckerAction.setAllocationService(allocationService);
		stockCheckerAction.setProductSkuLookup(productSkuLookup);

		CheckoutServiceImpl service = new CheckoutServiceImpl();
		service.setSetupActionList(Collections.<CheckoutAction>singletonList(stockCheckerAction));

		service.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, orderPayment, false, false, orderReturn, checkoutResults);
	}

	private ProductSku createProductSku(final String skuCode) {
		final ProductImpl product = new ProductImpl() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getDisplayName(final Locale locale, final boolean fallback) {
				return "I'm a product, yes it's true";
			}
		};
		product.initialize();


		final ProductSku sku = new ProductSkuImpl();
		sku.initialize();
		sku.setSkuCode(skuCode);
		sku.setProduct(product);

		context.checking(new Expectations() { {
			allowing(productSkuLookup).findByGuid(sku.getGuid()); will(returnValue(sku));
		} });

		return sku;
	}

	/**
	 * Test that when a product sku is unavailable (date range falls outside the current date),
	 * checkout will fail with an AvailabilityException and the shopping cart will not be emptied.
	 */
	@Test(expected = AvailabilityException.class)
	public void testUnavailableOnCheckout() {
		//If the ShoppingCart were cleared this would fail because the mock shopping cart is not expecting it.

		final Shopper shopper = context.mock(Shopper.class);
		final ShoppingCart shoppingCart = context.mock(ShoppingCart.class);
		final ShoppingCartTaxSnapshot pricingSnapshot = context.mock(ShoppingCartTaxSnapshot.class);
		final Store store = context.mock(Store.class);
		final Warehouse warehouse = context.mock(Warehouse.class);
		final ShoppingItem shoppingItem = context.mock(ShoppingItem.class);
		final ProductSku productSku = createProductSku("skuCode");
		final long someFutureTime = 100000000L;
		productSku.setStartDate(new Date(System.currentTimeMillis() + someFutureTime));
		final Locale locale = Locale.CANADA;
		final OrderPayment orderPayment = context.mock(OrderPayment.class);
		final OrderReturn orderReturn = context.mock(OrderReturn.class);
		final CheckoutResults checkoutResults = context.mock(CheckoutResults.class);
		final CustomerSession customerSession = context.mock(CustomerSession.class);
		final List<ShoppingItem> items = new ArrayList<>();
		items.add(shoppingItem);

		context.checking(new Expectations() { {
			allowing(shoppingCart).isExchangeOrderShoppingCart(); will(returnValue(false));
			allowing(shoppingCart).getNumItems(); will(returnValue(1));
			allowing(shoppingCart).getStore(); will(returnValue(store));
			allowing(store).getWarehouse(); will(returnValue(warehouse));
			allowing(shoppingCart).getLeafShoppingItems(); will(returnValue(items));
			allowing(shoppingCart).getShopper(); will(returnValue(shopper));
			allowing(shopper).getUidPk(); will(returnValue(1L));
			allowing(customerSession).getLocale(); will(returnValue(locale));
			allowing(shoppingItem).getSkuGuid(); will(returnValue(productSku.getGuid()));
		} });

		StockCheckerCheckoutAction stockCheckerAction = new StockCheckerCheckoutAction() {
			@Override
			protected void verifyInventory(final ShoppingItem cartItem, final ProductSku itemSku, final Warehouse warehouse, final Locale locale) {
				//do nothing - inventory is fine
			}
			@Override
			protected void setCartItemErrorMessage(final ShoppingItem cartItem, final ErrorMessage errorMessage, final Locale locale) {
				//do nothing
			}
		};
		stockCheckerAction.setProductSkuLookup(productSkuLookup);

		CheckoutServiceImpl service = new CheckoutServiceImpl();
		service.setSetupActionList(Collections.<CheckoutAction>singletonList(stockCheckerAction));

		service.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, orderPayment, false, false, orderReturn, checkoutResults);
	}

	@Test
	public void verifyCheckoutActionContextPopulatedWithAllExpectedParameters() throws Exception {
		final ShoppingCart cart = context.mock(ShoppingCart.class);
		final CustomerSession customerSession = context.mock(CustomerSession.class);
		final OrderPayment templateOrderPayment = context.mock(OrderPayment.class);
		final boolean isOrderExchange = true;
		final boolean awaitExchangeCompletion = true;
		final OrderReturn exchange = context.mock(OrderReturn.class);

		final CheckoutActionContext actionContext = new CheckoutServiceImpl().createActionContext(
				cart,
				null, customerSession,
				templateOrderPayment,
				isOrderExchange,
				awaitExchangeCompletion,
				exchange);
	
		assertEquals("Unexpected shopping cart", cart, actionContext.getShoppingCart());
		assertEquals("Unexpected customer session", customerSession, actionContext.getCustomerSession());
		assertEquals("Unexpected templateOrderPayment", templateOrderPayment, actionContext.getOrderPaymentTemplate());
		assertEquals("Unexpected isOrderExchange", isOrderExchange, actionContext.isOrderExchange());
		assertEquals("Unexpected awaitExchangeCompletion", awaitExchangeCompletion, actionContext.isAwaitExchangeCompletion());
		assertEquals("Unexpected exchange", exchange, actionContext.getExchange());
	}

}

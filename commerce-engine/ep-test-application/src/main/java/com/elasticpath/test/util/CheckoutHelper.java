/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.shipping.Region;
import com.elasticpath.domain.shipping.ShippingRegion;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.service.shipping.ShippingServiceLevelService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.TestApplicationContext;

/**
 * A helper to with all util methods for checkout.
 */
public class CheckoutHelper {

	private final CheckoutService checkoutService;

	private final ShippingServiceLevelService shippingLevelService;

	private final PricingSnapshotService pricingSnapshotService;

	private final TaxSnapshotService taxSnapshotService;

	private final StoreTestPersister storeTestPersister;

	private TestApplicationContext tac;

	/**
	 * Constructor.
	 * 
	 * @param tac {@link TestApplicationContext}.
	 */
	public CheckoutHelper(final TestApplicationContext tac) {
		this.tac = tac;
		checkoutService = tac.getBeanFactory().getBean(ContextIdNames.CHECKOUT_SERVICE);
		shippingLevelService = tac.getBeanFactory().getBean(ContextIdNames.SHIPPING_SERVICE_LEVEL_SERVICE);
		pricingSnapshotService = tac.getBeanFactory().getBean(ContextIdNames.PRICING_SNAPSHOT_SERVICE);
		taxSnapshotService = tac.getBeanFactory().getBean(ContextIdNames.TAX_SNAPSHOT_SERVICE);
		storeTestPersister = tac.getPersistersFactory().getStoreTestPersister();
	}

	/**
	 * Enrich a shopping cart with default payment info and shipping info.
	 * @param shoppingCart {@link ShoppingCart}.
	 */
	public void enrichShoppingCartForCheckout(final ShoppingCart shoppingCart) {
		shoppingCart.setShippingServiceLevelList(defaultShippingLevels(shoppingCart.getStore()));
		final long defaultShippingLevel = getDefaultShippingLevelUidFromCart(shoppingCart);
		shoppingCart.setSelectedShippingServiceLevelUid(defaultShippingLevel);
		shoppingCart.setShippingAddress(createCustomerAddressMatchingShippingServiceLevel(getDefaultShippingLevelFromCart(shoppingCart)));
	}

	/**
	 * Checkout a shopping cart with default payment info and shipping info.
	 * @param shoppingCart {@link com.elasticpath.domain.shoppingcart.ShoppingCart}.
	 * @param customerSession {@link com.elasticpath.domain.customer.CustomerSession}
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfo(final ShoppingCart shoppingCart, final CustomerSession customerSession) {
		enrichShoppingCartForCheckout(shoppingCart);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		return checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, defaultOrderPayment(), false);
	}

	/**
	 * Checkout a shopping cart with default payment info and shipping info.
	 * @param customerSession {@link CustomerSession}.
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfo(final CustomerSession customerSession) {
		return checkoutWithDefaultInfo(customerSession.getShopper().getCurrentShoppingCart(), customerSession);
	}

	/**
	 * Checkout a shopping cart with default payment info but override selected shipping level
	 * @param customerSession {@link CustomerSession}.
	 * @param shippingServiceLevel service level specified
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfoOverrideShipping(final CustomerSession customerSession, final String shippingServiceLevel) {
		final ShoppingCart shoppingCart = customerSession.getShopper().getCurrentShoppingCart();
		List<ShippingServiceLevel> defaultShippingLevels = defaultShippingLevels(shoppingCart.getStore());
		boolean found = false;
		for(ShippingServiceLevel defaultShippingLevel : defaultShippingLevels) {
			if (shippingServiceLevel.equals(defaultShippingLevel.getDisplayName(Locale.getDefault(), true))) {
				shoppingCart.setShippingServiceLevelList(defaultShippingLevels);
				shoppingCart.setSelectedShippingServiceLevelUid(defaultShippingLevel.getUidPk());
				shoppingCart.setShippingAddress(createCustomerAddressMatchingShippingServiceLevel(getDefaultShippingLevelFromCart(shoppingCart)));
				found = true;
				break;
			}
		}
		if (!found) {
			throw new EpSystemException("Chosen ShippingLevel not available in for store.");
		}

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);


		return checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, defaultOrderPayment(), false);
	}

	private OrderPayment defaultOrderPayment() {
		OrderPayment defaultOrderPayment = tac.getBeanFactory().getBean(ContextIdNames.ORDER_PAYMENT);
		defaultOrderPayment.setCardType("Visa");
		defaultOrderPayment.setCardHolderName("Owen");
		defaultOrderPayment.setUnencryptedCardNumber("4111111111111111");
		defaultOrderPayment.setCvv2Code("000");
		defaultOrderPayment.setExpiryMonth("01");
		defaultOrderPayment.setExpiryYear("21");
		defaultOrderPayment.setPaymentMethod(PaymentType.CREDITCARD);
		return defaultOrderPayment;
	}

	private List<ShippingServiceLevel> defaultShippingLevels(final Store selectedStore) {
		return new ArrayList<>(shippingLevelService.findByStore(selectedStore.getCode()));
	}

	private long getDefaultShippingLevelUidFromCart(final ShoppingCart storeShoppingCart) {
		return storeShoppingCart.getShippingServiceLevelList().iterator().next().getUidPk();
	}

	private ShippingServiceLevel getDefaultShippingLevelFromCart(final ShoppingCart storeShoppingCart) {
		return storeShoppingCart.getShippingServiceLevelList().iterator().next();
	}
	
	private CustomerAddress defaultCustomerAddress() {
		return storeTestPersister.createCustomerAddress("Ou", "Owen", "street1", "street2", "city", "country", "state", "zip", "phone");
	}
	
	private CustomerAddress createCustomerAddressMatchingShippingServiceLevel(final ShippingServiceLevel shippingServiceLevel) {
		final ShippingRegion shippingRegion = shippingServiceLevel.getShippingRegion();
		final Map<String, Region> regionMap = shippingRegion.getRegionMap();
		final Collection<Region> regions = regionMap.values();
		final Region firstRegion = regions.iterator().next();
		final String countryCodeFromFirstRegion = firstRegion.getCountryCode();
		final String firstSubCountryFromFirstRegion = firstRegion.getSubCountryCodeList().get(0);
		final CustomerAddress customerAddressMatchingShippingServiceLevel = defaultCustomerAddress();
		customerAddressMatchingShippingServiceLevel.setCountry(countryCodeFromFirstRegion);
		customerAddressMatchingShippingServiceLevel.setSubCountry(firstSubCountryFromFirstRegion);
		return customerAddressMatchingShippingServiceLevel;
	}

}

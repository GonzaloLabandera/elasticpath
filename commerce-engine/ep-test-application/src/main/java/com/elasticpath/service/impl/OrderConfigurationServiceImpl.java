/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.impl;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.testcontext.ShoppingTestData;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.CustomerAuthenticationService;
import com.elasticpath.service.CustomerOrderingService;
import com.elasticpath.service.OrderConfigurationService;
import com.elasticpath.service.TestAddressService;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Service to help configure the order creation for <code>OrderFitFixture</code>.
 */
public class OrderConfigurationServiceImpl implements OrderConfigurationService {

	private CustomerAuthenticationService customerAuthenticationService;

	private ShopperService shopperService;

	private CustomerOrderingService customerOrderingService;

	private CartDirector cartDirector;

	private ShippingOptionService shippingOptionService;

	private TestAddressService addressService;

	/**
	 * Creates the shopping cart for given customer with given sku.
	 *
	 * @param store    the store to create the shopping cart in. May be different than the customer store.
	 * @param customer the customer
	 * @param skuMap   the sku to add to shopping cart
	 * @return new shopping cart
	 */
	@Override
	public ShoppingCart createShoppingCart(final Store store, final Customer customer, final Map<ProductSku, Integer> skuMap) {
		final ShoppingTestData shoppingTestData = ShoppingTestData.getInstance();
		customerAuthenticationService.loginStore(store, customer.getUsername());
		ShoppingCart shoppingCart = shoppingTestData.getShopper().getCurrentShoppingCart();
		for (Entry<ProductSku, Integer> entry : skuMap.entrySet()) {
			cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(entry.getKey().getSkuCode(), entry.getValue()));
		}
		return shoppingCart;
	}

	/**
	 * Selects the customer billing and shipping addresses for given shopping cart.
	 *
	 * @param shopper               the shopper
	 * @param streetShippingAddress the street of shipping address
	 * @param streetBillingAddress  the street of billing address
	 */
	@Override
	public void selectCustomerAddressesToShoppingCart(final Shopper shopper, final String streetShippingAddress,
			final String streetBillingAddress) {

		ShoppingCart shoppingCartToEdit = shopper.getCurrentShoppingCart();
		selectCustomerAddressByStreet(shopper.getCustomer(), shoppingCartToEdit, streetShippingAddress,
				(shoppingCart, customerAddress) -> {
					if (shoppingCart.requiresShipping()) {
						customerOrderingService.selectShippingAddress(shopper, customerAddress);
					}
				});

		selectCustomerAddressByStreet(shopper.getCustomer(), shoppingCartToEdit, streetBillingAddress,
				(shoppingCart, customerAddress) -> customerOrderingService.selectBillingAddress(shopper, customerAddress));

	}

	private void selectCustomerAddressByStreet(final Customer customer, final ShoppingCart shoppingCart, final String streetAddress,
			final CustomerAddressSelector customerAddressSelector) {
		if (StringUtils.isEmpty(streetAddress)) {
			return;
		}

		CustomerAddress customerAddressByStreet = addressService.findByCustomerAndStreet1(customer.getUidPk(), streetAddress);
		if (customerAddressByStreet == null) {
			throw new EpServiceException("Selected address could not be found");
		}

		customerAddressSelector.selectCustomerAddress(shoppingCart, customerAddressByStreet);
	}

	@Override
	public ShoppingCart selectShippingOption(final ShoppingCart shoppingCart, final String shippingOptionName) {
		if (StringUtils.isEmpty(shippingOptionName)) {
			return shoppingCart;
		}

		final ShippingOptionResult shippingOptionResult = getShippingOptionService().getShippingOptions(shoppingCart);
		final String errorMessage = format("Unable to get available shipping options for the given cart with guid '%s'. So cannot select one.",
				shoppingCart.getGuid());
		shippingOptionResult.throwExceptionIfUnsuccessful(
				errorMessage,
				singletonList(
						new StructuredErrorMessage(
								"shippingoptions.unavailable",
								errorMessage,
								ImmutableMap.of(
										"cart-id", shoppingCart.getGuid())
						)
				));
		final Locale locale = shoppingCart.getShopper().getLocale();
		for (ShippingOption shippingOption : shippingOptionResult.getAvailableShippingOptions()) {
			if (StringUtils.equals(shippingOptionName, shippingOption.getDisplayName(locale).orElse(null))) {
				return customerOrderingService.selectShippingOption(shoppingCart, shippingOption);
			}
		}

		throw new EpServiceException("Selected shipping option could not be found");
	}

	/**
	 * This interface represents methods for selection customer address for given shopping cart.
	 */
	private interface CustomerAddressSelector {

		/**
		 * Selects the customer address for given shopping cart.
		 *
		 * @param shoppingCart    the shopping cart
		 * @param customerAddress the customer address to select
		 */
		void selectCustomerAddress(ShoppingCart shoppingCart, CustomerAddress customerAddress);
	}

	public void setCustomerAuthenticationService(final CustomerAuthenticationService customerAuthenticationService) {
		this.customerAuthenticationService = customerAuthenticationService;
	}

	public void setCustomerOrderingService(final CustomerOrderingService customerOrderingService) {
		this.customerOrderingService = customerOrderingService;
	}

	public void setCartDirector(final CartDirector cartDirector) {
		this.cartDirector = cartDirector;
	}

	@Override
	public void setShopperService(ShopperService shopperService) {
		this.shopperService = shopperService;
	}

	@Override
	public ShopperService getShopperService() {
		return shopperService;
	}

	protected ShippingOptionService getShippingOptionService() {
		return this.shippingOptionService;
	}

	public void setShippingOptionService(final ShippingOptionService shippingOptionService) {
		this.shippingOptionService = shippingOptionService;
	}

	public void setAddressService(final TestAddressService addressService) {
		this.addressService = addressService;
	}
}

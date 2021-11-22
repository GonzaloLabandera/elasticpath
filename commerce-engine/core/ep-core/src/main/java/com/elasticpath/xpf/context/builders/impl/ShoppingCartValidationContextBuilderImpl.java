/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.context.builders.ShoppingCartValidationContextBuilder;
import com.elasticpath.xpf.converters.ShippingOptionConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;

/**
 * Implementation of {@code com.elasticpath.xpf.context.builders.ShoppingCartValidationContextBuilder}.
 */
public class ShoppingCartValidationContextBuilderImpl implements ShoppingCartValidationContextBuilder {

	private static final Logger LOG = LogManager.getLogger(ShoppingCartValidationContextBuilderImpl.class);

	private ShoppingCartConverter shoppingCartConverter;
	private PricingSnapshotService pricingSnapshotService;
	private ShippingOptionService shippingOptionService;
	private ShippingOptionConverter xpfShippingOptionConverter;

	@Override
	public XPFShoppingCartValidationContext build(final ShoppingCart shoppingCart) {
		Objects.requireNonNull(shoppingCart,
				"Required field shoppingCart for building XPFShoppingCartValidationContext is missing.");

		XPFShoppingCart xpfShoppingCart = shoppingCartConverter.convert(shoppingCart);

		boolean isPaymentRequired = false;
		Set<XPFShippingOption> availableShippingOptions = new HashSet<>();

		if (shoppingCart.getShopper().getCustomerSession() != null) {
			ShoppingCartPricingSnapshot pricingSnapshot;
			try {
				pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
				isPaymentRequired = pricingSnapshot.getSubtotal().signum() > 0 || pricingSnapshot.getShippingCost().getAmount().signum() > 0;
			} catch (EpSystemException e) {
				LOG.warn("Error while retrieving pricing snapshot for cart", e);
			}

			final ShippingOptionResult shippingOptionsResult = shippingOptionService.getShippingOptions(shoppingCart);
			if (shippingOptionsResult.isSuccessful()) {
				shippingOptionsResult.getAvailableShippingOptions().forEach(shippingOption ->
						availableShippingOptions.add(xpfShippingOptionConverter.convert(shippingOption))
				);
			}
		}


		return new XPFShoppingCartValidationContext(xpfShoppingCart, isPaymentRequired, availableShippingOptions);
	}

	public void setShoppingCartConverter(final ShoppingCartConverter shoppingCartConverter) {
		this.shoppingCartConverter = shoppingCartConverter;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

	public void setShippingOptionService(final ShippingOptionService shippingOptionService) {
		this.shippingOptionService = shippingOptionService;
	}

	public void setXpfShippingOptionConverter(final ShippingOptionConverter xpfShippingOptionConverter) {
		this.xpfShippingOptionConverter = xpfShippingOptionConverter;
	}
}

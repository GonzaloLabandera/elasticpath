/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview.impl;

import java.lang.reflect.Proxy;
import java.util.Objects;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.PerStoreProductSkuAvailability;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.domain.catalogview.StoreProductSkuFactory;

/**
 * Creates {@link StoreProductSku} instances.
 */
public class StoreProductSkuFactoryImpl implements StoreProductSkuFactory {

	@Override
	public StoreProductSku createStoreProductSku(final ProductSku productSku, final PerStoreProductSkuAvailability availability) {
		return (StoreProductSku) Proxy.newProxyInstance(StoreProductSkuFactoryImpl.class.getClassLoader(),
				new Class<?>[]{StoreProductSku.class},
				(proxy, method, args) -> {
					if ("equals".equals(method.getName()) && (args != null && args.length == 1)) {
						return isEqual(productSku, availability, args[0]);
					} else if ("hashCode".equals(method.getName()) && (args == null || args.length == 0)) {
						return hashCode(productSku, availability);
					} else if (method.getDeclaringClass().isAssignableFrom(PerStoreProductSkuAvailability.class)) {
						return method.invoke(availability, args);
					} else {
						return method.invoke(productSku, args);
					}
				});
	}

	/**
	 * Determines the equality of a given {@code other} object to an equivalent {@link StoreProductSku} consisting of the provided
	 * {@link ProductSku} and {@link PerStoreProductSkuAvailability}.
	 *
	 * @param productSku   the product SKU constituent of a {@link StoreProductSku}
	 * @param availability the availability constituent of a {@link StoreProductSku}
	 * @param other        the object to test equality
	 * @return true if when equal
	 */
	public static boolean isEqual(final ProductSku productSku, final PerStoreProductSkuAvailability availability, final Object other) {
		if (!(other instanceof StoreProductSku)) {
			return false;
		}

		return productSku.getSkuCode().equals(((StoreProductSku) other).getSkuCode())
				&& availability.equals(other);
	}

	/**
	 * Generates a hash code for the proxied implementation of {@link StoreProductSku}, derived from the {@link ProductSku} and
	 * {@link PerStoreProductSkuAvailability} constituents.
	 *
	 * @param productSku the product SKU constituent of a {@link StoreProductSku}
	 * @param availability the availability constituent of a {@link StoreProductSku}
	 * @return a hash code
	 */
	public int hashCode(final ProductSku productSku, final PerStoreProductSkuAvailability availability) {
		return Objects.hash(productSku, availability);
	}

}

/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.catalog;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;

/**
 * Helper interface for identifying bundles and calculated bundles.
 */
public interface BundleIdentifier {

	/**
	 * @param product the product to be checked
	 * @return <code>true</code> iff the product is a bundle
	 */
	boolean isBundle(Product product);

	/**
	 * @param product the product to be checked
	 * @return <code>true</code> iff the product is a calculated bundle
	 */
	boolean isCalculatedBundle(Product product);

	/**
	 * @param productSku the sku to be checked
	 * @return <code>true</code> iff the sku is a calculated bundle
	 */
	boolean isCalculatedBundle(ProductSku productSku);

	/**
	 * @param product the product to be casted
	 * @return a ProductBundle
	 * @throws ClassCastException if the product is not a bundle
	 */
	ProductBundle asProductBundle(Product product);
}

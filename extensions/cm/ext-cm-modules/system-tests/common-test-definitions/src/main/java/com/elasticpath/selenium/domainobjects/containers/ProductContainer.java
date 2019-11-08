/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.containers;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.selenium.domainobjects.Product;

/**
 * Product container class.
 */
public class ProductContainer {
	private final List<Product> products = new ArrayList<>();

	public List<Product> getProducts() {
		return products;
	}

	public void addProducts(final Product product) {
		if (product == null) {
			return;
		}
		products.add(product);
	}

	public String getProductCodeByPartialCode(final String code) {
		return getProducts()
				.stream()
				.filter(product -> product.getProductCode().startsWith(code))
				.map(Product::getProductCode)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get Product by partial Name.
	 *
	 * @param partialName partial Name of the product.
	 * @return Product.
	 */
	public Product getProductByPartialName(final String partialName) {
		return products
				.stream()
				.filter(product -> product.getProductName().startsWith(partialName))
				.findFirst()
				.orElse(null);
	}
}

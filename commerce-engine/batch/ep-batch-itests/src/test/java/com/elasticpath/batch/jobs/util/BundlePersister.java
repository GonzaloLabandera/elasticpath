/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.util;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Util class for persisting bundles.
 */
public class BundlePersister {

	private ProductSku bundleProductSku;
	private ProductSku mainSimpleConstituentSku;
	private ProductSku level2ConstituentSKU;
	private ProductSku level2BundleSKU;

	private TaxCode taxCode;
	private SimpleStoreScenario testScenario;
	private TestDataPersisterFactory persisterFactory;

	/**
	 * Custom constructor.
	 *
	 * @param persisterFactory the persistence factory.
	 * @param testScenario the test scenario.
	 */
	public BundlePersister(final TestDataPersisterFactory persisterFactory, final SimpleStoreScenario testScenario) {
		this.persisterFactory = persisterFactory;
		this.testScenario = testScenario;

		taxCode = persisterFactory.getTaxTestPersister().persistTaxCode("CA TAX");
	}

	/**
	 * Persists a bundle.
	 */
	public void persistBundle() {
		mainSimpleConstituentSku = persistSimpleProduct("MainSimpleBundleConstituent");
		level2ConstituentSKU = persistSimpleProduct("Level2BundleConstituent");
		level2BundleSKU = persistBundleWithSKU("Level2Bundle", level2ConstituentSKU.getProduct().getCode());
		bundleProductSku = persistBundleWithSKU("MainBundle", mainSimpleConstituentSku.getProduct().getCode(),
				level2BundleSKU.getProduct().getCode());
	}

	/**
	 * Persists simple product.
	 *
	 * @param productCode the product code.
	 * @return persisted product SKU.
	 */
	public ProductSku persistSimpleProduct(final String productCode) {
		Product product = persisterFactory.getCatalogTestPersister().persistSimpleProduct(productCode, "Jobs",
				testScenario.getCatalog(), testScenario.getCategory(), taxCode);

		return persisterFactory.getCatalogTestPersister().persistSimpleProductSku(productCode + "SKU", 1.0,
				"CAD",  true, product, testScenario.getWarehouse());
	}

	private ProductSku persistBundleWithSKU(final String bundleCode, final String... productCodes) {
		Product bundle = persisterFactory.getCatalogTestPersister().persistProductBundle(testScenario.getCatalog(), testScenario.getCategory(),
				bundleCode, productCodes);
		return persisterFactory.getCatalogTestPersister().persistSimpleProductSku(bundleCode + "SKU", 1.0, "CAD",
				true, bundle, testScenario.getWarehouse());
	}

	public ProductSku getBundleProductSku() {
		return bundleProductSku;
	}

	public ProductSku getMainSimpleConstituentSku() {
		return mainSimpleConstituentSku;
	}

	public ProductSku getLevel2ConstituentSKU() {
		return level2ConstituentSKU;
	}

	public ProductSku getLevel2BundleSKU() {
		return level2BundleSKU;
	}

	public TaxCode getTaxCode() {
		return taxCode;
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Product Steps.
 */
public class ProductSteps {

	@Autowired
	private TestApplicationContext tac;

	@Autowired
	private ProductLookup productLookup;

	private SimpleStoreScenario scenario;

	/**
	 * Initialize the simple store scenario before the test.
	 */
	@Before(order = 0)
	public void initializeScenario() {
		scenario = tac.useScenario(SimpleStoreScenario.class);
	}

	/**
	 * Creates a product.
	 *
	 * @param productCode the product code
	 */
	@Given("^I create a product (\\w+)$")
	public void createProduct(final String productCode) {
		tac.getPersistersFactory().getCatalogTestPersister().persistProductWithSku(
				scenario.getCatalog(),
				scenario.getCategory(),
				scenario.getWarehouse(),
				BigDecimal.ONE,
				TestDataPersisterFactory.DEFAULT_CURRENCY,
				null,
				productCode,
				productCode + " product",
				productCode + "_sku",
				TaxTestPersister.TAX_CODE_GOODS,
				AvailabilityCriteria.ALWAYS_AVAILABLE,
				0);
	}

	/**
	 * Creates a product bundle and adds a single constituent.
	 *
	 * @param productBundleCode      the product bundle code
	 * @param constituentProductCode the constituent product code
	 */
	@Given("^I create a product bundle (\\w+) with constituent (\\w+)$")
	public void createProductBundle(final String productBundleCode, final String constituentProductCode) {
		tac.getPersistersFactory().getCatalogTestPersister().persistProductBundle(scenario.getCatalog(),
				scenario.getCategory(),
				productBundleCode,
				constituentProductCode);
	}

	/**
	 * Verifies the product's visibility has an expected value.
	 *
	 * @param productCode  the product code
	 * @param storeVisible the store visible flag
	 */
	@Given("^the product (\\w+) visibility is set to (\\w+)$")
	public void verifyProductVisibility(final String productCode, final boolean storeVisible) {
		final Product product = productLookup.findByGuid(productCode);

		assertThat(product.isHidden())
				.isNotEqualTo(storeVisible);
	}
}

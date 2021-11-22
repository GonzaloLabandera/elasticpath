/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.test.integration.extension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.testscenarios.SingleStoreMultiCatalogScenario;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.impl.ProductRecommendationXPFBridgeImpl;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;
import com.elasticpath.xpf.extensions.ElasticPathProductRecommendationsRetrieval;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFInMemoryExtensionResolverImpl;

@DirtiesDatabase
public class ProductRecommendationXPFBridgeImplTest extends BasicSpringContextTest {
	private static final int RESULT_COUNT = 1;
	@Autowired
	private ProductRecommendationXPFBridgeImpl productRecommendationXPFBridge;

	/**
	 * The main object under test.
	 */
	@Autowired
	@Qualifier(value = "productAssociationService")
	private ProductAssociationService service;

	@Autowired
	private XPFInMemoryExtensionResolverImpl resolver;

	private Store store;

	private Category category;

	private Catalog catalog;

	private CatalogTestPersister catalogTestPersister;

	private SingleStoreMultiCatalogScenario scenario;

	private ProductAssociationType associationType;

	private Product sourceProduct;

	private Product targetProduct;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SingleStoreMultiCatalogScenario.class);
		service = getBeanFactory().getSingletonBean(ContextIdNames.PRODUCT_ASSOCIATION_SERVICE, ProductAssociationService.class);
		store = scenario.getStore();
		category = scenario.getCategory();
		catalog = scenario.getCatalog();
		catalogTestPersister = getTac().getPersistersFactory().getCatalogTestPersister();

		sourceProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(catalog, category, store.getWarehouse());
		targetProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(catalog, category, store.getWarehouse());
		associationType = ProductAssociationType.ACCESSORY;
		catalogTestPersister.persistProductAssociation(sourceProduct.getMasterCatalog(), sourceProduct, targetProduct,
				associationType);

	}

	@Test
	public void testGetPaginatedResultWithDefaultExtensions() {
		XPFProductRecommendations result = productRecommendationXPFBridge.getPaginatedResult(store, sourceProduct.getCode(),
				associationType.getName(), 1, 10);

		assertEquals(RESULT_COUNT, result.getTotalResultsCount());
		assertEquals(RESULT_COUNT, result.getRecommendations().size());
		assertEquals(targetProduct.getCode(), result.getRecommendations().get(0).getTargetProductCode());
	}

	/**
	 * Integration test to check cart data is copied to order data.
	 */

	@Test
	public void testGetPaginatedResultWithMinimalExtensions() {
		resolver.removeExtensionFromSelector(ElasticPathProductRecommendationsRetrieval.class.getName(), null,
				XPFExtensionPointEnum.PRODUCT_RECOMMENDATIONS, new XPFExtensionSelectorAny());

		assertThatThrownBy(() -> productRecommendationXPFBridge.getPaginatedResult(store, sourceProduct.getCode(), associationType.getName(), 1, 10))
				.isInstanceOf(EpServiceException.class)
				.hasMessage("No valid extension found for extension point ProductRecommendations");
	}
}

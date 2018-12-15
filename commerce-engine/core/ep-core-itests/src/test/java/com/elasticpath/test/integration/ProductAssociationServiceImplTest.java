/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.testscenarios.SingleStoreMultiCatalogScenario;

/**
 * An integration test for the ProductAssociationService.
 */
public class ProductAssociationServiceImplTest extends BasicSpringContextTest {

	/** The main object under test. */
	@Autowired
	@Qualifier(value = "productAssociationService")
	private ProductAssociationService service;

	private Store store;

	private Category category;

	private Catalog catalog;

	private CatalogTestPersister catalogTestPersister;

	private SingleStoreMultiCatalogScenario scenario;

	private Catalog virtualCatalog;

	private Product sourceProduct;

	private Product targetProduct;

	private Product productInFirstCategory;

	private Product productInSecondCategory;

	private Product productInNoLinkCategory;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SingleStoreMultiCatalogScenario.class);
		service = getBeanFactory().getBean("productAssociationService");
		store = scenario.getStore();
		category = scenario.getCategory();
		catalog = scenario.getCatalog();
		catalogTestPersister = getTac().getPersistersFactory().getCatalogTestPersister();
		virtualCatalog = scenario.getVirtualCatalog();

		sourceProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(catalog, category, store.getWarehouse());
		targetProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(catalog, category, store.getWarehouse());

		Product tempProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(catalog, category, store.getWarehouse());
		productInFirstCategory = catalogTestPersister.addProductToCategoryAndUpdate(tempProduct, scenario.getFirstLinkedCategory());
		tempProduct = catalogTestPersister.createDefaultProductWithSkuAndInventory(scenario.getSecondMasterCatalog(), scenario
				.getSecondCatalogCategory(), store.getWarehouse());
		productInSecondCategory = catalogTestPersister.addProductToCategoryAndUpdate(tempProduct, scenario.getSecondLinkedCategory());
		productInNoLinkCategory = catalogTestPersister.createDefaultProductWithSkuAndInventory(scenario.getSecondMasterCatalog(), scenario
				.getSecondCatalogNoLinkCategory(), store.getWarehouse());
	}

	/**
	 * Test that calling getAssociationsByType will load a ProductAssociation's TargetProduct and that the TargetProduct's DefaultSku will be
	 * populated.
	 */
	@DirtiesDatabase
	@Test
	public void testLoadPopulatesTargetProductDefaultSku() {
		ProductAssociationType associationType = ProductAssociationType.ACCESSORY;
		ProductAssociation pa = catalogTestPersister.persistProductAssociation(sourceProduct.getMasterCatalog(), sourceProduct, targetProduct,
			associationType);
		Set<ProductAssociation> returnedAssociations = service.getAssociationsByType(sourceProduct.getCode(), associationType,
				sourceProduct.getMasterCatalog().getCode(), false);
		assertThat(returnedAssociations).hasSize(1);

		ProductAssociation savedAssociation = returnedAssociations.iterator().next();
		assertThat(savedAssociation.getTargetProduct().getDefaultSku()).isNotNull();
		assertThat(savedAssociation.getUidPk()).isEqualTo(pa.getUidPk());
	}

	/**
	 * Test that when we ask for associations that are only valid in the given catalog we only get the valid associations.
	 */
	@DirtiesDatabase
	@Test
	public void testVirtualCatalogAssociation() {

		ProductAssociation associationWithinCatalog = catalogTestPersister.persistProductAssociation(virtualCatalog, productInFirstCategory,
			productInSecondCategory, ProductAssociationType.ACCESSORY);
		assertThat(service.isAssociationInCatalog(associationWithinCatalog))
			.as("Association in catalog should be recognized as such")
			.isTrue();

		ProductAssociation associationOutsideOfCatalog = catalogTestPersister.persistProductAssociation(virtualCatalog, productInFirstCategory,
			productInNoLinkCategory, ProductAssociationType.ACCESSORY);
		assertThat(service.isAssociationInCatalog(associationOutsideOfCatalog))
			.as("Association out of catalog should be recognized as such")
			.isFalse();

		Set<ProductAssociation> allAssociations = service.getAssociations(productInFirstCategory.getCode(), virtualCatalog.getCode(), false);
		assertThat(allAssociations).hasSize(2);

		Set<ProductAssociation> associationsWithinCatalog = service
			.getAssociations(productInFirstCategory.getCode(), virtualCatalog.getCode(), true);
		assertThat(associationsWithinCatalog)
			.as("There should only be 1 association within the virtual catalog")
			.hasSize(1);

		ProductAssociation retrievedAssociation = associationsWithinCatalog.iterator().next();
		assertThat(retrievedAssociation.getUidPk()).isEqualTo(associationWithinCatalog.getUidPk());

		Set<ProductAssociation> filteredAssociations = service.limitAssociationsToCatalog(allAssociations);
		assertThat(filteredAssociations).hasSize(1);
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductLoadTunerImpl;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test class to verify that execution of JPQL queries is not affected by malicious content (e.g. SKUs with malicious JPQL/SQL statements).
 */
public class JPAPersistenceEngineTest extends DbTestCase {

	private static final String MALICIOUS_SKU_CODE = "SomeSKU;delete from ProductImpl p where p.uidPk=%d";
	private static final List<String> SKUS_WITH_SPECIAL_CHARS  = Arrays.asList("'SomeSKU", "\"SomeSKU", "Some.SKU", "Some,SKU", "Some<SKU"
		, "Some>SKU", "Some<>SKU", "Some/SKU", "Some?SKU", "--SomeSKU", "Some;SKU", "Some:SKU", "Some[SKU", "Some]SKU", "Some{SKU"
		, "Some}SKU", "Some\\SKU", "Some||delete from ProductSkuImpl||SKU", "Some=SKU", "Some==SKU", "Some_SKU", "Some++SKU"
		, "Some(SKU", "Some)SKU", "Some*SKU", "Some&SKU", "Some&&SKU", "Some^SKU", "Some%SKU", "Some$SKU", "Some$$SKU", "Some#SKU"
		, "Some@SKU", "Some!!SKU", "Some`SKU", "Some~SKU", "Some°SKU");

	@Autowired
	private ProductService productService;

	@Autowired
	private PersistenceEngine persistenceEngine;

	private ProductLoadTuner productLoadTuner = new ProductLoadTunerImpl();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private Product peristedProduct;
	private String formatedMaliciousSkuCode;

	@Before
	public void init() {

			Product maliciousProduct = createPersistedTestProductWithSKUs();

			//verify that product is created without loading SKUs
			peristedProduct = productService.getTuned(maliciousProduct.getUidPk(), productLoadTuner);

			assertThat(peristedProduct)
				.isNotNull();
	}

	/**
	 * Test retrieving a product with a malicious SKU via uidPK (no JPQL involved)
	 */
	@Test
	@DirtiesDatabase
	public void shouldRetrieveProductByUidPkWithMaliciousSKUWithoutAffectingTheProduct () {

		//load SKUs
		productLoadTuner.setLoadingSkus(true);
		// and fetch the product again via UidPK
		peristedProduct = productService.getTuned(peristedProduct.getUidPk(), productLoadTuner);

		//product must exist
		assertThat(peristedProduct)
			.isNotNull();

		// and default sku must be loaded
		assertThat(peristedProduct.getDefaultSku())
			.isNotNull();

	}

	/**
	 * Test retrieving a product with a malicious SKU using a dynamic JPQL and a list of parameters
	 */
	@Test
	@DirtiesDatabase
	public void shouldRetrieveProductWithMaliciousSKUUsingDynamicJPQLWithoutAffectingTheProduct () {

		//fetch product with all SKUs
		List<Product> products = persistenceEngine.retrieve("select distinct p from ProductImpl p join fetch p.productSkusInternal",
			(Object[]) null);

		//product must exist
		assertThat(products)
			.hasSize(1);

		// and default sku must be loaded
		assertThat(products.get(0).getDefaultSku())
			.isNotNull();

		// execute a query with more malicious SKUs
		List<ProductSku> skus = persistenceEngine.retrieve("select sku from ProductSkuImpl sku where sku.skuCodeInternal in ('"
			+ formatedMaliciousSkuCode + "', 'delete from CatalogImpl')", (Object[])null);

		//product must exist
		assertThat(skus)
			.hasSize(1);

		// and sku must be loaded
		assertThat(skus.get(0).getSkuCode())
			.isEqualTo(formatedMaliciousSkuCode);

		// execute a query with injected list, containing malicious SKUs
		List<String> maliciousSKUCodes = Arrays.asList(formatedMaliciousSkuCode, "delete from CatalogImpl;\"'`~!@#$%^&*()_+[]{}\\|;:,.<>/?");

		skus = persistenceEngine
			.retrieveWithList("select sku from ProductSkuImpl sku where sku.skuCodeInternal in :list", "list",
				maliciousSKUCodes, null, 0, 1);

		//product must exist
		assertThat(skus)
			.hasSize(1);

		// and sku must be loaded
		assertThat(skus.get(0).getSkuCode())
			.isEqualTo(formatedMaliciousSkuCode);

	}

	/**
	 * Test retrieving a product with a malicious SKU using a dynamic JPQL and a list of parameters
	 */
	@Test
	@DirtiesDatabase
	public void shouldRetrieveProductWithMaliciousSKUUsingNamedQueryWithoutAffectingTheProduct () {

		//fetch SKU by passing malicious string
		List<String> skuGUIDs = persistenceEngine.retrieveByNamedQuery("PRODUCT_SKU_GUID_SELECT_BY_GUID",
			new Object[]{formatedMaliciousSkuCode});

		//product must exist
		assertThat(skuGUIDs)
			.hasSize(1);

		// and sku must be loaded
		assertThat(skuGUIDs.get(0))
			.isEqualTo(formatedMaliciousSkuCode);

		// execute a query with more malicious SKUs
		List<ProductSku> skus = persistenceEngine.retrieveByNamedQueryWithList("PRODUCT_SKUS_BY_SKU_CODES", "list",
			Arrays.asList(formatedMaliciousSkuCode, "delete from CatalogImpl"), (Object[]) null);

		//product must exist
		assertThat(skus)
			.hasSize(1);

		// and sku must be loaded
		assertThat(skus.get(0).getSkuCode())
			.isEqualTo(formatedMaliciousSkuCode);

	}

	/**
	 * Test dynamic and named queries using empty list of parameters.
	 */
	@Test
	@DirtiesDatabase
	public void shouldReturnNothingWhenEmptyListOfParamsIsPassedToAQuery () {

		//test dynamic query
		List<ProductSku> skus = persistenceEngine.retrieveWithList("select sku from ProductSkuImpl sku where sku.uidPk in (:listUids)",
			"listUids", new ArrayList<Long>(), null, 0, 10);

		//product must exist
		assertThat(skus)
			.isEmpty();

		//test named query
		skus = persistenceEngine.retrieveByNamedQueryWithList("PRODUCT_SKUS_BY_SKU_CODES", "list", new ArrayList<String>(),
			(Object[]) null);

		//product must exist
		assertThat(skus)
			.isEmpty();

	}

	@Test
	@DirtiesDatabase
	public void shouldReturnAllSKUsWithSpecialCharacters () {
		//test dynamic query
		List<ProductSku> skus = persistenceEngine.retrieveWithList("select sku from ProductSkuImpl sku where sku.skuCodeInternal in (:skus)",
			"skus", SKUS_WITH_SPECIAL_CHARS, null, 0, 100);

		assertThat(skus)
			.extracting("skuCode")
			.containsAll(SKUS_WITH_SPECIAL_CHARS);

		//test named query
		skus = persistenceEngine.retrieveByNamedQueryWithList("PRODUCT_SKUS_BY_SKU_CODES", "list", SKUS_WITH_SPECIAL_CHARS,
			(Object[]) null);

		assertThat(skus)
			.extracting("skuCode")
			.containsAll(SKUS_WITH_SPECIAL_CHARS);

	}

	private Product createPersistedTestProductWithSKUs() {
		TaxCode taxCode  = getTac().getPersistersFactory().getTaxTestPersister().getTaxCode("NONE");

		Product product = getTac().getPersistersFactory().getCatalogTestPersister()
				.createSimpleProduct("testProductType", "maliciousProduct", scenario.getCatalog(), taxCode, scenario
			.getCategory());

		productService.saveOrUpdate(product);
		formatedMaliciousSkuCode = String.format(MALICIOUS_SKU_CODE, product.getUidPk());

		product = getTac().getPersistersFactory().getCatalogTestPersister()
			.addSkuToProduct(product, formatedMaliciousSkuCode, new ArrayList<>(), new Date(), new Date());

		for (String specialSku : SKUS_WITH_SPECIAL_CHARS) {
			product = getTac().getPersistersFactory().getCatalogTestPersister()
				.addSkuToProduct(product, specialSku, new ArrayList<>(), new Date(), new Date());
		}

		return product;
	}
}
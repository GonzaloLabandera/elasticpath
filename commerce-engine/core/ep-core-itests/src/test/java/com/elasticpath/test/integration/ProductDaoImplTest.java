/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.util.Utils;

/**
 * Tests DAO operations on <code>ProductType</code>.
 */
public class ProductDaoImplTest extends DbTestCase {

	private static final String PRODUCT = "product";

	private static final String GOODS = "GOODS";

	private static final String MULTI_SKU_PRODUCT = "Multi-Sku Product";

	private static final String NEW_SKU = "newSku";

	@Autowired
	private ProductDao productDao;

	@Autowired
	private ProductLookup productLookup;

	private static final double PRODUCT_PRICE = 269.00;

	/**
	 * Test multi sku products can have new skus added and correctly persisted without exception,
	 * and that non-zero uidPk's are assigned.
	 */
	@DirtiesDatabase
	@Test
	public void testAddSkuToMultiSkuProduct() {
		// Persist a test multi-sku product
		final String skuCode1 = Utils.uniqueCode(NEW_SKU);
		final String skuCode2 = Utils.uniqueCode(NEW_SKU);
		final Product product = persisterFactory.getCatalogTestPersister().persistMultiSkuProduct(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(PRODUCT_PRICE), TestDataPersisterFactory.DEFAULT_CURRENCY,
			null, Utils.uniqueCode(PRODUCT), MULTI_SKU_PRODUCT, GOODS, null, 0, skuCode1, skuCode2);

		final ProductLoadTuner productLoadTuner = getBeanFactory().getPrototypeBean(ContextIdNames.PRODUCT_LOAD_TUNER, ProductLoadTuner.class);
		productLoadTuner.setLoadingSkus(true);
		final Product retrievedProduct = productDao.getTuned(product.getUidPk(), productLoadTuner);
		assertThat(retrievedProduct.getProductSkus()).hasSize(2);

		// Add a new SKU
		final ProductSku productSku = getBeanFactory().getPrototypeBean(ContextIdNames.PRODUCT_SKU, ProductSku.class);
		productSku.setSkuCode("ADDANOTHER2");
		productSku.setStartDate(new Date());
		retrievedProduct.addOrUpdateSku(productSku);

		// Persist the product and return it
		final Product loadedProduct = productDao.saveOrUpdate(retrievedProduct);

		for (final ProductSku sku : loadedProduct.getProductSkus().values()) {
			assertThat(sku.getUidPk()).isNotEqualTo(0);
		}
	}

	/**
	 * This method tests the result of getAllUids() which includes the product bundle uids.
	 */
	@DirtiesDatabase
	@Test
	public void testFindAllUidsIncludesProductBundleUids() {
		final String productType = "DUMMY_TYPE";
		final String bundleSkuCode = "DUMMY_BUNDLE_SKU";
		final String code = "DUMMY_PRODUCT";

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();

		catalogTestPersister.persistSimpleProduct(
			code,
			productType,
			scenario.getCatalog(),
			scenario.getCategory(),
			catalogTestPersister.getPersistedTaxCode(GOODS)
		);

		final ProductBundle bundle = catalogTestPersister.createSimpleProductBundle(
			productType,
			bundleSkuCode,
			scenario.getCatalog(),
			scenario.getCategory(),
			catalogTestPersister.getPersistedTaxCode(GOODS)
		);
		productDao.saveOrUpdate(bundle);

		final List<Long> uids = productDao.findAllUids();

		final int expectedNumberOfProducts = 2;
		assertThat(uids).hasSize(expectedNumberOfProducts);
		assertThat(productLookup.<Product>findByUid(uids.get(1))).isInstanceOf(ProductBundle.class);
	}

	/**
	 * This method tests the updateProductLastModifiedTime().
	 * It was rise exception for mysql without
	 * <entry key="openjpa.jdbc.DBDictionary" value="supportsCorrelatedSubselect=false" />
	 */
	@DirtiesDatabase
	@Test
	public void testLastUpdateDate() {
		// Persist a test multi-sku product
		final String skuCode1 = Utils.uniqueCode(NEW_SKU);
		final String skuCode2 = Utils.uniqueCode(NEW_SKU);
		final Product product = persisterFactory.getCatalogTestPersister().persistMultiSkuProduct(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(PRODUCT_PRICE), TestDataPersisterFactory.DEFAULT_CURRENCY,
				null, Utils.uniqueCode(PRODUCT), MULTI_SKU_PRODUCT, GOODS, null, 0, skuCode1, skuCode2);

		productDao.updateProductLastModifiedTime(product);

	}

	/**
	 * Test enriching data is correctly retrieved for single/multi sku product.
	 */
	@DirtiesDatabase
	@Test
	public void testFindEnrichingData() {

		// Persist a test multi-sku product
		final String skuCode1 = Utils.uniqueCode(NEW_SKU);
		final String skuCode2 = Utils.uniqueCode(NEW_SKU);
		final String productMultiSKUCode = Utils.uniqueCode(PRODUCT);
		persisterFactory.getCatalogTestPersister().persistMultiSkuProduct(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(PRODUCT_PRICE), TestDataPersisterFactory.DEFAULT_CURRENCY,
			null, productMultiSKUCode, MULTI_SKU_PRODUCT, GOODS, null, 0, skuCode1, skuCode2);

		final Collection<String> collMS = ImmutableList.of(productMultiSKUCode);
		final List<Object[]> resultMS = productDao.findEnrichingData("PRODUCT_ENRICH_DTO_BY_GUIDS", collMS, Locale.ENGLISH);
		assertThat(resultMS).hasSize(1);
		assertThat(resultMS.get(0))
			.containsExactly(productMultiSKUCode, MULTI_SKU_PRODUCT, true, skuCode1);

		final String productSingleSKUCode = Utils.uniqueCode(PRODUCT);
		final String productSingleSKUskuCode = productSingleSKUCode + "SKU";
		persisterFactory.getCatalogTestPersister().persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(PRODUCT_PRICE), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productSingleSKUCode, "Single-Sku Product", productSingleSKUskuCode, GOODS, null, 0);

		final Collection<String> collSS = ImmutableList.of(productSingleSKUCode);
		final List<Object[]> resultSS = productDao.findEnrichingData("PRODUCT_ENRICH_DTO_BY_GUIDS", collSS, Locale.ENGLISH);
		assertThat(resultSS).hasSize(1);
		assertThat(resultSS.get(0))
			.containsExactly(productSingleSKUCode, "Single-Sku Product", false, productSingleSKUskuCode);

	}

	/**
	 * Tests find uid by sku code.
	 */
	@DirtiesDatabase
	@Test
	public void testFindUidBySkuCode() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();

		final String productSingleSKUCode = Utils.uniqueCode(PRODUCT);
		final String productSingleSKUskuCode = productSingleSKUCode + "SKU";

		final Product product = catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(PRODUCT_PRICE), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productSingleSKUCode, "Single-Sku Product", productSingleSKUskuCode, GOODS, null, 0);

		final Long resultUid = productDao.findUidBySkuCode(productSingleSKUskuCode);

		assertThat(resultUid).isEqualTo(product.getUidPk());
	}
}

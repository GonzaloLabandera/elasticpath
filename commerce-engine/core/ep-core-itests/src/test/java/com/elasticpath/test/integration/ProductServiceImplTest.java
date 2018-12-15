/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.catalog.AttributeValueIsRequiredException;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.util.Utils;

/**
 * An integration test for ProductServiceImpl, we are testing from a client's point of view with Spring and the Database up and running.
 */
 @SuppressWarnings("PMD.GodClass")
public class ProductServiceImplTest extends DbTestCase {

	private static final String REQUIRED_ATTR_KEY = "my_req_attr_key";

	@Autowired
	@Qualifier("productService")
	/** The main object under test. */
	private ProductService service;

	@Autowired
	@Qualifier("nonCachingProductLookup")
	private ProductLookup productLookup;

	private static final int TIMESTAMP_TOLERANCE = 1000;

	/**
	 * Test adding categories to products.
	 */
	@Test
	public void testProductCategories() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		assertThat(product.getCategories()).hasSize(1);

		final Category newCategory = persisterFactory.getCatalogTestPersister().persistCategory(Utils.uniqueCode("newcategory"),
			scenario.getCatalog(), scenario.getCategory().getCategoryType(), "new category", Locale.ENGLISH.toString());
		product.addCategory(newCategory);

		final Product updatedProduct = service.saveOrUpdate(product);
		assertThat(updatedProduct.getCategories()).hasSize(2);

		final Product loadedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(loadedProduct.getCategories()).hasSize(2);
	}

	/**
	 * Tests finding products by category uid.
	 */
	@Test
	public void testFindByCategoryUid() {

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		final List<Product> returnedProducts = service.findByCategoryUid(scenario.getCategory().getUidPk());

		// assertPersistenceInList(product, returnedProducts);

		assertThat(returnedProducts).hasSize(1);
		final Product returnedProduct = returnedProducts.get(0);
		assertThat(returnedProduct.getUidPk()).isEqualTo(product.getUidPk());
	}

	/**
	 * Tests finding products by category uid with load tuner.
	 */
	@Test
	public void testFindByCategoryUidWithLoadTuner() {

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		final ProductLoadTuner loadTuner = getBeanFactory().getBean("PRODUCT_LOAD_TUNER_ALL");
		final List<Product> returnedProducts = service.findByCategoryUid(scenario.getCategory().getUidPk(), loadTuner);

		// assertPersistenceInList(product, returnedProducts);

		assertThat(returnedProducts).hasSize(1);
		final Product returnedProduct = returnedProducts.get(0);
		assertThat(returnedProduct.getUidPk()).isEqualTo(product.getUidPk());
		assertThat(returnedProduct.getDefaultSku()).isNotNull();
	}

	/**
	 * Tests removing of a product.
	 */
	@Test
	public void testRemove() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
		service.removeProductTree(product.getUidPk());
	}

	/**
	 * Tests that products that are retrieved and then have the end date changed persist the changes.
	 */
	@Test
	public void testMergeProductWithDateChange() {
		Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date date = new Date();
		product.setEndDate(date);

		product = service.saveOrUpdate(product);

		final Product retrievedProduct = productLookup.findByUid(product.getUidPk());

		final Product mergedProduct = service.saveOrUpdate(retrievedProduct);

		assertThat(mergedProduct.getEndDate()).isNotNull();

		final Product retrievedProduct2 = productLookup.findByUid(product.getUidPk());
		retrievedProduct2.setEndDate(null);

		final Product mergedProduct2 = service.saveOrUpdate(retrievedProduct2);

		assertThat(mergedProduct2.getEndDate()).isNull();
	}

	 /**
	 * Test whether a catgeory is affected by a product update.
	 *
	 * @throws InterruptedException in case of sleep interruption
	 */
	@Test
	public void testProductUpdateAffectsCategory() throws InterruptedException {

		final Category category = scenario.getCategory();
		final Catalog catalog = scenario.getCatalog();
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(catalog,
			category, scenario.getWarehouse());
		assertThat(product.getDefaultCategory(catalog)).isEqualTo(category);

		// Get the last modified date of the category
		final Date lastModified = category.getLastModifiedDate();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH);
		assertThat(lastModified).isNotNull();
		final String lastModifiedString = formatter.format(lastModified);
		assertThat(lastModifiedString)
			.as("Date should be able to be formatted as a string")
			.isNotEmpty();

		// we need to delay long enough to be sure that a difference would be detected if the catgeory
		// lastModifiedDate was changed.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		// Make a change to the product and save it
		final String productName = "New Product Name";
		product.setDisplayName(productName, Locale.ENGLISH);
		final Product updatedProduct = service.saveOrUpdate(product);
		assertThat(updatedProduct.getDisplayName(Locale.ENGLISH))
			.as("Updated product should have new product name")
			.isEqualTo(productName);
		final Date newLastModified = updatedProduct.getDefaultCategory(catalog).getLastModifiedDate();
		assertThat(newLastModified)
			.as("Category should still have same updated time")
			.isEqualTo(lastModified);

		// reload the category
		final CategoryLookup categoryLookup = getBeanFactory().getBean(ContextIdNames.CATEGORY_LOOKUP);
		final Category loadedCategory = categoryLookup.findByUid(category.getUidPk());
		assertThat(loadedCategory.getLastModifiedDate())
			.as("Loaded category time should still be the same")
			.isInSameSecondAs(lastModified);
	}

	/**
	 * Test that updating something on a child object (eg SKU) is persisted when we persist the product.
	 */
	@Test
	public void testUpdateChildObject() {
		// Create a product and get the sku
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final ProductSku sku = product.getProductSkus().entrySet().iterator().next().getValue();
		assertThat(sku).isNotNull();
		final String skuCode = sku.getSkuCode();
		assertThat(skuCode).isNotEmpty();

		// Make a change to the sku and save the product
		sku.setHeight(BigDecimal.TEN);
		final Product updatedProduct = service.saveOrUpdate(product);
		assertThat(updatedProduct.getSkuByCode(skuCode).getHeight())
			.isEqualByComparingTo(BigDecimal.TEN);

		// Reload the product and check the value again
		final Product loadedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(loadedProduct.getSkuByCode(skuCode).getHeight())
			.as("Loaded product's sku should still have same height value")
			.isEqualByComparingTo(BigDecimal.TEN);

	}

	/**
	 * Test that the paginated methods retrieve just the required number of products.
	 */
	@Test
	public void testPagination() {
		// Create a new category to contain a bunch of products
		final Category fullCategory = scenario.getCategory();
		assertThat(fullCategory).isNotNull();
		assertThat(fullCategory.isPersisted()).isTrue();

		// Create a bunch of products for a category
		for (int i = 0; i < 10; i++) {
			persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(), fullCategory,
				scenario.getWarehouse());
		}

		// Call the product service to request 5 products from this category
		final List<Product> products = service.findByCategoryUidPaginated(fullCategory.getUidPk(), 0, 5, null);
		assertThat(products).hasSize(5);
	}

	/**
	 * Tests product sku removal.
	 */
	@Test
	public void testRemoveSku() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		product.removeSku(product.getDefaultSku());
		service.saveOrUpdate(product);
		final Product updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct).isNotNull();
	}

	/**
	 * Tests adding a product.
	 */
	@Test
	public void testAdd() {
		final Product product = persisterFactory.getCatalogTestPersister().createSimpleProduct(
			"TestProductType", Utils.uniqueCode("product"), scenario.getCatalog(),
			getTac().getPersistersFactory().getTaxTestPersister().getTaxCode(TaxTestPersister.TAX_CODE_GOODS),
			scenario.getCategory());

		assertThat(product.isPersisted()).isFalse();

		final Product savedProduct = service.saveOrUpdate(product);

		assertThat(savedProduct).isEqualTo(product);
		assertThat(savedProduct.isPersisted()).isTrue();
	}

	/**
	 * Test getting the Sku count for a product.
	 */
	@Test
	public void testGetProductSkuCount() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final long productUid = product.getUidPk();
		final int skuCount = service.getProductSkuCount(productUid);
		assertThat(skuCount).isEqualTo(1);

		assertThat(service.getProductSkuCount(0L))
			.as("Non-existent product should have 0 sku count")
			.isEqualTo(0);
	}

	/**
	 * Test getting the maximum featured product order within a category.
	 */
	@Test
	public void testGetMaxFeaturedProductOrder() {
		assertThat(service.getMaxFeaturedProductOrder(0L))
			.as("Max featured product order should be 0 for non-existent category")
			.isEqualTo(0);
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		product.setFeaturedRank(scenario.getCategory(), 1);
		assertThat(product.getMaxFeaturedProductOrder()).isEqualTo(1);
		service.saveOrUpdate(product);
		assertThat(service.getMaxFeaturedProductOrder(scenario.getCategory().getUidPk()))
			.as("Max featured product order should match what the product thinks")
			.isEqualTo(1);
	}

	/**
	 * Test getting a product specifying which fields to load via a fetch group.
	 */
	@Test
	public void testGetTunedWithFetchGroup() {
		assertThat(service.getTuned(0L, (FetchGroupLoadTuner) null))
			.as("Request for Tuned non-existent product should return null")
			.isNull();

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final long productUid = product.getUidPk();

		final FetchGroupLoadTuner categoryOnlyTuner = getBeanFactory().getBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER);
		categoryOnlyTuner.addFetchGroup(FetchGroupConstants.LINK_PRODUCT_CATEGORY);
		final Product categoryOnlyProduct = service.getTuned(productUid, categoryOnlyTuner);
		assertThat(categoryOnlyProduct.getCategories())
			.as("Category only tuned product should include categories")
			.isNotEmpty();

		product.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		service.saveOrUpdate(product);

		final FetchGroupLoadTuner indexTuner = getBeanFactory().getBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER);
		indexTuner.addFetchGroup(FetchGroupConstants.PRODUCT_INDEX);
		final Product indexProduct = service.getTuned(productUid, indexTuner);
		assertThat(indexProduct.getCategories())
			.as("Index tuned product should include categories")
			.isNotEmpty();
		assertThat(indexProduct.getProductSkus())
			.as("Index tuned product should include skus")
			.isNotEmpty();
	}

	/**
	 * Test getting a product specifying which fields to load via a load tuner.
	 */
	@Test
	public void testGetTunedWithLoadTuner() {
		assertThat(service.getTuned(0L, (ProductLoadTuner) null))
			.as("Request for Tuned non-existent product should return null")
			.isNull();

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final long productUid = product.getUidPk();

		final ProductLoadTuner tuner = getBeanFactory().getBean(ContextIdNames.PRODUCT_LOAD_TUNER);
		tuner.setLoadingAttributeValue(true);
		tuner.setLoadingSkus(false);

		Product tunedProduct = service.getTuned(productUid, tuner);
		assertThat(tunedProduct.getAttributeValueMap())
			.as("Tuned product should include attributes")
			.isNotNull();

		tuner.setLoadingSkus(true);
		tunedProduct = service.getTuned(productUid, tuner);
		assertThat(tunedProduct.getAttributeValueMap())
			.as("Tuned product should include attributes")
			.isNotNull();
		assertThat(tunedProduct.getProductSkus())
			.as("Tuned product should include skus")
			.isNotEmpty();
	}

	/**
	 * Test saving and updating a product.
	 *
	 * @throws InterruptedException if sleep is interrupted.
	 */
	@Test
	public void testSaveOrUpdate() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createSimpleProduct(
			"TestProductType", Utils.uniqueCode("product"), scenario.getCatalog(),
			getTac().getPersistersFactory().getTaxTestPersister().getTaxCode(TaxTestPersister.TAX_CODE_GOODS),
			scenario.getCategory());
		assertThat(product.isPersisted()).isFalse();

		final Product savedProduct = service.saveOrUpdate(product);
		assertThat(savedProduct.isPersisted()).isTrue();

		final Date preUpdateDate = savedProduct.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		savedProduct.setMinOrderQty(10);
		final Product updatedProduct = service.saveOrUpdate(savedProduct);
		assertThat(updatedProduct.getLastModifiedDate().after(preUpdateDate))
			.as("The updated product should have a later last modified date")
			.isTrue();

		final Product loadedProduct = productLookup.findByUid(savedProduct.getUidPk());
		assertThat(loadedProduct.getMinOrderQty()).isEqualTo(10);
	}

	@Test
	public void verifySparselyLoadedProductCanBePersisted() throws Exception {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(),
			scenario.getWarehouse());

		final ProductLoadTuner tuner = getTac().getBeanFactory().getBean(ContextIdNames.PRODUCT_LOAD_TUNER);
		tuner.setLoadingAttributeValue(false);
		tuner.setLoadingSkus(false);

		final Product tunedProduct = service.getTuned(product.getUidPk(), tuner);

		final Product persistedProduct = service.saveOrUpdate(tunedProduct);
		assertThat(persistedProduct)
			.as("The sparsely-loaded product should have been persisted")
			.isNotNull();
	}

	/**
	 * Test removing a product tree.
	 */
	@Test
	public void testRemoveProductTree() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		persisterFactory.getCatalogTestPersister().persistProductAssociation(scenario.getCatalog(),
			product1, product2, ProductAssociationType.ACCESSORY);
		assertThat(productLookup.<Product>findByUid(product1.getUidPk())).isNotNull();
		assertThat(productLookup.<Product>findByUid(product2.getUidPk())).isNotNull();

		service.removeProductTree(product2.getUidPk());
		assertThat(productLookup.<Product>findByUid(product1.getUidPk())).isNotNull();
		assertThat(productLookup.<Product>findByUid(product2.getUidPk())).isNull();
	}

	/**
	 * Test removing a list of products.
	 */
	@Test
	public void testRemoveProductList() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		persisterFactory.getCatalogTestPersister().persistProductAssociation(scenario.getCatalog(),
			product1, product2, ProductAssociationType.ACCESSORY);
		assertThat(productLookup.<Product>findByUid(product1.getUidPk())).isNotNull();
		assertThat(productLookup.<Product>findByUid(product2.getUidPk())).isNotNull();
		final List<Long> productUidList = new ArrayList<>();
		productUidList.add(product1.getUidPk());
		productUidList.add(product2.getUidPk());
		service.removeProductList(productUidList);
		assertThat(productLookup.<Product>findByUid(product1.getUidPk())).isNull();
		assertThat(productLookup.<Product>findByUid(product2.getUidPk())).isNull();
	}

	/**
	 * Test finding top seller product for a category.
	 */
	@Test
	public void testFindProductTopSellerForCategory() {
		Category category = persisterFactory.getCatalogTestPersister().persistCategory(Utils.uniqueCode("category"),
			scenario.getCatalog(), scenario.getCategory().getCategoryType(), null, null);
		Category subCategory = persisterFactory.getCatalogTestPersister().persistCategory(Utils.uniqueCode("subcategory"),
			scenario.getCatalog(), scenario.getCategory().getCategoryType(), null, null);
		subCategory.setParent(category);
		final CategoryService categoryService = getBeanFactory().getBean("categoryService");
		subCategory = categoryService.saveOrUpdate(subCategory);

		Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			category, scenario.getWarehouse());
		Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			subCategory, scenario.getWarehouse());

		product1.setSalesCount(10);
		product2.setSalesCount(1);
		product1 = service.saveOrUpdate(product1);
		product2 = service.saveOrUpdate(product2);

		final int topSellersToRetrieve = 3;
		List<Product> topSellers = service.findProductTopSellerForCategory(subCategory.getUidPk(), topSellersToRetrieve);
		assertThat(topSellers)
			.as("We should only receive one product in the subcategory")
			.containsOnly(product2);

		topSellers = service.findProductTopSellerForCategory(category.getUidPk(), topSellersToRetrieve);
		assertThat(topSellers)
			.as("We should receive the two products that are in the category tree")
			.containsExactlyInAnyOrder(product1, product2);
	}

	/**
	 * Test finding a product by last modified date.
	 */
	@Test
	public void testFindByModifiedDate() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();
		assertThat(lastModified).isNotNull();

		final List<Product> foundList = service.findByModifiedDate(lastModified);
		assertThat(foundList).containsOnly(product);

		final Calendar otherDate = Calendar.getInstance();
		otherDate.setTime(lastModified);
		otherDate.add(Calendar.YEAR, 1);
		final List<Product> notFoundList = service.findByModifiedDate(otherDate.getTime());
		assertThat(notFoundList)
			.as("No results should have been found for another date")
			.isEmpty();
	}

	/**
	 * Test finding a uid by the deleted date.
	 */
	@Test
	public void testFindUidsByDeletedDate() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
		final long productUid = product.getUidPk();
		service.removeProductTree(productUid);
		final Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		final List<Long> deletedList = service.findUidsByDeletedDate(yesterday.getTime());
		assertThat(deletedList)
			.as("the product that was removed should appear in the list of deleted products")
			.contains(productUid);
	}


	/**
	 * Test finding a list of products given a list of uids.
	 */
	@Test
	public void testFindByUids() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product3 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		final List<Long> uids = new ArrayList<>();
		uids.add(product1.getUidPk());
		uids.add(product2.getUidPk());
		final List<Product> products = service.findByUids(uids, null);
		assertThat(products)
			.contains(product1, product2)
			.doesNotContain(product3);
	}

	/**
	 * Test finding all uids.
	 */
	@Test
	public void testFindAllUids() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product3 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		final List<Long> products = service.findAllUids();
		assertThat(products).contains(product1.getUidPk(), product2.getUidPk(), product3.getUidPk());

	}

	/**
	 * Test find a product by the brand uid.
	 */
	@Test
	public void testFindByBrandUid() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Brand brand = persisterFactory.getCatalogTestPersister().persistProductBrand(scenario.getCatalog(), Utils.uniqueCode("brand"));
		product.setBrand(brand);
		final Product updatedProduct = service.saveOrUpdate(product);

		final List<Product> foundList = service.findByBrandUid(brand.getUidPk());
		assertThat(foundList).containsOnly(updatedProduct);

	}

	/**
	 * Test finding a paginated list of products by the category uid.
	 */
	@Test
	public void testFindByCategoryUidPaginated() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product3 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		final long categoryUid = scenario.getCategory().getUidPk();

		final List<Product> foundProducts = service.findByCategoryUidPaginated(categoryUid, 0, 2, null);
		assertThat(foundProducts).hasSize(2);

		final List<Product> nextProducts = service.findByCategoryUidPaginated(categoryUid, 2, 10, null);
		assertThat(nextProducts).hasSize(1);
		assertThat(foundProducts).doesNotContain(nextProducts.get(0));

		final List<Product> allProducts = new ArrayList<>();
		allProducts.addAll(foundProducts);
		allProducts.addAll(nextProducts);
		assertThat(allProducts).contains(product1, product2, product3);

		final List<Product> noProducts = service.findByCategoryUidPaginated(categoryUid, 3, 10, null);
		assertThat(noProducts).isEmpty();

	}

	/**
	 * Test finding uids for available products.
	 */
	@Test
	public void testFindAvailableUids() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Calendar lastMonth = Calendar.getInstance();
		lastMonth.add(Calendar.MONTH, -1);
		product2.setStartDate(lastMonth.getTime());
		product2.setEndDate(lastMonth.getTime());
		service.saveOrUpdate(product2);

		final List<Long> availableUids = service.findAvailableUids();
		assertThat(availableUids)
			.contains(product1.getUidPk())
			.doesNotContain(product2.getUidPk());
	}

	/**
	 * Test finding uids for available products by modified date.
	 */
	@Test
	public void testFindAvailableUidsByModifiedDate() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Calendar lastMonth = Calendar.getInstance();
		lastMonth.add(Calendar.MONTH, -1);
		product2.setStartDate(lastMonth.getTime());
		product2.setEndDate(lastMonth.getTime());
		service.saveOrUpdate(product2);

		final List<Long> availableUids = service.findAvailableUids();
		assertThat(availableUids)
			.contains(product1.getUidPk())
			.doesNotContain(product2.getUidPk());

		final Calendar otherDate = Calendar.getInstance();
		otherDate.setTime(product1.getLastModifiedDate());
		otherDate.add(Calendar.YEAR, 1);
		final List<Long> notFoundList = service.findAvailableUidsByModifiedDate(otherDate.getTime());
		assertThat(notFoundList).isEmpty();
	}

	/**
	 * Test fiding uids for products by modified date.
	 */
	@Test
	public void testFindUidsByModifiedDate() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();
		assertThat(lastModified).isNotNull();

		final List<Long> foundList = service.findUidsByModifiedDate(lastModified);
		assertThat(foundList).containsOnly(product.getUidPk());

		final Calendar otherDate = Calendar.getInstance();
		otherDate.setTime(lastModified);
		otherDate.add(Calendar.YEAR, 1);
		final List<Long> notFoundList = service.findUidsByModifiedDate(otherDate.getTime());
		assertThat(notFoundList).isEmpty();
	}

	/**
	 * Test finding a product given the Id (GUID).
	 */
	@Test
	public void testFindUidById() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final long uid = service.findUidById(product.getGuid());
		assertThat(uid).isEqualTo(product.getUidPk());

		long invalidUid = service.findUidById("");
		assertThat(invalidUid)
			.as("An invalid search should have returned 0")
			.isEqualTo(0);

		//This tests the scanario where if the funtion is invoked with non empty string id it should return 0 as uidPK.
		invalidUid = service.findUidById("1234");
		assertThat(invalidUid)
			.as("An invalid search should have returned 0")
			.isEqualTo(0);
	}

	/**
	 * Test that no entries are returned when the query list is empty.
	 */
	@Test
	public void testEmptyFindCodesByUids() {
		List<Long> productUids = new LinkedList<>();
		Map<Long, String> results = service.findCodesByUids(productUids);
		assertThat(results)
			.as("Results map should be empty because the query was empty.")
			.isEmpty();
	}

	/**
	 * Test that a query with a product uid that exists and one that doesn't exist will
	 * return a result that only has one entry in it (for the uid that exists).
	 */
	@Test
	public void testFindCodesByUidsWhereUidDoesNotExist() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
			scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		final long badUid = product1.getUidPk() + 1;

		assertThat(productLookup.<Product>findByUid(badUid)).isNull();

		List<Long> productUids = new LinkedList<>();
		productUids.add(product1.getUidPk());
		productUids.add(badUid);
		Map<Long, String> results = service.findCodesByUids(productUids);
		assertThat(results)
			.hasSize(1)
			.doesNotContainKey(badUid);
		assertThat(results.get(product1.getUidPk())).isEqualTo(product1.getCode());
	}

	/**
	 * Test finding a list of product codes using a list of product uids.
	 */
	@Test
	public void testFindCodesByUids() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
			scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
			scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		final Product product3 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
			scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		List<Long> productUids = new LinkedList<>();
		productUids.add(product1.getUidPk());
		productUids.add(product2.getUidPk());
		productUids.add(product3.getUidPk());

		Map<Long, String> results = service.findCodesByUids(productUids);
		assertThat(results).containsOnly(
			entry(product1.getUidPk(), product1.getCode()),
			entry(product2.getUidPk(), product2.getCode()),
			entry(product3.getUidPk(), product3.getCode()));
	}

	/**
	 * Test that a guid exists when it should.
	 */
	@Test
	public void testGuidExists() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		assertThat(service.guidExists(product.getGuid())).isTrue();
		assertThat(service.guidExists("badGuid"))
			.as("A made up guid should not exist")
			.isFalse();
	}

	/**
	 * Test finding product uids given a list of category uids.
	 */
	@Test
	public void testFindUidsByCategoryUids() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final List<Long> categoryUids = new ArrayList<>();
		categoryUids.add(scenario.getCategory().getUidPk());
		final List<Long> productUids = service.findUidsByCategoryUids(categoryUids);
		assertThat(productUids).containsOnly(product.getUidPk());

		assertThat(service.findUidsByCategoryUids(null))
			.as("A null catgeory list should return an empty result set")
			.isEmpty();

		final List<Long> emptyList = new ArrayList<>();
		assertThat(service.findUidsByCategoryUids(emptyList))
			.as("An empty category list should return an empty result set")
			.isEmpty();

		final List<Long> invalidList = new ArrayList<>();
		invalidList.add(0L);
		assertThat(service.findUidsByCategoryUids(invalidList))
			.as("A list of invalid category uids should return an empty result set")
			.isEmpty();
	}

	/**
	 * Test updating the order of featured products.
	 */
	@Test
	public void testUpdateFeaturedProductOrder() {
		final Product product1 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Category newCategory = persisterFactory.getCatalogTestPersister().persistCategory(Utils.uniqueCode("category"),
			scenario.getCatalog(), scenario.getCategory().getCategoryType(), null, null);
		final Product product3 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			newCategory, scenario.getWarehouse());

		product1.setFeaturedRank(scenario.getCategory(), 1);
		product2.setFeaturedRank(scenario.getCategory(), 2);
		final Product updatedProduct1 = service.saveOrUpdate(product1);
		final Product updatedProduct2 = service.saveOrUpdate(product2);
		assertThat(product1.getFeaturedRank(scenario.getCategory())).isEqualTo(1);
		assertThat(product2.getFeaturedRank(scenario.getCategory())).isEqualTo(2);

		service.updateFeaturedProductOrder(updatedProduct1.getUidPk(), scenario.getCategory().getUidPk(), product2.getUidPk());
		assertThat(updatedProduct1.getFeaturedRank(scenario.getCategory())).isEqualTo(1);
		assertThat(updatedProduct2.getFeaturedRank(scenario.getCategory())).isEqualTo(2);

		assertThatThrownBy(() ->
			service.updateFeaturedProductOrder(updatedProduct1.getUidPk(), scenario.getCategory().getUidPk(), product3.getUidPk()))
			.as("Trying to update a product not in the given category should throw an exception")
			.isInstanceOf(EpServiceException.class);

	}

	/**
	 * Test checking if products are in a given category.
	 */
	@Test
	public void testHasProductsInCategory() {
		final Category category = persisterFactory.getCatalogTestPersister().persistCategory(Utils.uniqueCode("category"),
			scenario.getCatalog(), scenario.getCategory().getCategoryType(), null, null);

		persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());

		assertThat(service.hasProductsInCategory(scenario.getCategory().getUidPk())).isTrue();
		assertThat(service.hasProductsInCategory(category.getUidPk())).isFalse();
	}

	/**
	 * Test that notification of a product type updates the last modified time of the products in that type.
	 * @throws InterruptedException if sleep interrupted.
	 */
	@Test
	public void testNotifyProductTypeUpdated() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		service.notifyProductTypeUpdated(product.getProductType());
		final Product updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct.getLastModifiedDate())
			.as("Product's last updated date should be updated")
			.isAfter(lastModified);
	}

	/**
	 * Test that notification of product sku updates the last modified time of the products the sku belongs to.
	 * @throws InterruptedException if sleep interrupted.
	 */
	@Test
	public void testNotifySkuUpdated() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		service.notifySkuUpdated(product.getDefaultSku());
		final Product updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct.getLastModifiedDate().after(lastModified))
			.as("Product's last updated date should be updated")
			.isTrue();
	}

	/**
	 * Test that notification of brand updates the last modified time of products in that brand.
	 * @throws InterruptedException if sleep interrupted.
	 */
	@Test
	public void testNotifyBrandUpdated() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Brand brand = persisterFactory.getCatalogTestPersister().persistProductBrand(scenario.getCatalog(), Utils.uniqueCode("brand"));
		product.setBrand(brand);
		Product updatedProduct = service.saveOrUpdate(product);
		final Date lastModified = updatedProduct.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		service.notifyBrandUpdated(updatedProduct.getBrand());
		updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct.getLastModifiedDate().after(lastModified))
			.as("Product's last updated date should be updated")
			.isTrue();
	}

	/**
	 * Test that notification of category updates the last modified time of product in that category.
	 * @throws InterruptedException if sleep interrupted.
	 */
	@Test
	public void testNotifyCategoryUpdated() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		service.notifyCategoryUpdated(scenario.getCategory());
		final Product updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct.getLastModifiedDate().after(lastModified))
			.as("Product's last updated date should be updated")
			.isTrue();
	}

	/**
	 * Test finding product uids given the store uid.
	 */
	@Test
	public void testFindUidsByStoreUid() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final long storeUid = scenario.getStore().getUidPk();

		final List<Long> storeProducts = service.findUidsByStoreUid(storeUid);
		assertThat(storeProducts).containsOnly(product.getUidPk());
	}

	/**
	 * Test the indicator of whether a product can be deleted.
	 */
	@Test
	public void testCanDelete() {
		Product product1 = this.persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		Product product2 = this.persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		persisterFactory.getOrderTestPersister().createOrderWithSkus(scenario.getStore(), product1.getDefaultSku());

		assertThat(service.canDelete(product1))
			.as("Should not be able to delete first product as it is part of an order")
			.isFalse();

		assertThat(service.canDelete(product2))
			.as("Should be able to delete second product as it is not part of an order")
			.isTrue();
	}

	/**
	 * Test that updating a product updates its last modified time.
	 * @throws InterruptedException  if sleep interrupted.
	 */
	@Test
	public void testUpdateLastModifiedTime() throws InterruptedException {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final Date lastModified = product.getLastModifiedDate();

		// we need to delay long enough to be sure of a difference significant to the timestamp precision.
		Thread.sleep(TIMESTAMP_TOLERANCE);

		service.updateLastModifiedTime(product);
		final Product updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct.getLastModifiedDate()).isAfter(lastModified);
	}

	/**
	 * Tests adding new sku option value to already existing product's product sku.
	 */
	@Test
	public void testAddSkuWithNewSkuOptionValue() {
		final Product product = persisterFactory.getCatalogTestPersister().persistMultiSkuProduct(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(),
			new BigDecimal("100"), Currency.getInstance(Locale.US), "multiSku1", Utils.uniqueCode("prodType123"), "product name",
			TaxTestPersister.TAX_CODE_GOODS, AvailabilityCriteria.ALWAYS_AVAILABLE, 9, Utils.uniqueCode("sku1"));
		final SkuOption skuOption = product.getProductType().getSkuOptions().iterator().next();

		// create new sku option value
		final SkuOptionValue optionValue = new SkuOptionValueImpl();
		final String optionValueKey = Utils.uniqueCode("test_option_value_key");
		optionValue.setOptionValueKey(optionValueKey);
		optionValue.setOrdering(1);
		skuOption.addOptionValue(optionValue);

		// update the sku option
		final SkuOptionService skuService = getBeanFactory().getBean(ContextIdNames.SKU_OPTION_SERVICE);
		final SkuOption updatedSkuOption = skuService.saveOrUpdate(skuOption);

		final ProductSku productSku = getBeanFactory().getBean(ContextIdNames.PRODUCT_SKU);

		final String skuCode = Utils.uniqueCode("skucode1");
		productSku.setSkuCode(skuCode);
		productSku.setSkuOptionValue(updatedSkuOption, optionValueKey);

		product.addOrUpdateSku(productSku);

		productSku.addOrUpdateSkuOption(updatedSkuOption);

		final Product updatedProduct = service.saveOrUpdate(product);

		assertThat(updatedProduct.getProductType().getSkuOptions())
			.hasSize(2)
			.contains(skuOption);

		SkuOptionValue skuOptionValue = updatedProduct.getSkuByCode(skuCode).getSkuOptionValue(updatedSkuOption);
		assertThat(skuOptionValue.getOptionValueKey()).isEqualTo(optionValueKey);

		final Product loadedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(loadedProduct).isNotNull();

		skuOptionValue = loadedProduct.getSkuByCode(skuCode).getSkuOptionValue(updatedSkuOption);
		assertThat(skuOptionValue.getOptionValueKey()).isEqualTo(optionValueKey);
	}

	/**
	 * Tests adding a new sku option value to a product's product sku where the product is still not persisted.
	 *
	 * 1. Create a new multi-sku product with populated required fields
	 * 2. Create a new SKU option value
	 * 3. Add the sku option value to already existing SKU option
	 * 4. Set the sku option value to a product SKU and add it to the product
	 * 5. Persist the product
	 */
	@Test
	public void testPersistMultiSkuProductWithNewSkuAndNewSkuOptionValue() {
		final Product product = persisterFactory.getCatalogTestPersister().createMultiSkuProduct(scenario.getCatalog(), scenario.getCategory(),
			null, Utils.uniqueCode("multiSku1"), "prodType123", "product name", TaxTestPersister.TAX_CODE_GOODS, AvailabilityCriteria.ALWAYS_AVAILABLE, 9, Utils.uniqueCode("sku1"));
		final SkuOption skuOption = product.getProductType().getSkuOptions().iterator().next();

		// create new sku option value
		final SkuOptionValue optionValue = new SkuOptionValueImpl();
		final String optionValueKey = Utils.uniqueCode("test_option_value_key");
		optionValue.setOptionValueKey(optionValueKey);
		optionValue.setOrdering(1);
		skuOption.addOptionValue(optionValue);

		// update the sku option
		final SkuOptionService skuService = getBeanFactory().getBean(ContextIdNames.SKU_OPTION_SERVICE);
		final SkuOption updatedSkuOption = skuService.saveOrUpdate(skuOption);

		final ProductSku productSku = new ProductSkuImpl();
		productSku.initialize();

		final String skuCode = Utils.uniqueCode("skucode1");
		productSku.setSkuCode(skuCode);
		productSku.setSkuOptionValue(updatedSkuOption, optionValueKey);

		product.addOrUpdateSku(productSku);

		Product updatedProduct = service.saveOrUpdate(product);
		assertThat(updatedProduct.getProductType().getSkuOptions())
			.hasSize(2)
			.contains(skuOption);

		SkuOptionValue skuOptionValue = updatedProduct.getSkuByCode(skuCode).getSkuOptionValue(updatedSkuOption);
		assertThat(skuOptionValue.getOptionValueKey()).isEqualTo(optionValueKey);

		updatedProduct = productLookup.findByUid(product.getUidPk());
		assertThat(updatedProduct).isNotNull();

		skuOptionValue = updatedProduct.getSkuByCode(skuCode).getSkuOptionValue(updatedSkuOption);
		assertThat(skuOptionValue.getOptionValueKey()).isEqualTo(optionValueKey);
	}

	/**
	 * Test finding uid by sku code.
	 */
	@Test
	public void testFindUidBySkuCode() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();


		final String productSingleSKUCode = Utils.uniqueCode("prodict");
		final String productSingleSKUskuCode = productSingleSKUCode + "SKU";

		final Product product = catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productSingleSKUCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		final long resultUid = service.findUidBySkuCode(productSingleSKUskuCode);

		assertThat(resultUid).isEqualTo(product.getUidPk());
	}

	/**
	 * Test finding uid by sku code where sku code not found.
	 */
	@Test
	public void testFindUidBySkuCodeNotFound() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();


		final String productSingleSKUCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productSingleSKUCode + "SKU";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productSingleSKUCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		final long resultUid = service.findUidBySkuCode("invalidSkuCode");

		assertThat(resultUid)
			.as("product uid should not be found for sku")
			.isEqualTo(0);
	}

	/**
	 * Test validation of existence of values for all required attributes.
	 */
	@Test(expected = AttributeValueIsRequiredException.class)
	public void testValidateExistenceOfValuesForRequiredNonLocaleDependentAttributesFails() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();


		final String productCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productCode + "SKU";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		//load the product
		Product product = productLookup.findByGuid(productCode);

		assertThat(product).isNotNull();

		// add the new attribute
		String attributeKey = REQUIRED_ATTR_KEY;
		String attributeName = REQUIRED_ATTR_KEY;
		Attribute attr = createUniqueAttribute(catalogTestPersister, attributeKey, attributeName, false);
		catalogTestPersister.assignProductAttributesToProductType(new String[]{attr.getKey()},
			product.getProductType().getName());

		//re-load product after type changed
		product = productLookup.findByGuid(productCode);
		// change the product
		product.setDisplayName("new name", scenario.getCatalog().getDefaultLocale());

		service.saveOrUpdate(product);
	}

	/**
	 * Test validation of existence of values for all required attributes.
	 */
	@Test
	public void testValidateExistenceOfValuesForRequiredNonLocaleDependentAttributesPass() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();


		final String productCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productCode + "SKU";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		//load the product
		Product product = productLookup.findByGuid(productCode);

		assertThat(product).isNotNull();

		// add the new attribute
		Attribute attr = createUniqueAttribute(catalogTestPersister, REQUIRED_ATTR_KEY, REQUIRED_ATTR_KEY, false);
		catalogTestPersister.assignProductAttributesToProductType(new String[]{attr.getKey()},
			product.getProductType().getName());

		//re-load product after type changed
		product = productLookup.findByGuid(productCode);
		// add the value of the attribute for the product
		catalogTestPersister.addAttributeValue(product, attr.getKey(), "new value");
		// change the product
		product.setDisplayName("new name", scenario.getCatalog().getDefaultLocale());
		service.saveOrUpdate(product);
	}

	/**
	 * Test validation of existence of values for all required attributes.
	 * ProductType was changed from the time the product was load and a new required attribute was added to it
	 */
	@Test
	public void testValidateExistenceOfValuesForRequiredAttributesWillNotPickupChangesToProductType() {

		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();


		final String productCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productCode + "SKU";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		//load the product
		final Product product = productLookup.findByGuid(productCode);

		assertThat(product).isNotNull();

		// add the new attribute
		Attribute attr = createUniqueAttribute(catalogTestPersister, REQUIRED_ATTR_KEY, REQUIRED_ATTR_KEY, false);
		catalogTestPersister.assignProductAttributesToProductType(new String[]{attr.getKey()},
			product.getProductType().getName());

		// change the product
		product.setDisplayName("new name", scenario.getCatalog().getDefaultLocale());

		// no exception will be thrown because we are not updating the productType inside a save
		service.saveOrUpdate(product);
	}


	/**
	 * Test validation of existence of values for all required attributes.
	 * ProductType that is loaded into the product has a required attribute that the customer didn't added a value for
	 */
	@Test(expected = AttributeValueIsRequiredException.class)
	public void testValidateExistenceOfValuesForRequiredLocaleDependentAttributesThatAreLocaleDependedFail() {
		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();

		final String productCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productCode + "SKU";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		Product product = productLookup.findByGuid(productCode);

		assertThat(product).isNotNull();

		final Attribute attr = createUniqueAttribute(catalogTestPersister, REQUIRED_ATTR_KEY, REQUIRED_ATTR_KEY, true);

		// refresh the type to be able to see the new attached attributes.
		catalogTestPersister.assignProductAttributesToProductType(new String[]{attr.getKey()},
			product.getProductType().getName());

		// reload the product, because in this test the product has the latest productType
		product = productLookup.findByGuid(productCode);

		product.setDisplayName("new name", scenario.getCatalog().getDefaultLocale());

		// no exception will be thrown because we are not updating the productType inside a save
		service.saveOrUpdate(product);
	}

	/**
	 * Test validation of existence of values for all required attributes.
	 * ProductType that is loaded into the product has a required attribute that the customer didn't added a value for
	 */
	@Test
	public void testValidateExistenceOfValuesForRequiredLocaleDependentAttributesThatAreLocaleDependedPass() {
		final CatalogTestPersister catalogTestPersister = persisterFactory.getCatalogTestPersister();

		final String productCode = Utils.uniqueCode("product");
		final String productSingleSKUskuCode = productCode + "SKU";
		final String attrValue = "new value";

		catalogTestPersister.persistProductWithSku(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse(), BigDecimal.valueOf(33.00), TestDataPersisterFactory.DEFAULT_CURRENCY,
			"brandCode", productCode, "Single-Sku Product", productSingleSKUskuCode, "GOODS", null, 0);

		Product product = productLookup.findByGuid(productCode);

		assertThat(product).isNotNull();

		final Attribute attr = createUniqueAttribute(catalogTestPersister, REQUIRED_ATTR_KEY, REQUIRED_ATTR_KEY, true);

		// refresh the type to be able to see the new attached attributes.
		catalogTestPersister.assignProductAttributesToProductType(new String[]{attr.getKey()},
			product.getProductType().getName());

		// reload the product, because in this test the product has the latest productType
		product = productLookup.findByGuid(productCode);

		// add value only for the master default locale for all supported locale
		final Locale locale = product.getMasterCatalog().getDefaultLocale();
		final List<AttributeValue> list = product.getFullAttributeValues(locale);
		for (final AttributeValue av : list) {
			if (av.getAttribute().getKey().equals(attr.getKey())) {
				av.setStringValue(attrValue);
				product.getAttributeValueMap().put(av.getLocalizedAttributeKey(), av);
				break;
			}
		}
		service.saveOrUpdate(product);
	}

	private Attribute createUniqueAttribute(final CatalogTestPersister catalogTestPersister, final String attributeKey, final String attributeName, final boolean multiLanguage) {
		return catalogTestPersister.persistAttribute(scenario.getCatalog().getCode(), Utils.uniqueCode(attributeKey), Utils.uniqueCode(attributeName),
				AttributeUsageImpl.PRODUCT_USAGE.toString(), AttributeType.SHORT_TEXT.toString(), multiLanguage, true, false);
	}

}

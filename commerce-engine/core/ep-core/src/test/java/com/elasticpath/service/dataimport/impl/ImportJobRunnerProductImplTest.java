package com.elasticpath.service.dataimport.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.junit.Test;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.dataimport.ImportDataType;
import com.elasticpath.domain.dataimport.ImportFault;
import com.elasticpath.domain.dataimport.ImportJob;
import com.elasticpath.domain.dataimport.ImportJobRequest;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeProductCategoryAssociationImpl;
import com.elasticpath.domain.dataimport.impl.ImportJobImpl;
import com.elasticpath.domain.dataimport.impl.ImportJobRequestImpl;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.service.dataimport.ImportGuidHelper;
import com.elasticpath.service.dataimport.ImportService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.test.jmock.AbstractEPServiceTestCase;

/**
 * Test cases for the {@code ImportJobRunnerProductImpl}.  This does more targeted unit tests on individual methods rather
 * than the full process, which is already tested in {@code AbstractImportJobRunnerImplTest}.
 */
public class ImportJobRunnerProductImplTest extends AbstractEPServiceTestCase {

	private static final String PRODUCT_GUID = "PRODUCT_GUID";
	private static final long PRODUCT_UIDPK = 1L;
	private static final String IMPORT_DATA_TYPE_NAME = "Product / Category Association";
	private static final String JOB_PROCESS_ID = "JOB_PROCESS_ID";
	private static final String CATALOG_GUID = "CATALOG_GUID";
	private static final String CATEGORY_GUID = "CATEGORY_GUID";
	private static final String DEFAULT_CATEGORY_GUID = "DEFAULT_CATEGORY_GUID";
	private static final long CATEGORY_UIDPK = 1L;
	private static final long DEFAULT_CATEGORY_UIDPK = 2L;
	private static final String MAPPING_PRODUCT_CODE = "productCode";
	private static final String MAPPING_CATEGORY_CODE = "categoryCode";
	private static final String MAPPING_FEATURED_PRODUCT_ORDER = "featuredProductOrder";
	private static final int MAPPING_PRODUCT_CODE_INDEX = 0;
	private static final int EXPECTED_SIZE_1 = 1;
	private static final int MAPPING_CATEGORY_CODE_INDEX = EXPECTED_SIZE_1;
	private static final int MAPPING_FEATURED_PRODUCT_ORDER_INDEX = 2;
	private static final int TIME_OFFSET = -10;
	private static final String CURRENCY_CAD = "CAD";

	private PersistenceSession mockPersistenceSession;
	private ImportGuidHelper mockImportGuidHelper;
	private TimeService mockTimeService;
	private IndexNotificationService mockIndexNotificationService;
	private ImportService mockImportService;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		mockPersistenceSession = context.mock(PersistenceSession.class);

		mockImportGuidHelper = context.mock(ImportGuidHelper.class);

		mockTimeService = context.mock(TimeService.class);

		mockIndexNotificationService = context.mock(IndexNotificationService.class);

		mockImportService = context.mock(ImportService.class);
	}

	/**
	 * Test case for the findEntityByGuid method on {@code ImportJobRunnerProductImpl}.
	 */
	@Test
	public void testFindEntityByGuid() {
		Product product = new ProductImpl();
		product.setCode(PRODUCT_GUID);

		context.checking(new Expectations() {
			{
				oneOf(mockImportGuidHelper).findProductByGuid(PRODUCT_GUID, false, false, true);
				will(returnValue(product));
			}
		});

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();

		testImportJobRunnerProduct.setImportGuidHelper(mockImportGuidHelper);

		Entity product2 = testImportJobRunnerProduct.findEntityByGuid(PRODUCT_GUID);

		assertNotNull(product2);
		assertTrue(product2 instanceof Product);
		assertEquals(PRODUCT_GUID, product2.getGuid());
	}

	/**
	 * Test case for the updateEntityBeforeSave method on {@code ImportJobRunnerProductImpl}.
	 */
	@Test
	public void testUpdateEntityBeforeSave() {
		Calendar lastModifiedTime = Calendar.getInstance();

		// Checking a time from the past to ensure we're getting the current LMD from the Product.
		lastModifiedTime.roll(Calendar.MINUTE, TIME_OFFSET);

		context.checking(new Expectations() {
			{
				oneOf(mockTimeService).getCurrentTime();
				will(returnValue(lastModifiedTime.getTime()));
			}
		});

		Product product = new ProductImpl();

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();
		testImportJobRunnerProduct.setTimeService(mockTimeService);

		testImportJobRunnerProduct.updateEntityBeforeSave(product);

		assertEquals(product.getLastModifiedDate().getTime(), lastModifiedTime.getTimeInMillis());
	}

	/**
	 * Test case for the saveEntityHelper method on {@code ImportJobRunnerProductImpl} for new Product.
	 */
	@Test
	public void testSaveEntityHelperForInsert() {

		Product product = new ProductImpl();
		product.setCode(PRODUCT_GUID);

		context.checking(new Expectations() {
			{
				allowing(mockPersistenceSession).save(product);
				will(new CustomAction("Update the Product's UIDPK") {
					@Override
					public Object invoke(final Invocation invocation) throws Throwable {
						Object obj = invocation.getParameter(0);
						((Product) obj).setUidPk(PRODUCT_UIDPK);
						return null;
					}
				});

				allowing(mockIndexNotificationService).addNotificationForEntityIndexUpdate(IndexType.PRODUCT, PRODUCT_UIDPK);
			}
		});

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();
		testImportJobRunnerProduct.setIndexNotificationService(mockIndexNotificationService);

		Entity entity = testImportJobRunnerProduct.saveEntityHelper(mockPersistenceSession, product);

		assertNotNull(entity);
		assertNotNull(entity instanceof Product);
		assertEquals(PRODUCT_GUID, ((Product) entity).getCode());
		assertEquals(PRODUCT_UIDPK, ((Product) entity).getUidPk());
	}

	/**
	 * Test case for the saveEntityHelper method on {@code ImportJobRunnerProductImpl} for existing Product.
	 */
	@Test
	public void testSaveEntityHelperForUpdate() {

		Product product = new ProductImpl();
		product.setCode(PRODUCT_GUID);
		product.setUidPk(PRODUCT_UIDPK);

		context.checking(new Expectations() {
			{
				oneOf(mockPersistenceSession).update(product);
				will(returnValue(product));

				oneOf(mockIndexNotificationService).addNotificationForEntityIndexUpdate(IndexType.PRODUCT, PRODUCT_UIDPK);
			}
		});

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();
		testImportJobRunnerProduct.setIndexNotificationService(mockIndexNotificationService);

		Entity entity = testImportJobRunnerProduct.saveEntityHelper(mockPersistenceSession, product);

		assertNotNull(entity);
		assertNotNull(entity instanceof Product);
		assertEquals(PRODUCT_GUID, ((Product) entity).getCode());
		assertEquals(PRODUCT_UIDPK, ((Product) entity).getUidPk());
	}

	/**
	 * Test case for the removeCategoryFromProduct method on {@code ImportJobRunnerProductImpl}.
	 */
	@Test
	public void testRemoveCategoryFromProduct() {

		Catalog catalog = new CatalogImpl();
		catalog.setMaster(true);
		catalog.setCode(CATALOG_GUID);

		ImportJobRequest importJobRequest = new ImportJobRequestImpl();
		ImportJob importJob = new ImportJobImpl();
		importJob.setImportDataTypeName(IMPORT_DATA_TYPE_NAME);
		importJob.setMappings(getProdCatAssignmentMappings());
		importJob.setCatalog(catalog);

		importJobRequest.setImportJob(importJob);

		ImportDataType importDataType = new ImportDataTypeProductCategoryAssociationImpl();

		Category category = new CategoryImpl();
		category.setCode(CATEGORY_GUID);
		category.setGuid(CATALOG_GUID);
		category.setUidPk(CATEGORY_UIDPK);
		category.setCatalog(catalog);

		Category defaultCategory = new CategoryImpl();
		defaultCategory.setCode(DEFAULT_CATEGORY_GUID);
		defaultCategory.setGuid(DEFAULT_CATEGORY_GUID);
		defaultCategory.setUidPk(DEFAULT_CATEGORY_UIDPK);
		defaultCategory.setCatalog(catalog);

		Product product = new ProductImpl();
		product.setCode(PRODUCT_GUID);
		product.setUidPk(PRODUCT_UIDPK);
		product.addCategory(category);
		product.addCategory(defaultCategory);
		product.setCategoryAsDefault(defaultCategory);

		context.checking(new Expectations() {
			{
				oneOf(mockImportService).findImportDataType(IMPORT_DATA_TYPE_NAME);
				will(returnValue(importDataType));

				oneOf(mockImportService).initImportDataTypeLocalesAndCurrencies(importDataType, importJob);
				will(new CustomAction("Sets currencies and locales on the ImportDataType") {
					@Override
					public Object invoke(final Invocation invocation) throws Throwable {
						ImportDataType dataType = (ImportDataType) invocation.getParameter(0);
						dataType.setSupportedCurrencies(Collections.singleton(Currency.getInstance(CURRENCY_CAD)));
						dataType.setSupportedLocales(Collections.singleton(Locale.CANADA));
						dataType.setRequiredCurrency(Currency.getInstance(CURRENCY_CAD));
						dataType.setRequiredLocale(Locale.CANADA);
						return null;
					}
				});

				oneOf(mockImportGuidHelper).findCategoryByGuidAndCatalogGuid(CATEGORY_GUID, CATALOG_GUID);
				will(returnValue(category));

				oneOf(mockImportGuidHelper).isProductGuidExist(PRODUCT_GUID);
				will(returnValue(Boolean.TRUE));

				oneOf(mockPersistenceSession).save(product);

				oneOf(mockIndexNotificationService).addNotificationForEntityIndexUpdate(IndexType.PRODUCT, PRODUCT_UIDPK);
			}
		});

		// Need to init this here after setting up the expectations since importDataType is used in those expectations.
		importDataType.init(null);

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();
		testImportJobRunnerProduct.setImportService(mockImportService);
		testImportJobRunnerProduct.setIndexNotificationService(mockIndexNotificationService);
		testImportJobRunnerProduct.setImportGuidHelper(mockImportGuidHelper);

		testImportJobRunnerProduct.init(importJobRequest, JOB_PROCESS_ID);

		String[] nextLine = {PRODUCT_GUID, CATEGORY_GUID, "0"};

		testImportJobRunnerProduct.callRemoveCategoryFromProduct(mockPersistenceSession, product, nextLine);

		assertEquals(EXPECTED_SIZE_1, product.getCategories().size());
		assertEquals(DEFAULT_CATEGORY_GUID, product.getCategories().iterator().next().getCode());
	}

	/**
	 * Test case for the removeCategoryFromProduct method on {@code ImportJobRunnerProductImpl}.
	 */
	@Test
	public void testRemoveCategoryFromProductWithDefaultOnly() {

		Catalog catalog = new CatalogImpl();
		catalog.setMaster(true);
		catalog.setCode(CATALOG_GUID);

		ImportJobRequest importJobRequest = new ImportJobRequestImpl();
		ImportJob importJob = new ImportJobImpl();
		importJob.setImportDataTypeName(IMPORT_DATA_TYPE_NAME);
		importJob.setMappings(getProdCatAssignmentMappings());
		importJob.setCatalog(catalog);

		importJobRequest.setImportJob(importJob);

		ImportDataType importDataType = new ImportDataTypeProductCategoryAssociationImpl();

		Category category = new CategoryImpl();
		category.setCode(CATEGORY_GUID);
		category.setGuid(CATALOG_GUID);
		category.setUidPk(CATEGORY_UIDPK);
		category.setCatalog(catalog);

		Product product = new ProductImpl();
		product.setCode(PRODUCT_GUID);
		product.setUidPk(PRODUCT_UIDPK);
		product.addCategory(category);
		product.setCategoryAsDefault(category);

		context.checking(new Expectations() {
			{
				oneOf(mockImportService).findImportDataType(IMPORT_DATA_TYPE_NAME);
				will(returnValue(importDataType));

				oneOf(mockImportService).initImportDataTypeLocalesAndCurrencies(importDataType, importJob);
				will(new CustomAction("Sets currencies and locales on the ImportDataType") {
					@Override
					public Object invoke(final Invocation invocation) throws Throwable {
						ImportDataType dataType = (ImportDataType) invocation.getParameter(0);
						dataType.setSupportedCurrencies(Collections.singleton(Currency.getInstance(CURRENCY_CAD)));
						dataType.setSupportedLocales(Collections.singleton(Locale.CANADA));
						dataType.setRequiredCurrency(Currency.getInstance(CURRENCY_CAD));
						dataType.setRequiredLocale(Locale.CANADA);
						return null;
					}
				});

				oneOf(mockImportGuidHelper).findCategoryByGuidAndCatalogGuid(CATEGORY_GUID, CATALOG_GUID);
				will(returnValue(category));

				oneOf(mockImportGuidHelper).isProductGuidExist(PRODUCT_GUID);
				will(returnValue(Boolean.TRUE));

				// There should be no PersistenceSession.save().
				// There should be no IndexNotificationService.addNotificationForEntityIndexUpdate().
			}
		});

		// Need to init this here after setting up the expectations since importDataType is used in those expectations.
		importDataType.init(null);

		TestImportJobRunnerProductImpl testImportJobRunnerProduct = new TestImportJobRunnerProductImpl();
		testImportJobRunnerProduct.setImportService(mockImportService);
		testImportJobRunnerProduct.setIndexNotificationService(mockIndexNotificationService);
		testImportJobRunnerProduct.setImportGuidHelper(mockImportGuidHelper);

		testImportJobRunnerProduct.init(importJobRequest, JOB_PROCESS_ID);

		String[] nextLine = {PRODUCT_GUID, CATEGORY_GUID, "0"};

		testImportJobRunnerProduct.callRemoveCategoryFromProduct(mockPersistenceSession, product, nextLine);

		assertEquals(EXPECTED_SIZE_1, product.getCategories().size());
		assertEquals(CATEGORY_GUID, product.getCategories().iterator().next().getCode());
	}

	private Map<String, Integer> getProdCatAssignmentMappings() {
		Map<String, Integer> mappings = new HashMap<>();

		mappings.put(MAPPING_PRODUCT_CODE, MAPPING_PRODUCT_CODE_INDEX);
		mappings.put(MAPPING_CATEGORY_CODE, MAPPING_CATEGORY_CODE_INDEX);
		mappings.put(MAPPING_FEATURED_PRODUCT_ORDER, MAPPING_FEATURED_PRODUCT_ORDER_INDEX);

		return mappings;
	}

	/**
	 * Helper class to allow for more targeted unit testing.  Wrapper methods for the individual protected methods in the ImportJobRunnerProductImpl
	 * are implemented to allow the test cases to call the wrapped, protected methods without testing the entire job runner logic.
	 */
	private class TestImportJobRunnerProductImpl extends ImportJobRunnerProductImpl {
		/**
		 * Calls the protected findEntityByGuid method.
		 * @param guid the entity GUID
		 * @return the entity
		 */
		Entity callFindEntityByGuid(final String guid) {
			return super.findEntityByGuid(guid);
		}

		/**
		 * Calls the protected callCreateNewEntity method.
		 * @param baseObject the expected base object
		 * @return the created entity
		 */
		Entity callCreateNewEntity(final Object baseObject) {
			return super.createNewEntity(baseObject);
		}

		/**
		 * Calls the protected callUpdateEntityBeforeSave method.
		 * @param entity the entity
		 */
		void callUpdateEntityBeforeSave(final Entity entity) {
			super.updateEntityBeforeSave(entity);
		}

		/**
		 * Calls the protected callSaveEntityHelper method.
		 * @param session the persistence session
		 * @param entity the entity
		 * @return the saved entity
		 */
		Entity callSaveEntityHelper(final PersistenceSession session, final Entity entity) {
			return super.saveEntityHelper(session, entity);
		}

		/**
		 * Calls the protected callRemoveCategoryFromProduct method.
		 * @param session the persistence session
		 * @param product the {@code Product}
		 * @param nextLine the import line
		 */
		void callRemoveCategoryFromProduct(final PersistenceSession session, final Product product, final String[] nextLine) {
			super.removeCategoryFromProduct(session, product, nextLine);
		}

		/**
		 * Calls the protected callValidateChangeSetStatus method.
		 * @param nextLine the import line
		 * @param rowNumber the current row number
		 * @param faults collection of faults
		 * @param persistenceObject the persisted object
		 */
		void callValidateChangeSetStatus(
				final String[] nextLine, final int rowNumber, final List<ImportFault> faults, final Persistable persistenceObject) {
			super.validateChangeSetStatus(nextLine, rowNumber, faults, persistenceObject);
		}
	}
}

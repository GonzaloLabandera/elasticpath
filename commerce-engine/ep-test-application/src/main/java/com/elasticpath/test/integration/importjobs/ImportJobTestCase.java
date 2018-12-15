/*
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration.importjobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.dataimport.ImportBadRow;
import com.elasticpath.domain.dataimport.ImportDataType;
import com.elasticpath.domain.dataimport.ImportJob;
import com.elasticpath.domain.dataimport.ImportJobRequest;
import com.elasticpath.domain.dataimport.ImportJobStatus;
import com.elasticpath.domain.dataimport.ImportType;
import com.elasticpath.domain.dataimport.impl.AbstractImportTypeImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeCategoryImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeCustomerAddressImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeCustomerImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeInventoryImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeProductAssociationImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeProductCategoryAssociationImpl;
import com.elasticpath.domain.dataimport.impl.ImportJobImpl;
import com.elasticpath.domain.dataimport.impl.ImportJobRequestImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.dataimport.ImportService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.ImportJobScenario;
import com.elasticpath.test.util.Utils;

/**
 * Provides additional setup and services for import testing. Note that the Db is reset for each test so that the data imported after each test is
 * guaranteed not to exist in the database.<br>
 * NOTE: CSV files to be imported can be found in ${EP_DIR}/integrationtests/assets/import directory.
 */
@SuppressWarnings({ "PMD.AbstractNaming", "PMD.GodClass" })
public abstract class ImportJobTestCase extends BasicSpringContextTest {

	private static final String PRODUCT_SKU_PREFIX = "ProductSku - ";

	private static final String PRODUCT_PREFIX = "Product - ";

	/** CSV file text strings qualifier. */
	public static final char CSV_FILE_TEXT_QUALIFIER = '"';

	/** CSV file column delimiter. */
	public static final char CSV_FILE_COL_DELIMETER = '|';

	/** Import Data type name. */
	public static final String DEFAULT_IMPORT_DATA_TYPE = "Sample Data Type";

	@Autowired protected ImportService importService;

	protected ImportJobScenario scenario;

	protected TestDataPersisterFactory persisterFactory;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 * @throws Exception if an error occurs
	 */
	@Before
	public void setUp() throws Exception {
		createTestData();
		persisterFactory = getTac().getPersistersFactory();
	}

	protected void createTestData() {
		scenario = getTac().useScenario(ImportJobScenario.class);
	}

	/**
	 * Helper to create an import jobs for testing purposes.
	 *
	 * @param associatedDomainObject the associate domain object (store, catalog or warehouse)
	 * @param name the name of the import job
	 * @param csvFileName the name of the csv file
	 * @param importType the import type
	 * @param importDataTypeName the import data type name. Should be only the name of the import file without any parent folders specified.
	 * @param mappings the map of import mappings
	 * @return the persisted importJob
	 */
	protected ImportJob createSimpleImportJob(final Persistable associatedDomainObject, final String name, final String csvFileName,
			final ImportType importType, final String importDataTypeName, final Map<String, Integer> mappings) {
		ImportJob importJob = getBeanFactory().getBean(ContextIdNames.IMPORT_JOB);
		if (associatedDomainObject instanceof Store) {
			importJob.setStore((Store) associatedDomainObject);
		} else if (associatedDomainObject instanceof Catalog) {
			importJob.setCatalog((Catalog) associatedDomainObject);
		} else if (associatedDomainObject instanceof Warehouse) {
			importJob.setWarehouse((Warehouse) associatedDomainObject);
		} else {
			throw new IllegalArgumentException("Store, Catalog or Warehouse object expected for associatedDomainObject.");
		}

		importJob.setImportDataTypeName(importDataTypeName);
		importJob.setImportType(importType);
		importJob.setName(name);
		importJob.setCsvFileName(csvFileName);
		importJob.setCsvFileColDelimeter(CSV_FILE_COL_DELIMETER);
		importJob.setCsvFileTextQualifier(CSV_FILE_TEXT_QUALIFIER);
		importJob.setMappings(mappings);

		return importService.saveOrUpdateImportJob(importJob);
	}

	/**
	 * Helper to create fake import job, which means that most of parameters are predefined.<br>
	 * Such a job is useful to test basic import service operations like import job insertion or removing.
	 *
	 * @param importType the job's import type.
	 * @return transient initialized import job.
	 */
	protected ImportJob createDefaultImportJob(final ImportType importType) {
		final ImportJob importJob = new ImportJobImpl();
		importJob.setName(Utils.uniqueCode("ImportJob"));
		importJob.setCsvFileName(Utils.uniqueCode("CsvName"));
		importJob.setCatalog(scenario.getCatalog());
		importJob.setStore(scenario.getStore());
		importJob.setWarehouse(scenario.getWarehouse());
		importJob.initialize();
		importJob.setCsvFileColDelimeter(CSV_FILE_COL_DELIMETER);
		importJob.setCsvFileTextQualifier(CSV_FILE_TEXT_QUALIFIER);

		importJob.setImportType(importType);
		importJob.setImportDataTypeName(DEFAULT_IMPORT_DATA_TYPE);
		importJob.setMappings(new HashMap<>());
		return importJob;
	}

	public List<ImportBadRow> executeImportJob(final ImportJob importJob) {
		CmUser initiator = scenario.getCmUser();

		ImportJobRequest importJobProcessRequest = new ImportJobRequestImpl();
		importJobProcessRequest.setImportJob(importJob);
		importJobProcessRequest.setImportSource(importJob.getCsvFileName());
		importJobProcessRequest.setInitiator(initiator);
		importJobProcessRequest.setReportingLocale(Locale.getDefault());

		ImportJobStatus status = importService.scheduleImport(importJobProcessRequest);

		status = new ImportJobProcessorLauncher(getBeanFactory()).launchAndWaitToFinish(status);

		return status.getBadRows();
	}

	protected ImportDataType findByType(final List<ImportDataType> importDataTypes, final Class<? extends ImportDataType> type) {

		for (ImportDataType importDataType : importDataTypes) {
			if (type.isInstance(importDataType)) {
				return importDataType;
			}
		}
		return null;
	}

	protected ImportJob createInsertCustomerImportJob() {
		return createInsertCustomerImportJob("customer_insert.csv");
	}

	protected ImportJob createInsertCustomerImportInvalidJob() {
		return createInsertCustomerImportJob("customer_insert_invalid.csv");
	}

	protected ImportJob createInsertCustomerImportJob(final String csvFileName) {
		List<ImportDataType> importDataTypes = importService.getCustomerImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCustomerImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("guid", 1)
				.put("userId", 2)
				.put("CP_FIRST_NAME", 3)
				.put("CP_LAST_NAME", 4)
				.put("CP_EMAIL", 5)
				.put("CP_ANONYMOUS_CUST", 6)
				.put("CP_HTML_EMAIL", 7)
				.put("status", 8)
				.put("creationDate", 9)
				.put("CP_PHONE", 10)
				.build();

		return createSimpleImportJob(scenario.getStore(), Utils.uniqueCode("Insert Customers"),
				csvFileName, AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertUpdateCustomerImportJob() {
		List<ImportDataType> importDataTypes = importService.getCustomerImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCustomerImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("guid", 1)
				.put("userId", 2)
				.put("CP_FIRST_NAME", 3)
				.put("CP_LAST_NAME", 4)
				.put("CP_EMAIL", 5)
				.put("CP_ANONYMOUS_CUST", 6)
				.put("CP_HTML_EMAIL", 7)
				.put("status", 8)
				.put("creationDate", 9)
				.put("CP_PHONE", 10)
				.build();

		return createSimpleImportJob(scenario.getStore(), Utils.uniqueCode("Insert and Update Customers"),
				"customer_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateCustomerImportJob() {
		List<ImportDataType> importDataTypes = importService.getCustomerImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCustomerImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("guid", 1)
				.put("CP_EMAIL", 2)
				.put("CP_PHONE", 3)
				.build();

		return createSimpleImportJob(scenario.getStore(), Utils.uniqueCode("Update Customers"),
				"customer_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createDeleteCustomerImportJob() {
		List<ImportDataType> importDataTypes = importService.getCustomerImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCustomerImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("guid", 1)
				.build();

		return createSimpleImportJob(scenario.getStore(), Utils.uniqueCode("Delete Customers"),
				"customer_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertCategoriesImportJob() {

		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCategoryImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("categoryCode", 1)
				.put("parentCategoryCode", 2)
				.put("displayName(en)", 3)
				.put("enableDate", 4)
				.put("disableDate", 5)
				.put("storeVisible", 6)
				.put("ordering", 7)
				.put("seoUrl(en)", 8)
				.put("seoTitle(en)", 9)
				.put("seoKeyWords(en)", 10)
				.put("seoDescription(en)", 11)
				.put("catImage", 12)
				.put("catDescription(en)", 13)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert Categories"),
				"categories_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertUpdateCategoriesImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = importDataTypes.get(0).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("categoryCode", 1)
				.put("parentCategoryCode", 2)
				.put("displayName(en)", 3)
				.put("enableDate", 4)
				.put("disableDate", 5)
				.put("storeVisible", 6)
				.put("ordering", 7)
				.put("seoUrl(en)", 8)
				.put("seoTitle(en)", 9)
				.put("seoKeyWords(en)", 10)
				.put("seoDescription(en)", 11)
				.put("catImage", 12)
				.put("catDescription(en)", 13)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update Categories"),
				"categories_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateCategoriesImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = importDataTypes.get(0).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("categoryCode", 1)
				.put("displayName(en)", 2)
				.put("catDescription(en)", 3)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update Categories"),
				"categories_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createDeleteCategoriesImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = importDataTypes.get(0).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("categoryCode", 1)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Delete Categories"),
				"categories_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, importDataTypeName, map);
	}

	/**
	 * The job should contain relatively many row in order to keep import pending before second import. <br>
	 * Required for concurrent import and similar tests.
	 *
	 * @return the import job.
	 */
	protected ImportJob createInsertCategoriesImportJobManyRows() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = importDataTypes.get(0).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("categoryCode", 1)
				.put("displayName(en)", 2)
				.put("parentCategoryCode", 3)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Concurrent Import Categories"),
				"categories_insert_concurrent.csv", AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertCategoryAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductCategoryAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("categoryCode", 2)
				.put("featuredProductOrder", 3)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert Categories"),
				"categoryassociation_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertUpdateCategoryAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductCategoryAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("categoryCode", 2)
				.put("featuredProductOrder", 3)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update Categories"),
				"categoryassociation_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateCategoryAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductCategoryAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("featuredProductOrder", 2)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update Categories"),
				"categoryassociation_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createDeleteCategoryAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductCategoryAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Delete Categories"),
				"categoryassociation_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateCustomerAddressImportJob() {
		List<ImportDataType> importDataTypes = importService.getCustomerImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeCustomerAddressImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("guid", 1)
				.put("customerGuid", 2)
				.put("firstName", 3)
				.put("lastName", 4)
				.put("phoneNumber", 5)
				.put("street1", 6)
				.put("street2", 7)
				.put("city", 8)
				.put("subCountry", 9)
				.put("country", 10)
				.put("zipOrPostalCode", 11)
				.build();

		return createSimpleImportJob(scenario.getStore(), Utils.uniqueCode("Update Customer Addresses"),
				"customeraddress_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertInventoryImportJob() {
		List<ImportDataType> importDataTypes = importService.getWarehouseImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeInventoryImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productSku", 1)
				.put("quantityOnHand", 2)
				.put("reservedQuantity", 3)
				.put("reorderMinimum", 4)
				.put("reorderQuantity", 5)
				.put("restockDate", 6)
				.build();

		return createSimpleImportJob(scenario.getWarehouse(), Utils.uniqueCode("Insert Inventories"),
				"inventory_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertUpdateInventoryImportJob() {
		List<ImportDataType> importDataTypes = importService.getWarehouseImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeInventoryImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productSku", 1)
				.put("quantityOnHand", 2)
				.put("reservedQuantity", 3)
				.put("reorderMinimum", 4)
				.put("reorderQuantity", 5)
				.put("restockDate", 6)
				.build();

		return createSimpleImportJob(scenario.getWarehouse(), Utils.uniqueCode("Insert and Update Categories"),
				"inventory_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateInventoryImportJob() {
		List<ImportDataType> importDataTypes = importService.getWarehouseImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeInventoryImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productSku", 1)
				.put("quantityOnHand", 2)
				.put("reservedQuantity", 3)
				.put("reorderMinimum", 4)
				.put("reorderQuantity", 5)
				.put("restockDate", 6)
				.build();

		return createSimpleImportJob(scenario.getWarehouse(), Utils.uniqueCode("Update Categories"),
				"inventory_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createDeleteInventoryImportJob() {
		List<ImportDataType> importDataTypes = importService.getWarehouseImportDataTypes();
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeInventoryImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productSku", 1)
				.build();

		return createSimpleImportJob(scenario.getWarehouse(), Utils.uniqueCode("Delete Categories"),
				"inventory_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertProductAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("sourceProductCode", 1)
				.put("targetProductCode", 2)
				.put("associationType", 3)
				.put("sourceProductDependant", 4)
				.put("defaultQuantity", 5)
				.put("ordering", 6)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert Product Association"),
				"productassociation_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertUpdateProductAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("sourceProductCode", 1)
				.put("targetProductCode", 2)
				.put("associationType", 3)
				.put("sourceProductDependant", 4)
				.put("defaultQuantity", 5)
				.put("ordering", 6)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update Product Associations"),
				"productassociation_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createClearThenInsertProductAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("sourceProductCode", 1)
				.put("targetProductCode", 2)
				.put("associationType", 3)
				.put("sourceProductDependant", 4)
				.put("defaultQuantity", 5)
				.put("ordering", 6)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Clear then Insert Product Associations"),
				"productassociation_clear_then_insert.csv", AbstractImportTypeImpl.CLEAR_INSERT_TYPE, importDataTypeName, map);
	}

	protected ImportJob createUpdateProductAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("sourceProductCode", 1)
				.put("defaultQuantity", 2)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update Product Associations"),
				"productassociation_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createDeleteProductAssociationImportJob() {
		List<ImportDataType> importDataTypes = importService.getCatalogImportDataTypes(scenario.getCatalog().getUidPk());
		String importDataTypeName = findByType(importDataTypes, ImportDataTypeProductAssociationImpl.class).getName();

		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("sourceProductCode", 1)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Delete Product Associations"),
				"productassociation_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, importDataTypeName, map);
	}

	protected ImportJob createInsertProductSkuImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("image", 3)
				.put("productCode", 4)
				.put("Color", 5)
				.put("listPrice(USD)", 6)
				.put("salePrice(USD)", 7)
				.put("width", 8)
				.put("length", 9)
				.put("height", 10)
				.put("weight", 11)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert ProductSku"),
				"productsku_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, PRODUCT_SKU_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertUpdateProductSkuImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("image", 3)
				.put("productGuid", 4)
				.put("color", 5)
				.put("listPrice_USD", 6)
				.put("salePrice_USD", 7)
				.put("width", 8)
				.put("length", 9)
				.put("height", 10)
				.put("weight", 11)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update ProductSku"),
				"productsku_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, PRODUCT_SKU_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createUpdateProductSkuImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("image", 3)
				.put("productGuid", 4)
				.put("color", 5)
				.put("listPrice_USD", 6)
				.put("salePrice_USD", 7)
				.put("width", 8)
				.put("length", 9)
				.put("height", 10)
				.put("weight", 11)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update Categories"),
				"productsku_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, PRODUCT_SKU_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("image", 7)
				.put("defaultCategoryCode", 8)
				.put("brandCode", 9)
				.put("listPrice(USD)", 10)
				.put("salePrice(USD)", 11)
				.put("seoTitle(en)", 12)
				.put("seoTitle(fr)", 13)
				.put("seoKeyWords(en)", 14)
				.put("seoKeyWords(fr)", 15)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.put("width", 18)
				.put("length", 19)
				.put("height", 20)
				.put("weight", 21)
				.put("A00996", 22)
				.put("A00995(en)", 23)
				.put("A00995(fr)", 24)
				.put("A00601", 25)
				.put("A00600", 26)
				.put("A01006", 27)
				.put("A04393(en)", 28)
				.put("A04393(fr)", 29)
				.put("A01278", 30)
				.put("A01003(en)", 31)
				.put("A01003(fr)", 32)
				.put("A00430(en)", 33)
				.put("A00430(fr)", 34)
				.put("availabilityCriteria", 38)
				.put("preBackOrderLimit", 39)
				.build();


		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert Products"),
				"product_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertUpdateProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("image", 7)
				.put("defaultCategoryCode", 8)
				.put("brandCode", 9)
				.put("listPrice(USD)", 10)
				.put("salePrice(USD)", 11)
				.put("seoTitle(en)", 12)
				.put("seoTitle(fr)", 13)
				.put("seoKeyWords(en)", 14)
				.put("seoKeyWords(fr)", 15)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.put("width", 18)
				.put("length", 19)
				.put("height", 20)
				.put("weight", 21)
				.put("A00996", 22)
				.put("A00995(en)", 23)
				.put("A00995(fr)", 24)
				.put("A00601", 25)
				.put("A00600", 26)
				.put("A01006", 27)
				.put("A04393(en)", 28)
				.put("A04393(fr)", 29)
				.put("A01278", 30)
				.put("A01003(en)", 31)
				.put("A01003(fr)", 32)
				.put("A00430(en)", 33)
				.put("A00430(fr)", 34)
				.put("availabilityCriteria", 38)
				.put("preBackOrderLimit", 39)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update Product"),
				"product_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createUpdateProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update Product"),
				"product_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createDeleteProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Delete Product"),
				"product_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertProductMultiCatalogImportJob(final Catalog catalog) {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("image", 7)
				.put("defaultCategoryCode", 8)
				.put("brandCode", 9)
				.put("listPrice(USD)", 10)
				.put("salePrice(USD)", 11)
				.put("seoTitle(en)", 12)
				.put("seoTitle(fr)", 13)
				.put("seoKeyWords(en)", 14)
				.put("seoKeyWords(fr)", 15)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.put("width", 18)
				.put("length", 19)
				.put("height", 20)
				.put("weight", 21)
				.put("A00996", 22)
				.put("A00995(en)", 23)
				.put("A00995(fr)", 24)
				.put("A00601", 25)
				.put("A00600", 26)
				.put("A01006", 27)
				.put("A04393(en)", 28)
				.put("A04393(fr)", 29)
				.put("A01278", 30)
				.put("A01003(en)", 31)
				.put("A01003(fr)", 32)
				.put("A00430(en)", 33)
				.put("A00430(fr)", 34)
				.put("availabilityCriteria", 38)
				.put("preBackOrderLimit", 39)
				.build();

		return createSimpleImportJob(catalog, Utils.uniqueCode("Insert Products"),
				"product_multi_catalog_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertUpdateProductMultiCatalogImportJob(final Catalog catalog) {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("skuCode", 1)
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("image", 7)
				.put("defaultCategoryCode", 8)
				.put("brandCode", 9)
				.put("listPrice(USD)", 10)
				.put("salePrice(USD)", 11)
				.put("seoTitle(en)", 12)
				.put("seoTitle(fr)", 13)
				.put("seoKeyWords(en)", 14)
				.put("seoKeyWords(fr)", 15)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.put("width", 18)
				.put("length", 19)
				.put("height", 20)
				.put("weight", 21)
				.put("A00996", 22)
				.put("A00995(en)", 23)
				.put("A00995(fr)", 24)
				.put("A00601", 25)
				.put("A00600", 26)
				.put("A01006", 27)
				.put("A04393(en)", 28)
				.put("A04393(fr)", 29)
				.put("A01278", 30)
				.put("A01003(en)", 31)
				.put("A01003(fr)", 32)
				.put("A00430(en)", 33)
				.put("A00430(fr)", 34)
				.put("availabilityCriteria", 38)
				.put("preBackOrderLimit", 39)
				.build();

		return createSimpleImportJob(catalog, Utils.uniqueCode("Insert and Update Product"),
				"product_multi_catalog_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createUpdateProductMultiCatalogImportJob(final Catalog catalog) {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 2)
				.put("description(en)", 3)
				.put("description(fr)", 4)
				.put("displayName(en)", 5)
				.put("displayName(fr)", 6)
				.put("defaultCategoryCode", 8)
				.put("seoDescription(en)", 16)
				.put("seoDescription(fr)", 17)
				.build();

		return createSimpleImportJob(catalog, Utils.uniqueCode("Update Product"),
				"product_multi_catalog_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createDeleteProductMultiCatalogImportJob(final Catalog catalog) {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.build();

		return createSimpleImportJob(catalog, Utils.uniqueCode("Delete Product"),
				"product_multi_catalog_delete.csv", AbstractImportTypeImpl.DELETE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.SINGLE_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertMultiskuProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("description(en)", 2)
				.put("description(fr)", 3)
				.put("displayName(en)", 4)
				.put("displayName(fr)", 5)
				.put("image", 6)
				.put("defaultCategoryCode", 7)
				.put("brandCode", 8)
				.put("listPrice(USD)", 9)
				.put("salePrice(USD)", 10)
				.put("seoTitle(en)", 11)
				.put("seoTitle(fr)", 12)
				.put("seoKeyWords(en)", 13)
				.put("seoKeyWords(fr)", 14)
				.put("seoDescription(en)", 15)
				.put("seoDescription(fr)", 16)
				.put("A00871(en)", 17)
				.put("A00871(fr)", 18)
				.put("A00870(en)", 19)
				.put("A00870(fr)", 20)
				.put("A00276(en)", 21)
				.put("A00276(fr)", 22)
				.put("A03378(en)", 23)
				.put("A03378(fr)", 24)
				.put("A03379", 25)
				.put("A00341", 26)
				.put("A03380", 27)
				.put("A02028", 28)
				.put("A01015(en)", 29)
				.put("A01015(fr)", 30)
				.put("A02805", 31)
				.put("A03190", 32)
				.put("A00140", 33)
				.put("A01584(en)", 34)
				.put("A01584(fr)", 35)
				.put("A00152(en)", 36)
				.put("A00152(fr)", 37)
				.put("A00138(en)", 38)
				.put("A00138(fr)", 39)
				.put("A01206(en)", 40)
				.put("A01206(fr)", 41)
				.put("A00409(en)", 42)
				.put("A00409(fr)", 43)
				.put("A00413", 44)
				.put("A00601", 45)
				.put("A00600", 46)
				.put("A00983", 47)
				.put("A00985", 48)
				.put("A00984", 49)
				.put("A01071", 50)
				.put("A00981(en)", 51)
				.put("A00981(fr)", 52)
				.put("A00373(en)", 53)
				.put("A00373(fr)", 54)
				.put("A03519", 55)
				.put("A03517", 56)
				.put("A01376", 57)
				.put("A00551", 58)
				.put("A03497", 59)
				.put("A02638", 60)
				.put("A01244(en)", 61)
				.put("A01244(fr)", 62)
				.put("A00430(en)", 63)
				.put("A00430(fr)", 64)
				.put("A00258", 65)
				.put("A01381(en)", 66)
				.put("A01381(fr)", 67)
				.put("A00260", 68)
				.put("A00652", 69)
				.put("A00548", 70)
				.put("A00919", 71)
				.put("A01570(en)", 72)
				.put("A01570(fr)", 73)
				.put("A01207(en)", 74)
				.put("A01207(fr)", 75)
				.put("A00537", 76)
				.put("A00256(en)", 77)
				.put("A00256(fr)", 78)
				.put("A00920(en)", 79)
				.put("A00920(fr)", 80)
				.put("A00556(en)", 81)
				.put("A00556(fr)", 82)
				.put("availabilityCriteria", 86)
				.put("preBackOrderLimit", 87)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert MultiskuProduct"),
				"productmultisku_insert.csv", AbstractImportTypeImpl.INSERT_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	protected ImportJob createInsertUpdateMultiskuProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("description(en)", 2)
				.put("description(fr)", 3)
				.put("displayName(en)", 4)
				.put("displayName(fr)", 5)
				.put("image", 6)
				.put("defaultCategoryCode", 7)
				.put("brandCode", 8)
				.put("listPrice(USD)", 9)
				.put("salePrice(USD)", 10)
				.put("seoTitle(en)", 11)
				.put("seoTitle(fr)", 12)
				.put("seoKeyWords(en)", 13)
				.put("seoKeyWords(fr)", 14)
				.put("seoDescription(en)", 15)
				.put("seoDescription(fr)", 16)
				.put("A00871(en)", 17)
				.put("A00871(fr)", 18)
				.put("A00870(en)", 19)
				.put("A00870(fr)", 20)
				.put("A00276(en)", 21)
				.put("A00276(fr)", 22)
				.put("A03378(en)", 23)
				.put("A03378(fr)", 24)
				.put("A03379", 25)
				.put("A00341", 26)
				.put("A03380", 27)
				.put("A02028", 28)
				.put("A01015(en)", 29)
				.put("A01015(fr)", 30)
				.put("A02805", 31)
				.put("A03190", 32)
				.put("A00140", 33)
				.put("A01584(en)", 34)
				.put("A01584(fr)", 35)
				.put("A00152(en)", 36)
				.put("A00152(fr)", 37)
				.put("A00138(en)", 38)
				.put("A00138(fr)", 39)
				.put("A01206(en)", 40)
				.put("A01206(fr)", 41)
				.put("A00409(en)", 42)
				.put("A00409(fr)", 43)
				.put("A00413", 44)
				.put("A00601", 45)
				.put("A00600", 46)
				.put("A00983", 47)
				.put("A00985", 48)
				.put("A00984", 49)
				.put("A01071", 50)
				.put("A00981(en)", 51)
				.put("A00981(fr)", 52)
				.put("A00373(en)", 53)
				.put("A00373(fr)", 54)
				.put("A03519", 55)
				.put("A03517", 56)
				.put("A01376", 57)
				.put("A00551", 58)
				.put("A03497", 59)
				.put("A02638", 60)
				.put("A01244(en)", 61)
				.put("A01244(fr)", 62)
				.put("A00430(en)", 63)
				.put("A00430(fr)", 64)
				.put("A00258", 65)
				.put("A01381(en)", 66)
				.put("A01381(fr)", 67)
				.put("A00260", 68)
				.put("A00652", 69)
				.put("A00548", 70)
				.put("A00919", 71)
				.put("A01570(en)", 72)
				.put("A01570(fr)", 73)
				.put("A01207(en)", 74)
				.put("A01207(fr)", 75)
				.put("A00537", 76)
				.put("A00256(en)", 77)
				.put("A00256(fr)", 78)
				.put("A00920(en)", 79)
				.put("A00920(fr)", 80)
				.put("A00556(en)", 81)
				.put("A00556(fr)", 82)
				.put("availabilityCriteria", 86)
				.put("preBackOrderLimit", 87)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Insert and Update MultiskuProduct"),
				"productmultisku_insert_update.csv", AbstractImportTypeImpl.INSERT_UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	/**
	 *
	 * @return ImportJob instance
	 */
	protected ImportJob createUpdateMultiskuProductImportJob() {
		ImmutableMap<String, Integer> map = new ImmutableMap.Builder<String, Integer>()
				.put("productCode", 1)
				.put("description(en)", 2)
				.put("description(fr)", 3)
				.put("displayName(en)", 4)
				.put("displayName(fr)", 5)
				.put("seoDescription(en)", 15)
				.put("seoDescription(fr)", 16)
				.build();

		return createSimpleImportJob(scenario.getCatalog(), Utils.uniqueCode("Update MultiskuProduct"),
				"productmultisku_update.csv", AbstractImportTypeImpl.UPDATE_TYPE, PRODUCT_PREFIX
						+ ImportJobScenario.MULTI_SKU_PRODUCT_TYPE, map);
	}

	protected Date string2Date(final String dateString, final Locale locale) throws ParseException {
		return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", locale).parse(dateString);
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.collect.ImmutableMap;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.ThreadLocalMap;
import com.elasticpath.commons.enums.OperationEnum;
import com.elasticpath.commons.util.TestDomainMarshaller;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.adapters.products.ProductAdapter;
import com.elasticpath.importexport.common.adapters.products.ProductBundleAdapter;
import com.elasticpath.importexport.common.dto.products.ProductAvailabilityDTO;
import com.elasticpath.importexport.common.dto.products.ProductDTO;
import com.elasticpath.importexport.common.dto.products.bundles.ProductBundleDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.DependentElementConfiguration;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Import Product Steps.
 */
public class ImportProductSteps {

	private static final String PRODUCTS_IMPORT_FILE = "products.xml";

	private static final String BUNDLES_IMPORT_FILE = "bundles.xml";

	private static final String MANIFEST_IMPORT_FILE = "manifest.xml";

	private static int runNumber = 1;

	@Autowired
	private ProductLookup productLookup;

	@Autowired
	private ImportController importController;

	@Autowired
	private ProductAdapter productAdapter;

	@Autowired
	private ProductBundleAdapter productBundleAdapter;

	@Autowired
	private ThreadLocalMap<String, Object> persistenceListenerMetadataMap;

	private final TestDomainMarshaller testDomainMarshaller = new TestDomainMarshaller();

	/**
	 * Imports the product into the change set and overrides the visibility.
	 *
	 * @param productCode   the product code
	 * @param storeVisible  the store visible flag
	 * @param changeSetGuid the change set
	 * @throws Exception in case of error
	 */
	@When("^I import product (\\w+) with visibility set to (\\w+) into change set (\\w+)$")
	public void importProduct(final String productCode, final boolean storeVisible, final String changeSetGuid) throws Exception {
		final File importDirectory = createImportDirectory();
		writeManifestFile(importDirectory, PRODUCTS_IMPORT_FILE);

		final Product product = productLookup.findByGuid(productCode);
		final ProductDTO productDTO = new ProductDTO();
		productAdapter.populateDTO(product, productDTO);

		final ProductAvailabilityDTO productAvailability = productDTO.getProductAvailability();
		productAvailability.setStorevisible(storeVisible);

		writeProductsFile(importDirectory, productDTO);

		configureImportChangeSet(changeSetGuid);
		executeImport(importDirectory);
	}

	/**
	 * Imports the product bundle into the change set and overrides the visibility.
	 *
	 * @param productBundleCode the product bundle code
	 * @param storeVisible      the store visible flag
	 * @param changeSetGuid     the change set
	 * @throws Exception in case of error
	 */
	@When("^I import product bundle (\\w+) with visibility set to (\\w+) into change set (\\w+)$")
	public void importProductBundle(final String productBundleCode, final boolean storeVisible, final String changeSetGuid) throws Exception {
		final File importDirectory = createImportDirectory();
		writeManifestFile(importDirectory, PRODUCTS_IMPORT_FILE, BUNDLES_IMPORT_FILE);

		final Product product = productLookup.findByGuid(productBundleCode);
		final ProductDTO productDTO = new ProductDTO();
		productAdapter.populateDTO(product, productDTO);

		final ProductAvailabilityDTO productAvailability = productDTO.getProductAvailability();
		productAvailability.setStorevisible(storeVisible);

		final ProductBundleDTO productBundleDTO = new ProductBundleDTO();
		productBundleAdapter.populateDTO((ProductBundle) product, productBundleDTO);

		writeProductsFile(importDirectory, productDTO);
		writeBundlesFile(importDirectory, productBundleDTO);

		configureImportChangeSet(changeSetGuid);
		executeImport(importDirectory);
	}

	private void writeManifestFile(final File importDirectory, final String... resources) throws IOException {
		final Manifest manifest = new Manifest();
		Arrays.asList(resources).forEach(manifest::addResource);
		TestDomainMarshaller.marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
	}

	private void writeProductsFile(final File importDirectory, final ProductDTO productDTO) throws Exception {
		testDomainMarshaller.marshall(productDTO, JobType.PRODUCT, new File(importDirectory, PRODUCTS_IMPORT_FILE).getAbsolutePath());
	}

	private void writeBundlesFile(final File importDirectory, final ProductBundleDTO productBundleDTO) throws Exception {
		testDomainMarshaller.marshall(productBundleDTO, JobType.PRODUCTBUNDLE, new File(importDirectory, BUNDLES_IMPORT_FILE).getAbsolutePath());
	}

	private File createImportDirectory() {
		final File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		return importDirectory;
	}

	private void configureImportChangeSet(final String changeSetGuid) {
		persistenceListenerMetadataMap.put("changeSetGuid", changeSetGuid);
		persistenceListenerMetadataMap.put("changeSetOperation", OperationEnum.OPERATIONAL);
		persistenceListenerMetadataMap.put("importOperation", OperationEnum.OPERATIONAL);
	}

	private void executeImport(final File importDirectory) throws ConfigurationException {
		final ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.PRODUCT, ImportStrategyType.INSERT_OR_UPDATE)
				.addImporterConfiguration(JobType.PRODUCTBUNDLE, ImportStrategyType.INSERT_OR_UPDATE)
				.build();

		final DependentElementConfiguration skuAttributesConfig = new DependentElementConfiguration();
		skuAttributesConfig.setDependentElementType(DependentElementType.SKU_ATTRIBUTES);
		skuAttributesConfig.setCollectionStrategyType(CollectionStrategyType.RETAIN_COLLECTION);

		final ImporterConfiguration productConfig = importConfiguration.getImporterConfiguration(JobType.PRODUCT);
		productConfig.setDependentElementMap(ImmutableMap.of(DependentElementType.SKU_ATTRIBUTES, skuAttributesConfig));

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}
}

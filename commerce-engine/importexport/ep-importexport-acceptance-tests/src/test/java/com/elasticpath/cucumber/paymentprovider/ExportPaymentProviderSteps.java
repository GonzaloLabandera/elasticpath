/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.paymentprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.PropertyDTO;
import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProvidersDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;

/**
 * Export payment provider configurations.
 */
public class ExportPaymentProviderSteps {

	private static final String PAYMENT_PROVIDERS_EXPORT_FILE = "payment_providers.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";
	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	private PaymentProvidersDTO paymentProviderConfigurationsDTO;
	private File exportDirectory;

	/**
	 * Export payment provider configurations with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^exporting payment provider configurations with the importexport tool")
	public void runExport() throws Exception {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.PAYMENTPROVIDER))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Parses the exported payment provider configurations data.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@When("^the exported payment provider configurations data is parsed$")
	public void parseExportedPaymentProviderConfigurationData() throws IOException {
		final File exportedPaymentProviderConfigurationFile = new File(exportDirectory, PAYMENT_PROVIDERS_EXPORT_FILE);

		assertThat(exportedPaymentProviderConfigurationFile)
				.as("Exported payment provider configurations file not found: %s", exportedPaymentProviderConfigurationFile
						.getAbsolutePath())
				.exists();

		try (final FileInputStream exportedPaymentProviderConfigurationFileStream =
					 new FileInputStream(exportedPaymentProviderConfigurationFile)) {
			final XMLUnmarshaller paymentProviderConfigurationsUnmarshaller = new XMLUnmarshaller(PaymentProvidersDTO.class);
			paymentProviderConfigurationsDTO = paymentProviderConfigurationsUnmarshaller.unmarshall(exportedPaymentProviderConfigurationFileStream);
		}
	}

	/**
	 * Ensure the payment provider configurations is exported.
	 *
	 * @param dataTable payment provider configuration info.
	 */
	@Then("^the exported payment provider configurations records should equal$")
	public void ensurePaymentProviderConfigurationExported(final DataTable dataTable) {
		final List<PaymentProviderDTO> matchingPaymentProviderConfigurationDTOs =
				paymentProviderConfigurationsDTO.getPaymentProviders();
		for (Map<String, String> properties : dataTable.asMaps(String.class, String.class)) {
			PaymentProviderDTO paymentProviderConfigurationDTO = new PaymentProviderDTO();
			paymentProviderConfigurationDTO.setGuid(properties.get("paymentProviderConfigGuid"));
			paymentProviderConfigurationDTO.setPaymentProviderPluginBeanName(properties.get("paymentProviderPluginBeanName"));
			paymentProviderConfigurationDTO.setName(properties.get("configName"));
			paymentProviderConfigurationDTO.setDefaultDisplayName(properties.get("defaultName"));
			paymentProviderConfigurationDTO.setStatus(properties.get("status").toUpperCase());
			paymentProviderConfigurationDTO.setLocalizedNames(convertToListOfDisplayValues(properties.get("localizedNames")));

			PaymentProviderDTO exportedPaymentProviderConfigurationDTO = findPaymentProviderConfigurationsDTOByGuid(
					matchingPaymentProviderConfigurationDTOs, paymentProviderConfigurationDTO.getGuid());

			assertThat(exportedPaymentProviderConfigurationDTO).isEqualToComparingOnlyGivenFields(paymentProviderConfigurationDTO,
					"guid", "name", "status", "paymentProviderPluginBeanName", "defaultDisplayName");

			final List<DisplayValue> exportedLocalizedNames = exportedPaymentProviderConfigurationDTO.getLocalizedNames();
			final List<DisplayValue> localizedNames = paymentProviderConfigurationDTO.getLocalizedNames();
			assertThat(isSame(exportedLocalizedNames, localizedNames)).isTrue();

			verifyPaymentProviderConfigurationDataDTO(exportedPaymentProviderConfigurationDTO,
					properties);
		}
	}


	private void verifyPaymentProviderConfigurationDataDTO(final PaymentProviderDTO exportedPaymentProviderConfigurationDTO,
														   final Map<String, String> properties) {

		final Map<String, String> configDataMap = Splitter.on(",")
				.omitEmptyStrings()
				.trimResults()
				.withKeyValueSeparator(";")
				.split(properties.get("configData"));

		Map<String, String> exportedConfigDataMap = new HashMap<>();
		for (PropertyDTO property : exportedPaymentProviderConfigurationDTO.getProperties()) {
			exportedConfigDataMap.put(property.getPropertyKey(), property.getValue());
		}

		assertThat(exportedConfigDataMap).containsAllEntriesOf(configDataMap);
	}

	private PaymentProviderDTO findPaymentProviderConfigurationsDTOByGuid(
			final List<PaymentProviderDTO> paymentProviderConfigurationDTOs, final String guid) {

		final Optional<PaymentProviderDTO> matchingPaymentProviderConfigurationDTOs = paymentProviderConfigurationDTOs
				.stream()
				.filter(paymentProviderConfiguration -> guid.equals(paymentProviderConfiguration.getGuid()))
				.findFirst();

		return matchingPaymentProviderConfigurationDTOs.orElse(null);
	}

	/**
	 * Ensure manifest file includes an entry for payment provider configurations.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Then("^the exported manifest file should have an entry for payment provider configurations$")
	public void ensureManifestIncludesPaymentProviderConfiguration() throws IOException {
		final File exportedManifestFile = new File(exportDirectory, MANIFEST_EXPORT_FILE);

		assertThat(exportedManifestFile)
				.as("Exported manifest file not found: %s", exportedManifestFile.getAbsolutePath())
				.exists();

		try (final FileInputStream exportedManifestFileStream = new FileInputStream(exportedManifestFile)) {
			final XMLUnmarshaller manifestUnmarshaller = new XMLUnmarshaller(Manifest.class);
			final Manifest manifest = manifestUnmarshaller.unmarshall(exportedManifestFileStream);

			assertThat(manifest.getResources())
					.as("Manifest file missing payment provider configurations entry")
					.contains(PAYMENT_PROVIDERS_EXPORT_FILE);
		}
	}

	private List<DisplayValue> convertToListOfDisplayValues(final String source) {
		return Stream.of(source.split(";")).map(this::convertToDisplayValue).collect(Collectors.toList());

	}

	private DisplayValue convertToDisplayValue(final String source) {
		final String[] strings = source.split(":");
		return new DisplayValue(strings[0], strings[1]);
	}

	private boolean isSame(final List<DisplayValue> displayValues1, final List<DisplayValue> displayValues2) {
		return displayValues1.stream().allMatch(displayValue -> isListContainSameDisplayValue(displayValues2, displayValue));
	}

	private boolean isListContainSameDisplayValue(final List<DisplayValue> displayValues, final DisplayValue displayValue) {
		return displayValues.stream().anyMatch(displayValueFromList -> isSame(displayValueFromList, displayValue));
	}

	private boolean isSame(final DisplayValue displayValue1, final DisplayValue displayValue2) {
		return Objects.equals(displayValue1.getLanguage(), displayValue2.getLanguage())
				&& Objects.equals(displayValue1.getValue(), displayValue2.getValue());
	}

}

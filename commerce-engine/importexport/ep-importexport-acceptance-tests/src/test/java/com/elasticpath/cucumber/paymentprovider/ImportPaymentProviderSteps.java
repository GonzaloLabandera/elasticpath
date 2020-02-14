/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.paymentprovider;

import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;
import static com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl.PAYMENT_LOCALIZED_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProvidersDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValueImpl;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderConfigurationImpl;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;

/**
 * Import payment provider configurations.
 */
public class ImportPaymentProviderSteps {

	private static final String PAYMENT_PROVIDERS_IMPORT_FILE = "payment_providers.xml";

	private static final String MANIFEST_IMPORT_FILE = "manifest.xml";

	private static final String SEPARATOR = "_";

	@Autowired
	private ImportController importController;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	private PaymentProvidersDTO paymentProvidersDTO;

	private Summary summary;

	private static int runNumber = 1;

	/**
	 * Clear import data.
	 */
	@Given("^the payment provider configuration import data has been emptied out$")
	public void clearDataPolicyImportData() {
		paymentProvidersDTO = new PaymentProvidersDTO();
	}

	/**
	 * Setup the tests with payment provider configurations.
	 *
	 * @param dataTable payment provider configurations info.
	 */
	@Given("^the existing payment provider configuration of$")
	public void setUpPaymentProviderConfigurations(final DataTable dataTable) {
		savePaymentProviderConfigurationsFromDataTable(dataTable.asMaps(String.class, String.class));
	}

	/**
	 * Setup the tests with payment provider configuration.
	 *
	 * @param dataTable payment provider configuration info.
	 */
	@Given("^the payment provider configuration to import of$")
	public void setUpPaymentProviderDtos(final DataTable dataTable) {
		final List<PaymentProviderDTO> paymentProviderDTOs = getPaymentProviderDTOsFromDataTable(dataTable.asMaps(String.class, String.class));

		paymentProvidersDTO.getPaymentProviders().addAll(paymentProviderDTOs);
	}

	/**
	 * Import payment provider configuration with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^importing payment provider configuration with the importexport tool$")
	public void executeImport() throws Exception {
		final File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		final Manifest manifest = new Manifest();
		manifest.addResource(PAYMENT_PROVIDERS_IMPORT_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(PaymentProvidersDTO.class, paymentProvidersDTO, new File(importDirectory, PAYMENT_PROVIDERS_IMPORT_FILE));

		final ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.PAYMENTPROVIDER, ImportStrategyType.INSERT)
				.build();

		importController.loadConfiguration(importConfiguration);
		summary = importController.executeImport();
	}

	/**
	 * Ensure payment provider configurations contain expected values.
	 *
	 * @param dataTable message info.
	 */
	@Then("^database should contain payment provider configurations of$")
	public void paymentProviderConfigurationsShouldContain(final DataTable dataTable) {
		final List<PaymentProviderConfiguration> storedPaymentProviderConfigurations = paymentProviderConfigurationService.findAll();

		final List<PaymentProviderConfiguration> expectedPaymentProviderConfigurations = dataTable.asMaps(String.class, String.class).stream()
				.map(this::createPaymentProviderConfiguration)
				.collect(Collectors.toList());

		assertThat(isSame(expectedPaymentProviderConfigurations, storedPaymentProviderConfigurations)).isTrue();
	}

	/**
	 * Ensure an unsupported operation warning.
	 *
	 * @param dataTable message info.
	 */
	@Then("^there is the unsupported payment provider configuration operation warning message in the summary$")
	public void ensureWarningIsInSummary(final DataTable dataTable) {
		final List<Message> warnings = summary.getWarnings();

		assertThat(warnings)
				.as("The import should have warnings.")
				.isNotEmpty();

		verifySummaryMessages(dataTable.asMaps(String.class, String.class), warnings);
	}

	/**
	 * Ensure there are no any warnings and failures in summary.
	 */
	@Then("^there are no any warning and failures in summary$")
	public void ensureWarningAndFailuresAreNotInSummary() {
		final List<Message> warnings = summary.getWarnings();
		final List<Message> failures = summary.getFailures();

		assertThat(warnings)
				.as("The import should not have warnings.")
				.isEmpty();

		assertThat(failures)
				.as("The import should not have failures.")
				.isEmpty();
	}

	private void verifySummaryMessages(final List<Map<String, String>> messagesMap, final List<Message> importedMessages) {
		assertThat(importedMessages).hasSize(messagesMap.size());

		for (Map<String, String> message : messagesMap) {
			final int index = messagesMap.indexOf(message);
			String[] details = message.get("details").split(",");

			assertThat(importedMessages.get(index).getCode())
					.isEqualTo(message.get("code"));
			assertThat(importedMessages.get(index).getParams())
					.isEqualTo(details);
		}
	}

	private void savePaymentProviderConfigurationsFromDataTable(final List<Map<String, String>> paymentProviderConfigurationsMap) {
		paymentProviderConfigurationsMap.stream()
				.map(this::createPaymentProviderConfiguration)
				.forEach(paymentProviderConfigurationService::saveOrUpdate);
	}

	private PaymentProviderConfiguration createPaymentProviderConfiguration(final Map<String, String> properties) {
		final PaymentProviderConfiguration paymentProviderConfiguration = new PaymentProviderConfigurationImpl();

		return PaymentProviderConfigurationBuilder.builder()
				.withGuid(properties.get("guid"))
				.withPaymentProviderPluginId(properties.get("paymentProviderPluginBeanName"))
				.withConfigurationName(properties.get("name"))
				.withStatus(PaymentProviderConfigurationStatus.valueOf(properties.get("status")))
				.withDefaultDisplayName(properties.get("defaultDisplayName"))
				.withPaymentConfigurationData(Collections.emptySet())
				.withPaymentLocalizedProperties(convertToPaymentLocalizedProperties(convertToListOfDisplayValues(properties.get("localizedNames"))))
				.build(paymentProviderConfiguration);
	}

	private PaymentProviderDTO createPaymentProviderDTO(final Map<String, String> properties) {
		final PaymentProviderDTO paymentProviderDTO = new PaymentProviderDTO();
		paymentProviderDTO.setGuid(properties.get("guid"));
		paymentProviderDTO.setName(properties.get("name"));
		paymentProviderDTO.setPaymentProviderPluginBeanName(properties.get("paymentProviderPluginBeanName"));
		paymentProviderDTO.setStatus(properties.get("status"));
		paymentProviderDTO.setDefaultDisplayName(properties.get("defaultDisplayName"));
		paymentProviderDTO.setLocalizedNames(convertToListOfDisplayValues(properties.get("localizedNames")));

		return paymentProviderDTO;
	}

	private List<PaymentProviderDTO> getPaymentProviderDTOsFromDataTable(final List<Map<String, String>> paymentProviderConfigurationsMap) {
		return paymentProviderConfigurationsMap.stream().map(this::createPaymentProviderDTO).collect(Collectors.toList());
	}

	private List<DisplayValue> convertToListOfDisplayValues(final String source) {
		return Stream.of(source.split(";")).map(this::convertToDisplayValue).collect(Collectors.toList());

	}

	private DisplayValue convertToDisplayValue(final String source) {
		final String[] strings = source.split(":");
		return new DisplayValue(strings[0], strings[1]);
	}

	private PaymentLocalizedProperties convertToPaymentLocalizedProperties(final List<DisplayValue> localizedNames) {
		final PaymentLocalizedProperties paymentLocalizedProperties = new PaymentLocalizedPropertiesImpl();

		paymentLocalizedProperties.setPaymentLocalizedPropertiesMap(createPaymentLocalizedPropertiesMap(localizedNames),
				PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE);

		return paymentLocalizedProperties;
	}

	private Map<String, PaymentLocalizedPropertyValue> createPaymentLocalizedPropertiesMap(final List<DisplayValue> localizedNames) {
		return localizedNames.stream()
				.collect(Collectors.toMap(displayValue -> createKeyForPaymentLocalizedPropertiesMap(displayValue.getLanguage()),
						displayValue -> convertToPaymentLocalizedPropertyValue(displayValue.getLanguage(), displayValue.getValue())));
	}

	private String createKeyForPaymentLocalizedPropertiesMap(final String language) {
		return PAYMENT_LOCALIZED_PROPERTY_NAME + SEPARATOR + language;
	}

	private PaymentLocalizedPropertyValue convertToPaymentLocalizedPropertyValue(final String language, final String name) {
		final PaymentLocalizedPropertyValue paymentLocalizedPropertyValue = new PaymentLocalizedPropertyValueImpl();
		paymentLocalizedPropertyValue.setPaymentLocalizedPropertyKey(PAYMENT_LOCALIZED_PROPERTY_NAME + SEPARATOR + language);
		paymentLocalizedPropertyValue.setValue(name);

		return paymentLocalizedPropertyValue;
	}

	private boolean isSame(final List<PaymentProviderConfiguration> paymentProviderConfigurations1,
						   final List<PaymentProviderConfiguration> paymentProviderConfiguration2) {
		if (!Objects.equals(paymentProviderConfigurations1.size(), paymentProviderConfiguration2.size())) {
			return false;
		}

		return paymentProviderConfigurations1.stream()
				.allMatch(paymentProviderConfiguration -> isListContainsSameValue(paymentProviderConfiguration2, paymentProviderConfiguration));
	}

	private boolean isListContainsSameValue(final List<PaymentProviderConfiguration> paymentProviderConfigurations,
											final PaymentProviderConfiguration paymentProviderConfiguration) {
		return paymentProviderConfigurations.stream().anyMatch(configuration -> isSame(configuration, paymentProviderConfiguration));
	}

	private boolean isSame(final PaymentProviderConfiguration configuration1, final PaymentProviderConfiguration configuration2) {
		if (!Objects.equals(configuration1.getGuid(), configuration2.getGuid())) {
			return false;
		}

		if (!Objects.equals(configuration1.getPaymentProviderPluginId(), configuration2.getPaymentProviderPluginId())) {
			return false;
		}

		if (!Objects.equals(configuration1.getConfigurationName(), configuration2.getConfigurationName())) {
			return false;
		}

		if (!Objects.equals(configuration1.getStatus(), configuration2.getStatus())) {
			return false;
		}

		if (!Objects.equals(configuration1.getDefaultDisplayName(), configuration2.getDefaultDisplayName())) {
			return false;
		}

		final Map<String, PaymentLocalizedPropertyValue> paymentLocalizedPropertiesMap1 = configuration1.getPaymentLocalizedProperties()
				.getPaymentLocalizedPropertiesMap();

		final Map<String, PaymentLocalizedPropertyValue> paymentLocalizedPropertiesMap2 = configuration2.getPaymentLocalizedProperties()
				.getPaymentLocalizedPropertiesMap();

		if (!Objects.equals(paymentLocalizedPropertiesMap1.size(), paymentLocalizedPropertiesMap2.size())) {
			return false;
		}

		return paymentLocalizedPropertiesMap1.entrySet().stream()
				.allMatch(entry -> isSame(entry.getValue(), paymentLocalizedPropertiesMap2.get(entry.getKey())));
	}

	private boolean isSame(final PaymentLocalizedPropertyValue property1, final PaymentLocalizedPropertyValue property2) {
		if (!Objects.equals(property1.getPaymentLocalizedPropertyKey(), property2.getPaymentLocalizedPropertyKey())) {
			return false;
		}

		return Objects.equals(property1.getValue(), property2.getValue());
	}

}

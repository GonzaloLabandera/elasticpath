/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.store;

import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.store.CartTypeDTO;
import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.common.dto.store.StoreGlobalizationDTO;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreType;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.store.StoresDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.store.StoreService;

/**
 * Import Store.
 */
public class ImportStoreSteps {

	private static final String STORES_IMPORT_FILE = "stores.xml";
	private static final String MANIFEST_IMPORT_FILE = "manifest.xml";
	private static final String SEPARATOR = ";";

	@Autowired
	private ImportController importController;

	@Autowired
	private StoreService storeService;

	@Autowired
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	private StoresDTO storesDTO;

	private Summary summary;

	private static int runNumber = 1;

	/**
	 * Clear import data.
	 */
	@Given("^the stores import data has been emptied out$")
	public void clearDataPolicyImportData() {
		storesDTO = new StoresDTO();
	}

	/**
	 * Setup the tests with stores.
	 *
	 * @param dataTable stores info.
	 */
	@Given("^the stores to import of$")
	public void setUpStoreDtos(final DataTable dataTable) {
		final List<StoreDTO> storeDTOs = getStoreDTOsFromDataTable(dataTable.asMaps(String.class, String.class));

		storesDTO.getStores().addAll(storeDTOs);
	}

	/**
	 * Import stores with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^importing stores with the importexport tool$")
	public void executeImport() throws Exception {
		final File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		final Manifest manifest = new Manifest();
		manifest.addResource(STORES_IMPORT_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(StoresDTO.class, storesDTO, new File(importDirectory, STORES_IMPORT_FILE));

		final ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.STORE, ImportStrategyType.INSERT)
				.build();

		importController.loadConfiguration(importConfiguration);
		summary = importController.executeImport();
	}

	/**
	 * Ensure there are no any warnings and failures in stores importing summary.
	 */
	@Then("^there are no any warnings and failures in stores importing summary$")
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

	/**
	 * Verify failures in stores importing summary.
	 *
	 * @param dataTable dataTable
	 */
	@Then("^the follow failures are in stores importing summary$")
	public void verifyFailuresIsReportedInSummary(final DataTable dataTable) {
		final List<Message> failures = summary.getFailures();

		List<String> failureExceptions = failures.stream()
				.filter(message -> message.getException() != null)
				.map(message -> message.getException().getMessage())
				.collect(Collectors.toList());

		dataTable.asList(String.class).forEach(error -> {
			assertThat(failures)
					.as("The import should have failures.")
					.isNotEmpty();
			assertThat(failureExceptions)
					.as("The import should contain the expected error: " + error)
					.contains(error);
		});
	}

	/**
	 * Ensure database contains store with guid wired with payment provider config.
	 *
	 * @param code store code.
	 * @param paymentProviderConfigGuid payment provider configuration guid.
	 */
	@Then("^database should contain store with code (.+) wired with payment provider config with guid (.+)$")
	public void ensureStoreWiredWithPaymentProviderConfig(final String code, final String paymentProviderConfigGuid) {
		final Store store = storeService.findStoreWithCode(code);
		final List<String> paymentProviderConfigGuids = storePaymentProviderConfigService.findByStore(store).stream()
				.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
				.collect(Collectors.toList());

		assertThat(paymentProviderConfigGuids).containsExactly(paymentProviderConfigGuid);
	}

	/**
	 * Ensure database contains store with guid haven't wired with any payment provider config.
	 *
	 * @param code store code.
	 */
	@Then("^database should not contain store with code (.+)")
	public void ensureDatabaseShouldNotContainStoreWithCode(final String code) {
		final Store store = storeService.findStoreWithCode(code);

		assertThat(store).isNull();
	}

	/**
	 * Ensure database don't contain any stores wired with payment provider configuration.

	 * @param paymentProviderConfigGuid payment provider configuration guid.
	 */
	@Then("^database should not contain any stores wired with payment provider configuration with guid (.+)$")
	public void ensureDatabaseShouldNotContainStoreWiredWithPaymentProviderConfig(final String paymentProviderConfigGuid) {
		final StorePaymentProviderConfig storePaymentProviderConfig = storePaymentProviderConfigService.findByGuid(paymentProviderConfigGuid);

		assertThat(storePaymentProviderConfig).isNull();
	}

	/**
	 * Ensure an unsupported operation warning.
	 *
	 * @param dataTable message info.
	 */
	@Then("^there is the unsupported store importing operation warning message in the summary$")
	public void ensureWarningIsInSummary(final DataTable dataTable) {
		final List<Message> failures = summary.getFailures();

		assertThat(failures)
				.as("The import should have failures.")
				.isNotEmpty();

		verifySummaryMessages(dataTable.asMaps(String.class, String.class), failures);
	}

	/**
	 * Ensure database contains payment provider configuration with particular guid and status.
	 *
	 * @param paymentProviderConfigGuid payment provider configuration guid.
	 * @param status                    payment provider configuration status.
	 */
	@Then("^database should contain payment provider configuration with guid (.+) and status (.+)$")
	public void ensureDatabaseContainPaymentProviderConfigurationWithGuidAndStatus(final String paymentProviderConfigGuid, final String status) {
		final PaymentProviderConfiguration paymentProviderConfiguration = paymentProviderConfigurationService.findByGuid(paymentProviderConfigGuid);

		assertThat(paymentProviderConfiguration.getStatus()).isEqualTo(PaymentProviderConfigurationStatus.valueOf(status));
	}

	private void verifySummaryMessages(final List<Map<String, String>> messagesMap, final List<Message> importedMessages) {
		assertThat(importedMessages).hasSize(messagesMap.size());

		for (Map<String, String> message : messagesMap) {
			final int index = messagesMap.indexOf(message);
			String[] details = message.get("details").split(SEPARATOR);

			assertThat(importedMessages.get(index).getCode())
					.isEqualTo(message.get("code"));
			assertThat(importedMessages.get(index).getParams())
					.isEqualTo(details);
		}
	}

	private List<StoreDTO> getStoreDTOsFromDataTable(final List<Map<String, String>> paymentProviderConfigurationsMap) {
		return paymentProviderConfigurationsMap.stream().map(this::createStoreDto).collect(Collectors.toList());
	}

	private StoreDTO createStoreDto(final Map<String, String> properties) {
		final StoreDTO storeDTO = new StoreDTO();
		storeDTO.setCode(properties.get("code"));
		storeDTO.setGlobalization(convertToStoreGlobalizationDTO(properties.get("globalization")));
		storeDTO.setUrl(properties.get("url"));
		storeDTO.setName(properties.get("name"));
		storeDTO.setStoreState(Integer.valueOf(properties.get("state")));
		storeDTO.setStoreType(StoreType.valueOf(properties.get("type")));
		storeDTO.setCatalogCode("Mobile");
		storeDTO.setDisplayOutOfStock(Boolean.valueOf(properties.get("display_out_of_stock")));
		storeDTO.setEmailSenderName(properties.get("email_sender_name"));
		storeDTO.setEmailSenderAddress(properties.get("email_sender_address"));
		storeDTO.setStoreAdminEmail(properties.get("store_admin_email"));
		storeDTO.setCvv2Enabled(Boolean.valueOf(properties.get("credit_card_cvv2_enabled")));
		storeDTO.setStoreFullCreditCards(Boolean.valueOf(properties.get("store_full_credit_cards")));
		storeDTO.setSupportedLocales(separateStringToListOfObjects(properties.get("locales"), SEPARATOR, Locale::forLanguageTag));
		storeDTO.setSupportedCurrencies(separateStringToListOfObjects(properties.get("currencies"), SEPARATOR, Currency::getInstance));
		storeDTO.setTaxCodeGuids(separateStringToListOfStrings(properties.get("tax_codes"), SEPARATOR));
		storeDTO.setCreditCardTypes(separateStringToListOfStrings(properties.get("credit_card_types"), SEPARATOR));
		if (properties.containsKey("payment_provider_configurations")) {
			storeDTO.setPaymentProviderConfigGuids(separateStringToListOfStrings(properties.get("payment_provider_configurations"), SEPARATOR));
		}
		if (properties.containsKey("cart_type_name")) {
			CartTypeDTO cartTypeDTO = new CartTypeDTO();
			cartTypeDTO.setName(properties.get("cart_type_name"));
			cartTypeDTO.setGuid(properties.get("cart_type_guid"));
			List<String> modifierGroups = new ArrayList<>();
			modifierGroups.add(properties.get("cart_type_modifier_group"));
			cartTypeDTO.setModifierGroups(modifierGroups);
			List<CartTypeDTO> cartTypeDTOS = new ArrayList<>();
			cartTypeDTOS.add(cartTypeDTO);
			storeDTO.setShoppingCartTypes(cartTypeDTOS);
		}

		storeDTO.setEnabled(true);

		return storeDTO;
	}

	private StoreGlobalizationDTO convertToStoreGlobalizationDTO(final String source) {
		final int defaultCurrencyField = 0;
		final int defaultLocaleField = 1;
		final int encodingField = 2;
		final int subCountryField = 3;
		final int countryField = 4;
		final int timeZoneField = 5;

		final String[] fields = source.split(SEPARATOR);

		final StoreGlobalizationDTO storeGlobalizationDTO = new StoreGlobalizationDTO();
		storeGlobalizationDTO.setDefaultCurrency(Currency.getInstance(fields[defaultCurrencyField]));
		storeGlobalizationDTO.setDefaultLocale(Locale.forLanguageTag(fields[defaultLocaleField]));
		storeGlobalizationDTO.setContentEncoding(fields[encodingField]);
		storeGlobalizationDTO.setSubCountry(fields[subCountryField]);
		storeGlobalizationDTO.setCountry(fields[countryField]);
		storeGlobalizationDTO.setTimeZone(TimeZone.getTimeZone(fields[timeZoneField]));

		return storeGlobalizationDTO;
	}

	private <T> List<T> separateStringToListOfObjects(final String source, final String separator, final Function<String, T> processor) {
		if (Objects.isNull(source)) {
			return Collections.emptyList();
		}

		return Stream.of(source.split(separator)).map(processor).collect(Collectors.toList());
	}

	private List<String> separateStringToListOfStrings(final String source, final String separator) {
		if (Objects.isNull(source)) {
			return Collections.emptyList();
		}

		return Arrays.asList(source.split(separator));
	}

}

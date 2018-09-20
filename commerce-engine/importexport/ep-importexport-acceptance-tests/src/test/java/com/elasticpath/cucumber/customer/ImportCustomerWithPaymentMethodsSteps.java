/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.cucumber.customer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.elasticpath.common.dto.customer.CreditCardDTO;
import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.common.dto.customer.LegacyCreditCardDTO;
import com.elasticpath.common.dto.customer.PaymentMethodDto;
import com.elasticpath.common.dto.customer.PaymentTokenDto;
import com.elasticpath.common.dto.customer.builder.CustomerDTOBuilder;
import com.elasticpath.common.dto.customer.transformer.CreditCardDTOTransformer;
import com.elasticpath.common.dto.customer.transformer.PaymentTokenDTOTransformer;
import com.elasticpath.commons.util.TestDomainMarshaller;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.customer.CustomerGroupBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.importexport.common.configuration.PackagerConfiguration;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.factory.TestPaymentDTOBuilderFactory;
import com.elasticpath.importexport.common.factory.TestPaymentMethodBuilderFactory;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.types.PackageType;
import com.elasticpath.importexport.common.types.TransportType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.importer.configuration.DependentElementConfiguration;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.configuration.RetrievalConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.importers.impl.SavingManager;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Steps for the import customer with payment methods feature.
 */
@ContextConfiguration("/integration-context.xml")
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class
})
@SuppressWarnings("PMD.TooManyFields")
public class ImportCustomerWithPaymentMethodsSteps {
	private static final String TEST_STORE_CODE = "testStoreCode";
	private static final String TEST_PASSWORD = "testPassword";
	private static final String TEST_SALT = "testSalt";
	private static final String TEST_DISPLAY_VALUE = "testDisplayValue";
	private static final String TEST_CARD_NUMBER = "testCardNumber";
	private static final String TEST_DISPLAY_VALUE_2 = "testDisplayValue2";
	private static final String TEST_USER_ID = "testUserId";
	private static final String TEST_GUID = "testGuid";

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerGroupService customerGroupService;

	@Autowired
	private ImportController importController;

	@Autowired
	private SavingManager<Persistable> savingManager;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private CustomerGroupBuilder customerGroupBuilder;

	@Autowired
	private TestDomainMarshaller testDomainMarshaller;

	@Autowired
	private PaymentTokenDTOTransformer paymentTokenDTOTransformer;

	@Autowired
	private CreditCardDTOTransformer creditCardDTOTransformer;

	@Autowired
	private TestPaymentMethodBuilderFactory testPaymentMethodBuilderFactory;

	@Autowired
	private TestPaymentDTOBuilderFactory paymentDTOBuilderFactory;

	private Customer existingCustomer;

	private ImportConfiguration importConfiguration;

	private PaymentMethodDto testPaymentMethodA;
	private PaymentMethodDto testPaymentMethodB;
	private PaymentMethodDto testPaymentMethodC;

	private LegacyCreditCardDTO legacyCreditCardA;
	private LegacyCreditCardDTO legacyCreditCardB;
	private LegacyCreditCardDTO legacyCreditCardC;
	private Summary summary;

	private String customersFilePath;

	/**
	 * Create test payment methods.
	 */
	@Before
	public void setupImportTestingComponents() {
		testPaymentMethodA = paymentDTOBuilderFactory.createPaymentTokenWithValue("A").build();
		testPaymentMethodB = paymentDTOBuilderFactory.createPaymentTokenWithValue("B").build();
		testPaymentMethodC = paymentDTOBuilderFactory.createPaymentTokenWithValue("C").build();

		if (customerGroupService.findByGroupName(CustomerGroup.DEFAULT_GROUP_NAME) == null) {
			final CustomerGroup defaultCustomerGroup = customerGroupBuilder.newInstance()
					.withGuid(String.format("guid_%s", CustomerGroup.DEFAULT_GROUP_NAME))
					.withName(CustomerGroup.DEFAULT_GROUP_NAME)
					.withDescription(String.format("Description for %s", CustomerGroup.DEFAULT_GROUP_NAME))
					.withEnabled(true)
					.build();
			customerGroupService.add(defaultCustomerGroup);
		}

		URL exportConfiguration = getClass().getClassLoader().getResource("customers");
		assert exportConfiguration != null : "Failed to load products directory";
		customersFilePath = exportConfiguration.getPath();
	}

	/**
	 * Create a existing customer.
	 */
	@Given("^a customer exists$")
	public void createACustomerWithNoPaymentMethods() {
		existingCustomer = createPersistedCustomerWithPaymentMethods();
		when(customerService.findByGuid(eq(existingCustomer.getGuid()), any(FetchGroupLoadTuner.class))).thenReturn(existingCustomer);
	}

	/**
	 * Create a existingCustomer.
	 */
	@Given("^a customer exists with payment methods$")
	public void createACustomerWithPaymentMethods() {
		PaymentMethod paymentMethod = testPaymentMethodBuilderFactory.createPaymentTokenBuilderWithIdentity(TEST_DISPLAY_VALUE).build();
		PaymentMethod paymentMethod2 = testPaymentMethodBuilderFactory.createPaymentTokenBuilderWithIdentity(TEST_DISPLAY_VALUE_2).build();
		PaymentMethod paymentMethod3 = testPaymentMethodBuilderFactory.createCreditCardBuilderWithIdentity(TEST_CARD_NUMBER).build();

		existingCustomer = createPersistedCustomerWithPaymentMethods(paymentMethod, paymentMethod2, paymentMethod3);

		when(customerService.findByGuid(eq(existingCustomer.getGuid()), any(FetchGroupLoadTuner.class))).thenReturn(existingCustomer);
	}

	/**
	 * Create an XML representation including 3 payment methods (last one default).
	 *
	 * @throws Exception in case of error
	 */
	@And("^an import with payment methods A,B and C and C is the default$")
	public void setupImportFileForCustomerWithPaymentMethodsAndDefault() throws Exception {
		CustomerDTO customerDTO = createCustomerDTOWithPaymentMethodsAndDefault(Arrays.<PaymentMethodDto>asList(testPaymentMethodA,
				testPaymentMethodB, testPaymentMethodC), testPaymentMethodC);
		createXmlRepresentationOfCustomerAndConfigureImport(customerDTO);
	}

	/**
	 * Create an XML representation with 3 payment methods but no default.
	 *
	 * @throws Exception in case of error
	 */
	@And("^an import with payment methods A,B and C and no default payment method$")
	public void setupImportFileForCustomerWithPaymentMethodsAndNoDefault() throws Exception {
		CustomerDTO customerDTO = createCustomerDTOWithPaymentMethodsAndDefault(Arrays.<PaymentMethodDto>asList(testPaymentMethodA, 
				testPaymentMethodB, testPaymentMethodC), null);

		createXmlRepresentationOfCustomerAndConfigureImport(customerDTO);
	}

	/**
	 * Create an import configuration that should retain collections.
	 *
	 * @throws Exception in case of error
	 */
	@And("^an import configured with a RETAIN_COLLECTION collection strategy for payment methods$")
	public void setupImportFileForCustomerWithRetainCollectionForPaymentMethods() throws Exception {
		CustomerDTO customerDTO = createCustomerDTOWithPaymentMethodsAndDefault(Arrays.<PaymentMethodDto>asList(testPaymentMethodA, 
				testPaymentMethodB, testPaymentMethodC), null);

		createXmlRepresentationOfCustomerAndConfigureImport(customerDTO);
		ImporterConfiguration importerConfiguration = importConfiguration.getImporterConfiguration(JobType.CUSTOMER);

		HashMap<DependentElementType, DependentElementConfiguration> dependentElementTypeMap =
			new HashMap<>();
		DependentElementConfiguration dependentElementConfiguration = new DependentElementConfiguration();
		dependentElementConfiguration.setDependentElementType(DependentElementType.PAYMENT_METHODS);
		dependentElementConfiguration.setCollectionStrategyType(CollectionStrategyType.RETAIN_COLLECTION);
		dependentElementTypeMap.put(DependentElementType.PAYMENT_METHODS, dependentElementConfiguration);

		importerConfiguration.setDependentElementMap(dependentElementTypeMap);
	}

	/**
	 * Setup a legacy format XML representation of a customer with 3 credit cards.
	 *
	 * @throws Exception in case of error
	 */
	@And("^an import with credit cards A,B and C and C is the default using the legacy format$")
	public void setupImportFileForCustomerWithCreditCardsUsingLegacyFormat() throws Exception {
		legacyCreditCardA = paymentDTOBuilderFactory.createLegacyWithIdentity("A").build();
		legacyCreditCardB = paymentDTOBuilderFactory.createLegacyWithIdentity("B").build();
		legacyCreditCardC = paymentDTOBuilderFactory.createLegacyWithIdentity("C").build();
		legacyCreditCardC.setDefaultCard(true);

		CustomerDTO customerDTO = createCustomerDTOWithPaymentMethodsAndDefault(Collections.<PaymentMethodDto>emptyList(), null);
		customerDTO.setCreditCards(Arrays.asList(legacyCreditCardA, legacyCreditCardB, legacyCreditCardC));

		createXmlRepresentationOfCustomerAndConfigureImport(customerDTO);
	}

	/**
	 * Setup an XML representation of a customer with no payment methods.
	 *
	 * @throws Exception in case of error
	 */
	@And("^an import with an empty collection of payment methods$")
	public void setupImportFilerForCustomerWithNoPaymentMethods() throws Exception {
		CustomerDTO customerDTO = createCustomerDTOWithPaymentMethodsAndDefault(Collections.<PaymentMethodDto>emptyList(), null);

		createXmlRepresentationOfCustomerAndConfigureImport(customerDTO);
	}

	/**
	 * Execute the import.
	 *
	 * @throws ConfigurationException if an error occurs during export configuration
	 */
	@When("^I execute the import$")
	public void executeImport() throws ConfigurationException {
		importController.loadConfiguration(importConfiguration);
		summary = importController.executeImport();
	}

	/**
	 * Ensure the customer has the expected payment methods and default.
	 */
	@Then("^the customer will be updated with payment methods A,B and C and C is chosen as the default$")
	public void ensureImportedCustomerHasListOfPaymentMethodsWithDefaultSelected() {
		PaymentMethod paymentMethod = transformToPaymentMethod(testPaymentMethodA);
		PaymentMethod paymentMethod2 = transformToPaymentMethod(testPaymentMethodB);
		PaymentMethod paymentMethod3 = transformToPaymentMethod(testPaymentMethodC);

		Customer persistedCustomer = getCustomerUpdated();

		new CustomerPaymentMethodsValidator(persistedCustomer)
				.withPaymentMethods(paymentMethod, paymentMethod2, paymentMethod3)
				.withDefaultPaymentMethod(paymentMethod3)
				.validate();
	}

	/**
	 * Ensure the customer has the expected payment methods and default.
	 */
	@Then("^the customer will be updated with payment methods A,B, and C and A will be chosen as the default$")
	public void ensureImportedCustomerHasListOfPaymentMethodsWithFirstPaymentMethodSelectedAsDefault() {
		PaymentMethod paymentMethod = transformToPaymentMethod(testPaymentMethodA);
		PaymentMethod paymentMethod2 = transformToPaymentMethod(testPaymentMethodB);
		PaymentMethod paymentMethod3 = transformToPaymentMethod(testPaymentMethodC);

		Customer persistedCustomer = getCustomerUpdated();

		new CustomerPaymentMethodsValidator(persistedCustomer)
				.withPaymentMethods(paymentMethod, paymentMethod2, paymentMethod3)
				.withDefaultPaymentMethod(paymentMethod)
				.validate();
	}

	/**
	 * Ensure the customer has the expected credit cards and default.
	 */
	@Then("^the customer will be updated with credit cards A,B and C and C will be chosen as the default$")
	public void ensureImportedCustomerHasListOfCreditCardsAndFirstCreditCardSelectedAsDefault() {
		PaymentMethod expectedCreditCard = transformToPaymentMethod(legacyCreditCardA);
		PaymentMethod expectedCreditCard2 = transformToPaymentMethod(legacyCreditCardB);
		PaymentMethod expectedCreditCard3 = transformToPaymentMethod(legacyCreditCardC);

		Customer persistedCustomer = getCustomerUpdated();

		new CustomerPaymentMethodsValidator(persistedCustomer)
				.withPaymentMethods(expectedCreditCard, expectedCreditCard2, expectedCreditCard3)
				.withDefaultPaymentMethod(expectedCreditCard3)
				.validate();
	}

	/**
	 * Ensure the customer has no payment methods.
	 */
	@Then("^the customer is updated and has no payment methods$")
	public void ensureImportedCustomerHasNoPaymentMethods() {
		Customer persistedCustomer = getCustomerUpdated();

		new CustomerPaymentMethodsValidator(persistedCustomer)
				.withPaymentMethods()
				.withDefaultPaymentMethod(null)
				.validate();
	}

	/**
	 * Ensure an unsupported operation throws an exception.
	 */
	@Then("^an unsupported operation exception is thrown$")
	public void ensureUnsupportedOperationExceptionIsThrown() {
		Assert.assertTrue("The import should have failed.", summary.failuresExist());
		List<Message> failures = summary.getFailures();

		Assert.assertEquals("There should only be two failure messages", 2, failures.size());
		Assert.assertThat(failures.get(0).getException().getMessage(), Matchers.containsString("Only CLEAR_COLLECTION is currently supported"));
	}

	private Customer getCustomerUpdated() {
		ArgumentCaptor <Customer> persistedCustomer = ArgumentCaptor.forClass(Customer.class);
		verify(savingManager).update(persistedCustomer.capture());
		verify(customerService).findByGuid(eq(existingCustomer.getGuid()), any(FetchGroupLoadTuner.class));
		return persistedCustomer.getValue();
	}

	private Customer createPersistedCustomerWithPaymentMethods(final PaymentMethod... paymentMethods) {
		return customerBuilder
				.withGuid(TEST_GUID)
				.withUidPk(1L)
				.withPaymentMethods(paymentMethods)
				.build();
	}

	private CustomerDTO createCustomerDTOWithPaymentMethodsAndDefault(final List<PaymentMethodDto> paymentMethodDtos,
			final PaymentMethodDto defaultPaymentMethod) {
		return new CustomerDTOBuilder().withGuid(TEST_GUID)
				.withCreationDate(new Date())
				.withLastEditDate(new Date())
				.withStoreCode(TEST_STORE_CODE)
				.withUserId(TEST_USER_ID)
				.withPaymentMethods(paymentMethodDtos.toArray(new PaymentMethodDto[paymentMethodDtos.size()]))
				.withDefaultPaymentMethod(defaultPaymentMethod)
				.withPassword(TEST_PASSWORD)
				.withSalt(TEST_SALT)
				.build();
	}

	private PaymentMethod transformToPaymentMethod(final PaymentMethodDto paymentMethodDto) {
		if (paymentMethodDto instanceof CreditCardDTO) {
			return creditCardDTOTransformer.transformToDomain((CreditCardDTO) paymentMethodDto);
		} else if (paymentMethodDto instanceof PaymentTokenDto) {
			return paymentTokenDTOTransformer.transformToDomain((PaymentTokenDto) paymentMethodDto);
		} else {
			return null;
		}
	}

	private void createXmlRepresentationOfCustomerAndConfigureImport(final CustomerDTO customerDTO) throws Exception {
		testDomainMarshaller.marshall(customerDTO, JobType.CUSTOMER, customersFilePath + "/customers.xml");

		importConfiguration = new ImportConfiguration();
		HashMap<JobType, ImporterConfiguration> importerConfigurationMap = new HashMap<>();
		ImporterConfiguration importerConfiguration = new ImporterConfiguration();
		importerConfiguration.setImportStrategyType(ImportStrategyType.INSERT_OR_UPDATE);
		importerConfiguration.setJobType(JobType.CUSTOMER);

		PackagerConfiguration packagerConfiguration = new PackagerConfiguration();
		packagerConfiguration.setType(PackageType.NONE);

		RetrievalConfiguration retrievalConfiguration = new RetrievalConfiguration();
		retrievalConfiguration.setMethod(TransportType.FILE);
		retrievalConfiguration.setSource(customersFilePath);

		importerConfigurationMap.put(JobType.CUSTOMER, importerConfiguration);

		importConfiguration.setImporterConfigurationMap(importerConfigurationMap);
		importConfiguration.setXmlValidation(true);
		importConfiguration.setPackagerConfiguration(packagerConfiguration);
		importConfiguration.setRetrievalConfiguration(retrievalConfiguration);
	}

}

/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.cucumber.customer;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.elasticpath.common.dto.assembler.customer.BuiltinFilters;
import com.elasticpath.common.dto.customer.CreditCardDTO;
import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.common.dto.customer.PaymentMethodDto;
import com.elasticpath.common.dto.customer.transformer.CreditCardDTOTransformer;
import com.elasticpath.common.dto.customer.transformer.PaymentTokenDTOTransformer;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.PaymentToken;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.common.dto.customer.CustomersDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.factory.TestPaymentMethodBuilderFactory;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.ql.parser.EPQueryType;
import com.elasticpath.search.searchengine.EpQLSearchEngine;
import com.elasticpath.search.searchengine.SolrIndexSearchResult;
import com.elasticpath.service.customer.CustomerService;

/**
 * Steps for the export customer with payment methods feature.
 */
@ContextConfiguration("/integration-context.xml")
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class
})
public class ExportCustomerWithPaymentMethodsSteps {
	private static final String EXISTING_CUSTOMER_EPQL_QUERY = "FIND Customer WHERE GUID = ";
	private static final String TEST_GUID = "testGuid";
	private static final String TEST_TOKEN_IDENTITY1 = "testTokenIdentity1";
	private static final String TEST_TOKEN_IDENTITY2 = "testTokenIdentity2";
	private static final String TEST_CARD_IDENTITY1 = "testCardIdentity1";
	private static final String TEST_CARD_IDENTITY2 = "testCardIdentity2";
	private static final String EXPORT_DIRECTORY_PATH = "target/test-classes/export/ExportCustomerWithPaymentMethodsSteps";

	@Autowired
	private ExportController exportController;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private EpQLSearchEngine epQLSearchEngine;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private TestPaymentMethodBuilderFactory testPaymentMethodBuilderFactory;

	@Autowired
	private PaymentTokenDTOTransformer paymentTokenDTOTransformer;

	@Autowired
	private CreditCardDTOTransformer creditCardDTOTransformer;

	private final XMLUnmarshaller xmlUnmarshaller = new XMLUnmarshaller(CustomersDTO.class);

	private Customer existingCustomer;
	private PaymentMethod paymentMethodA;
	private PaymentMethod paymentMethodB;
	private PaymentMethod paymentMethodC;

	/**
	 * Create a customer with 3 payment methods.
	 */
	@Given("^a customer exists with payment methods A, B and C and C is the default$")
	public void createACustomerWithPaymentMethods() {
		paymentMethodA = testPaymentMethodBuilderFactory.createPaymentTokenBuilderWithIdentity(TEST_TOKEN_IDENTITY1).build();
		paymentMethodB = testPaymentMethodBuilderFactory.createPaymentTokenBuilderWithIdentity(TEST_TOKEN_IDENTITY2).build();
		paymentMethodC = testPaymentMethodBuilderFactory.createCreditCardBuilderWithIdentity(TEST_CARD_IDENTITY1).build();

		existingCustomer = createPersistedCustomerWithPaymentMethods(paymentMethodA, paymentMethodB, paymentMethodC);
		existingCustomer.getPaymentMethods().setDefault(paymentMethodC);
	}

	/**
	 * Create a customer with two credit cards.
	 */
	@Given("^a customer exists with credit cards A and B")
	public void createACustomerWithCreditCards() {
		paymentMethodA = testPaymentMethodBuilderFactory.createCreditCardBuilderWithIdentity(TEST_CARD_IDENTITY1).build();
		paymentMethodB = testPaymentMethodBuilderFactory.createCreditCardBuilderWithIdentity(TEST_CARD_IDENTITY2).build();

		existingCustomer = createPersistedCustomerWithPaymentMethods(paymentMethodA, paymentMethodB);
	}

	/**
	 * Create a customer without payment methods.
	 */
	@Given("^a customer exists with no payment methods$")
	public void createACustomerWithNoPaymentMethods() {
		existingCustomer = createPersistedCustomerWithPaymentMethods();
	}

	/**
	 * Export a customer using a card number filter.
	 * @param cardNumberFilterValue the filter to use
	 * @throws Exception in case of error
	 */
	@When("^I export the customer with CARD_NUMBER_FILTER set to (\\w+)$")
	public void executeExportWithCardNumberFilterValue(final String cardNumberFilterValue) throws Exception {
		configureAndExportCustomerWithCardNumberFilterValue(cardNumberFilterValue);
	}

	/**
	 * Export a customer.
	 * @throws Exception in case of error
	 */
	@When("^I export the customer$")
	public void executeExport() throws Exception {
		configureAndExportCustomerWithCardNumberFilterValue("STATIC");
	}

	private void configureAndExportCustomerWithCardNumberFilterValue(final String cardNumberFilterValue) throws IOException, ConfigurationException {
		final String epQLSearchQuery = EXISTING_CUSTOMER_EPQL_QUERY + existingCustomer.getGuid();

		List<Long> customerUids = Arrays.asList(existingCustomer.getUidPk());
		SolrIndexSearchResult<Long> customerQuery = new SolrIndexSearchResult<>();
		customerQuery.setEpQueryType(EPQueryType.CUSTOMER);
		customerQuery.setResultUids(customerUids);

		when(epQLSearchEngine.<Long>search(epQLSearchQuery)).thenReturn(customerQuery);
		when(customerService.findByUids(customerUids)).thenReturn(Arrays.asList(existingCustomer));
		when(customerService.findByGuid(existingCustomer.getGuid())).thenReturn(existingCustomer);

		ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
					.setDeliveryTarget(EXPORT_DIRECTORY_PATH)
					.setExporterTypes(Arrays.asList(RequiredJobType.CUSTOMER))
					.addExporterOption(RequiredJobType.CUSTOMER, "CARD_NUMBER_FILTER", cardNumberFilterValue)
					.build();

		SearchConfiguration searchConfiguration = new SearchConfiguration();
		searchConfiguration.setEpQLQuery(epQLSearchQuery);

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Ensure the exported customer has the expected payment methods and default.
	 * @throws Exception in case of error
	 */
	@Then("^the exported customer will have payment methods A, B and C and C is the default$")
	public void ensureExportedCustomerDTOHasPaymentMethodsWithCorrectDefaultSelected() throws Exception {
		CustomersDTO customerDtosMarshalled = getCustomerDtosMarshalled();

		assertFalse("At least 1 customer should have been marshalled", customerDtosMarshalled.getCustomers().isEmpty());

		CustomerDTO customerDTO = customerDtosMarshalled.getCustomers().get(0);

		assertEquals(transformToPaymentMethodDto(paymentMethodC),
				customerDTO.getDefaultPaymentMethod().getPaymentMethod());
		assertEquals(Arrays.asList(
				transformToPaymentMethodDto(paymentMethodA),
				transformToPaymentMethodDto(paymentMethodB),
				transformToPaymentMethodDto(paymentMethodC)), customerDTO.getPaymentMethods());
	}

	/**
	 * Ensure the exported customer record doesn't include real credit card numbers.
	 * @throws Exception in case of error
	 */
	@Then("^the exported customer will contain credit cards A and B without the real credit card numbers$")
	public void ensureRealCreditCardNumbersNotExported() throws Exception {
		CustomersDTO customerDtosMarshalled = getCustomerDtosMarshalled();

		assertFalse("At least 1 customer should have been marshalled", customerDtosMarshalled.getCustomers().isEmpty());

		CustomerDTO customerDTO = customerDtosMarshalled.getCustomers().get(0);

		assertEquals(transformToPaymentMethodDto(paymentMethodA),
				customerDTO.getDefaultPaymentMethod().getPaymentMethod());
		assertEquals(Arrays.asList(
				transformToPaymentMethodDto(paymentMethodA),
				transformToPaymentMethodDto(paymentMethodB)), customerDTO.getPaymentMethods());
	}

	/**
	 * Ensure that the exported customer record has no payment methods.
	 * @throws Exception in case of error
	 */
	@Then("^the exported customer will have no payment methods$")
	public void ensureExportedCustomerDTOHasNoPaymentMethods() throws Exception {
		CustomersDTO customerDtosMarshalled = getCustomerDtosMarshalled();

		assertFalse("At least 1 customer should have been marshalled", customerDtosMarshalled.getCustomers().isEmpty());

		assertThat(customerDtosMarshalled.getCustomers().get(0).getPaymentMethods(), empty());
	}

	private PaymentMethodDto transformToPaymentMethodDto(final PaymentMethod paymentMethod) {
		if (paymentMethod instanceof CustomerCreditCard) {
			CreditCardDTO creditCardDTO = creditCardDTOTransformer.transformToDto((CustomerCreditCard) paymentMethod);
			return BuiltinFilters.STATIC.filter(creditCardDTO);
		} else if (paymentMethod instanceof PaymentToken) {
			return paymentTokenDTOTransformer.transformToDto((PaymentToken) paymentMethod);
		} else {
			return null;
		}
	}

	private CustomersDTO getCustomerDtosMarshalled() throws Exception {
		FileInputStream exportedCustomersFile = new FileInputStream(EXPORT_DIRECTORY_PATH + "/customers.xml");
		return xmlUnmarshaller.unmarshall(exportedCustomersFile);
	}

	private Customer createPersistedCustomerWithPaymentMethods(final PaymentMethod... paymentMethods) {
		return customerBuilder
				.withGuid(TEST_GUID)
				.withUidPk(1L)
				.withPaymentMethods(paymentMethods)
				.build();
	}
}

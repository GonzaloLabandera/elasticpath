/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.cucumber.customer;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.ql.parser.EPQueryType;
import com.elasticpath.search.searchengine.EpQLSearchEngine;
import com.elasticpath.search.searchengine.SolrIndexSearchResult;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.store.StoreService;

/**
 * Steps for the export customer with payment methods feature.
 */
public class ExportCustomerWithPaymentMethodsSteps {
	private static final String EXISTING_CUSTOMER_EPQL_QUERY = "FIND Customer WHERE GUID = ";
	private static final String TEST_GUID = "testGuid";
	private static final String EXPORT_DIRECTORY_PATH = "target/test-classes/export/ExportCustomerWithPaymentMethodsSteps";
	private static final String TEST_SHARED_ID = "testUser@email.com";
	private static final String TEST_EMAIL = TEST_SHARED_ID;
	private static final String TEST_FIRST_NAME = "testFirstName";
	private static final String TEST_LAST_NAME = "testLastName";

	@Autowired
	private ExportController exportController;

	@Autowired
	private EpQLSearchEngine epQLSearchEngine;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private StoreService storeService;

	@Autowired
	private CustomerService customerService;

	private Customer existingCustomer;

	/**
	 * Create a customer with 3 payment methods.
	 */
	@Given("^a customer exists with payment methods A and B and A is the default$")
	public void createACustomerWithPaymentMethods() {
		existingCustomer = createPersistedCustomer();
	}

	/**
	 * Create a customer without payment methods.
	 */
	@Given("^a customer exists with no payment methods$")
	public void createACustomerWithNoPaymentMethods() {
		existingCustomer = createPersistedCustomer();
	}

	/**
	 * Export a customer.
	 *
	 * @throws Exception in case of error
	 */
	@When("^I export the customer$")
	public void executeExport() throws Exception {
		configureAndExportCustomer();
	}

	private void configureAndExportCustomer() throws ConfigurationException {
		final String epQLSearchQuery = EXISTING_CUSTOMER_EPQL_QUERY + existingCustomer.getGuid();

		List<Long> customerUids = Collections.singletonList(existingCustomer.getUidPk());
		SolrIndexSearchResult<Long> customerQuery = new SolrIndexSearchResult<>();
		customerQuery.setEpQueryType(EPQueryType.CUSTOMER);
		customerQuery.setResultUids(customerUids);

		when(epQLSearchEngine.<Long>search(epQLSearchQuery)).thenReturn(customerQuery);

		ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(EXPORT_DIRECTORY_PATH)
						.setExporterTypes(Collections.singletonList(RequiredJobType.CUSTOMER))
						.build();

		SearchConfiguration searchConfiguration = new SearchConfiguration();
		searchConfiguration.setEpQLQuery(epQLSearchQuery);

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	private Customer createPersistedCustomer() {
		final Customer customer = customerBuilder.newInstance()
				.withGuid(TEST_GUID)
				.withFirstName(TEST_FIRST_NAME)
				.withLastName(TEST_LAST_NAME)
				.withEmail(TEST_EMAIL)
				.withUsername(TEST_EMAIL)
				.withStoreCode(storeService.findAllStores().get(0).getCode())
				.build();

		return customerService.add(customer);
	}
}

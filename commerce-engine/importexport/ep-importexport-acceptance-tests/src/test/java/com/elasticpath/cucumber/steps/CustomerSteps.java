/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.customer.CustomerGroupService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Customer steps.
 */
@ContextConfiguration("/integration-context.xml")
public class CustomerSteps {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerGroupService customerGroupService;

	@Autowired
	private CustomerBuilder customerBuilder;

	private static List<Customer> persistedCustomers = new ArrayList<>();

	private static long lastCustomerUid = 1L;


	/**
	 * Clear all created customers.
	 */
	@Given("^there is no existing customer in the system$")
	public void clearAllCreatedCustomers() {
		persistedCustomers.clear();
	}

	/**
	 * Creates a customer.
	 *
	 * @param customerFirstName the customer first name
	 * @param customerLastName the customer last name
	 * @param unused the unused
	 */
	@Given("^the customer \\[(\\w+) (\\w+)\\] has been created( with no .*)?$")
	public void createCustomer(final String customerFirstName, final String customerLastName, final String unused) {
		addCustomer(customerFirstName, customerLastName, Collections.<String>emptyList());
	}

	/**
	 * Creates a customer with customer groups.
	 *
	 * @param customerFirstName the customer first name
	 * @param customerLastName the customer last name
	 * @param customerGroupNames the customer group names
	 */
	@Given("^the customer \\[(\\w+) (\\w+)\\] has been created and assigned customer segments? \\[([A-Z0-9_,]+)\\]$")
	public void createCustomerWithCustomerGroups(
			final String customerFirstName, final String customerLastName, final String customerGroupNames) {
		addCustomer(customerFirstName, customerLastName, Arrays.asList(customerGroupNames.split(",")));
	}

	private void addCustomer(final String customerFirstName, final String customerLastName, final List<String> customerGroupNames) {
		final String customerGuid = generateCustomerGuidFromName(customerFirstName, customerLastName);
		final List<CustomerGroup> customerGroups = new ArrayList<>();
		for (String customerGroupName : customerGroupNames) {
			customerGroups.add(customerGroupService.findByGroupName(customerGroupName));
		}

		final Customer customer = customerBuilder.newInstance()
				.withUidPk(lastCustomerUid++)
				.withGuid(customerGuid)
				.withFirstName(customerFirstName)
				.withLastName(customerLastName)
				.withCustomerGroups(customerGroups.toArray(new CustomerGroup[customerGroups.size()]))
				.build();

		addCustomer(customer);

		// Clone the customer so that modification to the returned customer doesn't affect the stored copy
		// This emulates the persistence engine such that changes to the saved copy would require an explicit update
		final Customer clonedCustomer = (Customer) SerializationUtils.clone(customer);
		Mockito.when(customerService.findByGuid(customerGuid)).thenReturn(clonedCustomer);
		Mockito.when(customerService.findByGuid(Mockito.eq(customerGuid), Mockito.any(FetchGroupLoadTuner.class)))
			.thenReturn(clonedCustomer);
	}

	/**
	 * Ensure customer not assigned to groups.
	 *
	 * @param customerFirstName the customer first name
	 * @param customerLastName the customer last name
	 */
	@Then("the customer \\[(\\w+) (\\w+)\\] should not be assigned to any customer segment$")
	public void ensureCustomerNotAssignedToGroups(final String customerFirstName, final String customerLastName) {
		validateCustomerAssignedToGroups(customerFirstName, customerLastName, Collections.<String>emptyList());
	}

	/**
	 * Ensure customer assigned to groups.
	 *
	 * @param customerFirstName the customer first name
	 * @param customerLastName the customer last name
	 * @param customerGroupNames the customer group names
	 */
	@Then("the customer \\[(\\w+) (\\w+)\\] should be assigned to customer segments? \\[([A-Z0-9_,]+)\\]$")
	public void ensureCustomerAssignedToGroups(final String customerFirstName, final String customerLastName,
			final String customerGroupNames) {
		validateCustomerAssignedToGroups(customerFirstName, customerLastName, Arrays.asList(customerGroupNames.split(",")));
	}

	private void validateCustomerAssignedToGroups(final String customerFirstName, final String customerLastName,
			final List<String> customerGroupNames) {

		final String customerGuid = generateCustomerGuidFromName(customerFirstName, customerLastName);
		final Customer persistedCustomer = findPersistedCustomerByGuid(customerGuid);

		final Collection<String> persistedCustomerGroupNames =
				Collections2.transform(persistedCustomer.getCustomerGroups(), new Function<CustomerGroup, String>() {
					@Override
					public String apply(final CustomerGroup customerGroup) {
						return customerGroup.getName();
					}
				});

		Assert.assertTrue(
				String.format("Customer [%s %s] was not assigned the correct customer groups", customerFirstName, customerLastName),
				CollectionUtils.isEqualCollection(customerGroupNames, persistedCustomerGroupNames));
	}

	/**
	 * Adds a new customer.
	 *
	 * @param customer the customer
	 */
	public static void addCustomer(final Customer customer) {
		final Customer existingCustomer = findPersistedCustomerByGuid(customer.getGuid());

		Assert.assertNull(
				String.format("Save attempted on existing customer [%s %s] (update should be called instead)",
						customer.getFirstName(), customer.getLastName()),
				existingCustomer);

		persistedCustomers.add(customer);
	}

	/**
	 * Update an existing customer.
	 *
	 * @param customer the customer
	 */
	public static void updateCustomer(final Customer customer) {
		final String existingCustomerGuid = customer.getGuid();
		final Customer existingCustomer = findPersistedCustomerByGuid(existingCustomerGuid);

		Assert.assertNotNull(
				String.format("Update attempted on non-existent customer [%s %s]",
						customer.getFirstName(), customer.getLastName()),
				existingCustomer);

		// remove existing customer by guid
		persistedCustomers = new ArrayList<>(
			Collections2.filter(persistedCustomers, new Predicate<Customer>() {
				@Override
				public boolean apply(final Customer customer) {
					return !existingCustomerGuid.equals(customer.getGuid());
				}
			})
		);

		addCustomer(customer);
	}

	/**
	 * Generate customer guid from name.
	 *
	 * @param firstName the first name
	 * @param lastName the last name
	 * @return the string
	 */
	public static String generateCustomerGuidFromName(final String firstName, final String lastName) {
		return String.format("guid_%s_%s", firstName, lastName);
	}

	/**
	 * Find the persisted customer by guid.
	 *
	 * @param guid the guid
	 * @return the customer
	 */
	public static Customer findPersistedCustomerByGuid(final String guid) {
		final Collection<Customer> foundCustomers =
				Collections2.filter(persistedCustomers, new Predicate<Customer>() {
					@Override
					public boolean apply(final Customer customer) {
						return guid.equals(customer.getGuid());
					}
				});

		if (foundCustomers.isEmpty()) {
			return null;
		}

		return foundCustomers.iterator().next();
	}

	/**
	 * Gets the persisted customers.
	 *
	 * @return the persisted customers
	 */
	public static List<Customer> getPersistedCustomers() {
		return persistedCustomers;
	}
}

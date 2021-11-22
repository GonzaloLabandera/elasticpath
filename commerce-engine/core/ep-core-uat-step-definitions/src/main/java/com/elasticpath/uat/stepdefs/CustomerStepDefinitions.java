/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.uat.stepdefs;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.CustomerRegistrationService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.uat.ScenarioContextValueHolder;

/**
 * Step Definitions for Customer-based functionality.
 */
public class CustomerStepDefinitions {

	@Autowired
	private ScenarioContextValueHolder<CustomerBuilder> customerBuilderHolder;

	@Autowired
	private ScenarioContextValueHolder<Customer> customerHolder;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerRegistrationService customerRegistrationService;

	@Autowired
	private ScenarioContextValueHolder<Runnable> emailSendingCommandHolder;

	@Before(order = 1100)
	public void initialiseCustomerBuilderHolder() {
		customerBuilderHolder.set(customerBuilder);
	}

	@Given("^(?:the customer's|my) shopping locale is (.+)$")
	public void setCustomerLocale(final String localeStr) {
		customerBuilderHolder.set(customerBuilderHolder.get().withPreferredLocale(LocaleUtils.toLocale(localeStr)));
	}

	@Given("^(?:the customer's|my) email address (.+) has been used to register (?:my|their) user account$")
	public void setCustomerUserIdAndEmail(final String customerEmailAddress) {
		customerBuilderHolder.set(customerBuilderHolder.get()
				.withSharedId(customerEmailAddress)
				.withEmail(customerEmailAddress));
	}

	@When("^(?:I|(?:a|the) customer) registers? as a new user with email address (.+)$")
	public void registerNewCustomer(final String emailAddress) throws Exception {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			setCustomerUserIdAndEmail(emailAddress);
			final Customer customer = buildCustomer();
			customerHolder.set(customerService.addByAuthenticate(customer, false));
		});
	}

	@When("^an anonymous customer registers as a new user with email address (.+)$")
	public void registerAnonymousCustomer(final String emailAddress) throws Exception {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			final Customer customer = buildCustomer();
			final Customer persistedAnonymousCustomer = customerService.addByAuthenticate(customer, false);
			persistedAnonymousCustomer.setSharedId(emailAddress);
			persistedAnonymousCustomer.setEmail(emailAddress);
			persistedAnonymousCustomer.setUsername(emailAddress);
			customerHolder.set(customerRegistrationService.registerCustomer(persistedAnonymousCustomer));
		});
	}

	@When("^a CSR creates a new customer account with email address (.+)")
	public void registerCustomerFromCsr(final String emailAddress) throws Throwable {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			setCustomerUserIdAndEmail(emailAddress);
			final Customer customer = buildCustomer();
			customerHolder.set(customerRegistrationService.registerCustomerAndSendPassword(customer));
		});
	}

	@When("^(?:the customer|I) changes? (?:my|their) password$")
	public void changeCustomerPassword() throws Throwable {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			final Customer customer = buildCustomer();
			customerHolder.set(customerService.changePasswordAndSendEmail(customer, "NEWPASSWORD"));
		});
	}

	@When("^the customer's password is reset$")
	public void resetCustomerPassword() throws Throwable {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			final Customer customer = buildCustomer();

			Customer updatedCustomer = customerService.auditableResetPassword(customer);
			// The clear text password is a transient field that is mutated by auditableResetPassword
			// on the input customer. The updated customer returned would not have this field set,
			// but we need to know what the password is to make assertions in other test steps.
			updatedCustomer.setClearTextPassword(customer.getClearTextPassword());

			customerHolder.set(updatedCustomer);
		});
	}

	private Customer buildCustomer() {
		Customer customer = customerBuilderHolder.get().build();
		customerHolder.set(customer);
		return customer;
	}

}

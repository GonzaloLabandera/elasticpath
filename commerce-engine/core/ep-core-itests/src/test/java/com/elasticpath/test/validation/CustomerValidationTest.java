/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.validation;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.commons.constants.WebConstants;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerProfile;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.validation.groups.PasswordChange;

/**
 * Validation tests for {@link Customer}.
 */
public class CustomerValidationTest extends AbstractValidationTest {

	private Customer customer;

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	/** Test initialization. */
	@Before
	public void initialize() {
		customer = getBeanFactory().getBean(ContextIdNames.CUSTOMER);
		((CustomerImpl) customer).setUserIdMode(WebConstants.USE_EMAIL_AS_USER_ID_MODE);
	}

	/** Test validation wiring using null. */
	@Test(expected = IllegalArgumentException.class)
	public void testValidateNullCustomer() {
		getValidator().validate(null);
	}

	/** Test validation for email property. */
	@Test
	public void testValidateEmail() {
		Set<ConstraintViolation<Customer>> violations = getValidator().validate(customer);
		assertViolationsNotContains("unset value should pass", violations, "email"); // constraint handled elsewhere

		assertValidationViolation("no domain", customer, "email", "hferents");
		assertValidationViolation("domain has no root domain", customer, "email", "hferents@redhat");
		assertValidationSuccess(customer, "email", "hferents@redhat.com");
	}

	/** Test validation for username property. */
	@Test
	public void testValidateUsername() {
		Set<ConstraintViolation<Customer>> violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId(null);
		violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId(StringUtils.EMPTY);
		violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId("    ");
		violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId(StringUtils.repeat(USERNAME, 32));
		violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId("Ferentschik");
		violations = getValidator().validate(customer);
		assertViolationsContains(violations, USERNAME);

		customer.setUserId("hferents@redhat.com");
		violations = getValidator().validate(customer);
		assertViolationsNotContains(violations, USERNAME);
	}

	/** Test validation for password property. */
	@Test
	public void testValidatePassword() {
		Set<ConstraintViolation<Customer>> violations = getValidator().validate(customer, PasswordChange.class);
		assertViolationsContains(violations, PASSWORD);
		violations = getValidator().validate(customer); //the password should not be validated on the default group.
		assertViolationsNotContains(violations, PASSWORD);

		customer.setClearTextPassword("bad");
		violations = getValidator().validate(customer, PasswordChange.class);
		assertViolationsContains(violations, PASSWORD);
		assertViolationsMessageContainsToken(violations, PASSWORD, String.valueOf(Customer.MINIMUM_PASSWORD_LENGTH));
		assertViolationsMessageContainsToken(violations, PASSWORD, String.valueOf(GlobalConstants.SHORT_TEXT_MAX_LENGTH));
		violations = getValidator().validate(customer);
		assertViolationsNotContains(violations, PASSWORD);

		customer.setClearTextPassword("valid_password");
		violations = getValidator().validate(customer, PasswordChange.class);
		assertViolationsNotContains(violations, PASSWORD);
		violations = getValidator().validate(customer);
		assertViolationsNotContains(violations, PASSWORD);
	}

	/** A required attribute which isn't backed by a method on customer should validate. */
	@DirtiesDatabase
	@Test
	public void testValidateExtensionAttribute() {
		final String extensionAttributeKey = "ferentschik";

		// setup all required attributes
		Collection<Attribute> attributes = customer.getCustomerProfile().getProfileAttributes();
		Collection<Attribute> ootbAttributes = Collections2.filter(attributes, new Predicate<Attribute>() {
			@Override
			public boolean apply(final Attribute attribute) {
				return attribute.isRequired();
			}
		});

		populateAttributeValues(customer, ootbAttributes);

		Set<ConstraintViolation<Customer>> violations = getValidator().validate(customer);
		assertViolationsNotContains("Failed to validate with all required attributes?",
				violations,
				String.format("customerProfile.%s", extensionAttributeKey));

		// now add another unknown attribute
		Attribute attribute = getBeanFactory().getBean(ContextIdNames.ATTRIBUTE);
		attribute.setGlobal(true);
		attribute.setRequired(true);
		attribute.setKey(extensionAttributeKey);
		attribute.setAttributeUsage(AttributeUsageImpl.CUSTOMERPROFILE_USAGE);
		attribute.setAttributeType(AttributeType.SHORT_TEXT);

		AttributeService attributeService = getBeanFactory().getBean(ContextIdNames.ATTRIBUTE_SERVICE);
		attributeService.add(attribute);

		Customer extensionCustomer = getBeanFactory().getBean(ContextIdNames.CUSTOMER);
		populateAttributeValues(extensionCustomer, ootbAttributes);

		violations = getValidator().validate(extensionCustomer);
		assertViolationsContains("Should fail with a missing required attribute",
				violations,
				String.format("customerProfile.profileValueMap[%s]", extensionAttributeKey));
	}

	@SuppressWarnings("fallthrough")
	protected void populateAttributeValues(final Customer customer, final Collection<Attribute> attributes) {
		for (Attribute attr : attributes) {
			CustomerProfile customerProfile = customer.getCustomerProfile();
			switch (attr.getAttributeType().getOrdinal()) {
			case AttributeType.BOOLEAN_TYPE_ID:
				customerProfile.setProfileValue(attr.getKey(), true);
				break;
			case AttributeType.SHORT_TEXT_TYPE_ID:
			case AttributeType.LONG_TEXT_TYPE_ID:
				customerProfile.setProfileValue(attr.getKey(), "value");
			default:
				break;
			}
		}
	}

	/** Tests for the customer address property. */
	@Test
	public void testValidateCustomerAddresses() {
		Set<ConstraintViolation<Customer>> violations = getValidator().validate(customer);
		assertViolationsNotContains("address is not required", violations, "addresses");

		CustomerAddress customerAddress1 = createValidCustomerAddress();
		customer.getAddresses().add(customerAddress1);
		violations = getValidator().validate(customer);
		assertViolationsNotContains("valid address given", violations, "addresses[0].city");

		customerAddress1.setCity(null);
		violations = getValidator().validate(customer);
		assertViolationsContains("invalid city on address", violations, "addresses[0].city");

		CustomerAddress customerAddress2 = createValidCustomerAddress();
		customer.getAddresses().add(customerAddress2);
		violations = getValidator().validate(customer);
		assertViolationsNotContains("2nd valid address given", violations, "addresses[1].firstName");

		customerAddress2.setFirstName(null);
		violations = getValidator().validate(customer);
		assertViolationsContains("first name now invalid", violations, "addresses[1].firstName");
		assertViolationsContains("city still invalid", violations, "addresses[0].city");

		customerAddress1.setCity("a city");
		customerAddress2.setFirstName("a firstName");
		violations = getValidator().validate(customer);
		assertViolationsNotContains(violations, "addresses[1].firstName");
		assertViolationsNotContains(violations, "addresses[0].city");
	}

	public CustomerAddress createValidCustomerAddress() {
		CustomerAddress customerAddress = new CustomerAddressImpl();
		customerAddress.setCity("city");
		customerAddress.setCountry("country");
		customerAddress.setFirstName("firstName");
		customerAddress.setLastName("lastName");
		customerAddress.setStreet1("street1");
		customerAddress.setZipOrPostalCode("zip");
		customerAddress.setPhoneNumber("phone");
		return customerAddress;
	}
}

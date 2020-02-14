/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
// CHECKSTYLE:OFF
/**
 * Source code generated by Fluent Builders Generator
 * Do not modify this file
 * See generator home page at: http://code.google.com/p/fluent-builders-generator-eclipse-plugin/
 */

package com.elasticpath.test.factory;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerProfile;
import com.elasticpath.domain.customer.impl.CustomerImpl;

public class CustomerBuilder extends CustomerImplBuilderBase<CustomerBuilder> {
	public static CustomerBuilder newCustomer() {
		return new CustomerBuilder(null);
	}

	public static CustomerBuilder newCustomer(final BeanFactory beanFactory) {
		return new CustomerBuilder(beanFactory);
	}

	public CustomerBuilder(final BeanFactory beanFactory) {
		super(createCustomer(beanFactory));
	}

	protected static CustomerImpl createCustomer(final BeanFactory beanFactory) {
		CustomerImpl customer;
		if (beanFactory == null) {
			customer = new CustomerImpl();
		} else {
			customer = new CustomerImpl() {
				private static final long serialVersionUID = 740L;

				@Override
				public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
					return beanFactory.getPrototypeBean(name, clazz);
				}

			};
		}
		customer.initialize();
		customer.setCustomerProfileAttributes(new TestCustomerProfileFactory().getProfile());

		return customer;
	}

	public CustomerImpl build() {
		return getInstance();
	}
}

@SuppressWarnings({ "unchecked", "PMD.TooManyMethods" })
class CustomerImplBuilderBase<GeneratorT extends CustomerImplBuilderBase<GeneratorT>> {
	private final CustomerImpl instance;

	protected CustomerImplBuilderBase(final CustomerImpl aInstance) {
		instance = aInstance;
	}

	protected CustomerImpl getInstance() {
		return instance;
	}

	public GeneratorT withUserId(final String aValue) {
		instance.setUserId(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withEmail(final String aValue) {
		instance.setEmail(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withAddresses(final List<CustomerAddress> aValue) throws EpDomainException {
		instance.setAddresses(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withAddedAddress(final CustomerAddress aValue) throws EpDomainException {
		if (instance.getAddresses() == null) {
			instance.setAddresses(new ArrayList<>());
		}

		instance.getAddresses().add(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPreferredShippingAddress(final CustomerAddress aValue) {
		instance.setPreferredShippingAddress(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPreferredBillingAddress(final CustomerAddress aValue) {
		instance.setPreferredBillingAddress(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPreferredLocale(final Locale aValue) {
		instance.setPreferredLocale(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPreferredCurrency(final Currency aValue) {
		instance.setPreferredCurrency(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withFirstName(final String aValue) {
		instance.setFirstName(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withLastName(final String aValue) {
		instance.setLastName(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPassword(final String password, final String salt) {
		instance.setPassword(password, salt);

		return (GeneratorT) this;
	}

	public GeneratorT withClearTextPassword(final String aValue) {
		instance.setClearTextPassword(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withAnonymous(final boolean aValue) {
		instance.setAnonymous(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withCreationDate(final Date aValue) {
		instance.setCreationDate(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withLastEditDate(final Date aValue) {
		instance.setLastEditDate(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withDateOfBirth(final Date aValue) {
		instance.setDateOfBirth(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withCustomerGroups(final List<CustomerGroup> aValue) {
		instance.setCustomerGroups(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withAddedCustomerGroup(final CustomerGroup aValue) {
		if (instance.getCustomerGroups() == null) {
			instance.setCustomerGroups(new ArrayList<>());
		}

		instance.getCustomerGroups().add(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withPhoneNumber(final String aValue) {
		instance.setPhoneNumber(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withGender(final char aValue) {
		instance.setGender(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withCompany(final String aValue) {
		instance.setCompany(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withStatus(final int aValue) {
		instance.setStatus(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withCustomerProfile(final CustomerProfile aValue) {
		instance.setCustomerProfile(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withCustomerAuthentication(final CustomerAuthentication aValue) {
		instance.setCustomerAuthentication(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withToBeNotified(final boolean aValue) {
		instance.setToBeNotified(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withGuid(final String aValue) {
		instance.setGuid(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withHtmlEmailPreferred(final boolean aValue) {
		instance.setHtmlEmailPreferred(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withStoreCode(final String aValue) {
		instance.setStoreCode(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withUidPk(final long aValue) {
		instance.setUidPk(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withFaxNumber(final String aValue) {
		instance.setFaxNumber(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withLastModifiedDate(final Date aValue) {
		instance.setLastModifiedDate(aValue);

		return (GeneratorT) this;
	}

	public GeneratorT withFirstTimeBuyer(final boolean aValue) {
		instance.setFirstTimeBuyer(aValue);

		return (GeneratorT) this;
	}

}

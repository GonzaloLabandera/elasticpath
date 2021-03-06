/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.builder.customer;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.DomainObjectBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.CustomerType;

/**
 * A builder that builds {@link Customer}s for testing purposes.
 */
public class CustomerBuilder implements DomainObjectBuilder<Customer> {

	@Autowired
    private BeanFactory beanFactory;

	private String guid;
	private Long uidPk;
	private String sharedId;
	private Locale preferredLocale;
	private String username;
	private String email;
	private String firstName;
	private String lastName;
	private String clearTextPassword;
	private CustomerType customerType;
	private Date creationDate;
	private String storeCode;
	private int status;
	private CustomerGroup[] customerGroups = new CustomerGroup[] {};
	private String phone;

	public CustomerBuilder newInstance() {
		final CustomerBuilder newBuilder = new CustomerBuilder();
		newBuilder.setBeanFactory(beanFactory);

		return newBuilder;
	}

	public CustomerBuilder withUidPk(final Long uidPk) {
		this.uidPk = uidPk;
		return this;
	}

	public CustomerBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	public CustomerBuilder withSharedId(final String userId) {
        this.sharedId = userId;
        return this;
    }

    public CustomerBuilder withUsername(final String username) {
		this.username = username;
		return this;
	}

    public CustomerBuilder withEmail(final String email) {
        this.email = email;
        return this;
    }

    public CustomerBuilder withPreferredLocale(final Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
        return this;
    }

    public CustomerBuilder withFirstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CustomerBuilder withLastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CustomerBuilder withCustomerType(final CustomerType customerType) {
        this.customerType = customerType;
        return this;
    }

    public CustomerBuilder withCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public CustomerBuilder withStoreCode(final String storeCode) {
        this.storeCode = storeCode;
        return this;
    }

    public CustomerBuilder withStatus(final int status) {
        this.status = status;
        return this;
    }

    public CustomerBuilder withCustomerGroups(final CustomerGroup... customerGroups) {
        this.customerGroups = customerGroups;
        return this;
    }

    public CustomerBuilder withClearTextPassword(final String clearTextPassword) {
        this.clearTextPassword = clearTextPassword;
        return this;
    }
    
    public CustomerBuilder withPhoneNumber(final String phoneNumber) {
        this.phone = phoneNumber;
        return this;
    }

    @Override
    public Customer build() {
        Customer customer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
        setIfNotNull(customer::setUidPk, uidPk);
        setIfNotNull(customer::setGuid, guid);
        setIfNotNull(customer::setSharedId, sharedId);
		customer.setEmail(ObjectUtils.defaultIfNull(email, "john.smith@elasticpath.com"));
		customer.setUsername(ObjectUtils.defaultIfNull(username, "john.smith@elasticpath.com"));
		customer.setPreferredLocale(ObjectUtils.defaultIfNull(preferredLocale, Locale.ENGLISH));
        customer.setFirstName(ObjectUtils.defaultIfNull(firstName, "James"));
        customer.setLastName(ObjectUtils.defaultIfNull(lastName, "Bond"));
        customer.setCreationDate(ObjectUtils.defaultIfNull(creationDate, new Date()));
        customer.setStatus(ObjectUtils.defaultIfNull(status, Customer.STATUS_ACTIVE));
        customer.setClearTextPassword(ObjectUtils.defaultIfNull(clearTextPassword, "password"));
        customer.setStoreCode(ObjectUtils.defaultIfNull(storeCode, "storeCode"));
        customer.setCustomerType(ObjectUtils.defaultIfNull(customerType, CustomerType.REGISTERED_USER));
        customer.setPhoneNumber(phone);

        if (customerGroups != null) {
            customer.getCustomerGroups().addAll(Arrays.asList(customerGroups));
        }

        return customer;
    }

    private <T> void setIfNotNull(final Consumer<T> setter, final T value) {
		if (value != null) {
			setter.accept(value);
		}
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}

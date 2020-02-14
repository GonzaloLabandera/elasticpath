/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.builder.customer;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.DomainObjectBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;

/**
 * A builder that builds {@link Customer}s for testing purposes.
 */
public class CustomerBuilder implements DomainObjectBuilder<Customer> {

	@Autowired
    private BeanFactory beanFactory;

	private String guid;
	private Long uidPk;
	private String userId;
	private Locale preferredLocale;
	private String email;
	private String firstName;
	private String lastName;
	private String clearTextPassword;
	private Boolean anonymous;
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

	public CustomerBuilder withUserId(final String userId) {
        this.userId = userId;
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

    public CustomerBuilder withAnonymous(final Boolean anonymous) {
        this.anonymous = anonymous;
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
        setIfNotNull(customer::setUserId, userId);
		customer.setEmail((String) ObjectUtils.defaultIfNull(email, "john.smith@elasticpath.com"));
		customer.setPreferredLocale((Locale) ObjectUtils.defaultIfNull(preferredLocale, Locale.ENGLISH));
        customer.setFirstName((String) ObjectUtils.defaultIfNull(firstName, "James"));
        customer.setLastName((String) ObjectUtils.defaultIfNull(lastName, "Bond"));
        customer.setCreationDate((Date) ObjectUtils.defaultIfNull(creationDate, new Date()));
        customer.setStatus((Integer) ObjectUtils.defaultIfNull(status, Customer.STATUS_ACTIVE));
        customer.setClearTextPassword((String) ObjectUtils.defaultIfNull(clearTextPassword, "password"));
        customer.setStoreCode((String) ObjectUtils.defaultIfNull(storeCode, "storeCode"));
        customer.setAnonymous((Boolean) ObjectUtils.defaultIfNull(anonymous, Boolean.FALSE));
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

/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.customer.impl;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.core.messaging.customer.CustomerEventType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.customer.CustomerRegistrationResult;
import com.elasticpath.service.customer.CustomerRegistrationService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Services providing customer registration operations.
 */
public class CustomerRegistrationServiceImpl implements CustomerRegistrationService {

	private static final String PASSWORD_KEY = "password";

	private BeanFactory beanFactory;
	private CustomerService customerService;
	private EventMessagePublisher eventMessagePublisher;
	private EventMessageFactory eventMessageFactory;

	@Override
	public Customer registerCustomer(final Customer customer) {
		customer.setCustomerType(CustomerType.REGISTERED_USER);

		final CustomerRegistrationResult result = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER_REGISTRATION_RESULT,
				CustomerRegistrationResult.class);
		final Customer updatedCustomer = getCustomerService().update(customer, true);
		result.setRegisteredCustomer(updatedCustomer);

		sendCustomerEvent(CustomerEventType.CUSTOMER_REGISTERED, customer.getGuid(), null);

		return updatedCustomer;
	}

	@Override
	public Customer registerCustomerAndSendPassword(final Customer customer) {
		final String newPassword = customer.resetPassword();
		final Customer updatedCustomer = getCustomerService().update(customer);

		final Map<String, Object> additionalData = new HashMap<>();
		additionalData.put(PASSWORD_KEY, newPassword);

		sendCustomerEvent(CustomerEventType.ANONYMOUS_CUSTOMER_REGISTERED, updatedCustomer.getGuid(), additionalData);

		return updatedCustomer;
	}

	/**
	 * Sends an Event Message for Customer-related events.
	 *
	 * @param eventType the event type
	 * @param customerGuid the Customer GUID
	 * @param additionalData any additional data required for the given message
	 */
	protected void sendCustomerEvent(final EventType eventType, final String customerGuid, final Map<String, Object> additionalData) {
		try {
			final EventMessage orderCreatedEventMessage = getEventMessageFactory().createEventMessage(eventType, customerGuid, additionalData);

			getEventMessagePublisher().publish(orderCreatedEventMessage);

		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return this.eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

}

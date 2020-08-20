/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.customer.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.core.messaging.customer.CustomerEventType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.customer.CustomerRegistrationResult;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/**
 * Test class for {@link CustomerRegistrationServiceImpl}.
 */
public class CustomerRegistrationServiceImplTest {

	private static final String PASSWORD = "password";
	private static final String CUSTOMER_GUID = "CUST-1234";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory beanExpectations;

	@Mock
	private CustomerService customerService;

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private EventMessagePublisher eventMessagePublisher;

	private CustomerRegistrationServiceImpl customerRegistrationService;

	@Before
	public void setUp() {
		beanFactory = context.mock(BeanFactory.class);
		beanExpectations = new BeanFactoryExpectationsFactory(context, beanFactory);

		customerRegistrationService = new CustomerRegistrationServiceImpl();
		customerRegistrationService.setBeanFactory(beanFactory);
		customerRegistrationService.setCustomerService(customerService);
		customerRegistrationService.setEventMessageFactory(eventMessageFactory);
		customerRegistrationService.setEventMessagePublisher(eventMessagePublisher);

		context.checking(new Expectations() {
			{
				allowing(beanFactory).getPrototypeBean(ContextIdNames.CUSTOMER_REGISTRATION_RESULT, CustomerRegistrationResult.class);
					will(returnValue(new CustomerRegistrationResultImpl()));
			}
		});
	}

	@After
	public void tearDown() {
		beanExpectations.close();
	}

	@Test
	public void testRegisterCustomer() throws EmailException {
		final Customer customer = context.mock(Customer.class);
		final Customer updatedCustomer = new CustomerImpl();

		context.checking(new Expectations() {
			{
				oneOf(customer).setCustomerType(CustomerType.REGISTERED_USER);
				oneOf(customerService).update(customer, true); will(returnValue(updatedCustomer));
				atLeast(1).of(customer).getGuid(); will(returnValue(CUSTOMER_GUID));

				final EventMessage eventMessage = context.mock(EventMessage.class);
				oneOf(eventMessageFactory).createEventMessage(CustomerEventType.CUSTOMER_REGISTERED, CUSTOMER_GUID, null);
				will(returnValue(eventMessage));

				oneOf(eventMessagePublisher).publish(eventMessage);
			}
		});

		Customer registeredCustomer = customerRegistrationService.registerCustomer(customer);

		assertEquals("Result customer should be the updated customer.", updatedCustomer, registeredCustomer);
	}

	@Test
	public void testRegisterCustomerAndSendPassword() throws EmailException {
		final Customer customer = context.mock(Customer.class);
		final Customer updatedCustomer = context.mock(Customer.class, "Updated Customer");

		context.checking(new Expectations() {
			{
				oneOf(customer).resetPassword(); will(returnValue(PASSWORD));
				oneOf(customerService).update(customer); will(returnValue(updatedCustomer));
				atLeast(1).of(updatedCustomer).getGuid(); will(returnValue(CUSTOMER_GUID));

				final EventMessage eventMessage = context.mock(EventMessage.class);
				final Map<String, Object> eventMessageData = Collections.<String, Object>singletonMap("password", PASSWORD);
				oneOf(eventMessageFactory).createEventMessage(CustomerEventType.ANONYMOUS_CUSTOMER_REGISTERED, CUSTOMER_GUID, eventMessageData);
				will(returnValue(eventMessage));

				oneOf(eventMessagePublisher).publish(eventMessage);
			}
		});

		Customer resultCustomer = customerRegistrationService.registerCustomerAndSendPassword(customer);

		assertEquals("Result customer should be same as updated customer.", updatedCustomer, resultCustomer);
	}
}

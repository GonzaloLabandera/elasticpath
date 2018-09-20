/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.impl.OrderRepositoryImpl;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;

/**
 * Tests for {@link OrderRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderRepositoryImplTest {

	private static final String STORE_CODE = "STORE_CODE";
	private static final String ORDER_GUID = "ORDER_GUID";
	private static final String USER_GUID = "USER_GUID";

	@Mock
	private OrderService orderService;
	@Mock
	private BeanFactory coreBeanFactory;
	@InjectMocks
	private OrderRepositoryImpl orderRepository;


	@Test
	public void testFindByGuid() {
		Order order = mock(Order.class);
		when(orderService.findOrderByOrderNumber(ORDER_GUID)).thenReturn(order);

		ExecutionResult<Order> result = orderRepository.findByGuid(STORE_CODE, ORDER_GUID);

		assertTrue("This should be a successful operation.", result.isSuccessful());
		assertEquals("The resulting data should contain the expected order.", order, result.getData());
	}

	@Test
	public void testFindByGuidWithoutFindingAMatch() {
		when(orderService.findOrderByOrderNumber(ORDER_GUID)).thenReturn(null);

		ExecutionResult<Order> result = orderRepository.findByGuid(STORE_CODE, ORDER_GUID);

		assertTrue("This should result in a failed operation.", result.isFailure());
		assertEquals("The result status should be as expected.", ResourceStatus.NOT_FOUND, result.getResourceStatus());
	}

	@Test
	public void testGetOrdersForProfileWhenOneFound() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();
		shouldGetBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA, new CustomerSearchCriteria());
		shouldGetBean(ContextIdNames.ORDER_SEARCH_CRITERIA, orderSearchCriteria);
		shouldFindOrderNumbersBySearchCriteria(orderSearchCriteria, Collections.singletonList(ORDER_GUID));

		orderRepository.findOrderIdsByCustomerGuid(STORE_CODE, USER_GUID)
				.test()
				.assertValue(ORDER_GUID);
	}

	@Test
	public void testGetOrdersForProfileWhenMultipleFound() {
		List<String> orderGuids = Arrays.asList(ORDER_GUID, "guid2");
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();
		shouldGetBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA, new CustomerSearchCriteria());
		shouldGetBean(ContextIdNames.ORDER_SEARCH_CRITERIA, orderSearchCriteria);
		shouldFindOrderNumbersBySearchCriteria(orderSearchCriteria, orderGuids);

		orderRepository.findOrderIdsByCustomerGuid(STORE_CODE, USER_GUID)
				.test()
				.assertValueCount(2);
	}

	@Test
	public void testGetOrdersForProfileWhenNoOrdersFound() {
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();
		shouldGetBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA, new CustomerSearchCriteria());
		shouldGetBean(ContextIdNames.ORDER_SEARCH_CRITERIA, orderSearchCriteria);
		shouldFindOrderNumbersBySearchCriteria(orderSearchCriteria, Collections.emptyList());

		orderRepository.findOrderIdsByCustomerGuid(STORE_CODE, USER_GUID)
				.isEmpty()
				.test()
				.assertValue(true);
	}

	private void shouldFindOrderNumbersBySearchCriteria(final OrderSearchCriteria orderSearchCriteria, final List<String> result) {
		when(orderService.findOrderNumbersBySearchCriteria(orderSearchCriteria, 0, Integer.MAX_VALUE)).thenReturn(result);
	}

	private void shouldGetBean(final String beanName, final Object bean) {
		when(coreBeanFactory.getBean(beanName)).thenReturn(bean);
	}

}

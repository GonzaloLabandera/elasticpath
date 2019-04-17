/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.sellingchannel.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.elasticpath.common.dto.OrderItemDto;
import com.elasticpath.sellingchannel.presentation.OrderItemPresentationBean;
import com.elasticpath.sellingchannel.presentation.impl.OrderItemDtoListMapperImpl;
import com.elasticpath.sellingchannel.presentation.impl.OrderItemPresentationBeanMapperImpl;

/**
 * Tests the {@code OrderItemDtoListMapperImpl}.
 */
public class OrderItemDtoListMapperImplTest {
	/**
	 * Tests that mapping from {@code OrderItemDto} list to {@code OrderItemPresentationBean} list works.
	 */
	@Test
	public void testListMapping() {
		OrderItemDtoListMapperImpl mapper = new OrderItemDtoListMapperImpl();
		OrderItemPresentationBeanMapperImpl itemMapper = mock(OrderItemPresentationBeanMapperImpl.class);
		List<OrderItemDto> orderItemDtoList = new ArrayList<>();
		OrderItemDto dto1 = new OrderItemDto();
		OrderItemDto dto2 = new OrderItemDto();
		OrderItemPresentationBean document1 = mock(OrderItemPresentationBean.class);
		OrderItemPresentationBean document2 = mock(OrderItemPresentationBean.class);

		mapper.setOrderItemPresentationBeanMapper(itemMapper);

		orderItemDtoList.add(dto1);
		orderItemDtoList.add(dto2);

		when(document1.getOrderItemFields()).thenReturn(dto1.getItemFields());
		when(itemMapper.mapFrom(dto1)).thenReturn(document1);

		when(document2.getOrderItemFields()).thenReturn(dto2.getItemFields());
		when(itemMapper.mapFrom(dto2)).thenReturn(document2);

		assertThat(mapper.mapFrom(orderItemDtoList).size()).as("Expect 2 members - 1 for each dto").isEqualTo(2);
	}

}

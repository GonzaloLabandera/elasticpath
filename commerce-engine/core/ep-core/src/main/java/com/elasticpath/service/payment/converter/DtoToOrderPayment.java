/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.payment.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.PayerAuthValidationValue;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.plugin.payment.dto.OrderPaymentDto;

/**
 * Converter from OrderPaymentDto to OrderPayment.
 */
public class DtoToOrderPayment implements Converter<OrderPaymentDto, OrderPayment> {
	private BeanFactory beanFactory;

	@Override
	public OrderPayment convert(final OrderPaymentDto source) {
		ConversionService conversionService = beanFactory.getBean(ContextIdNames.CONVERSION_SERVICE);

		OrderPayment target = beanFactory.getBean(ContextIdNames.ORDER_PAYMENT);
		target.setPayerAuthValidationValue(conversionService.convert(source.getPayerAuthValidationValueDto(), PayerAuthValidationValue.class));
		BeanUtils.copyProperties(source, target);
		return target;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}

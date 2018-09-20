/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.payment.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.plugin.payment.dto.CardDetailsPaymentMethod;
import com.elasticpath.plugin.payment.dto.PayerAuthValidationValueDto;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.plugin.payment.dto.TokenPaymentMethod;
import com.elasticpath.service.payment.gateway.GiftCertificateOrderPaymentDto;

/**
 * Converter from OrderPayment to PaymentMethod.
 */
public class OrderPaymentToPaymentMethod implements Converter<OrderPayment, PaymentMethod> {
	private BeanFactory beanFactory;

	@Override
	public PaymentMethod convert(final OrderPayment source) {
		ConversionService conversionService = beanFactory.getBean(ContextIdNames.CONVERSION_SERVICE);

		PaymentMethod target;
		if (PaymentType.GIFT_CERTIFICATE.equals(source.getPaymentMethod())) {
			target = beanFactory.getBean(ContextIdNames.GIFT_CERTIFICATE_ORDER_PAYMENT_DTO);
			((GiftCertificateOrderPaymentDto) target).setPayerAuthValidationValueDto(
					conversionService.convert(source.getPayerAuthValidationValue(), PayerAuthValidationValueDto.class));
			((GiftCertificateOrderPaymentDto) target).setGiftCertificate(source.getGiftCertificate());
		} else if (PaymentType.PAYMENT_TOKEN.equals(source.getPaymentMethod())) {
			target = beanFactory.getBean(ContextIdNames.TOKEN_PAYMENT_METHOD);
			((TokenPaymentMethod) target).setValue(source.extractPaymentToken().getValue());
		} else {
			target = beanFactory.getBean(ContextIdNames.CARD_DETAILS_PAYMENT_METHOD);
			((CardDetailsPaymentMethod) target).setPayerAuthValidationValueDto(
					conversionService.convert(source.getPayerAuthValidationValue(), PayerAuthValidationValueDto.class));
		}
		BeanUtils.copyProperties(source, target);
		return target;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}

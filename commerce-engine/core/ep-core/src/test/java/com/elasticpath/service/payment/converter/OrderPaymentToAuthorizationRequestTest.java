/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.payment.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.impl.GiftCertificateImpl;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.order.impl.OrderPaymentImpl;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.plugin.payment.dto.CardDetailsPaymentMethod;
import com.elasticpath.plugin.payment.dto.MoneyDto;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.plugin.payment.dto.impl.CardDetailsPaymentMethodImpl;
import com.elasticpath.plugin.payment.dto.impl.MoneyDtoImpl;
import com.elasticpath.plugin.payment.transaction.AuthorizationTransactionRequest;
import com.elasticpath.plugin.payment.transaction.impl.AuthorizationTransactionRequestImpl;
import com.elasticpath.service.payment.gateway.GiftCertificateAuthorizationRequest;
import com.elasticpath.service.payment.gateway.impl.GiftCertificateAuthorizationRequestImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

public class OrderPaymentToAuthorizationRequestTest {
	private static final BigDecimal AMOUNT = new BigDecimal("100.25");
	private static final String CURRENCYCODE = "USD";
	private static final String REFERENCEID = "REFERENCEID";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory expectationsFactory;
	private final OrderPaymentToAuthorizationRequest orderPaymentToAuthorizationRequest = new OrderPaymentToAuthorizationRequest();
	private final MoneyDto moneyDto = new MoneyDtoImpl();
	private final CardDetailsPaymentMethod cardDetailsPaymentMethod = new CardDetailsPaymentMethodImpl();
	private final GiftCertificate giftCertificate = new GiftCertificateImpl();
	@Mock private ConversionService mockConversionService;

	@Before
	public void setUp() throws Exception {
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CONVERSION_SERVICE, mockConversionService);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.AUTHORIZATION_TRANSACTION_REQUEST, AuthorizationTransactionRequestImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.GIFT_CERTIFICATE_AUTHORIZATION_REQUEST,
				GiftCertificateAuthorizationRequestImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CARD_DETAILS_PAYMENT_METHOD, CardDetailsPaymentMethodImpl.class);
		orderPaymentToAuthorizationRequest.setBeanFactory(beanFactory);
		context.checking(new Expectations() {
			{
				allowing(mockConversionService).convert(with(any(OrderPaymentImpl.class)), with(same(MoneyDto.class)));
				will(returnValue(moneyDto));
				allowing(mockConversionService).convert(with(any(OrderPaymentImpl.class)), with(same(PaymentMethod.class)));
				will(returnValue(cardDetailsPaymentMethod));
			}
		});
	}

	@Test
	public void testConvertForCreditCard() throws Exception {
		OrderPayment source = new OrderPaymentImpl();
		source.setPaymentMethod(PaymentType.CREDITCARD);
		source.setAmount(AMOUNT);
		source.setCurrencyCode(CURRENCYCODE);
		source.setReferenceId(REFERENCEID);
		AuthorizationTransactionRequest target = orderPaymentToAuthorizationRequest.convert(source);
		assertEquals(moneyDto, target.getMoney());
		assertEquals(cardDetailsPaymentMethod, target.getPaymentMethod());
		assertEquals(REFERENCEID, target.getReferenceId());
	}

	@Test
	public void testConvertForGiftCertificate() throws Exception {
		OrderPayment source = new OrderPaymentImpl();
		source.setPaymentMethod(PaymentType.GIFT_CERTIFICATE);
		source.setAmount(AMOUNT);
		source.setCurrencyCode(CURRENCYCODE);
		source.setReferenceId(REFERENCEID);
		source.setGiftCertificate(giftCertificate);

		AuthorizationTransactionRequest target = orderPaymentToAuthorizationRequest.convert(source);
		assertTrue(target instanceof GiftCertificateAuthorizationRequest);
		assertEquals(moneyDto, target.getMoney());
		assertEquals(cardDetailsPaymentMethod, target.getPaymentMethod());
		assertEquals(REFERENCEID, target.getReferenceId());
		assertEquals(giftCertificate, ((GiftCertificateAuthorizationRequest) target).getGiftCertificate());
	}
}

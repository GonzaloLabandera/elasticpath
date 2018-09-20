/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.payment.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.net.InetAddresses;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.impl.GiftCertificateImpl;
import com.elasticpath.domain.misc.PayerAuthValidationValue;
import com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.order.impl.OrderPaymentImpl;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.plugin.payment.dto.CardDetailsPaymentMethod;
import com.elasticpath.plugin.payment.dto.PayerAuthValidationValueDto;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.plugin.payment.dto.impl.CardDetailsPaymentMethodImpl;
import com.elasticpath.plugin.payment.dto.impl.PayerAuthValidationValueDtoImpl;
import com.elasticpath.service.payment.gateway.GiftCertificateOrderPaymentDto;
import com.elasticpath.service.payment.gateway.impl.GiftCertificateOrderPaymentDtoImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

public class OrderPaymentToPaymentMethodTest {
	private static final String CARDTYPE = "VISA";
	private static final String CARDHOLDERNAME = "John Doe";
	private static final String CARDNUMBER = "4111111111111111";
	private static final String MASKEDCARDNUMBER = "xxxxxxxxxxxx1111";
	private static final String EXPIRYYEAR = "2020";
	private static final String EXPIRYMONTH = "05";
	private static final Date STARTDATE = new Date();
	private static final String ISSUENUMBER = "issueNumber";
	private static final String CVV2CODE = "123";
	private static final String REFERENCEID = "referenceId";
	private static final String EMAIL = "john.doe@elasticpath.com";
	private static final String TRANSACTIONTYPE = "AUTHORIZATION";
	private static final String GATEWAYTOKEN = "gatewayToken";
	private static final String IPADDRESS = InetAddresses.fromInteger(ThreadLocalRandom.current().nextInt()).getHostAddress();

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory expectationsFactory;
	private final OrderPaymentToPaymentMethod orderPaymentToPaymentMethod = new OrderPaymentToPaymentMethod();
	private final PayerAuthValidationValueDto payerAuthValidationDto = new PayerAuthValidationValueDtoImpl();
	private final PayerAuthValidationValue payerAuthValidationValue = new PayerAuthValidationValueImpl();
	private final GiftCertificate giftCertificate = new GiftCertificateImpl();
	@Mock private CreditCardEncrypter mockCreditCardEncrypter;
	@Mock private ConversionService mockConversionService;

	@Before
	public void setUp() throws Exception {
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CARD_DETAILS_PAYMENT_METHOD, CardDetailsPaymentMethodImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.GIFT_CERTIFICATE_ORDER_PAYMENT_DTO, GiftCertificateOrderPaymentDtoImpl.class);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CONVERSION_SERVICE, mockConversionService);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CREDIT_CARD_ENCRYPTER, mockCreditCardEncrypter);
		orderPaymentToPaymentMethod.setBeanFactory(beanFactory);
		context.checking(new Expectations() {
			{
				allowing(mockConversionService).convert(with(any(PayerAuthValidationValue.class)), with(same(PayerAuthValidationValueDto.class)));
				will(returnValue(payerAuthValidationDto));
				allowing(mockCreditCardEncrypter).encrypt(CARDNUMBER);
				will(returnValue(CARDNUMBER));
				allowing(mockCreditCardEncrypter).decrypt(CARDNUMBER);
				will(returnValue(CARDNUMBER));
				allowing(mockCreditCardEncrypter).mask(CARDNUMBER);
				will(returnValue(MASKEDCARDNUMBER));
			}
		});
	}

	@Test
	public void testConvertForCreditCard() throws Exception {
		OrderPayment source = new OrderPaymentImpl();
		source.setPaymentMethod(PaymentType.CREDITCARD);
		source.setCardType(CARDTYPE);
		source.setCardHolderName(CARDHOLDERNAME);
		source.setUnencryptedCardNumber(CARDNUMBER);
		source.setExpiryYear(EXPIRYYEAR);
		source.setExpiryMonth(EXPIRYMONTH);
		source.setStartDate(STARTDATE);
		source.setIssueNumber(ISSUENUMBER);
		source.setCvv2Code(CVV2CODE);
		source.setReferenceId(REFERENCEID);
		source.setEmail(EMAIL);
		source.setTransactionType(TRANSACTIONTYPE);
		source.setGatewayToken(GATEWAYTOKEN);
		source.setIpAddress(IPADDRESS);
		source.setPayerAuthValidationValue(payerAuthValidationValue);

		PaymentMethod target = orderPaymentToPaymentMethod.convert(source);
		assertTrue(target instanceof CardDetailsPaymentMethod);
		CardDetailsPaymentMethod cardDetailsTarget = (CardDetailsPaymentMethod) target;
		assertEquals(CARDTYPE, cardDetailsTarget.getCardType());
		assertEquals(CARDHOLDERNAME, cardDetailsTarget.getCardHolderName());
		assertEquals(CARDNUMBER, cardDetailsTarget.getUnencryptedCardNumber());
		assertEquals(EXPIRYYEAR, cardDetailsTarget.getExpiryYear());
		assertEquals(EXPIRYMONTH, cardDetailsTarget.getExpiryMonth());
		assertEquals(STARTDATE, cardDetailsTarget.getStartDate());
		assertEquals(ISSUENUMBER, cardDetailsTarget.getIssueNumber());
		assertEquals(CVV2CODE, cardDetailsTarget.getCvv2Code());
		assertEquals(REFERENCEID, cardDetailsTarget.getReferenceId());
		assertEquals(EMAIL, cardDetailsTarget.getEmail());
		assertEquals(TRANSACTIONTYPE, cardDetailsTarget.getTransactionType());
		assertEquals(GATEWAYTOKEN, cardDetailsTarget.getGatewayToken());
		assertEquals(IPADDRESS, cardDetailsTarget.getIpAddress());
		assertEquals(CARDNUMBER, cardDetailsTarget.getCardNumber());
		assertEquals(payerAuthValidationDto, cardDetailsTarget.getPayerAuthValidationValueDto());
	}

	@Test
	public void testConvertForGiftCertificate() throws Exception {
		OrderPayment source = new OrderPaymentImpl();
		source.setPaymentMethod(PaymentType.GIFT_CERTIFICATE);
		source.setCardType(CARDTYPE);
		source.setCardHolderName(CARDHOLDERNAME);
		source.setUnencryptedCardNumber(CARDNUMBER);
		source.setExpiryYear(EXPIRYYEAR);
		source.setExpiryMonth(EXPIRYMONTH);
		source.setStartDate(STARTDATE);
		source.setIssueNumber(ISSUENUMBER);
		source.setCvv2Code(CVV2CODE);
		source.setReferenceId(REFERENCEID);
		source.setEmail(EMAIL);
		source.setTransactionType(TRANSACTIONTYPE);
		source.setGatewayToken(GATEWAYTOKEN);
		source.setIpAddress(IPADDRESS);
		source.setGiftCertificate(giftCertificate);
		source.setPayerAuthValidationValue(payerAuthValidationValue);
		PaymentMethod target = orderPaymentToPaymentMethod.convert(source);

		assertTrue(target instanceof GiftCertificateOrderPaymentDto);
		GiftCertificateOrderPaymentDto giftCertificateTarget = (GiftCertificateOrderPaymentDto) target;
		assertEquals(CARDTYPE, giftCertificateTarget.getCardType());
		assertEquals(CARDHOLDERNAME, giftCertificateTarget.getCardHolderName());
		assertEquals(CARDNUMBER, giftCertificateTarget.getUnencryptedCardNumber());
		assertEquals(EXPIRYYEAR, giftCertificateTarget.getExpiryYear());
		assertEquals(EXPIRYMONTH, giftCertificateTarget.getExpiryMonth());
		assertEquals(STARTDATE, giftCertificateTarget.getStartDate());
		assertEquals(ISSUENUMBER, giftCertificateTarget.getIssueNumber());
		assertEquals(CVV2CODE, giftCertificateTarget.getCvv2Code());
		assertEquals(REFERENCEID, giftCertificateTarget.getReferenceId());
		assertEquals(EMAIL, giftCertificateTarget.getEmail());
		assertEquals(TRANSACTIONTYPE, giftCertificateTarget.getTransactionType());
		assertEquals(GATEWAYTOKEN, giftCertificateTarget.getGatewayToken());
		assertEquals(IPADDRESS, giftCertificateTarget.getIpAddress());
		assertEquals(CARDNUMBER, giftCertificateTarget.getCardNumber());
		assertEquals(payerAuthValidationDto, giftCertificateTarget.getPayerAuthValidationValueDto());
		assertEquals(giftCertificate, giftCertificateTarget.getGiftCertificate());
	}
}

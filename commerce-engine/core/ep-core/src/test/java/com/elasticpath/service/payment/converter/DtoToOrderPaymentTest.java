/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.payment.converter;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
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
import com.elasticpath.domain.misc.PayerAuthValidationValue;
import com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.order.impl.OrderPaymentImpl;
import com.elasticpath.plugin.payment.dto.OrderPaymentDto;
import com.elasticpath.plugin.payment.dto.PayerAuthValidationValueDto;
import com.elasticpath.plugin.payment.dto.impl.OrderPaymentDtoImpl;
import com.elasticpath.plugin.payment.dto.impl.PayerAuthValidationValueDtoImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

public class DtoToOrderPaymentTest {
	private static final String CARDTYPE = "VISA";
	private static final String CARDHOLDERNAME = "John Doe";
	private static final String CARDNUMBER = "4111111111111111";
	private static final String MASKEDCARDNUMBER = "xxxxxxxxxxxx1111";
	private static final String EXPIRYYEAR = "2020";
	private static final String EXPIRYMONTH = "05";
	private static final Date STARTDATE = new Date();
	private static final String ISSUENUMBER = "issueNumber";
	private static final String CVV2CODE = "123";
	private static final BigDecimal AMOUNT = new BigDecimal("100.25");
	private static final String REFERENCEID = "referenceId";
	private static final String REQUESTTOKEN = "requestToken";
	private static final String AUTHORIZATIONCODE = "authCode";
	private static final String CURRENCYCODE = "USD";
	private static final String EMAIL = "john.doe@elasticpath.com";
	private static final String TRANSACTIONTYPE = "AUTHORIZATION";
	private static final String GATEWAYTOKEN = "gatewayToken";
	private static final String IPADDRESS = InetAddresses.fromInteger(ThreadLocalRandom.current().nextInt()).getHostAddress();

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory expectationsFactory;
	private final DtoToOrderPayment dtoToOrderPayment = new DtoToOrderPayment();
	private final PayerAuthValidationValueDto payerAuthValidationValueDto = new PayerAuthValidationValueDtoImpl();
	private final PayerAuthValidationValue payerAuthValidationValue = new PayerAuthValidationValueImpl();
	@Mock private ConversionService mockConversionService;
	@Mock private CreditCardEncrypter mockCreditCardEncrypter;

	/**
	 * Set up and mocking.
	 */
	@Before
	public void setUp() {
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CONVERSION_SERVICE, mockConversionService);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CREDIT_CARD_ENCRYPTER, mockCreditCardEncrypter);
		expectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.ORDER_PAYMENT, OrderPaymentImpl.class);
		dtoToOrderPayment.setBeanFactory(beanFactory);
		context.checking(new Expectations() {
			{
				allowing(mockCreditCardEncrypter).encrypt(CARDNUMBER);
				will(returnValue(CARDNUMBER));
				allowing(mockCreditCardEncrypter).decrypt(CARDNUMBER);
				will(returnValue(CARDNUMBER));
				allowing(mockCreditCardEncrypter).mask(CARDNUMBER);
				will(returnValue(MASKEDCARDNUMBER));
				allowing(mockConversionService).convert(with(any(PayerAuthValidationValueDtoImpl.class)),
						with(same(PayerAuthValidationValue.class)));
				will(returnValue(payerAuthValidationValue));
			}
		});
	}

	@Test
	public void testConvert() throws Exception {
		OrderPaymentDto source = new OrderPaymentDtoImpl();
		source.setCardType(CARDTYPE);
		source.setCardHolderName(CARDHOLDERNAME);
		source.setUnencryptedCardNumber(CARDNUMBER);
		source.setExpiryYear(EXPIRYYEAR);
		source.setExpiryMonth(EXPIRYMONTH);
		source.setStartDate(STARTDATE);
		source.setIssueNumber(ISSUENUMBER);
		source.setCvv2Code(CVV2CODE);
		source.setAmount(AMOUNT);
		source.setReferenceId(REFERENCEID);
		source.setRequestToken(REQUESTTOKEN);
		source.setAuthorizationCode(AUTHORIZATIONCODE);
		source.setCurrencyCode(CURRENCYCODE);
		source.setEmail(EMAIL);
		source.setTransactionType(TRANSACTIONTYPE);
		source.setGatewayToken(GATEWAYTOKEN);
		source.setIpAddress(IPADDRESS);
		source.setPayerAuthValidationValueDto(payerAuthValidationValueDto);

		OrderPayment target = dtoToOrderPayment.convert(source);
		assertEquals(CARDTYPE, target.getCardType());
		assertEquals(CARDHOLDERNAME, target.getCardHolderName());
		assertEquals(CARDNUMBER, target.getUnencryptedCardNumber());
		assertEquals(EXPIRYYEAR, target.getExpiryYear());
		assertEquals(EXPIRYMONTH, target.getExpiryMonth());
		assertEquals(STARTDATE, target.getStartDate());
		assertEquals(ISSUENUMBER, target.getIssueNumber());
		assertEquals(CVV2CODE, target.getCvv2Code());
		assertEquals(AMOUNT, target.getAmount());
		assertEquals(REFERENCEID, target.getReferenceId());
		assertEquals(REQUESTTOKEN, target.getRequestToken());
		assertEquals(AUTHORIZATIONCODE, target.getAuthorizationCode());
		assertEquals(CURRENCYCODE, target.getCurrencyCode());
		assertEquals(EMAIL, target.getEmail());
		assertEquals(TRANSACTIONTYPE, target.getTransactionType());
		assertEquals(GATEWAYTOKEN, target.getGatewayToken());
		assertEquals(IPADDRESS, target.getIpAddress());
		assertEquals(payerAuthValidationValue, target.getPayerAuthValidationValue());
	}
}

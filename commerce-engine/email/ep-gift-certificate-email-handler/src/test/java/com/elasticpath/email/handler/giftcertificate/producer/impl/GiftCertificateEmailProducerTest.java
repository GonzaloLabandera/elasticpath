/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.email.handler.giftcertificate.producer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.email.EmailDto;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.giftcertificate.helper.GiftCertificateEmailPropertyHelper;
import com.elasticpath.email.producer.spi.composer.EmailComposer;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Test class for {@link com.elasticpath.email.handler.giftcertificate.producer.impl.GiftCertificateEmailProducer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GiftCertificateEmailProducerTest {

	private static final String GIFT_CERTIFICATE_GUID = "ABCD-1234-FGHI-5678";

	private static final String ORDER_GUID = "200000";

	private static final String ORDER_SKU_GUID = "ZYXW-8765-VUTS-4321";

	private static final String EMAIL_KEY = "emailAddress";

	private static final String RECIPIENT_EMAIL_ADDRESS = "recipient@elasticpath.com";

	@Mock
	private GiftCertificateEmailPropertyHelper giftCertificateEmailPropertyHelper;

	@Mock
	private EmailComposer emailComposer;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@InjectMocks
	private GiftCertificateEmailProducer emailProducer;

	@Test
	public void testVerifyThatGiftCertificateEmailIsConstructedForOrderAndOrderSku() throws Exception {
		final String giftCertificateThemeImageFilename = "hello.jpg";
		final Map<String, Object> emailData = createEmailDataMap(ORDER_GUID, ORDER_SKU_GUID, null);

		final EmailDto expectedEmail = EmailDto.builder()
				.withTo(RECIPIENT_EMAIL_ADDRESS)
				.build();

		final EmailProperties emailProperties = mock(EmailProperties.class);

		when(productSkuLookup.findImagePathBySkuGuid(ORDER_SKU_GUID)).thenReturn(giftCertificateThemeImageFilename);
		when(giftCertificateEmailPropertyHelper.getEmailProperties(giftCertificateThemeImageFilename, emailData))
				.thenReturn(emailProperties);
		when(emailComposer.composeMessage(emailProperties))
				.thenReturn(expectedEmail);

		final EmailDto actualEmail = emailProducer.createEmail(GIFT_CERTIFICATE_GUID, emailData);

		assertThat(actualEmail)
				.as("Unexpected Email instance produced")
				.isEqualTo(expectedEmail);
	}

	@Test
	public void testVerifyThatSpecifiedEmailAddressAddsToOriginalRecipient() throws Exception {
		final String overrideRecipientAddress = "override.recipient@elasticpath.com";
		final String giftCertificateThemeImageFilename = "hello.jpg";
		final Map<String, Object> emailData = createEmailDataMap(ORDER_GUID, ORDER_SKU_GUID, overrideRecipientAddress);

		final EmailDto emailDtoTemplate = EmailDto.builder()
				.withTo(RECIPIENT_EMAIL_ADDRESS)
				.build();

		final EmailDto expectedEmail = EmailDto.builder()
				.fromPrototype(emailDtoTemplate)
				.addTo(overrideRecipientAddress)
				.build();

		final EmailProperties emailProperties = mock(EmailProperties.class);

		when(productSkuLookup.findImagePathBySkuGuid(ORDER_SKU_GUID)).thenReturn(giftCertificateThemeImageFilename);
		when(giftCertificateEmailPropertyHelper.getEmailProperties(giftCertificateThemeImageFilename, emailData))
				.thenReturn(emailProperties);
		when(emailComposer.composeMessage(emailProperties))
				.thenReturn(emailDtoTemplate);



		final EmailDto actualEmail = emailProducer.createEmail(GIFT_CERTIFICATE_GUID, emailData);

		assertThat(actualEmail)
				.as("Unexpected Email instance produced")
				.isEqualTo(expectedEmail);
	}

	private Map<String, Object> createEmailDataMap(final String orderGuid, final String orderSkuGuid, final String recipient) {
		final Map<String, Object> emailDataMap = Maps.newHashMapWithExpectedSize(3);
		emailDataMap.put("orderGuid", orderGuid);
		emailDataMap.put("orderSkuGuid", orderSkuGuid);
		emailDataMap.put("orderLocale", "en");
		emailDataMap.put("orderStoreCode", "storeCode");
		emailDataMap.put("shipmentNumber", "12345-1");
		emailDataMap.put("shipmentType", "ELECTRONIC");
		emailDataMap.put("orderSkuTotalAmount", "10.00");
		emailDataMap.put("orderSkuTotalCurrency", "CAD");

		if (recipient != null) {
			emailDataMap.put(EMAIL_KEY, recipient);
		}

		return emailDataMap;
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.email.handler.giftcertificate.producer.impl;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.email.EmailDto;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.giftcertificate.helper.GiftCertificateEmailPropertyHelper;
import com.elasticpath.email.producer.spi.AbstractEmailProducer;
import com.elasticpath.email.producer.spi.composer.EmailComposer;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Creates an Gift Certificate {@link EmailDto} for a purchased gift certificate.
 */
public class GiftCertificateEmailProducer extends AbstractEmailProducer {

	private GiftCertificateEmailPropertyHelper giftCertificateEmailPropertyHelper;
	private EmailComposer emailComposer;
	private ProductSkuLookup productSkuLookup;

	private static final String EMAIL_KEY = "emailAddress";

	@Override
	public EmailDto createEmail(final String guid, final Map<String, Object> emailData) {
		final String giftCertificateThemeImageFilename = getGiftCertificateThemeImageFilename((String) emailData.get("orderSkuGuid"));

		final EmailProperties emailProperties = getGiftCertificateEmailPropertyHelper()
				.getEmailProperties(giftCertificateThemeImageFilename, emailData);

		EmailDto email = getEmailComposer().composeMessage(emailProperties);

		final Optional<String> recipient = getEmailAddress(emailData);

		if (recipient.isPresent()) {
			email = EmailDto.builder()
					.fromPrototype(email)
					.addTo(recipient.get())
					.build();
		}

		return email;
	}

	/**
	 * Checks the contextual data for an optional overriding email address.
	 * 
	 * @param emailData email contextual data
	 * @return the recipient email address
	 */
	protected Optional<String> getEmailAddress(final Map<String, Object> emailData) {
		final Object emailValue = emailData.get(EMAIL_KEY);

		return Optional.ofNullable(emailValue)
				.map(String::valueOf)
				.filter(StringUtils::isNotBlank);
	}

	/**
	 * Retrieves the Gift Certificate theme image filename from the given order sku guid.
	 *
	 * @param orderSkuGuid  the order sku guid
	 * @return a String representing the Gift Certificate theme image filename
	 */
	protected String getGiftCertificateThemeImageFilename(final String orderSkuGuid) {
		return getProductSkuLookup().findImagePathBySkuGuid(orderSkuGuid);
	}

	public void setGiftCertificateEmailPropertyHelper(final GiftCertificateEmailPropertyHelper giftCertificateEmailPropertyHelper) {
		this.giftCertificateEmailPropertyHelper = giftCertificateEmailPropertyHelper;
	}

	protected GiftCertificateEmailPropertyHelper getGiftCertificateEmailPropertyHelper() {
		return giftCertificateEmailPropertyHelper;
	}

	public void setEmailComposer(final EmailComposer emailComposer) {
		this.emailComposer = emailComposer;
	}

	protected EmailComposer getEmailComposer() {
		return emailComposer;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}
}

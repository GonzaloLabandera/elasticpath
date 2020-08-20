/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.email.handler.giftcertificate.helper;

import java.util.Map;

import com.elasticpath.email.domain.EmailProperties;

/**
 * Helper for constructing email properties.
 */
public interface GiftCertificateEmailPropertyHelper {

	/**
	 * Gets the {@link EmailProperties} for Gift Certificates confirmation e-mails.
	 * 
	 * @param giftCertificateThemeImageFilename the filename of the image to be used for the given gift certificate
	 * @param emailData the email data
	 *
	 * @return {@link EmailProperties}
	 */
	EmailProperties getEmailProperties(String giftCertificateThemeImageFilename, Map<String, Object> emailData);

}

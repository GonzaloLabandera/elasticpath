/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.email.handler.giftcertificate.helper.impl;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.giftcertificate.helper.GiftCertificateEmailPropertyHelper;
import com.elasticpath.money.Money;
import com.elasticpath.money.MoneyFormatter;

/**
 * Helper for processing email properties for gift certificate e-mails.
 */
public class GiftCertificateEmailPropertyHelperImpl implements GiftCertificateEmailPropertyHelper {

	private static final String GIFT_CERT_RECIPIENT_EMAIL_HTML_TEMPLATE = "GiftCertificateRecipientEmail.html";
	private static final String GIFT_CERT_RECIPIENT_EMAIL_TXT_TEMPLATE = "GiftCertificateRecipientEmail.txt";

	private MoneyFormatter moneyFormatter;
	private BeanFactory beanFactory;

	@Override
	public EmailProperties getEmailProperties(final String giftCertificateImageFilename, final Map<String, Object> emailData) {
		String storeCode = getDataValue(emailData, "orderStoreCode");
		Locale orderLocale = Locale.forLanguageTag(getDataValue(emailData, "orderLocale"));
		Money orderSkuTotal = getOrderSkuMoney(emailData);

		Map<String, Object> gcFields = getDataValue(emailData, "gcFields");

		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		Map<String, Object> templateMap = emailProperties.getTemplateResources();
		templateMap.put("senderEmail", gcFields.get(GiftCertificate.KEY_SENDER_EMAIL));
		templateMap.put("senderName", gcFields.get(GiftCertificate.KEY_SENDER_NAME));
		templateMap.put("purchaseAmount", convertBigDecimalStringToMoneyValueAndSymbol(orderSkuTotal, orderLocale));
		templateMap.put("giftCertCode", gcFields.get(GiftCertificate.KEY_CODE));
		templateMap.put("recipientName", gcFields.get(GiftCertificate.KEY_RECIPIENT_NAME));
		templateMap.put("recipientEmail", gcFields.get(GiftCertificate.KEY_RECIPIENT_EMAIL));
		templateMap.put("message", gcFields.get(GiftCertificate.KEY_MESSAGE));
		templateMap.put("locale", orderLocale);
		templateMap.put("giftCertificateThemeImageFilename", giftCertificateImageFilename);

		emailProperties.setLocaleDependentSubjectKey("email.giftCert.emailSubject");
		emailProperties.setDefaultSubject("Gift Certificate");
		emailProperties.setEmailLocale(orderLocale);
		emailProperties.setHtmlTemplate(GIFT_CERT_RECIPIENT_EMAIL_HTML_TEMPLATE);
		emailProperties.setTextTemplate(GIFT_CERT_RECIPIENT_EMAIL_TXT_TEMPLATE);
		emailProperties.setRecipientAddress((String) gcFields.get(GiftCertificate.KEY_RECIPIENT_EMAIL));
		emailProperties.setStoreCode(storeCode);

		return emailProperties;
	}

	@SuppressWarnings("unchecked")
	private <T>  T getDataValue(final Map<String, Object> data, final String key) {
		return (T) data.get(key);
	}

	private Money getOrderSkuMoney(final Map<String, Object> data) {
		String amount = getDataValue(data, "orderSkuTotalAmount");
		String currency = getDataValue(data, "orderSkuTotalCurrency");

		return Money.valueOf(new BigDecimal(amount), Currency.getInstance(currency));
	}

	/**
	 * Get a string representation of a decimal representation of an amount of money, including the currency symbol.
	 *
	 * @param orderSkuTotal the order sku total
	 * @param locale the order's locale
	 * @return the string
	 */
	String convertBigDecimalStringToMoneyValueAndSymbol(final Money orderSkuTotal, final Locale locale) {
		return getMoneyFormatter().formatCurrency(orderSkuTotal, locale);
	}

	private EmailProperties getEmailPropertiesBeanInstance() {
		return beanFactory.getPrototypeBean(ContextIdNames.EMAIL_PROPERTIES, EmailProperties.class);
	}

	public void setMoneyFormatter(final MoneyFormatter formatter) {
		this.moneyFormatter = formatter;
	}

	protected MoneyFormatter getMoneyFormatter() {
		return moneyFormatter;
	}
	
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

}

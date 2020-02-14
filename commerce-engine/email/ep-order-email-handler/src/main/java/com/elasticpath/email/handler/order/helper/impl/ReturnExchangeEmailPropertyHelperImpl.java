/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.email.handler.order.helper.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.order.helper.ReturnExchangeEmailPropertyHelper;
import com.elasticpath.service.email.EmailAddressesExtractionStrategy;

/**
 * Helper for constructing {@link OrderReturn} email properties.
 */
public class ReturnExchangeEmailPropertyHelperImpl implements ReturnExchangeEmailPropertyHelper {

	private static final String PHYSICAL_RMA_EMAIL_HTML_TEMPLATE = "RMA.html";

	private static final String PHYSICAL_RMA_EMAIL_TXT_TEMPLATE = "RMA.txt";
	
	private static final String GENERAL_RMA_EMAIL_HTML_TEMPLATE = "GRMA.html";

	private static final String GENERAL_RMA_EMAIL_TXT_TEMPLATE = "GRMA.txt";

	private static final String LOCALE_KEY_FOR_VM_TEMPLATE = "locale";

	private BeanFactory beanFactory;

	private EmailAddressesExtractionStrategy emailAddressesExtractionStrategy;

	@Override
	public EmailProperties getOrderReturnEmailProperties(final OrderReturn orderReturn) {
		final Order order = orderReturn.getOrder();
		EmailProperties emailProperties = getBeanFactory().getPrototypeBean(ContextIdNames.EMAIL_PROPERTIES, EmailProperties.class);
		emailProperties.getTemplateResources().put("orderReturn", orderReturn);
		emailProperties.getTemplateResources().put(LOCALE_KEY_FOR_VM_TEMPLATE, order.getLocale());
		emailProperties.setDefaultSubject("Order Return Confirmation");
		emailProperties.setLocaleDependentSubjectKey("RMA.emailSubject");
		emailProperties.setEmailLocale(order.getLocale());
		emailProperties.setStoreCode(order.getStoreCode());
		emailProperties.setRecipientAddress(getInlineRecipientAddress(order));

		setEmailTemplate(emailProperties, orderReturn);
		
		return emailProperties;
	}

	/**
	 * Gets the inline recipient addresses from order.
	 * @param order the order.
	 * @return the inline recipient addresses.
	 */
	private String getInlineRecipientAddress(final Order order) {
		return emailAddressesExtractionStrategy.extractToInline(order);
	}

	private void setEmailTemplate(final EmailProperties emailProperties, final OrderReturn orderReturn) {
		
		if (orderReturn.getPhysicalReturn()) {
			emailProperties.setHtmlTemplate(PHYSICAL_RMA_EMAIL_HTML_TEMPLATE);
			emailProperties.setTextTemplate(PHYSICAL_RMA_EMAIL_TXT_TEMPLATE);		
		} else {
			emailProperties.setHtmlTemplate(GENERAL_RMA_EMAIL_HTML_TEMPLATE);
			emailProperties.setTextTemplate(GENERAL_RMA_EMAIL_TXT_TEMPLATE);
		}
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setEmailAddressesExtractionStrategy(final EmailAddressesExtractionStrategy emailAddressesExtractionStrategy) {
		this.emailAddressesExtractionStrategy = emailAddressesExtractionStrategy;
	}
}

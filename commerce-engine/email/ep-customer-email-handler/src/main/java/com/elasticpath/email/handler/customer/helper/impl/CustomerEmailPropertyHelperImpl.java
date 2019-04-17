/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.email.handler.customer.helper.impl;

import static java.util.Optional.ofNullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.impl.AbstractEpDomainImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.customer.helper.CustomerEmailPropertyHelper;
import com.elasticpath.service.store.StoreService;

/**
 * Helper for processing email properties for Customer e-mails.
 */
public class CustomerEmailPropertyHelperImpl extends AbstractEpDomainImpl implements CustomerEmailPropertyHelper {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;


	private static final String LOCALE = "locale";

	private static final String CONFIRM_PWD_EMAIL_TXT_TEMPLATE = "confirmPasswd.txt";
	private static final String REGISTER_CUSTOMER_EMAIL_TXT_TEMPLATE = "sendRegisteredCustomerPasswd.txt";
	private static final String FORGOTTEN_PWD_EMAIL_TXT_TEMPLATE = "customerForgottenPasswd.txt";
	private static final String CREATE_ACCOUNT_EMAIL_HTML_TEMPLATE = "createAccount.html";
	private static final String CREATE_ACCOUNT_EMAIL_TXT_TEMPLATE = "createAccount.txt";

	private static final String NO_DEFAULT_LOCALE_MSG = "Customer and corresponding store doesn't have a default locale set!";

	private StoreService storeService;

	@Override
	public EmailProperties getPasswordConfirmationEmailProperties(final Customer customer) {
		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		emailProperties.getTemplateResources().put("customerUid", customer.getUidPk());
		emailProperties.getTemplateResources().put(LOCALE, customer.getPreferredLocale());
		emailProperties.setDefaultSubject("Password Change Confirmation");
		emailProperties.setLocaleDependentSubjectKey("account.passwdConf.emailSubject");
		emailProperties.setEmailLocale(customer.getPreferredLocale());
		emailProperties.setTextTemplate(CONFIRM_PWD_EMAIL_TXT_TEMPLATE);
		emailProperties.setRecipientAddress(customer.getEmail());
		emailProperties.setStoreCode(customer.getStoreCode());

		return emailProperties;
	}

	private EmailProperties getEmailPropertiesBeanInstance() {
		return getBean(ContextIdNames.EMAIL_PROPERTIES);
	}

	@Override
	public EmailProperties getNewlyRegisteredCustomerEmailProperties(final Customer customer, final String newPassword) {
		Objects.requireNonNull(customer, "Customer parameter specified is null!");
        final Locale locale = determineLocaleForCustomer(customer);
		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		emailProperties.getTemplateResources().put("customerUid", customer.getUidPk());
		emailProperties.getTemplateResources().put("newPassword", newPassword);
		emailProperties.getTemplateResources().put(LOCALE, locale);
		emailProperties.setDefaultSubject("Customer Registration Confirmation");
		emailProperties.setLocaleDependentSubjectKey("account.registerCustomer.emailSubject");
		emailProperties.setEmailLocale(locale);
		emailProperties.setTextTemplate(REGISTER_CUSTOMER_EMAIL_TXT_TEMPLATE);
		emailProperties.setRecipientAddress(customer.getEmail());
		emailProperties.setStoreCode(customer.getStoreCode());

		return emailProperties;
	}

	@Override
	public EmailProperties getForgottenPasswordEmailProperties(final Customer customer, final String newPassword) {
		final Locale locale = determineLocaleForCustomer(customer);
		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		emailProperties.getTemplateResources().put("customer", customer);
		emailProperties.getTemplateResources().put("newPassword", newPassword);
		emailProperties.getTemplateResources().put(LOCALE, locale);
		emailProperties.setDefaultSubject("Your Password Reminder");
		emailProperties.setLocaleDependentSubjectKey("account.passwdForgotton.emailSubject");
		emailProperties.setEmailLocale(locale);
		emailProperties.setTextTemplate(FORGOTTEN_PWD_EMAIL_TXT_TEMPLATE);
		emailProperties.setRecipientAddress(customer.getEmail());
		emailProperties.setStoreCode(customer.getStoreCode());

		return emailProperties;
	}

	@Override
	public EmailProperties getNewAccountEmailProperties(final Customer customer) {
		final Locale locale = determineLocaleForCustomer(customer);
		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		emailProperties.getTemplateResources().put("customer", customer);
		emailProperties.getTemplateResources().put(LOCALE, locale);
		emailProperties.setDefaultSubject("Create Account Confirmation");
		emailProperties.setLocaleDependentSubjectKey("account.create.emailSubject");
		emailProperties.setEmailLocale(locale);
		emailProperties.setHtmlTemplate(CREATE_ACCOUNT_EMAIL_HTML_TEMPLATE);
		emailProperties.setTextTemplate(CREATE_ACCOUNT_EMAIL_TXT_TEMPLATE);
		emailProperties.setRecipientAddress(customer.getEmail());
		emailProperties.setStoreCode(customer.getStoreCode());

		return emailProperties;
	}

	/**
	 * Determine the locale to use when sending an email to the customer.
	 *
	 * @param customer the customer
	 * @return the locale to use
	 */
	protected Locale determineLocaleForCustomer(final Customer customer) {
		Optional<Locale> optionalCustLocale = ofNullable(customer.getPreferredLocale());

		// If customer default locale is available then get it's Optional. Or else, if a store with given storeCode is present get corresponding
		// defaultLocale as Optional, otherwise get an empty Optional.
		final Optional<Locale> optionalLocale = optionalCustLocale.isPresent() ? optionalCustLocale
				: ofNullable(storeService.findStoreWithCode(customer.getStoreCode())).map(Store::getDefaultLocale);


		return optionalLocale.orElseThrow(() -> new IllegalArgumentException(NO_DEFAULT_LOCALE_MSG));
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations;

import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;

/**
 * Messages class for the Payment plugin's PaymentConfigurations section.
 */
@SuppressWarnings ({ "PMD.TooManyFields", "PMD.VariableNamingConventions" })
public final class AdminPaymentConfigurationMessages {

	private static final String BUNDLE_NAME =
			"com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationPluginResources"; //$NON-NLS-1$

	private AdminPaymentConfigurationMessages() {
	}

	// ----------------------------------------------------
	// Default package
	// ----------------------------------------------------
	public String PaymentConfigsAdminItemCompositeFactory_Admin;

	// Global
	public String PaymentConfigurationName;

	public String PaymentConfigurationImpl;

	// Payment configurations operations
	public String CreatePaymentConfiguration;

	public String EditPaymentConfiguration;

	public String ActivatePaymentConfiguration;

	public String DisablePaymentConfiguration;

	// Dialogs
	public String CreatePaymentConfigurationDialog;

	public String EditPaymentConfigurationDialog;

	public String PaymentDialogInitialMessage;

	public String PaymentConfigurationNameAlreadyExist;

	public String PaymentConfigurationPropertiesModifiedTitle;

	public String PaymentConfigurationPropertiesModifiedText;

	public String PaymentConfigurationNameLabel;

	public String PaymentConfigurationProviderLabel;

	public String PaymentConfigurationMethodLabel;

	public String PaymentConfigurationStoresLabel;

	public String PaymentConfigurationStatusLabel;

	public String PaymentConfigurationDisplayNameLabel;

	public String PaymentConfigurationLocalizedDisplayNameLabel;

	public String PaymentTooltipProviderCannotBeEdited;

	public String PaymentTooltipMethodCannotBeEdited;

	public String PaymentConfiguration_ActivateDialogTitle;

	public String PaymentConfiguration_ActivateConfirmation;

	public String PaymentConfiguration_DisableDialogTitle;

	public String PaymentConfiguration_DisableConfirmation;

	public String PaymentConfiguration_DisableDeniedDialogTitle;

	public String PaymentConfiguration_DisableDeniedConfirmation;

	public String PaymentConfigurationAddLocalizationStringLink;

	public String ComboFirstItem;

	// Payment properties list key
	public String PaymentPropertyListKey;

	// Payment properties list value
	public String PaymentPropertyListValue;

	public String PaymentPropertyInvalid;

	// Payment properties
	public String PaymentProperty;

	// Errors
	public String PaymentConfigurationNoLongerExists;
	
	public String PaymentConfigurationInUseTitle;
	
	public String PaymentConfigurationInUseMessage;

	/**
	 * Gets the NLS localize message class.
	 * @return the localized message class.
	 */
	public static AdminPaymentConfigurationMessages get() {
		return LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, AdminPaymentConfigurationMessages.class);
	}

}
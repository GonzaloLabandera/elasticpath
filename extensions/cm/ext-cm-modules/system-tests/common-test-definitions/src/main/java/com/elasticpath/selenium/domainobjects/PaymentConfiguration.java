package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentConfiguration class.
 */
public class PaymentConfiguration {
	private String paymentProvider;
	private String paymentMethod;
	private String configurationName;
	private String displayName;
	private final Map<String, String> names = new HashMap<>();

	public String getPaymentProvider() {
		return paymentProvider;
	}

	public void setPaymentProvider(final String paymentProvider) {
		this.paymentProvider = paymentProvider;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(final String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.paymentprovider;


import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Payment provider configuration steps.
 */
public class PaymentProviderSteps {

	@Autowired
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	/**
	 * Setup the tests payment provider configurations.
	 *
	 * @param dataTable payment provider configuration info.
	 */
	@Given("^the existing payment provider configurations of$")
	public void setUpPaymentProviderConfigurations(final DataTable dataTable) {
		for (Map<String, String> properties : dataTable.asMaps(String.class, String.class)) {
			final PaymentProviderConfigDTO paymentProviderConfiguration = new PaymentProviderConfigDTO();

			paymentProviderConfiguration.setGuid(properties.get("paymentProviderConfigGuid"));
			paymentProviderConfiguration.setPaymentProviderPluginBeanName(properties.get("paymentProviderPluginBeanName"));
			paymentProviderConfiguration.setConfigurationName(properties.get("configName"));
			paymentProviderConfiguration.setStatus(PaymentProviderConfigurationStatus.valueOf(properties.get("status").trim().toUpperCase()));
			paymentProviderConfiguration.setLocalizedNames(convertToMapOfLocalizedNames(properties.get("localizedNames")));
			paymentProviderConfiguration.setDefaultDisplayName(properties.get("defaultName"));

			final Map<String, String> configDataMap = Splitter.on(",")
					.omitEmptyStrings()
					.trimResults()
					.withKeyValueSeparator(";")
					.split(properties.get("configData"));

			paymentProviderConfiguration.setPaymentConfigurationData(configDataMap);
			paymentProviderConfigManagementService.saveOrUpdate(paymentProviderConfiguration);
		}
	}

	private Map<String, String> convertToMapOfLocalizedNames(final String source) {
		if (Objects.isNull(source)) {
			return Collections.emptyMap();
		}

		return Stream.of(source.split(";"))
				.map(localizedName -> localizedName.split(":"))
				.collect(Collectors.toMap(localizedName -> localizedName[0], localizedName -> localizedName[1]));
	}

}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.assembler.paymentprovider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.PropertyDTO;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * Assembler to go between PaymentProviderConfigDTO entity and PaymentProviderDTOs.
 */
public class PaymentProviderDTOAssembler extends AbstractDtoAssembler<PaymentProviderDTO, PaymentProviderConfigDomainProxy> {

	@Override
	public PaymentProviderConfigDomainProxy getDomainInstance() {
		return new PaymentProviderConfigDomainProxy();
	}

	@Override
	public PaymentProviderDTO getDtoInstance() {
		return new PaymentProviderDTO();
	}

	@Override
	public void assembleDto(final PaymentProviderConfigDomainProxy source, final PaymentProviderDTO target) {
		target.setName(source.getConfigurationName());
		target.setPaymentProviderPluginBeanName(source.getPaymentProviderPluginBeanName());
		target.setGuid(source.getGuid());
		target.setStatus(source.getStatus().name());
		target.setLocalizedNames(convertToDisplayValues(source.getLocalizedNames()));
		target.setDefaultDisplayName(source.getDefaultDisplayName());

		for (String configKey : source.getPaymentConfigurationData().keySet()) {
			target.getProperties().add(new PropertyDTO(configKey, source.getPaymentConfigurationData().get(configKey)));
		}
	}

	/**
	 * Populates the <code>target</code> domain entity from the <code>source</code> DTO.
	 *
	 * @param source The DTO to get data from
	 * @param target The domain entity to populate
	 */
	@Override
	public void assembleDomain(final PaymentProviderDTO source, final PaymentProviderConfigDomainProxy target) {
		target.setConfigurationName(source.getName());
		target.setPaymentProviderPluginBeanName(source.getPaymentProviderPluginBeanName());
		target.setGuid(source.getGuid());
		target.setStatus(PaymentProviderConfigurationStatus.valueOf(source.getStatus().trim().toUpperCase()));
		target.setLocalizedNames(convertToMap(source.getLocalizedNames()));
		target.setDefaultDisplayName(source.getDefaultDisplayName());

		Map<String, String> configDataMap = new HashMap<>(source.getProperties().size());
		for (PropertyDTO prop : source.getProperties()) {
			configDataMap.put(prop.getPropertyKey(), prop.getValue());
		}
		target.setPaymentConfigurationData(configDataMap);
	}

	private List<DisplayValue> convertToDisplayValues(final Map<String, String> paymentLocalizedProperties) {
		return paymentLocalizedProperties.entrySet().stream()
				.map(entry -> new DisplayValue(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private Map<String, String> convertToMap(final List<DisplayValue> displayValues) {
		return displayValues.stream().collect(Collectors.toMap(DisplayValue::getLanguage, DisplayValue::getValue));
	}

}

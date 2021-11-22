/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.views;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.text.WordUtils;

import com.elasticpath.cmclient.core.ui.framework.impl.EpDisabledColorTextDecorator;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * A model object of a payment provider list.
 */
public class PaymentConfigurationsListModel implements EpDisabledColorTextDecorator {

	private final Collection<String> storeNames;
	private final String paymentVendorId;
	private final String paymentMethodId;
	private final PaymentProviderConfigDTO configDto;
	private PaymentProviderConfigurationStatus status;

	/**
	 * Creating the model for the list view.
	 *
	 * @param configDTO  the payment provider config dto
	 * @param storeNames the list of store names associated with the configuration
	 * @param paymentVendorId the vendor id for payment.
	 * @param paymentMethodId the method id for payment.
	 */
	public PaymentConfigurationsListModel(final PaymentProviderConfigDTO configDTO, final Collection<String> storeNames,
										  final String paymentVendorId,
										  final String paymentMethodId) {
		this.status = configDTO.getStatus();
		this.storeNames = storeNames;
		this.configDto = configDTO;
		this.paymentVendorId = paymentVendorId;
		this.paymentMethodId = paymentMethodId;
	}

	/**
	 * Gets guid.
	 *
	 * @return the guid.
	 */
	public String getGuid() {
		return configDto.getGuid();
	}

	/**
	 * Gets payment provider id.
	 *
	 * @return the payment provider id.
	 */
	public String getPaymentProviderId() {
		return configDto.getPaymentProviderPluginBeanName();
	}

	public String getPaymentVendorId() {
		return paymentVendorId;
	}

	public String getPaymentMethodId() {
		return paymentMethodId;
	}

	/**
	 * Gets configuration name.
	 *
	 * @return the configuration name.
	 */
	public String getConfigurationName() {
		return configDto.getConfigurationName();
	}

	/**
	 * Gets payment configuration data.
	 *
	 * @return the payment configuration data.
	 */
	public Map<String, String> getPaymentConfigurationData() {
		return configDto.getPaymentConfigurationData();
	}

	/**
	 * Gets the status string formatted for displaying ex. "DRAFT" is changed to "Draft".
	 *
	 * @return the formatted status.
	 */
	public String getStatusString() {
		return WordUtils.capitalize(status.name().toLowerCase());
	}

	/**
	 * Gets the status.
	 *
	 * @return the status.
	 */
	public PaymentProviderConfigurationStatus getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the status.
	 */
	public void setStatus(final PaymentProviderConfigurationStatus status) {
		this.status = status;
	}

	/**
	 * Gets the updated payment provider config dto.
	 *
	 * @return the updated payment provider config dto.
	 */
	public PaymentProviderConfigDTO getConfigDto() {
		configDto.setStatus(status);
		return configDto;
	}

	public String getStoreNameString() {
		return String.join(", ", storeNames);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PaymentProviderConfigDTO) {
			PaymentProviderConfigDTO other = (PaymentProviderConfigDTO) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

	@Override
	public boolean isDisabled() {
		return status.equals(PaymentProviderConfigurationStatus.DISABLED);
	}
}

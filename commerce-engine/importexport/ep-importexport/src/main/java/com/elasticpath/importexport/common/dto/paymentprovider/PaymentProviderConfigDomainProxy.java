/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.paymentprovider;

import java.util.Map;
import java.util.Objects;

import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * Used during the export/import of PaymentProviderConfiguration class in payment-provider-api.
 * NOTE: this be because we aren't referencing PaymentProviderConfiguration directly in core
 */
public class PaymentProviderConfigDomainProxy extends PaymentProviderConfigDTO implements Persistable {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private PaymentProviderConfigDTO origin;

	/**
	 * Constructor.
	 */
	public PaymentProviderConfigDomainProxy() {
		// NOP
	}

	/**
	 * Constructor.
	 *
	 * @param origin PaymentProviderConfigDTO.
	 */
	public PaymentProviderConfigDomainProxy(final PaymentProviderConfigDTO origin) {
		this.origin = origin;
	}

	@Override
	public String getGuid() {
		if (Objects.isNull(origin)) {
			return super.getGuid();
		}

		return origin.getGuid();
	}

	@Override
	public void setGuid(final String guid) {
		if (Objects.isNull(origin)) {
			super.setGuid(guid);

			return;
		}

		origin.setGuid(guid);
	}

	@Override
	public String getPaymentProviderPluginBeanName() {
		if (Objects.isNull(origin)) {
			return super.getPaymentProviderPluginBeanName();
		}

		return origin.getPaymentProviderPluginBeanName();
	}

	@Override
	public void setPaymentProviderPluginBeanName(final String paymentProviderPluginBeanName) {
		if (Objects.isNull(origin)) {
			super.setPaymentProviderPluginBeanName(paymentProviderPluginBeanName);

			return;
		}

		origin.setPaymentProviderPluginBeanName(paymentProviderPluginBeanName);
	}

	@Override
	public String getConfigurationName() {
		if (Objects.isNull(origin)) {
			return super.getConfigurationName();
		}

		return origin.getConfigurationName();
	}

	@Override
	public void setConfigurationName(final String configurationName) {
		if (Objects.isNull(origin)) {
			super.setConfigurationName(configurationName);

			return;
		}

		origin.setConfigurationName(configurationName);
	}

	@Override
	public String getDefaultDisplayName() {
		if (Objects.isNull(origin)) {
			return super.getDefaultDisplayName();
		}

		return origin.getDefaultDisplayName();
	}

	@Override
	public void setDefaultDisplayName(final String defaultDisplayName) {
		if (Objects.isNull(origin)) {
			super.setDefaultDisplayName(defaultDisplayName);

			return;
		}

		origin.setDefaultDisplayName(defaultDisplayName);
	}

	@Override
	public Map<String, String> getPaymentConfigurationData() {
		if (Objects.isNull(origin)) {
			return super.getPaymentConfigurationData();
		}

		return origin.getPaymentConfigurationData();
	}

	@Override
	public void setPaymentConfigurationData(final Map<String, String> paymentConfigurationData) {
		if (Objects.isNull(origin)) {
			super.setPaymentConfigurationData(paymentConfigurationData);

			return;
		}

		origin.setPaymentConfigurationData(paymentConfigurationData);
	}

	@Override
	public PaymentProviderConfigurationStatus getStatus() {
		if (Objects.isNull(origin)) {
			return super.getStatus();
		}

		return origin.getStatus();
	}

	@Override
	public void setStatus(final PaymentProviderConfigurationStatus status) {
		if (Objects.isNull(origin)) {
			super.setStatus(status);

			return;
		}

		origin.setStatus(status);
	}

	@Override
	public Map<String, String> getLocalizedNames() {
		if (Objects.isNull(origin)) {
			return super.getLocalizedNames();
		}

		return super.getLocalizedNames();
	}

	@Override
	public void setLocalizedNames(final Map<String, String> paymentLocalizedProperties) {
		if (Objects.isNull(origin)) {
			super.setLocalizedNames(paymentLocalizedProperties);

			return;
		}

		origin.setLocalizedNames(paymentLocalizedProperties);
	}

	@Override
	public long getUidPk() {
		return 0;
	}

	@Override
	public void setUidPk(final long uidPk) {
		//empty on purpose
	}

	@Override
	public boolean isPersisted() {
		return false;
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
}

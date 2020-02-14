/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.creation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;

/**
 * Request object to encapsulate the payment instrument creation request.
 */
public class PaymentInstrumentCreationRequest {

	private Map<String, String> formData = Collections.emptyMap();
	private PICRequestContextDTO pICRequestContextDTO;
	private Map<String, String> pluginConfigData;

	/**
	 * Gets form data.
	 *
	 * @return the form data
	 */
	public Map<String, String> getFormData() {
		return formData;
	}

	/**
	 * Sets form data.
	 *
	 * @param formData the form data
	 */
	public void setFormData(final Map<String, String> formData) {
		this.formData = Optional.ofNullable(formData)
				.map(Collections::unmodifiableMap)
				.orElse(Collections.emptyMap());
	}

	/**
	 * Gets additional context information.
	 *
	 * @return the {@link PICRequestContextDTO}
	 */
	public PICRequestContextDTO getPICRequestContextDTO() {
		return pICRequestContextDTO;
	}

	/**
	 * Sets additional context information.
	 *
	 * @param pICRequestContextDTO the {@link PICRequestContextDTO}
	 */
	public void setPICRequestContextDTO(final PICRequestContextDTO pICRequestContextDTO) {
		this.pICRequestContextDTO = pICRequestContextDTO;
	}

	/**
	 * Gets plugin config data.
	 *
	 * @return the plugin config data
	 */
	public Map<String, String> getPluginConfigData() {
		return pluginConfigData;
	}

	/**
	 * Sets plugin config data.
	 *
	 * @param pluginConfigData the plugin config data
	 */
	public void setPluginConfigData(final Map<String, String> pluginConfigData) {
		this.pluginConfigData = pluginConfigData;
	}

}

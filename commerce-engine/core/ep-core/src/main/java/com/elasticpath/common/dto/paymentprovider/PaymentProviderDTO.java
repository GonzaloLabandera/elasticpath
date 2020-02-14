/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.paymentprovider;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.Dto;
import com.elasticpath.common.dto.PropertyDTO;

/**
 * JAXB DTO for Payment Provider (and their properties).
 */
@XmlRootElement(name = PaymentProviderDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { })
public class PaymentProviderDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/** Root XML element name. */
	public static final String ROOT_ELEMENT = "payment_provider_configuration";

	@XmlElement(required = true)
	private String name;

	@XmlAttribute(required = true)
	private String guid;

	@XmlAttribute(required = true)
	private String status;

	@XmlElement(name = "payment_provider_id", required = true)
	private String paymentProviderPluginBeanName;

	@XmlElementWrapper(name = "properties")
	@XmlElement(name = "property")
	private List<PropertyDTO> properties = new ArrayList<>();

	@XmlElementWrapper(name = "localized_names")
	@XmlElement(name = "value")
	private List<DisplayValue> localizedNames = new ArrayList<>();

	@XmlElement(name = "default_display_name")
	private String defaultDisplayName;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getPaymentProviderPluginBeanName() {
		return paymentProviderPluginBeanName;
	}

	public void setPaymentProviderPluginBeanName(final String paymentProviderPluginBeanName) {
		this.paymentProviderPluginBeanName = paymentProviderPluginBeanName;
	}

	public List<PropertyDTO> getProperties() {
		return properties;
	}

	public void setProperties(final List<PropertyDTO> properties) {
		this.properties = properties;
	}

	public List<DisplayValue> getLocalizedNames() {
		return localizedNames;
	}

	public void setLocalizedNames(final List<DisplayValue> localizedNames) {
		this.localizedNames = localizedNames;
	}

	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}
}

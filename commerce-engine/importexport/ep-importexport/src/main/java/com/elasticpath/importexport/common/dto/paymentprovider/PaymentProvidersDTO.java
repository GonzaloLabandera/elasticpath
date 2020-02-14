/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.paymentprovider;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;

/**
 * Wrapper JAXB entity for schema generation to collect a group of PaymentProviderDTOs.
 */
@XmlRootElement(name = "payment_providers")
@XmlType(name = "paymentProvidersDTO", propOrder = { })
@XmlAccessorType(XmlAccessType.NONE)
public class PaymentProvidersDTO {

	@XmlElement(name = PaymentProviderDTO.ROOT_ELEMENT)
	private final List<PaymentProviderDTO> paymentProviders = new ArrayList<>();

	public List<PaymentProviderDTO> getPaymentProviders() {
		return paymentProviders;
	}
}


/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.customer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;

/**
 * Wrapper JAXB entity for schema generation to collect a group of StoreCustomerAttributeDTOs.
 */
@XmlRootElement(name = "store_customer_attributes")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "storeCustomerAttributesDTO", propOrder = {})
public class StoreCustomerAttributesDTO {

	@XmlElement(name = "store_customer_attribute")
	private final List<StoreCustomerAttributeDTO> storeCustomerAttributes = new ArrayList<>();

	public List<StoreCustomerAttributeDTO> getStoreCustomerAttributes() {
		return storeCustomerAttributes;
	}
}


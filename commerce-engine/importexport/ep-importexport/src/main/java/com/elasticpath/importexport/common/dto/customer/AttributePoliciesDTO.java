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

import com.elasticpath.common.dto.customer.AttributePolicyDTO;

/**
 * Wrapper JAXB entity for schema generation to collect a group of AttributePolicyDTOs.
 */
@XmlRootElement(name = "attribute_policies")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "attributePoliciesDTO", propOrder = {})
public class AttributePoliciesDTO {

	@XmlElement(name = "attribute_policy")
	private final List<AttributePolicyDTO> attributePolicies = new ArrayList<>();

	public List<AttributePolicyDTO> getAttributePolicies() {
		return attributePolicies;
	}
}


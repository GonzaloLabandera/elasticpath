/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.importexport.common.dto.customer;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.customer.UserAccountAssociationDTO;

/**
 * This element contains zero or more cart_type elements. This class exists mainly for XSD generation.
 */
@XmlRootElement(name = "useraccountassociations")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "userAccountAssociationsDTO", propOrder = {})
public class UserAccountAssociationsDTO {

	@XmlElement(name = "useraccountassociation")
	private final List<UserAccountAssociationDTO> userAccountAssociationDTOS = new ArrayList<>();

	public List<UserAccountAssociationDTO> getUserAccountAssociationDTOS() {
		return userAccountAssociationDTOS;
	}

}

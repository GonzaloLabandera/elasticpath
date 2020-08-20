/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.common.dto.customer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.Dto;

/**
 * The User Account Association DTO.
 */
@XmlRootElement(name = UserAccountAssociationDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "userAccountAssociationDTO", propOrder = {})
public class UserAccountAssociationDTO implements Dto {

	/**
	 * Root element.
	 */
	public static final String ROOT_ELEMENT = "useraccountassociation";
	private static final long serialVersionUID = 6613931977776500186L;

	@XmlElement(name = "guid", required = true)
	private String guid;

	@XmlElement(name = "account_guid", required = true)
	private String accountGuid;

	@XmlElement(name = "user_guid", required = true)
	private String userGuid;

	@XmlElement(name = "role", required = true)
	private String role;

	/**
	 * Get the guid.
	 *
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid
	 */
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Get the account GUID.
	 *
	 * @return the GUID
	 */
	public String getAccountGuid() {
		return accountGuid;
	}

	/**
	 * Set the account GUID.
	 *
	 * @param accountGuid the account GUID
	 */
	public void setAccountGuid(final String accountGuid) {
		this.accountGuid = accountGuid;
	}


	/**
	 * Get the user guid.
	 *
	 * @return the user guid
	 */
	public String getUserGuid() {
		return userGuid;
	}

	/**
	 * Set the user GUID.
	 *
	 * @param userGuid the account GUID
	 */
	public void setUserGuid(final String userGuid) {
		this.userGuid = userGuid;
	}

	/**
	 * Get the role.
	 *
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Set the role.
	 *
	 * @param role the role
	 */
	public void setRole(final String role) {
		this.role = role;
	}
}
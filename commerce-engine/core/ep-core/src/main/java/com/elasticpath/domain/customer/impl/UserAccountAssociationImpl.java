/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.domain.customer.impl;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.customer.AccountRole;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * The User Account Association Impl.
 */
@Entity
@Table(name = UserAccountAssociationImpl.TABLE_NAME)
public class UserAccountAssociationImpl extends AbstractEntityImpl implements UserAccountAssociation {

	private static final long serialVersionUID = -2983069094704860593L;

	/**
	 * Table name.
	 */
	public static final String TABLE_NAME = "TUSERACCOUNTASSOCIATION";

	private long uidpk;

	private String guid;

	private String userGuid;

	private String accountGuid;

	private AccountRole role;

	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(
			name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME,
			allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return uidpk;
	}

	public void setUidPk(final long uidPk) {
		this.uidpk = uidPk;
	}

	@Column(name = "GUID")
	public String getGuid() {
		return this.guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Column(name = "USER_CUSTOMER_GUID")
	public String getUserGuid() {
		return this.userGuid;
	}

	public void setUserGuid(final String userGuid) {
		this.userGuid = userGuid;
	}

	@Column(name = "ACCOUNT_CUSTOMER_GUID")
	public String getAccountGuid() {
		return this.accountGuid;
	}

	public void setAccountGuid(final String accountGuid) {
		this.accountGuid = accountGuid;
	}

	@Persistent(optional = false)
	@Column(name = "ROLE")
	@Factory("valueOf")
	@Externalizer("getName")
	public AccountRole getAccountRole() {
		return this.role;
	}

	public void setAccountRole(final AccountRole accountRole) {
		this.role = accountRole;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		UserAccountAssociationImpl that = (UserAccountAssociationImpl) object;

		return Objects.equals(userGuid, that.userGuid)
				&& Objects.equals(accountGuid, that.accountGuid)
				&& Objects.equals(role, that.role);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "uidpk", "guid");
	}
}

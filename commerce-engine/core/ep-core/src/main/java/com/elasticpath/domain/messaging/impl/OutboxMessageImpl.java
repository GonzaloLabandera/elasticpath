/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.messaging.impl;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.messaging.OutboxMessage;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * The default implementation of <code>OutboxMessage</code>.
 */
@Entity
@Table(name = OutboxMessageImpl.TABLE_NAME)
@DataCache(enabled = false)
public class OutboxMessageImpl extends AbstractPersistableImpl implements OutboxMessage {

	private static final long serialVersionUID = -5036971819037998941L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TOUTBOXMESSAGE";

	private long uidPk;

	private Date createdDate;

	private String camelUri;

	private String messageBody;

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(
			name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE", nullable = false)
	public Date getCreationDate() {
		return createdDate;
	}

	@Override
	public void setCreationDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	@Basic
	@Column(name = "CAMEL_URI", nullable = false)
	public String getCamelUri() {
		return camelUri;
	}

	@Override
	public void setCamelUri(final String camelUri) {
		this.camelUri = camelUri;
	}

	@Override
	@Basic
	@Column(name = "MESSAGE_BODY", nullable = false)
	public String getMessageBody() {
		return messageBody;
	}

	@Override
	public void setMessageBody(final String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		OutboxMessageImpl that = (OutboxMessageImpl) other;
		return uidPk == that.uidPk;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uidPk);
	}
}

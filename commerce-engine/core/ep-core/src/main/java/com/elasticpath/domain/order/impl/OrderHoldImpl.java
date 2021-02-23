/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.domain.order.impl;

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
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Implements {@link OrderHold}.
 */
@Entity
@Table(name = OrderHoldImpl.TABLE_NAME)
@DataCache(enabled = false)
public class OrderHoldImpl extends AbstractEntityImpl implements OrderHold {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TORDERHOLD";

	private static final int HOLD_DESC_LENGTH = 1024;
	private static final int REVIEWER_NOTES_LENGTH = 2000;

	private long uidPk;
	private String permission;
	private String holdDescription;
	private OrderHoldStatus orderHoldStatus;
	private Date creationDate;
	private Date resolvedDate;
	private String reviewerNotes;
	private String guid;
	private String resolvedBy;
	private long orderUid;

	@Override
	@Basic
	@Column(name = "PERMISSION", nullable = false)
	public String getPermission() {
		return permission;
	}

	@Override
	public void setPermission(final String permission) {
		this.permission = permission;
	}

	@Override
	@Basic
	@Column(name = "HOLD_DESCRIPTION", length = HOLD_DESC_LENGTH, nullable = false)
	public String getHoldDescription() {
		return holdDescription;
	}

	@Override
	public void setHoldDescription(final String holdDescription) {
		this.holdDescription = holdDescription;
	}

	/**
	 * Get the status of the order hold.
	 *
	 * @return the order hold status
	 */
	@Override
	@Persistent(optional = false)
	@Column(name = "STATUS", nullable = false)
	@Externalizer("getName")
	@Factory("valueOf")
	public OrderHoldStatus getStatus() {
		return orderHoldStatus;
	}

	@Override
	public void setStatus(final OrderHoldStatus orderHoldStatus) {
		this.orderHoldStatus = orderHoldStatus;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "RESOLVED_DATE")
	public Date getResolvedDate() {
		return this.resolvedDate;
	}

	@Override
	public void setResolvedDate(final Date resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	@Override
	@Basic
	@Column(name = "REVIEWER_NOTES", length = REVIEWER_NOTES_LENGTH)
	public String getReviewerNotes() {
		return this.reviewerNotes;
	}

	@Override
	public void setReviewerNotes(final String reviewerNotes) {
		this.reviewerNotes = reviewerNotes;
	}

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME, allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return this.uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	/**
	 * Return the guid.
	 *
	 * @return the guid.
	 */
	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid to set.
	 */
	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	@Basic
	@Column(name = "RESOLVED_BY")
	public String getResolvedBy() {
		return resolvedBy;
	}

	public void setResolvedBy(final String resolvedBy) {
		this.resolvedBy = resolvedBy;
	}

	@Override
	@Basic
	@Column(name = "ORDER_UID")
	public long getOrderUid() {
		return orderUid;
	}

	@Override
	public void setOrderUid(final long orderUid) {
		this.orderUid = orderUid;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE", nullable = false)
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public boolean equals(final Object other) {

		if (this == other) {
			return true;
		}

		if (!(other instanceof OrderHoldImpl)) {
			return false;
		}

		final OrderHoldImpl orderHold = (OrderHoldImpl) other;
		return Objects.equals(uidPk, orderHold.getUidPk());
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, guid, holdDescription, permission, orderHoldStatus, reviewerNotes, resolvedDate, resolvedBy);
	}

}

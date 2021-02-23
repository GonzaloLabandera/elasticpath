/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.domain.order;

import java.util.Date;

import com.elasticpath.domain.DatabaseCreationDate;
import com.elasticpath.persistence.api.Entity;

/**
 * Defines order hold relative info.
 */
public interface OrderHold extends Entity, DatabaseCreationDate {

	/**
	 * Gets the permission to hold order.
	 *
	 * @return the permission string.
	 */
	String getPermission();

	/**
	 * Sets the permission to hold order.
	 *
	 * @param permission the permission string.
	 */
	void setPermission(String permission);

	/**
	 * Gets the order hold description.
	 *
	 * @return the description of the order hold.
	 */
	String getHoldDescription();

	/**
	 * Sets the order hold description.
	 *
	 * @param holdDescription the hold description.
	 */
	void setHoldDescription(String holdDescription);

	/**
	 * Gets status of the order hold.
	 *
	 * @return the status of the order hold.
	 */
	OrderHoldStatus getStatus();

	/**
	 * Sets the status.
	 *
	 * @param orderHoldStatus the status.
	 */
	void setStatus(OrderHoldStatus orderHoldStatus);

	/**
	 * Gets resolved date.
	 *
	 * @return the resolved date.
	 */
	Date getResolvedDate();

	/**
	 * Sets the resolved date.
	 *
	 * @param resolvedDate the resolved date.
	 */
	void setResolvedDate(Date resolvedDate);

	/**
	 * Gets the reviewer notes.
	 *
	 * @return the reviewer notes.
	 */
	String getReviewerNotes();

	/**
	 * Sets the reviewer notes.
	 *
	 * @param reviewerNotes the reviewer notes.
	 */
	void setReviewerNotes(String reviewerNotes);

	/**
	 * Gets the logon id  of the cm user who resolved.
	 *
	 * @return the logon id of the cm user who resolved this hold.
	 */
	String getResolvedBy();

	/**
	 * Sets the cm user who resolved the hold.
	 *
	 * @param cmUserLogonId the logon id of the cm user who resolved the hold.
	 */
	void setResolvedBy(String cmUserLogonId);

	/**
	 * Get the order that owns this hold.
	 *
	 * @return the order uid that this hold is associated to
	 */
	long getOrderUid();

	/**
	 * Set the order that owns this hold.
	 *
	 * @param orderUid the order uid that this hold should be associated to
	 */
	void setOrderUid(long orderUid);

}

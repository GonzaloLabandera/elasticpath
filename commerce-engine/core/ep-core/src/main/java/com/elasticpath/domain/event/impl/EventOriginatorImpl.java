/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.event.impl;

import java.util.Objects;

import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorType;
import com.elasticpath.domain.impl.AbstractEpDomainImpl;

/**
 * Represents who pop up the events.
 *
 */
public class EventOriginatorImpl extends AbstractEpDomainImpl implements EventOriginator {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	private EventOriginatorType type;

	private CmUser cmUser;

	private Customer customer;

	/**
	 * Get the event originator type.
	 * @return the type
	 */
	@Override
	public EventOriginatorType getType() {
		return type;
	}

	/**
	 * Set the event originator type.
	 * @param type the type to set
	 */
	@Override
	public void setType(final EventOriginatorType type) {
		this.type = type;
	}

	/**
	 * Get the cmUser, null if not related to the cmUser.
	 * @return the cmUser
	 */
	@Override
	public CmUser getCmUser() {
		return cmUser;
	}

	/**
	 * Set the cmUser, null if not related to the cmUser.
	 *
	 * @param cmUser the cmUser to set
	 */
	@Override
	public void setCmUser(final CmUser cmUser) {
		this.cmUser = cmUser;
	}

	/**
	 * Get the customer, null if not related to the customer.
	 *
	 * @return the customer
	 */
	@Override
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * Set the customer, null if not related to the customer.
	 *
	 * @param customer the customer to set
	 */
	@Override
	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}

	/**
	 * Determine hashcode using the type, customer and cmuser fields.
	 *
	 * @return a hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(type, cmUser, customer);
	}

	/**
	 * Determine equality using type, customer and cmuser fields.
	 *
	 * @param obj the object to compare against
	 * @return true if the objects are equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof EventOriginatorImpl)) {
			return false;
		}
		EventOriginatorImpl other = (EventOriginatorImpl) obj;

		return Objects.equals(type, other.type)
				&& Objects.equals(cmUser, other.cmUser)
				&& Objects.equals(customer, other.customer);

	}

}

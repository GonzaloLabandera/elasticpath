/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.customer;

import com.elasticpath.persistence.api.Persistable;

/**
 * <code>CustomerClosure</code> represents a customer closure.
 */
public interface CustomerClosure extends Persistable {

	/**
	 * Gets the guid of the ancestor.
	 * @return ancestor guid of the closure.
	 */
	String getAncestorGuid();

	/**
	 * Set the guid of the ancestor.
	 * @param ancestorGuid guid of the ancestor for a closure.
	 */
	void setAncestorGuid(String ancestorGuid);

	/**
	 * Gets the guid of the descendant.
	 * @return the guid of the descendant.
	 */
	String getDescendantGuid();

	/**
	 * Set the guid of the descendant.
	 * @param descendantGuid guid of the descendant for a closure.
	 */
	void setDescendantGuid(String descendantGuid);

	/**
	 * Gets the depth of the ancestor.
	 * @return the depth of the ancestor.
	 */
	long getAncestorDepth();

	/**
	 * Set the depth of the ancestor.
	 * @param ancestorDepth the depth of the ancestor.
	 */
	void setAncestorDepth(long ancestorDepth);
}

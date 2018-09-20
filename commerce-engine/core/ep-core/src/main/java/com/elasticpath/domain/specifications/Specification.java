/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.specifications;

/**
 * Common interface for specifications on domain objects.
 * @param <O> Object which to check specification against.
 */
public interface Specification<O> {
	
	/**
	 * Returns true if the specification is satisfied by the object.
	 *
	 * @param object the object to check the specification against.
	 * @return true if the specification is satisfied by the object, false otherwise.
	 */
	boolean isSatisfiedBy(O object);

}

/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.domain.order.impl;

/**
 * Defines a class which has listenable properties whose listeners can be registered.
 */
public interface ListenableProperties {

	/**
	 * Add property change listeners.
	 */
	void registerPropertyListeners();

}
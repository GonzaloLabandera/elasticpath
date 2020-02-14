/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.skuconfiguration;

import com.elasticpath.persistence.api.Persistable;

/**
 * Represents SKU option values that support loading of lazy fields via cache-backed service calls.
 */
public interface LazyLoadableSkuOptionValue extends Persistable {

	/**
	 * Get the option key.
	 *
	 * @return the option key
	 */
	String getOptionKey();

	/**
	 * Getter for FK OPTION_VALUE_UID field.
	 *
	 * @return FK ID
	 */
	Long getSkuOptionValueUidInternal();
}

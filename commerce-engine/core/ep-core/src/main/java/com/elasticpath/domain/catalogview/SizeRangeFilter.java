/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview;

import java.math.BigDecimal;

/**
 * Filter for sizes.
 */
public interface SizeRangeFilter extends RangeFilter<SizeRangeFilter, BigDecimal> {

	/**
	 * Get the size type.
	 * @return size type
	 */
	SizeType getSizeType();

	/**
	 * Set the size type.
	 * @param sizeType size type
	 */
	void setSizeType(SizeType sizeType);
}
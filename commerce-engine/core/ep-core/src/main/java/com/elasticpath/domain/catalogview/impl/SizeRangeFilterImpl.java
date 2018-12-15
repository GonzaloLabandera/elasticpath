/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview.impl;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.commons.constants.SeoConstants;
import com.elasticpath.domain.catalogview.RangeFilterType;
import com.elasticpath.domain.catalogview.SizeRangeFilter;
import com.elasticpath.domain.catalogview.SizeRangeFilterConstants;
import com.elasticpath.domain.catalogview.SizeType;

/**
 * Implementation of a size range filter.
 */
public class SizeRangeFilterImpl extends AbstractRangeFilterImpl<SizeRangeFilter, BigDecimal> implements SizeRangeFilter {

	private static final long serialVersionUID = 5000000001L;

	private SizeType sizeType;

	@Override
	public String getDisplayName(final Locale locale) {
		String displayname = super.getDisplayName(locale);

		if (displayname != null) {
			return displayname;
		}

		if (getRangeType() == RangeFilterType.BETWEEN) {
			displayname = getLowerValue() + " - " + getUpperValue();
		} else if (getRangeType() == RangeFilterType.LESS_THAN) {
			displayname = "< " + getUpperValue();
		} else if (getRangeType() == RangeFilterType.MORE_THAN) {
			displayname = "> " + getLowerValue();
		}

		return displayname;
	}

	@Override
	public void initialize(final Map<String, Object> properties) {
		this.setLowerValue((BigDecimal) properties.get(LOWER_VALUE_PROPERTY));
		this.setUpperValue((BigDecimal) properties.get(UPPER_VALUE_PROPERTY));
		this.setSizeType((SizeType) properties.get(SizeRangeFilterConstants.TYPE));
		this.setAlias((String) properties.get(SizeRangeFilterConstants.ALIAS));
		this.setId(getSeoId());
	}

	@Override
	public String getSeoId() {
		return SeoConstants.SIZE_RANGE_FILTER_PREFIX + getSeparatorInToken() + this.getSizeType().getLabel() + getSeparatorInToken() + getAlias();
	}

	@Override
	public Map<String, Object> parseFilterString(final String filterIdStr) {
		// not needed
		return null;
	}

	@Override
	public void setSizeType(final SizeType sizeType) {
		this.sizeType = sizeType;
	}

	@Override
	public SizeType getSizeType() {
		return sizeType;
	}

	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof SizeRangeFilter)) {
			return false;
		}
		return getId().equals(((SizeRangeFilter) object).getId());
	}

	/**
	 * Returns the hash code.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

}
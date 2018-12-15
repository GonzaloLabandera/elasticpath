/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.common.dto.search;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * Range facet.
 */
@SuppressWarnings("rawtypes")
public class RangeFacet implements Comparable {

	private BigDecimal start;
	private BigDecimal end;
	private Map<String, String> displayNameMap;

	/**
	 * Default Constructor for serialization.
	 */
	public RangeFacet() {
		// default constructor used for JSON serialization
	}

	/**
	 * Constructor.
	 * @param start start
	 * @param end end
	 * @param displayNameMap display name in multiple locale
	 */
	public RangeFacet(final BigDecimal start, final BigDecimal end, final Map<String, String> displayNameMap) {
		this.start = start;
		this.end = end;
		this.displayNameMap = displayNameMap;
	}

	public BigDecimal getStart() {
		return start;
	}

	public void setStart(final BigDecimal start) {
		this.start = start;
	}

	public BigDecimal getEnd() {
		return end;
	}

	public void setEnd(final BigDecimal end) {
		this.end = end;
	}

	public Map<String, String> getDisplayNameMap() {
		return displayNameMap;
	}

	public void setDisplayNameMap(final Map<String, String> displayName) {
		this.displayNameMap = displayName;
	}

	@Override
	public int compareTo(final Object object) {
		return Comparator.comparing(RangeFacet::getStart, Comparator.nullsFirst(BigDecimal::compareTo))
				.thenComparing(RangeFacet::getEnd, Comparator.nullsLast(BigDecimal::compareTo))
				.compare(this, (RangeFacet) object);
	}

	@Override
	public boolean equals(final Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof RangeFacet) {
			RangeFacet other = (RangeFacet) object;
			return Objects.equals(this.getStart(), other.getStart())
					&& Objects.equals(this.getEnd(), other.getEnd())
					&& Objects.equals(this.getDisplayNameMap(), other.getDisplayNameMap());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end, displayNameMap);
	}
}
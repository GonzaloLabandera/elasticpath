/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.common.dto.search;

import static com.elasticpath.common.dto.search.FacetDTOConstants.DISPLAY_VALUES;
import static com.elasticpath.common.dto.search.FacetDTOConstants.END;
import static com.elasticpath.common.dto.search.FacetDTOConstants.START;
import static com.elasticpath.common.dto.search.FacetDTOConstants.VALUE;

import java.math.BigDecimal;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.elasticpath.common.dto.DisplayValue;

/**
 * Range facet DTO for importexport.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class RangeFacetDTO {

	@XmlElement(name = START)
	private BigDecimal start;

	@XmlElement(name = END)
	private BigDecimal end;

	@XmlElementWrapper(name = DISPLAY_VALUES, required = true)
	@XmlElement(name = VALUE, required = true)
	private List<DisplayValue> displayValues;

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

	public List<DisplayValue> getDisplayValues() {
		return displayValues;
	}

	public void setDisplayValues(final List<DisplayValue> displayValues) {
		this.displayValues = displayValues;
	}
}

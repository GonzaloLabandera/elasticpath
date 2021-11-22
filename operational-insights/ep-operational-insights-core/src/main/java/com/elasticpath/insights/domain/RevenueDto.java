/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Data Transfer Object for order revenue data.
 */
public class RevenueDto {
	private String currencyCode;

	private Date orderDate;

	private String storeCode;

	private BigDecimal bookedRevenue;

	private long orderCount;

	public String getStoreCode() {
		return storeCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public BigDecimal getBookedRevenue() {
		return bookedRevenue;
	}

	public long getOrderCount() {
		return orderCount;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setOrderDate(final Date orderDate) {
		this.orderDate = orderDate;
	}

	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	public void setBookedRevenue(final BigDecimal bookedRevenue) {
		this.bookedRevenue = bookedRevenue;
	}

	public void setOrderCount(final long orderCount) {
		this.orderCount = orderCount;
	}
}

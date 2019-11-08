/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.Date;

/**
 * ProductSkuItem class state object.
 */
public class ProductSkuItem {
	private String itemCode;
	private String taxCode;
	private Date enableDate;
	private Date disableDate;

	public Date getEnableDate() {
		return enableDate;
	}

	public void setEnableDate(final Date enableDate) {
		this.enableDate = enableDate;
	}

	public Date getDisableDate() {
		return disableDate;
	}

	public void setDisableDate(final Date disableDate) {
		this.disableDate = disableDate;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(final String itemCode) {
		this.itemCode = itemCode;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(final String taxCode) {
		this.taxCode = taxCode;
	}
}

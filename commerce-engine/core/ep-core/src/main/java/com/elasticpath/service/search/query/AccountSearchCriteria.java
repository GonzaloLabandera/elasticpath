/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.search.query;

import com.elasticpath.service.search.AbstractSearchCriteriaImpl;
import com.elasticpath.service.search.IndexType;

/**
 * A criteria for advanced account search.
 */
public class AccountSearchCriteria extends AbstractSearchCriteriaImpl {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private boolean searchRootAccountsOnly;

	private String sharedId;

	private String businessName;

	private String businessNumber;

	private String phoneNumber;

	private String faxNumber;

	private String zipOrPostalCode;

	private String taxExemptionId;

	private String guid;

	public String getTaxExemptionId() {
		return taxExemptionId;
	}

	public void setTaxExemptionId(final String taxExemptionId) {
		this.taxExemptionId = taxExemptionId;
	}

	public boolean isSearchRootAccountsOnly() {
		return searchRootAccountsOnly;
	}

	public void setSearchRootAccountsOnly(final boolean searchRootAccountsOnly) {
		this.searchRootAccountsOnly = searchRootAccountsOnly;
	}

	public String getSharedId() {
		return sharedId;
	}

	public void setSharedId(final String sharedId) {
		this.sharedId = sharedId;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(final String businessName) {
		this.businessName = businessName;
	}

	public String getBusinessNumber() {
		return businessNumber;
	}

	public void setBusinessNumber(final String businessNumber) {
		this.businessNumber = businessNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(final String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public String getZipOrPostalCode() {
		return zipOrPostalCode;
	}

	public void setZipOrPostalCode(final String zipOrPostalCode) {
		this.zipOrPostalCode = zipOrPostalCode;
	}

	/**
	 * Optimizes a search criteria by removing unnecessary information.
	 */
	@Override
	@SuppressWarnings("PMD.NPathComplexity")
	public void optimize() {
		if (!isStringValid(sharedId)) {
			sharedId = null;
		}
		if (!isStringValid(businessName)) {
			businessName = null;
		}
		if (!isStringValid(businessNumber)) {
			businessNumber = null;
		}
		if (!isStringValid(phoneNumber)) {
			phoneNumber = null;
		}
		if (!isStringValid(faxNumber)) {
			faxNumber = null;
		}
		if (!isStringValid(taxExemptionId)) {
			taxExemptionId = null;
		}
	}

	/**
	 * Returns the index type this criteria deals with.
	 *
	 * @return the index type this criteria deals with.
	 */
	@Override
	public IndexType getIndexType() {
		return null;
	}


	/**
	 * Clears this <code>AccountSearchCriteria</code> and resets all criteria to their default values.
	 */
	public void clear() {
		this.searchRootAccountsOnly = false;
		this.sharedId = null;
		this.businessName = null;
		this.businessNumber = null;
		this.phoneNumber = null;
		this.faxNumber = null;
		this.taxExemptionId = null;
	}
}

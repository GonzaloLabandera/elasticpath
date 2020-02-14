/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.datapolicy;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This class contains common values from {@link CustomerConsent} and {@link DataPolicy} classes.
 * Some values that are coming from data policies are not valid for the customer consent and hence are ignored and left null.
 */
public class DataPolicyDescription {
	private final DataPolicy dataPolicy;

	// These values are not final because can be null
	private ConsentAction action;
	private Date consentDate;

	/**
	 * Constructor.
	 *
	 * @param dataPolicy data policy
	 */
	public DataPolicyDescription(final DataPolicy dataPolicy) {
		this.dataPolicy = dataPolicy;
	}

	/**
	 * Constructor.
	 * @param customerConsent customer consent
	 */
	public DataPolicyDescription(final CustomerConsent customerConsent) {
		this(customerConsent.getDataPolicy());
		this.action = customerConsent.getAction();
		this.consentDate = customerConsent.getConsentDate();
	}

	public String getGuid() {
		return this.dataPolicy.getGuid();
	}

	public String getPolicyName() {
		return this.dataPolicy.getPolicyName();
	}

	public String getStateName() {
		return this.dataPolicy.getState().getName();
	}

	public List<DataPoint> getDataPoints() {
		return this.dataPolicy.getDataPoints();
	}

	/**
	 * Can be null aka N/A.
	 * @return action
	 */
	public ConsentAction getAction() {
		return this.action;
	}

	/**
	 * Can be null aka N/A.
	 * @return date
	 */
	public Date getConsentDate() {
		return this.consentDate;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		DataPolicyDescription that = (DataPolicyDescription) other;
		return dataPolicy.equals(that.dataPolicy)
				&& Objects.equals(action, that.action)
				&& Objects.equals(consentDate, that.consentDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataPolicy, action, consentDate);
	}
}

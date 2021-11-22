/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.persistence.support;

import java.util.Collection;

import com.elasticpath.persistence.support.impl.CriteriaQuery;
import com.elasticpath.service.search.query.CustomerSearchCriteria;

/**
 * <code>CustomerCriterion</code> generates the proper criteria for order domain object.
 */
public interface CustomerCriterion {

	/**
	 * The Enum ResultType.
	 */
	enum ResultType {

		/**
		 * Query returns COUNT.
		 */
		COUNT,

		/**
		 * Query returns ENTITY.
		 */
		ENTITY
	}

	/**
	 * Return the criteria used for customer search.
	 *
	 * @param customerSearchCriteria the customer search criteria.
	 * @param stores                 the list of store filters to be returned
	 * @param resultType             the result type
	 * @return the criteria used to complete customer search
	 */
	CriteriaQuery getCustomerSearchCriteria(CustomerSearchCriteria customerSearchCriteria,
											Collection<String> stores, ResultType resultType);


}

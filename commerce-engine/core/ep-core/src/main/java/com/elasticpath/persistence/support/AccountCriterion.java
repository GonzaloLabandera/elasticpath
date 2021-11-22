/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.persistence.support;

import com.elasticpath.persistence.support.impl.CriteriaQuery;
import com.elasticpath.service.search.query.AccountSearchCriteria;

/**
 * <code>AccountCriterion</code> generates the proper criteria for order domain object.
 */
public interface AccountCriterion {

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
	 * Return the criteria used for account search.
	 *
	 * @param accountSearchCriteria the account search criteria.
	 * @param resultType             the result type
	 * @return the criteria used to complete account search
	 */
	CriteriaQuery getAccountSearchCriteria(AccountSearchCriteria accountSearchCriteria,
										   ResultType resultType);


}

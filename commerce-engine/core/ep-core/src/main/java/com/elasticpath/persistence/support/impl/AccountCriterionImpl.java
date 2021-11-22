/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.persistence.support.impl;

import static com.elasticpath.service.search.query.SortOrder.ASCENDING;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilder;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilderWhereGroup;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilderWhereGroup.ConjunctionType;
import com.elasticpath.persistence.support.AccountCriterion;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * Implementation of AccountCriterion that constructs the appropriate criteria for account queries
 * using the JpqlQueryBuilder.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AccountCriterionImpl implements AccountCriterion {
	private static final String CUSTOMER = "CustomerImpl";

	@Override
	public CriteriaQuery getAccountSearchCriteria(final AccountSearchCriteria accountSearchCriteria,
												  final ResultType resultType) {
		JpqlQueryBuilder queryBuilder = createCustomerQueryBuilder(resultType);

		final JpqlQueryBuilderWhereGroup whereGroup = queryBuilder.getDefaultWhereGroup();

		addCustomerClauses(queryBuilder, accountSearchCriteria, whereGroup);

		if (!ResultType.COUNT.equals(resultType)) {
			addOrderBy(accountSearchCriteria, queryBuilder);
		}

		queryBuilder.distinct();

		return new CriteriaQuery(queryBuilder);
	}

	/**
	 * Add customer clauses.
	 *
	 * @param queryBuilder          the query builder
	 * @param accountSearchCriteria the account search criteria
	 * @param whereGroup            the where group
	 */
	@SuppressWarnings("PMD.NPathComplexity")
	protected void addCustomerClauses(final JpqlQueryBuilder queryBuilder, final AccountSearchCriteria accountSearchCriteria,
									final JpqlQueryBuilderWhereGroup whereGroup) {
		if (accountSearchCriteria != null) {
			appendEqualsClause(whereGroup, "c.customerType", CustomerType.ACCOUNT);
			appendEqualsClause(whereGroup, "c.guid", accountSearchCriteria.getGuid());

			if (StringUtils.isNotEmpty(accountSearchCriteria.getSharedId())) {
				appendEqualsClause(whereGroup, "c.sharedId", accountSearchCriteria.getSharedId());
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getBusinessName())
					|| (accountSearchCriteria.getSortingType().getOrdinal() == StandardSortBy.BUSINESS_NAME_ORDINAL)) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "nm");
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getBusinessName())) {
				addBusinessNameClause(accountSearchCriteria.getBusinessName().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getBusinessNumber())
					|| (accountSearchCriteria.getSortingType().getOrdinal() == StandardSortBy.BUSINESS_NUMBER_ORDINAL)) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "nmbr");
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getBusinessNumber())) {
				addBusinessNumberClause(accountSearchCriteria.getBusinessNumber().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getPhoneNumber())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "phn");
				addPhoneNumberClause(accountSearchCriteria.getPhoneNumber().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getFaxNumber())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "fx");
				addFaxNumberClause(accountSearchCriteria.getFaxNumber().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(accountSearchCriteria.getZipOrPostalCode())) {
				appendLikeClause(whereGroup, "c.preferredBillingAddressInternal.zipOrPostalCode", accountSearchCriteria.getZipOrPostalCode());
			}

			if (accountSearchCriteria.isSearchRootAccountsOnly()) {
				whereGroup.appendWhereEmpty("c.parentGuid");
			}
		}
	}

	/**
	 * Add the phone number clause.
	 *
	 * @param phoneNumber the phone number
	 * @param whereGroup  the where group
	 */
	protected void addPhoneNumberClause(final String phoneNumber, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("phn.localizedAttributeKey", CustomerImpl.ATT_KEY_AP_PHONE);
		final JpqlQueryBuilderWhereGroup phoneNumberWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		phoneNumberWhereGroup.appendWhereEquals("UPPER(phn.shortTextValue)", phoneNumber);
		whereGroup.appendWhereGroup(phoneNumberWhereGroup);
	}

	/**
	 * Add the business name clause.
	 *
	 * @param businessName the business name
	 * @param whereGroup   the where group
	 */
	protected void addBusinessNameClause(final String businessName, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("nm.localizedAttributeKey", CustomerImpl.ATT_KEY_AP_NAME);
		final JpqlQueryBuilderWhereGroup businessNameWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		businessNameWhereGroup.appendLikeWithWildcards("UPPER(nm.shortTextValue)", businessName);
		whereGroup.appendWhereGroup(businessNameWhereGroup);
	}

	/**
	 * Add business name clause.
	 *
	 * @param businessNumber the business name
	 * @param whereGroup     the where group
	 */
	protected void addBusinessNumberClause(final String businessNumber, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("nmbr.localizedAttributeKey", CustomerImpl.ATT_KEY_AP_BUSINESS_NUMBER);
		final JpqlQueryBuilderWhereGroup businessNumberWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		businessNumberWhereGroup.appendLikeWithWildcards("UPPER(nmbr.shortTextValue)", businessNumber);
		whereGroup.appendWhereGroup(businessNumberWhereGroup);
	}

	/**
	 * Add fax number clause.
	 *
	 * @param faxNumber  the fax number
	 * @param whereGroup the where group
	 */
	protected void addFaxNumberClause(final String faxNumber, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("fx.localizedAttributeKey", CustomerImpl.ATT_KEY_AP_FAX);
		final JpqlQueryBuilderWhereGroup faxNumberWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		faxNumberWhereGroup.appendWhereEquals("UPPER(fx.shortTextValue)", faxNumber);
		whereGroup.appendWhereGroup(faxNumberWhereGroup);
	}

	/**
	 * Add the order by clauses based on the search criteria.
	 *
	 * @param accountSearchCriteria the criteria that includes the sorting requirements
	 * @param queryBuilder          the query builder being used to build the query
	 */
	protected void addOrderBy(final AccountSearchCriteria accountSearchCriteria, final JpqlQueryBuilder queryBuilder) {
		boolean ascending = ASCENDING.equals(accountSearchCriteria.getSortingOrder());
		switch (accountSearchCriteria.getSortingType().getOrdinal()) {
			case StandardSortBy.SHARED_ID_ORDINAL:
				queryBuilder.appendOrderBy("c.sharedId", ascending);
				break;
			case StandardSortBy.BUSINESS_NAME_ORDINAL:
				queryBuilder.appendOrderBy("nm.shortTextValue", ascending);
				break;
			case StandardSortBy.BUSINESS_NUMBER_ORDINAL:
				queryBuilder.appendOrderBy("nmbr.shortTextValue", ascending);
				break;
			case StandardSortBy.ADDRESS_ORDINAL:
				queryBuilder.appendOrderBy("c.preferredBillingAddressInternal", ascending);
				break;
			default:
				break;
		}
	}

	/**
	 * Append equals clause.
	 *
	 * @param whereGroup the where group
	 * @param fieldName  the field names
	 * @param value      the value
	 */
	protected void appendEqualsClause(final JpqlQueryBuilderWhereGroup whereGroup, final String fieldName, final String value) {
		if (StringUtils.isNotEmpty(value)) {
			whereGroup.appendWhereEquals(fieldName, value);
		}
	}

	/**
	 * Append equals clause.
	 *
	 * @param whereGroup the where group
	 * @param fieldName  the field names
	 * @param value      the value
	 */
	protected void appendEqualsClause(final JpqlQueryBuilderWhereGroup whereGroup, final String fieldName, final Object value) {
		if (value != null) {
			whereGroup.appendWhereEquals(fieldName, value);
		}
	}

	/**
	 * Append like clause.
	 *
	 * @param whereGroup the where group
	 * @param fieldName  the fieldname
	 * @param value      the value
	 */
	protected void appendLikeClause(final JpqlQueryBuilderWhereGroup whereGroup, final String fieldName, final String value) {
		if (StringUtils.isNotEmpty(value)) {
			whereGroup.appendLikeWithWildcards(fieldName, value);
		}
	}

	/**
	 * Create customer query builder.
	 *
	 * @param resultType the result type
	 * @return the query builder
	 */
	protected JpqlQueryBuilder createCustomerQueryBuilder(final ResultType resultType) {

		JpqlQueryBuilder queryBuilder = new JpqlQueryBuilder(CUSTOMER, "c");
		if (resultType == ResultType.COUNT) {
			queryBuilder.count();
		}

		return queryBuilder;
	}

}

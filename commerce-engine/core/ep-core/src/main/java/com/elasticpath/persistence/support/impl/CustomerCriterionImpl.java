/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.persistence.support.impl;

import static com.elasticpath.service.search.query.SortOrder.ASCENDING;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.LongValidator;

import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilder;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilderWhereGroup;
import com.elasticpath.persistence.openjpa.support.JpqlQueryBuilderWhereGroup.ConjunctionType;
import com.elasticpath.persistence.support.CustomerCriterion;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * Implementation of CustomerCriterion that constructs the appropriate criteria for customer queries
 * using the JpqlQueryBuilder.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CustomerCriterionImpl implements CustomerCriterion {
	private static final String CUSTOMER = "CustomerImpl";

	@Override
	public CriteriaQuery getCustomerSearchCriteria(final CustomerSearchCriteria customerSearchCriteria,
												   final Collection<String> stores, final ResultType resultType) {
		JpqlQueryBuilder queryBuilder = createCustomerQueryBuilder(resultType);

		final JpqlQueryBuilderWhereGroup whereGroup = queryBuilder.getDefaultWhereGroup();

		addCustomerClauses(queryBuilder, customerSearchCriteria, whereGroup);

		addStoreCodeClauses(customerSearchCriteria, whereGroup);

		if (ResultType.ENTITY.equals(resultType)) {
			addOrderBy(customerSearchCriteria, queryBuilder);
		}

		queryBuilder.distinct();

		return new CriteriaQuery(queryBuilder);
	}

	/**
	 * Add customer clauses.
	 *
	 * @param queryBuilder           the query builder
	 * @param customerSearchCriteria the search criteria
	 * @param whereGroup             the where group
	 */
	@SuppressWarnings("PMD.NPathComplexity")
	protected void addCustomerClauses(final JpqlQueryBuilder queryBuilder, final CustomerSearchCriteria customerSearchCriteria,
									final JpqlQueryBuilderWhereGroup whereGroup) {
		if (customerSearchCriteria != null) {
			Long customerNumber = LongValidator.getInstance().validate(customerSearchCriteria.getCustomerNumber());
			appendEqualsClause(whereGroup, "c.uidPk", customerNumber);
			appendEqualsClause(whereGroup, "c.guid", customerSearchCriteria.getGuid());

			if (StringUtils.isNotEmpty(customerSearchCriteria.getSharedId())) {
				appendEqualsClause(whereGroup, "c.sharedId", customerSearchCriteria.getSharedId());
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getUsername())) {
				appendLikeClause(whereGroup, "c.customerAuthentication.username", customerSearchCriteria.getUsername());
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getEmail())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "eml");
				addEmailClause(customerSearchCriteria.getEmail().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getFirstName())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "cpf");
				addFirstNameClause(customerSearchCriteria.getFirstName().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getLastName())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "cpl");
				addLastNameClause(customerSearchCriteria.getLastName().toUpperCase(), whereGroup);
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getZipOrPostalCode())) {
				appendLikeClause(whereGroup, "c.preferredBillingAddressInternal.zipOrPostalCode", customerSearchCriteria.getZipOrPostalCode());
			}

			if (StringUtils.isNotEmpty(customerSearchCriteria.getPhoneNumber())) {
				queryBuilder.appendLeftJoin("c.profileValueMap", "cpp");
				addPhoneNumberClause(customerSearchCriteria.getPhoneNumber(), whereGroup);
			}
		}

	}

	/**
	 * Add phone number clause.
	 *
	 * @param phoneNumber  the phone number
	 * @param whereGroup the where group
	 */
	protected void addPhoneNumberClause(final String phoneNumber, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("cpp.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_PHONE);
		final JpqlQueryBuilderWhereGroup phoneWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		phoneWhereGroup.appendLikeWithWildcards("c.preferredBillingAddressInternal.phoneNumber", phoneNumber);
		phoneWhereGroup.appendLikeWithWildcards("cpp.shortTextValue", phoneNumber);
		whereGroup.appendWhereGroup(phoneWhereGroup);
	}


	/**
	 * Add first name clause.
	 *
	 * @param firstName  the first name
	 * @param whereGroup the where group
	 */
	protected void addFirstNameClause(final String firstName, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("cpf.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_FIRST_NAME);
		final JpqlQueryBuilderWhereGroup firstNameWhere = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		firstNameWhere.appendLikeWithWildcards("UPPER(c.preferredBillingAddressInternal.firstName)", firstName);
		firstNameWhere.appendLikeWithWildcards("UPPER(cpf.shortTextValue)", firstName);
		whereGroup.appendWhereGroup(firstNameWhere);
	}

	/**
	 * Add last name clause.
	 *
	 * @param lastName   the last name
	 * @param whereGroup the where group
	 */
	protected void addLastNameClause(final String lastName, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("cpl.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_LAST_NAME);
		final JpqlQueryBuilderWhereGroup lastNameWhere = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
		lastNameWhere.appendLikeWithWildcards("UPPER(c.preferredBillingAddressInternal.lastName)", lastName);
		lastNameWhere.appendLikeWithWildcards("UPPER(cpl.shortTextValue)", lastName);
		whereGroup.appendWhereGroup(lastNameWhere);
	}

	/**
	 * Add email clause.
	 *
	 * @param email      the email
	 * @param whereGroup the where group
	 */
	protected void addEmailClause(final String email, final JpqlQueryBuilderWhereGroup whereGroup) {
		whereGroup.appendWhereEquals("eml.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_EMAIL);
		whereGroup.appendLikeWithWildcards("UPPER(eml.shortTextValue)", email);
	}

	/**
	 * Add store code clauses.
	 *
	 * @param customerSearchCriteria the search criteria
	 * @param whereGroup             the where group
	 */
	protected void addStoreCodeClauses(final CustomerSearchCriteria customerSearchCriteria, final JpqlQueryBuilderWhereGroup whereGroup) {
		Collection<String> codes = customerSearchCriteria.getStoreCodes();
		if (codes != null && !codes.isEmpty()) {
			final List<String> ucStoreCodes = codes.stream()
					.map(StringUtils::upperCase)
					.collect(toList());
			whereGroup.appendWhereInCollection("UPPER(c.storeCode)", ucStoreCodes);
		}
	}

	/**
	 * Add the order by clauses based on the search criteria.
	 *
	 * @param customerSearchCriteria the criteria that includes the sorting requirements
	 * @param queryBuilder           the query builder being used to build the query
	 */
	protected void addOrderBy(final CustomerSearchCriteria customerSearchCriteria, final JpqlQueryBuilder queryBuilder) {
		boolean ascending = ASCENDING.equals(customerSearchCriteria.getSortingOrder());
		switch (customerSearchCriteria.getSortingType().getOrdinal()) {
			case StandardSortBy.CUSTOMER_ID_ORDINAL:
				queryBuilder.appendOrderBy("c.sharedId", ascending);
				break;
			case StandardSortBy.STORE_CODE_ORDINAL:
				queryBuilder.appendOrderBy("c.storeCode", ascending);
				break;
			case StandardSortBy.SHARED_ID_ORDINAL:
				queryBuilder.appendOrderBy("c.sharedId", ascending);
				break;
			case StandardSortBy.EMAIL_ORDINAL:
				queryBuilder.appendLeftJoin("c.profileValueMap", "emls");
				final JpqlQueryBuilderWhereGroup emailWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
				emailWhereGroup.appendWhereEquals("emls.localizedAttributeKey", null);
				emailWhereGroup.appendWhereEquals("emls.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_EMAIL);
				queryBuilder.getDefaultWhereGroup().appendWhereGroup(emailWhereGroup);
				queryBuilder.appendOrderBy("emls.shortTextValue", ascending);
				break;
			case StandardSortBy.USERNAME_ORDINAL:
				queryBuilder.appendOrderBy("c.sharedId", ascending);
				break;
			case StandardSortBy.FIRST_NAME_ORDINAL:
				queryBuilder.appendLeftJoin("c.profileValueMap", "cpfs");
				final JpqlQueryBuilderWhereGroup firstNameWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
				firstNameWhereGroup.appendWhereEquals("cpfs.localizedAttributeKey", null);
				firstNameWhereGroup.appendWhereEquals("cpfs.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_FIRST_NAME);
				queryBuilder.getDefaultWhereGroup().appendWhereGroup(firstNameWhereGroup);
				queryBuilder.appendOrderBy("cpfs.shortTextValue", ascending);
				break;
			case StandardSortBy.LAST_NAME_ORDINAL:
				queryBuilder.appendLeftJoin("c.profileValueMap", "cpls");
				final JpqlQueryBuilderWhereGroup lastNameWhereGroup = new JpqlQueryBuilderWhereGroup(ConjunctionType.OR);
				lastNameWhereGroup.appendWhereEquals("cpls.localizedAttributeKey", null);
				lastNameWhereGroup.appendWhereEquals("cpls.localizedAttributeKey", CustomerImpl.ATT_KEY_CP_LAST_NAME);
				queryBuilder.getDefaultWhereGroup().appendWhereGroup(lastNameWhereGroup);
				queryBuilder.appendOrderBy("cpls.shortTextValue", ascending);
				break;
			case StandardSortBy.ADDRESS_ORDINAL:
				queryBuilder.appendOrderBy("c.preferredBillingAddressInternal", ascending);
				break;
			default:
				break;
		}
	}

	/**
	 * The append equals clause.
	 *
	 * @param whereGroup the where group
	 * @param fieldName  the field name
	 * @param value      the value
	 */
	protected void appendEqualsClause(final JpqlQueryBuilderWhereGroup whereGroup, final String fieldName, final String value) {
		if (StringUtils.isNotEmpty(value)) {
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
	 * Append like clause.
	 *
	 * @param whereGroup the where group
	 * @param fieldName  the fieldname
	 * @param value      the value
	 */
	protected void appendEqualsClause(final JpqlQueryBuilderWhereGroup whereGroup, final String fieldName, final Object value) {
		if (value != null) {
			whereGroup.appendWhereEquals(fieldName, value);
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

/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.custom.customer;

import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.ql.parser.AbstractEpQLCustomConfiguration;
import com.elasticpath.ql.parser.EpQLField;
import com.elasticpath.ql.parser.EpQLFieldType;
import com.elasticpath.ql.parser.EpQLSortOrder;
import com.elasticpath.ql.parser.fieldresolver.impl.NonLocalizedFieldResolver;
import com.elasticpath.ql.parser.querybuilder.SubQueryBuilder;
import com.elasticpath.ql.parser.valueresolver.impl.JPQLEnumValueResolver;

/**
 * Holds mapping between EqQL fields and Solr field descriptors for Customer.
 */
public class CustomerConfiguration extends AbstractEpQLCustomConfiguration {

	private NonLocalizedFieldResolver nonLocalizedFieldResolver;

	private SubQueryBuilder subQueryBuilder;

	@Override
	public void initialize() {
		setQueryPrefix("SELECT DISTINCT c.guid FROM TCUSTOMER c");

		final JPQLEnumValueResolver customerTypeEnumResolver = new JPQLEnumValueResolver();
		customerTypeEnumResolver.setEnumValues(getCustomerTypeEnumValues());
		configureField(EpQLField.CUSTOMER_TYPE, "c.CUSTOMER_TYPE", nonLocalizedFieldResolver, customerTypeEnumResolver,
				EpQLFieldType.ENUM, subQueryBuilder);
		configureField(EpQLField.SHARED_ID, "c.SHARED_ID", nonLocalizedFieldResolver, EpQLFieldType.STRING, subQueryBuilder);
		configureField(EpQLField.LAST_MODIFIED_DATE, "c.LAST_EDIT_DATE", nonLocalizedFieldResolver, EpQLFieldType.DATE,
				subQueryBuilder);

		addSortField("c.guid", EpQLSortOrder.ASC);
	}

	private Set<String> getCustomerTypeEnumValues() {
		return CustomerType.values().stream()
				.map(CustomerType::getName)
				.collect(Collectors.toSet());
	}

	public void setNonLocalizedFieldResolver(final NonLocalizedFieldResolver nonLocalizedFieldResolver) {
		this.nonLocalizedFieldResolver = nonLocalizedFieldResolver;
	}

	public void setSubQueryBuilder(final SubQueryBuilder subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}
}

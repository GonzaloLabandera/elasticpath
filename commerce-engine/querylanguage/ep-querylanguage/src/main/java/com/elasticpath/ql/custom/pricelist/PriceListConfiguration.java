/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.custom.pricelist;

import com.elasticpath.ql.parser.AbstractEpQLCustomConfiguration;
import com.elasticpath.ql.parser.EpQLField;
import com.elasticpath.ql.parser.EpQLFieldType;
import com.elasticpath.ql.parser.EpQLSortOrder;
import com.elasticpath.ql.parser.FetchType;
import com.elasticpath.ql.parser.fieldresolver.impl.NonLocalizedFieldResolver;
import com.elasticpath.ql.parser.querybuilder.SubQueryBuilder;

/**
 * Holds mapping between EqQL fields and field descriptors for Price list.
 */
public class PriceListConfiguration extends AbstractEpQLCustomConfiguration {
	
	private NonLocalizedFieldResolver nonLocalizedFieldResolver;

	private SubQueryBuilder subQueryBuilder;

	@Override
	public void initialize() {
		setQueryPrefix("select p.guid FROM PriceListDescriptorImpl p");
		configureField(EpQLField.PRICE_LIST_NAME, "p.name", nonLocalizedFieldResolver, EpQLFieldType.STRING, subQueryBuilder);
		addSortField("p.guid", EpQLSortOrder.ASC);
		setFetchType(FetchType.GUID);
	}
	
	/**
	 * Sets non localized field resolver restricting both parameter1 and parameter2 for fields of Code kind.
	 *  
	 * @param nonLocalizedFieldResolver non localized field resolver
	 */
	public void setNonLocalizedFieldResolver(final NonLocalizedFieldResolver nonLocalizedFieldResolver) {
		this.nonLocalizedFieldResolver = nonLocalizedFieldResolver;
	}

	/**
	 * Sets conventional query builder.
	 * 
	 * @param subQueryBuilder sub query builder
	 */
	public void setSubQueryBuilder(final SubQueryBuilder subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}
	
}

/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.parser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.ql.parser.fieldresolver.EpQLFieldResolver;
import com.elasticpath.ql.parser.querybuilder.CompleteQueryBuilder;
import com.elasticpath.ql.parser.querybuilder.SubQueryBuilder;
import com.elasticpath.ql.parser.valueresolver.EpQLValueResolver;

/**
 * Holds a description of all allowed fields along with fields description for a specific
 * EP QL object which is Product in the following example: * FIND Product.
 */
public abstract class AbstractEpQLCustomConfiguration {

	private final Map<EpQLField, EpQLFieldDescriptor> epQL2SolrMap = new EnumMap<>(EpQLField.class);

	private final List<EpQLSortClause> epQLSortClauses = new ArrayList<>();

	private EpQLValueResolver epQLValueResolver;

	private CompleteQueryBuilder completeQueryBuilder;
	
	private String queryPrefix;
	
	private FetchType fetchType = FetchType.UID;

	/**
	 * Gets a mapping between EqQL Field names and Solr field descriptors.
	 * 
	 * @return a map of Ep QL field (as string) to Solr Descriptor describing type, localization and some other characteristics.
	 */
	public Map<EpQLField, EpQLFieldDescriptor> getAvailableEpQLObjectFields() {
		return epQL2SolrMap;
	}

	/**
	 * Gets a list of sort clauses.
	 * @return the sort clauses
	 */
	public List<EpQLSortClause> getEpQLSortClauses() {
		return epQLSortClauses;
	}
	
	/**
	 * Initializes this EpQLObjectConfiguration object.
	 */
	public abstract void initialize();
	
	/**
	 * Prepares Solr field descriptor used to recognize EpQl field.
	 * Correspondence is kept by means of map
	 * 
	 * @param epQLFieldName the name which may occur in EpQL query
	 * @param solrFieldName recognizable Solr name existing in indexes
	 * @param fieldResolver resolver recognizing and translating EpQL field names into Solr field names
	 * @param fieldType type of field value
	 * @param subQueryBuilder query builder to build queries described by constructed descriptor
	 */
	public void configureField(final EpQLField epQLFieldName, final String solrFieldName,
			final EpQLFieldResolver fieldResolver, final EpQLFieldType fieldType, final SubQueryBuilder subQueryBuilder) {
		final EpQLFieldDescriptor descriptor = new EpQLFieldDescriptor();
		descriptor.setFieldTemplate(solrFieldName);
		descriptor.setEpQLFieldResolver(fieldResolver);
		descriptor.setEpQLValueResolver(epQLValueResolver);
		descriptor.setSubQueryBuilder(subQueryBuilder);
		descriptor.setType(fieldType);
		getAvailableEpQLObjectFields().put(epQLFieldName, descriptor);
	}

	/**
	 * Adds a field to sort by.
	 * @param nativeFieldName the field name in the native query language
	 * @param sortOrder the sort order
	 */
	public void addSortField(final String nativeFieldName, final EpQLSortOrder sortOrder) {
		epQLSortClauses.add(new EpQLSortClause(nativeFieldName, sortOrder));
	}

	/**
	 * Gets the query prefix.
	 * 
	 * @return the query prefix
	 */
	public String getQueryPrefix() {
		return queryPrefix;
	}

	/**
	 * Sets the prefix that will be used for building query.
	 * 
	 * @param queryPrefix the query prefix
	 */
	public void setQueryPrefix(final String queryPrefix) {
		this.queryPrefix = queryPrefix;
	}

	/**
	 * @return the epQLValueResolver
	 */
	protected EpQLValueResolver getEpQLValueResolver() {
		return epQLValueResolver;
	}

	/**
	 * Sets conventional value resolver which doesn't make any transformation of values to search for.
	 * 
	 * @param epQLValueResolver conventional value resolver
	 */
	public void setEpQLValueResolver(final EpQLValueResolver epQLValueResolver) {
		this.epQLValueResolver = epQLValueResolver;
	}

	/**
	 * Gets complete query builder.
	 * 
	 * @return the completeQueryBuilder the complete query builder
	 */
	public CompleteQueryBuilder getCompleteQueryBuilder() {
		return completeQueryBuilder;
	}

	/**
	 * Sets complete query builder.
	 * 
	 * @param completeQueryBuilder the completeQueryBuilder to set
	 */
	public void setCompleteQueryBuilder(final CompleteQueryBuilder completeQueryBuilder) {
		this.completeQueryBuilder = completeQueryBuilder;
	}

	/**
	 * Gets the fetch type.
	 * 
	 * @return teh fetch type
	 */
	public FetchType getFetchType() {
		return fetchType;
	}

	/**
	 * Sets the fetch type.
	 * 
	 * @param fetchType the fecth type
	 */
	public void setFetchType(final FetchType fetchType) {
		this.fetchType = fetchType;
	}


}

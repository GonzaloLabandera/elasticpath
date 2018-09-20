/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.importexport.exporter.configuration.search;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.ql.parser.EPQueryType;

/**
 * SearchConfiguration represents search query for export execution. Class is designed for JAXB and emulates the set of UI controls.
 */
@XmlRootElement(name = "searchconfiguration")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SearchConfiguration", propOrder = { "epQLQuery", "queries" })
public class SearchConfiguration {

	@XmlElement(name = "epql")
	private String epQLQuery;

	@XmlElement(name = "query", required = false, type = SearchQuery.class)
	private final List<SearchQuery> queries = new ArrayList<>();

	/**
	 * Gets the EPQL query.
	 *
	 * @return the EPQL query
	 */
	public String getEpQLQuery() {
		return epQLQuery;
	}

	/**
	 * Gets the queries.
	 * 
	 * @return the queries
	 */
	public List<SearchQuery> getQueries() {
		return queries;
	}

	/**
	 * Find the non-qualified query in the search configuration.
	 *
	 * @return the EPQL query which has no type attribute.
	 */
	public String getDefaultQuery() {
		for (SearchQuery query : queries) {
			if (query.getType() == null) {
				return query.getEPQL();
			}
		}

		return epQLQuery;
	}

	/**
	 * SearchConfiguration supports multiple queries. This method returns the one associated with the specified {@code EPQueryType} or if none is
	 * found, the one returned by getDefaultQuery.
	 *
	 * @param epQueryType see EPQueryType
	 * @return an EPQL query
	 */
	public String getQuery(final EPQueryType epQueryType) {
		for (SearchQuery query : queries) {
			if (epQueryType.getTypeName().equalsIgnoreCase(query.getType())) {
				return query.getEPQL();
			}
		}

		return getDefaultQuery();
	}

	/**
	 * Sets the EPQL query.
	 *
	 * @param epQLQuery the EPQL query
	 */
	public void setEpQLQuery(final String epQLQuery) {
		this.epQLQuery = epQLQuery;
	}
}

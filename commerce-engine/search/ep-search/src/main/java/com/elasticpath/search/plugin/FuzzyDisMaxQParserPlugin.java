/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.search.plugin;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * Solr plugin to provide a Disjunction Max Query that can use fuzzy search if required.
 */
public class FuzzyDisMaxQParserPlugin extends QParserPlugin {

	@Override
	public QParser createParser(final String qstr, final SolrParams localParams,
	                            final SolrParams params, final SolrQueryRequest req) {
		return new DismaxQParser(qstr, localParams, params, req);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) {
		// nothing required
	}
}

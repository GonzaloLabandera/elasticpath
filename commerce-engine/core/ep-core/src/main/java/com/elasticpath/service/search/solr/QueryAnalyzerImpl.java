/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.search.solr;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * Provides analyzing tools for <code>QueryComposer</code>s. Provides a bug-fix for Solr
 * thinking a space is a delimiter. I.e. the query "productName:canon camera" will be parsed as
 * "productName:canon &lt;defaultField&gt;:camera".
 */
public class QueryAnalyzerImpl extends AnalyzerImpl {
	/**
	 * Quote char.
	 */
	protected static final char QUOTE_CHAR = '"';

	private static final Analyzer ANALYZER = new SimpleAnalyzer();

	private static final ThreadLocal<QueryParser> QUERY_PARSER = new ThreadLocal<>();

	@Override
	public String analyze(final String value) {
		return analyze(value, false);
	}

	@Override
	public String analyze(final String value, final boolean isEscapeAllQuotes) {
		String result = super.analyze(value, isEscapeAllQuotes);
		return ClientUtils.escapeQueryChars(result);
	}

	/**
	 * Get parser object.
	 * @return parser object
	 */
	protected QueryParser getParser() {
		if (QUERY_PARSER.get() == null) {
			QUERY_PARSER.set(new QueryParser("text", ANALYZER));
		}
		return QUERY_PARSER.get();
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.search.solr.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.junit.Test;

import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.query.SearchCriteria;

/**
 * Test case for {@link AbstractQueryComposerImpl}.
 */
public class AbstractQueryComposerImplTest extends QueryComposerTestCase {

	private AbstractQueryComposerImpl queryComposer;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of any errors.
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		queryComposer = getQueryComposer();
	}

	private AbstractQueryComposerImpl getQueryComposer() {
		return new AbstractQueryComposerImpl() {

			@Override
			protected Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
				return new BooleanQuery.Builder().build();
			}

			@Override
			protected Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
				return new BooleanQuery.Builder().build();
			}

			@Override
			protected boolean isValidSearchCriteria(final SearchCriteria searchCriteria) {
				return true;
			}
		};
	}

	/**
	 * Test method for {@link AbstractQueryComposerImpl#getMatchAllQuery()}.
	 */
	@Test
	public void testMatchAllQuery() throws ParseException {
		// our query _has_ to go through the lucene query parser
		QueryParser parser = new QueryParser("text",
				new SimpleAnalyzer());

		Query query = parser.parse(queryComposer.getMatchAllQuery().toString());

		assertThat(query).isEqualTo(new MatchAllDocsQuery());
	}

	@Override
	public void testWrongSearchCriteria() {
		// not valid for the abstract class
	}

	@Override
	public void testEmptyCriteria() {
		// not valid for the abstract class
	}

	@Override
	protected QueryComposer getComposerUnderTest() {
		return null;
	}

	@Override
	protected SearchCriteria getCriteriaUnderTest() {
		return null;
	}
}

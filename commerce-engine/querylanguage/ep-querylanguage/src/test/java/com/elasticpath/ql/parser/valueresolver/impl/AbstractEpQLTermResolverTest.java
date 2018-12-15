/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.parser.valueresolver.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.ql.parser.EpQLField;
import com.elasticpath.ql.parser.EpQLTerm;
import com.elasticpath.ql.parser.gen.ParseException;
import com.elasticpath.service.search.solr.AnalyzerImpl;

/**
 * Tests for abstract functionality of <code>com.elasticpath.ql.parser.AbstractEpQLTermResolver</code>.
 */
public class AbstractEpQLTermResolverTest {

	private LuceneValueResolver valueResolver;

	/**
	 * Setup test.
	 */
	@Before
	public void setUp() throws Exception {
		valueResolver = new LuceneValueResolver();
		valueResolver.setAnalyzer(new AnalyzerImpl());
	}

	/**
	 * Tests for 'com.elasticpath.ql.parser.AbstractEpQLTermResolver.resolveDateValue(String field, String queryText) throws
	 * ParseException'.
	 */
	@Test
	public void testResolveDateValue() throws ParseException {
		final EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_START_DATE.getFieldName(), null, null, "=", "'2008-01-30T10:22:22'");
		// not a good idea to test actual strings, since resolved value is in UTC as opposed to queryText which is in local time.
		assertThat(valueResolver.resolveDateValue(epQLTerm)).isNotNull();
	}

	/**
	 * Tests for 'com.elasticpath.ql.parser.AbstractEpQLTermResolver.resolveStringValue(String field, String queryText) throws
	 * ParseException'.
	 */
	@Test
	public void testResolveStringValue() throws ParseException {
		EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_CODE.getFieldName(), null, null, "=", "'+-&&||!(){}[]^\"~*?:\\'");
		assertThat(valueResolver.resolveStringValue(epQLTerm)).isEqualTo("\\+\\-\\&\\&\\|\\|\\!\\(\\)\\{\\}\\[\\]\\^\\&quot;\\~\\*\\?\\:\\\\");
	}

	/**
	 * Tests that value resolver throws exceptions when trying to resolve string values without quotes.
	 */
	@Test
	public void testWrongStringValue() throws ParseException {
		EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_CODE.getFieldName(), null, null, "=", "no quotes");
		assertThatThrownBy(() -> valueResolver.resolveStringValue(epQLTerm))
			.as("This query should throw exception because string must be enclosed with single quotes")
			.isInstanceOf(ParseException.class)
			.hasMessage("Value must be enclosed with single quotes for field ProductCode");
	}

	/**
	 * Tests that value resolver throws exception when trying to resolve date values without quotes.
	 */
	@Test
	public void testWrongDateValue() throws ParseException {
			final EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_END_DATE.getFieldName(), null, null, "=", "2008-03-14:00:00:00");
			assertThatThrownBy(() -> valueResolver.resolveDateValue(epQLTerm))
				.as("This query should throw exception because date must be enclosed with single quotes")
				.isInstanceOf(ParseException.class)
				.hasMessage("Value must be enclosed with single quotes for field ProductEndDate");
	}
}

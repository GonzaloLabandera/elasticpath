/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.solr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link QueryAnalyzerImpl}.
 */
public class QueryAnalyzerImplTest {

	private QueryAnalyzerImpl queryAnalyzer;
	private static final String FORMAT = "test%sTest";
	private static final char ESCAPE_CHARACTER = '\\';

	@Before
	public void setUp() {
		queryAnalyzer = new QueryAnalyzerImpl();
	}

	/**
	 * Test method for {@link QueryAnalyzerImpl#analyze(String)}.
	 */
	@Test
	public void testEscapeSolrQueryCharacters() {
		String[] illegalCharacters = new String[] {
				"+",
				"-",
				"!",
				":",
				";",
				"(",
				")",
				"[",
				"]",
				"{",
				"}",
				"^",
				"\"",
				"*",
				"?",
				"|",
				"&",
				"/",
				"~"
		};

		for (String illegalCharacter : illegalCharacters) {
			String value = String.format(FORMAT, illegalCharacter);
			String analyze = queryAnalyzer.analyze(value);
			assertThat(analyze.charAt(analyze.indexOf(illegalCharacter) - 1))
					.as("expected " + illegalCharacter + " to be escaped")
					.isEqualTo(ESCAPE_CHARACTER);
		}

		assertThat(queryAnalyzer.analyze("test\\Test"))
				.as("expected \\ to be escaped")
				.isEqualTo("test\\\\Test");
	}

	@Test
	public void testValidStringNotEscaped() {
		String analyze = queryAnalyzer.analyze("test");
		assertThat(analyze).isEqualToIgnoringCase("test");
	}
}

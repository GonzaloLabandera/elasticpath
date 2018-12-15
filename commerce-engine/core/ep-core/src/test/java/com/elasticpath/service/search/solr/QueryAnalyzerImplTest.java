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
	private static final char EXPECTED_QUOTATION_MARK = '"';

	@Before
	public void setUp() {
		queryAnalyzer = new QueryAnalyzerImpl();
	}

	/**
	 * Test method for {@link QueryAnalyzerImpl#analyze(String)}.
	 */
	@Test
	public void testAnalyzeQuotedString() {
		String[] illegalCharacters = new String[] {
				";",
				" -",
				"(",
				")",
				"[",
				"]",
				"{",
				"}",
				"^",
				"\"",
				"~",
				":"
			};

		for (String illegalCharacter : illegalCharacters) {
			String value = String.format(FORMAT, illegalCharacter);
			String analyze = queryAnalyzer.analyze(value);

			assertThat(analyze.charAt(0)).as("expected " + illegalCharacter +" to be in quotation marks").isEqualTo(EXPECTED_QUOTATION_MARK);
		}
	}

	@Test
	public void testAnalyzeEscapedString() {
		String[] illegalCharacters = new String[] {
				"\\;",
				"\\(",
				"\\)",
				"\\[",
				"\\]",
				"\\{",
				"\\}",
				"\\^",
				"\\\"",
				"\\~"
		};

		for (String illegalCharacter : illegalCharacters) {
			String value = String.format(FORMAT, illegalCharacter);
			String analyze = queryAnalyzer.analyze(value);

			assertThat(analyze.charAt(analyze.indexOf(illegalCharacter)))
					.as("expected " + illegalCharacter +"  to be in proceeded by escape character in `" + analyze + "`")
					.isEqualTo('\\');
			assertThat(analyze.charAt(0))
					.as("expected no quotation mark at beginning of string")
					.isNotEqualTo('"');
		}
	}

	@Test
	public void testStringNotEscaped() {
		String analyze = queryAnalyzer.analyze("test");
		assertThat(analyze).isEqualToIgnoringCase("test");
	}
}

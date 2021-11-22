/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.persistence.openjpa.support;

import org.junit.Test;

public class JPAUtilTest {

	@Test
	public void shouldExpandNativeQueryWith2QuestionMarks() {
		String nativeQueryWithList = "SELECT field FROM table WHERE field IN (     ?    )";
		int numberOfListValues = 2;

		String expectedQuery = "SELECT field FROM table WHERE field IN (?,?)";
		String actualQuery = JPAUtil.expandListParameterForNativeQuery(nativeQueryWithList, numberOfListValues);

		assert actualQuery.equals(expectedQuery) : "Actual query doesn't match the expected one";
	}

	@Test
	public void shouldModifyNativeQueryWithOneQuestionMark() {
		String nativeQueryWithList = "SELECT field FROM table WHERE field IN (     ?    )";
		int numberOfListValues = 1;

		String expectedQuery = "SELECT field FROM table WHERE field IN (?)";
		String actualQuery = JPAUtil.expandListParameterForNativeQuery(nativeQueryWithList, numberOfListValues);

		assert actualQuery.equals(expectedQuery) : "Actual query doesn't match the expected one";
	}
}

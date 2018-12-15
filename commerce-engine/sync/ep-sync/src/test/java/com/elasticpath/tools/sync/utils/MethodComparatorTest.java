/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tools.sync.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * Test for MethodComparator.
 */
public class MethodComparatorTest {

	private static final String TEST_METHOD_1 = "testMethod1";

	/**
	 * Test Data Interface for getting Methods form it.
	 */
	private interface TestInterface {
		void testMethod1(String arg1); 
		
		int testMethod1();
		int testMethod2();
		
		int testMethod2(String arg1);

		void testMethod3();
		void testMethod3(int arg1);
		void testMethod3(String arg1);
	}
	
	private Method getMethodByName(final String name, final Class<?>...params) throws NoSuchMethodException {
		return TestInterface.class.getMethod(name, params);
	}

	/**
	 * Tests compare of the MethodComparator.
	 */
	@Test
	public void testCompare() throws NoSuchMethodException {
		final MethodComparator methodComparator = new MethodComparator();
		
		assertThat(methodComparator.compare(getMethodByName(TEST_METHOD_1), getMethodByName(TEST_METHOD_1))).isZero();
		assertThat(methodComparator.compare(getMethodByName(TEST_METHOD_1), getMethodByName("testMethod2"))).isNegative();
		assertThat(methodComparator.compare(getMethodByName(TEST_METHOD_1), getMethodByName(TEST_METHOD_1, String.class))).isNegative();
		assertThat(methodComparator.compare(getMethodByName("testMethod2"), getMethodByName("testMethod2", String.class))).isNegative();
		assertThat(methodComparator.compare(getMethodByName("testMethod3", int.class), getMethodByName("testMethod3", String.class))).isNegative();
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Tests for the custom Spring extensibleList namespace.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/extensibleListNamespace.xml")
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class
})
public class ExtensibleListNamespaceTest {

	private static final String RED = "red";
	private static final String BLACK = "black";
	private static final String GREEN = "green";
	private static final String BLUE = "blue";
	private static final Integer ONE_INT = Integer.valueOf(1);
	private static final Long ONE_HUNDRED_THOUSAND_LONG = Long.valueOf(100000);
	private static final Float ONE_HUNDRED_THOUSAND_OH_ONE_FLOAT = Float.valueOf(100000.01f);

	@Resource
	private List<String> testList1;

	@Resource
	private List<String> testList2;

	@Resource
	private List<String> testList3;

	@Resource
	private List<String> testList4;

	@Resource
	private List<Number> testListNumberTypes;

	/**
	 * Test creation of a list via extensibleList:create.
	 */
	@Test
	public void testCreateList() {
		assertThat(testList3).containsOnlyElementsOf(ImmutableList.of(RED, GREEN, BLUE));
	}

	/**
	 * Test removal of a list element via extensibleList:modify.
	 */
	@Test
	public void testElementsRemoved() {
		assertThat(testList2).containsOnlyElementsOf(ImmutableList.of(RED, GREEN));
	}

	/**
	 * Test addition of a list element via extensibleList:modify.
	 */
	@Test
	public void testElementsAdded() {
		assertThat(testList4).containsOnlyElementsOf(ImmutableList.of(RED, GREEN, BLUE, BLACK));
	}

	/**
	 * Test simultaneous addition and removal of list elements via extensibleList:modify.
	 */
	@Test
	public void testElementsAddedAndRemoved() {
		assertThat(testList1).containsOnlyElementsOf(ImmutableList.of(BLACK));
	}

	/**
	 * Test creation of a list with an invalid valueType element via extensibleList:create.
	 */
	@Test
	public void testCreateNumberTypesList() {
		assertThat(testListNumberTypes).containsOnlyElementsOf(ImmutableList.of(ONE_INT, ONE_HUNDRED_THOUSAND_LONG,
				ONE_HUNDRED_THOUSAND_OH_ONE_FLOAT));
	}
}
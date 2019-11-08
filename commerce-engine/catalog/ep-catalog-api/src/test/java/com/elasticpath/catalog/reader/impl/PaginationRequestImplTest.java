/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.reader.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;

import com.elasticpath.catalog.reader.PaginationRequest;

/**
 * Class for testing {@link PaginationRequestImpl}.
 */
public class PaginationRequestImplTest {

	@Test
	public void testThatValueIsSetWhenLimitIsNull() {
		final String startAfterValue = "startAfter";
		final PaginationRequest paginationRequest = new PaginationRequestImpl(null, startAfterValue);

		assertThat(paginationRequest.getLimit()).isNotNull();
	}

	@Test
	public void testThatValueIsSetWhenStartAfterIsNull() {
		final String someLimit = "2";
		final PaginationRequest paginationRequest = new PaginationRequestImpl(someLimit, null);

		assertThat(paginationRequest.getStartAfter()).isNotNull();
	}

	@Test
	public void testThatValuesIsSetWhenStartAfterAndLimitAreNull() {
		final PaginationRequest paginationRequest = new PaginationRequestImpl(null, null);

		assertThat(paginationRequest.getLimit()).isNotNull();
		assertThat(paginationRequest.getStartAfter()).isNotNull();
	}

	@Test
	public void testThatValuesIsSetWhenPaginationRequestIsBuiltThroughConstructorWithoutParameters() {
		final PaginationRequest paginationRequest = new PaginationRequestImpl();

		assertThat(paginationRequest.getLimit()).isNotNull();
		assertThat(paginationRequest.getStartAfter()).isNotNull();
	}
}

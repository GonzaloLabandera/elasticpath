/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.search.index.solr.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.search.index.solr.service.SearchIndexLocator;
import com.elasticpath.search.index.solr.service.impl.SearchIndexExistencePredicate.IndexVersionNumberSupplier;
import com.elasticpath.service.search.IndexType;

/**
 * Test class for {@link SearchIndexExistencePredicate}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchIndexExistencePredicateTest {

	private static final IndexType INDEX_TYPE = IndexType.PRODUCT;

	private final File indexDir = new File(".");

	@Mock
	private SearchIndexLocator searchIndexLocator;

	@Mock
	private IndexVersionNumberSupplier indexVersionNumberSupplier;

	@Spy
	@InjectMocks
	private SearchIndexExistencePredicate searchIndexExistencePredicate;

	@Before
	public void setup() throws IOException {
		when(searchIndexLocator.getSearchIndexLocation(IndexType.PRODUCT))
				.thenReturn(indexDir);

		doReturn(indexVersionNumberSupplier)
				.when(searchIndexExistencePredicate).createIndexVersionNumberSupplier(new File(indexDir, "index"));
	}

	@Test
	public void verifyIoExceptionsRethrownAsUnchecked() throws Exception {
		doThrow(new IOException("Boom!"))
				.when(searchIndexExistencePredicate).createIndexVersionNumberSupplier(new File(indexDir, "index"));

		assertThatThrownBy(() -> searchIndexExistencePredicate.test(INDEX_TYPE))
				.isInstanceOf(EpServiceException.class);
	}

	@Test
	public void verifyDirectoryDoesNotExistWhenIndexVersionIsTwo() {
		when(indexVersionNumberSupplier.get()).thenReturn(2L);

		assertThat(searchIndexExistencePredicate.test(IndexType.PRODUCT))
				.isFalse();
	}

	@Test
	public void verifyDirectoryExistsWhenIndexVersionGreaterThanTwo() {
		when(indexVersionNumberSupplier.get()).thenReturn(3L);

		assertThat(searchIndexExistencePredicate.test(IndexType.PRODUCT))
				.isTrue();
	}

}
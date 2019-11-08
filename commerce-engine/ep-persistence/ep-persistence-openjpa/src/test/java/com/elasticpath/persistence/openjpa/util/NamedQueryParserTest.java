/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.QueryMetaData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for the {@code NamedQueryParser} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class NamedQueryParserTest {
	private static final String RETRIABLE_QUERY_NAME = "RETRIABLE_QUERY_NAME";
	private static final String NON_RETRIABLE_QUERY_NAME = "NON_RETRIABLE_QUERY_NAME";
	private static final String RETRIABLE_QUERY = "Select c From CustomerImpl c Where c.uidPk=?1";
	private static final String NON_RETRIABLE_QUERY = "Select s From ShopperMementoImpl s";
	private static final String CUSTOMER_IMPL_FROM_ENTITY = "CustomerImpl";

	@InjectMocks
	@Spy
	private NamedQueryParser namedQueryParser;

	@Mock private QueryMetaData retriableQueryMetaData;
	@Mock private QueryMetaData nonRetriableQueryMetaData;
	@Mock private ClassMetaData customerImplMetaData;

	@Before
	public void init() {
		when(retriableQueryMetaData.getName()).thenReturn(RETRIABLE_QUERY_NAME);
		when(retriableQueryMetaData.getQueryString()).thenReturn(RETRIABLE_QUERY);

		when(nonRetriableQueryMetaData.getName()).thenReturn(NON_RETRIABLE_QUERY_NAME);
		when(nonRetriableQueryMetaData.getQueryString()).thenReturn(NON_RETRIABLE_QUERY);
	}

	/**
	 * Test parsing both retrirable and non retriable queries.
	 */
	@Test
	public final void shouldParseNamedQueries() {
		doReturn(new ClassMetaData[]{customerImplMetaData}).when(namedQueryParser).getQueryClassMetaDatas(RETRIABLE_QUERY);
		doReturn(CustomerImpl.class).when(customerImplMetaData).getDescribedType();

		namedQueryParser.parse(new QueryMetaData[]{retriableQueryMetaData, nonRetriableQueryMetaData});

		assertThat(namedQueryParser.getQueriedEntitiesByQueryName(RETRIABLE_QUERY_NAME))
			.contains(CUSTOMER_IMPL_FROM_ENTITY);
		assertThat(namedQueryParser.getQueriedEntitiesByQueryName(NON_RETRIABLE_QUERY_NAME))
			.isEmpty();

		assertThat(namedQueryParser.isQueryRetriable(RETRIABLE_QUERY_NAME))
			.isTrue();
		assertThat(namedQueryParser.isQueryRetriable(NON_RETRIABLE_QUERY_NAME))
			.isFalse();

	}

	/**
	 * Test parsing dynamic retriable queries.
	 */
	@Test
	public final void shouldParseDynamicRetriableQuery() {
		String dynamicRetriableQuery = "Select x From CustomerImpl x Where x.uidPk >=?1";

		doReturn(new ClassMetaData[]{customerImplMetaData}).when(namedQueryParser).getQueryClassMetaDatas(dynamicRetriableQuery);
		doReturn(CustomerImpl.class).when(customerImplMetaData).getDescribedType();

		//don't parse named queries - try to obtain FROM entities from a dynamic query
		assertThat(namedQueryParser.getQueriedEntitiesByQueryName(dynamicRetriableQuery))
			.contains("CustomerImpl");

		//dynamic retriable query must be in the map
		assertThat(namedQueryParser.isQueryRetriable(dynamicRetriableQuery))
			.isTrue();
	}

	/**
	 * Test handling dynamic non-retriable queries.
	 */
	@Test
	public final void shouldNotParseDynamicNonRetriableQuery() {
		String dynamicNonRetriableQuery = "Select x From XManEntityImpl x";

		assertThat(namedQueryParser.getQueriedEntitiesByQueryName(dynamicNonRetriableQuery))
			.isEmpty();

		assertThat(namedQueryParser.isQueryRetriable(dynamicNonRetriableQuery))
			.isFalse();
	}

	private class CustomerImpl {
		//empty class
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.catalog.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.query.QueryCriteria;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.impl.QueryResultImpl;

public class ProductLookupImplTest {
	private static final String PRODUCT_CODE = "PRODUCT_CODE";
	private static final long PRODUCT_UID = 1L;
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock private QueryService<Product> queryService;
	@Mock private ProductLoadTuner loadTuner;
	@Mock private PersistenceEngine persistenceEngine;

	private ProductLookupImpl lookup;
	private ProductImpl product;

	@Before
	public void setUp() {
		product = new ProductImpl();
		product.setUidPk(PRODUCT_UID);
		product.setCode(PRODUCT_CODE);

		lookup = new ProductLookupImpl();
		lookup.setQueryService(queryService);
		lookup.setProductLoadTuner(loadTuner);
		lookup.setPersistenceEngine(persistenceEngine);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByGuid() {
		//  Given
		context.checking(new Expectations() {
			{
				QueryResultImpl<Product> result = new QueryResultImpl<>();
				result.setResults(Collections.singletonList(product));
				allowing(queryService).query(with(any(QueryCriteria.class)));
				will(returnValue(result));
			}
		});

		// When
		Product found = lookup.findByGuid(PRODUCT_CODE);

		// Then
		assertSame("Reader should ask the query service for the corresponding product", product, found);
	}

	@Test
	public void testFindByUid() {
		//  Given
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).withLoadTuners(loadTuner); will(returnValue(persistenceEngine));
				allowing(persistenceEngine).get(ProductImpl.class, PRODUCT_UID);
				will(returnValue(product));
			}
		});

		// When
		Product found = lookup.findByUid(PRODUCT_UID);

		// Then
		assertSame("Reader should ask the persistence engine for the corresponding product", product, found);
	}


	@Test
	@SuppressWarnings("unchecked")
	public void testFindByUids() {
		//  Given
		context.checking(new Expectations() {
			{
				QueryResultImpl<Product> result = new QueryResultImpl<>();
				result.setResults(Collections.singletonList(product));
				allowing(queryService).query(with(any(QueryCriteria.class)));
				will(returnValue(result));
			}
		});

		// When
		List<Product> found = lookup.findByUids(Collections.singletonList(PRODUCT_UID));

		// Then
		assertEquals("Reader should ask the persistence engine for the corresponding products",
				Collections.singletonList(product), found);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByGuids() {
		context.checking(new Expectations() {
			{
				QueryResultImpl<Product> result = new QueryResultImpl<>();
				result.setResults(Collections.singletonList(product));
				allowing(queryService).query(with(any(QueryCriteria.class)));
				will(returnValue(result));
			}
		});

		final List<Product> found = lookup.findByGuids(Collections.singletonList(PRODUCT_CODE));

		assertEquals("Reader should ask the persistence engine for the corresponding products",
				Collections.singletonList(product), found);
	}
}

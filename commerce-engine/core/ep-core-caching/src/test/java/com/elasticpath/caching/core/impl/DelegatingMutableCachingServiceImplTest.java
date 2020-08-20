/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.impl;

import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.impl.AttributeImpl;

/**
 * Tests {@link DelegatingMutableCachingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingMutableCachingServiceImplTest {

	private DelegatingMutableCachingServiceImpl cachingService;

	@Mock
	private MutableCachingService<Attribute> attributeCachingService;

	private Attribute attribute;

	@Before
	public void setUp() {
		cachingService = new DelegatingMutableCachingServiceImpl();
		cachingService.setCachingServices(ImmutableMap.of(AttributeImpl.class, attributeCachingService));

		attribute = new AttributeImpl();
	}

	@Test
	public void testCache() {
		cachingService.cache(attribute);
		verify(attributeCachingService).cache(attribute);
	}

	@Test
	public void testInvalidate() {
		cachingService.invalidate(attribute);
		verify(attributeCachingService).invalidate(attribute);
	}

	@Test
	public void testInvalidateAll() {
		cachingService.invalidateAll();
		verify(attributeCachingService).invalidateAll();
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.service.attribute.impl.AttributeValueInfo;

/**
 * Tests {@link CachingAttributeServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingAttributeServiceImplTest {

	private static final String KEY = "key";
	private static final int USAGE_ID = 1;
	private static final Long ATTRIBUTE_UID = 2L;

	@InjectMocks
	private CachingAttributeServiceImpl cachingService;

	@Mock
	private Cache<Integer, List<Attribute>> attributesByUsageIdCache;
	@Mock
	private Cache<Long, List<AttributeValueInfo>> attributeValueByAttributeUidCache;
	@Mock
	private Cache<String, Attribute> attributeByKeyCache;
	@Mock
	private Attribute lookupAttribute;
	@Mock
	private Attribute attribute1;
	@Mock
	private Attribute attribute2;
	@Mock
	private AttributeValueInfo attributeValueInfo1;
	@Mock
	private AttributeValueInfo attributeValueInfo2;

	@Test
	public void testFindByKeyUsesCache() {
		given(attributeByKeyCache.get(KEY)).willReturn(attribute1);
		final Attribute result = cachingService.findByKey(KEY);
		assertThat(result).isEqualTo(attribute1);
	}

	@Test
	public void testGetAttributes() {
		given(attributesByUsageIdCache.get(USAGE_ID)).willReturn(Arrays.asList(attribute1, attribute2));
		List<Attribute> result = cachingService.getAttributes(USAGE_ID);
		assertThat(result).isEqualTo(Arrays.asList(attribute1, attribute2));
	}

	@Test
	public void testFindProductAttributeValueByAttributeUid() {
		given(lookupAttribute.getUidPk()).willReturn(ATTRIBUTE_UID);
		given(attributeValueByAttributeUidCache.get(ATTRIBUTE_UID)).willReturn(Arrays.asList(attributeValueInfo1, attributeValueInfo2));
		List<AttributeValueInfo> result = cachingService.findProductAttributeValueByAttributeUid(lookupAttribute);
		assertThat(result).isEqualTo(Arrays.asList(attributeValueInfo1, attributeValueInfo2));
	}
}

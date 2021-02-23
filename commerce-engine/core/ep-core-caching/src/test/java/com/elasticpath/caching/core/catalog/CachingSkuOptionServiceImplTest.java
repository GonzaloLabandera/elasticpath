/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.caching.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.impl.EhcacheCacheAdapter;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.persistence.api.CachedInstanceDetachmentStrategy;
import com.elasticpath.persistence.dao.ProductTypeDao;
import com.elasticpath.service.catalog.SkuOptionService;

@RunWith(MockitoJUnitRunner.class)
public class CachingSkuOptionServiceImplTest {
	private static final int FOUR = 4;
	private static final int THREE = 3;
	private static final long ONE = 1L;
	private static final long TWO = 2L;

	@Mock private SkuOptionService fallbackSkuOptionService;
	@Mock private ProductTypeDao productTypeDao;
	@Mock private CachedInstanceDetachmentStrategy detachmentStrategy;

	@InjectMocks
	private CachingSkuOptionServiceImpl fixture;

	private static final String CACHE_NAME = "skuOptionsCache";
	private final CacheManager cacheManager = CacheManager.create();
	private Cache skuOptionsCacheEhcache;
	private com.elasticpath.cache.Cache<SkuOptionCacheKey, SkuOption> skuOptionsCache;

	private final SkuOption skuOption = new SkuOptionImpl();
	private final SkuOption skuOption2 = new SkuOptionImpl();
	private final SkuOption skuOption3 = new SkuOptionImpl();

	private final SkuOptionValue skuOptionValue = new SkuOptionValueImpl();

	private final ProductType productType = new ProductTypeImpl();
	private final ProductType productType2 = new ProductTypeImpl();
	private final ProductType productType3 = new ProductTypeImpl();

	private Set<SkuOption> skuOptions;

	@Before
	public void setUp() {
		skuOption.setOptionKey("optionKey1");
		skuOption2.setOptionKey("optionKey2");
		skuOption3.setOptionKey("optionKey3");

		skuOptionValue.setUidPk(ONE);
		skuOption.setOptionValues(Sets.newHashSet(skuOptionValue));

		skuOptions = Sets.newHashSet(skuOption, skuOption2);

		productType.setUidPk(ONE);
		productType.setSkuOptions(skuOptions);

		productType2.setUidPk(TWO);
		productType2.setSkuOptions(Sets.newHashSet(skuOption, skuOption3));

		productType3.setUidPk(THREE);
		productType3.setSkuOptions(Sets.newHashSet(skuOption2, skuOption3));

		skuOptionsCacheEhcache = new Cache(CACHE_NAME, 0, false, true, 0, 0);
		cacheManager.addCache(skuOptionsCacheEhcache);

		skuOptionsCache = new EhcacheCacheAdapter<>(skuOptionsCacheEhcache);
		fixture.setSkuOptionsCache(skuOptionsCache);
	}

	@After
	public void tearDown() {
		cacheManager.removeCache(CACHE_NAME);
	}

	@Test
	public void shouldInitializeCacheAndReturnSkuOptionsForAllRequestedProductTypes() {
		when(productTypeDao.list()).thenReturn(Lists.newArrayList(productType, productType2, productType3));

		fixture.init();
		assertThat(skuOptionsCacheEhcache.getSize()).isEqualTo(THREE);
		verify(detachmentStrategy, times(FOUR)).detach(any());

		Set<SkuOption> skuOptions = fixture.findByProductTypeUid(productType.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption, skuOption2);

		skuOptions = fixture.findByProductTypeUid(productType2.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption, skuOption3);

		skuOptions = fixture.findByProductTypeUid(productType3.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption2, skuOption3);
	}

	@Test
	public void shouldRetrieveProductTypeSkuOptionsByProductUid() {

		when(fallbackSkuOptionService.findByProductTypeUid(ONE)).thenReturn(skuOptions);
		Set<SkuOption> actualSkuOptions = fixture.findByProductTypeUid(ONE);

		assertThat(actualSkuOptions).containsOnly(skuOption, skuOption2);
		verify(fallbackSkuOptionService).findByProductTypeUid(ONE);

		//should fetch from the cache
		actualSkuOptions = fixture.findByProductTypeUid(ONE);

		assertThat(actualSkuOptions).containsOnly(skuOption, skuOption2);
		verifyNoMoreInteractions(fallbackSkuOptionService);
	}

	@Test
	public void shouldRetrieveSkuOptionByOptionKey() {
		String optionKey = skuOption.getOptionKey();

		when(fallbackSkuOptionService.findByKey(optionKey)).thenReturn(skuOption);
		SkuOption actualSkuOption = fixture.findByKey(optionKey);

		assertThat(actualSkuOption).isEqualTo(skuOption);
		verify(fallbackSkuOptionService).findByKey(optionKey);

		//should fetch from the cache
		actualSkuOption = fixture.findByKey(optionKey);

		assertThat(actualSkuOption).isEqualTo(skuOption);
		verifyNoMoreInteractions(fallbackSkuOptionService);
	}

	@Test
	public void shouldRetrieveSkuOptionValueByOptionKeyAndOptionValueUid() {
		String optionKey = skuOption.getOptionKey();

		when(fallbackSkuOptionService.findByKey(optionKey)).thenReturn(skuOption);
		SkuOptionValue actualSkuOptionValue = fixture.findOptionValueByOptionKeyAndValueUid(optionKey, skuOptionValue.getUidPk());

		assertThat(actualSkuOptionValue).isEqualTo(skuOptionValue);
		verify(fallbackSkuOptionService).findByKey(optionKey);

		//should fetch from the cache
		actualSkuOptionValue = fixture.findOptionValueByOptionKeyAndValueUid(optionKey, skuOptionValue.getUidPk());

		assertThat(actualSkuOptionValue).isEqualTo(skuOptionValue);
		verifyNoMoreInteractions(fallbackSkuOptionService);
	}
}

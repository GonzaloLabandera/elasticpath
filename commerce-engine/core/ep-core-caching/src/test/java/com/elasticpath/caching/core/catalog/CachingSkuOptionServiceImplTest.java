/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.caching.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	private static final int THIRTY = 30;
	private static final int THREE = 3;
	private static final int SIX = 6;
	private static final int FOURTEEN = 14;
	private static final long ONE = 1L;
	private static final long TWO = 2L;

	private static final String OPTIONKEY_1 = "optionKey1";
	private static final String OPTIONKEY_2 = "optionKey2";
	private static final String OPTIONKEY_3 = "optionKey3";

	@Mock private SkuOptionService fallbackSkuOptionService;
	@Mock private ProductTypeDao productTypeDao;
	@Mock private CachedInstanceDetachmentStrategy detachmentStrategy;

	@InjectMocks
	private CachingSkuOptionServiceImpl fixture;

	private static final String CACHE_NAME = "skuOptionsCache";
	private final CacheManager cacheManager = CacheManager.create();
	private Cache skuOptionsCacheEhcache;

	private final SkuOption skuOption = new SkuOptionImpl();
	private final SkuOption skuOptionCopy = new SkuOptionImpl();
	private final SkuOption skuOption2 = new SkuOptionImpl();
	private final SkuOption skuOption3 = new SkuOptionImpl();

	private final SkuOptionValue skuOptionValue = new SkuOptionValueImpl();

	private final ProductType productType = new ProductTypeImpl();
	private final ProductType productType2 = new ProductTypeImpl();
	private final ProductType productType3 = new ProductTypeImpl();

	private Set<SkuOption> skuOptions1;
	private Set<SkuOption> skuOptions2;

	@Before
	public void setUp() {
		skuOption.setOptionKey(OPTIONKEY_1);
		skuOptionCopy.setOptionKey(OPTIONKEY_1);
		skuOption2.setOptionKey(OPTIONKEY_2);
		skuOption3.setOptionKey(OPTIONKEY_3);

		skuOptionValue.setUidPk(ONE);
		skuOption.setOptionValues(Sets.newHashSet(skuOptionValue));

		//OpenJPA will return 2 different instances of the same sku option
		skuOptions1 = Sets.newHashSet(skuOption, skuOption2);
		skuOptions2 = Sets.newHashSet(skuOptionCopy, skuOption3);

		productType.setUidPk(ONE);
		productType.setSkuOptions(skuOptions1);

		productType2.setUidPk(TWO);
		productType2.setSkuOptions(skuOptions2);

		productType3.setUidPk(THREE);
		productType3.setSkuOptions(Sets.newHashSet(skuOption2, skuOption3));

		skuOptionsCacheEhcache = new Cache(CACHE_NAME, 0, false, true, 0, 0);
		cacheManager.addCache(skuOptionsCacheEhcache);

		final com.elasticpath.cache.Cache<Object, Object> skuOptionsCache = new EhcacheCacheAdapter<>(skuOptionsCacheEhcache);
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
		assertThat(skuOptionsCacheEhcache.getSize()).isEqualTo(SIX);
		verify(detachmentStrategy, times(FOURTEEN)).detach(any());

		Set<SkuOption> skuOptions = fixture.findByProductTypeUid(productType.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption, skuOption2);

		skuOptions = fixture.findByProductTypeUid(productType2.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption, skuOption3);

		skuOptions = fixture.findByProductTypeUid(productType3.getUidPk());
		assertThat(skuOptions).containsOnly(skuOption2, skuOption3);
	}

	@Test
	public void shouldRetrieveProductTypeSkuOptionsByProductUid() {

		when(fallbackSkuOptionService.findByProductTypeUid(ONE)).thenReturn(skuOptions1);
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

	/*
	 * When caching product type sku options as a Set, the set must contain the same common option, to avoid duplicates in memory.
	 *
	 * E.g. ProductType1 has SkuOpt1 and SkuOpt2
	 * 		ProductType2 has SkuOpt1 and SkuOpt3
	 *
	 * 	When returned from the db, the OpenJPA returns 2 different instances of SkuOpt1.
	 * 	The desired state for the product type sku options set is to always contain the reference to the same common sku option.
	 *
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void multipleThreadsShouldHaveSameCommonSkuOptions() throws InterruptedException {
		/*start with an empty cache
		skuOptions1 and skuOptions1 contain 2 different instances of the same sku option identified by "optoinKey1"
		as they would be returned by OpenJPA */

		when(fallbackSkuOptionService.findByProductTypeUid(ONE)).thenReturn(skuOptions1);
		when(fallbackSkuOptionService.findByProductTypeUid(TWO)).thenReturn(skuOptions2);

		Runnable runnable1 = () -> fixture.findByProductTypeUid(ONE);
		Runnable runnable2 = () -> fixture.findByProductTypeUid(TWO);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.execute(runnable1);
		executor.execute(runnable2);

		executor.shutdown();
		executor.awaitTermination(THIRTY, TimeUnit.SECONDS);

		Object commonSkuOption = skuOptionsCacheEhcache.get(OPTIONKEY_1).getObjectValue(); //product types ONE & TWO share this option

		assertThat(commonSkuOption).isNotNull();
		assertThat(skuOptionsCacheEhcache.get(OPTIONKEY_2).getObjectValue()).isNotNull();
		assertThat(skuOptionsCacheEhcache.get(OPTIONKEY_3).getObjectValue()).isNotNull();

		Set<SkuOption> prodType1Options = (Set<SkuOption>) skuOptionsCacheEhcache.get(ONE).getObjectValue();
		Set<SkuOption> prodType2Options = (Set<SkuOption>) skuOptionsCacheEhcache.get(TWO).getObjectValue();

		assertThat(prodType1Options).isNotNull();
		assertThat(prodType2Options).isNotNull();

		assertSameReference(prodType1Options, commonSkuOption);
		assertSameReference(prodType2Options, commonSkuOption);
	}

	private void assertSameReference(final Set<SkuOption> prodTypeOptions, final Object commonSkuOption) {
		for (SkuOption prodType1Option : prodTypeOptions) {
			if (prodType1Option.equals(commonSkuOption)) {
				assertThat(prodType1Option).isSameAs(commonSkuOption);
				return;
			}
		}

		fail("Common SKU option is not found in:" + prodTypeOptions);
	}
}

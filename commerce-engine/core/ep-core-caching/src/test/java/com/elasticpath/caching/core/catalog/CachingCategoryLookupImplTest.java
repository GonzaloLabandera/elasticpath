/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.caching.core.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.elasticpath.cache.Cache;
import com.elasticpath.cache.CacheLoader;
import com.elasticpath.cache.CacheResult;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.service.catalog.CategoryLookup;

@SuppressWarnings("unchecked")
public class CachingCategoryLookupImplTest {

	private static final String NULL_CATEGORY_MSG = "Null category should have been returned";
	private static final Long CATEGORY_UID = 1L;

	@Mock private Cache<Long, Category> categoryByUidCache;
	@Mock private Cache<String, Long> categoryUidByGuidCache;
	@Mock private Cache<String, Long> categoryUidByCompoundGuidCache;
	@Mock private Cache<Long, List<Long>> childCache;
	@Mock private CategoryLookup fallbackReader;
	private CachingCategoryLookupImpl cachingCategoryLookup;
	private CategoryImpl category;
	private CategoryImpl category2;
	private CatalogImpl catalog;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		catalog = new CatalogImpl();
		catalog.setCode("catalog");

		category = new CategoryImpl();
		category.initialize();
		category.setUidPk(1L);
		category.setCode("1");
		category.setCatalog(catalog);

		category2 = new CategoryImpl();
		category2.initialize();
		category2.setUidPk(2L);
		category2.setCode("2");
		category2.setCatalog(catalog);
		category2.setParent(category);

		cachingCategoryLookup = new CachingCategoryLookupImpl();
		cachingCategoryLookup.setCategoryByUidCache(categoryByUidCache);
		cachingCategoryLookup.setCategoryUidByGuidCache(categoryUidByGuidCache);
		cachingCategoryLookup.setCategoryUidByCompoundGuidCache(categoryUidByCompoundGuidCache);
		cachingCategoryLookup.setChildCategoryCache(childCache);
		cachingCategoryLookup.setFallbackReader(fallbackReader);
	}

	@Test
	public void testFindByUidDelegatesToCacheWithFallbackLoader() {
		// Given
		when(categoryByUidCache.get(eq(category.getUidPk()), any(Function.class))).thenReturn(category);

		// When
		Category found = cachingCategoryLookup.findByUid(category.getUidPk());

		assertSame("uid Cached category should have been returned", category, found);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByUidsDelegatesToMultiKeyCacheWithFallbackLoader() {
		// Given
		final List<Long> categoryUids = Arrays.asList(category.getUidPk(), category2.getUidPk());
		when(categoryByUidCache.getAll(eq(categoryUids), any(Function.class))).thenReturn(
				ImmutableMap.of(category.getUidPk(), category, category2.getUidPk(), category2));

		// When
		List<Category> found = cachingCategoryLookup.findByUids(categoryUids);

		assertEquals("Cached categories should have been returned",
				Arrays.asList(category, category2), found);
	}


	@Test
	public void testFindByGuidDelegatesToCacheWithFallbackLoader() {
		// Given
		when(categoryUidByGuidCache.get(eq(category.getGuid()), any(Function.class))).thenReturn(category.getUidPk());
		when(categoryByUidCache.get(eq(category.getUidPk()))).thenReturn(CacheResult.create(category));
		// When
		Category found = cachingCategoryLookup.findByGuid(category.getGuid());

		assertSame("guid Cached category should have been returned", category, found);
	}

	@Test
	public void testFindByCategoryCodeAndCatalogDelegatesToCache() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getGuid() + "|" + catalog.getGuid()), any(Function.class)))
			.thenReturn(category.getUidPk());
		when(categoryByUidCache.get(eq(category.getUidPk()))).thenReturn(CacheResult.create(category));

		// When
		Category found = cachingCategoryLookup.findByCategoryCodeAndCatalog(category.getGuid(), catalog);

		assertSame("guid Cached category should have been returned", category, found);
	}

	@Test
	public void testFindByCategoryAndCatalogCodeDelegatesToCache() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getGuid() + "|" + catalog.getGuid()), any(Function.class)))
			.thenReturn(category.getUidPk());
		when(categoryByUidCache.get(eq(category.getUidPk()))).thenReturn(CacheResult.create(category));

		// When
		Category found = cachingCategoryLookup.findByCategoryAndCatalogCode(category.getGuid(), catalog.getCode());

		assertSame("guid Cached category should have been returned", category, found);
	}

	@Test
	public void testFindByCompoundCategoryAndCatalogCodesDelegatesToCache() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getCode() + "|" + catalog.getCode()), any(Function.class)))
			.thenReturn(category.getUidPk());
		when(categoryByUidCache.get(eq(category.getUidPk()))).thenReturn(CacheResult.create(category));

		// When
		Category found = cachingCategoryLookup.findByCompoundCategoryAndCatalogCodes(category.getCode() + "|" + catalog.getCode());

		assertSame("compound guid cached category should have been returned", category, found);
	}

	@Test
	public void testFindParentDelegatesToFindByGuid() {
		// Given
		when(categoryUidByGuidCache.get(eq(category.getGuid()), any(Function.class))).thenReturn(category.getUidPk());
		when(categoryByUidCache.get(eq(category.getUidPk()))).thenReturn(CacheResult.create(category));

		// When
		Category found = cachingCategoryLookup.findParent(category2);

		assertSame("Category 2's parent category should have been returned", category, found);
	}

	@Test
	public void testFindParentReturnsNullIfParentIsNull() {
		// When
		Category found = cachingCategoryLookup.findParent(category);

		assertNull(found);

		verifyNoMoreInteractions(categoryUidByGuidCache);
		verifyNoMoreInteractions(categoryByUidCache);
	}

	@Test
	public void testFindChildrenRetrievesFromCacheOnCacheHit() {
		// Given
		when(childCache.get(category.getUidPk()))
				.thenReturn(CacheResult.create(Collections.singletonList(category2.getUidPk())));
		when(categoryByUidCache.getAll(eq(Collections.singletonList(category2.getUidPk())), any(Function.class)))
				.thenReturn(Collections.singletonMap(category2.getUidPk(), category2));

		// When
		List<Category> found = cachingCategoryLookup.findChildren(category);

		assertEquals("Cached child categories should have been returned",
				Collections.singletonList(category2), found);
	}

	@Test
	public void testFindChildrenRetrievesFromFallbackReaderOnCacheMiss() {
		// Given
		when(childCache.get(category.getUidPk())).thenReturn(CacheResult.notPresent());
		when(fallbackReader.findChildren(category)).thenReturn(
				Collections.<Category>singletonList(category2));

		// When
		List<Category> found = cachingCategoryLookup.findChildren(category);

		// Then
		assertEquals("Cached child categories should have been returned",
				Collections.singletonList(category2), found);
		// Verify that the list of child category ids are cached
		verify(childCache).put(category.getUidPk(), Collections.singletonList(category2.getUidPk()));
	}

	@Test
	public void testFindChildrenCachesEmptyChildIdListsForLeafNodes() {
		// Given
		when(childCache.get(category2.getUidPk())).thenReturn(CacheResult.notPresent());
		when(fallbackReader.findChildren(category2)).thenReturn(
				Collections.<Category>emptyList());

		// When
		List<Category> found = cachingCategoryLookup.findChildren(category2);

		// Then
		assertEquals("Category2 has no children", Collections.emptyList(), found);
		// Verify that the (empty) list of child category ids was cached
		verify(childCache).put(category2.getUidPk(), Collections.<Long>emptyList());
	}

	@Test
	public void testCategoriesByUidpkLoaderDelegatesToFallbackForSingleIds() {
		// Given
		when(fallbackReader.findByUid(category.getUidPk())).thenReturn(category);
		when(categoryUidByGuidCache.get(eq(category.getGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByCompoundGuidCache.get(eq(category.getCompoundGuid()))).thenReturn(CacheResult.notPresent());

		// When
		CacheLoader<Long, Category> loader = cachingCategoryLookup.getCategoriesByUidLoader();
		Category found = loader.load(category.getUidPk());

		// Then
		assertSame("uidpk Loader should delegate to fallback", category, found);
	}

	@Test
	public void testCategoriesByUidpkLoaderDelegatesToFallbackForMultipleIds() {
		// Given
		when(fallbackReader.findByUids(Arrays.asList(category.getUidPk(), category2.getUidPk())))
				.thenReturn(Arrays.<Category>asList(category, category2));
		when(categoryUidByGuidCache.get(eq(category.getGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByGuidCache.get(eq(category2.getGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByCompoundGuidCache.get(eq(category.getCompoundGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByCompoundGuidCache.get(eq(category2.getCompoundGuid()))).thenReturn(CacheResult.notPresent());

		// When
		CacheLoader<Long, Category> loader = cachingCategoryLookup.getCategoriesByUidLoader();
		Map<Long, Category> found = loader.loadAll(Arrays.asList(category.getUidPk(), category2.getUidPk()));

		// Then
		assertEquals("uidpk Loader should delegate to fallback",
				ImmutableMap.of(
						category.getUidPk(), category,
						category2.getUidPk(), category2),
				found);
	}

	@Test
	public void testCategoriesByGuidLoaderDelegatesToFallbackForSingleIds() {
		// Given
		when(fallbackReader.findByGuid(category.getGuid())).thenReturn(category);
		when(categoryByUidCache.get(category.getUidPk()))
			.thenReturn(CacheResult.notPresent())
			.thenReturn(CacheResult.create(category));
		when(categoryUidByGuidCache.get(eq(category.getGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByCompoundGuidCache.get(eq(category.getCompoundGuid()))).thenReturn(CacheResult.notPresent());

		// When
		CacheLoader<String, Long> loader = cachingCategoryLookup.getCategoryUidByGuidLoader();
		Long categoryUidPk = loader.load(category.getGuid());
		CacheResult<Category> found = categoryByUidCache.get(categoryUidPk);
		// Then
		assertSame("guid Loader should delegate to fallback", category, found.get());

		verify(categoryByUidCache).put(categoryUidPk, category);
		verify(categoryUidByCompoundGuidCache).put(category.getCompoundGuid(), categoryUidPk);
	}

	@Test
	public void testCategoriesByCodeLoaderDelegatesToFallbackForSingleIds() {
		// Given
		when(fallbackReader.findByCompoundCategoryAndCatalogCodes(category.getCompoundGuid())).thenReturn(category);
		when(categoryByUidCache.get(category.getUidPk()))
			.thenReturn(CacheResult.notPresent())
			.thenReturn(CacheResult.create(category));
		when(categoryUidByGuidCache.get(eq(category.getGuid()))).thenReturn(CacheResult.notPresent());
		when(categoryUidByCompoundGuidCache.get(eq(category.getCompoundGuid()))).thenReturn(CacheResult.notPresent());

		// When
		CacheLoader<String, Long> loader = cachingCategoryLookup.getCategoryUidByCompoundGuidLoader();
		Long categoryUidPk = loader.load(category.getCompoundGuid());
		CacheResult<Category> found = categoryByUidCache.get(categoryUidPk);

		// Then
		assertSame("guid Loader should delegate to fallback", category, found.get());

		verify(categoryByUidCache).put(categoryUidPk, category);
		verify(categoryUidByGuidCache).put(category.getGuid(), categoryUidPk);
	}

	//the following tests verify caching robustness for situations where valid input must be provided, but the invalid one is provided instead
	@Test
	public void testFindByGuidReturnsNullOnCacheMiss() {
		// Given
		when(categoryUidByGuidCache.get(eq(category.getGuid()), any(Function.class))).thenReturn(CATEGORY_UID);
		when(categoryByUidCache.get(CATEGORY_UID)).thenReturn(CacheResult.create(null));
		// When
		Category found = cachingCategoryLookup.findByGuid(category.getGuid());

		assertNull(NULL_CATEGORY_MSG, found);
	}

	@Test
	public void testFindByCategoryCodeAndCatalogReturnsNullOnCacheMiss() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getGuid() + "|" + catalog.getGuid()), any(Function.class)))
			.thenReturn(CATEGORY_UID);
		when(categoryByUidCache.get(CATEGORY_UID)).thenReturn(CacheResult.create(null));

		// When
		Category found = cachingCategoryLookup.findByCategoryCodeAndCatalog(category.getGuid(), catalog);

		assertNull(NULL_CATEGORY_MSG, found);
	}

	@Test
	public void testFindByCategoryAndCatalogCodeReturnsNullOnCacheMiss() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getGuid() + "|" + catalog.getGuid()), any(Function.class)))
			.thenReturn(CATEGORY_UID);
		when(categoryByUidCache.get(CATEGORY_UID)).thenReturn(CacheResult.create(null));

		// When
		Category found = cachingCategoryLookup.findByCategoryAndCatalogCode(category.getGuid(), catalog.getCode());

		assertNull(NULL_CATEGORY_MSG, found);
	}

	@Test
	public void testFindByCompoundCategoryAndCatalogCodesReturnsNullOnCacheMiss() {
		// Given
		when(categoryUidByCompoundGuidCache.get(eq(category.getCode() + "|" + catalog.getCode()), any(Function.class)))
			.thenReturn(CATEGORY_UID);
		when(categoryByUidCache.get(CATEGORY_UID)).thenReturn(CacheResult.create(null));

		// When
		Category found = cachingCategoryLookup.findByCompoundCategoryAndCatalogCodes(category.getCode() + "|" + catalog.getCode());

		assertNull(NULL_CATEGORY_MSG, found);
	}
}

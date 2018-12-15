/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.catalogview.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalogview.EpCatalogViewRequestBindException;
import com.elasticpath.domain.catalogview.search.SearchRequest;
import com.elasticpath.domain.catalogview.search.impl.SearchRequestImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalogview.StoreConfig;
import com.elasticpath.service.search.query.CategorySearchCriteria;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;

/**
 * Test cases for {@link SearchCriteriaFactoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchCriteriaFactoryImplTest {

	private static final String STORE_CODE = "store_code";

	private static final String ANCESTOR_CATEGORY_CODE = "1234";

	private static final String KEYWORDS = "sony";

	private static final String CATALOG_CODE = "CATALOG_CODE";

	private SearchCriteriaFactoryImpl searchCriteriaFactory;

	@Mock
	private CategoryService mockCategoryService;

	@Mock
	private StoreConfig mockStoreConfig;

	@Mock
	private CatalogService mockCatalogService;

	@Mock
	private BeanFactory beanFactory;

	private static final Locale LOCALE = Locale.CANADA;

	private static final Currency CURRENCY = Currency.getInstance(LOCALE);

	private static final long CATEGORY_UID = 123L;

	/**
	 * The setup steps.
	 */
	@Before
	public void setUp() {
		searchCriteriaFactory = new SearchCriteriaFactoryImpl();

		searchCriteriaFactory.setCatalogService(mockCatalogService);
		searchCriteriaFactory.setCategoryService(mockCategoryService);
		searchCriteriaFactory.setStoreConfig(mockStoreConfig);
		searchCriteriaFactory.setBeanFactory(beanFactory);
		final Store store = new StoreImpl();
		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		store.setCatalog(catalog);
		when(mockStoreConfig.getStore()).thenReturn(store);

		when(mockStoreConfig.getStoreCode()).thenReturn(STORE_CODE);

	}

	/**
	 * Tests population of search criteria from a search request with np category uid by <code>createCategorySearchCriteria()</code>.
	 */
	@Test
	public void testCreateCategorySearchCriteriaNoCategoryUid() {

		SearchRequest searchRequest = setUpSearchRequest();

		when(beanFactory.getBean(ContextIdNames.CATEGORY_SEARCH_CRITERIA)).thenReturn(new CategorySearchCriteria());

		CategorySearchCriteria categorySearchCriteria = searchCriteriaFactory.createCategorySearchCriteria(searchRequest);

		examineSearchCriteria(categorySearchCriteria);

		assertThat(categorySearchCriteria.getAncestorCode())
			.as("The ancestor category code in the search criteria should be null")
			.isNull();
	}

	/**
	 * Tests population of search criteria from a search request with category uid by <code>createCategorySearchCriteria()</code>.
	 */
	@Test
	public void testCreateCategorySearchCriteriaWithCategoryUid() {

		SearchRequest searchRequest = setUpSearchRequest();
		searchRequest.setCategoryUid(CATEGORY_UID);

		when(mockCategoryService.findCodeByUid(CATEGORY_UID)).thenReturn(ANCESTOR_CATEGORY_CODE);
		when(beanFactory.getBean(ContextIdNames.CATEGORY_SEARCH_CRITERIA)).thenReturn(new CategorySearchCriteria());

		CategorySearchCriteria categorySearchCriteria = searchCriteriaFactory.createCategorySearchCriteria(searchRequest);

		examineSearchCriteria(categorySearchCriteria);
		verify(mockCategoryService).findCodeByUid(CATEGORY_UID);
		verify(beanFactory).getBean(ContextIdNames.CATEGORY_SEARCH_CRITERIA);
		assertThat(categorySearchCriteria.getAncestorCode())
			.as("The ancestor category code in the search criteria should be equal to the category code ")
			.isEqualTo(ANCESTOR_CATEGORY_CODE);
	}

	/**
	 * Test for <code>createProductSearchCriteria</code> to make sure the object is created.
	 */
	@Test
	public void testCreateProductSearchCriteria() {

		SearchRequest searchRequest = setUpSearchRequest();

		when(beanFactory.getBean(ContextIdNames.PRODUCT_SEARCH_CRITERIA)).thenReturn(new ProductSearchCriteria());

		assertThat(searchCriteriaFactory.createProductSearchCriteria(searchRequest))
			.as("The product search criteria object should not be null")
			.isNotNull();
		verify(beanFactory).getBean(ContextIdNames.PRODUCT_SEARCH_CRITERIA);
	}
	
	/**
	 * Test for <code>createKeywordProductCategorySearchCriteria</code> to make sure the object is created.
	 */
	@Test
	public void testCreateKeywordProductCategorySearchCriteria() {
		SearchRequest searchRequest = setUpSearchRequest();

		when(beanFactory.getBean(ContextIdNames.KEYWORD_SEARCH_CRITERIA)).thenReturn(new KeywordSearchCriteria());


		KeywordSearchCriteria keywordSearchCriteria = searchCriteriaFactory.createKeywordProductCategorySearchCriteria(searchRequest);
		assertThat(keywordSearchCriteria.getCatalogCode())
			.as("The catalog code for search criteria should be the same as the store catalog code.")
			.isEqualTo(CATALOG_CODE);
		assertThat(keywordSearchCriteria.getStoreCode())
			.as("The search criteria should contain the store code from the search request.")
			.isEqualTo(STORE_CODE);
		assertThat(keywordSearchCriteria.getKeyword())
			.as("The search criteria should have the same keywords as the search request.")
			.isEqualTo(KEYWORDS);
		assertThat(keywordSearchCriteria.isFuzzySearchDisabled())
			.as("The search criteria should have isFuzzySearchEnabled set to false.")
			.isFalse();
		verify(beanFactory).getBean(ContextIdNames.KEYWORD_SEARCH_CRITERIA);

	}

	private void examineSearchCriteria(final CategorySearchCriteria categorySearchCriteria) {
		assertThat(categorySearchCriteria.getCategoryName())
			.as("The category name in the search criteria should contain the key words from the search request.")
			.isEqualTo(KEYWORDS);
		assertThat(categorySearchCriteria.getLocale())
			.as("The search criteria should contain the locale from the search request.")
			.isEqualTo(LOCALE);
		assertThat(categorySearchCriteria.isCategoryNameExact())
			.as("The search criteria should have categoryNameExact set to true.")
			.isTrue();
		assertThat(categorySearchCriteria.isDisplayableOnly())
			.as("The search criteria should have displayableOnly set to true.")
			.isTrue();
		assertThat(categorySearchCriteria.getCatalogCodes())
			.as("The collection of catalog codes in the search criteria should not be null.")
			.isNotNull();
		assertThat(categorySearchCriteria.getCatalogCodes().contains(CATALOG_CODE))
			.as("The collection of catalog codes in the search criteria should contain the catalogCode.")
			.isTrue();
	}

	private SearchRequest setUpSearchRequest() {
		SearchRequest searchRequest = new SearchRequestImpl() {
			private static final long serialVersionUID = -5966026798431773853L;

			/**
			 * overrides the setFilterIdStr to remove the extra logic not required for the purpose of this tests.
			 * 
			 * @param filtersIdStr the filter id str
			 * @param store the store
			 * @throws EpCatalogViewRequestBindException not being used
			 */
			@Override
			public void setFiltersIdStr(final String filtersIdStr, final Store store) throws EpCatalogViewRequestBindException {
				// does nothing
			}
		};
		searchRequest.setCurrency(CURRENCY);
		searchRequest.setFiltersIdStr("filter", null);
		searchRequest.setLocale(LOCALE);
		searchRequest.setKeyWords(KEYWORDS);

		return searchRequest;
	}

}

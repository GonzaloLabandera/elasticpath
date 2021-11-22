/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalogview.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.TopSeller;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.TopSellerImpl;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CatalogViewResult;
import com.elasticpath.domain.catalogview.CatalogViewResultHistory;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.FeaturedProductFilter;
import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.catalogview.FilterOption;
import com.elasticpath.domain.catalogview.FilterOptionCompareToComparator;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.browsing.BrowsingRequest;
import com.elasticpath.domain.catalogview.browsing.BrowsingResult;
import com.elasticpath.domain.catalogview.browsing.impl.BrowsingResultImpl;
import com.elasticpath.domain.catalogview.impl.FeaturedProductFilterImpl;
import com.elasticpath.domain.catalogview.impl.FilterOptionCompareToComparatorImpl;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.FilterBucketComparator;
import com.elasticpath.domain.misc.impl.FilterBucketComparatorImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.TopSellerService;
import com.elasticpath.service.catalogview.FilterFactory;
import com.elasticpath.service.catalogview.StoreConfig;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.search.index.IndexSearchResult;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;
import com.elasticpath.service.search.solr.IndexUtilityImpl;
import com.elasticpath.settings.SettingValueFactory;
import com.elasticpath.settings.SettingsService;
import com.elasticpath.settings.domain.SettingValue;
import com.elasticpath.settings.domain.impl.SettingDefinitionImpl;
import com.elasticpath.settings.impl.SettingValueFactoryWithDefinitionImpl;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.settings.test.support.SimpleSettingValueProvider;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.factory.TestShopperFactory;
import com.elasticpath.test.jmock.AbstractEPServiceTestCase;

/** Test cases for <code>CategoryServiceImpl</code>. */
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects" })
public class BrowsingServiceImplTest extends AbstractEPServiceTestCase {

	private static final int NUMBER_OF_ITEMS_PER_PAGE = 2;

	private BrowsingServiceImpl browsingServiceImpl;

	private CategoryLookup mockCategoryLookup;
	private CategoryService mockCategoryService;

	private StoreProductService mockStoreProductService;

	private TopSellerService mockTopSellerService;

	private IndexSearchService mockIndexSearchService;

	private ShoppingCart mockShoppingCart;

	private CustomerSession mockCustomerSession;

	private BeanFactoryExpectationsFactory bfef;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@SuppressWarnings("PMD.ExcessiveMethodLength")
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		bfef = getBeanFactoryExpectationsFactory();
		
		bfef.allowingBeanFactoryGetSingletonBean(ContextIdNames.FILTER_FACTORY, FilterFactory.class, new FilterFactoryImpl());
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.BROWSING_RESULT, CatalogViewResult.class, BrowsingResultImpl.class);

		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.FEATURED_PRODUCT_FILTER, FeaturedProductFilter.class,
				FeaturedProductFilterImpl.class);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.FILTER_BUCKET_COMPARATOR, FilterBucketComparator.class,
				FilterBucketComparatorImpl.class);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.FILTER_OPTION_COMPARETO_COMPARATOR, FilterOptionCompareToComparator.class,
				FilterOptionCompareToComparatorImpl.class);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.PRODUCT_SEARCH_CRITERIA, ProductSearchCriteria.class, ProductSearchCriteria.class);

		
		browsingServiceImpl = new BrowsingServiceImpl();

		final Store mockedStore = getMockedStore();
		final String storeCode = mockedStore.getCode();

		final SettingValueProvider<Boolean> attributeFilterEnabledSettingValueProvider = new SimpleSettingValueProvider<>(storeCode, true);
		final SettingValueProvider<Integer> featuredProductCountSettingValueProvider = new SimpleSettingValueProvider<>(storeCode, 1);

		browsingServiceImpl.setAttributeFilterEnabledSettingValueProvider(attributeFilterEnabledSettingValueProvider);
		browsingServiceImpl.setFeaturedProductCountSettingValueProvider(featuredProductCountSettingValueProvider);

		mockCategoryLookup = getContext().mock(CategoryLookup.class);
		browsingServiceImpl.setCategoryLookup(mockCategoryLookup);
		mockCategoryService = getContext().mock(CategoryService.class);
		browsingServiceImpl.setCategoryService(mockCategoryService);
		mockTopSellerService = getContext().mock(TopSellerService.class);
		browsingServiceImpl.setTopSellerService(mockTopSellerService);

		mockStoreProductService = getContext().mock(StoreProductService.class);
		browsingServiceImpl.setStoreProductService(mockStoreProductService);

		mockIndexSearchService = getContext().mock(IndexSearchService.class);
		browsingServiceImpl.setIndexSearchService(mockIndexSearchService);
		browsingServiceImpl.setIndexUtility(new IndexUtilityImpl());

		mockCustomerSession = getContext().mock(CustomerSession.class);
		mockShoppingCart = getContext().mock(ShoppingCart.class);

		final Shopper shopper = TestShopperFactory.getInstance().createNewShopperWithMementoAndCustomerAndCustomerSessionAndShoppingCart(null,
				mockCustomerSession, mockShoppingCart);

		SettingsService mockSettingsService = getContext().mock(SettingsService.class);
		getContext().checking(new Expectations() {
			{
				allowing(mockShoppingCart).getStore();
				will(returnValue(mockedStore));

				allowing(mockShoppingCart).getShopper();
				will(returnValue(shopper));
			}
		});
		//Create a setting value that will be returned when the mockStoreConfig's
		// "getSetting" method is called
		final StoreConfig mockStoreConfig = getContext().mock(StoreConfig.class);
		final SettingValueFactory svf = new SettingValueFactoryWithDefinitionImpl();
		final SettingValue value = svf.createSettingValue(new SettingDefinitionImpl());
		value.setValue("1");
		getContext().checking(new Expectations() {
			{
				allowing(mockStoreConfig).getSettingValue(attributeFilterEnabledSettingValueProvider);
				will(returnValue(attributeFilterEnabledSettingValueProvider.get(storeCode)));

				allowing(mockStoreConfig).getSettingValue(featuredProductCountSettingValueProvider);
				will(returnValue(featuredProductCountSettingValueProvider.get(storeCode)));

				allowing(mockStoreConfig).getStore();
				will(returnValue(mockedStore));

				allowing(mockStoreConfig).getStoreCode();
				will(returnValue(storeCode));
			}
		});
		browsingServiceImpl.setStoreConfig(mockStoreConfig);
		browsingServiceImpl.setSettingsService(mockSettingsService);
		browsingServiceImpl.setPaginationService(new PaginationServiceImpl());
		browsingServiceImpl.setBeanFactory(getBeanFactory());
	}

	/**
	 * Test browsing(BrowsingRequest, null).
	 */
	@Test
	public void testBrowsing() {

		final BrowsingRequest browsingRequest = getMockedBrowsingRequest();

		final Category category = getCategory();

		final TopSeller topSeller = new TopSellerImpl();
		getContext().checking(new Expectations() {
			{
				allowing(mockTopSellerService).findTopSellerByCategoryUid(with(any(long.class)));
				will(returnValue(topSeller));
			}
		});

		final List<Product> products = new ArrayList<>();
		final boolean loadProductAssociations = true;
		getContext().checking(new Expectations() {
			{
				allowing(mockStoreProductService).getProductsForStore(
						with(Collections.<Long>emptyList()), with(any(Store.class)), with(equal(loadProductAssociations)));
				will(returnValue(products));
			}
		});

		getMockedIndexSearchResult();

		final BrowsingResult browsingResult = browsingServiceImpl.browsing(browsingRequest, null, mockShoppingCart, loadProductAssociations, 0,
				NUMBER_OF_ITEMS_PER_PAGE);
		assertNotNull(browsingResult);
		assertEquals(category, browsingResult.getCategory());
		assertEquals(products, browsingResult.getProducts());
	}

	/**
	 * Test browsing(BrowsingRequest, BrowsingResultHistory).
	 */
	@Test
	public void testBrowsingWithBrowsingResultHistory() {
		
		getCategory();

		final boolean loadProductAssociations = true;
		final BrowsingRequest browsingRequest = getMockedBrowsingRequest();
		final CatalogViewResultHistory mockBrowsingResultHistory = getContext().mock(CatalogViewResultHistory.class);
		final CatalogViewResultHistory browsingResultHistory = mockBrowsingResultHistory;
		final BrowsingResult dummyBrowsingResult = new BrowsingResultImpl();
		getContext().checking(new Expectations() {
			{
				allowing(mockBrowsingResultHistory).addRequest(with(same(browsingRequest)));
				will(returnValue(dummyBrowsingResult));
			}
		});

		final TopSeller topSeller = new TopSellerImpl();
		getContext().checking(new Expectations() {
			{
				allowing(mockTopSellerService).findTopSellerByCategoryUid(with(any(long.class)));
				will(returnValue(topSeller));

				allowing(mockStoreProductService).getProductsForStore(
						with(Collections.<Long>emptyList()), with(any(Store.class)), with(equal(loadProductAssociations)));
				will(returnValue(new ArrayList<Product>()));
			}
		});

		getMockedIndexSearchResult();

		final BrowsingResult browsingResult = browsingServiceImpl.browsing(browsingRequest, browsingResultHistory, mockShoppingCart,
				loadProductAssociations, 0, NUMBER_OF_ITEMS_PER_PAGE);
		assertNotNull(browsingResult);
		assertNotNull(browsingResult.getProducts());
		assertSame(dummyBrowsingResult, browsingResult);
	}

	/**
	 * @return the default mocked store.
	 */
	@Override
	protected Store getMockedStore() {
		Store store = new StoreImpl();
		store.setCatalog(getCatalog());
		store.setCode("irrelevant store code");
		return store;
	}

	/**
	 * @return the master catalog singleton
	 */
	private Catalog getCatalog() {
		Catalog masterCatalog = new CatalogImpl();
		masterCatalog.setMaster(true);
		masterCatalog.setCode("irrelevant catalog code");
		return masterCatalog;
	}

	/**
	 * @return a new <code>Category</code> instance.
	 */
	private Category getCategory() {
		final Category category = new CategoryImpl();
		category.initialize();
		category.setCode((new RandomGuidImpl()).toString());
		
		getContext().checking(new Expectations() {
			{
				allowing(mockCategoryLookup).findByUid(with(any(long.class)));
				will(returnValue(category));
				allowing(mockCategoryLookup).findChildren(category);
				will(returnValue(Collections.emptyList()));
				allowing(mockCategoryService).getPath(category);
				will(returnValue(Collections.singletonList(category)));
			}
		});
		
		return category;
	}
	
	private void getMockedIndexSearchResult() {
		
		final IndexSearchResult mockIndexSearchResult = getContext().mock(IndexSearchResult.class);
		getContext().checking(new Expectations() {
			{
				allowing(mockIndexSearchResult).getNumFound();
				will(returnValue(0));

				allowing(mockIndexSearchResult).getLastNumFound();
				will(returnValue(0));

				allowing(mockIndexSearchResult).getCategoryFilterOptions();
				will(returnValue(new ArrayList<FilterOption<CategoryFilter>>()));

				allowing(mockIndexSearchResult).getBrandFilterOptions();
				will(returnValue(new ArrayList<FilterOption<BrandFilter>>()));

				allowing(mockIndexSearchResult).getPriceFilterOptions();
				will(returnValue(new ArrayList<FilterOption<PriceFilter>>()));

				allowing(mockIndexSearchResult).getAttributeValueFilterOptions();
				will(returnValue(new HashMap<Attribute, List<FilterOption<AttributeValueFilter>>>()));

				allowing(mockIndexSearchResult).getAttributeRangeFilterOptions();
				will(returnValue(new HashMap<Attribute, List<FilterOption<AttributeRangeFilter>>>()));

				allowing(mockIndexSearchResult).setRememberOptions(with(any(boolean.class)));

				atLeast(1).of(mockIndexSearchService).search(with(any(SearchCriteria.class)));
				will(returnValue(mockIndexSearchResult));

				atLeast(1).of(mockIndexSearchResult).getResults(with(any(int.class)), with(any(int.class)));
				will(returnValue(new ArrayList<Long>()));

				atLeast(1).of(mockIndexSearchResult).getAllResults();
				will(returnValue(new ArrayList<Long>()));

				atLeast(1).of(mockCustomerSession).getPriceListStack();
				will(returnValue(getContext().mock(PriceListStack.class)));
			}
		});
	}
	 
	private BrowsingRequest getMockedBrowsingRequest() {
		final BrowsingRequest mockBrowsingRequest = getContext().mock(BrowsingRequest.class);
		getContext().checking(new Expectations() {
			{
				allowing(mockBrowsingRequest).getCategoryUid();
				will(returnValue((long) 0));

				allowing(mockBrowsingRequest).getLocale();
				will(returnValue(Locale.US));

				allowing(mockBrowsingRequest).getCurrency();
				will(returnValue(Currency.getInstance(Locale.US)));

				allowing(mockBrowsingRequest).getFilters();
				will(returnValue(new ArrayList<Filter<?>>()));

				allowing(mockBrowsingRequest).getSortOrder();
				will(returnValue(SortOrder.DESCENDING));

				allowing(mockBrowsingRequest).getSortType();
				will(returnValue(StandardSortBy.RELEVANCE));
			}
		});
		return mockBrowsingRequest;
	}
	
	private JUnitRuleMockery getContext() {
		return context;
	}
}

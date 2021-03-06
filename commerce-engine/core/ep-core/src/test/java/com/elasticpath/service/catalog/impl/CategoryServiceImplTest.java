/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog.impl;

import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.DuplicateKeyException;
import com.elasticpath.commons.exception.IllegalOperationException;
import com.elasticpath.commons.util.CategoryGuidUtil;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryDeleted;
import com.elasticpath.domain.catalog.CategoryLoadTuner;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.AbstractCategoryImpl;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CatalogLocaleImpl;
import com.elasticpath.domain.catalog.impl.CategoryDeletedImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.LinkedCategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.misc.RandomGuid;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/** Test cases for <code>CategoryServiceImpl</code>. */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveClassLength",
		"PMD.TooManyStaticImports", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects" })
public class CategoryServiceImplTest {

	private static final String CATALOG_CODE = "catalog_code";

	private static final String CATEGORY_CODE = "category_code";

	private static final String CATEGORY_CODE_CATALOG_CODE = "category_code|catalog_code";

	private static final int FOUR = 4;

	private static final String CATEGORY_UID_SELECT_BY_CHILDREN_UIDS = "CATEGORY_UID_SELECT_BY_CHILDREN_UIDS";

	private static final String PLACEHOLDER_FOR_LIST = "list";

	private static final String EXISTING_CATEGORY_GUID = "12345";

	private static final String CATEGORY_LIST_SUBCATEGORY = "CATEGORY_LIST_SUBCATEGORY";

	private static final String ROOT_CODE = "root";

	private static final long UID_PKS = 1L;

	private static final String CHILD_CODE = "child";

	private static final String LINKED_CATEGORY_COUNT_BY_CODE_QUERY = "LINKED_CATEGORY_COUNT_BY_CODE";

	private static final String CATEGORY_GUID_COUNT_QUERY = "CATEGORY_GUID_COUNT";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private CategoryServiceImpl categoryService;
	private CategoryLookup categoryLookup;
	private Category category;
	private CategoryLoadTuner categoryLoadTuner;

	private BeanFactory beanFactory;

	private BeanFactoryExpectationsFactory beanFactoryExpectationsFactory;

	private ProductService productService;

	private Catalog catalog;

	private CatalogService catalogService;

	private IndexNotificationService indexNotificationService;

	private Catalog masterCatalog;

	private PersistenceEngine persistenceEngine;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@Before
	public void setUp() throws Exception {
		beanFactory = context.mock(BeanFactory.class);
		beanFactoryExpectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);

		beanFactoryExpectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.ABSTRACT_CATEGORY, AbstractCategoryImpl.class);
		beanFactoryExpectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CATEGORY, CategoryImpl.class);
		beanFactoryExpectationsFactory.allowingBeanFactoryGetBean(ContextIdNames.CATALOG_LOCALE, CatalogLocaleImpl.class);
		beanFactoryExpectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class,
				FetchGroupLoadTunerImpl.class);
		beanFactoryExpectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.LINKED_CATEGORY, Category.class, LinkedCategoryImpl.class);
		beanFactoryExpectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.RANDOM_GUID, RandomGuid.class, RandomGuidImpl.class);

		persistenceEngine = context.mock(PersistenceEngine.class);
		categoryLookup = context.mock(CategoryLookup.class);
		categoryLoadTuner = context.mock(CategoryLoadTuner.class);

		categoryService = new CategoryServiceImpl();
		categoryService.setBeanFactory(beanFactory);
		categoryService.setCategoryGuidUtil(new CategoryGuidUtil());
		categoryService.setPersistenceEngine(persistenceEngine);
		categoryService.setCategoryLookup(categoryLookup);
		categoryService.setCategoryLoadTunerDefaultPlusAttributes(categoryLoadTuner);

		productService = context.mock(ProductService.class);
		categoryService.setProductService(productService);

		catalog = context.mock(Catalog.class);
		context.checking(new Expectations() {
			{
				allowing(catalog).getUidPk(); will(returnValue(2L));
				allowing(beanFactory).getBeanImplClass(ContextIdNames.ABSTRACT_CATEGORY); will(returnValue(AbstractCategoryImpl.class));
			}
		});

		catalogService = context.mock(CatalogService.class);
		categoryService.setCatalogService(catalogService);

		indexNotificationService = context.mock(IndexNotificationService.class);

		beanFactoryExpectationsFactory.allowingBeanFactoryGetSingletonBean(ContextIdNames.INDEX_NOTIFICATION_SERVICE, IndexNotificationService.class,
				indexNotificationService);

		setupCategory();
	}

	@After
	public void tearDown() {
		beanFactoryExpectationsFactory.close();
	}

	private void setupCategory() {
		category = getCategory();
		category.setUidPk(UID_PKS);
	}

	/**
	 * Test the simple success case of adding a non-linked category with a code that
	 * doesn't already exist.
	 * Check that add() calls save() and then calls the product service to
	 * notify that the category was updated.
	 */
	@Test
	public void testAddNonLinkedSucceeds() {
		persistenceEngine = mock(PersistenceEngine.class);
		productService = mock(ProductService.class);
		indexNotificationService = mock(IndexNotificationService.class);
		beanFactory = mock(BeanFactory.class);

		category.setCode("testCode1");
		//expect that save() is called on the persistence engine
		doNothing().when(persistenceEngine).save(category);
		doNothing().when(productService).notifyCategoryUpdated(category);
		doNothing().when(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, category.getUidPk());

		//pretend that this master category doesn't already exist - the add should succeed
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected boolean masterCategoryExists(final String categoryCode) {
				return false;
			}
		};
		serviceImpl.setBeanFactory(beanFactory);
		serviceImpl.setPersistenceEngine(persistenceEngine);
		serviceImpl.setProductService(productService);
		serviceImpl.setBeanFactory(beanFactory);
		when(beanFactory.getSingletonBean(ContextIdNames.INDEX_NOTIFICATION_SERVICE, IndexNotificationService.class))
			.thenReturn(indexNotificationService);

		assertNotNull(serviceImpl.add(category));

		verify(persistenceEngine).save(category);
		verify(productService).notifyCategoryUpdated(category);
		verify(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, category.getUidPk());
	}

	/**
	 * Test that trying to add a non-linked category with a code that already exists fails.
	 */
	@Test(expected = DuplicateKeyException.class)
	public void testAddNonLinkedPreExistingFails() {
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected boolean masterCategoryExists(final String categoryCode) {
				return true;
			}
		};
		serviceImpl.setPersistenceEngine(persistenceEngine);
		serviceImpl.add(category);
	}

	/**
	 * Test that adding a linked category with a code that already exists in the DB will fail.
	 */
	@Test(expected = DuplicateKeyException.class)
	public void testAddLinkedPreExistingFails() {
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected boolean linkedCategoryExists(final String categoryCode, final String catalogCode) {
				return true;
			}
		};
		serviceImpl.setPersistenceEngine(persistenceEngine);
		final Category linkedCategory = mock(Category.class);
		when(linkedCategory.isLinked()).thenReturn(true);
		when(linkedCategory.getCatalog()).thenReturn(mock(Catalog.class));

		serviceImpl.add(linkedCategory);

		verify(linkedCategory).isLinked();
		verify(linkedCategory).getCatalog();
	}

	/**
	 * Test that masterCategoryExists() will call the CATEGORY_COUNT_BY_CODE named query and will
	 * return true if the returned list's first element (the count) is > 1.
	 */
	@Test
	public void testMasterCategoryExists() {
		final String testCode = "myCode";
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl();
		serviceImpl.setPersistenceEngine(persistenceEngine);
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_COUNT_BY_CODE", testCode);
				will(returnValue(Arrays.asList(1L)));
			}
		});
		assertTrue("Count returns 1, so the category exists", serviceImpl.masterCategoryExists(testCode));

		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_COUNT_BY_CODE", testCode);
				will(returnValue(Arrays.asList(0L)));
			}
		});
		assertFalse("Count returns 0, so the category doesn't exist", serviceImpl.masterCategoryExists(testCode));
	}

	/**
	 * Test that linkedCategoryExists() will call the LINKED_CATEGORY_COUNT_BY_CODE named query and will
	 * return true if the returned lists's first element (the count) is > 1.
	 */
	@Test
	public void testLinkedCategoryExists() {
		persistenceEngine = mock(PersistenceEngine.class);
		final String testCode = "myCode";
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl();
		serviceImpl.setPersistenceEngine(persistenceEngine);
		//Test true
		when(persistenceEngine.retrieveByNamedQuery(LINKED_CATEGORY_COUNT_BY_CODE_QUERY, testCode, null)).thenReturn(Arrays.asList(1L));
		assertTrue(serviceImpl.linkedCategoryExists(testCode, null));
		verify(persistenceEngine).retrieveByNamedQuery(LINKED_CATEGORY_COUNT_BY_CODE_QUERY, testCode, null);

		//Test false
		when(persistenceEngine.retrieveByNamedQuery(LINKED_CATEGORY_COUNT_BY_CODE_QUERY, testCode, null)).thenReturn(Arrays.asList(0L));

		assertFalse(serviceImpl.linkedCategoryExists(testCode, null));
		verify(persistenceEngine, times(2)).retrieveByNamedQuery(LINKED_CATEGORY_COUNT_BY_CODE_QUERY, testCode, null);
	}

	@Test
	public void testIsGuidInUse() {
		final PersistenceEngine persistenceEngine = mock(PersistenceEngine.class);
		final String testGuid = "myGuid";
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl();
		serviceImpl.setPersistenceEngine(persistenceEngine);

		when(persistenceEngine.retrieveByNamedQuery(CATEGORY_GUID_COUNT_QUERY, testGuid)).thenReturn(Arrays.asList(1L));
		assertTrue("the guid exists", serviceImpl.isGuidInUse(testGuid));
		verify(persistenceEngine).retrieveByNamedQuery(CATEGORY_GUID_COUNT_QUERY, testGuid);


		when(persistenceEngine.retrieveByNamedQuery(CATEGORY_GUID_COUNT_QUERY, testGuid)).thenReturn(Arrays.asList(0L));
		assertFalse("the category guid doesn't exist", serviceImpl.isGuidInUse(testGuid));
		verify(persistenceEngine, times(2)).retrieveByNamedQuery(CATEGORY_GUID_COUNT_QUERY, testGuid);
	}

	/**
	 * Test that update() will call persistenceEngine.update() and will also call ProductService with the
	 * updated category to notify it that the category was updated.
	 */
	@Test
	public void testUpdate() {
		final Category updatedCategory = new CategoryImpl();
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).update(category); will(returnValue(updatedCategory));
				oneOf(productService).notifyCategoryUpdated(updatedCategory);
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, updatedCategory.getUidPk());
			}
		});
		assertEquals("update must return the object retrieved from the persistence engine on update",
				updatedCategory, categoryService.update(category));
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.listRootCategories()'.
	 */
	@Test
	public void testListRootCategories() {
		persistenceEngine = mock(PersistenceEngine.class);
		beanFactory = mock(BeanFactory.class);
		categoryService.setBeanFactory(beanFactory);
		categoryService.setPersistenceEngine(persistenceEngine);
		final List<Category> categories = new ArrayList<>();

		// expectations
		when(persistenceEngine.withLoadTuners(any(LoadTuner.class))).thenReturn(persistenceEngine);
		when(persistenceEngine.retrieveByNamedQuery("CATEGORY_LIST_ROOT", 2L)).thenReturn(Collections.emptyList());
		when(beanFactory.getPrototypeBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class))
				.thenReturn(mock(FetchGroupLoadTuner.class));

		assertEquals(categories, categoryService.listRootCategories(catalog, false));
		verify(persistenceEngine).retrieveByNamedQuery("CATEGORY_LIST_ROOT", 2L);
		verify(persistenceEngine).withLoadTuners(any(LoadTuner.class));
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.listRootCategories()'.
	 */
	@Test
	public void testListAvailableRootCategories() {
		final List<Category> categories = new ArrayList<>();

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).withLoadTuners(with(any(LoadTuner[].class))); will(returnValue(persistenceEngine));
				oneOf(persistenceEngine).retrieveByNamedQuery(with("CATEGORY_LIST_AVAILABLE_ROOT"), with(any(Object[].class)));
				will(returnValue(categories));
				oneOf(persistenceEngine).retrieveByNamedQuery(with("LINKED_CATEGORY_LIST_AVAILABLE_ROOT"), with(any(Object[].class)));
				will(returnValue(categories));
			}
		});

		assertEquals(categories, categoryService.listRootCategories(catalog, true));
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.saveOrUpdate(Category)'.
	 */
	@Test
	public void testSaveOrUpdate() {
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).saveOrMerge(category); will(returnValue(category));
				oneOf(productService).notifyCategoryUpdated(category);
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, category.getUidPk());
			}
		});
		categoryService.saveOrUpdate(category);
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.isProductInCategory(long, long)'.
	 */
	@Test
	public void testIsProductInCategory() {
		final long productUid = UID_PKS;
		final long categoryUid = 2L;
		persistenceEngine = mock(PersistenceEngine.class);
		categoryService.setPersistenceEngine(persistenceEngine);

		// expectations
		when(persistenceEngine.retrieveByNamedQuery("SELECT_PRODUCT_CATEGORY_ASSOCIATION", productUid, categoryUid))
				.thenReturn(Collections.emptyList());
		assertFalse(categoryService.isProductInCategory(productUid, categoryUid));

		when(persistenceEngine.retrieveByNamedQuery("SELECT_PRODUCT_CATEGORY_ASSOCIATION", productUid, categoryUid))
				.thenReturn(Collections.singletonList(2L));
		assertTrue(categoryService.isProductInCategory(productUid, categoryUid));
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.findCategoryByCriterias(MAP)'.
	 */
	@Test
	public void testFindCategoryByCriterias() {
		/*
		 * Map messageMap = new HashMap(); messageMap.put("searchFilter", "name"); messageMap.put("matchType", "MatchContain");
		 * messageMap.put(ACTIVE_STRING, TRUE_STRING); messageMap.put(INACTIVE_STRING, TRUE_STRING); messageMap.put(SEARCH_VALUE, "watch"); ArrayList
		 * argList = new ArrayList(); argList.add("%" + messageMap.get("searchValue").toString().toUpperCase() + "%");
		 * argList.add(getElasticPath().getDefaultLocale());
		 * getMockPersistenceEngine().expects(once()).method(RETRIEVE).with(COMPOSED_CATEGORY_SEARCH_QTY, argList.toArray(), 0,
		 * eq(getElasticPath().getMaxCmSearchResultCount())).will(returnValue(new ArrayList())); categoryService.findCategoryByCriteria(messageMap);
		 */
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.removeCategoryTree(long)'.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testRemoveCategoryTree() {
		beanFactoryExpectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.CATEGORY_DELETED, CategoryDeleted.class,
				CategoryDeletedImpl.class);

		// Compose a category branch
		final Category subCategory = getCategory();
		subCategory.setParent(category);

		final int categoryNodesInTheBranch = 2;

		// expectations
		context.checking(new Expectations() {
			{
				allowing(persistenceEngine).withLoadTuners(categoryLoadTuner);
				will(returnValue(persistenceEngine));

				// load the top category once
				oneOf(categoryLookup).findByUid(category.getUidPk());
				will(returnValue(category));

				allowing(categoryLookup).findChildren(category);
				will(returnValue(Collections.singletonList(subCategory)));
				allowing(categoryLookup).findChildren(subCategory);
				will(returnValue(Collections.emptyList()));

				// delete the category
				exactly(categoryNodesInTheBranch).of(persistenceEngine).delete(with(any(AbstractCategoryImpl.class)));

				// save the category deleted record
				exactly(categoryNodesInTheBranch).of(persistenceEngine).save(with(any(CategoryDeletedImpl.class)));

				oneOf(persistenceEngine).retrieveByNamedQuery(with("LINKED_CATEGORY_SELECT_BY_MASTER_CATEGORY_UID"), with(any(Object[].class)));
				will(returnValue(new ArrayList<Category>()));

				oneOf(persistenceEngine).evictObjectFromCache(with(any(AbstractCategoryImpl.class)));
				oneOf(productService).hasProductsInCategory(category.getUidPk());
				will(returnValue(false));
				oneOf(productService).hasProductsInCategory(subCategory.getUidPk());
				will(returnValue(false));
			}
		});

		categoryService.removeCategoryTree(category.getUidPk());
	}

	/**
	 * Ensure that order is preserved when updating {@link Category}s which has the same ordering as another when they
	 * are not a root category.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateOrderingNonRootSameOrdering() {
		final long category1Uid = 1244;
		final long category2Uid = 55125;
		final int sameOrdering = 5515;

		final Catalog catalog = mock(Catalog.class);
		final Category category1 = new CategoryImpl();
		final Category category2 = new CategoryImpl();
		category1.setUidPk(category1Uid);
		category1.setOrdering(sameOrdering);
		category1.setCatalog(catalog);
		category1.setGuid("a");
		category2.setUidPk(category2Uid);
		category2.setOrdering(sameOrdering);
		category2.setCatalog(catalog);
		category2.setGuid("b");

		final Category parent = mock(Category.class, "category-2");

		final BeanFactory beanFactory = mock(BeanFactory.class);
		final CategoryLookup categoryLookup = mock(CategoryLookup.class);
		final PersistenceEngine persistenceEngine = mock(PersistenceEngine.class);
		final ProductService productService = mock(ProductService.class);
		final IndexNotificationService indexNotificationService = mock(IndexNotificationService.class);

		categoryService = new CategoryServiceImpl();
		categoryService.setCategoryLookup(categoryLookup);
		categoryService.setBeanFactory(beanFactory);
		categoryService.setPersistenceEngine(persistenceEngine);
		categoryService.setProductService(productService);

		when(beanFactory.getBeanImplClass(eq(ContextIdNames.ABSTRACT_CATEGORY))).thenReturn(null);
		when(catalog.getUidPk()).thenReturn(1L);
		when(categoryLookup.findParent(category1)).thenReturn(parent);
		when(categoryLookup.findChildren(parent)).thenReturn(Arrays.asList(category1, category2));

		when(persistenceEngine.load(null, category2Uid)).thenReturn(category2);
		when(persistenceEngine.saveOrMerge(category1)).thenReturn(category1);
		when(persistenceEngine.saveOrMerge(category2)).thenReturn(category2);
		when(beanFactory.getSingletonBean(ContextIdNames.INDEX_NOTIFICATION_SERVICE, IndexNotificationService.class))
			.thenReturn(indexNotificationService);
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

		// must use compareTo because we know ordering is the same before swapping
		assertTrue("Expectations were setup incorrectly", category1.compareTo(category2) < 0);
		categoryService.updateOrder(category1, category2Uid);
		assertTrue("Ordering was not swapped", category1.getOrdering() > category2.getOrdering());
	}

	/**
	 * Test method for 'com.elasticpath.service.CategoryServiceImpl.updateOrder(long, long)'. Tests the case where reordering a couple of root
	 * categories that haven't been ordered before.
	 */
	@Test
	public void testUpdateOrder() {
		final long oneUid = UID_PKS;
		final long twoUid = 2L;

		// these categories don't have a parent, so must be root categories
		final Category catOne = new CategoryImpl();
		catOne.setUidPk(oneUid);
		catOne.setGuid(String.valueOf(oneUid));
		catOne.setCatalog(catalog);

		final Category catTwo = new CategoryImpl();
		catTwo.setUidPk(twoUid);
		catTwo.setGuid(String.valueOf(twoUid));
		catTwo.setCatalog(catalog);

		// override category service
		CategoryServiceImpl categoryService = new CategoryServiceImpl() {
			// need to override this so that root categories get populated correctly.
			@Override
			public List<Category> listRootCategories(final Catalog catalog, final boolean availableOnly) {
				final List<Category> children = new ArrayList<>();
				children.add(catOne);
				children.add(catTwo);
				return children;
			}
		};
		categoryService.setBeanFactory(beanFactory);
		categoryService.setCategoryLookup(categoryLookup);
		categoryService.setPersistenceEngine(persistenceEngine);
		categoryService.setProductService(productService);

		context.checking(new Expectations() {
			{
				exactly(FOUR).of(indexNotificationService).addNotificationForEntityIndexUpdate(with(IndexType.CATEGORY), with(any(Long.class)));
				allowing(productService).notifyCategoryUpdated(with(any(AbstractCategoryImpl.class)));
				oneOf(persistenceEngine).withLoadTuners(with(any(LoadTuner[].class))); will(returnValue(persistenceEngine));
				oneOf(persistenceEngine).load(with(AbstractCategoryImpl.class), with(any(Long.class)));
				will(returnValue(catTwo));
				allowing(categoryLookup).findParent(catOne); will(returnValue(null));
				allowing(categoryLookup).findParent(catTwo); will(returnValue(null));
				atLeast(1).of(persistenceEngine).saveOrMerge(catOne);
				will(returnValue(catOne));
				atLeast(1).of(persistenceEngine).saveOrMerge(catTwo);
				will(returnValue(catTwo));
			}
		});
		categoryService.updateOrder(catOne, 2);
	}

	/**
	 * Test method for
	 * {@link CategoryServiceImpl#updateOrder(Category, long) where categories are in different catalogs.
	 */
	@Test(expected = Exception.class)
	public void testUpdateOrderInDifferentCatalog() {
		final long oneUid = UID_PKS;
		final long twoUid = 2L;

		final Catalog catalog1 = context.mock(Catalog.class, "catalog1");
		final Catalog catalog2 = context.mock(Catalog.class, "catalog2");
		context.checking(new Expectations() {
			{
				allowing(catalog1).getUidPk(); will(returnValue(UID_PKS));
				allowing(catalog2).getUidPk(); will(returnValue(2L));
			}
		});

		// these categories don't have a parent, so must be root categories
		final Category catOne = new CategoryImpl();
		catOne.setUidPk(oneUid);
		catOne.setGuid(String.valueOf(oneUid));
		catOne.setCatalog(catalog1);

		final Category catTwo = new CategoryImpl();
		catTwo.setUidPk(twoUid);
		catTwo.setGuid(String.valueOf(twoUid));
		catTwo.setCatalog(catalog2);

		// override category service
		CategoryServiceImpl categoryService = new CategoryServiceImpl() {
			// need to override this so that root categories get populated correctly.
			@Override
			public List<Category> listRootCategories(final Catalog catalog, final boolean availableOnly) {
				final List<Category> children = new ArrayList<>();
				children.add(catOne);
				children.add(catTwo);
				return children;
			}
		};
		categoryService.setBeanFactory(beanFactory);
		categoryService.setPersistenceEngine(persistenceEngine);
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).withLoadTuners(with(any(LoadTuner[].class))); will(returnValue(persistenceEngine));
				oneOf(persistenceEngine).load(AbstractCategoryImpl.class, twoUid); will(returnValue(catTwo));
			}
		});

		categoryService.updateOrder(catOne, 2);
	}

	/**
	 * Tests that findCodeByUid() returns an empty string when no codes are returned by the retrieval query.
	 */
	@Test
	public void testFindCodeByUidNoCodesReturned() {
		final List<String> codes = new ArrayList<>();
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CODE_SELECT_BY_UID", 2L);
				will(returnValue(codes));
				oneOf(persistenceEngine).retrieveByNamedQuery("LINKED_CATEGORY_CODE_SELECT_BY_UID", 2L);
				will(returnValue(codes));
			}
		});
		final String result = categoryService.findCodeByUid(2L);
		assertEquals("The result should contain the code returned by the query", "", result);
	}

	/**
	 * Tests that findCodeByUid() returns the code returned by the retrieval query.
	 */
	@Test
	public void testFindCodeByUidOneCodeReturned() {
		final List<String> codes = new ArrayList<>();
		codes.add("CODE");
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CODE_SELECT_BY_UID", 2L);
				will(returnValue(codes));
			}
		});
		final String result = categoryService.findCodeByUid(2L);
		assertEquals("The result should contain the code returned by the query", codes.get(0), result);
	}

	/**
	 * Tests that findAncestorCategoryUidsByCategoryUid() returns an empty Set
	 * when the category has no parents.
	 *
	 */
	@Test
	public void testFindAncestorCategoryUidsByCategoryUidNoParents() {
		final List<Long> categoryUids = new ArrayList<>();
		final long categoryUid = 123L;
		categoryUids.add(new Long(categoryUid));

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, categoryUids);
				will(returnValue(Collections.emptyList()));
			}
		});

		final Set<Long> result = categoryService.findAncestorCategoryUidsByCategoryUid(categoryUid);
		assertEquals("No parent category uids should be returned.", 0, result.size());
	}

	/**
	 * Tests that findAncestorCategoryUidsByCategoryUid() returns the uid of a category's parent.
	 * In this case only one parent was setup.
	 */
	@Test
	public void testFindAncestorCategoryUidsByCategoryUid() {
		final List<Long> categoryUids = new ArrayList<>();
		final long categoryUid = 123L;
		categoryUids.add(categoryUid);

		final List<Long> ancestorUids = new ArrayList<>();
		final long ancestorUid = 234L;
		ancestorUids.add(ancestorUid);

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, categoryUids);
				will(returnValue(ancestorUids));
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, ancestorUids);
				will(returnValue(Collections.emptyList()));
			}
		});

		final Set<Long> result = categoryService.findAncestorCategoryUidsByCategoryUid(categoryUid);
		assertEquals(1, result.size());
		assertTrue(result.contains(ancestorUid));
	}

	/**
	 * Tests that findAncestorCategoryCodesByCategoryUid() returns an empty Set
	 * when the category has no parents.
	 *
	 */
	@Test
	public void testFindAncestorCategoryCodesByCategoryUidNoParents() {
		final long childUid = 123L;
		final List<Long> childUids = new ArrayList<>();
		childUids.add(childUid);

		// First the child's immediate parents are retrieved (in this case none)
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, childUids);
				will(returnValue(Collections.emptyList()));
				oneOf(persistenceEngine).retrieveByNamedQueryWithList("CATEGORY_CODES_SELECT_BY_UIDS", PLACEHOLDER_FOR_LIST, Collections.emptyList());
				will(returnValue(Collections.emptyList()));
			}
		});

		final Set<String> ancestorCodesResults = categoryService.findAncestorCategoryCodesByCategoryUid(childUid);
		assertEquals("No parent category codes should be returned.", 0, ancestorCodesResults.size());
	}

	/**
	 * Tests that findAncestorCategoryCodesByCategoryUid() returns the code of a category's parent.
	 * In this case only one parent was setup.
	 */
	@Test
	public void testFindAncestorCategoryCodesByCategoryUid() {
		final long childUid = 123L;
		final List<Long> childUids = new ArrayList<>();
		childUids.add(childUid);

		final Long ancestorUid = 234L;
		final List<Long> ancestorUids = new ArrayList<>();
		ancestorUids.add(ancestorUid);

		final String ancestorCode = "900000243";
		final List<String> ancestorCodes = new ArrayList<>();
		ancestorCodes.add(ancestorCode);

		context.checking(new Expectations() {
			{
				// First the child's immediate parents are retrieved
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, childUids);
				will(returnValue(ancestorUids));

				// Next the parents of the child's parent is retrieved.
				// In this case there are no parents of the child's parent.
				oneOf(persistenceEngine).retrieveByNamedQueryWithList(CATEGORY_UID_SELECT_BY_CHILDREN_UIDS, PLACEHOLDER_FOR_LIST, ancestorUids);
				will(returnValue(Collections.emptyList()));

				// Finally, get the category codes of the parent uids
				oneOf(persistenceEngine).retrieveByNamedQueryWithList("CATEGORY_CODES_SELECT_BY_UIDS", PLACEHOLDER_FOR_LIST, ancestorUids);
				will(returnValue(ancestorCodes));
			}
		});

		final Set<String> ancestorCodesResults = categoryService.findAncestorCategoryCodesByCategoryUid(childUid);
		assertEquals("The set of ancestor codes should contain only one value.", 1, ancestorCodesResults.size());
		assertTrue("The set should contain the child's parent code", ancestorCodesResults.contains(ancestorCode));
	}

	/**
	 * Test that {@link CategoryServiceImpl#addLinkedCategory(long, long, long)} loads
	 * the categories and the catalog and then calls
	 * {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog)}.
	 */
	@Test
	public void testAddLinkedCategoryLoadsObjects() {
		final long masterCategoryUid = 524L;
		final long parentCategoryUid = 4353L;
		final long catalogUid = 12141L;

		final Category parentCategory = new CategoryImpl();
		parentCategory.setUidPk(parentCategoryUid);

		final Category masterCategory = new CategoryImpl();
		masterCategory.setUidPk(masterCategoryUid);

		final Category addedCategory = new CategoryImpl();

		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected Category addLinkedCategory(
					final Category masterCategory, final Category parentCategory, final Catalog catalog) {
				return addedCategory; //we can ensure that this method is called
			}
		};
		serviceImpl.setBeanFactory(beanFactory);
		serviceImpl.setCategoryLookup(categoryLookup);
		serviceImpl.setPersistenceEngine(persistenceEngine);
		context.checking(new Expectations() {
			{
				allowing(categoryLookup).findByUid(parentCategoryUid); will(returnValue(parentCategory));
				allowing(categoryLookup).findByUid(masterCategoryUid); will(returnValue(masterCategory));
				allowing(catalogService).load(with(any(Long.class)), with(any(FetchGroupLoadTuner.class)), with(any(Boolean.class)));
				will(returnValue(catalog));
			}
		});
		serviceImpl.setCatalogService(catalogService);

		assertEquals("Should return result of calling protected method with loaded objects",
				addedCategory, serviceImpl.addLinkedCategory(masterCategoryUid, parentCategoryUid, catalogUid));
	}

	/**
	 * Test that {@link CategoryServiceImpl#addLinkedCategory(long, long, long)} sets
	 * the default category of the contained products to the new linked category.
	 * {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog)}.
	 */
	@Test
	public void testUpdateProductsWithNewLinkedCategoryAndProductIsDefault() {
		final long masterCategoryUid = 524L;
		final long virtualCatalogUid = 12141L;

		final Catalog virtualCatalog = new CatalogImpl();
		virtualCatalog.setUidPk(virtualCatalogUid);
		virtualCatalog.setCode("virtual");
		final Catalog masterCatalog = new CatalogImpl();
		masterCatalog.setUidPk(1);
		masterCatalog.setCode("master");

		final Category masterCategory = new CategoryImpl();
		masterCategory.initialize();
		masterCategory.setUidPk(masterCategoryUid);
		masterCategory.setCatalog(masterCatalog);

		final Category addedCategory = new CategoryImpl();
		addedCategory.initialize();
		addedCategory.setCatalog(virtualCatalog);

		final Product product = new ProductImpl();
		product.setCategoryAsDefault(masterCategory);

		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
//			@Override //set expectations - assume any load tuner will work
//			public Category load(final long uid, final FetchGroupLoadTuner loadTuner) {
//				if (masterCategoryUid == uid && loadTuner != null) {
//					return masterCategory;
//				}
//				return null; //load wasn't called with any expected arguments
//			}
		};

		final LinkedCategoryImpl linkedCategory = new LinkedCategoryImpl();
		linkedCategory.initialize();
		linkedCategory.setMasterCategory(masterCategory);
		linkedCategory.setCatalog(virtualCatalog);

		serviceImpl.setBeanFactory(beanFactory);
		serviceImpl.setPersistenceEngine(persistenceEngine);
		serviceImpl.setProductService(new TestProductService(product));

		// call updateProducts
		serviceImpl.updateProductsWithNewLinkedCategory(linkedCategory);

		//expect that the product has a default category for the new virtual catalog
		assertEquals("Product must have a default category for the new virtual catalog it was created in",
				linkedCategory, product.getDefaultCategory(virtualCatalog));
	}

	/**
	 * Test that {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog)} calls
	 * {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog, int)} with a depth
	 * of zero.
	 */
	@Test
	public void testAddLinkedCategoryWithObjectsStartsWithZeroDepth() {
		final Category addedCategory = new CategoryImpl();
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected Category addLinkedCategory(final Category master, final Category parent, final Catalog catalog, final int depth) {
				assertEquals("The initial depth of the tree of linked categories passed into the recursive method "
						+ "should be zero because there aren't any yet. ", 0, depth);
				return addedCategory;
			}
		};
		final Category fakeCategory = context.mock(Category.class, "fakeCategory");
		final Catalog fakeCatalog = context.mock(Catalog.class, "fakeCatalog");
		assertEquals(addedCategory, serviceImpl.addLinkedCategory(fakeCategory, fakeCategory, fakeCatalog));
	}

	/**
	 * Test that the recursive {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog, int)}
	 * throws a DuplicateKeyException if a linked category for the given master category already exists
	 * in the given catalog.
	 */
	@Test(expected = DuplicateKeyException.class)
	public void testAddLinkedCategoryRecursiveChecksForDuplicates() {
		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl() {
			@Override
			protected boolean linkedCategoryExists(final String categoryCode, final String catalogCode) {
				return true;
			}
		};
		final Category category = context.mock(Category.class, "category");
		context.checking(new Expectations() {
			{
				ignoring(category).getCode();
				ignoring(catalog).getCode();
				allowing(catalog).isMaster();
				will(returnValue(false));
			}
		});

		serviceImpl.addLinkedCategory(category, null, catalog);
	}

	/**
	 * Test that {@link CategoryServiceImpl#updateProductsWithNewLinkedCategory(Category)}
	 * adds the given category to every product in the given category's master category.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testUpdateProductsWithNewLinkedCategory() {
		//Create a master category
		final Category masterCategory = context.mock(Category.class, "category");
		//Create a linked category, linked to the master category
		final Category linkedCategory = context.mock(Category.class, "linkedCategory");

		//Create a couple products to be in the master category
		final Collection<Product> products = new ArrayList<>();
		final Product product1 = new ProductImpl();
		final Product product2 = new ProductImpl();
		products.add(product1);
		products.add(product2);
		//Ensure that the product service returns the products when asked
		context.checking(new Expectations() {
			{
				allowing(masterCategory).getUidPk();
				will(returnValue(UID_PKS));
				allowing(masterCategory).getCatalog();
				will(returnValue(new CatalogImpl()));

				allowing(linkedCategory).getUidPk();
				will(returnValue(2L));
				allowing(linkedCategory).getMasterCategory();
				will(returnValue(masterCategory));
				allowing(linkedCategory).getGuid();
				will(returnValue("LINKED_GUID"));
				allowing(linkedCategory).getCatalog();
				will(returnValue(new CatalogImpl()));

				allowing(productService).findByCategoryUid(with(any(Long.class)), with(any(FetchGroupLoadTuner.class)));
				will(returnValue(products));
				exactly(products.size()).of(productService).saveOrUpdate(with(any(Product.class)));
				oneOf(productService).notifyCategoryUpdated(with(any(Category.class)));
			}
		});

		final CategoryServiceImpl serviceImpl = new CategoryServiceImpl();
		serviceImpl.setBeanFactory(beanFactory);
		serviceImpl.setProductService(productService);

		//TEST
		serviceImpl.updateProductsWithNewLinkedCategory(linkedCategory);

		for (final Product product : products) {
			assertTrue("Every product should now have the linked category",
					product.getCategories().contains(linkedCategory));
		}
	}

	/**
	 * Test that the recursive {@link CategoryServiceImpl#addLinkedCategory(Category, Category, Catalog, int)}
	 * recurses through every subcategory of the master category and creates a new linked category for each,
	 * setting the parent of every linked category properly.
	 */
	@Test
	public void testAddLinkedCategoryRecursiveRecurses() {
		beanFactoryExpectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.LINKED_CATEGORY, Category.class, LinkedCategoryImpl.class);

		final String masterCategoryCode = "MASTER";
		final String masterSubCategoryCode = "SUB";
		final String catalogCode = "CATALOG";
		final int masterCategoryOrder = 5;
		final int masterSubCategoryOrder = 10;
		final String masterLinkedCategoryGuid = "master-linked-category";

		//Create a master category
		final Category masterCategory = new CategoryImpl();
		masterCategory.initialize();
		masterCategory.setUidPk(UID_PKS);
		masterCategory.setCode(masterCategoryCode);
		masterCategory.setOrdering(masterCategoryOrder);

		//Create a sub category for the master category
		final Category masterSubCategory = new CategoryImpl();
		masterSubCategory.initialize();
		masterSubCategory.setCode(masterSubCategoryCode);
		masterSubCategory.setOrdering(masterSubCategoryOrder);
		masterSubCategory.setParent(masterCategory);

		//add the subcategory as a child to the master category
		final List<Category> children = Collections.singletonList(masterSubCategory);

		context.checking(new Expectations() {
			{
				allowing(catalog).getCode(); will(returnValue(catalogCode));
				allowing(catalog).isMaster(); will(returnValue(false));

				allowing(categoryLookup).findChildren(masterCategory); will(returnValue(children));
				allowing(categoryLookup).findChildren(masterSubCategory); will(returnValue(Collections.emptyList()));
				allowing(categoryLookup).findChildren(with(Matchers.<Category>instanceOf(LinkedCategoryImpl.class)));
				will(returnValue(Collections.emptyList()));
			}
		});
		//Create a parent category
		final Category parentCategory = new CategoryImpl();
		parentCategory.initialize();
		parentCategory.setUidPk(2L);
		parentCategory.setCatalog(catalog);

		context.checking(new Expectations() {
			{
				// Linked Category already Exists?  Nope.
				allowing(persistenceEngine).retrieveByNamedQuery(
						with(equal(LINKED_CATEGORY_COUNT_BY_CODE_QUERY)), with(any(Object[].class)));
				will(returnValue(Collections.singletonList(0L)));

				// Verify that the top linked category is added
				oneOf(persistenceEngine).saveOrMerge(with(any(Category.class)));
				will(new VerifyTopLinkedCategoryAction(masterCategory, parentCategory, masterLinkedCategoryGuid));

				// Verify that the child linked category is added
				oneOf(persistenceEngine).saveOrMerge(with(any(Category.class)));
				will(new VerifyChildLinkedCategoryAction(masterSubCategory, masterLinkedCategoryGuid));

				//  Noise
				allowing(indexNotificationService).addNotificationForEntityIndexUpdate(
						with(equal(IndexType.CATEGORY)), with(any(Long.class)));
				allowing(productService).findByCategoryUid(with(any(long.class)), with(any(FetchGroupLoadTuner.class)));
				will(returnValue(Collections.emptyList()));
				allowing(productService).notifyCategoryUpdated(with(any(Category.class)));
			}
		});

		//Call the test method
		final Category topLinkedCategory = categoryService.addLinkedCategory(masterCategory, parentCategory, catalog);

		//Check the top linked category
		assertEquals("addLinkedCategory() should return the top linked category",
				masterCategoryCode, topLinkedCategory.getCode());
	}

	private class VerifyTopLinkedCategoryAction implements Action {
		private final Category masterCategory;
		private final Category parentCategory;
		private final String masterLinkedCategoryGuid;

		VerifyTopLinkedCategoryAction(final Category masterCategory, final Category parentCategory, final String masterLinkedCategoryGuid) {
			this.masterCategory = masterCategory;
			this.parentCategory = parentCategory;
			this.masterLinkedCategoryGuid = masterLinkedCategoryGuid;
		}

		@Override
		public Object invoke(final Invocation invocation) throws Throwable {
			LinkedCategoryImpl topLinkedCategory = (LinkedCategoryImpl) invocation.getParameter(0);
			assertNotNull(topLinkedCategory); // fail fast
			assertTrue("Top Linked Category must be of type linked", topLinkedCategory.isLinked());
			assertEquals("Top Linked Category should be in the same catalog.",
					catalog, topLinkedCategory.getCatalog());
			assertEquals("Top Linked Category should have the master category that was passed in.",
					masterCategory, topLinkedCategory.getMasterCategory());
			assertEquals("Top Linked Category should have the parent category that was passed in.",
					parentCategory.getGuid(), topLinkedCategory.getParentGuid());
			assertTrue("Top Linked Category should be 'included'.", topLinkedCategory.isIncluded());
			assertEquals("Top Linked Category should have the same ordering as its master category",
					masterCategory.getOrdering(), topLinkedCategory.getOrdering());
			topLinkedCategory.setGuid(masterLinkedCategoryGuid);
			topLinkedCategory.setUidPk(UID_PKS);

			return topLinkedCategory;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("verifies the root linked category");
		}
	}

	private class VerifyChildLinkedCategoryAction implements Action {
		private final Category masterSubCategory;
		private final String masterLinkedCategoryGuid;

		VerifyChildLinkedCategoryAction(final Category masterSubCategory, final String masterLinkedCategoryGuid) {
			this.masterSubCategory = masterSubCategory;
			this.masterLinkedCategoryGuid = masterLinkedCategoryGuid;
		}

		@Override
		public Object invoke(final Invocation invocation) throws Throwable {
			LinkedCategoryImpl newSubCat = (LinkedCategoryImpl) invocation.getParameter(0);
			assertTrue("Linked subCategory must be of type linked", newSubCat.isLinked());
			assertEquals("Linked subCategory should be in the same catalog.", catalog, newSubCat.getCatalog());
			assertEquals("Linked subCategory should have the subcategory of the master category that was passed in.",
					masterSubCategory, newSubCat.getMasterCategory());
			assertEquals("Linked subCategory should have the top linked category as its parent category.",
					masterLinkedCategoryGuid, newSubCat.getParentGuid());
			assertTrue("Linked subCategory should be 'included'.", newSubCat.isIncluded());
			assertEquals("Linked subCategoryshould have the same ordering as its master category (the master's category's subcategory)",
					masterSubCategory.getOrdering(), newSubCat.getOrdering());

			return newSubCat;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("verifies the child linked category");
		}
	}

	/**
	 * Test method for {@link CategoryServiceImpl#addLinkedCategoryProducts(Category)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testAddLinkedCategoryProducts() {
		final long categoryUid = 3L;
		final long subCategoryUid = 44L;

		final Category category = context.mock(Category.class, "linked category");
		final Category subCategory = context.mock(Category.class, "sub category/master category");
		final Product product = context.mock(Product.class);
		final ProductDao productDao = context.mock(ProductDao.class);

		categoryService.setProductDao(productDao);
		context.checking(new Expectations() {
			{
				allowing(category).getUidPk(); will(returnValue(categoryUid));
				allowing(category).isLinked(); will(returnValue(true));
				oneOf(category).setIncluded(true);
				allowing(subCategory).getUidPk(); will(returnValue(subCategoryUid));
				allowing(subCategory).isLinked(); will(returnValue(true));
				oneOf(subCategory).setIncluded(true);
				allowing(categoryLookup).findChildren(category); will(returnValue(Collections.singletonList(subCategory)));
				allowing(category).getMasterCategory(); will(returnValue(subCategory));
				allowing(subCategory).getMasterCategory(); will(returnValue(subCategory));
				allowing(categoryLookup).findChildren(subCategory); will(returnValue(Collections.emptyList()));

				// once for the category and once for the sub-category

				exactly(2).of(product).addCategory(category);
				oneOf(productService).findByCategoryUid(with(subCategoryUid), with(any(FetchGroupLoadTuner.class)));
				will(returnValue(Arrays.asList(product, product)));
				oneOf(productService).findByCategoryUid(with(subCategoryUid), with(any(FetchGroupLoadTuner.class)));
				will(returnValue(Collections.emptyList()));
				exactly(2).of(productDao).saveOrUpdate(product); will(returnValue(product));
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, subCategoryUid);
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, categoryUid);
				oneOf(persistenceEngine).saveOrMerge(category); will(returnValue(category));
				oneOf(persistenceEngine).saveOrMerge(subCategory); will(returnValue(subCategory));
				exactly(2).of(productService).notifyCategoryUpdated(category);
				exactly(2).of(productService).notifyCategoryUpdated(subCategory);
			}
		});

		categoryService.addLinkedCategoryProducts(category);
	}

	/**
	 * Test method for {@link CategoryServiceImpl#removeCategoryProducts(Category)}.
	 */
	@Test
	public void testRemoveCategoryProducts() {
		final long categoryUid = 3L;
		final long subCategoryUid = 44L;

		final Category category = context.mock(Category.class, "linked category");
		final Category subCategory = context.mock(Category.class, "sub category/master category");

		context.checking(new Expectations() {
			{
				allowing(category).getUidPk(); will(returnValue(categoryUid));
				allowing(category).isLinked(); will(returnValue(true));
				oneOf(category).setIncluded(false);
				allowing(subCategory).getUidPk(); will(returnValue(subCategoryUid));
				allowing(subCategory).isLinked(); will(returnValue(true));
				oneOf(subCategory).setIncluded(false);
				allowing(categoryLookup).findChildren(category); will(returnValue(Collections.singletonList(subCategory)));
				allowing(category).getMasterCategory(); will(returnValue(subCategory));
				allowing(subCategory).getMasterCategory(); will(returnValue(subCategory));
				allowing(categoryLookup).findChildren(subCategory); will(returnValue(Collections.emptyList()));

				oneOf(persistenceEngine).executeNamedQuery("PRODUCTCATEGORY_DELETE_BY_CATEGORY_UID", categoryUid);
				will(returnValue(1));
				oneOf(persistenceEngine).executeNamedQuery("PRODUCTCATEGORY_DELETE_BY_CATEGORY_UID", subCategoryUid);
				will(returnValue(1));
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, categoryUid);
				oneOf(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.CATEGORY, subCategoryUid);
				oneOf(persistenceEngine).saveOrMerge(category); will(returnValue(category));
				oneOf(persistenceEngine).saveOrMerge(subCategory); will(returnValue(subCategory));
				exactly(2).of(productService).notifyCategoryUpdated(category);
				exactly(2).of(productService).notifyCategoryUpdated(subCategory);
			}
		});

		categoryService.removeCategoryProducts(category);
	}

	@Test
	public void testFindMasterCategoryUidByCompoundCategoryGuid() {
		final String code = CATEGORY_CODE_CATALOG_CODE;
		final Long uid = UID_PKS;

		context.checking(new Expectations() { {
			allowing(persistenceEngine).retrieveByNamedQuery(
					with(any(String.class)),
					(Object []) with(arrayContaining(CATEGORY_CODE, CATALOG_CODE)));
			will(returnValue(Collections.singletonList(uid)));
		} });

		assertEquals(uid, categoryService.findUidByCompoundGuid(code));
	}

	/**
	 * Tests the categoryExistsWithCompoundGuid method.
	 */
	@Test
	public void testCategoryExistsWithCompoundGuid() {
		final String code = CATEGORY_CODE_CATALOG_CODE;

		//category exists
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("categoryCode", CATEGORY_CODE);
		parameters.put("catalogCode", CATALOG_CODE);
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
				will(returnValue(Collections.singletonList(1L)));
			}
		});

		final boolean result = categoryService.categoryExistsWithCompoundGuid(code);
		assertTrue("Category Should exist", result);

	}

	/**
	 * Tests the categoryExistsWithCompoundGuid method.
	 */
	@Test
	public void testLinkedCategoryExistsWithCompoundGuid() {
		final String code = CATEGORY_CODE_CATALOG_CODE;
		//category doesn't exist, but linked category does.
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("categoryCode", CATEGORY_CODE);
		parameters.put("catalogCode", CATALOG_CODE);
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
				will(returnValue(Collections.emptyList()));

				oneOf(persistenceEngine).retrieveByNamedQuery("LINKED_CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
				will(returnValue(Collections.singletonList(1L)));
			}
		});

		final boolean result = categoryService.categoryExistsWithCompoundGuid(code);
		assertTrue("Category Should exist", result);
	}

	/**
	 * Tests the categoryExistsWithCompoundGuid method.
	 */
	@Test
	public void testCategoryDoesNotExistsWithCompoundGuid() {

		//neither category nor linked category exists.
		final String code = CATEGORY_CODE_CATALOG_CODE;

		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("categoryCode", CATEGORY_CODE);
		parameters.put("catalogCode", CATALOG_CODE);
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
				will(returnValue(Collections.emptyList()));

				oneOf(persistenceEngine).retrieveByNamedQuery("LINKED_CATEGORY_EXISTS_FOR_COMPOUND_GUID", parameters);
				will(returnValue(Collections.emptyList()));
			}
		});

		final boolean result = categoryService.categoryExistsWithCompoundGuid(code);
		assertFalse("Category Should not exist", result);

	}

	/**
	 * Test assertions when category UID is persisted.
	 */
	@Test
	public void testFindDirectDescendantCategoriesWithExistingCategoryUid() {
		final List<Category> expectedCategories = new ArrayList<>();
		expectedCategories.add(new CategoryImpl());

		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery(CATEGORY_LIST_SUBCATEGORY, EXISTING_CATEGORY_GUID);
				will(returnValue(expectedCategories));
			}
		});

		final List<Category> categories = categoryService.findDirectDescendantCategories(EXISTING_CATEGORY_GUID);
		assertEquals("The returned categories should be the same as expected", expectedCategories, categories);
	}

	/**
	 * Tests method CategoryServiceImpl.findAncestorCategoryUidsWithTreeOrder().
	 */
	@Test
	public void testFindAncestorCategoryUidsWithTreeOrder() {
		final List<Long> expectedReturnByNamedQuery = new ArrayList<>(Arrays.asList(1L, 2L));
		final Set<Long> expectedInputForSecondTime = new HashSet<>(expectedReturnByNamedQuery);

		final Set<Long> categoryUidSet = new HashSet<>();
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_CHILDREN_UIDS", "list", categoryUidSet);
				will(returnValue(expectedReturnByNamedQuery));
				oneOf(persistenceEngine).retrieveByNamedQueryWithList("CATEGORY_UID_SELECT_BY_CHILDREN_UIDS", "list", expectedInputForSecondTime);
				will(returnValue(Collections.emptyList()));
			}
		});

		final Set<Long> resultSet = categoryService.findAncestorCategoryUidsWithTreeOrder(categoryUidSet);

		final Set<Long> expectedSet = new LinkedHashSet<>(Arrays.asList(1L, 2L));
		assertEquals(resultSet.size(), expectedSet.size());

		//check the correct order of UIDs, because implementation of returned set is a LinkedHashSet
		final Iterator<Long> resultIterator = resultSet.iterator();
		final Iterator<Long> expecedIterator = expectedSet.iterator();
		assertEquals(expecedIterator.next(), resultIterator.next());
		assertEquals(expecedIterator.next(), resultIterator.next());
	}

	/**
	 * Tests that adding a linked category to a master catalog is disabled.
	 */
	@Test(expected = IllegalOperationException.class)
	public void testAddingLinkedCategoryToMasterCatalog() {
		CategoryServiceImpl service = new CategoryServiceImpl();

		Category masterCategory = context.mock(Category.class);
		Category parentCategory = context.mock(Category.class, "parentCategory");

		context.checking(new Expectations() { {
			oneOf(catalog).isMaster(); will(returnValue(true));
		} });

		service.addLinkedCategory(masterCategory, parentCategory, catalog, 0);
	}

	/** Test finding a maximum value where the catalog exists. */
	@Test
	public void testFindMaxRootOrderingCatalogExists() {
		final long catalogUid = 5515;
		final int ordering = 151;
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_ROOT_MAX_ORDERING", catalogUid);
				will(returnValue(Collections.singletonList(ordering)));
			}
		});

		assertEquals("Unexpected ordering", ordering, categoryService.findMaxRootOrdering(catalogUid));
	}

	/** Test finding a maximum value where the catalog does not exist. */
	@Test
	public void testFindMaxRootOrderingCatalogNotExists() {
		final long catalogUid = 55689;
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_ROOT_MAX_ORDERING", catalogUid);
				will(returnValue(Collections.emptyList()));
			}
		});

		assertEquals("Zero should be returned for unknown catalogs", 0, categoryService.findMaxRootOrdering(catalogUid));
	}

	/** Test finding a minimum value where the catalog exists. */
	@Test
	public void testFindMinRootOrderingCatalogExists() {
		final long catalogUid = 1245561;
		final int ordering = 88;
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_ROOT_MIN_ORDERING", catalogUid);
				will(returnValue(Collections.singletonList(ordering)));
			}
		});

		assertEquals("Unexpected ordering", ordering, categoryService.findMinRootOrdering(catalogUid));
	}

	/** Test finding a minimum value where the catalog does not exist. */
	@Test
	public void testFindMinRootOrderingCatalogNotExists() {
		final long catalogUid = 7872;
		context.checking(new Expectations() {
			{
				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_ROOT_MIN_ORDERING", catalogUid);
				will(returnValue(Collections.emptyList()));
			}
		});

		assertEquals("Zero should be returned for unknown catalogs", 0, categoryService.findMinRootOrdering(catalogUid));
	}

	/** Test finding a maximum value where the parent category exists. */
	@Test
	public void testFindMaxChildOrderingParentExists() {
		final Category category = context.mock(Category.class);
		final int ordering = 151;
		context.checking(new Expectations() {
			{
				final long categoryUid = 14515;
				allowing(category).getUidPk();
				will(returnValue(categoryUid));

				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CHILD_MAX_ORDERING", categoryUid);
				will(returnValue(Collections.singletonList(ordering)));
			}
		});

		assertEquals("Ordering was not returned", ordering, categoryService.findMaxChildOrdering(category));
	}

	/** Test finding a maximum value where the parent category does not exists. */
	@Test
	public void testFindMaxChildOrderingParentNotExists() {
		final Category category = context.mock(Category.class);
		context.checking(new Expectations() {
			{
				final long categoryUid = 14515;
				allowing(category).getUidPk();
				will(returnValue(categoryUid));

				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CHILD_MAX_ORDERING", categoryUid);
				will(returnValue(Collections.emptyList()));
			}
		});

		assertEquals("Zero should be returned for unknown parents", 0, categoryService.findMaxChildOrdering(category));
	}

	/** Test finding a minimum value where the parent category exists. */
	@Test
	public void testFindMinChildOrderingParentExists() {
		final Category category = context.mock(Category.class);
		final int ordering = 88;
		context.checking(new Expectations() {
			{
				final long categoryUid = 56008;
				allowing(category).getUidPk();
				will(returnValue(categoryUid));

				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CHILD_MIN_ORDERING", categoryUid);
				will(returnValue(Collections.singletonList(ordering)));
			}
		});

		assertEquals("Ordering was not returned", ordering, categoryService.findMinChildOrdering(category));
	}

	/** Test finding a minimum value where the parent category does not exist. */
	@Test
	public void testFindMinChildOrderingParentNotExists() {
		final Category category = context.mock(Category.class);
		context.checking(new Expectations() {
			{
				final long categoryUid = 600018;
				allowing(category).getUidPk();
				will(returnValue(categoryUid));

				oneOf(persistenceEngine).retrieveByNamedQuery("CATEGORY_CHILD_MIN_ORDERING", categoryUid);
				will(returnValue(Collections.emptyList()));
			}
		});

		assertEquals("Zero should be returned for unknown parents", 0, categoryService.findMinChildOrdering(category));
	}

	@Test
	public void testGetPath() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		final Category grandChild = createCategory(uidPks++, child);
		grandChild.setCode("grandChild");
		final Category leaf = createCategory(uidPks++, grandChild);
		leaf.setCode("leaf");

		context.checking(new Expectations() {
			{
				allowing(categoryLookup).findParent(root); will(returnValue(null));
				allowing(categoryLookup).findParent(child); will(returnValue(root));
				allowing(categoryLookup).findParent(grandChild); will(returnValue(child));
				allowing(categoryLookup).findParent(leaf); will(returnValue(grandChild));
			}
		});

		assertEquals("getPath() should return a path from the root to the specified category",
				Arrays.asList(root, child, grandChild),
				categoryService.getPath(grandChild));
	}

	@Test
	public void testThatNotHiddenAndNotExpiredCategoryWithNotHiddenAndNotExpiredParentCanSyndicate() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		root.setHidden(false);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		child.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(child, root);
		Date expiredDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		root.setEndDate(expiredDate);
		child.setEndDate(expiredDate);

		Category checked = categoryService.findByCode(child.getCode());
		assertTrue(categoryService.canSyndicate(checked));
	}

	@Test
	public void testThatHiddenAndNotExpiredCategoryWithNotHiddenAndNotExpiredParentCannotSyndicate() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		root.setHidden(false);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		child.setHidden(true);
		CategoryServiceImpl categoryService = mockCategoryService(child, root);
		Date expiredDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		root.setEndDate(expiredDate);
		child.setEndDate(expiredDate);

		Category checked = categoryService.findByCode(child.getCode());
		assertFalse(categoryService.canSyndicate(checked));
	}

	@Test
	public void testThatNotHiddenAndNotExpiredCategoryWithHiddenAndNotExpiredParentCannotSyndicate() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		root.setHidden(true);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		child.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(child, root);
		Date expiredDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		root.setEndDate(expiredDate);
		child.setEndDate(expiredDate);

		Category checked = categoryService.findByCode(child.getCode());
		assertFalse(categoryService.canSyndicate(checked));
	}

	@Test
	public void testThatNotHiddenAndExpiredCategoryWithNotHiddenAndNotExpiredParentCannotSyndicate() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		root.setHidden(false);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		child.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(child, root);
		Date expiredChildDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() - 1);
		Date expiredParentDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);

		root.setEndDate(expiredParentDate);
		child.setEndDate(expiredChildDate);

		Category checked = categoryService.findByCode(child.getCode());
		assertFalse(categoryService.canSyndicate(checked));
	}

	@Test
	public void testThatNotHiddenAndNotExpiredCategoryWithNotHiddenAndExpiredParentCannotSyndicate() {
		long uidPks = UID_PKS;
		final Category root = createCategory(uidPks++);
		root.setCode(ROOT_CODE);
		root.setHidden(false);
		final Category child = createCategory(uidPks++, root);
		child.setCode(CHILD_CODE);
		child.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(child, root);
		Date expiredChildDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		Date expiredParentDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() - 1);

		root.setEndDate(expiredParentDate);
		child.setEndDate(expiredChildDate);

		Category checked = categoryService.findByCode(child.getCode());
		assertFalse(categoryService.canSyndicate(checked));
	}

	@Test
	public void testThatLinkedCategoryIsSyndicatedWhenMasterParentCanSyndicate() {
		long uidPks = UID_PKS;
		final Category rootMaster = createCategory(uidPks++);
		rootMaster.setCode(ROOT_CODE);
		rootMaster.setHidden(false);
		final Category childMaster = createCategory(uidPks++, rootMaster);
		childMaster.setCode(CHILD_CODE);
		childMaster.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(childMaster, rootMaster);
		Date validEndDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		rootMaster.setEndDate(validEndDate);
		childMaster.setEndDate(validEndDate);

		final Category linked = getLinkedCategory(childMaster);

		assertTrue(categoryService.canSyndicate(linked));
	}

	@Test
	public void testThatLinkedCategoryIsNotSyndicatedWhenMasterParentCannotSyndicate() {
		long uidPks = UID_PKS;
		final Category tombstoneRootMaster = createCategory(uidPks++);
		tombstoneRootMaster.setCode(ROOT_CODE);
		tombstoneRootMaster.setHidden(true);
		final Category childMaster = createCategory(uidPks++, tombstoneRootMaster);
		childMaster.setCode(CHILD_CODE);
		childMaster.setHidden(false);
		CategoryServiceImpl categoryService = mockCategoryService(childMaster, tombstoneRootMaster);
		Date validEndDate = new Date(categoryService.getTimeService().getCurrentTime().getTime() + 1);
		tombstoneRootMaster.setEndDate(validEndDate);
		childMaster.setEndDate(validEndDate);

		final Category linked = getLinkedCategory(childMaster);

		assertFalse(categoryService.canSyndicate(linked));
	}

	private CategoryServiceImpl mockCategoryService(final Category child, final Category root) {
		CategoryLookup categoryLookup = mock(CategoryLookup.class);
		PersistenceEngine persistenceEngine = mock(PersistenceEngine.class);
		TimeService timeService = mock(TimeService.class);

		CategoryServiceImpl categoryService = new CategoryServiceImpl();
		categoryService.setCategoryLookup(categoryLookup);
		categoryService.setPersistenceEngine(persistenceEngine);
		categoryService.setTimeService(timeService);
		when(categoryLookup.findParent(child)).thenReturn(root);

		when(persistenceEngine.retrieveByNamedQuery("CATEGORY_SELECT_BY_CODE", child.getCode())).thenReturn(Collections.singletonList(child));
		when(timeService.getCurrentTime()).thenReturn(new Date());
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

		return categoryService;
	}

	/**
	 * A test implementation of the product service.
	 */
	private final class TestProductService extends ProductServiceImpl {
		private final Product product;

		protected TestProductService(final Product product) {
			this.product = product;
		}

		@Override
		public Collection<Product> findByCategoryUid(final long categoryUid, final FetchGroupLoadTuner loadTuner) {
			return Arrays.asList(product);
		}

		@Override
		public Product saveOrUpdate(final Product product) throws EpServiceException {
			return product;
		}

		@Override
		public void notifyCategoryUpdated(final Category category) {
			// does nothing
		}
	}

	/**
	 * @return the master catalog singleton
	 */
	private Catalog getCatalog() {
		if (masterCatalog == null) {
			masterCatalog = new CatalogImpl();
			masterCatalog.setMaster(true);
			masterCatalog.setCode("irrelevent catalog code");
		}

		return masterCatalog;
	}

	/**
	 * @return a new <code>Category</code> instance.
	 */
	private Category getCategory() {
		final Category category = new CategoryImpl();
		category.initialize();
		category.setCode(new RandomGuidImpl().toString());
		category.setCatalog(getCatalog());

		return category;
	}

	/**
	 * @return a new <code>Category</code> instance.
	 * @param childMaster
	 */
	private Category getLinkedCategory(final Category childMaster) {
		final Category category = new LinkedCategoryImpl();
		category.initialize();
		category.setCatalog(getCatalog());
		category.setMasterCategory(childMaster);

		return category;
	}

	private Category createCategory(final long uidPk) {
		Category category = getCategory();
		category.setUidPk(uidPk);

		return category;
	}

	private Category createCategory(final long uidPk, final Category parentCategory) {
		Category category = createCategory(uidPk);
		category.setParent(parentCategory);

		return category;
	}

}

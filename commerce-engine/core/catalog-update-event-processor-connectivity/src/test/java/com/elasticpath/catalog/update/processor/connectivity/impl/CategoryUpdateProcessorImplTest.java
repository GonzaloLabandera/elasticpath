/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.CATEGORY_BULK_UPDATE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.CategoryUpdateProcessorImpl.PRODUCTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.impl.ProductQueryService;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryResult;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.CategoryRelation;

/**
 * Tests {@link CategoryUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryUpdateProcessorImplTest {

	private static final String STORE_CODE = "store";
	private static final String CATEGORY_GUID = "guid";
	private static final String CHILD_CATEGORY_GUID = CATEGORY_GUID + "Child";
	private static final long CATEGORY_UID_PK = 1L;
	private static final String CATEGORY_CODE = "categoryCode";
	private static final String CHILD_CODE = CATEGORY_CODE + "Child";
	private static final String PRODUCT_CODE = "productCode";
	private static final String CHILD_PRODUCT_CODE = PRODUCT_CODE + "Child";
	private static final String FIRST = "1";
	private static final String SECOND = "2";
	private static final String THIRD = "3";
	private static final int BULK_CHANGE_EVENT_SIZE = 2;

	@Mock
	private final com.elasticpath.domain.catalog.Category category = mock(com.elasticpath.domain.catalog.Category.class);
	@Mock
	private final com.elasticpath.domain.catalog.Category child = mock(com.elasticpath.domain.catalog.Category.class);
	@Mock
	private final Category childProjection = mock(Category.class);
	@Mock
	private final Catalog catalog = mock(Catalog.class);
	@Mock
	private ProjectionService<com.elasticpath.domain.catalog.Category, Category> projectionService;
	@Mock
	private CatalogProjectionPluginProvider provider;
	@Mock
	private CatalogProjectionPlugin plugin;
	@Mock
	private CategoryWriterRepository repository;
	@Mock
	private CategoryReaderCapability categoryReaderCapability;
	@Mock
	private CategoryService categoryService;
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private ProductQueryService productQueryService;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessage eventMessage;

	private CategoryUpdateProcessorImpl categoryUpdateProcessor;

	/**
	 * Setup for the database.
	 */
	@Before
	public void setUp() {
		when(category.getGuid()).thenReturn(CATEGORY_GUID);
		when(category.getUidPk()).thenReturn(CATEGORY_UID_PK);
		when(category.getCode()).thenReturn(CATEGORY_CODE);
		when(child.getCode()).thenReturn(CHILD_CODE);
		when(child.getCatalog()).thenReturn(catalog);
		when(child.getGuid()).thenReturn(CHILD_CATEGORY_GUID);
		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Arrays.asList(mock(Category.class),
				mock(Category.class)));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getWriterCapability(CategoryWriterRepository.class)).thenReturn(Optional.of(repository));
		when(plugin.getReaderCapability(CategoryReaderCapability.class)).thenReturn(Optional.of(categoryReaderCapability));

		final QueryResult<Product> productQueryResult = mockQueryResult(Collections.emptyList());
		when(productQueryService.<Product>query(any())).thenReturn(productQueryResult);

		categoryUpdateProcessor = new CategoryUpdateProcessorImpl(projectionService, provider, categoryService, eventMessagePublisher,
				productQueryService,
				eventMessageFactory, BULK_CHANGE_EVENT_SIZE);
	}

	/**
	 * Processing of CATEGORY_CREATED event.
	 */
	@Test
	public void processCategoryCreatedTest() {
		categoryUpdateProcessor.processCategoryCreated(category);
		verify(projectionService).buildProjections(category, category.getCatalog());
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin, times(1)).getReaderCapability(CategoryReaderCapability.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of CATEGORY_UPDATED event.
	 */
	@Test
	public void processCategoryUpdatedTest() {
		categoryUpdateProcessor.processCategoryUpdated(category);
		verify(projectionService).buildProjections(category, category.getCatalog());
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin, times(1)).getReaderCapability(CategoryReaderCapability.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of CATEGORY_DELETED event.
	 */
	@Test
	public void processCategoryDeletedTest() {
		categoryUpdateProcessor.processCategoryDeleted(category.getGuid());
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin).getReaderCapability(CategoryReaderCapability.class);
		verify(repository).delete(anyString());
		verify(category).getGuid();
	}

	/**
	 * Processing of CATEGORY_UNLINKED event.
	 */
	@Test
	public void processCategoryUnlinkedTest() {
		categoryUpdateProcessor.processCategoryUnlinked(category.getGuid(), Collections.singletonList(STORE_CODE));
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin).getReaderCapability(CategoryReaderCapability.class);
		verify(repository).delete(CATEGORY_GUID, STORE_CODE);
		verify(category).getGuid();
	}

	/**
	 * Processing of CATEGORY_LINKED event.
	 */
	@Test
	public void processCategoryLINKEDTest() {
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);

		categoryUpdateProcessor.processCategoryLinked(category);
		verify(projectionService).buildProjections(category, category.getCatalog());
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin, times(1)).getReaderCapability(CategoryReaderCapability.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of CATEGORY_LINKED_UPDATED event for excluded category.
	 */
	@Test
	public void processCategoryLINKEDUPDATEDWhenCategoryIsExcludedTest() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		when(category.isIncluded()).thenReturn(false);
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);

		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
		verify(plugin, times(1)).getReaderCapability(CategoryReaderCapability.class);
		verify(repository, times(1)).delete(category.getCode(), STORE_CODE);
	}

	/**
	 * Processing of CATEGORY_LINKED_UPDATED event for included category.
	 */
	@Test
	public void processCategoryLINKEDUPDATEDWhenCategoryIsIncludedTest() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		when(category.isIncluded()).thenReturn(false);
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);

		when(category.isIncluded()).thenReturn(true);
		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
		verify(projectionService).buildProjections(category, category.getCatalog());
		verify(provider, times(2)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Sending of bulk event for excluded category.
	 */
	@Test
	public void categoryBulkEventShouldBeSentAfterExcludingCategory() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		final Product product = mockProduct(PRODUCT_CODE);
		final EventMessage eventMessage = mock(EventMessage.class);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.singletonList(product));
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);
		when(masterCategory.getUidPk()).thenReturn(1L);
		when(category.isIncluded()).thenReturn(false);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE,
				CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Collections.singletonList(PRODUCT_CODE)))).thenReturn(eventMessage);
		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);

		verify(eventMessageFactory).createEventMessage(CATEGORY_BULK_UPDATE,
				CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Collections.singletonList(PRODUCT_CODE)));
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void categoryBulkEventMessageShouldContainOfferCodesOfChildren() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		final Product product = mockProduct(PRODUCT_CODE);
		final Product childProduct = mockProduct(CHILD_PRODUCT_CODE);

		final EventMessage eventMessage = mock(EventMessage.class);
		final com.elasticpath.domain.catalog.Category masterSybCategory = mock(com.elasticpath.domain.catalog.Category.class);
		final QueryResult<Product> queryChildResult = mockQueryResult(Collections.singletonList(childProduct));
		final QueryResult<Product> queryResult = mockQueryResult(Collections.singletonList(product));

		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);
		when(masterCategory.getUidPk()).thenReturn(1L);
		when(masterSybCategory.getUidPk()).thenReturn(2L);
		when(category.isIncluded()).thenReturn(false);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Arrays.asList(PRODUCT_CODE, CHILD_PRODUCT_CODE)))).thenReturn(eventMessage);

		when(categoryService.findDirectDescendantCategories(category.getGuid())).thenReturn(Collections.singletonList(child));
		when(categoryService.findDirectDescendantCategories(child.getGuid())).thenReturn(Collections.emptyList());
		when(categoryService.findByCode(child.getCode())).thenReturn(masterSybCategory);
		when(productQueryService.<Product>query(CriteriaBuilder.criteriaFor(Product.class)
				.with(CategoryRelation.having().uids(2L))
				.returning(ResultType.ENTITY))).thenReturn(queryChildResult);

		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
		verify(eventMessageFactory).createEventMessage(CATEGORY_BULK_UPDATE,
				CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Arrays.asList(PRODUCT_CODE, CHILD_PRODUCT_CODE)));
		verify(eventMessagePublisher).publish(eventMessage);
	}

	/**
	 * Sending of bulk event for included category.
	 */
	@Test
	public void categoryBulkEventShouldBeSentAfterIncludingCategory() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		final Product product = mockProduct(PRODUCT_CODE);
		final EventMessage eventMessage = mock(EventMessage.class);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.singletonList(product));
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);
		when(masterCategory.getUidPk()).thenReturn(1L);
		when(category.isIncluded()).thenReturn(true);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE,
				CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Collections.singletonList(PRODUCT_CODE)))).thenReturn(eventMessage);
		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);

		verify(eventMessageFactory).createEventMessage(CATEGORY_BULK_UPDATE,
				CATEGORY_CODE,
				Collections.singletonMap(PRODUCTS, Collections.singletonList(PRODUCT_CODE)));
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void processCategoryUpdatedShouldCallQueryWithCategoryUidPkWhenRepositoryWriteCategoryProjectionReturnsTrue() {
		final NameIdentity nameIdentity = mock(NameIdentity.class);

		final Category categoryProjection = mock(Category.class);
		when(categoryProjection.getIdentity()).thenReturn(nameIdentity);

		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(true);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(productQueryService).query(CriteriaBuilder.criteriaFor(Product.class)
				.with(CategoryRelation.having().uids(CATEGORY_UID_PK))
				.returning(ResultType.ENTITY));
	}

	@Test
	public void processCategoryUpdatedShouldCallCreateEventMessageWithCategoryBulkUpdateEventAndCategoryCodeAndProductCode() {
		final NameIdentity nameIdentity = mock(NameIdentity.class);

		final Category categoryProjection = mock(Category.class);
		when(categoryProjection.getIdentity()).thenReturn(nameIdentity);

		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(true);

		final Product product = mockProduct(PRODUCT_CODE);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.singletonList(product));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, eventDataWithProductsCodes(PRODUCT_CODE)))
				.thenReturn(eventMessage);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(eventMessageFactory).createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, Collections.singletonMap(PRODUCTS,
				Collections.singletonList(PRODUCT_CODE)));
	}

	@Test
	public void processCategoryUpdatedShouldCallPublishEventMessage2TimesWhenProductsCountIs3AndBulkChangeEventSizeIs2() {
		final int productsCount = 3;
		final int expectedMessagesCount = 2;

		final NameIdentity nameIdentity = mock(NameIdentity.class);

		final Category categoryProjection = mock(Category.class);
		when(categoryProjection.getIdentity()).thenReturn(nameIdentity);

		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(true);

		final Product product = mockProduct(PRODUCT_CODE);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.nCopies(productsCount, product));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, eventDataWithProductsCodes(PRODUCT_CODE)))
				.thenReturn(eventMessage);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, eventDataWithProductsCodes(PRODUCT_CODE, PRODUCT_CODE)))
				.thenReturn(eventMessage);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(eventMessagePublisher, times(expectedMessagesCount)).publish(any(EventMessage.class));
	}

	@Test
	public void processCategoryUpdatedShouldNotCallQueryWhenRepositoryWriteReturnsFalse() {
		final Category categoryProjection = mock(Category.class);
		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(false);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(productQueryService, never()).query(any());
	}

	@Test
	public void processCategoryUpdatedShouldNotCallCreateEventMessageWithAnyParametersWhenRepositoryWriteReturnsFalse() {
		final Category categoryProjection = mock(Category.class);
		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(false);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(eventMessageFactory, never()).createEventMessage(any(), anyString(), anyMap());
	}

	@Test
	public void processCategoryUpdatedShouldNotCallPublishAnyEventMessageWhenRepositoryWriteReturnsFalse() {
		final Category categoryProjection = mock(Category.class);
		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(false);

		categoryUpdateProcessor.processCategoryUpdated(category);

		verify(eventMessagePublisher, never()).publish(any(EventMessage.class));
	}

	@Test
	public void processCategoryUpdatedShouldCallPublishEventMessageWithProductCodesInAscendingProductCodeOrder() {
		final Product product1 = mockProduct(FIRST);
		final Product product2 = mockProduct(SECOND);
		final Product product3 = mockProduct(THIRD);

		final NameIdentity nameIdentity = mock(NameIdentity.class);

		final Category categoryProjection = mock(Category.class);
		when(categoryProjection.getIdentity()).thenReturn(nameIdentity);

		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(true);

		final QueryResult<Product> queryResult = mockQueryResult(Arrays.asList(product3, product2, product1));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, CATEGORY_CODE, eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		categoryUpdateProcessor.processCategoryUpdated(category);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	@Test
	public void processCategoryUpdatedShouldCallPublishEventMessageForAllNestedSubCategories() {
		final int eventMessageCount = 3;

		final Product product1 = mockProduct(FIRST);
		final Product product2 = mockProduct(SECOND);

		final NameIdentity nameIdentity = mock(NameIdentity.class);

		final Category categoryProjection = mock(Category.class);
		when(categoryProjection.getIdentity()).thenReturn(nameIdentity);

		final Long categoryUidPk = 1L;
		final String categoryGuid = "categoryGuid";
		final String categoryCode = CATEGORY_CODE;
		final com.elasticpath.domain.catalog.Category category = mockCategory(categoryUidPk, categoryGuid, categoryCode);

		final Long subCategory1UidPk = 2L;
		final String subCategory1Guid = "subCategory1Guid";
		final String subCategory1Code = "subCategory1Code";
		final com.elasticpath.domain.catalog.Category subCategory1 = mockCategory(subCategory1UidPk, subCategory1Guid, subCategory1Code);

		final Long subCategory2UidPk = 3L;
		final String subCategory2Guid = "subCategory2Guid";
		final String subCategory2Code = "subCategory2Code";
		final com.elasticpath.domain.catalog.Category subCategory2 = mockCategory(subCategory2UidPk, subCategory2Guid, subCategory2Code);

		final List<com.elasticpath.domain.catalog.Category> subCategories1 = new ArrayList<>();
		subCategories1.add(subCategory1);
		when(categoryService.findDirectDescendantCategories(categoryGuid)).thenReturn(subCategories1);
		final List<com.elasticpath.domain.catalog.Category> subCategories2 = new ArrayList<>();
		subCategories1.add(subCategory2);
		when(categoryService.findDirectDescendantCategories(subCategory1Guid)).thenReturn(subCategories2);
		when(categoryService.findDirectDescendantCategories(subCategory2Guid)).thenReturn(Collections.emptyList());

		when(projectionService.buildProjections(category, category.getCatalog())).thenReturn(Collections.singletonList(categoryProjection));
		when(repository.write(categoryProjection)).thenReturn(true);

		final QueryResult<Product> queryResult = mockQueryResult(Arrays.asList(product2, product1));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		final EventMessage firstMessage = mock(EventMessage.class);
		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage thirdMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, categoryCode, eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, subCategory1Code, eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(secondMessage);
		when(eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, subCategory2Code, eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(thirdMessage);

		categoryUpdateProcessor.processCategoryUpdated(category);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		verify(eventMessagePublisher, times(eventMessageCount)).publish(any());
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
		inOrder.verify(eventMessagePublisher).publish(thirdMessage);
	}

	@Test
	public void processCategoryExcludedShouldMakeChildrenTombstone() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		when(category.isIncluded()).thenReturn(false);
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);
		when(categoryService.findDirectDescendantCategories(category.getGuid())).thenReturn(Collections.singletonList(child));

		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
		verify(repository, times(1)).delete(child.getCode(), STORE_CODE);
	}

	@Test
	public void processCategoryIncludedShouldMakeChildrenNotTombstone() {
		final List<String> stores = Collections.singletonList(STORE_CODE);
		final com.elasticpath.domain.catalog.Category masterCategory = mock(com.elasticpath.domain.catalog.Category.class);
		when(category.isIncluded()).thenReturn(false);
		when(categoryService.findByCode(CATEGORY_CODE)).thenReturn(masterCategory);
		when(categoryService.findDirectDescendantCategories(category.getGuid())).thenReturn(Collections.singletonList(child));
		when(category.isIncluded()).thenReturn(true);
		when(projectionService.buildProjections(child, child.getCatalog())).thenReturn(Collections.singletonList(childProjection));

		categoryUpdateProcessor.processCategoryIncludedExcluded(category, stores);
		verify(repository).write(childProjection);
	}

	private Product mockProduct(final String code) {
		final Product product = mock(Product.class);
		when(product.getCode()).thenReturn(code);

		return product;
	}

	private com.elasticpath.domain.catalog.Category mockCategory(final Long categoryUidPk, final String categoryGuid, final String categoryCode) {
		final com.elasticpath.domain.catalog.Category category = mock(com.elasticpath.domain.catalog.Category.class);
		when(category.getUidPk()).thenReturn(categoryUidPk);
		when(category.getGuid()).thenReturn(categoryGuid);
		when(category.getCode()).thenReturn(categoryCode);

		return category;
	}

	@SuppressWarnings("unchecked")
	private QueryResult<Product> mockQueryResult(final List<Product> products) {
		final QueryResult<Product> queryResult = (QueryResult<Product>) mock(QueryResult.class);
		when(queryResult.getResults()).thenReturn(products);

		return queryResult;
	}

	private Map<String, Object> eventDataWithProductsCodes(final String... codes) {
		return Collections.singletonMap(PRODUCTS, Arrays.asList(codes));
	}

}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.CATEGORY_BULK_UPDATE;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.CategoryRelation;

/**
 * Implementation of {@link CategoryUpdateProcessor}.
 */
public class CategoryUpdateProcessorImpl implements CategoryUpdateProcessor {

	/**
	 * Name of field in Brand bulk update event which contains list of products required for update.
	 */
	public static final String PRODUCTS = "products";

	private static final Logger LOGGER = LogManager.getLogger(CategoryUpdateProcessorImpl.class);

	private final ProjectionService<com.elasticpath.domain.catalog.Category, Category> projectionService;
	private final CategoryWriterRepository repository;
	private final CategoryReaderCapability categoryReaderCapability;
	private final CategoryService categoryService;
	private final EventMessagePublisher eventMessagePublisher;
	private final QueryService<Product> productQueryService;
	private final EventMessageFactory eventMessageFactory;
	private final int bulkChangeMaxEventSize;

	/**
	 * Constructor for CategoryUpdateProcessorImpl.
	 *
	 * @param projectionService      {@link ProjectionService} for projections building.
	 * @param provider               {@link  CatalogProjectionPluginProvider}.
	 * @param categoryService        the category service.
	 * @param eventMessagePublisher  publisher of  eventMessagePublisher.
	 * @param productQueryService    query service for a Product domain entities.
	 * @param eventMessageFactory    event message factory.
	 * @param bulkChangeMaxEventSize number of products codes in one bulk event.
	 */
	public CategoryUpdateProcessorImpl(final ProjectionService<com.elasticpath.domain.catalog.Category, Category> projectionService,
									   final CatalogProjectionPluginProvider provider, final CategoryService categoryService,
									   final EventMessagePublisher eventMessagePublisher, final QueryService<Product> productQueryService,
									   final EventMessageFactory eventMessageFactory, final int bulkChangeMaxEventSize) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(CategoryWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.categoryReaderCapability = provider.getCatalogProjectionPlugin()
				.getReaderCapability(CategoryReaderCapability.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.categoryService = categoryService;
		this.eventMessagePublisher = eventMessagePublisher;
		this.productQueryService = productQueryService;
		this.eventMessageFactory = eventMessageFactory;
		this.bulkChangeMaxEventSize = bulkChangeMaxEventSize;
	}

	@Override
	public void processCategoryCreated(final com.elasticpath.domain.catalog.Category category) {
		LOGGER.debug("Category created: {}", category.getGuid());

		final List<Category> categories = projectionService.buildProjections(category, category.getCatalog());
		categories.forEach(repository::write);
	}

	@Override
	public void processCategoryUpdated(final com.elasticpath.domain.catalog.Category category) {
		LOGGER.debug("Category updated: {}", category.getGuid());

		final List<Category> categories = projectionService.buildProjections(category, category.getCatalog());

		final List<Category> updatedCategories = new ArrayList<>();
		for (Category categoryProjection : categories) {
			boolean updated = repository.write(categoryProjection);
			if (updated) {
				updatedCategories.add(categoryProjection);
			}
		}

		if (!updatedCategories.isEmpty()) {
			publishBulkEvent(category.getCode(), findProductsByCategoryUidPk(category.getUidPk()));

			final List<String> stores = updatedCategories.stream()
					.map(Category::getIdentity).map(NameIdentity::getStore)
					.distinct().collect(toList());

			final List<com.elasticpath.domain.catalog.Category> childCategories = extractAllChildCategory(category);

			final List<com.elasticpath.domain.catalog.Category> childCategoriesWithExistProjections = childCategories.stream()
					.filter(domainCategory -> stores.stream()
							.map(store -> categoryReaderCapability.get(store, domainCategory.getCode())).findAny().isPresent())
					.collect(toList());

			childCategoriesWithExistProjections
					.forEach(domainCategory -> publishBulkEvent(domainCategory.getCode(), findProductsByCategoryUidPk(domainCategory.getUidPk())));
		}
	}

	@Override
	public void processCategoryDeleted(final String guid) {
		LOGGER.debug("Category deleted: {}", guid);

		repository.delete(guid);
	}

	@Override
	public void processCategoryLinked(final com.elasticpath.domain.catalog.Category category) {
		LOGGER.debug("Category linked: {}", category.getGuid());

		final List<Category> categories = projectionService.buildProjections(category, category.getCatalog());
		categories.forEach(repository::write);

		final com.elasticpath.domain.catalog.Category masterCategory = categoryService.findByCode(category.getCode());
		publishBulkEvent(category.getCode(), findProductsByCategoryUidPk(masterCategory.getUidPk()));
	}

	@Override
	public void processCategoryIncludedExcluded(final com.elasticpath.domain.catalog.Category linkedCategory, final List<String> stores) {
		LOGGER.debug("Linked category updated: {}", linkedCategory.getGuid());

		final List<com.elasticpath.domain.catalog.Category> childCategories = extractAllChildCategory(linkedCategory);

		if (linkedCategory.isIncluded()) {
			makeIncluded(linkedCategory, childCategories);
		} else {
			makeExcluded(linkedCategory, stores, childCategories);
		}

		final List<Product> childrenProducts = childCategories.stream()
				.map(this::findProductsByLinkedCategoryUidPk)
				.flatMap(Collection::stream)
				.collect(toList());

		final List<Product> allAffectedProducts = Stream.of(childrenProducts, findProductsByLinkedCategoryUidPk(linkedCategory))
				.flatMap(Collection::stream)
				.collect(toList());

		publishBulkEvent(linkedCategory.getCode(), allAffectedProducts);
	}

	@Override
	public void processCategoryUnlinked(final String code, final List<String> stores) {
		LOGGER.debug("Category unlinked: {}", code);

		stores.forEach(store -> repository.delete(code, store));

		final com.elasticpath.domain.catalog.Category category = categoryService.findByCode(code);

		if (Objects.nonNull(category)) {
			publishBulkEvent(category.getCode(), findProductsByCategoryUidPk(category.getUidPk()));
		}
	}

	private void makeExcluded(final com.elasticpath.domain.catalog.Category linkedCategory, final List<String> stores,
							  final List<com.elasticpath.domain.catalog.Category> childCategories) {

		stores.forEach(store -> repository.delete(linkedCategory.getCode(), store));
		childCategories.forEach(child -> stores.forEach(store -> repository.delete(child.getCode(), store)));
	}

	private void makeIncluded(final com.elasticpath.domain.catalog.Category linkedCategory,
							  final List<com.elasticpath.domain.catalog.Category> childCategories) {

		final List<Category> categories = projectionService.buildProjections(linkedCategory, linkedCategory.getCatalog());
		final List<Category> children = childCategories.stream()
				.map(child -> projectionService.buildProjections(child, child.getCatalog()))
				.flatMap(Collection::stream)
				.collect(toList());

		categories.forEach(repository::write);
		children.forEach(repository::write);
	}

	private List<Product> findProductsByCategoryUidPk(final long uidPk) {
		return productQueryService.<Product>query(CriteriaBuilder.criteriaFor(Product.class)
				.with(CategoryRelation.having().uids(uidPk))
				.returning(ResultType.ENTITY))
				.getResults();
	}

	private List<Product> findProductsByLinkedCategoryUidPk(final com.elasticpath.domain.catalog.Category linkedCategory) {
		final Optional<com.elasticpath.domain.catalog.Category> masterCategory =
				Optional.ofNullable(categoryService.findByCode(linkedCategory.getCode()));

		return masterCategory.map(Persistable::getUidPk)
				.map(this::findProductsByCategoryUidPk)
				.orElse(Collections.emptyList());
	}

	private void publishBulkEvent(final String code, final List<Product> products) {
		if (!products.isEmpty()) {

			final List<String> productCodes = products.stream()
					.map(Product::getCode)
					.sorted()
					.collect(toList());

			Lists.partition(productCodes, bulkChangeMaxEventSize).stream()
					.map(codes -> eventMessageFactory.createEventMessage(CATEGORY_BULK_UPDATE, code, Collections.singletonMap(PRODUCTS, codes)))
					.forEach(eventMessagePublisher::publish);
		}
	}

	private List<com.elasticpath.domain.catalog.Category> extractAllChildCategory(final com.elasticpath.domain.catalog.Category category) {
		final List<com.elasticpath.domain.catalog.Category> childCategories = categoryService.findDirectDescendantCategories(category.getGuid());

		childCategories.addAll(childCategories.stream().map(this::extractAllChildCategory).flatMap(List::stream).collect(toList()));

		return childCategories;
	}

}

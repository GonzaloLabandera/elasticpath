/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.recommendations.impl;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.recommendations.ItemRecommendationsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendation;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;

/**
 * Repository for gathering sourceProduct associations.
 */
@Singleton
@Named("itemRecommendationsRepository")
public class ItemRecommendationsRepositoryImpl implements ItemRecommendationsRepository {

	/**
	 * Error that indicated the failure when searching for recommendation item ids.
	 */
	static final String ITEM_ID_SEARCH_FAILURE = "Server error when searching for item ids";
	private static final Logger LOG = LoggerFactory.getLogger(ItemRecommendationsRepositoryImpl.class);
	private final ItemRepository itemRepository;
	private final RecommendedItemsPageSizeResolver pageSizeResolver;
	private final ProductRecommendationXPFBridge productRecommendationXPFBridge;
	private final ProductLookup productLookup;

	/**
	 * Creates instance with needed services wired in.
	 *
	 * @param itemRepository                 the item repository
	 * @param pageSizeResolver               the resolver for page size
	 * @param productRecommendationXPFBridge the product recommendation bridge
	 * @param productLookup                  the product lookup
	 */
	@Inject
	public ItemRecommendationsRepositoryImpl(
			@Named("itemRepository") final ItemRepository itemRepository,
			@Named("recommendedItemsPageSizeResolver") final RecommendedItemsPageSizeResolver pageSizeResolver,
			@Named("productRecommendationXPFBridge") final ProductRecommendationXPFBridge productRecommendationXPFBridge,
			@Named("productLookup") final ProductLookup productLookup) {
		this.itemRepository = itemRepository;
		this.pageSizeResolver = pageSizeResolver;
		this.productRecommendationXPFBridge = productRecommendationXPFBridge;
		this.productLookup = productLookup;
	}

	@Override
	public Single<PaginatedResult> getRecommendedItemsFromGroup(final Store store, final Product sourceProduct,
																final String recommendationGroup, final int pageNumber) {
		Single<PaginatedResult> result;
		try {
			int pageSize = pageSizeResolver.getPageSize();

			final XPFProductRecommendations productRecommendations = productRecommendationXPFBridge.getPaginatedResult(store,
					sourceProduct.getCode(),
					recommendationGroup, pageNumber, pageSize);

			result = extractRecommendedItemIds(productRecommendations.getRecommendations())
					.map(itemIds -> new PaginatedResult(itemIds, pageNumber, pageSize, productRecommendations.getTotalResultsCount()));
		} catch (Exception exception) {
			LOG.error("Error when searching for item associations", exception);
			result = Single.error(ResourceOperationFailure.serverError(ITEM_ID_SEARCH_FAILURE));
		}
		return result;
	}

	/**
	 * Extract recommendation ids as list.
	 *
	 * @param recommendations the list of product recommendation codes
	 * @return single list of ids
	 */
	protected Single<List<String>> extractRecommendedItemIds(final List<XPFProductRecommendation> recommendations) {
		return Observable.fromIterable(recommendations)
				.map(XPFProductRecommendation::getTargetProductCode)
				.toList()
				.map(productLookup::findByGuids)
				.toObservable()
				.flatMap(Observable::fromIterable)
				.map(product -> itemRepository.getDefaultItemIdForProduct((Product) product))
				.toList();
	}
}

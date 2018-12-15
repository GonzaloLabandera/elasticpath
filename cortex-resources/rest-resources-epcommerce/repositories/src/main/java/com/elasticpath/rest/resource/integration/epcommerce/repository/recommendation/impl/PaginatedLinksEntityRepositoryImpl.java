/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.recommendation.impl;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.recommendations.ItemRecommendationGroupIdentifier;
import com.elasticpath.rest.definition.recommendations.PaginatedRecommendationsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.recommendations.ItemRecommendationsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

/**
 * Paginated links entity repo for the recommendation group.
 *
 * @param <R> paginated recommendations identifier
 * @param <I> item identifier
 */
@Component(service = PaginationRepository.class)
public class PaginatedLinksEntityRepositoryImpl<R extends PaginatedRecommendationsIdentifier, I extends ItemIdentifier>
		extends PaginationRepository<PaginatedRecommendationsIdentifier, ItemIdentifier> {

	private ItemRepository itemRepository;
	private StoreRepository storeRepository;
	private ItemRecommendationsRepository itemRecommendationsRepository;

	@Override
	public Single<PaginationEntity> getPaginationInfo(final PaginatedRecommendationsIdentifier identifier) {
		return getRecommendedItemsFromGroup(identifier)
				.flatMap(this::convertPaginationResultToPaginationEntity);
	}

	@Override
	public Observable<ItemIdentifier> getElements(final PaginatedRecommendationsIdentifier identifier) {
		IdentifierPart<String> scope = identifier.getItemRecommendationGroup().getItemRecommendationGroups().getItem().getScope();

		return getRecommendedItemsFromGroup(identifier)
				.flatMapObservable(paginatedResult -> Observable.fromIterable(paginatedResult.getResultIds()))
				.map(itemId -> ItemIdentifier.builder()
						.withItemId(CompositeIdentifier.of(CompositeIdUtil.decodeCompositeId(itemId)))
						.withScope(scope)
						.build());
	}

	@Override
	public Observable<PagingLink<PaginatedRecommendationsIdentifier>> getPagingLinks(final PaginatedRecommendationsIdentifier identifier) {
		int pageId = identifier.getPageId().getValue();

		return getRecommendedItemsFromGroup(identifier)
				.map(PaginatedResult::getNumberOfPages)
				.flatMapObservable(maxNumPages -> createPagingLinks(pageId, maxNumPages, identifier));
	}

	@Override
	public PaginatedRecommendationsIdentifier buildPageIdentifier(
			final PaginatedRecommendationsIdentifier identifier, final IdentifierPart<Integer> pageId) {

		return PaginatedRecommendationsIdentifier
				.builderFrom(identifier)
				.withPageId(pageId)
				.build();
	}

	/**
	 * Get recommended items from group.
	 *
	 * @param paginatedRecommendationsIdentifier paginated recommendations identifier
	 * @return paginated result
	 */
	protected Single<PaginatedResult> getRecommendedItemsFromGroup(final PaginatedRecommendationsIdentifier paginatedRecommendationsIdentifier) {
		int pageId = paginatedRecommendationsIdentifier.getPageId().getValue();
		ItemRecommendationGroupIdentifier identifier = paginatedRecommendationsIdentifier.getItemRecommendationGroup();
		String groupId = identifier.getGroupId().getValue();
		ItemIdentifier itemIdentifier = identifier.getItemRecommendationGroups().getItem();
		IdentifierPart<Map<String, String>> itemId = itemIdentifier.getItemId();
		String scope = itemIdentifier.getScope().getValue();

		return getPaginatedResult(scope, groupId, pageId, itemId);
	}

	/**
	 * Get paginated result for the recommendation group.
	 *
	 * @param scope scope
	 * @param recommendationGroup recommendation group id
	 * @param pageNumber page number
	 * @param itemId item id
	 * @return paginated result
	 */
	protected Single<PaginatedResult> getPaginatedResult(final String scope, final String recommendationGroup,
													   final int pageNumber, final IdentifierPart<Map<String, String>> itemId) {
		return itemRepository.getSkuForItemId(itemId.getValue())
				.map(ProductSku::getProduct)
				.flatMap(product -> storeRepository.findStoreAsSingle(scope)
						.flatMap(store -> itemRecommendationsRepository
								.getRecommendedItemsFromGroup(store, product, recommendationGroup, pageNumber)));
	}

	/**
	 * Convert pagination DTO to pagination entity.
	 *
	 * @param paginatedResult paginated result
	 * @return pagination entity
	 */
	protected Single<PaginationEntity> convertPaginationResultToPaginationEntity(final PaginatedResult paginatedResult) {
		return Single.just(PaginationEntity.builder()
				.withCurrent(paginatedResult.getCurrentPage())
				.withPages(paginatedResult.getNumberOfPages())
				.withPageSize(paginatedResult.getResultsPerPage())
				.withResults(paginatedResult.getTotalNumberOfResults())
				.withResultsOnPage(paginatedResult.getResultIds().size())
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setStoreRepository(final StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}

	@Reference
	public void setItemRecommendationsRepository(final ItemRecommendationsRepository itemRecommendationsRepository) {
		this.itemRecommendationsRepository = itemRecommendationsRepository;
	}
}

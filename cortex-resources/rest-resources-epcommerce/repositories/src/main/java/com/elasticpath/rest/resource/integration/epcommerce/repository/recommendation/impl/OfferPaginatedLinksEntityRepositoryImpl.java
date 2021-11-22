/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.recommendation.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupIdentifier;
import com.elasticpath.rest.definition.recommendations.PaginatedOffersRecommendationsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;
import com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations.OfferRecommendationsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

/**
 * Paginated links entity repo for the recommendation group.
 *
 * @param <R> paginated recommendations identifier
 * @param <I> offer identifier
 */
@Component(service = PaginationRepository.class)
public class OfferPaginatedLinksEntityRepositoryImpl<R extends PaginatedOffersRecommendationsIdentifier, I extends OfferIdentifier>
		extends PaginationRepository<PaginatedOffersRecommendationsIdentifier, OfferIdentifier> {

	private StoreRepository storeRepository;
	private OfferRecommendationsRepository offerRecommendationsRepository;

	@Override
	public Single<PaginationEntity> getPaginationInfo(final PaginatedOffersRecommendationsIdentifier identifier) {
		return getRecommendedOffersFromGroup(identifier)
				.flatMap(this::convertPaginationResultToPaginationEntity);
	}

	@Override
	public Observable<OfferIdentifier> getElements(final PaginatedOffersRecommendationsIdentifier identifier) {
		IdentifierPart<String> scope = identifier.getOfferRecommendationGroup().getOfferRecommendationGroups().getOffer().getScope();

		return getRecommendedOffersFromGroup(identifier)
				.flatMapObservable(paginatedResult -> Observable.fromIterable(paginatedResult.getResultIds()))
				.map(offerId -> OfferIdentifier.builder()
						.withOfferId(CompositeIdentifier.of(ImmutableMap.of(PRODUCT_GUID_KEY, offerId)))
						.withScope(scope)
						.build());
	}

	@Override
	public Observable<PagingLink<PaginatedOffersRecommendationsIdentifier>> getPagingLinks(
			final PaginatedOffersRecommendationsIdentifier identifier) {
		int pageId = identifier.getRecommendationPageId().getValue();

		return getRecommendedOffersFromGroup(identifier)
				.map(PaginatedResult::getNumberOfPages)
				.flatMapObservable(maxNumPages -> createPagingLinks(pageId, maxNumPages, identifier));
	}

	@Override
	public PaginatedOffersRecommendationsIdentifier buildPageIdentifier(
			final PaginatedOffersRecommendationsIdentifier identifier, final IdentifierPart<Integer> pageId) {

		return PaginatedOffersRecommendationsIdentifier
				.builderFrom(identifier)
				.withRecommendationPageId(pageId)
				.build();
	}

	/**
	 * Get recommended offers from group.
	 *
	 * @param paginatedRecommendationsIdentifier paginated recommendations identifier
	 * @return paginated result
	 */
	protected Single<PaginatedResult> getRecommendedOffersFromGroup(
			final PaginatedOffersRecommendationsIdentifier paginatedRecommendationsIdentifier) {
		int pageId = paginatedRecommendationsIdentifier.getRecommendationPageId().getValue();
		OfferRecommendationGroupIdentifier identifier = paginatedRecommendationsIdentifier.getOfferRecommendationGroup();
		String groupId = identifier.getRecommendationGroupId().getValue();
		OfferIdentifier offerIdentifier = identifier.getOfferRecommendationGroups().getOffer();
		IdentifierPart<Map<String, String>> offerId = offerIdentifier.getOfferId();
		String scope = offerIdentifier.getScope().getValue();

		return getPaginatedResult(scope, groupId, pageId, offerId);
	}

	/**
	 * Get paginated result for the recommendation group.
	 *
	 * @param scope               scope
	 * @param recommendationGroup recommendation group id
	 * @param pageNumber          page number
	 * @param offerId             offer id
	 * @return paginated result
	 */
	protected Single<PaginatedResult> getPaginatedResult(final String scope, final String recommendationGroup,
														 final int pageNumber, final IdentifierPart<Map<String, String>> offerId) {

		return storeRepository.findStoreAsSingle(scope)
				.flatMap(store -> offerRecommendationsRepository
						.getRecommendedOffersFromGroup(store, offerId.getValue().get(PRODUCT_GUID_KEY), recommendationGroup, pageNumber));
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
	public void setStoreRepository(final StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}

	@Reference
	public void setOfferRecommendationsRepository(final OfferRecommendationsRepository offerRecommendationsRepository) {
		this.offerRecommendationsRepository = offerRecommendationsRepository;
	}
}

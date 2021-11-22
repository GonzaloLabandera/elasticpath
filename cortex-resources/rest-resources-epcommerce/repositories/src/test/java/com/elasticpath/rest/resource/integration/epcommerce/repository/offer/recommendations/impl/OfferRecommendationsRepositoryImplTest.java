/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendation;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;

/**
 * Unit tests for {@link OfferRecommendationsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferRecommendationsRepositoryImplTest {

	private static final String GROUP = "upsell";
	private static final String OFFER_ID = "offerId";
	private static final int PAGE_SIZE = 10;

	@InjectMocks
	private OfferRecommendationsRepositoryImpl repository;
	@Mock
	private Store store;
	@Mock
	private RecommendedOfferPageSizeResolver paginationResolver;
	@Mock
	private ProductRecommendationXPFBridge productRecommendationXPFBridge;

	@Before
	public void setUp() {
		when(paginationResolver.getPageSize()).thenReturn(PAGE_SIZE);
		when(productRecommendationXPFBridge.getPaginatedResult(store, OFFER_ID, GROUP, 1, PAGE_SIZE))
				.thenReturn(new XPFProductRecommendations(0, Collections.emptyList()));
	}

	@Test
	public void testGettingRecommendedOffersWhenZeroReturned() {
		PaginatedResult paginatedResult = repository.getRecommendedOffersFromGroup(store, OFFER_ID, GROUP, 1)
				.blockingGet();

		assertThat(paginatedResult.getCurrentPage()).isEqualTo(1);
		assertThat(paginatedResult.getNumberOfPages()).isEqualTo(1);
		assertThat(paginatedResult.getResultsPerPage()).isEqualTo(PAGE_SIZE);
		assertThat(paginatedResult.getTotalNumberOfResults()).isEqualTo(0);
		assertThat(paginatedResult.getResultIds()).isEmpty();
	}

	@Test
	public void testGettingRecommendedOffersWhenOneReturned() {
		when(productRecommendationXPFBridge.getPaginatedResult(store, OFFER_ID, GROUP, 1, PAGE_SIZE))
				.thenReturn(new XPFProductRecommendations(1, Collections.singletonList(new XPFProductRecommendation(OFFER_ID))));

		PaginatedResult paginatedResult = repository.getRecommendedOffersFromGroup(store, OFFER_ID, GROUP, 1).blockingGet();

		assertThat(paginatedResult.getCurrentPage()).isEqualTo(1);
		assertThat(paginatedResult.getNumberOfPages()).isEqualTo(1);
		assertThat(paginatedResult.getResultsPerPage()).isEqualTo(PAGE_SIZE);
		assertThat(paginatedResult.getTotalNumberOfResults()).isEqualTo(1);
		assertThat(paginatedResult.getResultIds().stream().findFirst().get()).isEqualTo(OFFER_ID);
	}

	@Test
	public void testGettingRecommendedOffersWhenServiceError() {
		when(productRecommendationXPFBridge.getPaginatedResult(store, OFFER_ID, GROUP, 1, PAGE_SIZE))
				.thenThrow(new EpServiceException(""));

		repository.getRecommendedOffersFromGroup(store, OFFER_ID, GROUP, 1)
				.test()
				.assertError(ResourceOperationFailure.serverError(OfferRecommendationsRepositoryImpl.OFFER_ID_SEARCH_FAILURE));
	}
}

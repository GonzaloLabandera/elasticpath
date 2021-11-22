/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.recommendations.impl;

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
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendation;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;

/**
 * Unit tests for {@link ItemRecommendationsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemRecommendationsRepositoryImplTest {
	private static final String GROUP = "upsell";
	private static final int PAGE_SIZE = 10;
	private static final String PRODUCT_CODE = "productCode";
	private static final String PRODUCT_RECOMMENDATION_CODE = "recommendation";
	private static final String RECOMMENDED_ITEM = "recommendedItem";

	@InjectMocks
	private ItemRecommendationsRepositoryImpl repository;
	@Mock
	private Store store;
	@Mock
	private Product product;
	@Mock
	private Product recommendedProduct;
	@Mock
	private RecommendedItemsPageSizeResolver paginationResolver;
	@Mock
	private ProductRecommendationXPFBridge productRecommendationXPFBridge;
	@Mock
	private ProductLookup productLookup;
	@Mock
	private ItemRepository itemRepository;

	@Before
	public void setUp() {
		when(product.getCode()).thenReturn(PRODUCT_CODE);
		when(paginationResolver.getPageSize()).thenReturn(PAGE_SIZE);
		when(productRecommendationXPFBridge.getPaginatedResult(store, PRODUCT_CODE, GROUP, 1, PAGE_SIZE))
				.thenReturn(new XPFProductRecommendations(0, Collections.emptyList()));
		when(productLookup.findByGuids(Collections.singletonList(PRODUCT_RECOMMENDATION_CODE)))
				.thenReturn(Collections.singletonList(recommendedProduct));
		when(itemRepository.getDefaultItemIdForProduct(recommendedProduct)).thenReturn(RECOMMENDED_ITEM);
	}

	@Test
	public void testGettingRecommendedItemsWhenZeroReturned() {
		PaginatedResult paginatedResult = repository.getRecommendedItemsFromGroup(store, product, GROUP, 1)
				.blockingGet();

		assertThat(paginatedResult.getCurrentPage()).isEqualTo(1);
		assertThat(paginatedResult.getNumberOfPages()).isEqualTo(1);
		assertThat(paginatedResult.getResultsPerPage()).isEqualTo(PAGE_SIZE);
		assertThat(paginatedResult.getTotalNumberOfResults()).isEqualTo(0);
		assertThat(paginatedResult.getResultIds()).isEmpty();
	}

	@Test
	public void testGettingRecommendedItemsWhenOneReturned() {
		when(productRecommendationXPFBridge.getPaginatedResult(store, PRODUCT_CODE, GROUP, 1, PAGE_SIZE))
				.thenReturn(new XPFProductRecommendations(1, Collections.singletonList(new XPFProductRecommendation(PRODUCT_RECOMMENDATION_CODE))));
		PaginatedResult paginatedResult = repository.getRecommendedItemsFromGroup(store, product, GROUP, 1)
				.blockingGet();

		assertThat(paginatedResult.getCurrentPage()).isEqualTo(1);
		assertThat(paginatedResult.getNumberOfPages()).isEqualTo(1);
		assertThat(paginatedResult.getResultsPerPage()).isEqualTo(PAGE_SIZE);
		assertThat(paginatedResult.getTotalNumberOfResults()).isEqualTo(1);
		assertThat(paginatedResult.getResultIds().stream().findFirst().get()).isEqualTo(RECOMMENDED_ITEM);
	}

	@Test
	public void testGettingRecommendedItemsWhenServiceError() {
		when(productRecommendationXPFBridge.getPaginatedResult(store, PRODUCT_CODE, GROUP, 1, PAGE_SIZE))
				.thenThrow(new EpServiceException(""));

		repository.getRecommendedItemsFromGroup(store, product, GROUP, 1)
				.test()
				.assertError(ResourceOperationFailure.serverError(ItemRecommendationsRepositoryImpl.ITEM_ID_SEARCH_FAILURE));
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.query.ProductAssociationSearchCriteria;
import com.elasticpath.xpf.connectivity.context.XPFProductRecommendationsContext;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

/**
 * Test or {@link ElasticPathProductRecommendationsRetrieval}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticPathProductRecommendationsRetrievalTest {

	private static final String RECOMMENDATION_TYPE = ProductAssociationType.ACCESSORY.getName();
	private static final String SOURCE_PRODUCT_CODE = "sourceProductCode";
	private static final int PAGE_SIZE = 10;
	private static final int PAGE_NUMBER = 1;
	private static final String RECOMMENDED_PRODUCT_CODE = "recommendedProductCode";
	private static final String CATALOG_CODE = "catalogCode";
	@Mock
	private ProductAssociationService productAssociationService;
	@Mock
	private TimeService timeService;
	@Mock
	private XPFStore store;
	@Mock
	private ProductAssociation productAssociation;
	@Mock
	private Product targetProduct;
	@Mock
	private XPFCatalog catalog;

	private XPFProductRecommendationsContext context;

	@InjectMocks
	private ElasticPathProductRecommendationsRetrieval recommendationsRetrieval;

	private void setUp(final List<ProductAssociation> associationList) {
		Date date = new Date();

		when(timeService.getCurrentTime()).thenReturn(date);
		when(store.getCatalog()).thenReturn(catalog);
		when(catalog.getCode()).thenReturn(CATALOG_CODE);

		context = new XPFProductRecommendationsContext(PAGE_SIZE, PAGE_NUMBER, RECOMMENDATION_TYPE, SOURCE_PRODUCT_CODE, store);

		final ProductAssociationSearchCriteria criteria = new ProductAssociationSearchCriteria();
		criteria.setAssociationType(ProductAssociationType.fromName(context.getRecommendationType()));
		criteria.setSourceProductCode(context.getSourceProductCode());
		criteria.setCatalogCode(context.getStore().getCatalog().getCode());
		criteria.setWithinCatalogOnly(true);
		criteria.setStartDateBefore(date);
		criteria.setEndDateAfter(date);

		when(productAssociationService.findCountForCriteria(criteria)).thenReturn((long) associationList.size());
		when(productAssociationService.findByCriteria(criteria, (context.getPageNumber() - 1) * context.getPageSize(), PAGE_SIZE))
				.thenReturn(associationList);
		when(productAssociation.getTargetProduct()).thenReturn(targetProduct);
		when(targetProduct.getCode()).thenReturn(RECOMMENDED_PRODUCT_CODE);
	}

	@Test
	public void testGetRecommendationsReturnsOneResult() {
		setUp(Collections.singletonList(productAssociation));
		XPFProductRecommendations recommendations = recommendationsRetrieval.getRecommendations(context);

		assertEquals(1, recommendations.getTotalResultsCount());
		assertEquals(1, recommendations.getRecommendations().size());
		assertEquals(RECOMMENDED_PRODUCT_CODE, recommendations.getRecommendations().get(0).getTargetProductCode());

		verifyMocks();
	}

	@Test
	public void testGetRecommendationsReturnsEmptyResult() {
		setUp(Collections.emptyList());

		XPFProductRecommendations recommendations = recommendationsRetrieval.getRecommendations(context);

		assertEquals(0, recommendations.getTotalResultsCount());
		assertEquals(0, recommendations.getRecommendations().size());
	}

	private void verifyMocks() {
		verify(timeService).getCurrentTime();
		verify(store, times(2)).getCatalog();
		verify(catalog, times(2)).getCode();
		verify(productAssociation).getTargetProduct();
		verify(targetProduct).getCode();
	}
}

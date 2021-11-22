/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.impl.ProductRecommendationXPFBridgeImpl;
import com.elasticpath.xpf.connectivity.context.XPFProductRecommendationsContext;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductRecommendations;
import com.elasticpath.xpf.converters.StoreConverter;
import com.elasticpath.xpf.extensions.ElasticPathProductRecommendationsRetrieval;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Test or {@link ProductRecommendationXPFBridge}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductRecommendationXPFBridgeTest {
	private static final String SOURCE_PRODUCT_CODE = "sourceProductCode";
	private static final String RECOMMENDATION_GROUP = "recommendationGroup";
	private static final int PAGE_SIZE = 10;
	@Mock
	private StoreConverter storeConverter;
	@Mock
	private XPFExtensionLookup xpfExtensionLookup;
	@Mock
	private Store store;
	@Mock
	private XPFStore xpfStore;
	@Mock
	private ElasticPathProductRecommendationsRetrieval recommendationsRetrieval;
	@Mock
	private XPFProductRecommendations xpfProductRecommendations;

	@InjectMocks
	private ProductRecommendationXPFBridgeImpl productRecommendationXPFBridge;

	@Before
	public void setUp() {
		when(storeConverter.convert(store)).thenReturn(xpfStore);
		when(xpfExtensionLookup.getSingleExtension(ProductRecommendations.class,
				XPFExtensionPointEnum.PRODUCT_RECOMMENDATIONS, new XPFExtensionSelectorByStoreCode(store.getCode())))
				.thenReturn(recommendationsRetrieval);
		when(recommendationsRetrieval.getRecommendations(new XPFProductRecommendationsContext(PAGE_SIZE, 1, RECOMMENDATION_GROUP,
				SOURCE_PRODUCT_CODE,
				xpfStore))).thenReturn(xpfProductRecommendations);
	}

	@Test
	public void testGetPaginatedResult() {
		XPFProductRecommendations recommendations = productRecommendationXPFBridge.getPaginatedResult(store, SOURCE_PRODUCT_CODE,
				RECOMMENDATION_GROUP, 1, PAGE_SIZE);

		assertEquals(xpfProductRecommendations, recommendations);

		verify(storeConverter).convert(store);
		verify(xpfExtensionLookup).getSingleExtension(ProductRecommendations.class,
				XPFExtensionPointEnum.PRODUCT_RECOMMENDATIONS, new XPFExtensionSelectorByStoreCode(store.getCode()));
		verify(recommendationsRetrieval).getRecommendations(new XPFProductRecommendationsContext(PAGE_SIZE, 1, RECOMMENDATION_GROUP,
				SOURCE_PRODUCT_CODE, xpfStore));
	}
}

/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges.impl;

import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge;
import com.elasticpath.xpf.connectivity.context.XPFProductRecommendationsContext;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductRecommendations;
import com.elasticpath.xpf.converters.StoreConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Implementation of {@code com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge}.
 */
public class ProductRecommendationXPFBridgeImpl implements ProductRecommendationXPFBridge {

	private StoreConverter storeConverter;
	private XPFExtensionLookup xpfExtensionLookup;

	/**
	 * Returns paginated list of product codes.
	 *
	 * @param store               the store code
	 * @param sourceProductCode   the source product code
	 * @param recommendationGroup the recommendation group
	 * @param pageNumber          the page number
	 * @param pageSize            the page size
	 * @return product recommendations
	 */
	public XPFProductRecommendations getPaginatedResult(final Store store, final String sourceProductCode, final String recommendationGroup,
														final int pageNumber, final int pageSize) {

		final XPFStore xpfStore = storeConverter.convert(store);
		final XPFProductRecommendationsContext context = new XPFProductRecommendationsContext(pageSize, pageNumber, recommendationGroup,
				sourceProductCode,
				xpfStore);

		final ProductRecommendations recommendationsRetrieval =
				xpfExtensionLookup.getSingleExtension(ProductRecommendations.class, XPFExtensionPointEnum.PRODUCT_RECOMMENDATIONS,
						new XPFExtensionSelectorByStoreCode(store.getCode()));

		return recommendationsRetrieval.getRecommendations(context);
	}

	protected StoreConverter getStoreConverter() {
		return storeConverter;
	}

	public void setStoreConverter(final StoreConverter storeConverter) {
		this.storeConverter = storeConverter;
	}


	protected XPFExtensionLookup getXpfExtensionLookup() {
		return xpfExtensionLookup;
	}

	public void setXpfExtensionLookup(final XPFExtensionLookup xpfExtensionLookup) {
		this.xpfExtensionLookup = xpfExtensionLookup;
	}
}

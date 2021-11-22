/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.service.catalog.ProductAssociationService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.query.ProductAssociationSearchCriteria;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductRecommendationsContext;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendation;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductRecommendations;

/**
 * Extension for retrieving product recommendations from the Elastic Path database.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.PRODUCT_RECOMMENDATIONS, priority = 1050)
public class ElasticPathProductRecommendationsRetrieval extends XPFExtensionPointImpl implements ProductRecommendations {

	@Autowired
	private ProductAssociationService productAssociationService;
	@Autowired
	private TimeService timeService;

	@Override
	public XPFProductRecommendations getRecommendations(final XPFProductRecommendationsContext context) {
		final ProductAssociationType productAssociationType = ProductAssociationType.fromName(context.getRecommendationType());

		final ProductAssociationSearchCriteria criteria = new ProductAssociationSearchCriteria();
		criteria.setAssociationType(productAssociationType);
		criteria.setSourceProductCode(context.getSourceProductCode());
		criteria.setCatalogCode(context.getStore().getCatalog().getCode());
		criteria.setWithinCatalogOnly(true);
		final Date now = timeService.getCurrentTime();
		criteria.setStartDateBefore(now);
		criteria.setEndDateAfter(now);

		final int startIndex = (context.getPageNumber() - 1) * context.getPageSize();

		final int totalResultCount = productAssociationService.findCountForCriteria(criteria).intValue();
		List<String> recommendedItemIds = Collections.emptyList();
		if (totalResultCount > 0) {
			recommendedItemIds = extractRecommendedItemIds(criteria, context.getPageSize(), startIndex);
		}
		final List<XPFProductRecommendation> result = recommendedItemIds.stream().map(XPFProductRecommendation::new).collect(Collectors.toList());

		return new XPFProductRecommendations(totalResultCount, result);
	}

	private List<String> extractRecommendedItemIds(final ProductAssociationSearchCriteria criteria, final int pageSize, final int startIndex) {
		return productAssociationService.findByCriteria(criteria, startIndex, pageSize).stream()
				.map(ProductAssociation::getTargetProduct)
				.map(Product::getCode)
				.collect(Collectors.toList());
	}
}

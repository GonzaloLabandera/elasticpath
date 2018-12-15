/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.rest.definition.base.ScopeIdentifierPart;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdIdentifierPart;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

@RunWith(MockitoJUnitRunner.class)
public class OfferComponentLinkRepositoryImplTest {

	@Mock
	private StoreProductRepository storeProductRepository;
	@InjectMocks
	private OfferComponentLinkRepositoryImpl<OfferIdentifier, OfferComponentsIdentifier> offerComponentLinkRepository;

	@Test
	public void testHasLink() {
		ProductBundleImpl productBundle = Mockito.mock(ProductBundleImpl.class);
		when(storeProductRepository.findByGuid(anyString())).thenReturn(Single.just(productBundle));
		offerComponentLinkRepository.getElements(OfferIdentifier.builder()
				.withOfferId(OfferIdIdentifierPart.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, "id")).withScope(ScopeIdentifierPart.of("hello")).build())
				.test()
				.assertValueCount(1);
	}

	@Test
	public void testNoLink() {
		ProductImpl productBundle = Mockito.mock(ProductImpl.class);
		when(storeProductRepository.findByGuid(anyString())).thenReturn(Single.just(productBundle));
		offerComponentLinkRepository.getElements(OfferIdentifier.builder()
				.withOfferId(OfferIdIdentifierPart.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, "id")).withScope(ScopeIdentifierPart.of("hello")).build())
				.test()
				.assertNoValues();
	}

}
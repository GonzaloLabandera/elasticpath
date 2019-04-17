/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.impl.BundleConstituentImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.rest.definition.base.ScopeIdentifierPart;
import com.elasticpath.rest.definition.items.ItemIdIdentifierPart;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdIdentifierPart;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

@RunWith(MockitoJUnitRunner.class)
public class OfferComponentLinksRepositoryImplTest {

	@Mock
	private StoreProductRepository storeProductRepository;
	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private OfferComponentLinksRepositoryImpl<OfferComponentsIdentifier, ResourceIdentifier> offerComponentLinksRepository;

	@Test
	public void testGetProduct() {
		BundleConstituentImpl constituent = Mockito.mock(BundleConstituentImpl.class);
		ConstituentItem constituentItem = Mockito.mock(ConstituentItem.class);
		ProductImpl product = Mockito.mock(ProductImpl.class);
		when(product.getGuid()).thenReturn("guid");
		when(constituentItem.getProduct()).thenReturn(product);
		when(constituent.getConstituent()).thenReturn(constituentItem);
		ProductBundleImpl productBundle = Mockito.mock(ProductBundleImpl.class);
		when(productBundle.getConstituents()).thenReturn(Collections.singletonList(constituent));
		when(storeProductRepository.findByGuid(anyString())).thenReturn(Single.just(productBundle));
		when(itemRepository.asProductBundle(any())).thenReturn(Single.just(productBundle));
		offerComponentLinksRepository.getElements(OfferComponentsIdentifier.builder()
					.withOffer(OfferIdentifier.builder()
							.withOfferId(OfferIdIdentifierPart.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, "id"))
							.withScope(ScopeIdentifierPart.of("hello")).build())
					.build())
				.map(identifier -> ((OfferIdentifier) identifier).getOfferId().getValue().get(SearchRepositoryImpl.PRODUCT_GUID_KEY))
				.test()
				.assertValue("guid");
	}

	@Test
	public void testGetProductSku() {
		BundleConstituentImpl constituent = Mockito.mock(BundleConstituentImpl.class);
		ConstituentItem constituentItem = Mockito.mock(ConstituentItem.class);
		when(constituentItem.isProductSku()).thenReturn(true);
		when(constituentItem.getProductSku()).thenReturn(new ProductSkuImpl("sku", AvailabilityCriteria.ALWAYS_AVAILABLE));
		when(constituent.getConstituent()).thenReturn(constituentItem);
		ProductBundleImpl productBundle = Mockito.mock(ProductBundleImpl.class);
		when(productBundle.getConstituents()).thenReturn(Collections.singletonList(constituent));
		when(storeProductRepository.findByGuid(anyString())).thenReturn(Single.just(productBundle));
		when(itemRepository.asProductBundle(any())).thenReturn(Single.just(productBundle));
		offerComponentLinksRepository.getElements(OfferComponentsIdentifier.builder()
					.withOffer(OfferIdentifier.builder()
							.withOfferId(ItemIdIdentifierPart.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, "id"))
							.withScope(ScopeIdentifierPart.of("hello")).build())
					.build())
				.map(identifier -> ((ItemIdentifier) identifier).getItemId().getValue().get(ItemRepository.SKU_CODE_KEY))
				.test()
				.assertValue("sku");
	}
}
package com.elasticpath.common.pricing.service.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Currency;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.tags.TagSet;

/**
 * Tests for {@link PromotedPriceLookupServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PromotedPriceLookupServiceImplTest {

	@Mock
	private BundleIdentifier bundleIdentifier;

	@InjectMocks
	private PromotedPriceLookupServiceImpl promotedPriceLookupService;

	@Mock
	private Product product;

	@Mock
	private Store store;

	private static final Currency CURRENCY = Currency.getInstance("USD");

	@Mock
	private Price priceForSku;

	@Mock
	private TagSet tagSet;

	@Mock
	private SettingValueProvider<Boolean> booleanSettingValueProvider;


	@Test
	public void applyCatalogPromotionsEnabled() {
		given(booleanSettingValueProvider.get()).willReturn(true);
		given(bundleIdentifier.isCalculatedBundle(product)).willReturn(true);
		promotedPriceLookupService.applyCatalogPromotions(product, store, CURRENCY, priceForSku, tagSet);
		verify(bundleIdentifier).isCalculatedBundle(product);
	}

	@Test
	public void applyCatalogPromotionsDisabled() {
		given(booleanSettingValueProvider.get()).willReturn(false);
		promotedPriceLookupService.applyCatalogPromotions(product, store, CURRENCY, priceForSku, tagSet);
		verify(bundleIdentifier, never()).isCalculatedBundle(product);
	}
}
/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.catalog.BundleIdentifier;

/**
 * Contains tests for ItemRepository operations.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class ItemRepositoryImplTest {
	private static final String BUNDLE_CONS_NOT_FOUND = "Bundle constituent not found.";
	private static final String CONSTITUENT_NOT_BUNDLE = "Constituent is not a bundle.";
	private static final String PRODUCT_CODE = "product code";
	private static final String SKU_CODE = "sku code";

	@Mock
	private ProductSkuRepository mockProductSkuRepository;
	@Mock
	private BundleIdentifier mockBundleIdentifier;

	private ItemRepositoryImpl itemRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	private final Product product = new ProductImpl();
	private final ProductSku productSku = createProductSku(SKU_CODE, product);

	@Before
	public void setUp() {
		product.setCode(PRODUCT_CODE);
		itemRepository = new ItemRepositoryImpl(mockProductSkuRepository, mockBundleIdentifier, reactiveAdapter);
	}

	@Test
	public void ensureThatItemIdGeneratedForSkuCanBeUsedToRetrieveIt() {
		final IdentifierPart<Map<String, String>> itemId = itemRepository.getItemIdMap(productSku.getSkuCode());

		assertThat(itemId).isNotNull();

		when(mockProductSkuRepository.getProductSkuWithAttributesByCode(productSku.getSkuCode()))
				.thenReturn(Single.just(productSku));

		itemRepository.getSkuForItemId(itemId.getValue())
				.test()
				.assertNoErrors()
				.assertValue(productSku);
	}

	@Test(expected = AssertionError.class)
	public void ensureThatGetSkuForNullItemIdThrowsAssertionError() {
		itemRepository.getSkuForItemId(null);
	}

	@Test
	public void ensureIsItemBundleReturnsResultsFromService() {
		when(mockProductSkuRepository.isProductBundleByCode(productSku.getSkuCode())).thenReturn(Single.just(true));

		itemRepository.isItemBundle(generateItemIdMap(SKU_CODE))
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void isProductSkuExistForItemIdShouldFailOnInvalidItemId() {

		itemRepository.isProductSkuExistForItemId(Collections.emptyMap())
				.test()
				.assertError(ResourceOperationFailure.notFound("Item not found."))
				.assertNoValues();
	}

	@Test
	public void isProductSkuExistForItemIdShouldReturnTrue() {
		when(mockProductSkuRepository.isProductSkuExistByCode(SKU_CODE)).thenReturn(Single.just(true));
		itemRepository.isProductSkuExistForItemId(generateItemIdMap(SKU_CODE))
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void isProductSkuExistForItemIdShouldReturnFalse() {
		when(mockProductSkuRepository.isProductSkuExistByCode(SKU_CODE)).thenReturn(Single.just(false));
		itemRepository.isProductSkuExistForItemId(generateItemIdMap(SKU_CODE))
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void ensureAsProductBundleReturnsABundleAsSingle() {
		ProductBundle mockProductBundle = mock(ProductBundle.class);
		when(mockBundleIdentifier.asProductBundle(product)).thenReturn(mockProductBundle);

		itemRepository.asProductBundle(product)
				.test()
				.assertValue(mockProductBundle);
	}

	@Test
	public void ensureAsProductBundleAsSingleReturnsErrorIfProductIsNull() {
		itemRepository.asProductBundle(null)
				.test()
				.assertError(ResourceOperationFailure.notFound("product should not be null."));
	}

	@Test
	public void givenValidItemIdReturnSkuOptionsAsObservable() {
		Set<SkuOption> skuOptions = Sets.newHashSet();

		when(mockProductSkuRepository.getProductSkuOptionsByCode(SKU_CODE)).thenReturn(Observable.fromIterable(skuOptions));

		itemRepository.getSkuOptionsForItemId(generateItemIdMap(SKU_CODE))
				.test()
				.assertNoErrors()
				.assertValueSet(skuOptions);
	}

	@Test
	public void givenEndOfGuidPathShouldReturnCurrentBundleConstituent() {
		final BundleConstituent currBundleCons = mock(BundleConstituent.class);
		final Iterator<String> emptyPathIterator = Collections.emptyIterator();

		itemRepository.getNestedBundleConstituent(currBundleCons, emptyPathIterator)
				.test()
				.assertNoErrors()
				.assertValue(currBundleCons);
	}

	@Test
	public void givenGuidPathShouldReturnEndBundleConstituent() {
		final BundleConstituent currBundleCons = mock(BundleConstituent.class);
		final ConstituentItem constituentItem = mock(ConstituentItem.class);
		final ProductBundle mockProductBundle = mock(ProductBundle.class);
		final BundleConstituent nestedBundleCons1 = mock(BundleConstituent.class);
		final BundleConstituent nestedBundleCons2 = mock(BundleConstituent.class);

		final Iterator<String> guidPathIterator = Lists.list("nestedBundleCons2").iterator();

		when(currBundleCons.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProduct()).thenReturn(product);
		when(constituentItem.isBundle()).thenReturn(true);
		when(mockBundleIdentifier.asProductBundle(product)).thenReturn(mockProductBundle);
		when(mockProductBundle.getConstituents()).thenReturn(Lists.list(nestedBundleCons1, nestedBundleCons2));
		when(nestedBundleCons1.getGuid()).thenReturn("nestedBundleCons1");
		when(nestedBundleCons2.getGuid()).thenReturn("nestedBundleCons2");

		itemRepository.getNestedBundleConstituent(currBundleCons, guidPathIterator)
				.test()
				.assertNoErrors()
				.assertValue(nestedBundleCons2);
	}

	@Test
	public void givenBundleConstituentThatIsBundleReturnProductBundle() {
		final BundleConstituent bundleCons = mock(BundleConstituent.class);
		final ConstituentItem constituentItem = mock(ConstituentItem.class);
		final ProductBundle mockProductBundle = mock(ProductBundle.class);

		when(bundleCons.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProduct()).thenReturn(product);
		when(constituentItem.isBundle()).thenReturn(true);
		when(mockBundleIdentifier.asProductBundle(product)).thenReturn(mockProductBundle);

		itemRepository.getProductBundleFromConstituent(bundleCons)
				.test()
				.assertNoErrors()
				.assertValue(mockProductBundle);
	}

	@Test
	public void givenBundleConstituentThatIsNotBundleReturnError() {
		final BundleConstituent bundleCons = mock(BundleConstituent.class);
		final ConstituentItem constituentItem = mock(ConstituentItem.class);

		when(bundleCons.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.isBundle()).thenReturn(false);

		itemRepository.getProductBundleFromConstituent(bundleCons)
				.test()
				.assertError(ResourceOperationFailure.notFound(CONSTITUENT_NOT_BUNDLE))
				.assertNoValues();
	}

	@Test
	public void givenBundleConstituentListContainingGuidShouldReturnBundleConstituent() {
		final String expectedGuid = "bundleCons2";
		final BundleConstituent bundleCons1 = mock(BundleConstituent.class);
		final BundleConstituent bundleCons2 = mock(BundleConstituent.class);

		when(bundleCons1.getGuid()).thenReturn("bundleCons1");
		when(bundleCons2.getGuid()).thenReturn(expectedGuid);

		itemRepository.findBundleConstituentWithGuid(Lists.list(bundleCons1, bundleCons2), expectedGuid)
				.test()
				.assertNoErrors()
				.assertValue(bundleCons2);
	}

	@Test
	public void givenBundleConstituentListNotContainingGuidShouldReturnError() {
		final String expectedGuid = "wrongGuid";
		final BundleConstituent bundleCons1 = mock(BundleConstituent.class);
		final BundleConstituent bundleCons2 = mock(BundleConstituent.class);
		final BundleConstituent bundleCons3 = mock(BundleConstituent.class);

		when(bundleCons1.getGuid()).thenReturn("bundleCons1");
		when(bundleCons2.getGuid()).thenReturn("bundleCons2");
		when(bundleCons3.getGuid()).thenReturn("bundleCons3");

		itemRepository.findBundleConstituentWithGuid(Lists.list(bundleCons1, bundleCons2, bundleCons3), expectedGuid)
				.test()
				.assertError(ResourceOperationFailure.notFound(BUNDLE_CONS_NOT_FOUND))
				.assertNoValues();
	}

	private ProductSku createProductSku(final String skuCode) {
		ProductSku productSku = new ProductSkuImpl();
		productSku.setSkuCode(skuCode);
		return productSku;
	}

	private ProductSku createProductSku(final String skuCode, final Product product) {
		ProductSku productSku = createProductSku(skuCode);
		productSku.setProduct(product);
		return productSku;
	}

	private Map<String, String> generateItemIdMap(final String skuCode) {
		return itemRepository.getItemIdMap(skuCode).getValue();
	}
}

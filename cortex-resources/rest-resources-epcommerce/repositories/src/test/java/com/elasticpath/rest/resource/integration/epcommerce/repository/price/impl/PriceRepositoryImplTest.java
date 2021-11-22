/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.impl.StoreProductImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.store.Store;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.prices.OfferPriceRangeEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.store.StoreService;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.TooManyFields", "PMD.GodClass"})
@RunWith(MockitoJUnitRunner.class)
public class PriceRepositoryImplTest {

	private static final String STORE_CODE = "test store code";
	private static final String SKU_CODE = "sku code";
	private static final String PRODUCT_GUID_CODE = "product guid code";
	private static final String ZERO_USD = "0 USD";
	private static final String USD = "USD";
	private static final String SKU_1 = "sku1";
	private static final String SKU_2 = "sku2";
	private static final String SKU_3 = "sku3";

	private static final BigDecimal SKU1_LIST_PRICE = new BigDecimal(2);
	private static final BigDecimal SKU1_SALE_PRICE = new BigDecimal(3);
	private static final BigDecimal OTHER_SKU_SALE_PRICE = new BigDecimal(4);
	private static final BigDecimal OTHER_SKU_LIST_PRICE = new BigDecimal(5);

	@Mock
	private Price price;
	@Mock
	private StoreRepository mockStoreRepository;
	@Mock
	private PriceLookupFacade mockPriceLookupFacade;
	@Mock
	private ShoppingItemDtoFactory mockShoppingItemDtoFactory;
	@Mock
	private ShoppingItemDto mockShoppingItemDto;
	@Mock
	private ShopperRepository mockShopperRepository;
	@Mock
	private Shopper mockShopper;
	@Mock
	private Store mockStore;
	@Mock
	private ProductSkuRepository mockProductSkuRepository;
	@Mock
	private StoreProductRepository storeProductRepository;
	@Mock
	private MoneyTransformer moneyTransformer;
	@Mock
	private BeanFactory coreBeanFactory;
	@Mock
	private StoreProductService storeProductService;
	@Mock
	private StoreService storeService;
	@Mock
	private StoreProduct storeProduct;
	@Mock
	private ProductSku sku1, sku2, sku3;

	private Product product;
	private ProductSkuImpl sku;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private PriceRepositoryImpl priceRepository;

	@Before
	public void setUp() {
		priceRepository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository, mockShopperRepository,
				mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository, reactiveAdapterImpl, coreBeanFactory,
				storeService, storeProductService, moneyTransformer);
	}

	@Test
	public void ensurePriceExistsReturnsTrue() {
		mockPriceResult(price);

		priceRepository.priceExists(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(Boolean.TRUE);
	}

	@Test
	public void ensurePriceExistsForProductReturnsTrue() {
		priceRepositoryWithMultiSkuProductAndPrices(mock(StoreProductRepository.class))
				.priceExistsForProduct(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertNoErrors()
				.assertValue(Boolean.TRUE);
	}

	@Test
	public void ensurePriceExistsReturnsTrueWhenCached() {
		mockPriceResult(price);

		priceRepository.priceExists(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(Boolean.TRUE);
	}

	@Test
	public void ensurePriceDoesNotExistReturnsFalse() {
		mockPriceResult(null);

		priceRepository.priceExists(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(Boolean.FALSE);
	}

	@Test
	public void ensureGetPriceReturnsExpectedPrice() {
		mockPriceResult(price);

		priceRepository.getPrice(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(price);
	}

	@Test
	public void ensureGetPriceReturnsNotFoundForNoPrice() {
		mockPriceResult(null);

		priceRepository.getPrice(STORE_CODE, SKU_CODE)
				.test()
				.assertError(throwable -> checkErrorMessageAndStatus((ResourceOperationFailure) throwable,
						String.format(PriceRepositoryImpl.SKU_PRICE_NOT_FOUND, SKU_CODE)));
	}

	private boolean checkErrorMessageAndStatus(final ResourceOperationFailure failure, final String errorMessage) {
		boolean messageCheck = failure.getLocalizedMessage().equals(errorMessage);
		boolean statusCheck = failure.getResourceStatus().equals(ResourceStatus.NOT_FOUND);
		return messageCheck && statusCheck;
	}

	@Test
	public void ensureGetLowestItemPriceReturnsExpectedPrice() {
		mockLowestPriceResult(price, true);

		priceRepository.getLowestPrice(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(price);
	}

	@Test
	public void ensureGetLowestItemPriceReturnsExpectedPriceWhenCached() {
		mockLowestPriceResult(price, true);

		priceRepository.getLowestPrice(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(price);
	}

	@Test
	public void ensureGetLowestPriceForProductWithNoMultipleSkusReturnsError() {
		mockLowestPriceResult(price, false);

		priceRepository.getLowestPrice(SKU_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

	@Test
	public void ensureGetLowestPriceReturnsNotFoundForNoLowestPrice() {
		mockLowestPriceResult(null, true);

		priceRepository.getLowestPrice(SKU_CODE)
				.test()
				.assertError(throwable -> checkErrorMessageAndStatus((ResourceOperationFailure) throwable,
						String.format(PriceRepositoryImpl.PRODUCT_PRICE_NOT_FOUND, product.getGuid())));
	}

	@Test
	public void ensureGetLowestItemPriceRulesReturnsExpectedRules() {
		mockLowestPriceResult(price, true);
		final long appliedRuleId = 1L;

		final DiscountRecord discountRecord = mock(DiscountRecord.class);
		when(discountRecord.getRuleId()).thenReturn(appliedRuleId);
		when(price.getDiscountRecords()).thenReturn(Collections.singleton(discountRecord));

		Set<Long> appliedRules = new HashSet<>();
		appliedRules.add(appliedRuleId);
		priceRepository.getLowestPriceRules(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(appliedRules);
	}

	@Test
	public void ensureGetLowestItemPriceRulesReturnsExpectedRulesWhenCached() {
		mockLowestPriceResult(price, true);
		final long appliedRuleId = 1L;

		final DiscountRecord discountRecord = mock(DiscountRecord.class);
		when(discountRecord.getRuleId()).thenReturn(appliedRuleId);
		when(price.getDiscountRecords()).thenReturn(Collections.singleton(discountRecord));

		Set<Long> appliedRules = new HashSet<>();
		appliedRules.add(appliedRuleId);
		priceRepository.getLowestPriceRules(STORE_CODE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(appliedRules);
	}

	@Test
	public void testEmptyPriceForPriceRange() {
		mockGetActiveSkusMethodCalls();
		when(storeProduct.isSkuDisplayable(any())).thenReturn(true);

		StoreProductRepository storeProductRepository = mock(StoreProductRepository.class);
		PriceRepository repository = priceRepositoryWithMultiSkuProductAndPrices(storeProductRepository);
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertValue(offerPriceEntity -> offerPriceEntity.getListPriceRange().getFromPrice().size() == 0);
		verify(storeProductRepository, times(1))
				.findDisplayableStoreProductWithAttributesByProductGuid(STORE_CODE, PRODUCT_GUID_CODE);
	}

	@Test
	public void testNonEmptyPriceForPriceRange() {
		mockGetActiveSkusMethodCalls();
		when(storeProduct.isSkuDisplayable(any())).thenReturn(true);

		PriceRepository repository = priceRepositoryWithMultiSkuProductAndPrices(mock(StoreProductRepository.class));
		when(moneyTransformer.transformToEntity(any()))
				.thenReturn(CostEntity.builder().withAmount(BigDecimal.ZERO).withCurrency(USD).withDisplay(ZERO_USD).build());
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertValue(offerPriceEntity -> offerPriceEntity.getListPriceRange().getFromPrice().stream()
						.allMatch(costEntity ->
								ZERO_USD.equals(costEntity.getDisplay())
										&& USD.equals(costEntity.getCurrency())
										&& BigDecimal.ZERO.equals(costEntity.getAmount())
						));
	}

	@Test
	public void testGetPricesForPriceRange() {
		mockGetActiveSkusMethodCalls();
		when(storeProduct.isSkuDisplayable(any())).thenReturn(true);

		PriceRepository repository = priceRepositoryWithMultiSkuProductWhereListPriceIsSmallerThanSalePrice(mock(StoreProductRepository.class));

		stubMoneyTransformerTransformToEntity(SKU1_LIST_PRICE);
		stubMoneyTransformerTransformToEntity(OTHER_SKU_SALE_PRICE);
		stubMoneyTransformerTransformToEntity(OTHER_SKU_LIST_PRICE);

		OfferPriceRangeEntity offerPriceRangeEntity = repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE).blockingGet();

		assertEquals("Invalid lower bound for purchase price", SKU1_LIST_PRICE,
				offerPriceRangeEntity.getPurchasePriceRange().getFromPrice().get(0).getAmount());
		assertEquals("Invalid upper bound for purchase price", OTHER_SKU_SALE_PRICE,
				offerPriceRangeEntity.getPurchasePriceRange().getToPrice().get(0).getAmount());

		assertEquals("Invalid lower bound for list price", SKU1_LIST_PRICE,
				offerPriceRangeEntity.getListPriceRange().getFromPrice().get(0).getAmount());
		assertEquals("Invalid upper bound for list price", OTHER_SKU_LIST_PRICE,
				offerPriceRangeEntity.getListPriceRange().getToPrice().get(0).getAmount());
	}

	private void stubMoneyTransformerTransformToEntity(final BigDecimal bigDecimal) {
		String displayValue = bigDecimal.toString() + " " + USD;
		when(moneyTransformer.transformToEntity(Money.valueOf(bigDecimal, Currency.getInstance(Locale.getDefault()))))
				.thenReturn(CostEntity.builder().withAmount(bigDecimal).withCurrency(USD).withDisplay(displayValue).build());
	}

	@Test
	public void testNoPurchasePriceFallsBackToListPriceForPriceRange() {
		mockGetActiveSkusMethodCalls();
		when(storeProduct.isSkuDisplayable(any())).thenReturn(true);

		PriceRepository repository = priceRepositoryWithNoSalePrice(mock(StoreProductRepository.class));
		when(moneyTransformer.transformToEntity(any()))
				.thenReturn(CostEntity.builder().withAmount(BigDecimal.ZERO).withCurrency(USD).withDisplay(ZERO_USD).build());
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertNoErrors()
				.assertValue(offerPriceRangeEntity -> offerPriceRangeEntity.getListPriceRange()
						.equals(offerPriceRangeEntity.getPurchasePriceRange()));

	}


	@Test
	public void testPriceRangesIgnoreSkusWithoutPrices() {
		mockGetActiveSkusMethodCalls();
		when(storeProduct.isSkuDisplayable(any())).thenReturn(true);

		PriceRepository repository = priceRepositoryWithMissingPricesForSomeSkus(mock(StoreProductRepository.class));
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertNoErrors()
				.assertValue(offerPriceRangeEntity -> offerPriceRangeEntity.getListPriceRange()
						.equals(offerPriceRangeEntity.getPurchasePriceRange()));

	}



	@Test
	public void testPriceRangesIgnoreSkusWhereAvailabilityIsFalse() {
		mockGetActiveSkusMethodCalls();

		when(sku1.getSkuCode()).thenReturn(SKU_1);
		when(sku2.getSkuCode()).thenReturn(SKU_2);
		when(sku3.getSkuCode()).thenReturn(SKU_3);

		when(storeProduct.isSkuDisplayable(sku1.getSkuCode())).thenReturn(false);
		when(storeProduct.isSkuDisplayable(sku2.getSkuCode())).thenReturn(true);
		when(storeProduct.isSkuDisplayable(sku3.getSkuCode())).thenReturn(true);

		PriceRepository repository = priceRepositoryWithMultiSkuProductAndPrices(mock(StoreProductRepository.class));

		BigDecimal bigDecimalTwo = BigDecimal.valueOf(2);
		String currency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
		String display = bigDecimalTwo.toString() + " " + currency;

		when(moneyTransformer.transformToEntity(Money.valueOf(bigDecimalTwo, Currency.getInstance(Locale.getDefault()))))
				.thenReturn(CostEntity.builder().withAmount(bigDecimalTwo).withCurrency(currency).withDisplay(display).build());

		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertNoErrors()
				.assertValue(offerPriceRangeEntity -> offerPriceRangeEntity.getListPriceRange().getFromPrice().get(0).getAmount()
						.equals(offerPriceRangeEntity.getListPriceRange().getToPrice().get(0).getAmount()))
						.equals(bigDecimalTwo);
	}


	private void mockLowestPriceResult(final Price price, final boolean hasMultipleSkus) {

		setupMockStore();
		mockProduct(hasMultipleSkus);
		mockItemRepositoryToReturnSku();
		mockPriceLookupFacadeToReturnPromotedPriceForSku(price);

	}

	private void mockPriceResult(final Price price) {
		setupMockStore();
		setupMockShoppingItemDto(SKU_CODE, 1);
		mockProduct(true);
		mockItemRepositoryToReturnSku();
		mockPriceLookupFacadeToReturnPriceForSku(price);
	}

	private void setupMockStore() {
		when(mockShopperRepository.findOrCreateShopper()).thenReturn(Single.just(mockShopper));
		when(mockShopper.getStoreCode()).thenReturn(STORE_CODE);
		when(mockStoreRepository.findStoreAsSingle(STORE_CODE)).thenReturn(Single.just(mockStore));
	}

	private void setupMockShoppingItemDto(final String skuCode, final int quantity) {
		when(mockShoppingItemDtoFactory.createDto(skuCode, quantity)).thenReturn(mockShoppingItemDto);
	}

	private void mockProduct(final boolean hasMultipleSkus) {
		product = mock(Product.class);
		when(product.getGuid()).thenReturn(PRODUCT_GUID_CODE);
		when(product.hasMultipleSkus()).thenReturn(hasMultipleSkus);
		when(product.getProductSkus()).thenReturn(new HashMap<>());

		sku = new ProductSkuImpl();
		sku.setSkuCode(SKU_CODE);
		sku.setProduct(product);
	}

	private void mockItemRepositoryToReturnSku() {
		when(mockProductSkuRepository.getProductSkuWithAttributesByCode(SKU_CODE)).thenReturn(Single.just(sku));
	}

	private void mockPriceLookupFacadeToReturnPriceForSku(final Price price) {
		when(mockPriceLookupFacade.getShoppingItemDtoPrice(mockShoppingItemDto, mockStore, mockShopper))
				.thenReturn(price);
	}

	private void mockPriceLookupFacadeToReturnPromotedPriceForSku(final Price price) {
		when(mockPriceLookupFacade.getPromotedPriceForProduct(product, mockStore, mockShopper)).thenReturn(price);
	}

	private PriceRepository  priceRepositoryWithMultiSkuProductAndPrices(final StoreProductRepository storeProductRepository) {
		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockShopperRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, storeService, storeProductService, moneyTransformer) {
			@Override
			public Maybe<Price> getStorePriceForSku(final String storeCode, final String skuCode) {
				return Maybe.just(new PriceImpl() {

					@Override
					public Money getListPrice() {
						return Money.valueOf(SKU_1.equals(skuCode) ? 1 : 2, Currency.getInstance(Locale.getDefault()));
					}

					@Override
					public Money getSalePrice() {
						return Money.valueOf(SKU_1.equals(skuCode) ? 1 : 2, Currency.getInstance(Locale.getDefault()));
					}
				});
			}
		};
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(anyString(), any()))
				.thenReturn(Single.just(new StoreProductImpl(new ProductImpl() {
					@Override
					public Map<String, ProductSku> getProductSkus() {
						Map<String, ProductSku> result = new HashMap<>();
						result.put(SKU_1, sku1);
						result.put(SKU_2, sku2);
						result.put(SKU_3, sku3);
						return result;
					}
				})));
		return repository;
	}

	private StoreProduct stubStoreProduct() {
		return new StoreProductImpl(new ProductImpl() {
			@Override
			public Map<String, ProductSku> getProductSkus() {
				Map<String, ProductSku> result = new HashMap<>();
				result.put(SKU_1, sku1);
				result.put(SKU_2, sku2);
				result.put(SKU_3, sku3);
				return result;
			}
		});
	}

	private PriceRepository priceRepositoryWithMultiSkuProductWhereListPriceIsSmallerThanSalePrice(
			final StoreProductRepository storeProductRepository) {

		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockShopperRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, storeService, storeProductService, moneyTransformer) {
			@Override
			public Maybe<Price> getStorePriceForSku(final String storeCode, final String skuCode) {
				return Maybe.just(new PriceImpl() {

					@Override
					public Money getListPrice() {
						return getMoneyValue(skuCode, SKU1_LIST_PRICE, OTHER_SKU_LIST_PRICE);
					}

					@Override
					public Money getSalePrice() {
						return getMoneyValue(skuCode, SKU1_SALE_PRICE, OTHER_SKU_SALE_PRICE);
					}

					@Override
					public Money getLowestPrice() {
						return getMoneyValue(skuCode, SKU1_LIST_PRICE.min(SKU1_SALE_PRICE), OTHER_SKU_LIST_PRICE.min(OTHER_SKU_SALE_PRICE));
					}

				});
			}
		};
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(anyString(), any()))
				.thenReturn(Single.just(stubStoreProduct()));
		return repository;
	}

	private PriceRepository priceRepositoryWithNoSalePrice(final StoreProductRepository storeProductRepository) {
		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockShopperRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, storeService, storeProductService, moneyTransformer) {
			@Override
			public Maybe<Price> getStorePriceForSku(final String storeCode, final String skuCode) {
				return Maybe.just(new PriceImpl() {

					@Override
					public Money getListPrice() {
						return Money.valueOf(SKU_1.equals(skuCode) ? 1 : 2, Currency.getInstance(Locale.getDefault()));
					}

					@Override
					public Money getSalePrice() {
						return null;
					}
				});
			}
		};
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(anyString(), any()))
				.thenReturn(Single.just(new StoreProductImpl(new ProductImpl() {
					@Override
					public Map<String, ProductSku> getProductSkus() {
						Map<String, ProductSku> result = new HashMap<>();
						result.put(SKU_1, new ProductSkuImpl());
						result.put(SKU_2, new ProductSkuImpl());
						result.put(SKU_3, new ProductSkuImpl());
						return result;
					}
				})));
		return repository;
	}

	private PriceRepository priceRepositoryWithMissingPricesForSomeSkus(final StoreProductRepository storeProductRepository) {
		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockShopperRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, storeService, storeProductService, moneyTransformer) {
			@Override
			public Maybe<Price> getStorePriceForSku(final String storeCode, final String skuCode) {
				return Maybe.just(new PriceImpl() {

					@Override
					public Money getListPrice() {
						Money money = null;
						if (SKU_1.equals(skuCode)) {
							money = Money.valueOf(1, Currency.getInstance(Locale.getDefault()));
						} else if (SKU_2.equals(skuCode)) {
							money = Money.valueOf(2, Currency.getInstance(Locale.getDefault()));
						}
						return money;
					}

					@Override
					public Money getSalePrice() {
						return null;
					}
				});
			}
		};
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(anyString(), any()))
				.thenReturn(Single.just(new StoreProductImpl(new ProductImpl() {
					@Override
					public Map<String, ProductSku> getProductSkus() {
						Map<String, ProductSku> result = new HashMap<>();
						result.put(SKU_1, new ProductSkuImpl());
						result.put(SKU_2, new ProductSkuImpl());
						result.put(SKU_3, new ProductSkuImpl());
						return result;
					}
				})));
		return repository;
	}

	private void mockGetActiveSkusMethodCalls() {
		when(mockShopperRepository.findOrCreateShopper()).thenReturn(Single.just(mockShopper));
		when(mockShopper.getStoreCode()).thenReturn("mobee");
		when(storeService.findStoreWithCode("mobee")).thenReturn(mockStore);
		when(coreBeanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenReturn(new PriceImpl());
		when(storeProductService.getProductForStore(any(), any())).thenReturn(storeProduct);
	}

	private Money getMoneyValue(final String skuCode, final BigDecimal sku1Price, final BigDecimal nonSku1Price) {
		if (SKU_1.equals(skuCode)) {
			return Money.valueOf(sku1Price, Currency.getInstance(Locale.getDefault()));
		} else {
			return Money.valueOf(nonSku1Price, Currency.getInstance(Locale.getDefault()));
		}
	}

}

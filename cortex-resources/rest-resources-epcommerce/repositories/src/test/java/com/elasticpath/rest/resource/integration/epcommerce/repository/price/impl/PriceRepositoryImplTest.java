/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static org.mockito.ArgumentMatchers.any;
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
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.store.Store;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;

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
	private CustomerSessionRepository mockCustomerSessionRepository;
	@Mock
	private CustomerSession mockCustomerSession;
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

	private Product product;
	private ProductSkuImpl sku;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private PriceRepositoryImpl priceRepository;

	@Before
	public void setUp() {
		priceRepository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository, mockCustomerSessionRepository,
				mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository, reactiveAdapterImpl, coreBeanFactory, moneyTransformer);
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
		when(coreBeanFactory.getBean(ContextIdNames.PRICE)).thenReturn(new PriceImpl());
		StoreProductRepository storeProductRepository = mock(StoreProductRepository.class);
		PriceRepository repository = priceRepositoryWithMultiSkuProductAndPrices(storeProductRepository);
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertValue(offerPriceEntity -> offerPriceEntity.getListPriceRange().getFromPrice().size() == 0);
		verify(storeProductRepository, times(1)).findByGuid(any());
	}

	@Test
	public void testNonEmptyPriceForPriceRange() {
		when(coreBeanFactory.getBean(ContextIdNames.PRICE)).thenReturn(new PriceImpl());
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
	public void testNoPurchasePriceFallsBackToListPriceForPriceRange() {
		when(coreBeanFactory.getBean(ContextIdNames.PRICE)).thenReturn(new PriceImpl());
		PriceRepository repository = priceRepositoryWithNoSalePrice(mock(StoreProductRepository.class));
		when(moneyTransformer.transformToEntity(any()))
				.thenReturn(CostEntity.builder().withAmount(BigDecimal.ZERO).withCurrency(USD).withDisplay(ZERO_USD).build());
		repository.getPriceRange(STORE_CODE, PRODUCT_GUID_CODE)
				.test()
				.assertNoErrors()
				.assertValue(offerPriceRangeEntity -> offerPriceRangeEntity.getListPriceRange().equals(offerPriceRangeEntity.getPurchasePriceRange()));

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
		when(mockCustomerSessionRepository.findOrCreateCustomerSessionAsSingle()).thenReturn(Single.just(mockCustomerSession));
		when(mockCustomerSession.getShopper()).thenReturn(mockShopper);
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

	private PriceRepository priceRepositoryWithMultiSkuProductAndPrices(final StoreProductRepository storeProductRepository) {
		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockCustomerSessionRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, moneyTransformer) {
			@Override
			public Single<Price> getPrice(final String storeCode, final String skuCode) {
				return Single.just(new PriceImpl() {

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
		when(storeProductRepository.findByGuid(any())).thenReturn(Single.just(new ProductImpl() {
			@Override
			public Map<String, ProductSku> getProductSkus() {
				Map<String, ProductSku> result = new HashMap<>();
				result.put(SKU_1, new ProductSkuImpl());
				result.put(SKU_2, new ProductSkuImpl());
				result.put(SKU_3, new ProductSkuImpl());
				return result;
			}
		}));
		return repository;
	}

	private PriceRepository priceRepositoryWithNoSalePrice(final StoreProductRepository storeProductRepository) {
		PriceRepository repository = new PriceRepositoryImpl(mockShoppingItemDtoFactory, mockStoreRepository,
				mockCustomerSessionRepository, mockPriceLookupFacade, mockProductSkuRepository, storeProductRepository,
				reactiveAdapterImpl, coreBeanFactory, moneyTransformer) {
			@Override
			public Single<Price> getPrice(final String storeCode, final String skuCode) {
				return Single.just(new PriceImpl() {

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
		when(storeProductRepository.findByGuid(any())).thenReturn(Single.just(new ProductImpl() {
			@Override
			public Map<String, ProductSku> getProductSkus() {
				Map<String, ProductSku> result = new HashMap<>();
				result.put(SKU_1, new ProductSkuImpl());
				result.put(SKU_2, new ProductSkuImpl());
				result.put(SKU_3, new ProductSkuImpl());
				return result;
			}
		}));
		return repository;
	}


}

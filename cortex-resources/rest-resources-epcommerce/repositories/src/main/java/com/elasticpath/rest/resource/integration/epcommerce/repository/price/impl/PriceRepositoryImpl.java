/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static io.reactivex.Single.error;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Predicate;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperReference;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.store.Store;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.prices.OfferPriceRangeEntity;
import com.elasticpath.rest.definition.prices.PriceRangeEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.store.StoreService;

/**
 * Repository that consolidates access to price domain concepts.
 */
@Singleton
@Named("priceRepository")
public class PriceRepositoryImpl implements PriceRepository {

	/**
	 * error message when product price not found.
	 */
	public static final String PRODUCT_PRICE_NOT_FOUND = "No price found for product with guid = '%s'";
	/**
	 * error message when sku price not found.
	 */
	public static final String SKU_PRICE_NOT_FOUND = "No price found for sku with code = '%s'";

	private static final int SINGLE_QTY = 1;

	private final ShoppingItemDtoFactory shoppingItemDtoFactory;
	private final StoreRepository storeRepository;
	private final CustomerSessionRepository customerSessionRepository;
	private final PriceLookupFacade priceLookupFacade;
	private final ProductSkuRepository productSkuRepository;
	private final ReactiveAdapter reactiveAdapter;
	private final StoreProductRepository storeProductRepository;
	private final MoneyTransformer moneyTransformer;
	private final BeanFactory coreBeanFactory;
	private final StoreService storeService;

	private final StoreProductService storeProductService;

	private static final BiFunction<Price, Price, Price> MAX_LIST_PRICE_REDUCER = (price, price2) -> ObjectUtils.compare(price.getListPrice(),
			price2.getListPrice()) > 0 ? price : price2;
	private static final BiFunction<Price, Price, Price> MAX_PURCHASE_PRICE_REDUCER = (price, price2) -> ObjectUtils.compare(price.getSalePrice(),
			price2.getSalePrice()) > 0 ? price : price2;
	private static final BiFunction<Price, Price, Price> MIN_LIST_PRICE_REDUCER = (price, price2) -> ObjectUtils.compare(price.getListPrice(),
			price2.getListPrice()) < 0 ? price : price2;
	private static final BiFunction<Price, Price, Price> MIN_PURCHASE_PRICE_REDUCER = (price, price2) -> ObjectUtils.compare(price.getSalePrice(),
			price2.getSalePrice()) < 0 ? price : price2;

	private static final Logger LOG = LoggerFactory.getLogger(PriceRepositoryImpl.class);

	/**
	 * Creates instance with needed services wired in.
	 *
	 * @param shoppingItemDtoFactory    		shopping item dto factory
	 * @param storeRepository           		store repository
	 * @param customerSessionRepository 		customer session repository
	 * @param priceLookupFacade         		price lookup facade
	 * @param productSkuRepository      		product sku repository
	 * @param storeProductRepository    		store product repository
	 * @param reactiveAdapter           		reactive adapter
	 * @param coreBeanFactory           		core bean factory
	 * @param storeService						store service
	 * @param storeProductService				storeProductService
	 * @param moneyTransformer          		money transformer
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	@Inject
	PriceRepositoryImpl(
			@Named("shoppingItemDtoFactory") final ShoppingItemDtoFactory shoppingItemDtoFactory,
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("customerSessionRepository") final CustomerSessionRepository customerSessionRepository,
			@Named("priceLookupFacade") final PriceLookupFacade priceLookupFacade,
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("storeProductRepository") final StoreProductRepository storeProductRepository,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter,
			@Named("coreBeanFactory") final BeanFactory coreBeanFactory,
			@Named("storeService") final StoreService storeService,
			@Named("storeProductService") final StoreProductService storeProductService,
			@Named("moneyTransformer") final MoneyTransformer moneyTransformer) {

		this.shoppingItemDtoFactory = shoppingItemDtoFactory;
		this.storeRepository = storeRepository;
		this.customerSessionRepository = customerSessionRepository;
		this.priceLookupFacade = priceLookupFacade;
		this.productSkuRepository = productSkuRepository;
		this.reactiveAdapter = reactiveAdapter;
		this.coreBeanFactory = coreBeanFactory;
		this.storeProductRepository = storeProductRepository;
		this.storeService = storeService;
		this.storeProductService = storeProductService;
		this.moneyTransformer = moneyTransformer;
	}


	@Override
	@CacheResult(uniqueIdentifier = "getPrice")
	public Single<Price> getPrice(final String storeCode, final String skuCode) {
		return getStorePriceForSku(storeCode, skuCode).switchIfEmpty(
				error(new ResourceOperationFailure(String.format(SKU_PRICE_NOT_FOUND, skuCode), null, ResourceStatus.NOT_FOUND))
		);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getPriceRange")
	public Single<OfferPriceRangeEntity> getPriceRange(final String storeCode, final String guid) {
		Observable<Price> prices = getPrices(storeCode, guid);
		Predicate<Price> listPricePredicate = price -> Objects.nonNull(price.getListPrice());
		Predicate<Price> salePricePredicate = price -> Objects.nonNull(price.getSalePrice());

		Price maxListPrice = getPrice(prices, MAX_LIST_PRICE_REDUCER, listPricePredicate);
		Price maxPurchasePrice = getPrice(prices, MAX_PURCHASE_PRICE_REDUCER, salePricePredicate);
		Price minListPrice = getPrice(prices, MIN_LIST_PRICE_REDUCER, listPricePredicate);
		Price minPurchasePrice = getPrice(prices, MIN_PURCHASE_PRICE_REDUCER, salePricePredicate);


		PriceRangeEntity purchasePriceRange = buildPriceRangeEntity(minPurchasePrice.getSalePrice(), maxPurchasePrice.getSalePrice());
		PriceRangeEntity listPriceRange = buildPriceRangeEntity(minListPrice.getListPrice(), maxListPrice.getListPrice());

		if (Objects.isNull(purchasePriceRange.getFromPrice()) && Objects.isNull(purchasePriceRange.getToPrice())) {
			purchasePriceRange = listPriceRange;
		}
		return Single.just(OfferPriceRangeEntity.builder()
				.withPurchasePriceRange(purchasePriceRange)
				.withListPriceRange(listPriceRange)
				.build());
	}

	private Price getPrice(final Observable<Price> prices, final BiFunction<Price, Price, Price> reducer, final Predicate<Price> pricePredicate) {
		return prices
					.filter(pricePredicate)
					.reduce(reducer)
					.blockingGet(coreBeanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class));
	}

	@CacheResult(uniqueIdentifier = "getPrices")
	private Observable<Price> getPrices(final String storeCode, final String guid) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(storeCode, guid)
				.flatMapObservable(product -> Observable.fromIterable(getAvailableSkus(product).keySet()))
				.flatMapMaybe(sku -> getStorePriceForSku(storeCode, sku));
	}

	private Map<String, ProductSku> getAvailableSkus(final Product product) {
		CustomerSession customerSession = customerSessionRepository.findOrCreateCustomerSession().blockingGet();
		Store store = storeService.findStoreWithCode(customerSession.getShopper().getStoreCode());
		StoreProduct storeProduct = storeProductService.getProductForStore(product, store);
		Map<String, ProductSku> productSkuMap = product.getProductSkus();

		Map<String, ProductSku> activeSkuMap = new HashMap<>();

		for (Map.Entry<String, ProductSku> runner : productSkuMap.entrySet()) {
			ProductSku sku = runner.getValue();
			if (storeProduct.isSkuDisplayable(sku.getSkuCode())) {
				activeSkuMap.put(runner.getKey(), sku);
			}
		}

		return activeSkuMap;
	}

	private PriceRangeEntity buildPriceRangeEntity(final Money minprice, final Money maxprice) {
		final PriceRangeEntity.Builder builder = PriceRangeEntity.builder();
		if (minprice != null) {
			builder.withFromPrice(Collections.singleton(moneyTransformer.transformToEntity(minprice)));
		}
		if (maxprice != null) {
			builder.withToPrice(Collections.singleton(moneyTransformer.transformToEntity(maxprice)));
		}
		return builder.build();
	}

	/**
	 * Get store price for sku.
	 * @param storeCode the store code.
	 * @param skuCode the sku code.
	 * @return the price.
	 */
	protected Maybe<Price> getStorePriceForSku(final String storeCode, final String skuCode) {
		return customerSessionRepository.findOrCreateCustomerSession()
				.map(ShopperReference::getShopper)
				.flatMapMaybe(shopper -> Single.just(shoppingItemDtoFactory.createDto(skuCode, SINGLE_QTY))
						.flatMapMaybe(shoppingItemDto -> getPriceFromShoppingItemDto(storeCode, shopper, shoppingItemDto, skuCode)));
	}

	private Maybe<Price> getPriceFromShoppingItemDto(final String storeCode, final Shopper shopper, final ShoppingItemDto shoppingItemDto,
													 final String skuCode) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMapMaybe(store -> getPriceFromStore(shopper, shoppingItemDto, store)
						.doOnComplete(()-> LOG.warn("Unable to get price for store code '{}' and sku '{}'.", storeCode, skuCode))
				);
	}

	private Maybe<Price> getPriceFromStore(final Shopper shopper, final ShoppingItemDto shoppingItemDto, final Store store) {
		return reactiveAdapter.fromServiceAsMaybe(() -> priceLookupFacade.getShoppingItemDtoPrice(shoppingItemDto, store, shopper));
	}

	@Override
	@CacheResult(uniqueIdentifier = "getLowestPrice")
	public Single<Price> getLowestPrice(final String skuCode) {
		return getLowestPriceImpl(skuCode, true)
				.map(LowestPrice::getPrice);
	}

	@Override
	@CacheResult
	public Single<Set<Long>> getLowestPriceRules(final String storeCode, final String itemId) {
		return getLowestPriceImpl(itemId, false)
				.map(LowestPrice::getRules);
	}

	private Single<LowestPrice> getLowestPriceImpl(final String skuCode, final boolean validateMultipleSkus) {
		return getProduct(skuCode, validateMultipleSkus)
				.flatMap(product -> getPriceUsingPromotedPrice(product)
						.map(this::getLowestPriceAndRules));
	}

	private LowestPrice getLowestPriceAndRules(final Price price) {
		final Collection<DiscountRecord> discountRecords = price.getDiscountRecords();
		final Set<Long> appliedRules = ImmutableSet.copyOf(
				Iterables.transform(discountRecords, createDiscountRecordAppliedRulesFunction()));
		return new LowestPrice(price, appliedRules);
	}

	private Single<Price> getPriceUsingPromotedPrice(final Product product) {
		return customerSessionRepository.findOrCreateCustomerSession()
				.map(ShopperReference::getShopper)
				.flatMap(shopper -> storeRepository.findStoreAsSingle(shopper.getStoreCode())
						.flatMap(store -> getPriceSingleForProduct(product, shopper, store)));
	}

	private Single<Price> getPriceSingleForProduct(final Product product, final Shopper shopper, final Store store) {
		return reactiveAdapter.fromNullableAsSingle(() -> priceLookupFacade
				.getPromotedPriceForProduct(product, store, shopper), String.format(PRODUCT_PRICE_NOT_FOUND, product.getGuid()));
	}

	private Single<Product> getProduct(final String skuCode, final boolean validateMultipleSkus) {
		return productSkuRepository.getProductSkuWithAttributesByCode(skuCode).flatMap(productSku -> {
			Product product = productSku.getProduct();
			assert product != null : "product must not be null";
			assert product.getGuid() != null : "product guid must not be null.";
			if (validateMultipleSkus && !product.hasMultipleSkus()) {
				return error(ResourceOperationFailure.notFound());
			}
			return Single.just(product);
		});
	}

	@Override
	@CacheResult(uniqueIdentifier = "priceExists")
	public Single<Boolean> priceExists(final String storeCode, final String skuCode) {
		return getPrice(storeCode, skuCode)
				.map(price -> Boolean.TRUE)
				.onErrorReturn(throwable -> Boolean.FALSE);
	}

	@Override
	public Single<Boolean> priceExistsForProduct(final String storeCode, final String productGuid) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(storeCode, productGuid)
				.flatMapObservable(product -> Observable.fromIterable(product.getProductSkus().keySet()))
				.flatMapSingle(skuCode -> priceExists(storeCode, skuCode))
				.reduce((Boolean firstExists, Boolean secondExists) -> firstExists || secondExists)
				.toSingle(false);
	}

	/**
	 * Factory method for creating a function to transform from a {@link DiscountRecord} to an applied rule ID.
	 *
	 * @return a function to transform from a {@link DiscountRecord} to an applied rule ID.
	 */
	protected Function<? super DiscountRecord, Long> createDiscountRecordAppliedRulesFunction() {
		return new DiscountRecordToAppliedRuleIdFunction();
	}

	/**
	 * Named immutable lowest price pair.
	 */
	static class LowestPrice {
		private final Price price;
		private final Set<Long> rules;

		/**
		 * constructor.
		 *
		 * @param price price
		 * @param rules rules
		 */
		LowestPrice(final Price price, final Set<Long> rules) {
			this.price = price;
			this.rules = rules;
		}

		public Set<Long> getRules() {
			return rules;
		}

		public Price getPrice() {

			return price;
		}
	}

	/**
	 * A function to transform from a {@link DiscountRecord} to an applied rule ID.
	 */
	private static final class DiscountRecordToAppliedRuleIdFunction implements Function<DiscountRecord, Long> {
		@Override
		public Long apply(final DiscountRecord input) {
			return input.getRuleId();
		}
	}

}

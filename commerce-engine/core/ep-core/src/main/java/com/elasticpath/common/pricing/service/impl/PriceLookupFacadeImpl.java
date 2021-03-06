/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.common.pricing.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.pricing.DisplayPriceDTO;
import com.elasticpath.common.dto.pricing.impl.DisplayPriceDTOImpl;
import com.elasticpath.common.pricing.service.BundleShoppingItemPriceBuilder;
import com.elasticpath.common.pricing.service.BundleShoppingItemPriceBuilderFactory;
import com.elasticpath.common.pricing.service.PriceListLookupService;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.common.pricing.service.PromotedPriceLookupService;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.pricing.BaseAmountObjectType;
import com.elasticpath.domain.pricing.PriceAdjustment;
import com.elasticpath.domain.pricing.PriceListDescriptor;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.pricing.listeners.PricingEventListener;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.sellingchannel.director.ShoppingItemAssembler;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.pricing.PriceListDescriptorService;
import com.elasticpath.service.pricing.PriceLookupService;
import com.elasticpath.tags.TagSet;

/**
 * It uses the {@link PriceLookupService} to find prices now.
 */
@SuppressWarnings({"PMD.GodClass"})
public class PriceLookupFacadeImpl implements PriceLookupFacade {

	private static final Logger LOG = LogManager.getLogger(PriceLookupFacadeImpl.class);

	private PriceLookupService priceLookupService;

	private PromotedPriceLookupService promotedPriceLookupService;

	private PriceListLookupService priceListLookupService;

	private PriceListDescriptorService priceListDescriptorService;

	private BeanFactory beanFactory;

	private BundleShoppingItemPriceBuilderFactory bundleShoppingItemPriceBuilderFactory;

	private List<PricingEventListener<? super Entity>> listenerList;
	
	private ShoppingItemAssembler shoppingItemAssembler;
	private ProductSkuLookup productSkuLookup;

	private BundleIdentifier bundleIdentifier;

	@Override
	public Map<String, PriceAdjustment> getPriceAdjustmentsForBundle(final ProductBundle bundle, final String catalogCode,
			final Shopper shopper) {

		final PriceListStack priceListStackFromSession = getPriceListStackFromSession(catalogCode, shopper);

		final String plGuid = priceLookupService.findPriceListWithPriceForProductSku(bundle.getDefaultSku(), priceListStackFromSession);
		if (plGuid == null) {
			return Collections.emptyMap();
		}

		return priceLookupService.getProductBundlePriceAdjustmentsMap(bundle, plGuid);
	}

	@Override
	public Price getPromotedPriceForSku(final ProductSku productSku, final Store store,
										final Shopper shopper) {
		invokeListeners(productSku);
		final PriceListStack plStack = getPriceListStackFromSession(store.getCatalog().getCode(), shopper);
		return getPromotedPriceLookupService().getSkuPrice(productSku, plStack, store, shopper.getTagSet());
	}

	@Override
	public Price getPromotedPriceForProduct(final Product product, final Store store,
											final Shopper shopper) {
		invokeListeners(product);
		final PriceListStack plStack = getPriceListStackFromSession(store.getCatalog().getCode(), shopper);
		return getPromotedPriceLookupService().getProductPrice(product, plStack, store, shopper.getTagSet());
	}



	@Override
	public Map<String, Price> getPromotedPricesForProducts(final Collection<Product> products, final Store store,
															final Shopper shopper) {
		invokeListeners(products);
		final PriceListStack plStack = getPriceListStackFromSession(store.getCatalog().getCode(), shopper);
		return getPromotedPriceLookupService().getProductsPrices(products, plStack, store, shopper.getTagSet());
	}


	@Override
	public List<DisplayPriceDTO> getPricesForOrderSku(final ProductSku productSku, final PriceListStack stack,
													  final Integer tierQuantity, final Store store, final TagSet tagSet) {
		final Map<String, Price> prices = getPromotedPriceLookupService().getSkuPrices(productSku, stack, store, tagSet);

		if (prices.isEmpty()) {
			return Collections.emptyList();
		}

		final List<DisplayPriceDTO> dtos = new ArrayList<>();

		for (final Map.Entry<String, Price> entry : prices.entrySet()) {
			final String plGuid = entry.getKey();
			final Price price = entry.getValue();

			final PriceListDescriptor pld = priceListDescriptorService.findByGuid(plGuid);

			if (pld == null) {
				LOG.error("No price list found matching guid: " + plGuid);
				continue;
			}

			if (tierQuantity == null) {
				addEachTierAsDisplayPriceDTO(dtos, pld, productSku, price);
			} else {
				addTiersThatComplyWithQuantityAsDisplayPriceDTO(dtos, pld, productSku,
						price, tierQuantity);
			}

		}
		return dtos;

	}

	/**
	 * add only those tiers to dtos that have tier quantity less or equal to the tierQuantity provided.
	 * @param dtos the dtos to be returned
	 * @param pld the price list
	 * @param productSku the sku for which to get the prices
	 * @param price the price for this sku
	 * @param tierQuantity the tier quantity
	 */
	void addTiersThatComplyWithQuantityAsDisplayPriceDTO(
			final List<DisplayPriceDTO> dtos, final PriceListDescriptor pld,
			final ProductSku productSku, final Price price,
			final Integer tierQuantity) {

		for (final PriceTier tier : price.getPriceTiers().values()) { // the priceTiers set should be sorted by ascending qty

			if (tier.getMinQty() > tierQuantity) {
				break;
			}


			final DisplayPriceDTO dto = new DisplayPriceDTOImpl(
					pld.getName(),
					pld.getGuid(),
					productSku.getGuid(),
					BaseAmountObjectType.SKU.toString(),
					tier.getListPrice(),
					new BigDecimal(tier.getMinQty()),
					tier.getSalePrice());
			dto.setLowestPrice(price.getLowestPrice(tier.getMinQty()));
			dtos.add(dto);
		}
	}

	/**
	 * add all tiers to dtos from price.
	 * @param dtos the dtos to be returned
	 * @param pld the price list
	 * @param productSku the sku for which to get the prices
	 * @param price the price for this sku
	 */
	void addEachTierAsDisplayPriceDTO(final List<DisplayPriceDTO> dtos,
			final PriceListDescriptor pld, final ProductSku productSku,
			final Price price) {
		for (final PriceTier tier : price.getPriceTiers().values()) {

			final DisplayPriceDTO dto = new DisplayPriceDTOImpl(
					pld.getName(),
					pld.getGuid(),
					productSku.getGuid(),
					BaseAmountObjectType.SKU.toString(),
					tier.getListPrice(),
					new BigDecimal(tier.getMinQty()),
					tier.getSalePrice());
			dto.setLowestPrice(price.getLowestPrice(tier.getMinQty()));
			dtos.add(dto);
		}
	}


	/**
	 * @param product product
	 * @param store store
	 * @param currency currency
	 * @param priceForSku price for sku
	 * @param tagSet set of tags within customer session
	 */
	@Override
	public void applyCatalogPromotions(final Product product, final Store store, final Currency currency,
									   final Price priceForSku, final TagSet tagSet) {
		getPromotedPriceLookupService().applyCatalogPromotions(product, store, currency, priceForSku, tagSet);
	}

	/**
	 * @param priceLookupService the priceLookupService to set
	 */
	public void setPriceLookupService(final PriceLookupService priceLookupService) {
		this.priceLookupService = priceLookupService;
	}

	/**
	 * @param priceListLookupService {@code PriceListLookupService}
	 */
	public void setPriceListLookupService(
			final PriceListLookupService priceListLookupService) {
		this.priceListLookupService = priceListLookupService;
	}

	/**
	 * @param priceListDescriptorService {@code PriceListDescriptorService}
	 */
	public void setPriceListDescriptorService(
			final PriceListDescriptorService priceListDescriptorService) {
		this.priceListDescriptorService = priceListDescriptorService;
	}

	/**
	 * Calculates the price of a shopping item, considering the selections inside bundles.
	 *
	 * @param shoppingItem the shopping item
	 * @param store        the store
	 * @param shopper      the customer session
	 * @return the calculated price
	 */
	@Override
	public Price getShoppingItemPrice(final ShoppingItem shoppingItem, final Store store, final Shopper shopper) {

		if (shoppingItem.isBundle(getProductSkuLookup())) {
			final BundleShoppingItemPriceBuilder bundleShoppingItemPriceBuilder =
					bundleShoppingItemPriceBuilderFactory.createBundleShoppingItemPriceBuilder(shoppingItem, this, beanFactory);

			return bundleShoppingItemPriceBuilder.build(shoppingItem, shopper, store);
		}

		final ProductSku sku = getProductSkuLookup().findByGuid(shoppingItem.getSkuGuid());
		return getPromotedPriceForSku(sku, store, shopper);
	}
	
	/**
	 * Calculates the price of a shopping item DTO, considering the selections inside bundles.
	 * 
	 * @param shoppingItemDto the shopping item
	 * @param store the store
	 * @param shopper the customer session
	 * @return the calculated price
	 */
	@Override
	public Price getShoppingItemDtoPrice(final ShoppingItemDto shoppingItemDto, final Store store, final Shopper shopper) {
		ShoppingItem shoppingItem = getShoppingItemAssembler().createShoppingItem(shoppingItemDto);
		return getShoppingItemPrice(shoppingItem, store, shopper);
	}

	@Override
	public boolean hasPriceForSku(final ProductSku productSku, final Store store, final Shopper shopper) {
		if (bundleIdentifier.isCalculatedBundle(productSku)) {
			return true;
		}

		final PriceListStack plStack = getPriceListStackFromSession(store.getCatalog().getCode(), shopper);
		Price skuPrice = priceLookupService.getSkuPrice(productSku, plStack);

		return skuPrice != null;
	}

	private void invokeListeners(final Entity entity) {
		if (!CollectionUtils.isEmpty(listenerList)) {
			for (final PricingEventListener<? super Entity> listener : listenerList) {
				listener.execute(entity);
			}
		}
	}

	private void invokeListeners(final Collection<Product> products) {
		for (final Product product : products) {
			invokeListeners(product);
		}
	}

	/**
	 * @param listenerList the listenerList to set
	 */
	public void setListenerList(final List<PricingEventListener<? super Entity>> listenerList) {
		this.listenerList = listenerList;
	}

	/**
	 * @return the listenerList
	 */
	protected List<PricingEventListener<? super Entity>> getListenerList() {
		return listenerList;
	}


	/**
	 * @param catalogCode the catalog
	 * @param shopper {@link Shopper}
	 * @return {@link PriceListStack}
	 */
	protected PriceListStack getPriceListStackFromSession(final String catalogCode, final Shopper shopper) {
		if (shopper.isPriceListStackValid()) {
			return shopper.getPriceListStack();
		}
		final PriceListStack stack = priceListLookupService.getPriceListStack(catalogCode, shopper.getCurrency(), shopper.getTagSet());
		shopper.setPriceListStack(stack);
		return stack;
	}


	/**
	 * Sets the bean factory.
	 * 
	 * @param beanFactory the bean factory
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @param bundleShoppingItemPriceBuilderFactory the bundleShoppingItemPriceBuilderFactory to set
	 */
	public void setBundleShoppingItemPriceBuilderFactory(
			final BundleShoppingItemPriceBuilderFactory bundleShoppingItemPriceBuilderFactory) {
		this.bundleShoppingItemPriceBuilderFactory = bundleShoppingItemPriceBuilderFactory;
	}


	public void setPromotedPriceLookupService(final PromotedPriceLookupService promotedPriceLookupService) {
		this.promotedPriceLookupService = promotedPriceLookupService;
	}

	protected PromotedPriceLookupService getPromotedPriceLookupService() {
		return promotedPriceLookupService;
	}

	public void setShoppingItemAssembler(final ShoppingItemAssembler shoppingItemAssembler) {
		this.shoppingItemAssembler = shoppingItemAssembler;
	}

	protected ShoppingItemAssembler getShoppingItemAssembler() {
		return shoppingItemAssembler;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	public BundleIdentifier getBundleIdentifier() {
		return bundleIdentifier;
	}

	public void setBundleIdentifier(final BundleIdentifier bundleIdentifier) {
		this.bundleIdentifier = bundleIdentifier;
	}
}

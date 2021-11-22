/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartPostProcessor;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;
import com.elasticpath.xpf.context.builders.ShoppingCartValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Abstract Ep strategy for multicarts (b2c, b2b etc).
 */
@Singleton
@Named("b2bMulticartStrategy")
public abstract class AbstractEpMultiCartStrategyImpl implements MultiCartResolutionStrategy {

	@Inject
	private ShoppingCartService shoppingCartService;
	@Inject
	private ShopperRepository shopperRepository;
	@Inject
	private CartPostProcessor cartPostProcessor;
	@Inject
	private ReactiveAdapter reactiveAdapter;
	@Inject
	private StoreService storeService;
	@Inject
	private ExceptionTransformer exceptionTransformer;
	@Inject
	private ResourceOperationContext resourceOperationContext;
	@Inject
	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;
	@Inject
	private ShoppingCartValidationContextBuilder shoppingCartValidationContextBuilder;
	@Inject
	private XPFExtensionLookup extensionLookup;
	@Inject
	private StructuredErrorMessageConverter structuredErrorMessageConverter;

	@Override
	 public Single<ShoppingCart> getShoppingCartSingle(final String cartGuid) {
		return shopperRepository.findOrCreateShopper()
				.flatMap(shopper -> getShoppingCartSingle(cartGuid, shopper));
	}

	/**
	 * Gets the shopping cart single for cart and customer session.
	 * @param cartGuid the cart guid
	 * @param shopper the shopper
	 * @return the shopper wrapped in a single
	 */
	protected Single<ShoppingCart> getShoppingCartSingle(final String cartGuid, final Shopper shopper) {
		return getCartByGuid(cartGuid).flatMap(cart -> reactiveAdapter.fromServiceAsSingle(() -> {
			cartPostProcessor.postProcessCart(cart, shopper);
			return cart;
		}));
	}

	/**
	 * Gets the cart by guid.
	 * @param cartGuid the cart guid.
	 * @return the cart as a single.
	 */
	protected Single<ShoppingCart> getCartByGuid(final String cartGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> shoppingCartService.findByGuid(cartGuid));
	}


	@Override
	public List<ModifierField> getModifierFields(final String storeCode) {
		Store store = storeService.findStoreWithCode(storeCode);
		List<ModifierField> modifierFieldsList = new ArrayList<>();
		store.getShoppingCartTypes().stream().findFirst().orElseThrow(() ->new EpServiceException("No Cart Type found"))
				.getModifiers().stream().map(ModifierGroup::getModifierFields)
				.forEach(modifierFieldsList::addAll);
		return modifierFieldsList;
	}

	/**
	 * Internal create cart method. Validates cart can be created and then returns a newly created cart.
	 * @param shopper the shopper
	 * @param descriptors the descriptors that describe the cart
	 * @return a newly created cart
	 */
	protected Single<ShoppingCart> createCartInternal(final Shopper shopper, final Map<String, String> descriptors) {

		final ShoppingCart cart = getShoppingCartService().createByShopper(shopper);
		cart.getModifierFields().putAll(descriptors);

		try {
			validateCreateOrUpdate(cart);
		} catch (EpStructureErrorMessageException exception) {

			return Single.error(exceptionTransformer.getResourceOperationFailure(
					new EpValidationException("Cannot Create Cart", exception.getStructuredErrorMessages())));
		}

		final ShoppingCart savedCart = getShoppingCartService().saveOrUpdate(cart);

		getCartPostProcessor().postProcessCart(savedCart, shopper);
		return Single.just(savedCart);
	}

	@Override
	public void validateCreateOrUpdate(final ShoppingCart shoppingCart) {
		final XPFShoppingCartValidationContext context = shoppingCartValidationContextBuilder.build(shoppingCart);

		XPFExtensionSelector xpfExtensionSelector = new XPFExtensionSelectorByStoreCode(shoppingCart.getShopper().getStoreCode());
		final Collection<StructuredErrorMessage> validationMessages
				= extensionLookup.getMultipleExtensions(ShoppingCartValidator.class,
				XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CART_CREATE_OR_UPDATE,
				xpfExtensionSelector)
				.stream()
				.map(strategy -> strategy.validate(context))
						.flatMap(Collection::stream)
				.map(structuredErrorMessageConverter::convert)
						.collect(Collectors.toList());

		if (!validationMessages.isEmpty()) {
			throw new EpStructureErrorMessageException("Create cart validation failure.",
					ImmutableList.copyOf(validationMessages));
		}

	}

	/**
	 * Checks that there is a multicart type associated with the given store.
	 * @return true if it has a multicart type for the store, false otherwise.
	 * @param storeCode the storecode.
	 */
	@Override
	public boolean hasMulticartEnabled(final String storeCode) {
		return getStoreService().getCartTypeNamesForStore(storeCode)
				.stream().anyMatch(storeCartTypeName -> getValidCartTypeForStrategy(storeCode).equals(storeCartTypeName));

	}

	/**
	 * 	returns the cart type associated with the strategy.
	 *
	 * @param storeCode the store code
	 * @return the cart type name.
	 */
	protected String getValidCartTypeForStrategy(final String storeCode) {
		return multicartItemListTypeLocationProvider.getMulticartItemListTypeForStore(storeCode);
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	protected ShopperRepository getShopperRepository() {
		return shopperRepository;
	}

	protected CartPostProcessor getCartPostProcessor() {
		return cartPostProcessor;
	}

	protected ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	protected ExceptionTransformer getExceptionTransformer() {
		return exceptionTransformer;
	}

	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	public void setShopperRepository(final ShopperRepository shopperRepository) {
		this.shopperRepository = shopperRepository;
	}

	public void setCartPostProcessor(final CartPostProcessor cartPostProcessor) {
		this.cartPostProcessor = cartPostProcessor;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setExceptionTransformer(final ExceptionTransformer exceptionTransformer) {
		this.exceptionTransformer = exceptionTransformer;
	}

	public void setMulticartItemListTypeLocationProvider(final MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider) {
		this.multicartItemListTypeLocationProvider = multicartItemListTypeLocationProvider;
	}

	public ResourceOperationContext getResourceOperationContext() {
		return resourceOperationContext;
	}

	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	public void setStructuredErrorMessageConverter(final StructuredErrorMessageConverter structuredErrorMessageConverter) {
		this.structuredErrorMessageConverter = structuredErrorMessageConverter;
	}
}

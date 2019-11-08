/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartPostProcessor;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.validation.CreateShoppingCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.store.StoreService;

/**
 * Abstract Ep strategy for multicarts (b2c, b2b etc).
 */
@Singleton
@Named("b2bMulticartStrategy")
public abstract class AbstractEpMultiCartStrategyImpl implements MultiCartResolutionStrategy {

	@Inject
	private ShoppingCartService shoppingCartService;
	@Inject
	private CustomerSessionRepository customerSessionRepository;
	@Inject
	private CartPostProcessor cartPostProcessor;
	@Inject
	private ReactiveAdapter reactiveAdapter;
	@Inject
	private CreateShoppingCartValidationService createShoppingCartValidationService;
	@Inject
	private StoreService storeService;
	@Inject
	private ExceptionTransformer exceptionTransformer;
	@Inject
	private ResourceOperationContext resourceOperationContext;


	@Override
	 public Single<ShoppingCart> getShoppingCartSingle(final String cartGuid) {
		return customerSessionRepository.findOrCreateCustomerSessionAsSingle()
				.flatMap(customerSession -> getShoppingCartSingle(cartGuid, customerSession));
	}

	/**
	 * Gets the shopping cart single for cart and customer sessoin.
	 * @param cartGuid the cart guid.
	 * @param customerSession the customer session.
	 * @return the shopper  wrapped in a single.
	 */
	protected Single<ShoppingCart> getShoppingCartSingle(final String cartGuid, final CustomerSession customerSession) {
		return getCartByGuid(cartGuid).flatMap(cart -> reactiveAdapter.fromServiceAsSingle(() -> {
			cartPostProcessor.postProcessCart(cart, cart.getShopper(), customerSession);
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
	 * @param customerSession the customer session.
	 * @param descriptors the descriptors that describe the cart.
	 * @return a newly created cart.
	 */
	protected Single<ShoppingCart> createCartInternal(final CustomerSession customerSession, final Map<String, String> descriptors) {

		final ShoppingCart cart = getShoppingCartService().createByCustomerSession(customerSession);
		descriptors.forEach(cart::setCartDataFieldValue);

		try {
			validateCreate(cart);
		} catch (EpStructureErrorMessageException exception) {

			return Single.error(exceptionTransformer.getResourceOperationFailure(
					new EpValidationException("Cannot Create Cart", exception.getStructuredErrorMessages())));
		}

		cart.setShopper(customerSession.getShopper());

		final ShoppingCart savedCart = getShoppingCartService().saveOrUpdate(cart);

		getCartPostProcessor().postProcessCart(savedCart, customerSession.getShopper(), customerSession);
		return Single.just(savedCart);
	}

	@Override
	public void validateCreate(final ShoppingCart shoppingCart) {
		final ShoppingCartValidationContext validationContext = getCreateShoppingCartValidationService().buildContext(shoppingCart);
		Collection<StructuredErrorMessage> validationMessages = getCreateShoppingCartValidationService().validate(validationContext);
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
				.stream().anyMatch(storeCartTypeName -> getValidCartTypeForStrategy().equals(storeCartTypeName));

	}



	/**
	 * returns the cart type associated with the strategy.
	 * @return the cart type name.
	 */
	protected abstract String getValidCartTypeForStrategy();


	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	protected CustomerSessionRepository getCustomerSessionRepository() {
		return customerSessionRepository;
	}

	protected CartPostProcessor getCartPostProcessor() {
		return cartPostProcessor;
	}

	protected ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}

	protected CreateShoppingCartValidationService getCreateShoppingCartValidationService() {
		return createShoppingCartValidationService;
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

	public void setCustomerSessionRepository(final CustomerSessionRepository customerSessionRepository) {
		this.customerSessionRepository = customerSessionRepository;
	}

	public void setCartPostProcessor(final CartPostProcessor cartPostProcessor) {
		this.cartPostProcessor = cartPostProcessor;
	}

	public void setCreateShoppingCartValidationService(final CreateShoppingCartValidationService createShoppingCartValidationService) {
		this.createShoppingCartValidationService = createShoppingCartValidationService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setExceptionTransformer(final ExceptionTransformer exceptionTransformer) {
		this.exceptionTransformer = exceptionTransformer;
	}

	public ResourceOperationContext getResourceOperationContext() {
		return resourceOperationContext;
	}

	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}

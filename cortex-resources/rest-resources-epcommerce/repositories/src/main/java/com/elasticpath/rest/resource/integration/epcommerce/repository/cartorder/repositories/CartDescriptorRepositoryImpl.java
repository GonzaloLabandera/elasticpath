/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Repository for cart descriptors.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CartDescriptorRepositoryImpl<E extends CartDescriptorEntity,
		I extends CartDescriptorIdentifier>
implements Repository<CartDescriptorEntity, CartDescriptorIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;

	private ShoppingCartService shoppingCartService;

	private MultiCartResolutionStrategyHolder multiCartResolutionStrategyHolder;

	private ResourceOperationContext resourceOperationContext;

	private ExceptionTransformer exceptionTransformer;


	@Override
	public Single<CartDescriptorEntity> findOne(final CartDescriptorIdentifier identifier) {
		return Single.just(getCartDescriptors(identifier));
	}

	@Override
	public Completable update(final CartDescriptorEntity entity, final CartDescriptorIdentifier identifier) {


		String cartGuid = identifier.getCart().getCartId().getValue();
		ShoppingCart shoppingCart = shoppingCartService.findByGuid(cartGuid);
		Map<String, String> identifiers = entity.getDynamicProperties();
		for (Map.Entry<String, String> entry : identifiers.entrySet()) {
			shoppingCart.setCartDataFieldValue(entry.getKey(), entry.getValue());
		}
		try {
			getStrategy().validateCreate(shoppingCart);
		} catch (EpStructureErrorMessageException exception) {
			return Completable.error(exceptionTransformer.getResourceOperationFailure(
					new EpValidationException("Cannot Update Cart Identifier", exception.getStructuredErrorMessages())));
		}

			shoppingCartService.saveOrUpdate(shoppingCart);
			return Completable.complete();
	}


	private MultiCartResolutionStrategy getStrategy() {
		MultiCartResolutionStrategy strategy = null;
		for (MultiCartResolutionStrategy cartResolutionStrategy : this.multiCartResolutionStrategyHolder.getStrategies()) {
			if (cartResolutionStrategy.isApplicable(resourceOperationContext.getSubject())) {
				strategy = cartResolutionStrategy;
			}
		}
		assert strategy != null;
		return strategy;
	}
	private CartDescriptorEntity getCartDescriptors(final CartDescriptorIdentifier identifier) {
		Map<String, CartData> cartDescriptors =
				shoppingCartRepository.getCartDescriptors(identifier.getCart().getCartId().getValue());

		CartDescriptorEntity.Builder identifierBuilder = CartDescriptorEntity.builder();

		cartDescriptors.values().stream()
				.filter(data -> !ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_IDENTIFIERS.contains(data.getKey()))
				.forEach(value -> identifierBuilder.addingProperty(value.getKey(), value.getValue()));

		if (shoppingCartRepository.getDefaultShoppingCartGuid().blockingGet()
				.equals(identifier.getCart().getCartId().getValue())) {
			identifierBuilder.addingProperty("default", "true");
		}

		return identifierBuilder.build();
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Reference
	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	@Reference
	public void setMultiCartResolutionStrategyHolder(final MultiCartResolutionStrategyHolder multiCartResolutionStrategyHolder) {
		this.multiCartResolutionStrategyHolder = multiCartResolutionStrategyHolder;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setExceptionTransformer(final ExceptionTransformer exceptionTransformer) {
		this.exceptionTransformer = exceptionTransformer;
	}
}

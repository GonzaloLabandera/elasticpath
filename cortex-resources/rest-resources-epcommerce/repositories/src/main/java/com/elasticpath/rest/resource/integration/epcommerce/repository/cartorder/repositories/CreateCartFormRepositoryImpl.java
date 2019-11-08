/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.List;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CreateCartFormEntity;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CreateCartFormRepositoryImpl<E extends CartDescriptorEntity, I extends CreateCartFormIdentifier>
		implements Repository<CreateCartFormEntity, CreateCartFormIdentifier> {


	private MultiCartResolutionStrategyHolder strategyHolder;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<CreateCartFormEntity> findOne(final CreateCartFormIdentifier identifier) {

		String storeCode = identifier.getCarts().getScope().getValue();

		if (!getStrategy().hasMulticartEnabled(storeCode)) {
			return Single.error(ResourceOperationFailure.stateFailure(ShoppingCartResourceConstants.CREATE_CART_NOT_SUPPORTED));
		}
		CartDescriptorEntity.Builder descriptorBuilder = CartDescriptorEntity.builder();

		getModifierFieldsFromStrategy(storeCode)
				.forEach(modifierField -> descriptorBuilder.addingProperty(modifierField.getCode(), ""));

		return Single.just(CreateCartFormEntity.builder()
				.withDescriptor(descriptorBuilder.build())
				.build());
	}


	private List<ModifierField> getModifierFieldsFromStrategy(final String storeCode) {

		return getStrategy().getModifierFields(storeCode);
	}

	private MultiCartResolutionStrategy getStrategy() {
		MultiCartResolutionStrategy strategy = null;
		for (MultiCartResolutionStrategy cartResolutionStrategy : this.strategyHolder.getStrategies()) {
			if (cartResolutionStrategy.isApplicable(resourceOperationContext.getSubject())) {
				strategy = cartResolutionStrategy;
			}
		}
		assert strategy != null;
		return strategy;
	}

	@Reference
	public void setStrategyHolder(final MultiCartResolutionStrategyHolder strategyHolder) {
		this.strategyHolder = strategyHolder;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

}

/*
 *  Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.promotions.applied;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.promotions.PromotionIdentifierUtil
		.buildPromotionIdentifiers;

import java.util.Collection;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.promotions.AppliedPromotionsForShippingOptionIdentifier;
import com.elasticpath.rest.definition.promotions.PromotionIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.PromotionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;

/**
 * Repository that implements reading applied promotions for shippingoption.
 *
 * @param <I>	extends AppliedPromotionsForShippingOptionIdentifier
 * @param <IE>	extends PromotionIdentifier
 */
@Component
public class AppliedPromotionsForShippingOptionRepositoryImpl<I extends AppliedPromotionsForShippingOptionIdentifier,
		IE extends PromotionIdentifier> implements LinksRepository<AppliedPromotionsForShippingOptionIdentifier, PromotionIdentifier> {

	private CartOrderRepository cartOrderRepository;
	private ShippingServiceLevelRepository shippingServiceLevelRepository;
	private PricingSnapshotRepository pricingSnapshotRepository;
	private PromotionRepository promotionRepository;

	@Override
	public Observable<PromotionIdentifier> getElements(final AppliedPromotionsForShippingOptionIdentifier identifier) {
		ShippingOptionIdentifier shippingOptionIdentifier = identifier.getShippingOption();
		String scope = shippingOptionIdentifier.getScope().getValue();
		Map<String, String> shipmentDetailsId = shippingOptionIdentifier.getShipmentDetailsId().getValue();
		String shippingOptionId = shippingOptionIdentifier.getShippingOptionId().getValue();
		return cartOrderRepository.getEnrichedShoppingCartForShipments(scope, shipmentDetailsId)
				.flatMap(shoppingCart -> getAppliedPromotions(shippingOptionId, shoppingCart))
				.flatMapObservable(appliedPromotions -> buildPromotionIdentifiers(scope, appliedPromotions));
	}

	private Single<Collection<String>> getAppliedPromotions(final String shippingOptionId, final ShoppingCart shoppingCart) {
		return pricingSnapshotRepository.getShoppingCartPricingSnapshotSingle(shoppingCart)
				.flatMap(cartPricingSnapshot -> getShippingPromotions(shippingOptionId,
						Observable.fromIterable(shoppingCart.getShippingServiceLevelList()), cartPricingSnapshot));
	}

	private Single<Collection<String>> getShippingPromotions(final String shippingOptionId,
															 final Observable<ShippingServiceLevel> shippingServiceLevels,
															 final ShoppingCartPricingSnapshot cartPricingSnapshot) {
		return shippingServiceLevelRepository.getShippingServiceLevel(shippingOptionId, shippingServiceLevels)
				.map(shippingServiceLevel -> promotionRepository.getAppliedShippingPromotions(cartPricingSnapshot, shippingServiceLevel));
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setShippingServiceLevelRepository(final ShippingServiceLevelRepository shippingServiceLevelRepository) {
		this.shippingServiceLevelRepository = shippingServiceLevelRepository;
	}

	@Reference
	public void setPricingSnapshotRepository(final PricingSnapshotRepository pricingSnapshotRepository) {
		this.pricingSnapshotRepository = pricingSnapshotRepository;
	}

	@Reference
	public void setPromotionRepository(final PromotionRepository promotionRepository) {
		this.promotionRepository = promotionRepository;
	}
}

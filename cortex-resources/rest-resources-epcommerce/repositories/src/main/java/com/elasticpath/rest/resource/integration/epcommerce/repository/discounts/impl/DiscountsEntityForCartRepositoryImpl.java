/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.discounts.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.discounts.DiscountEntity;
import com.elasticpath.rest.definition.discounts.DiscountForCartIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;

/**
 * Discounts Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class DiscountsEntityForCartRepositoryImpl<E extends DiscountEntity, I extends DiscountForCartIdentifier>
		implements Repository<DiscountEntity, DiscountForCartIdentifier> {

	private CartOrderRepository cartOrderRepository;
	private ShopperRepository shopperRepository;
	private MoneyTransformer moneyTransformer;
	private PricingSnapshotRepository pricingSnapshotRepository;

	@Override
	public Single<DiscountEntity> findOne(final DiscountForCartIdentifier identifier) {

		String cartId = identifier.getCart().getCartId().getValue();
		String scope = identifier.getCart().getCarts().getScope().getValue();

		Single<Money> subtotalDiscountMoney = getSubtotalDiscountMoney(scope, cartId);

		Single<Shopper> shopper = shopperRepository.findOrCreateShopper();

		return getCostEntity(subtotalDiscountMoney, shopper)
				.map(costEntity -> DiscountEntity.builder()
						.addingDiscount(costEntity)
						.withCartId(cartId)
						.build());
	}

	private Single<CostEntity> getCostEntity(final Single<Money> subtotalDiscountMoney, final Single<Shopper> shopper) {
		return shopper.zipWith(subtotalDiscountMoney,
				(thisShopper, money) -> moneyTransformer.transformToEntity(money, thisShopper.getLocale()));
	}

	private Single<Money> getSubtotalDiscountMoney(final String scope, final String cartId) {
		return cartOrderRepository.getEnrichedShoppingCart(scope, cartId, CartOrderRepository.FindCartOrder.BY_CART_GUID)
				.flatMap(pricingSnapshotRepository::getShoppingCartPricingSnapshot)
				.map(ShoppingCartPricingSnapshot::getSubtotalDiscountMoney);
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setShopperRepository(final ShopperRepository shopperRepository) {
		this.shopperRepository = shopperRepository;
	}

	@Reference
	public void setMoneyTransformer(final MoneyTransformer moneyTransformer) {
		this.moneyTransformer = moneyTransformer;
	}

	@Reference
	public void setPricingSnapshotRepository(final PricingSnapshotRepository pricingSnapshotRepository) {
		this.pricingSnapshotRepository = pricingSnapshotRepository;
	}

}
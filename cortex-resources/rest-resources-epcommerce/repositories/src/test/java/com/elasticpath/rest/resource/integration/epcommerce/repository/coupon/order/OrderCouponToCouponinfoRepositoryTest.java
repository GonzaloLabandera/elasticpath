/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.order;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_ORDER_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.COUPON_CODE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.EMAIL;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.ORDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.CouponTestFactory.buildOrderCouponIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.order.OrderCouponToCouponinfoRepository.COUPON_IS_NOT_FOUND;

import java.util.Collections;
import java.util.Set;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.coupons.CouponEntity;
import com.elasticpath.rest.definition.coupons.OrderCouponIdentifier;
import com.elasticpath.rest.definition.orders.OrdersMediaTypes;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.CouponEntityBuilder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.CouponRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.coupon.CouponTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class OrderCouponToCouponinfoRepositoryTest {

	private static final String NOT_EXISTING_COUPON_ID = "random id";
	private static final String COUPON_CODE_IS_NOT_VALID = "Coupon code is not valid";
	private static final CouponEntity COUPON_ENTITY = CouponTestFactory.buildCouponEntity(
			COUPON_CODE, CART_ORDER_GUID, "type");

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private CouponEntityBuilder builder;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private Coupon coupon;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ShoppingCart shoppingCart;

	@InjectMocks
	private OrderCouponToCouponinfoRepository<CouponEntity, OrderCouponIdentifier> repository;

	@Before
	public void setup() {
		when(couponRepository.findByCouponCode(COUPON_CODE)).thenReturn(Single.just(coupon));
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShopper().getCustomer().getEmail()).thenReturn(EMAIL);
		when(coupon.getCouponCode()).thenReturn(COUPON_CODE);
		when(cartOrder.addCoupon(COUPON_CODE)).thenReturn(true);
		when(cartOrderRepository.saveCartOrderAsSingle(cartOrder)).thenReturn(Single.just(cartOrder));
		when(builder.build(coupon, OrdersMediaTypes.ORDER.id(), CART_ORDER_GUID))
				.thenReturn(Single.just(COUPON_ENTITY));
	}

	@Test
	public void submitValidCouponEntityReturnsSubmitResultOfOrderCouponIdentifier() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(couponRepository.isCouponValidInStore(COUPON_CODE, SCOPE, EMAIL)).thenReturn(Single.just(true));

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.submit(COUPON_ENTITY, StringIdentifier.of(SCOPE))
				.test()
				.assertValue(orderCouponIdentifierSubmitResult -> orderCouponIdentifierSubmitResult.getIdentifier().equals(orderCouponIdentifier));
	}

	@Test
	public void submitWithInvalidCouponReturnsStateFailureResourceOperationFailure() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(couponRepository.isCouponValidInStore(COUPON_CODE, SCOPE, EMAIL)).thenReturn(Single.just(false));

		repository.submit(COUPON_ENTITY, StringIdentifier.of(SCOPE))
				.test()
				.assertError(ResourceOperationFailure.stateFailure(COUPON_CODE_IS_NOT_VALID));
	}

	@Test
	public void submitWithInvalidCartOrderReturnsNotFoundResourceOperationFailure() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND)));

		repository.submit(COUPON_ENTITY, StringIdentifier.of(SCOPE))
				.test()
				.assertError(ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND));
	}

	@Test
	public void submitValidExistingCouponEntityReturnsSubmitResultOfOrderCouponIdentifier() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(couponRepository.isCouponValidInStore(COUPON_CODE, SCOPE, EMAIL)).thenReturn(Single.just(true));
		when(cartOrder.addCoupon(COUPON_CODE)).thenReturn(false);


		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.submit(COUPON_ENTITY, StringIdentifier.of(SCOPE))
				.test()
				.assertValue(orderCouponIdentifierSubmitResult -> orderCouponIdentifierSubmitResult.getIdentifier().equals(orderCouponIdentifier));
	}

	@Test
	public void findOneWithCartOrderWithoutCouponsReturnsNotFoundResourceOperationFailure() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.findOne(orderCouponIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND));
	}

	@Test
	public void findOneValidCartOrderWithCouponsShouldReturnCouponEntity() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(cartOrder.getCouponCodes()).thenReturn(Collections.singleton(COUPON_CODE));

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.findOne(orderCouponIdentifier)
				.test()
				.assertValue(couponEntity -> couponEntity.equals(COUPON_ENTITY));
	}

	@Test
	public void deleteValidCouponReturnsCompletableComplete() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(cartOrder.removeCoupon(COUPON_CODE)).thenReturn(true);

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.delete(orderCouponIdentifier)
				.test()
				.assertComplete();
	}

	@Test
	public void deleteCouponFromInvalidCartOrderReturnsNotFoundResourceOperationFailure() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND)));

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.delete(orderCouponIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND));
	}

	@Test
	public void deleteInvalidCouponReturnsNotComplete() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(cartOrder.removeCoupon(COUPON_CODE)).thenReturn(false);

		OrderCouponIdentifier orderCouponIdentifier = buildOrderCouponIdentifier(COUPON_CODE, CART_ORDER_GUID, SCOPE);

		repository.delete(orderCouponIdentifier)
				.test()
				.assertNotComplete();
	}

	@Test
	public void getExistingCouponEntitySuccess() {
		Set<String> couponCodes = Collections.singleton(COUPON_CODE);

		repository.getCouponEntity(COUPON_CODE, couponCodes, CART_ORDER_GUID)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(couponEntity -> couponEntity.equals(COUPON_ENTITY));
	}

	@Test
	public void getCouponEntityWithFailure() {
		Set<String> couponCodes = Collections.singleton(NOT_EXISTING_COUPON_ID);

		repository.getCouponEntity(COUPON_CODE, couponCodes, ORDER_ID)
				.test()
				.assertError(throwable -> throwable.equals(
						ResourceOperationFailure.notFound(COUPON_IS_NOT_FOUND)));
	}
}
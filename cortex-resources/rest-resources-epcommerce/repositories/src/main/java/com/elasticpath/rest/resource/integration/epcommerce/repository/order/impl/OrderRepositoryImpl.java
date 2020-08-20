/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * The facade for {@link Order} related operations.
 */
@Singleton
@Named("orderRepository")
public class OrderRepositoryImpl implements OrderRepository {

	/**
	 * Error message used when purchase can't be found.
	 */
	public static final String PURCHASE_NOT_FOUND = "No purchase with GUID %s was found in store %s.";
	private static final String LINE_ITEM_NOT_FOUND = "Line item not found";
	private final OrderService orderService;
	private final BeanFactory coreBeanFactory;
	private final CartOrderRepository cartOrderRepository;
	private final ProductSkuRepository productSkuRepository;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Default constructor.
	 *
	 * @param orderService         the order service
	 * @param coreBeanFactory      the core bean factory
	 * @param reactiveAdapter      reactiveAdapter
	 * @param cartOrderRepository  cartOrderRepository
	 * @param productSkuRepository productSkuRepository
	 */
	@Inject
	public OrderRepositoryImpl(
			@Named("orderService") final OrderService orderService,
			@Named("coreBeanFactory") final BeanFactory coreBeanFactory,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter,
			@Named("cartOrderRepository") final CartOrderRepository cartOrderRepository,
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository) {
		this.orderService = orderService;
		this.coreBeanFactory = coreBeanFactory;
		this.reactiveAdapter = reactiveAdapter;
		this.cartOrderRepository = cartOrderRepository;
		this.productSkuRepository = productSkuRepository;
	}

	@Override
	@CacheResult
	public Single<Order> findByGuid(final String storeCode, final String orderGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> orderService.findOrderByOrderNumber(orderGuid),
				String.format(PURCHASE_NOT_FOUND, orderGuid, storeCode));
	}

	@Override
	@CacheResult
	public Observable<String> findOrderIdsByCustomerGuid(final String storeCode, final String customerGuid) {
		CustomerSearchCriteria customerSearchCriteria = createCustomerSearchCriteria(customerGuid);
		OrderSearchCriteria orderSearchCriteria = createOrderSearchCriteria(customerSearchCriteria, storeCode);
		orderSearchCriteria.setExcludeAccountOrders(true);
		Collection<String> orderGuidList = orderService.findOrderNumbersBySearchCriteria(orderSearchCriteria, 0, Integer.MAX_VALUE);

		return Observable.fromIterable(orderGuidList);
	}

	@Override
	public Maybe<String> findByGuidAndCustomerGuid(final String storeCode, final String orderGuid, final String customerGuid) {
		CustomerSearchCriteria customerSearchCriteria = createCustomerSearchCriteria(customerGuid);
		OrderSearchCriteria orderSearchCriteria = createOrderSearchCriteria(customerSearchCriteria, storeCode);
		orderSearchCriteria.setOrderNumber(orderGuid);
		Collection<String> orderList = orderService.findOrderNumbersBySearchCriteria(orderSearchCriteria, 0, 1);

		return orderList.stream()
				.map(Maybe::just)
				.findFirst()
				.orElseGet(Maybe::empty);
	}

	@Override
	public Single<CartOrder> getOrderByOrderId(final String scope, final String orderId) {
		return cartOrderRepository.getCartOrder(scope, orderId, CartOrderRepository.FindCartOrder.BY_ORDER_GUID);
	}

	@Override
	public Single<OrderSku> findOrderSku(final String scope, final String orderId, final List<String> guidPathFromRootItem) {
		return findByGuid(scope, orderId)
				.map(order -> (OrderSku) findShoppingItem(order, guidPathFromRootItem))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(LINE_ITEM_NOT_FOUND)));
	}

	/**
	 * Find the shopping item.
	 *
	 * @param order                the order
	 * @param guidPathFromRootItem a shopping item can be a root item or a component of a bundle item. This is a list of all guids
	 *                             from the root item to the component item.
	 * @return ShoppingItem
	 */
	@VisibleForTesting
	ShoppingItem findShoppingItem(final Order order, final List<String> guidPathFromRootItem) {
		Collection<? extends ShoppingItem> shoppingItems = order.getRootShoppingItems();
		ShoppingItem shoppingItem = null;
		for (String guid : guidPathFromRootItem) {
			shoppingItem = getShoppingItemWithGuid(shoppingItems, guid);
			if (shoppingItem == null) {
				return null;
			}
			shoppingItems = shoppingItem.getChildren();
		}
		return shoppingItem;
	}

	@Override
	public Single<ProductSku> findProductSku(final String scope, final String orderId, final List<String> guidPathFromRootItem) {
		return findByGuid(scope, orderId)
				.flatMap(order -> findProductSku(order, guidPathFromRootItem));
	}

	@Override
	@CacheResult
	public Observable<String> findOrderIdsByAccountGuid(final String storeCode,
														final String accountGuid,
														final int firstResult,
														final int maxResults) {
		AccountSearchCriteria accountSearchCriteria = createAccountSearchCriteria(accountGuid);
		OrderSearchCriteria orderSearchCriteria = createOrderSearchCriteria(accountSearchCriteria, storeCode);
		Collection<String> orderGuidList = orderService.findOrderNumbersBySearchCriteria(orderSearchCriteria, firstResult, maxResults);
		return Observable.fromIterable(orderGuidList);
	}

	@Override
	public long getAccountPurchasesSize(final String storeCode, final String accountGuid) {
		AccountSearchCriteria accountSearchCriteria = createAccountSearchCriteria(accountGuid);
		OrderSearchCriteria orderSearchCriteria = createOrderSearchCriteria(accountSearchCriteria, storeCode);
		return orderService.getOrderCountBySearchCriteria(orderSearchCriteria);
	}

	@Override
	public Customer getCustomerByOrderNumber(final String orderNumber) {
		return orderService.getCustomerByOrderNumber(orderNumber);
	}

	private Single<ProductSku> findProductSku(final Order order, final List<String> guidPathFromRootItem) {
		return productSkuRepository.getProductSkuWithAttributesByGuid(findShoppingItem(order, guidPathFromRootItem).getSkuGuid())
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(LINE_ITEM_NOT_FOUND)));
	}

	private ShoppingItem getShoppingItemWithGuid(final Collection<? extends ShoppingItem> shoppingItems, final String guid) {
		for (ShoppingItem shoppingItem : shoppingItems) {
			if (shoppingItem.getGuid().equals(guid)) {
				return shoppingItem;
			}
		}
		return null;
	}

	private OrderSearchCriteria createOrderSearchCriteria(final CustomerSearchCriteria customerSearchCriteria, final String validatedStoreCode) {
		OrderSearchCriteria orderSearchCriteria = createDefaultOrderSearchCriteria(validatedStoreCode);
		orderSearchCriteria.setCustomerSearchCriteria(customerSearchCriteria);
		return orderSearchCriteria;
	}

	private OrderSearchCriteria createOrderSearchCriteria(final AccountSearchCriteria accountSearchCriteria, final String validatedStoreCode) {
		OrderSearchCriteria orderSearchCriteria = createDefaultOrderSearchCriteria(validatedStoreCode);
		orderSearchCriteria.setAccountSearchCriteria(accountSearchCriteria);
		return orderSearchCriteria;
	}

	private OrderSearchCriteria createDefaultOrderSearchCriteria(final String validatedStoreCode) {
		OrderSearchCriteria orderSearchCriteria = coreBeanFactory.getPrototypeBean(ContextIdNames.ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		orderSearchCriteria.setExcludedOrderStatus(OrderStatus.FAILED);
		Set<String> storeCodes = Collections.singleton(validatedStoreCode);
		orderSearchCriteria.setStoreCodes(storeCodes);
		orderSearchCriteria.setSortingType(StandardSortBy.DATE);
		orderSearchCriteria.setSortingOrder(SortOrder.DESCENDING);
		return orderSearchCriteria;
	}

	private CustomerSearchCriteria createCustomerSearchCriteria(final String customerGuid) {
		CustomerSearchCriteria customerSearchCriteria = coreBeanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA,
				CustomerSearchCriteria.class);
		customerSearchCriteria.setGuid(customerGuid);
		return customerSearchCriteria;
	}

	private AccountSearchCriteria createAccountSearchCriteria(final String accountGuid) {
		AccountSearchCriteria accountSearchCriteria = coreBeanFactory.getPrototypeBean(ContextIdNames.ACCOUNT_SEARCH_CRITERIA,
				AccountSearchCriteria.class);
		accountSearchCriteria.setGuid(accountGuid);
		return accountSearchCriteria;
	}
}

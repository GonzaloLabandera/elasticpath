/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.giftcertificate.GiftCertificateEventType;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.catalog.GiftCertificateService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.GiftCertificateFactory;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversiblePostCaptureCheckoutAction;
import com.elasticpath.service.store.StoreService;

/**
 * CheckoutAction to create gift certificates that were purchased
 * in an order and store necessary values from the certificates on the
 * order skus.
 */
public class CreateGiftCertificatesCheckoutAction implements ReversiblePostCaptureCheckoutAction {

	private GiftCertificateService giftCertificateService;

	private GiftCertificateFactory giftCertificateFactory;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private ProductSkuLookup productSkuLookup;

	private PricingSnapshotService pricingSnapshotService;

	private StoreService storeService;

	private OrderService orderService;

	@Override
	public void execute(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		final Customer customer = context.getCustomer();
		final Store store = storeService.findStoreWithCode(context.getOrder().getStoreCode());
		final Map<OrderSku, GiftCertificate> giftCertificateMap = createAndPersistGiftCertificates(context.getOrder(), customer, store);
		updateOrderSkus(context.getOrder(), giftCertificateMap);

		orderService.update(context.getOrder());

		sendGiftCertificateCreatedEvent(context.getOrder(), giftCertificateMap.keySet());
	}

	private void sendGiftCertificateCreatedEvent(final Order order, final Set<OrderSku> orderSkus) {
		orderSkus.forEach(orderSku -> {
			final Map<String, String> gcFields = orderSku.getModifierFields().getMap();
			final PriceCalculator priceCalculator = getPricingSnapshotService().getPricingSnapshotForOrderSku(orderSku).getPriceCalc();
			final Map<String, Object> data = new HashMap<>(8);
			data.put("orderLocale", order.getLocale());
			data.put("orderStoreCode", order.getStoreCode());
			data.put("shipmentNumber", orderSku.getShipment().getShipmentNumber());
			data.put("shipmentType", orderSku.getShipment().getOrderShipmentType().toString());
			data.put("orderSkuGuid", orderSku.getGuid());
			data.put("orderSkuTotalAmount", priceCalculator.withCartDiscounts().getAmount().toString());
			data.put("orderSkuTotalCurrency", priceCalculator.getMoney().getCurrency());
			data.put("gcFields", gcFields);

			final String giftCertificateGuid = gcFields.get(GiftCertificate.KEY_GUID);

			final EventMessage giftCertificateCreatedEventMessage = getEventMessageFactory()
					.createEventMessage(GiftCertificateEventType.GIFT_CERTIFICATE_CREATED, giftCertificateGuid, data);
			getEventMessagePublisher().publish(giftCertificateCreatedEventMessage);
		});
	}

	@Override
	public void rollback(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		for (final OrderSku orderSku : context.getOrder().getRootShoppingItems()) {
			if (isGiftCertificateLineItem(orderSku)) {
				rollbackGiftCertificate(orderSku);
			}
		}
	}

	/**
	 * Removes the given GiftCertificate from the DB and also resets the KEY_CODE and KEY_SENDER_EMAIL to null.
	 * @param orderSku The GiftCertificate to rollback.
	 */
	private void rollbackGiftCertificate(final OrderSku orderSku) {
		String gcCode = orderSku.getModifierFields().get(GiftCertificate.KEY_CODE);
		if (gcCode == null) {
			return;
		}
		GiftCertificate giftCertificate = giftCertificateService.findByGiftCertificateCode(gcCode);
		if (giftCertificate == null) {
			return;
		}
		giftCertificateService.removeGiftCertificate(giftCertificate.getUidPk());

		List<String> modifierFieldsToRemove = Lists.newArrayList(GiftCertificate.KEY_CODE, GiftCertificate.KEY_SENDER_EMAIL);
		orderSku.getModifierFields().removeAll(modifierFieldsToRemove);
	}

	/**
	 * Records any data necessary from the gift certificates on the order skus. For example
	 * the key and sender email.
	 *
	 * @param order order to get the skus from
	 * @param giftCertificateMap map the order sku to the gift certificate created from it
	 */
	protected void updateOrderSkus(final Order order, final Map<OrderSku, GiftCertificate> giftCertificateMap) {
		for (final Map.Entry<OrderSku, GiftCertificate> entry : giftCertificateMap.entrySet()) {
			final OrderSku orderSku = entry.getKey();
			final GiftCertificate giftCertificate = entry.getValue();

			orderSku.getModifierFields().put(GiftCertificate.KEY_CODE, giftCertificate.getGiftCertificateCode());
			orderSku.getModifierFields().put(GiftCertificate.KEY_GUID, giftCertificate.getGuid());
			orderSku.getModifierFields().put(GiftCertificate.KEY_SENDER_EMAIL, giftCertificate.getPurchaser().getEmail());
		}
	}

	/**
	 * Creates and persists all gift certificates that were purchased as part of the given {@code Order}.
	 * Calls {@link #createAndPersistGiftCertificate(ShoppingItem, ShoppingItemPricingSnapshot, Customer, Store, Order)}.
	 * @param completedOrder the completed (persisted) order.
	 * @param customer the customer who's purchased the gift certificates
	 * @param store the store in which the gift certificates were purchased
	 * @return a map of {@code OrderSku}s to created {@code GiftCertificate}s
	 */
	protected Map<OrderSku, GiftCertificate> createAndPersistGiftCertificates(final Order completedOrder,
			final Customer customer, final Store store) {
		final Map<OrderSku, GiftCertificate> giftCertificateMap = new HashMap<>();
		for (final OrderShipment orderShipment : completedOrder.getAllShipments()) {
			for (final OrderSku orderSku : orderShipment.getShipmentOrderSkus()) {
				if (isGiftCertificateLineItem(orderSku)) {
					final ShoppingItemPricingSnapshot pricingSnapshot =
							getPricingSnapshotService().getPricingSnapshotForOrderSku(orderSku);
					giftCertificateMap.put(orderSku, createAndPersistGiftCertificate(orderSku, pricingSnapshot, customer, store, completedOrder));
				}
			}
		}
		return giftCertificateMap;
	}

	private boolean isGiftCertificateLineItem(final OrderSku orderSku) {
		ProductSku productSku = getProductSkuLookup().findByGuid(orderSku.getSkuGuid());
		final ProductType productType = productSku.getProduct().getProductType();

		return productType.isGiftCertificate();
	}

	/**
	 * Creates a new gift certificate.
	 *
	 * @param shoppingItem the shoppingItem
	 * @param shoppingItemPricingSnapshot the pricing snapshot corresponding to the shopping item
	 * @param customer the customer (purchaser)
	 * @param store the store
	 * @param order the order
	 * @return the created GiftCertificate
	 */
	GiftCertificate createAndPersistGiftCertificate(final ShoppingItem shoppingItem, final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot,
													final Customer customer, final Store store, final Order order) {
		//Persist the created GC so that when the OrderSku is persisted and cascades the persist to its GC, the foreign
		//key constraint is satisfied. Once GC is removed from the OrderSku this will change.
		final GiftCertificate giftCertificate = giftCertificateFactory.createGiftCertificate(shoppingItem,
																							shoppingItemPricingSnapshot,
																							customer, store,
																							order.getCurrency());
		giftCertificate.setOrderGuid(order.getGuid());
		return giftCertificateService.add(giftCertificate);
	}

	/**
	 * Inject giftCertificateFactory.
	 *
	 * @param giftCertificateFactory the giftCertificateFactory to set
	 */
	public void setGiftCertificateFactory(final GiftCertificateFactory giftCertificateFactory) {
		this.giftCertificateFactory = giftCertificateFactory;
	}

	/**
	 * Inject giftCertificateService.
	 *
	 * @param giftCertificateService the giftCertificateService to set
	 */
	public void setGiftCertificateService(final GiftCertificateService giftCertificateService) {
		this.giftCertificateService = giftCertificateService;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

	public StoreService getStoreService() {
		return storeService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

	public OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}
}

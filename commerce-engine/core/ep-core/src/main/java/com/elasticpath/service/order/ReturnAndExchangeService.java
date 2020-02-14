/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.order;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.elasticpath.commons.handlers.order.OrderShipmentHandler;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.EpPersistenceService;
import com.elasticpath.service.search.query.OrderReturnSearchCriteria;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Provides storage and access to <code>OrderReturn</code> objects.
 */
public interface ReturnAndExchangeService extends EpPersistenceService {
	/**
	 * Returns list of all <code>OrderReturn</code> objects.
	 *
	 * @return Full list of Order exchanges and order returns.
	 * @throws com.elasticpath.base.exception.EpServiceException in case of any errors
	 */
	List<OrderReturn> list();

	/**
	 * Returns list of all <code>OrderReturn</code> uids.
	 *
	 * @return Full list of Order exchanges and order return uids.
	 * @throws com.elasticpath.base.exception.EpServiceException in case of any errors
	 */
	List<Long> findAllUids();

	/**
	 * Returns a list of <code>OrderReturn</code> based on the given uids.
	 *
	 * @param orderUids a collection of order return uids
	 * @return a list of <code>OrderReturn</code>s
	 */
	List<OrderReturn> findByUids(Collection<Long> orderUids);

	/**
	 * Retrieves list of <code>OrderReturn</code> uids where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>OrderReturn</code> whose last modified date is later than the specified date
	 */
	List<Long> findUidsByModifiedDate(Date date);

	/**
	 * Returns list of <code>OrderReturn</code>'s that associated with order defined by uidPk.
	 *
	 * @param uidPk the <code>Order</code> UID.
	 * @return List of Order exchanges and order returns for the specified order.
	 * @throws com.elasticpath.base.exception.EpServiceException in case of any errors
	 */
	List<OrderReturn> list(long uidPk);

	/**
	 * Returns list of <code>OrderReturn</code>'s of concrete type type that associated with order defined by uidPk.
	 *
	 * @param uidPk      the <code>Order</code> UID.
	 * @param returnType type of the order(EXCHANGE or RETURN).
	 * @return List of exchanges or returns for the specified order.
	 * @throws com.elasticpath.base.exception.EpServiceException in case of any errors
	 */
	List<OrderReturn> list(long uidPk, OrderReturnType returnType);

	/**
	 * Get the order return with the given UID. Return null if no matching record exists.
	 *
	 * @param orderReturnUid the order return UID
	 * @return the order return if UID exists, otherwise null
	 * @throws com.elasticpath.base.exception.EpServiceException - in case of any errors
	 */
	OrderReturn get(long orderReturnUid);

	/**
	 * Gets the order return with the given UID. Returns <code>null</code> if no matching
	 * records exist. Give a load tuner to fine tune the result or <code>null</code> to load the
	 * default fields.
	 *
	 * @param orderReturnUid the order return UID
	 * @param loadTuner      the load tuner or <code>null</code> for the default
	 * @return the order return if the UID exists, otherwise <code>null</code>
	 * @throws com.elasticpath.base.exception.EpServiceException in case of any errors
	 */
	OrderReturn get(long orderReturnUid, FetchGroupLoadTuner loadTuner);

	/**
	 * Generic get method for all persistable domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws com.elasticpath.base.exception.EpServiceException - in case of any errors
	 */
	Object getObject(long uid);

	/**
	 * Updates the given order return. Handles OptimisticLockException. If it's detected that
	 * return was modified by another user, then the exception is wrapped into
	 * <code>OrderReturnOutOfDateException</code> and thrown.
	 *
	 * @param orderReturn the order return to update
	 * @return the persisted instance of order return
	 * @throws com.elasticpath.base.exception.EpServiceException - in case of any errors, specifically
	 *                                                           OrderReturnOutOfDateException in case of version collision.
	 */
	OrderReturn update(OrderReturn orderReturn);

	/**
	 * Adds the given order return.
	 *
	 * @param orderReturn the order return to add
	 * @return the persisted instance of order return
	 * @throws com.elasticpath.base.exception.EpServiceException - in case of any errors
	 */
	OrderReturn add(OrderReturn orderReturn);

	/**
	 * Populates internal exchange's shopping cart. This exchange shopping cart will be used to create exchange order.
	 *
	 * @param orderExchange   exchange shopping cart will be created for
	 * @param itemList        the list of cart items
	 * @param shippingOption  shipping option
	 * @param shippingAddress shipping address
	 * @return recalculated shopping cart.
	 */
	ShoppingCart populateShoppingCart(OrderReturn orderExchange, Collection<? extends ShoppingItem> itemList,
									  ShippingOption shippingOption, Address shippingAddress);

	/**
	 * Populates internal exchange's shopping cart. This exchange shopping cart will be used to create exchange order.
	 *
	 * @param orderExchange    exchange shopping cart will be created for
	 * @param itemList         the list of cart items
	 * @param shippingOption   shipping option
	 * @param shippingCost     shipping cost for the exchange order
	 * @param shippingDiscount shipping discount for the exchange order
	 * @param shippingAddress  shipping address
	 * @return recalculated shopping cart.
	 */
	ShoppingCart populateShoppingCart(OrderReturn orderExchange, Collection<? extends ShoppingItem> itemList,
									  ShippingOption shippingOption, BigDecimal shippingCost, BigDecimal shippingDiscount,
									  Address shippingAddress);

	/**
	 * Gets exchange by order exchange's uidPk.
	 *
	 * @param uidPk exchange order uidPk for which exchange is obtained.
	 * @return exchange for order exchange if found.
	 */
	OrderReturn getExchange(long uidPk);

	/**
	 * Creates the exchange. Logs the event.
	 *
	 * @param exchange           order exchange to be created.
	 * @param refundType         type of refund selected for exchange
	 * @param paymentInstruments explicit payment instruments used for reservation
	 * @return processed return
	 */
	OrderReturn createExchange(OrderReturn exchange, ReturnExchangeRefundTypeEnum refundType, List<PaymentInstrumentDTO> paymentInstruments);

	/**
	 * Edits the return. Logs the event.
	 *
	 * @param orderReturn order return to be edited.
	 * @return processed return
	 */
	OrderReturn editReturn(OrderReturn orderReturn);

	/**
	 * Completes the return. Logs the event.
	 *
	 * @param orderReturn order return to be completed.
	 * @param refundType  type of refund selected for return
	 * @return processed return
	 */
	OrderReturn completeReturn(OrderReturn orderReturn, ReturnExchangeRefundTypeEnum refundType);

	/**
	 * Completes the exchange. Logs the event.
	 *
	 * @param exchange   order exchange to be completed.
	 * @param refundType type of refund selected for exchange
	 * @return processed return
	 */
	OrderReturn completeExchange(OrderReturn exchange, ReturnExchangeRefundTypeEnum refundType);

	/**
	 * Cancels the return. Logs the event.
	 *
	 * @param orderReturn order return to be canceled.
	 * @return processed return
	 */
	OrderReturn cancelReturnExchange(OrderReturn orderReturn);

	/**
	 * Receives the return. Logs the event.
	 *
	 * @param orderReturn order return to be received.
	 * @return processed return
	 */
	OrderReturn receiveReturn(OrderReturn orderReturn);

	/**
	 * order return count function based on the OrderReturnSearchCriteria.
	 *
	 * @param orderReturnSearchCriteria the order return  search criteria.
	 * @return the count of orders matching the given criteria.
	 */
	long getOrderReturnCountBySearchCriteria(OrderReturnSearchCriteria orderReturnSearchCriteria);

	/**
	 * order return search function based on the OrderReturnSearchCriteria.
	 *
	 * @param orderReturnSearchCriteria the order return search criteria.
	 * @param start                     the starting record to search
	 * @param maxResults                the max results to be returned
	 * @return the list of orders matching the given criteria.
	 */
	List<OrderReturn> findOrderReturnBySearchCriteria(OrderReturnSearchCriteria orderReturnSearchCriteria,
													  int start, int maxResults);

	/**
	 * Resends the Return/Exchange notification.
	 *
	 * @param orderReturnUid the order return UID
	 * @param emailRecipient the email recipient
	 */
	void resendReturnExchangeNotification(long orderReturnUid, String emailRecipient);

	/**
	 * Creates the return against the specified shipment. Logs the event.
	 *
	 * @param orderReturn   order return to be created.
	 * @param orderShipment the order shipment against which the refund payment will be applied.
	 * @param type          type of return is being created.
	 * @param originator    the order return originator
	 * @return processed return
	 */
	OrderReturn createShipmentReturn(OrderReturn orderReturn, ReturnExchangeRefundTypeEnum type, OrderShipment orderShipment,
									 EventOriginator originator);

	/**
	 * Populates the OrderReturn template based on the order / order shipment information.
	 *
	 * @param orderShipment   The order shipment to populate the OrderReturn for.
	 * @param orderReturnType The order return type to indicate it is an order return or exchange order
	 * @return An instance of OrderReturn with the information populated based on the order / order shipment.
	 */
	OrderReturn getOrderReturnPrototype(OrderShipment orderShipment, OrderReturnType orderReturnType);

	/**
	 * Retrieves the OrderShipmentHandler based on the OrderShipment type passed in.
	 *
	 * @param orderShipment The OrderShipment to look up the handler for
	 * @return An instance of OrderShipmentHandler based on the type of OrderShipment
	 */
	OrderShipmentHandler retrieveOrderShipmentHandler(OrderShipment orderShipment);

}

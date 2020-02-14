/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.FETCH_GROUP_LOAD_TUNER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_CRITERION;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_EVENT_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_RETURN;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_RETURN_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.SHOPPING_CART;
import static com.elasticpath.commons.constants.ContextIdNames.TAX_DOCUMENT_MODIFICATION_CONTEXT;
import static com.elasticpath.domain.order.OrderReturnStatus.AWAITING_STOCK_RETURN;
import static com.elasticpath.domain.order.OrderReturnStatus.CANCELLED;
import static com.elasticpath.domain.order.OrderReturnStatus.COMPLETED;
import static com.elasticpath.service.order.ReturnExchangeRefundTypeEnum.PHYSICAL_RETURN_REQUIRED;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.OptimisticLockException;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.handlers.order.OrderShipmentHandler;
import com.elasticpath.commons.handlers.order.OrderShipmentHandlerFactory;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnStatus;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.OrderCriterion;
import com.elasticpath.persistence.support.OrderCriterion.ResultType;
import com.elasticpath.persistence.support.impl.CriteriaQuery;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.IllegalReturnStateException;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderReturnOutOfDateException;
import com.elasticpath.service.order.OrderReturnValidator;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.search.query.OrderReturnSearchCriteria;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.ReturnTaxOperationService;
import com.elasticpath.service.tax.TaxDocumentModificationContext;
import com.elasticpath.service.tax.TaxDocumentModificationType;
import com.elasticpath.service.tax.TaxOperationService;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Provides storage and access to <code>OrderReturn</code> objects.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.ExcessiveClassLength", "deprecation",
		"PMD.GodClass"})
public class ReturnAndExchangeServiceImpl extends AbstractEpPersistenceServiceImpl implements ReturnAndExchangeService {

	private static final Logger LOG = Logger.getLogger(ReturnAndExchangeServiceImpl.class);
	private static final String ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE = "Error occurred while processing an exchange.";

	/**
	 * The default shipping cost amount to be used when no value is provided.
	 */
	static final BigDecimal DEFAULT_SHIPPING_COST = new BigDecimal(Integer.MIN_VALUE);

	/**
	 * The default shipping discount amount to be used when no value is provided.
	 */
	static final BigDecimal DEFAULT_SHIPPING_DISCOUNT = new BigDecimal(Integer.MIN_VALUE);

	private CheckoutService checkoutService;
	private TimeService timeService;
	private ProductSkuLookup productSkuLookup;
	private OrderService orderService;
	private ShopperService shopperService;
	private ShoppingCartService shoppingCartService;
	private ShippingOptionService shippingOptionService;
	private CustomerSessionService customerSessionService;
	private StoreService storeService;
	private ReturnTaxOperationService returnTaxOperationService;
	private TaxOperationService taxOperationService;
	private EventMessageFactory eventMessageFactory;
	private EventMessagePublisher eventMessagePublisher;
	private OrderShipmentHandlerFactory orderShipmentHandlerFactory;
	private OrderReturnValidator orderReturnValidator;
	private PricingSnapshotService pricingSnapshotService;
	private TaxSnapshotService taxSnapshotService;
	private CartOrderService cartOrderService;
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Override
	public List<OrderReturn> list(final long uidPk) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_RETURN_LIST", uidPk);
	}

	@Override
	public List<OrderReturn> list(final long uidPk, final OrderReturnType returnType) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_RETURN_LIST_BY_RETURN_TYPE", uidPk, returnType);
	}

	@Override
	public OrderReturn get(final long orderReturnUid) {
		return get(orderReturnUid, null);
	}

	@Override
	public OrderReturn get(final long orderReturnUid, final FetchGroupLoadTuner loadTuner) {
		sanityCheck();
		FetchGroupLoadTuner tuner = loadTuner;
		if (loadTuner == null) {
			tuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		}

		if (orderReturnUid <= 0) {
			return getPrototypeBean(ORDER_RETURN, OrderReturn.class);
		}

		tuner.addFetchGroup(FetchGroupConstants.ORDER_NOTES); //added to ensure that the Order's orderEvents field is loaded

		return getPersistentBeanFinder()
				.withLoadTuners(tuner)
				.get(ORDER_RETURN, orderReturnUid);
	}

	@Override
	public Object getObject(final long uid) {
		return get(uid);
	}

	@Override
	public List<OrderReturn> list() {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_EXCHANGE_AND_RETURN_LIST_BY_ORDER_UID");
	}

	@Override
	public List<Long> findAllUids() {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_RETURN_UIDS_LIST");
	}

	@Override
	public List<OrderReturn> findByUids(final Collection<Long> orderUids) {
		sanityCheck();

		if (orderUids == null || orderUids.isEmpty()) {
			return new ArrayList<>();
		}

		return getPersistenceEngine().retrieveByNamedQueryWithList("ORDER_RETURNS_BY_UIDS", "list", orderUids);
	}

	/**
	 * Retrieves list of <code>OrderReturn</code> uids where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>OrderReturn</code> whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findUidsByModifiedDate(final Date date) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_RETURN_UIDS_SELECT_BY_MODIFIED_DATE", date);
	}

	@Override
	public OrderReturn update(final OrderReturn orderReturn) {
		sanityCheck();
		OrderReturn updatedOrderReturn;
		try {
			orderReturn.setLastModifiedDate(timeService.getCurrentTime());
			updatedOrderReturn = getPersistenceEngine().merge(orderReturn);
		} catch (OptimisticLockException e) {
			throw new OrderReturnOutOfDateException("Return cannot be updated as it has been updated by another user", e);
		}
		return updatedOrderReturn;
	}

	@Override
	public OrderReturn add(final OrderReturn orderReturn) {
		sanityCheck();
		orderReturn.setLastModifiedDate(timeService.getCurrentTime());
		getPersistenceEngine().save(orderReturn);
		return orderReturn;
	}

	@Override
	public OrderReturn getExchange(final long uidPk) {
		sanityCheck();
		List<OrderReturn> exchanges = getPersistenceEngine().retrieveByNamedQuery("EXCHANGE_BY_EXCHANGE_ORDER_UID", uidPk);
		if (!exchanges.isEmpty()) {
			return exchanges.get(0);
		}
		return null;
	}

	/**
	 * Partially or fully refund the order based on the order return payment and
	 * order shipment. If the shipment is null, an EP service exception is thrown.
	 *
	 * @param orderReturn   order return to be refunded.
	 * @param orderShipment the order shipment against which the refund will be applied.
	 */
	protected void refundOrderShipmentReturn(final OrderReturn orderReturn, final OrderShipment orderShipment) {
		if (orderShipment == null) {
			throw new EpServiceException("Can not refund as there is no orderShipment");
		}

		final BigDecimal refundTotal = orderReturn.getReturnTotal();
		if (refundTotal.compareTo(BigDecimal.ZERO) > 0) {
			final Order order = orderReturn.getOrder();
			orderService.refundOrderPayment(order, emptyList(), Money.valueOf(refundTotal, order.getCurrency()), order.getModifiedBy());
		}
	}

	/**
	 * Creates new payment in order to keep amount that was refunded manually, e.g. by cash. The payment is required in order to properly calculate
	 * return's refunded money. Exchange order shouldn't and can't know if refund was processed manually or actually.
	 *
	 * @param orderReturn to be manually refunded.
	 */
	protected void manualRefundOrderReturn(final OrderReturn orderReturn) {
		final BigDecimal refundTotal = orderReturn.getReturnTotal();
		if (refundTotal.compareTo(BigDecimal.ZERO) > 0) {
			final Order order = orderReturn.getOrder();
			orderService.manualRefundOrderPayment(order, Money.valueOf(refundTotal, order.getCurrency()), order.getModifiedBy());
		}
	}

	private EventOriginator getEventOriginator(final CmUser cmUser) {
		EventOriginatorHelper helper = getSingletonBean(EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
		return helper.getCmUserOriginator(cmUser);
	}

	@Override
	public ShoppingCart populateShoppingCart(final OrderReturn exchangeOrder,
											 final Collection<? extends ShoppingItem> itemList,
											 final ShippingOption shippingOption,
											 final Address shippingAddress) {
		return populateShoppingCart(exchangeOrder, itemList, shippingOption,
				DEFAULT_SHIPPING_COST, DEFAULT_SHIPPING_DISCOUNT, shippingAddress);
	}

	@Override
	public ShoppingCart populateShoppingCart(final OrderReturn exchange,
											 final Collection<? extends ShoppingItem> itemList,
											 final ShippingOption shippingOption,
											 final BigDecimal shippingCost,
											 final BigDecimal shippingDiscount,
											 final Address shippingAddress) {

		ShoppingCart shoppingCart = exchange.getExchangeShoppingCart();
		if (shoppingCart == null) {

			final Order order = exchange.getOrder();
			final CustomerSession customerSession = createCustomerSessionForOrder(order);
			shoppingCart = createShoppingCartForOrderAndShopper(order, customerSession);

			shoppingCart.setExchangeOrderShoppingCart(true);
			exchange.setExchangeCustomerSession(customerSession);
		}

		shoppingCart.setShippingAddress(shippingAddress);

		if (shippingOption != null) {
			final ShippingOptionResult shippingOptionResult = shippingOptionService.getShippingOptions(shoppingCart);

			final String errorMessage = format("Unable to get available shipping options for the given cart with guid '%s'. "
							+ "Shipping option cannot be validated.",
					shoppingCart.getGuid());
			shippingOptionResult.throwExceptionIfUnsuccessful(
					errorMessage,
					singletonList(
							new StructuredErrorMessage(
									"shippingoptions.unavailable",
									errorMessage,
									ImmutableMap.of(
											"cart-id", shoppingCart.getGuid(),
											"shipping-option", shippingOption.getCode()))
					));

			final List<ShippingOption> validShippingOptions = shippingOptionResult.getAvailableShippingOptions();

			if (shippingOptionResult.getAvailableShippingOptions().stream()
					.noneMatch(element -> element.getCode().equals(shippingOption.getCode()))) {
				throw new EpDomainException(format("Shipping option '%s' is not a valid available shipping option. Valid shipping options: %s",
						shippingOption.getCode(), validShippingOptions));
			}

			shoppingCart.setSelectedShippingOption(shippingOption);
		}

		/*
		 * Workaround to prevent shopping cart from removing product sku items. see MSC-5254
		 */
		shoppingCart.clearItems();
		for (final ShoppingItem item : itemList) {
			shoppingCart.addCartItem(item);
		}

		if (DEFAULT_SHIPPING_COST.compareTo(shippingCost) != 0) {
			shoppingCart.setShippingCostOverride(shippingCost);
		}

		if (DEFAULT_SHIPPING_DISCOUNT.compareTo(shippingDiscount) != 0) {
			shoppingCart.setSubtotalDiscountOverride(shippingDiscount);
		}

		populateShoppingCartData(exchange.getOrder(), shoppingCart);

		ShoppingCartPricingSnapshot pricingSnapshotForCart = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshotForCart = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshotForCart);
		exchange.setExchangeShoppingCart(shoppingCart, taxSnapshotForCart);

		return shoppingCart;
	}

	/**
	 * Populates the order data from origin order to shopping cart created for exchange.
	 *
	 * @param originOrder             the origin order.
	 * @param shoppingCartForExchange the shopping cart created for exchange.
	 */
	private void populateShoppingCartData(final Order originOrder, final ShoppingCart shoppingCartForExchange) {
		originOrder.getFieldValues().forEach(shoppingCartForExchange::setCartDataFieldValue);
	}

	private ShoppingCart createShoppingCartForOrderAndShopper(final Order order, final CustomerSession customerSession) {
		final ShoppingCart shoppingCart = getPrototypeBean(SHOPPING_CART, ShoppingCart.class);
		final Store store = getStoreService().findStoreWithCode(order.getStoreCode());

		shoppingCart.setCustomerSession(customerSession);
		customerSession.setShoppingCart(shoppingCart);
		customerSession.setCurrency(order.getCurrency());
		customerSession.setLocale(order.getLocale());

		shoppingCart.setBillingAddress(order.getBillingAddress());
		shoppingCart.setStore(store);

		return shoppingCart;
	}

	private CustomerSession createCustomerSessionForOrder(final Order order) {
		final Store store = getStoreService().findStoreWithCode(order.getStoreCode());
		final Shopper shopper = shopperService.findOrCreateShopper(order.getCustomer(), order.getStoreCode());
		final CustomerSession customerSessionWithShopper = customerSessionService.createWithShopper(shopper);
		customerSessionWithShopper.setCurrency(order.getCurrency());
		customerSessionWithShopper.setLocale(order.getLocale());
		return customerSessionService.initializeCustomerSessionForPricing(customerSessionWithShopper, store.getCode(), order.getCurrency());
	}

	@Override
	public OrderReturn createShipmentReturn(final OrderReturn orderReturn,
											final ReturnExchangeRefundTypeEnum type,
											final OrderShipment orderShipment,
											final EventOriginator originator) {

		orderReturn.updateOrderReturnableQuantity(orderReturn.getOrder(), getProductSkuLookup());
		getOrderReturnValidator().validate(orderReturn, orderShipment);

		orderReturn.recalculateOrderReturn();
		orderReturn.getOrder().setModifiedBy(originator);

		switch (type) {
			case PHYSICAL_RETURN_REQUIRED:
				orderReturn.setPhysicalReturn(true);
				break;
			case REFUND_TO_ORIGINAL:
				refundOrderShipmentReturn(orderReturn, orderShipment);
				break;
			case MANUAL_REFUND:
				manualRefundOrderReturn(orderReturn);
				break;
			default:
				throw new EpServiceException("Unexpected order return refund type: " + type);
		}

		final OrderReturnStatus status;
		if (type == PHYSICAL_RETURN_REQUIRED) {
			status = AWAITING_STOCK_RETURN;
		} else {
			status = COMPLETED;
		}
		finalizeReturn(status, orderReturn);

		OrderReturn result = getReturnAndExchangeService().add(orderReturn);
		getOrderEventHelper().logOrderReturnCreated(orderReturn.getOrder(), orderReturn);

		performPostCreateReturn(result);
		sendReturnExchangeEvent(result.getUidPk(), OrderEventType.RETURN_CREATED, null);
		return result;
	}

	private void finalizeReturn(final OrderReturnStatus status, final OrderReturn updatedOrderReturn) {

		updatedOrderReturn.setReturnStatus(status);
		updatedOrderReturn.normalizeOrderReturn();

		switch (updatedOrderReturn.getReturnType()) {
			case RETURN:
				updatedOrderReturn.getOrder().addReturn(updatedOrderReturn);
				break;
			case EXCHANGE:
				break;
			default:
				throw new EpServiceException("Unexpected order return type: " + updatedOrderReturn.getReturnType());
		}
	}

	@SuppressWarnings("fallthrough")
	@Override
	public OrderReturn createExchange(final OrderReturn orderReturn,
									  final ReturnExchangeRefundTypeEnum refundType,
									  final List<PaymentInstrumentDTO> paymentInstruments) {
		final ShoppingCart shoppingCart = orderReturn.getExchangeShoppingCart();
		shoppingCartService.saveIfNotPersisted(shoppingCart);
		final Address shippingAddress = shoppingCart.getShippingAddress();
		final Optional<ShippingOption> selectedShippingOption = shoppingCart.getSelectedShippingOption();
		cartOrderService.createOrderIfPossible(shoppingCart); // this call potentially resets shipping info
		shoppingCart.setShippingAddress(shippingAddress);
		selectedShippingOption.ifPresent(shoppingCart::setSelectedShippingOption);
		shoppingCartService.saveOrUpdate(shoppingCart);
		addCartOrderPaymentInstruments(shoppingCart, paymentInstruments);

		checkoutService.checkoutExchangeOrder(orderReturn, refundType == PHYSICAL_RETURN_REQUIRED);
		orderReturn.setExchangeOrder(shoppingCart.getCompletedOrder());

		try	{
			switch (refundType) {
				case PHYSICAL_RETURN_REQUIRED:
					orderReturn.setPhysicalReturn(true);
					break;
				case REFUND_TO_ORIGINAL:
					refundOrderShipmentReturn(orderReturn, orderReturn.getOrderShipmentForReturn());
					break;
				case MANUAL_REFUND:
					manualRefundOrderReturn(orderReturn);
					break;
				default:
					throw new EpServiceException("Unexpected order exchange refund type: " + refundType);
			}
		} catch (PaymentsException | IncorrectRefundAmountException paymentsException) {
			LOG.error(ERROR_OCCURRED_WHILE_PROCESSING_AN_EXCHANGE, paymentsException);
			orderService.cancelOrder(orderReturn.getExchangeOrder());
			orderReturn.setReturnStatus(CANCELLED);

			return orderReturn;
		}

		final OrderReturnStatus status;
		if (refundType == PHYSICAL_RETURN_REQUIRED) {
			status = AWAITING_STOCK_RETURN;
		} else {
			status = COMPLETED;
		}
		finalizeReturn(status, orderReturn);

		final OrderReturn result = getReturnAndExchangeService().add(orderReturn);
		getOrderEventHelper().logOrderExchangeCreated(orderReturn.getOrder(), orderReturn);

		performPostCreateReturn(result);
		sendReturnExchangeEvent(result.getUidPk(), OrderEventType.EXCHANGE_CREATED, null);

		return result;
	}

	/**
	 * Adds payment instruments to the shopping cart.
	 *
	 * @param shoppingCart   shopping cart
	 * @param instrumentDTOs payment instruments
	 */
	protected void addCartOrderPaymentInstruments(final ShoppingCart shoppingCart, final List<PaymentInstrumentDTO> instrumentDTOs) {
		final CartOrder cartOrder = cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());
		for (PaymentInstrumentDTO instrumentDTO : instrumentDTOs) {
            final CartOrderPaymentInstrument instrument = getPrototypeBean(CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class);

            instrument.setCartOrderUid(cartOrder.getUidPk());
            instrument.setPaymentInstrumentGuid(instrumentDTO.getGUID());
            instrument.setLimitAmount(BigDecimal.ZERO);
            instrument.setCurrency(shoppingCart.getShopper().getCurrency());

            cartOrderPaymentInstrumentService.saveOrUpdate(instrument);
        }
	}

	@Override
	public OrderReturn editReturn(final OrderReturn orderReturn) {
		if (orderReturn.isInTerminalState()) {
			throw new IllegalReturnStateException("Detected state: " + orderReturn.getReturnStatus());
		}
		orderReturn.normalizeOrderReturn();
		getOrderEventHelper().logOrderReturnChanged(orderReturn.getOrder(), orderReturn);

		OrderReturn result = getReturnAndExchangeService().update(orderReturn);
		performPostEditReturn(result);

		return result;
	}

	@Override
	public OrderReturn completeReturn(final OrderReturn orderReturn, final ReturnExchangeRefundTypeEnum refundType) {
		switch (refundType) {
			case REFUND_TO_ORIGINAL:
				refundOrderShipmentReturn(orderReturn, orderReturn.getOrderShipmentForReturn());
				break;
			case MANUAL_REFUND:
				manualRefundOrderReturn(orderReturn);
				break;
			default:
				throw new EpServiceException("Unexpected order return refund type: " + refundType);
		}

		orderReturn.setReturnStatus(COMPLETED);

		// the logging should happen with the original order return as it has the event originator
		getOrderEventHelper().logOrderReturnCompleted(orderReturn.getOrder(), orderReturn);
		return getReturnAndExchangeService().update(orderReturn);
	}

	@SuppressWarnings("fallthrough")
	@Override
	public OrderReturn completeExchange(final OrderReturn orderReturn, final ReturnExchangeRefundTypeEnum refundType) {
		Order exchangeOrder = orderReturn.getExchangeOrder();

		if (exchangeOrder == null) {
			throw new EpServiceException("Exchange order isn't specified.");
		}

		final OrderStatus exchangeOrderStatus = exchangeOrder.getStatus();
		final boolean exchangeOrderNotCancelled = !OrderStatus.CANCELLED.equals(exchangeOrderStatus);
		if (!OrderStatus.AWAITING_EXCHANGE.equals(exchangeOrderStatus) && exchangeOrderNotCancelled) {
			throw new EpServiceException("Cannot complete exchange order. Incorrect order state.");
		}

		final OrderReturn updatedOrderReturn = completeReturn(orderReturn, refundType);

		if (exchangeOrderNotCancelled) {
			exchangeOrder.setModifiedBy(getEventOriginator(orderReturn.getCreatedByCmUser()));
			exchangeOrder = orderService.releaseOrder(exchangeOrder);

			for (OrderShipment orderShipment : exchangeOrder.getPhysicalShipments()) {
				orderShipment.setStatus(OrderShipmentStatus.INVENTORY_ASSIGNED);
				for (final OrderSku skus : orderShipment.getShipmentOrderSkus()) {
					if (!skus.isAllocated()) {
						orderShipment.setStatus(OrderShipmentStatus.AWAITING_INVENTORY);
					}
				}
			}

			orderService.update(exchangeOrder);
		}
		return updatedOrderReturn;
	}

	@Override
	public OrderReturn cancelReturnExchange(final OrderReturn orderReturn) {
		if (orderReturn.getReturnStatus() != AWAITING_STOCK_RETURN) {
			throw new IllegalReturnStateException("Detected state: " + orderReturn.getReturnStatus());
		}
		orderReturn.setReturnStatus(CANCELLED);

		// Log the return/exchange canceled.
		try {
			if (orderReturn.getReturnType() == OrderReturnType.EXCHANGE) {
				getOrderEventHelper().logOrderExchangeCanceled(orderReturn.getOrder(), orderReturn);
			} else {
				getOrderEventHelper().logOrderReturnCanceled(orderReturn.getOrder(), orderReturn);
			}
			OrderReturn result = getReturnAndExchangeService().update(orderReturn);
			performPostCancelReturnExchange(orderReturn);

			Optional.ofNullable(orderReturn.getExchangeOrder())
					.ifPresent(exchangeOrder -> cancelExchangeOrder(exchangeOrder, orderReturn.getCreatedByCmUser()));

			return result;
		} catch (org.springframework.orm.jpa.JpaOptimisticLockingFailureException e) {
			throw new OrderReturnOutOfDateException("Return cannot be Canceled as it has been updated by another user.", e);
		}
	}

	private void cancelExchangeOrder(final Order exchangeOrder, final CmUser user) {
		exchangeOrder.setModifiedBy(getEventOriginator(user));
		orderService.cancelOrder(exchangeOrder);
	}

	@Override
	public OrderReturn receiveReturn(final OrderReturn orderReturn) {
		getOrderEventHelper().logOrderReturnReceived(orderReturn.getOrder(), orderReturn);
		return getReturnAndExchangeService().update(orderReturn);
	}

	@Override
	public List<OrderReturn> findOrderReturnBySearchCriteria(final OrderReturnSearchCriteria criteria, final int start, final int maxResults) {
		sanityCheck();
		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		CriteriaQuery query = orderCriterion.getOrderReturnSearchCriteria(criteria, ResultType.ENTITY);

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.ORDER_RETURN_INDEX);

		if (query.getParameters().isEmpty()) {
			return getPersistenceEngine()
					.withLoadTuners(loadTuner)
					.retrieve(query.getQuery());
		}

		return getPersistenceEngine()
				.withLoadTuners(loadTuner)
				.retrieve(query.getQuery(), query.getParameters().toArray());
	}

	@Override
	public long getOrderReturnCountBySearchCriteria(final OrderReturnSearchCriteria orderReturnSearchCriteria) {
		sanityCheck();
		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		CriteriaQuery query = orderCriterion.getOrderReturnSearchCriteria(orderReturnSearchCriteria, ResultType.COUNT);
		List<Long> orderCount;

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.ORDER_RETURN_INDEX);

		if (query.getParameters().isEmpty()) {
			orderCount = getPersistenceEngine()
					.withLoadTuners(loadTuner)
					.retrieve(query.getQuery());
		} else {
			orderCount = getPersistenceEngine()
					.withLoadTuners(loadTuner)
					.retrieve(query.getQuery(), query.getParameters().toArray());
		}

		return orderCount.get(0);
	}

	/**
	 * Commits the return's tax documents.
	 */
	private void performPostCreateReturn(final OrderReturn result) {

		getReturnTaxOperationService().commitDocument(result.calculateTaxes().getTaxDocument(), result);

		// Specifically calls update to save the tax document id for the order return
		getReturnAndExchangeService().update(result);

		if (result.getReturnType() != OrderReturnType.EXCHANGE) {
			return;
		}

		Order exchangeOrder = result.getExchangeOrder();

		for (OrderShipment shipment : exchangeOrder.getAllShipments()) {
			getTaxOperationService().commitDocument(shipment.calculateTaxes().getTaxDocument(), shipment);
		}
	}

	/**
	 * Reverse the return's tax document.
	 */
	private void performPostCancelReturnExchange(final OrderReturn result) {

		getReturnTaxOperationService().reverseTaxes(result, result.getOrderReturnAddress());
	}

	/**
	 * Reverse the return's previous tax document, and commits its updated tax document.
	 */
	private void performPostEditReturn(final OrderReturn result) {

		TaxDocumentModificationContext taxDocumentModificationContext = getPrototypeBean(
				TAX_DOCUMENT_MODIFICATION_CONTEXT, TaxDocumentModificationContext.class);
		taxDocumentModificationContext.add(result, result.getOrderReturnAddress(), TaxDocumentModificationType.UPDATE);

		result.resetTaxDocumentId();
		OrderReturn updatedOrderReturn = getReturnAndExchangeService().update(result);

		getReturnTaxOperationService().updateTaxes(updatedOrderReturn, taxDocumentModificationContext);
	}

	@Override
	public void resendReturnExchangeNotification(final long orderReturnUid, final String emailRecipient) {
		sendReturnExchangeEvent(orderReturnUid, OrderEventType.RESEND_RETURN_EXCHANGE_NOTIFICATION, emailRecipient);
	}

	private void sendReturnExchangeEvent(final long orderReturnUid, final EventType eventType, final String emailRecipient) {
		final Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("UID", orderReturnUid);
		if (emailRecipient != null) {
			additionalData.put("EMAIL", emailRecipient);
		}

		// Send notification via messaging system
		try {
			final EventMessage orderReturnExchangeCreatedEventMessage = getEventMessageFactory().createEventMessage(
					eventType,
					null,
					additionalData);

			getEventMessagePublisher().publish(orderReturnExchangeCreatedEventMessage);

		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	@Override
	public OrderReturn getOrderReturnPrototype(final OrderShipment orderShipment, final OrderReturnType orderReturnType) {
		final OrderReturn orderReturn = getPrototypeBean(ORDER_RETURN, OrderReturn.class);
		orderReturn.setReturnType(orderReturnType);

		final OrderShipmentHandler handler = retrieveOrderShipmentHandler(orderShipment);
		handler.handleOrderReturn(orderReturn, orderShipment);
		return orderReturn;
	}

	@Override
	public OrderShipmentHandler retrieveOrderShipmentHandler(final OrderShipment orderShipment) {
		return getOrderShipmentHandlerFactory().getOrderShipmentHandler(orderShipment.getOrderShipmentType());
	}

	/**
	 * The OrderShipmentHandlerFactory to set.
	 *
	 * @param orderShipmentHandlerFactory the orderShipmentHandlerFactory to set
	 */
	public void setOrderShipmentHandlerFactory(final OrderShipmentHandlerFactory orderShipmentHandlerFactory) {
		this.orderShipmentHandlerFactory = orderShipmentHandlerFactory;
	}

	/**
	 * Returns the OrderShipmentHandlerFactory instance.
	 *
	 * @return the orderShipmentHandlerFactory
	 */
	public OrderShipmentHandlerFactory getOrderShipmentHandlerFactory() {
		return orderShipmentHandlerFactory;
	}

	protected ReturnAndExchangeService getReturnAndExchangeService() {
		return getSingletonBean(ORDER_RETURN_SERVICE, ReturnAndExchangeService.class);
	}

	protected OrderEventHelper getOrderEventHelper() {
		return getSingletonBean(ORDER_EVENT_HELPER, OrderEventHelper.class);
	}

	public void setShopperService(final ShopperService shopperService) {
		this.shopperService = shopperService;
	}

	public ShopperService getShopperService() {
		return shopperService;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	public void setShippingOptionService(final ShippingOptionService shippingOptionService) {
		this.shippingOptionService = shippingOptionService;
	}

	public void setCustomerSessionService(final CustomerSessionService customerSessionService) {
		this.customerSessionService = customerSessionService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setCheckoutService(final CheckoutService checkoutService) {
		this.checkoutService = checkoutService;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	public ReturnTaxOperationService getReturnTaxOperationService() {
		return returnTaxOperationService;
	}

	public void setReturnTaxOperationService(final ReturnTaxOperationService returnTaxOperationService) {
		this.returnTaxOperationService = returnTaxOperationService;
	}

	public TaxOperationService getTaxOperationService() {
		return taxOperationService;
	}

	public void setTaxOperationService(final TaxOperationService taxOperationService) {
		this.taxOperationService = taxOperationService;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return this.eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return this.eventMessagePublisher;
	}

	public OrderReturnValidator getOrderReturnValidator() {
		return orderReturnValidator;
	}

	public void setOrderReturnValidator(final OrderReturnValidator orderReturnValidator) {
		this.orderReturnValidator = orderReturnValidator;
	}

	public PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

	public TaxSnapshotService getTaxSnapshotService() {
		return taxSnapshotService;
	}

	public void setTaxSnapshotService(final TaxSnapshotService taxSnapshotService) {
		this.taxSnapshotService = taxSnapshotService;
	}

	protected CartOrderService getCartOrderService() {
		return cartOrderService;
	}

	public void setCartOrderService(final CartOrderService cartOrderService) {
		this.cartOrderService = cartOrderService;
	}

	protected CartOrderPaymentInstrumentService getCartOrderPaymentInstrumentService() {
		return cartOrderPaymentInstrumentService;
	}

	public void setCartOrderPaymentInstrumentService(final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService) {
		this.cartOrderPaymentInstrumentService = cartOrderPaymentInstrumentService;
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}
}

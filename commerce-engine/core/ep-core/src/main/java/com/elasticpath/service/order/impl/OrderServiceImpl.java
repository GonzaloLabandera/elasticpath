/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.FETCH_GROUP_LOAD_TUNER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_CRITERION;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_EVENT_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_LOCK_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_API_CLEANUP_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_API_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SERVICE;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.SHIPMENT_ORDER_SKUS_INTERNAL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.openjpa.lib.jdbc.ReportingSQLException;
import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.catalog.InventoryAudit;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.EventOriginatorType;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.AllocationEventType;
import com.elasticpath.domain.order.AllocationResult;
import com.elasticpath.domain.order.DuplicateOrderException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderLock;
import com.elasticpath.domain.order.OrderMessageIds;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.order.PurchaseHistorySearchCriteria;
import com.elasticpath.domain.order.impl.AbstractOrderShipmentImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.OrderCriterion;
import com.elasticpath.persistence.support.OrderCriterion.ResultType;
import com.elasticpath.persistence.support.impl.CriteriaQuery;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.AllocationService;
import com.elasticpath.service.order.CompleteShipmentFailedException;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderLockService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.OrderShipmentNotFoundException;
import com.elasticpath.service.order.ReleaseShipmentFailedException;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.TaxDocumentModificationContext;
import com.elasticpath.service.tax.TaxDocumentModificationItem;
import com.elasticpath.service.tax.TaxDocumentModificationType;
import com.elasticpath.service.tax.TaxOperationService;

/**
 * Provides storage and access to <code>Order</code> objects.
 */
@SuppressWarnings({"PMD.ExcessiveClassLength", "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.GodClass"})
public class OrderServiceImpl extends AbstractEpPersistenceServiceImpl implements OrderService {

	private static final long MINUTE_IN_MS = 60L * 1000L; // 60 sec * 1000 msec
	private static final String LIST_PARAMETER_NAME = "list";

	private static final Logger LOG = Logger.getLogger(OrderServiceImpl.class);

	private TimeService timeService;

	private AllocationService allocationService;

	private LoadTuner defaultLoadTuner;

	private RuleService ruleService;

	private StoreService storeService;

	private TaxOperationService taxOperationService;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;


	private static final String ORDER_ALREADY_SUBMITTED = "Order has already been submitted and can't be resubmitted";

	/**
	 * SQL Error Codes. See:
	 * https://docs.oracle.com/cd/E11882_01/appdev.112/e10827/appd.htm
	 * https://dev.mysql.com/doc/refman/5.7/en/server-error-reference.html
	 */
	private static final String UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = "23000";
	private static final int MYSQL_DUPLICATE_ENTRY_ERROR_CODE = 1062;
	private static final int ORACLE_DUPLICATE_ENTRY_ERROR_CODE = 1;

	/**
	 * Adds the given order.
	 *
	 * @param order the order to add
	 * @return the persisted instance of order
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Order add(final Order order) {
		sanityCheck();
		order.setLastModifiedDate(timeService.getCurrentTime());

		try {
			getPersistenceEngine().save(order);

			getPersistenceEngine().flush();
		} catch (PersistenceException e) {
			if (isDuplicateOrderException(e)) {
				final StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(OrderMessageIds.ORDER_ALREADY_SUBMITTED,
						ORDER_ALREADY_SUBMITTED,
						null);
				throw new DuplicateOrderException(ORDER_ALREADY_SUBMITTED, Collections.singletonList(structuredErrorMessage), e);
			}
		}
		return populateRelationships(order);
	}

	/**
	 * Updates the given order.
	 *
	 * @param order the order to update
	 * @return the persisted instance of order
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	@SuppressWarnings("deprecation")
	public Order update(final Order order) {
		sanityCheck();
		order.setLastModifiedDate(timeService.getCurrentTime());

		Order persistedOrder = getPersistenceEngine().merge(order);
		// Avoid using storeService to avoid flushing the OpenJPA Context
		if (order.getStore() == null) {
			persistedOrder.setStore(getStore(order));
		} else {
			persistedOrder.setStore(order.getStore());
		}

		return persistedOrder;
	}

	@Override
	public Order update(final Order order, final TaxDocumentModificationContext taxDocumentModificationContext) {

		// update the order shipment document id as needed
		for (TaxDocumentModificationItem item : taxDocumentModificationContext.get(TaxDocumentModificationType.UPDATE)) {
			OrderShipment orderShipment = order.getShipment(item.getTaxDocumentReferenceId());
			orderShipment.resetTaxDocumentId();
		}
		// update the order with its shipments
		Order updatedOrder = update(order);

		// update the order shipment' tax documents
		getTaxOperationService().updateTaxes(updatedOrder, taxDocumentModificationContext);

		return updatedOrder;
	}


	/**
	 * Retrieves list of <code>Order</code> where the created date is later than the specified date.
	 *
	 * @param date date to compare with the created date
	 * @return list of <code>Order</code> whose created date is later than the specified date
	 */
	@Override
	public List<Order> findByCreatedDate(final Date date) {
		sanityCheck();
		final List<Order> orders = getPersistenceEngineWithDefaultLoadTuner().retrieveByNamedQuery("ORDER_SELECT_BY_CREATED_DATE", date);

		return populateRelationships(orders);
	}

	/**
	 * Retrieve the list of orders, whose specified property matches the given criteria value.
	 *
	 * @param propertyName  order property to search on.
	 * @param criteriaValue criteria value to be used for searching.
	 * @param isExactMatch  true for doing an exact match; false for doing a fuzzy match.
	 * @return list of orders matching the given criteria.
	 */
	@Override
	public List<Order> findOrder(final String propertyName, final String criteriaValue, final boolean isExactMatch) {
		if (propertyName == null || propertyName.length() == 0) {
			throw new EpServiceException("propertyName not set");
		}
		List<Order> orders;
		if (StringUtils.isNotBlank(criteriaValue)) {
			sanityCheck();
			if (isExactMatch) {
				orders = getPersistenceEngineWithDefaultLoadTuner()
						.retrieve("SELECT o FROM OrderImpl " + "as o WHERE o." + propertyName + " = ?1", criteriaValue);
			} else {
				orders = getPersistenceEngineWithDefaultLoadTuner()
						.retrieve("SELECT o FROM OrderImpl as o WHERE o." + propertyName + " LIKE ?1", "%" + criteriaValue + "%");
			}

		} else {
			orders = Collections.emptyList();
		}
		return populateRelationships(orders);
	}

	/**
	 * Retrieve the list of orders by the customer's guid. This will exclude failed orders. Use findOrdersBySearchCriteria()
	 * to retrieve the list including failed.
	 *
	 * @param customerGuid the customer's guid
	 * @param isExactMatch true for doing an exact match; false for doing a fuzzy match.
	 * @return list of orders matching the customer's guid.
	 */
	@Override
	public List<Order> findOrderByCustomerGuid(final String customerGuid, final boolean isExactMatch) {
		sanityCheck();

		CustomerSearchCriteria customerSearchCriteria = getPrototypeBean(CUSTOMER_SEARCH_CRITERIA, CustomerSearchCriteria.class);
		customerSearchCriteria.setGuid(customerGuid);
		customerSearchCriteria.setFuzzySearchDisabled(isExactMatch);

		OrderSearchCriteria orderSearchCriteria = getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		orderSearchCriteria.setCustomerSearchCriteria(customerSearchCriteria);
		orderSearchCriteria.setExcludedOrderStatus(OrderStatus.FAILED);

		return findOrdersBySearchCriteria(orderSearchCriteria, 0, Integer.MAX_VALUE, defaultLoadTuner);
	}

	@Override
	public List<Order> findOrdersByCustomerGuidAndStoreCode(final String customerGuid, final String storeCode, final boolean retrieveFullInfo) {
		sanityCheck();

		LoadTuner loadTuner;
		if (retrieveFullInfo) {
			loadTuner = defaultLoadTuner;
		} else {
			loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
			((FetchGroupLoadTuner) loadTuner).addFetchGroup(FetchGroupConstants.ORDER_LIST_BASIC);
		}

		final List<Order> orderList = getPersistenceEngineWithLoadTuner(loadTuner)
				.retrieveByNamedQuery("ORDER_SELECT_BY_CUSTOMER_GUID_AND_STORECODE",
						customerGuid,
						storeCode);

		return populateRelationships(orderList);
	}

	/**
	 * Retrieve the list of orders by the gift certificate code.
	 *
	 * @param giftCertificateCode the gift certificate code
	 * @param isExactMatch        true for doing an exact match; false for doing a fuzzy match.
	 * @return list of orders matching the gift certificate code.
	 */
	@Override
	public List<Order> findOrderByGiftCertificateCode(final String giftCertificateCode, final boolean isExactMatch) {
		sanityCheck();

		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		final List<Order> orderList = getPersistenceEngineWithDefaultLoadTuner().retrieve(
				orderCriterion.getOrderGiftCertificateCriteria("giftCertificateCode", giftCertificateCode, isExactMatch).getQuery());

		return populateRelationships(orderList);
	}

	/**
	 * order count function based on the OrderSearchCriteria.
	 *
	 * @param orderSearchCriteria the order search criteria.
	 * @return the count of orders matching the given criteria.
	 */
	@Override
	public long getOrderCountBySearchCriteria(final OrderSearchCriteria orderSearchCriteria) {
		sanityCheck();
		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		Collection<String> storeCodes = new LinkedList<>();
		CriteriaQuery query = orderCriterion.getOrderSearchCriteria(orderSearchCriteria, storeCodes, ResultType.COUNT);

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.ORDER_SEARCH);
		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		List<Long> orderCount;

		if (query.getParameters().isEmpty() && storeCodes.isEmpty()) {
			orderCount = getPersistenceEngineWithLoadTuner(loadTuner).retrieve(query.getQuery());
		} else if (storeCodes.isEmpty()) {
			orderCount = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), query.getParameters().toArray());
		} else {
			orderCount = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieveWithList(query.getQuery(), "storeList", storeCodes,
							query.getParameters().toArray(), 0, Integer.MAX_VALUE);
		}

		return orderCount.get(0);
	}

	/**
	 * Order search function based on the OrderSearchCriteria. This uses the ORDER_SEARCH fetch group
	 * load tuner and forces JOIN fetch mode.
	 *
	 * @param orderSearchCriteria the order search criteria.
	 * @param start               the starting record to search
	 * @param maxResults          the max results to be returned
	 * @return the list of orders matching the given criteria.
	 */
	@Override
	public List<Order> findOrdersBySearchCriteria(final OrderSearchCriteria orderSearchCriteria,
												  final int start,
												  final int maxResults) {
		sanityCheck();

		FetchGroupLoadTuner loadTuner = getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class);
		loadTuner.addFetchGroup(FetchGroupConstants.ORDER_SEARCH);

		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		return findOrdersBySearchCriteria(orderSearchCriteria, start, maxResults, loadTuner);
	}

	/**
	 * Find orders by search criteria using the given load tuner.
	 *
	 * @param orderSearchCriteria the order search criteria.
	 * @param start               the starting record to search
	 * @param maxResults          the max results to be returned
	 * @param loadTuner           the load tuner
	 * @return the list of orders matching the given criteria.
	 */
	@Override
	public List<Order> findOrdersBySearchCriteria(final OrderSearchCriteria orderSearchCriteria, final int start, final int maxResults,
												  final LoadTuner loadTuner) {

		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		Collection<String> storeCodes = new LinkedList<>();
		CriteriaQuery query = orderCriterion.getOrderSearchCriteria(orderSearchCriteria, storeCodes, ResultType.ENTITY);

		List<Order> orderList;
		if (query.getParameters().isEmpty() && storeCodes.isEmpty()) {
			orderList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), start, maxResults);
		} else if (storeCodes.isEmpty()) {
			orderList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieve(query.getQuery(), query.getParameters().toArray(), start, maxResults);
		} else {
			orderList = getPersistenceEngineWithLoadTuner(loadTuner)
					.retrieveWithList(query.getQuery(), "storeList", storeCodes, query.getParameters().toArray(), start, maxResults);
		}

		return populateRelationships(orderList);
	}

	/**
	 * Find orders by search criteria using the given load tuner.
	 *
	 * @param orderSearchCriteria the order search criteria.
	 * @param start               the starting record to search
	 * @param maxResults          the max results to be returned
	 * @return the list of order numbers matching the given criteria.
	 */
	@Override
	public List<String> findOrderNumbersBySearchCriteria(final OrderSearchCriteria orderSearchCriteria, final int start, final int maxResults) {

		final OrderCriterion orderCriterion = getPrototypeBean(ORDER_CRITERION, OrderCriterion.class);
		Collection<String> storeCodes = new LinkedList<>();
		CriteriaQuery query = orderCriterion.getOrderSearchCriteria(orderSearchCriteria, storeCodes, ResultType.ORDER_NUMBER);

		List<String> orderList;
		if (query.getParameters().isEmpty() && storeCodes.isEmpty()) {
			orderList = getPersistenceEngine().retrieve(query.getQuery(), start, maxResults);
		} else if (storeCodes.isEmpty()) {
			orderList = getPersistenceEngine().retrieve(query.getQuery(), query.getParameters().toArray(), start, maxResults);
		} else {
			orderList = getPersistenceEngine().retrieveWithList(query.getQuery(), "storeList", storeCodes,
					query.getParameters().toArray(), start, maxResults);
		}

		return orderList;
	}

	/**
	 * Get a map with lazy fields. By default,  <strong>AbstractOrderShipmentImpl.shipmentOrderSkusInternal</strong> field will be loaded.
	 * <p>
	 * If <strong>withFullDetails</strong> is true, then <strong>OrderSkuImpl.productSku</strong> field will be loaded too.
	 *
	 * @param withFullDetails if true, additional field will be loaded.
	 * @return a map with lazy fields.
	 */
	protected Map<Class<?>, String> getLazyFields(final boolean withFullDetails) {

		final Map<Class<?>, String> lazyFields = new HashMap<>();
		lazyFields.put(AbstractOrderShipmentImpl.class, SHIPMENT_ORDER_SKUS_INTERNAL);

		if (withFullDetails) {
			lazyFields.put(OrderSkuImpl.class, PRODUCT_SKU);
		}

		return lazyFields;
	}

	/**
	 * Returns a list of <code>Order</code> based on the given uids. The returned orders will be populated based on the given load tuner.
	 *
	 * @param orderUids a collection of order uids
	 * @return a list of <code>Order</code>s
	 */
	@Override
	public List<Order> findByUids(final Collection<Long> orderUids) {
		return findOrdersByUids(orderUids, false);
	}

	/**
	 * Returns a list of fully initialized <code>Order</code> objects based on the given uids. The returned orders will be populated based on the
	 * given load tuner.
	 *
	 * @param orderUids a collection of order uids
	 * @return a list of <code>Order</code>s
	 */
	@Override
	public List<Order> findDetailedOrdersByUids(final Collection<Long> orderUids) {
		return findOrdersByUids(orderUids, true);
	}

	/**
	 * Returns a list of <code>Order</code> based on the given uids. The returned orders will be populated based on the given load tuner.
	 *
	 * @param orderUids           a collection of order uids
	 * @param isDetailedFetchPlan if true, all lazy fields will be loaded. See #getLazyFields(boolean)
	 * @return a list of <code>Order</code>s
	 */
	private List<Order> findOrdersByUids(final Collection<Long> orderUids, final boolean isDetailedFetchPlan) {
		sanityCheck();

		if (orderUids == null || orderUids.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Class<?>, String> lazyFields = isDetailedFetchPlan
				? getLazyFields(true)
				: Collections.emptyMap();

		final List<Order> orders = getPersistenceEngineWithDefaultLoadTuner()
				.withLazyFields(lazyFields)
				.retrieveByNamedQueryWithList("ORDER_BY_UIDS", LIST_PARAMETER_NAME, orderUids);

		return populateRelationships(orders);
	}

	/**
	 * Get the order with the given UID. Return null if no matching record exists.
	 *
	 * @param orderUid the order UID
	 * @return the order if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Order get(final long orderUid) {
		sanityCheck();
		Order order;
		if (orderUid <= 0) {
			order = getPrototypeBean(ORDER, Order.class);
		} else {
			order = getPersistentBeanFinder()
					.withLoadTuners(defaultLoadTuner)
					.withLazyFields(getLazyFields(false))
					.get(ORDER, orderUid);
		}
		return populateRelationships(order);
	}

	/**
	 * Get the order with the given UID. Return <code>null</code> if no matching record exists. Fine tune the order with the given load tuner. If
	 * <code>null</code> is given, the default load tuner will be used.
	 *
	 * @param orderUid  the order UID
	 * @param loadTuner the load tuner to use (or <code>null</code> for the default)
	 * @return the order if UID exists, otherwise <code>null</code>
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public Order get(final long orderUid, final FetchGroupLoadTuner loadTuner) {
		sanityCheck();
		Order order;
		if (orderUid <= 0) {
			order = getPrototypeBean(ORDER, Order.class);
		} else {
			order = getPersistentBeanFinder()
					.withLoadTuners(loadTuner)
					.get(ORDER, orderUid);
		}

		return populateRelationships(order);
	}

	/**
	 * Generic get method for all persistable domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Object getObject(final long uid) {
		return get(uid);
	}

	/**
	 * Return the fully initialized order object.
	 *
	 * @param orderUid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Order getOrderDetail(final long orderUid) {
		sanityCheck();
		Order order;
		if (orderUid <= 0) {
			order = getPrototypeBean(ORDER, Order.class);
		} else {
			order = getPersistentBeanFinder()
					.withLoadTuners(defaultLoadTuner)
					.withLazyFields(getLazyFields(true))
					.get(ORDER, orderUid);
		}
		return populateRelationships(order);
	}

	@Override
	public void updateOrderShipmentStatus() {
		final long startTime = System.currentTimeMillis();
		LOG.debug("Start release shipments quartz job at: " + new Date(startTime));

		sanityCheck();

		List<OrderStatus> orderStatusList = new ArrayList<>();
		orderStatusList.add(OrderStatus.IN_PROGRESS);
		orderStatusList.add(OrderStatus.PARTIALLY_SHIPPED);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("listOrderStatus", orderStatusList);
		parameters.put("shipmentStatus", OrderShipmentStatus.INVENTORY_ASSIGNED);

		final List<Order> results = getPersistenceEngine().retrieveByNamedQuery("ORDERS_BY_ORDER_STATUS_AND_SHIPMENT_STATUS", parameters);

		for (final Order order : results) {
			releaseReleasableShipments(order);
		}

		LOG.debug("Release shipments quartz job completed in (ms): " + (System.currentTimeMillis() - startTime));
	}

	@Override
	public void releaseReleasableShipments(final Order order) {
		final long currentTime = timeService.getCurrentTime().getTime();
		final EventOriginator systemEventOriginator = getSystemEventOriginator();

		final List<PhysicalOrderShipment> shipments = order.getPhysicalShipments();
		final Store store = getStore(order);
		final long pickTimeStamp = currentTime - store.getWarehouse().getPickDelay() * MINUTE_IN_MS;
		final Date pickDate = new Date(pickTimeStamp);

		for (final OrderShipment shipment : shipments) {
			if (shipment.getCreatedDate().before(pickDate)
					&& OrderShipmentStatus.INVENTORY_ASSIGNED.equals(shipment.getShipmentStatus())) {
				try {
					shipment.getOrder().setModifiedBy(systemEventOriginator);
					getOrderService().processReleaseShipment(shipment);
				} catch (ReleaseShipmentFailedException e) {
					LOG.error("Quartz job release shipment failed.", e);
				}
			}
		}
	}

	/**
	 * Gets the system originator for automatic releasing of the ready shipments.
	 */
	private EventOriginator getSystemEventOriginator() {
		EventOriginatorHelper helper = getSingletonBean(EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
		return helper.getSystemOriginator();
	}

	/**
	 * Sends an OrderEvent in the case of the automatic release shipment failing.
	 */
	private void sendOrderEventForFailedReleaseShipment(final OrderShipment shipment, final String errorMessage) {
		final Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("shipmentType", shipment.getOrderShipmentType().getName());
		additionalData.put("orderGuid", shipment.getOrder().getOrderNumber());
		additionalData.put("errorMessage", errorMessage);

		sendOrderEvent(OrderEventType.ORDER_SHIPMENT_RELEASE_FAILED, shipment.getShipmentNumber(), additionalData);
	}

	/**
	 * Searches for order shipment with the specified shipment number and shipment type.
	 *
	 * @param shipmentNumber the order shipment number
	 * @param shipmentType   the type of shipment (physical or electronic)
	 * @return the shipment requested, or null if not found
	 */
	@Override
	public OrderShipment findOrderShipment(final String shipmentNumber, final ShipmentType shipmentType) {

		String queryName;
		if (ShipmentType.PHYSICAL.equals(shipmentType)) {
			queryName = "PHYSICAL_SHIPMENT_BY_SHIPMENT_NUMBER";
		} else {
			queryName = "ELECTRONIC_SHIPMENT_BY_SHIPMENT_NUMBER";
		}

		final List<OrderShipment> results = getPersistenceEngineWithDefaultLoadTuner().retrieveByNamedQuery(queryName, shipmentNumber);

		OrderShipment shipment = null;
		if (!results.isEmpty()) {
			shipment = results.get(0);
		}

		populateRelationships(shipment);

		return shipment;
	}

	@Override
	public Order completeShipment(final String shipmentNumber, final String trackingCode, final boolean captureFunds, final Date shipmentDate,
								  final boolean sendConfEmail, final EventOriginator eventOriginator) {
		List<PaymentEvent> paymentEvents = Collections.emptyList();
		try {
			// Capture funds. This will update the OrderShipment's Order's list of OrderPayment.
			if (captureFunds) {
				paymentEvents = getOrderService().processOrderShipmentPayment(shipmentNumber);
			}
			paymentEvents = paymentEvents.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType().equals(TransactionType.CHARGE))
					.filter(paymentEvent -> paymentEvent.getPaymentStatus().equals(PaymentStatus.APPROVED))
					.collect(Collectors.toList());

			final Order updatedOrder = getOrderService().processOrderShipment(shipmentNumber, trackingCode, shipmentDate, eventOriginator);

			sendOrderEvent(OrderEventType.ORDER_SHIPMENT_SHIPPED,
					shipmentNumber,
					ImmutableMap.of("orderGuid", updatedOrder.getGuid()));

			return updatedOrder;
		} catch (final Exception e) {
			if (!paymentEvents.isEmpty()) {
				getOrderPaymentApiService().rollbackShipmentCompleted(findOrderShipment(shipmentNumber), paymentEvents);
			}
			LOG.error("Complete shipment failed during release shipment phase.", e);
			throw new CompleteShipmentFailedException("Complete shipment failed. Caused by: " + e.getMessage(), e);
		}
	}

	@Override
	public List<PaymentEvent> processOrderShipmentPayment(final String shipmentNumber) {
		final PhysicalOrderShipment orderShipmentToComplete = (PhysicalOrderShipment) getOrderService().findOrderShipment(shipmentNumber,
				ShipmentType.PHYSICAL);

		if (orderShipmentToComplete == null) {
			throw new OrderShipmentNotFoundException("Unable to find Physical Shipment with number " + shipmentNumber);
		}

		List<PaymentEvent> paymentEvents = getOrderPaymentApiService().shipmentCompleted(orderShipmentToComplete);

		// Save the updated Order with its new orderPayment
		try {
			getOrderService().update(orderShipmentToComplete.getOrder());
		} catch (RuntimeException e) {
			//  Eat Exception to preserve existing logic flow in checkout
			LOG.error("Error occurred when attempting to update the order.", e);
		}
		return paymentEvents;
	}

	private OrderService getOrderService() {
		return getSingletonBean(ORDER_SERVICE, OrderService.class);
	}

	/**
	 * IMPORTANT: This method is defined for the transaction issue purpose, and should not be called by your code. Refactor the code to notify Spring
	 * to start a transaction on the processOrderShipment function. Release the order shipment after the payment is captured. Send the order shipment
	 * email and execute the extra tasks, eg. capture the payment for gift certificate. This method will run as an atomic DB transaction (This is
	 * specified in the Spring configuration)
	 *
	 * @param shipmentNumber  the number (GUID) of the PHYSICAL orderShipment to be released.
	 * @param trackingCode    the trackingCode for the orderShipment to be released.
	 * @param shipmentDate    the date of complete shipment process
	 * @param eventOriginator the event originator, could be cm user, ws user, customer or system originator. See {@link EventOriginatorHelper }
	 * @return the updated order
	 * @throws OrderShipmentNotFoundException if a physical shipment with the given number cannot be found
	 * @throws EpServiceException             if a single order can't be found with the given order number
	 */
	@Override
	public Order processOrderShipment(final String shipmentNumber, final String trackingCode, final Date shipmentDate,
									  final EventOriginator eventOriginator) {
		final PhysicalOrderShipment shipment = (PhysicalOrderShipment) getOrderService().findOrderShipment(shipmentNumber, ShipmentType.PHYSICAL);

		if (shipment == null) {
			throw new OrderShipmentNotFoundException("Unable to find Physical Shipment with number " + shipmentNumber);
		}

		shipment.getOrder().setModifiedBy(eventOriginator);

		// release the inventory
		for (OrderSku sku : shipment.getShipmentOrderSkus()) {

			allocationService.processAllocationEvent(sku, AllocationEventType.ORDER_SHIPMENT_COMPLETED, eventOriginator.getType().toString(),
					sku.getQuantity(), "Order shipment " + shipment.getShipmentNumber() + " completed");
		}

		shipment.setTrackingCode(trackingCode);
		if (shipmentDate == null) {
			shipment.setShipmentDate(timeService.getCurrentTime());
		} else {
			shipment.setShipmentDate(shipmentDate);
		}
		shipment.setStatus(OrderShipmentStatus.SHIPPED);

		return getOrderService().update(shipment.getOrder());
	}

	@Override
	public void refundOrderPayment(final Order order,
								   final List<PaymentInstrumentDTO> paymentInstruments,
								   final Money refundAmount,
								   final EventOriginator eventOriginator) {
		order.setModifiedBy(eventOriginator);

		if (!order.isRefundable()) {
			throw new EpServiceException("Order is not applicable for a refund.");
		}

		if (BigDecimal.ZERO.compareTo(refundAmount.getAmount()) >= 0) {
			throw new IncorrectRefundAmountException("Amount to refund must be positive.");
		}

		final OrderPaymentApiService orderPaymentApiService = getOrderPaymentApiService();
		final OrderPaymentAmounts amounts = orderPaymentApiService.getOrderPaymentAmounts(order);
		if (refundAmount.getAmount().compareTo(amounts.getAmountRefundable().getAmount()) > 0) {
			throw new IncorrectRefundAmountException("The refund amount exceeds the total amount captured for this order.");
		}

		orderPaymentApiService.refund(order, paymentInstruments, refundAmount);
	}

	@Override
	public void manualRefundOrderPayment(final Order order, final Money refundAmount, final EventOriginator eventOriginator) {
		order.setModifiedBy(eventOriginator);

		if (!order.isRefundable()) {
			throw new EpServiceException("Order is not applicable for a refund.");
		}

		if (BigDecimal.ZERO.compareTo(refundAmount.getAmount()) >= 0) {
			throw new IncorrectRefundAmountException("Amount to refund must be positive.");
		}
		final OrderPaymentApiService orderPaymentApiService = getOrderPaymentApiService();
		final OrderPaymentAmounts amounts = orderPaymentApiService.getOrderPaymentAmounts(order);
		if (refundAmount.getAmount().compareTo(amounts.getAmountRefundable().getAmount()) > 0) {
			throw new IncorrectRefundAmountException("The refund amount exceeds the total amount captured for this order.");
		}

		orderPaymentApiService.manualRefund(order, refundAmount);
	}

	/**
	 * Get the orderSku uid -> returned quantity map for the order with given uid.
	 *
	 * @param orderUid the uid of the order.
	 * @return the orderSku uid -> returned quantity map.
	 */
	@Override
	public Map<Long, Integer> getOrderSkuReturnQtyMap(final long orderUid) {
		// load the order instance
		final Order order = get(orderUid);

		final Multiset<Long> orderSkuReturnQtyMap = HashMultiset.create();
		if (order.getReturns() != null) {
			for (final OrderReturn orderReturn : order.getReturns()) {
				for (final OrderReturnSku orderReturnSku : orderReturn.getOrderReturnSkus()) {
					final Long orderSkuUid = orderReturnSku.getOrderSku().getUidPk();
					orderSkuReturnQtyMap.add(orderSkuUid, orderReturnSku.getQuantity());
				}
			}
		}

		HashMap<Long, Integer> result = Maps.newHashMap();
		for (Multiset.Entry<Long> entry : orderSkuReturnQtyMap.entrySet()) {
			result.put(entry.getElement(), entry.getCount());
		}

		return result;
	}

	/**
	 * Add the given <code>OrderReturn</code> to the order with given uid.
	 *
	 * @param order       the given order.
	 * @param orderReturn orderReturn to be added.
	 * @return the updated order.
	 */
	@Override
	public Order addOrderReturn(final Order order, final OrderReturn orderReturn) {
		order.addReturn(orderReturn);
		return update(order);
	}

	/**
	 * Returns all order uids as a list.
	 *
	 * @return all order uids as a list
	 */
	@Override
	public List<Long> findAllUids() {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_UIDS_ALL");
	}

	/**
	 * Retrieves list of <code>Order</code> uids where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Order</code> whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findUidsByModifiedDate(final Date date) {
		sanityCheck();
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_UIDS_SELECT_BY_MODIFIED_DATE", date);
	}

	/**
	 * Returns a list of ProductCodes for all Products of which the product's skus have been purchased by a particular user, given additional search
	 * criteria.
	 *
	 * @param criteria the criteria to use in finding the product codes
	 * @return distinct list of product codes corresponding to skus that were purchased by a user, filtered by the given search criteria
	 */
	@Override
	public List<String> findProductCodesPurchasedByUser(final PurchaseHistorySearchCriteria criteria) {
		List<String> result;

		if (criteria.getFromDate() == null) {
			result = getPersistenceEngine().retrieveByNamedQuery("FIND_PRODUCT_CODES_PURCHASED_BY_USER", criteria.getUserId(),
					criteria.getStoreCode(),
					criteria.getToDate());
		} else {
			result = getPersistenceEngine().retrieveByNamedQuery("FIND_PRODUCT_CODES_PURCHASED_BY_USER_BETWEEN_DATES",
					criteria.getUserId(),
					criteria.getStoreCode(),
					criteria.getToDate(),
					criteria.getFromDate());
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * Update the address for order, sometimes we don't want to update the whole order.
	 *
	 * @param address the given address.
	 */
	@Override
	public void updateAddress(final OrderAddress address) {
		getPersistenceEngine().update(address);
	}

	/**
	 * Given a list of domain objects fresh from persistence, populates related fields (i.e. Store) that are
	 * not directly linked from OpenJPA.
	 *
	 * @param orders The orders to populate
	 * @return the list of orders passed in (for chaining)
	 */
	protected List<Order> populateRelationships(final List<Order> orders) {
		for (Order order : orders) {
			populateRelationships(order);
		}

		return orders;
	}

	/**
	 * Given an order fresh from persistence, populates related fields (i.e. Store) that are
	 * not directly linked from OpenJPA.
	 *
	 * @param order The order to populate
	 * @return the order passed in (for chaining)
	 */
	@SuppressWarnings("deprecation")
	protected Order populateRelationships(final Order order) {
		if (order == null) {
			return null;
		}

		String storeCode = order.getStoreCode();
		if (storeCode != null) {
			order.setStore(getStoreService().findStoreWithCode(storeCode));
		}

		return order;
	}

	/**
	 * Given an order shipment fresh from persistence, populates related fields (i.e. Store) that are
	 * not directly linked from OpenJPA.
	 *
	 * @param shipment The order shipment to populate
	 * @return the order passed in (for chaining)
	 */
	protected OrderShipment populateRelationships(final OrderShipment shipment) {
		if (shipment == null) {
			return null;
		}

		populateRelationships(shipment.getOrder());

		return shipment;
	}

	/**
	 * Set the time service.
	 *
	 * @param timeService the <code>TimeService</code> instance.
	 */
	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	/**
	 * Set the allocation service.
	 *
	 * @param allocationService the <code>allocationService</code> instance.
	 */
	public void setAllocationService(final AllocationService allocationService) {
		this.allocationService = allocationService;
	}

	@Override
	public List<PhysicalOrderShipment> getAwaitingShipments(final Warehouse warehouse) {
		sanityCheck();
		List<PhysicalOrderShipment> releasedShipments =
				getPersistenceEngine().retrieveByNamedQuery("PHYSICAL_SHIPMENTS_BY_STATUS_AND_WAREHOUSE",
						OrderShipmentStatus.RELEASED, warehouse.getUidPk());
		List<PhysicalOrderShipment> pickableShipments = new ArrayList<>();

		//Released state is dependent on Order status
		for (PhysicalOrderShipment shipment : releasedShipments) {
			if (shipment.getShipmentStatus().equals(OrderShipmentStatus.RELEASED)) {
				pickableShipments.add(shipment);
			}
		}
		return pickableShipments;
	}

	@Override
	public Long getAwaitingShipmentsCount(final Warehouse warehouse) {
		sanityCheck();
		return getPersistenceEngine().<Long>retrieveByNamedQuery("COUNT_PHYSICAL_SHIPMENTS_BY_STATUS_AND_WAREHOUSE",
				OrderShipmentStatus.RELEASED,
				warehouse.getUidPk()).get(0);
	}

	/**
	 * Releases order lock if order was locked and updates the order.
	 *
	 * @param order  the order to be unlocked and updated.
	 * @param cmUser the user which is releasing the order lock.
	 * @throws EpServiceException in case of any errors,
	 *                            InvalidUnlockerException if when the orderLock was obtained not by the cmUser, but by some other user.
	 */
	@Override
	public void unlockAndUpdate(final Order order, final CmUser cmUser) {
		final OrderLockService orderLockService = getSingletonBean(ORDER_LOCK_SERVICE, OrderLockService.class);
		final OrderLock orderLock = orderLockService.getOrderLock(order);
		if (orderLock != null) {
			/* the order was locked, try to release it now. */
			orderLockService.releaseOrderLock(orderLock, cmUser);
		}
		update(order);
	}

	/**
	 * Cancel an order.
	 *
	 * @param order the order to be cancelled.
	 * @return the updated order
	 */
	@Override
	public Order cancelOrder(final Order order) {
		if (!order.isCancellable()) {
			throw new EpServiceException("Order is not cancellable.");
		}

		getOrderPaymentApiService().orderCanceled(order);

		getOrderEventHelper().logOrderCanceled(order);

		// Run it in a transaction.
		Order result = getOrderService().processOrderCancellation(order);

		getTaxOperationService().reverseTaxes(result);

		return result;
	}

	/**
	 * Cancel an order, update the db and objects in one transaction. This method should not be called outside of the service. Use cancelOrder()
	 * instead.
	 *
	 * @param order the order to be canceled.
	 * @return the updated order
	 */
	@Override
	public Order processOrderCancellation(final Order order) {
		// Since this is still a public method, should verify again that the order can be cancelled.
		if (!order.isCancellable()) {
			throw new EpServiceException("Order is not cancellable");
		}
		String eventOriginator = getEventOriginator(order);

		for (final OrderSku orderSku : order.getRootShoppingItems()) {
			if (orderSku.getSkuGuid() != null) {
				allocationService.processAllocationEvent(orderSku, AllocationEventType.ORDER_CANCELLATION, eventOriginator, orderSku
						.getAllocatedQuantity(), null);
			}
		}

		order.cancelOrder();

		sendOrderEvent(OrderEventType.ORDER_CANCELLED, order.getOrderNumber());

		return update(order);
	}

	/**
	 * Gets an event originator from an order.
	 *
	 * @param order the order
	 * @return the InventoryAudit String representing the event originator
	 */
	String getEventOriginator(final Order order) {
		String eventOriginator = InventoryAudit.EVENT_ORIGINATOR_WS;
		if (order.getModifiedBy() != null && order.getModifiedBy().getType() == EventOriginatorType.CMUSER) {
			eventOriginator = InventoryAudit.EVENT_ORIGINATOR_CMUSER + order.getModifiedBy().getCmUser().getGuid();
		}
		return eventOriginator;
	}

	/**
	 * Cancel an orderShipment.
	 *
	 * @param orderShipment the orderShipment to be canceled.
	 * @return the updated orderShipment
	 */
	@Override
	public PhysicalOrderShipment cancelOrderShipment(final PhysicalOrderShipment orderShipment) {
		try {
			Order freshOrder = get(orderShipment.getOrder().getUidPk());
			orderShipment.setOrder(freshOrder);
			getOrderPaymentApiService().shipmentCanceled(orderShipment);
		} catch (final Exception e) {
			// If anything wrong on the payment, won't affect the order
			// cancellation. Since we only try to reverse the auth.
			LOG.error("Reverse payment failed when cancelling order: " + orderShipment.getOrder().getOrderNumber(), e);
		}

		// Run it in a transaction.
		PhysicalOrderShipment result = getOrderService().processOrderShipmentCancellation(orderShipment);

		getTaxOperationService().reverseTaxes(result, orderShipment.getShipmentAddress());
		return result;
	}

	/**
	 * Cancel an orderShipment, update the db and objects in one transaction.
	 *
	 * @param orderShipment the orderShipment to be canceled.
	 * @return the updated orderShipment
	 */
	@Override
	public PhysicalOrderShipment processOrderShipmentCancellation(final PhysicalOrderShipment orderShipment) {
		Order order = orderShipment.getOrder();
		String eventOriginator = getEventOriginator(order);
		for (final OrderSku orderSku : orderShipment.getShipmentOrderSkus()) {
			if (orderSku.getSkuGuid() != null) {
				allocationService.processAllocationEvent(orderSku, AllocationEventType.ORDER_CANCELLATION, eventOriginator, orderSku
						.getAllocatedQuantity(), null);
			}
		}

		String shipmentNumber = orderShipment.getShipmentNumber();
		order.getShipment(shipmentNumber).setStatus(OrderShipmentStatus.CANCELLED);
		final Order updatedOrder = update(order);

		return (PhysicalOrderShipment) updatedOrder.getShipment(shipmentNumber);
	}

	/**
	 * Finds order by order number.
	 *
	 * @param orderNumber order number.
	 * @return the order
	 */
	@Override
	public Order findOrderByOrderNumber(final String orderNumber) {
		sanityCheck();
		getFetchPlanHelper().setFetchMode(FetchMode.JOIN);

		List<Order> results = getPersistenceEngineWithDefaultLoadTuner().retrieveByNamedQuery("ORDER_SELECT_BY_ORDERNUMBER", orderNumber);

		Order order = null;
		if (results.size() == 1) {
			order = results.get(0);
		} else if (results.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate order numbers exist -- " + orderNumber);
		}
		return order;
	}

	/**
	 * Places a hold on an order.
	 *
	 * @param order the order upon which to place a hold
	 * @return the modified order
	 */
	@Override
	public Order holdOrder(final Order order) {
		if (!order.isHoldable()) {
			LOG.error("Order (uidpk:  " + order.getUidPk() + " is not holdable but hold was invoked on it.");
		}
		order.holdOrder();

		getOrderEventHelper().logOrderOnHold(order);

		sendOrderEvent(OrderEventType.ORDER_HELD, order.getOrderNumber());

		return update(order);
	}

	@Deprecated
	@Override
	public Order releaseHoldOnOrder(final Order order) {
		return releaseOrder(order);
	}

	@Override
	public Order releaseOrder(final Order order) {
		if (!order.isReleasable()) {
			throw new EpServiceException("Cannot release order with status [" + order.getStatus() + "].");
		}

		final OrderStatus previousStatus = order.getStatus();

		order.releaseOrder();

		if (OrderStatus.ONHOLD.equals(previousStatus)) {
			// Log the order on hold released event.
			getOrderEventHelper().logOrderHoldReleased(order);

			// Capture payments for electronic items, since they have not been captured during checkout.
			captureImmediatelyShippableShipments(order);
		}

		getOrderEventHelper().logOrderReleasedForFulfilment(order);

		releaseReleasableShipments(order);

		sendOrderEvent(OrderEventType.ORDER_RELEASED, order.getOrderNumber(), null);

		return update(order);
	}

	/**
	 * Places order which exchange requires physical return to AWAITING_EXCHANGE state.
	 *
	 * @param order the order upon which to place a hold
	 * @return the modified order
	 */
	@Override
	public Order awaitExchangeCompletionForOrder(final Order order) {
		order.awaitExchangeCompletionOrder();
		return order;
	}

	@Override
	public OrderShipment processReleaseShipment(final OrderShipment orderShipment) {
		try {
			return processReleaseShipmentInternal(orderShipment);
		} catch (final ReleaseShipmentFailedException e) {
			sendOrderEventForFailedReleaseShipment(orderShipment, e.getMessage());
			return orderShipment;
		}
	}

	/**
	 * Internal method for releasing the shipment for pick/pack.  No {@code ORDER_SHIPMENT_RELEASE_FAILED} message is published in the event of a
	 * {@link ReleaseShipmentFailedException} being thrown.
	 *
	 * @param orderShipment the order shipment to be released
	 * @return the updated order shipment
	 * @throws ReleaseShipmentFailedException on error setting up the authorization payments
	 */
	protected OrderShipment processReleaseShipmentInternal(final OrderShipment orderShipment) {
		// We need to grab fresh shipment and do changes directly in DB, because of order locking issues.
		// If one RCP session releases the shipment, DB changes are taken place, but order won't be marked dirty
		// That's why if another session tries to release shipment again, it couldnt't detect that shipment was already released.
		Order freshOrder = get(orderShipment.getOrder().getUidPk());
		if (freshOrder == null) {
			throw new ReleaseShipmentFailedException("Cannot find order with uidPk: " + orderShipment.getOrder().getUidPk());
		}
		OrderShipment foundShipment = null;
		for (OrderShipment freshShipment : freshOrder.getAllShipments()) {
			if (freshShipment.getUidPk() == orderShipment.getUidPk()) {
				foundShipment = freshShipment;
				break;
			}
		}
		if (foundShipment == null) {
			throw new ReleaseShipmentFailedException("Shipment to be released cannot be found");
		}

		if (!OrderShipmentStatus.INVENTORY_ASSIGNED.equals(foundShipment.getShipmentStatus())) {
			throw new ReleaseShipmentFailedException("Shipment to be released is not in INVENTORY_ASSIGNED " + "state. Current state is "
					+ foundShipment.getShipmentStatus());
		}

		foundShipment.setStatus(OrderShipmentStatus.RELEASED);

		getOrderEventHelper().logOrderShipmentReleased(orderShipment.getOrder(), foundShipment);
		// update the order and get the updated order instance
		Order updatedOrder = update(foundShipment.getOrder());
		// grab the updated shipment instance from the updated order instance
		return updatedOrder.getShipment(foundShipment.getShipmentNumber());
	}

	/**
	 * Allocates the inventory for the given order.
	 *
	 * @param order the order
	 */
	private void allocateInventoryForOrder(final Order order) {
		for (final OrderSku orderSku : order.getOrderSkus()) {
			// Allocate inventory to the order
			final AllocationResult eventResult = allocationService.processAllocationEvent(orderSku, AllocationEventType.ORDER_PLACED,
					InventoryAudit.EVENT_ORIGINATOR_SF, orderSku.getQuantity(), null);
			final int allocatedQuantity = eventResult.getQuantityAllocatedInStock();
			orderSku.setAllocatedQuantity(allocatedQuantity);
			// Need to set the updated inventory object back into the product sku, otherwise the inventory change will be overwritten when
			// the order is saved.
		}
	}

	/**
	 * Allocates inventory, logs order event and saves the order to the database.
	 *
	 * @param order           the order
	 * @param isExchangeOrder true if the order is of exchange type
	 * @return the completed order object
	 * @throws EpServiceException on error
	 */
	@Override
	public Order processOrderOnCheckout(final Order order, final boolean isExchangeOrder) {

		if (order == null) {
			throw new IllegalArgumentException("Cannot process a null order");
		}

		allocateInventoryForOrder(order);

		if (isExchangeOrder) {
			getOrderEventHelper().logOrderExchangeCreated(order);
		} else {
			getOrderEventHelper().logOrderPlaced(order);
		}

		order.getCustomer().setFirstTimeBuyer(false);

		Order persistedOrder = update(order);
		persistedOrder.setModifiedBy(order.getModifiedBy());

		return persistedOrder;
	}

	@Override
	public void updateLimitedUsagePromotionCurrentNumbers(final Collection<Long> appliedRuleUids, final List<String> limitedUsagePromotionCodes) {

		if (CollectionUtils.isEmpty(limitedUsagePromotionCodes)) {
			return;
		}

		for (Long appliedRuleUid : appliedRuleUids) {
			Rule rule = getRuleService().get(appliedRuleUid);
			if (limitedUsagePromotionCodes.contains(rule.getCode())) {
				rule.setCurrentLupNumber(rule.getCurrentLupNumber() + 1);
				getPersistenceEngine().merge(rule);
			}
		}
	}

	@Override
	public List<Long> getFailedOrderUids(final Date toDate, final int maxResults) {
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_UID_FOR_FAILED_ORDERS_BEFORE_DATE", new Object[]{toDate}, 0, maxResults);
	}

	@Override
	public void deleteOrders(final List<Long> orderUids) {
		getOrderPaymentApiCleanupService().removeByOrderUidList(orderUids);
		getPersistenceEngine().executeNamedQueryWithList("DELETE_ORDER_BY_ORDER_UID_LIST", LIST_PARAMETER_NAME, orderUids);
	}

	@Override
	public String findLatestOrderGuidByCartOrderGuid(final String cartOrderGuid) {
		List<String> results = getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_GUIDS_BY_CART_ORDER_GUID",
				new Object[]{cartOrderGuid}, 0, 1);
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	@Override
	public List<String> findOrderNumbersByCustomerGuid(final String storeCode, final String customerGuid) {
		return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_NUMBERS_BY_CUSTOMER_GUID", customerGuid, storeCode);
	}

	@Override
	public OrderShipment findOrderShipment(final String shipmentNumber) {

		if (StringUtils.isBlank(shipmentNumber)) {
			throw new EpServiceException("shipmentNumber cannot be null or empty.");
		}
		OrderShipment shipment = null;
		final List<OrderShipment> results = getPersistenceEngineWithDefaultLoadTuner()
				.retrieveByNamedQuery("ABSTRACT_ORDER_SHIPMENT_BY_SHIPMENT_NUMBER", shipmentNumber);
		if (!results.isEmpty()) {
			shipment = results.get(0);
		}
		return shipment;
	}

	@Override
	public void captureImmediatelyShippableShipments(final Order order) {

		List<OrderShipment> nonPhysicalShipments = getImmediatelyShippableShipments(order);

		for (final OrderShipment orderShipment : nonPhysicalShipments) {
			getOrderPaymentApiService().shipmentCompleted(orderShipment);
			// if we don't have to pay for electronically shipments, we should still put the status on SHIPPED
			orderShipment.setStatus(OrderShipmentStatus.SHIPPED);
		}
	}

	/**
	 * Extracts the shipments whose payments must be captured during checkout.
	 *
	 * @param order the order.
	 * @return the list of shipments
	 */
	protected List<OrderShipment> getImmediatelyShippableShipments(final Order order) {
		return order.getAllShipments().stream()
				.filter(orderShipment -> !(orderShipment instanceof PhysicalOrderShipment))
				.collect(Collectors.toList());
	}

	@Override
	public void resendOrderConfirmationEvent(final String orderNumber) {
		sendOrderEvent(OrderEventType.RESEND_ORDER_CONFIRMATION, orderNumber, null);
	}

	/**
	 * Helper method which returns the store associated with a given order.
	 *
	 * @param order the order
	 * @return the store
	 */
	protected Store getStore(final Order order) {
		return getStoreService().findStoreWithCode(order.getStoreCode());
	}

	/**
	 * Triggers an order event.
	 *
	 * @param eventType   the type of Order Event to trigger
	 * @param orderNumber the order id associated with the event
	 */
	protected void sendOrderEvent(final EventType eventType, final String orderNumber) {
		sendOrderEvent(eventType, orderNumber, null);
	}

	/**
	 * Triggers an order event.
	 *
	 * @param eventType      the type of Order Event to trigger
	 * @param orderNumber    the order id associated with the event
	 * @param additionalData additional data to include in the message
	 */
	protected void sendOrderEvent(final EventType eventType, final String orderNumber, final Map<String, Object> additionalData) {
		// Send notification via messaging system
		try {
			final EventMessage eventMessage = getEventMessageFactory().createEventMessage(eventType, orderNumber, additionalData);

			getEventMessagePublisher().publish(eventMessage);

		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	/**
	 * Detects if the JpaSystemException thrown from the DB is caused by a duplicate order constraint violation.
	 *
	 * @param persistenceException The exception.
	 * @return boolean indicating the result.
	 */
	protected boolean isDuplicateOrderException(final PersistenceException persistenceException) {
		return Arrays.stream(persistenceException.getNestedThrowables())
				.filter(nestedThrowable -> nestedThrowable.getCause() instanceof ReportingSQLException)
				.anyMatch(nestedThrowable -> {
					ReportingSQLException reportingSQLException = (ReportingSQLException) nestedThrowable.getCause();
					return reportingSQLException.getSQLState().equals(UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE)
							&& (reportingSQLException.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR_CODE
							|| reportingSQLException.getErrorCode() == ORACLE_DUPLICATE_ENTRY_ERROR_CODE);
				});

	}

	public void setDefaultLoadTuner(final LoadTuner loadTuner) {
		defaultLoadTuner = loadTuner;
	}

	public void setRuleService(final RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	protected OrderEventHelper getOrderEventHelper() {
		return getSingletonBean(ORDER_EVENT_HELPER, OrderEventHelper.class);
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
		return eventMessagePublisher;
	}


	private PersistenceEngine getPersistenceEngineWithDefaultLoadTuner() {
		return getPersistenceEngine().withLoadTuners(defaultLoadTuner);
	}

	private PersistenceEngine getPersistenceEngineWithLoadTuner(final LoadTuner loadTuner) {
		return getPersistenceEngine().withLoadTuners(loadTuner);
	}

	protected OrderPaymentApiService getOrderPaymentApiService() {
		return getSingletonBean(ORDER_PAYMENT_API_SERVICE, OrderPaymentApiService.class);
	}

	protected OrderPaymentApiCleanupService getOrderPaymentApiCleanupService() {
		return getSingletonBean(ORDER_PAYMENT_API_CLEANUP_SERVICE, OrderPaymentApiCleanupService.class);
	}

}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ACCOUNT_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR;
import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.FETCH_GROUP_LOAD_TUNER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_CRITERION;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_EVENT_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_API_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SERVICE;
import static com.elasticpath.domain.order.OrderShipmentStatus.AWAITING_INVENTORY;
import static com.elasticpath.domain.order.OrderShipmentStatus.INVENTORY_ASSIGNED;
import static com.elasticpath.domain.order.OrderShipmentStatus.RELEASED;
import static com.elasticpath.domain.order.OrderShipmentStatus.SHIPPED;
import static com.elasticpath.domain.order.OrderStatus.CANCELLED;
import static com.elasticpath.domain.order.OrderStatus.COMPLETED;
import static com.elasticpath.domain.order.OrderStatus.FAILED;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.SHIPMENT_ORDER_SKUS_INTERNAL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.openjpa.lib.jdbc.ReportingSQLException;
import org.apache.openjpa.persistence.PersistenceException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.event.impl.EventOriginatorHelperImpl;
import com.elasticpath.domain.event.impl.EventOriginatorImpl;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.order.DuplicateOrderException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.impl.AbstractOrderShipmentImpl;
import com.elasticpath.domain.order.impl.ElectronicOrderShipmentImpl;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.OrderReturnImpl;
import com.elasticpath.domain.order.impl.OrderReturnSkuImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.domain.order.impl.ServiceOrderShipmentImpl;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.domain.store.impl.WarehouseImpl;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.persistence.support.OrderCriterion;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.persistence.support.impl.OrderCriterionImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.CompleteShipmentFailedException;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReleaseShipmentFailedException;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.TaxCalculationResult;
import com.elasticpath.service.tax.TaxOperationService;
import com.elasticpath.service.tax.impl.TaxCalculationResultImpl;

/**
 * Test <code>OrderServiceImpl</code>.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.ExcessiveClassLength", "PMD.CouplingBetweenObjects", "PMD.GodClass",
		"PMD.AvoidDecimalLiteralsInBigDecimalConstructor", "PMD.TooManyFields"})
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {

	private static final String GUID_VALUE = "guidValue";
	private static final String ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING = "The orders should come from the retrieve call";

	public static final Currency CAD = Currency.getInstance("CAD");
	private static final String SHIPMENT_NUMBER = "1-1";

	private OrderServiceImpl orderServiceImpl;

	private static final String ORDER_NUMBER = "order_1";

	private Store store;
	private Order order;
	private Order order2;

	@Mock
	private Order duplicateOrder;
	@Mock
	private PersistenceException persistenceException;
	@Mock
	private PersistenceException nestedPersistenceException;
	@Mock
	private ReportingSQLException duplicateSQLException;
	@Mock
	private TimeService mockTimeService;
	@Mock
	private OrderPaymentApiService mockOrderPaymentApiService;
	@Mock
	private StoreService mockStoreService;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private OrderEventHelper orderEventHelper;
	@Mock
	private PersistenceEngine persistenceEngine;
	@Mock
	private FetchPlanHelper fetchPlanHelper;
	@Mock
	private BeanFactory beanFactory;
	@Mock
	private TaxOperationService taxOperationService;

	private EventOriginatorHelper eventOriginatorHelper;

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	private static final String UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = "23000";
	private static final int MYSQL_DUPLICATE_ENTRY_ERROR_CODE = 1062;
	private static final int ORACLE_DUPLICATE_ENTRY_ERROR_CODE = 1;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@Before
	public void setUp() throws Exception {
		orderServiceImpl = new OrderServiceImpl();
		orderServiceImpl.setPersistenceEngine(persistenceEngine);
		orderServiceImpl.setFetchPlanHelper(fetchPlanHelper);

		when(beanFactory.getSingletonBean(ORDER_PAYMENT_API_SERVICE, OrderPaymentApiService.class))
				.thenReturn(mockOrderPaymentApiService);

		elasticPath.setBeanFactory(beanFactory);
		when(beanFactory.getBeanImplClass(ORDER)).thenAnswer(answer -> OrderImpl.class);
		when(beanFactory.getPrototypeBean(ORDER, Order.class)).thenReturn(new OrderImpl());

		when(beanFactory.getPrototypeBean(ORDER_CRITERION, OrderCriterion.class)).thenReturn(new OrderCriterionImpl());

		eventOriginatorHelper = new EventOriginatorHelperImpl();
		when(beanFactory.getSingletonBean(EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class)).thenReturn(eventOriginatorHelper);
		when(beanFactory.getSingletonBean(ORDER_EVENT_HELPER, OrderEventHelper.class)).thenReturn(orderEventHelper);
		when(beanFactory.getPrototypeBean(EVENT_ORIGINATOR, EventOriginator.class)).thenReturn(new EventOriginatorImpl());

		when(mockTimeService.getCurrentTime()).thenReturn(new Date());

		orderServiceImpl.setEventMessageFactory(eventMessageFactory);
		orderServiceImpl.setEventMessagePublisher(eventMessagePublisher);
		orderServiceImpl.setStoreService(mockStoreService);
		orderServiceImpl.setTimeService(mockTimeService);

		Warehouse warehouse = new WarehouseImpl();

		store = new StoreImpl();
		store.setCode("store");
		store.setWarehouses(singletonList(warehouse));

		when(mockStoreService.findStoreWithCode(store.getCode())).thenReturn(store);
		when(mockStoreService.calculateCurrentPickDelayTimestamp(store.getCode())).thenReturn(new Date());

		order = new OrderImpl();
		order.setUidPk(1L);
		order.setStoreCode(store.getCode());
		order.setOrderNumber(ORDER_NUMBER);
		order.setCurrency(Currency.getInstance(Locale.US));

		order2 = new OrderImpl();
		order2.setUidPk(1L);
		order2.setStoreCode(store.getCode());

		when(persistenceEngine.withLoadTuners(any(LoadTuner.class))).thenReturn(persistenceEngine);
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.setPersistenceEngine(PersistenceEngine)'.
	 */
	@Test(expected = EpServiceException.class)
	public void testSetPersistenceEngine() {
		orderServiceImpl.setPersistenceEngine(null);
		orderServiceImpl.add(new OrderImpl());
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.add(Order)'.
	 */
	@Test
	public void testAdd() {
		Order added = orderServiceImpl.add(order);
		verify(persistenceEngine).save(order);
		assertThat(added.getStore()).as("Store should have been set").isEqualTo(store);
	}

	/**
	 * Test findOrder with null inputs.
	 */
	@Test(expected = EpServiceException.class)
	public void testFindOrderWithNullsShouldThrowException() {
		orderServiceImpl.findOrder(null, null, true);
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.findOrder'.
	 */
	@Test
	public void testFindOrder() {
		when(persistenceEngine.retrieve(anyString(), anyString())).thenReturn(singletonList(order));

		final List<Order> resultList1 = orderServiceImpl.findOrder("orderNumber", ORDER_NUMBER, true);
		assertThat(resultList1).isEqualTo(singletonList(order));
		assertThat(order.getStore()).as("Store should be populated via the store code").isEqualTo(store);

		final List<Order> resultList2 = orderServiceImpl.findOrder("orderNumber", ORDER_NUMBER, false);
		assertThat(resultList2).isEqualTo(singletonList(order));
		assertThat(order.getStore()).as("Store should be populated via the store code").isEqualTo(store);
	}

	@Test
	public void testFindOrderByGiftCertificateCode() {
		final boolean isExactMatch = true;
		when(persistenceEngine.retrieve(anyString())).thenReturn(singletonList(order));

		assertThat(orderServiceImpl.findOrderByGiftCertificateCode("GC1", isExactMatch)).isEqualTo(singletonList(order));
		assertStoreIsPopulated(store, order);
	}

	@Test
	public void testAwaitingShipmentsCount() {
		final List<Long> orders = new ArrayList<>();
		orders.add(2L);

		Warehouse warehouse = new WarehouseImpl();

		when(persistenceEngine.retrieveByNamedQuery("COUNT_PHYSICAL_SHIPMENTS_BY_STATUS_AND_WAREHOUSE", RELEASED, warehouse.getUidPk()))
				.thenAnswer(answer -> orders);

		final Long result = orderServiceImpl.getAwaitingShipmentsCount(warehouse);
		assertThat(result).isEqualTo(2L);
		verify(persistenceEngine).retrieveByNamedQuery("COUNT_PHYSICAL_SHIPMENTS_BY_STATUS_AND_WAREHOUSE", RELEASED, warehouse.getUidPk());
	}

	@Test
	public void testFindOrderByCustomerGuid() {
		// expectations
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();
		when(beanFactory.getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class)).thenReturn(orderSearchCriteria);
		CustomerSearchCriteria customerSearchCriteria = new CustomerSearchCriteria();
		when(beanFactory.getPrototypeBean(CUSTOMER_SEARCH_CRITERIA, CustomerSearchCriteria.class)).thenReturn(customerSearchCriteria);
		when(persistenceEngine.retrieve(anyString(), any(Object[].class), anyInt(), anyInt())).thenReturn(singletonList(order));

		assertThat(orderServiceImpl.findOrderByCustomerGuid(GUID_VALUE, true))
				.as(ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING)
				.isEqualTo(singletonList(order));
		assertStoreIsPopulated(store, order);
		assertThat(customerSearchCriteria.getGuid())
				.as("The customer search criteria should include the guid")
				.isEqualTo(GUID_VALUE);
		assertThat(orderSearchCriteria.getCustomerSearchCriteria())
				.as("The order criteria should include the customer criteria")
				.isEqualTo(customerSearchCriteria);
		assertThat(orderSearchCriteria.getExcludedOrderStatus())
				.as("The order criteria should exclude failed orders")
				.isEqualTo(FAILED);
		assertThat(customerSearchCriteria.isFuzzySearchDisabled())
				.as("The customer search criteria should disable fuzzy match")
				.isTrue();

		assertThat(orderServiceImpl.findOrderByCustomerGuid(GUID_VALUE, false))
				.as(ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING)
				.isEqualTo(singletonList(order));
		assertThat(customerSearchCriteria.isFuzzySearchDisabled())
				.as("The customer search criteria should enable fuzzy match")
				.isFalse();
	}

	@Test
	public void testFindOrderByAccountGuid() {
		// expectations
		OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();
		when(beanFactory.getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class)).thenReturn(orderSearchCriteria);
		AccountSearchCriteria accountSearchCriteria = new AccountSearchCriteria();
		when(beanFactory.getPrototypeBean(ACCOUNT_SEARCH_CRITERIA, AccountSearchCriteria.class)).thenReturn(accountSearchCriteria);
		when(persistenceEngine.retrieve(anyString(), any(Object[].class), anyInt(), anyInt())).thenReturn(singletonList(order));

		assertThat(orderServiceImpl.findOrderByAccountGuid(GUID_VALUE, true))
				.as(ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING)
				.isEqualTo(singletonList(order));
		assertStoreIsPopulated(store, order);
		assertThat(accountSearchCriteria.getGuid())
				.as("The account search criteria should include the guid")
				.isEqualTo(GUID_VALUE);
		assertThat(orderSearchCriteria.getAccountSearchCriteria())
				.as("The order criteria should include the customer criteria")
				.isEqualTo(accountSearchCriteria);
		assertThat(orderSearchCriteria.getExcludedOrderStatus())
				.as("The order criteria should exclude failed orders")
				.isEqualTo(FAILED);
		assertThat(accountSearchCriteria.isFuzzySearchDisabled())
				.as("The account search criteria should disable fuzzy match")
				.isTrue();

		assertThat(orderServiceImpl.findOrderByAccountGuid(GUID_VALUE, false))
				.as(ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING)
				.isEqualTo(singletonList(order));
		assertThat(accountSearchCriteria.isFuzzySearchDisabled())
				.as("The customer search criteria should enable fuzzy match")
				.isFalse();
	}

	@Test
	public void testFindOrderByCustomerGuidAndStoreCode() {
		when(persistenceEngine.retrieveByNamedQuery("ORDER_SELECT_BY_CUSTOMER_GUID_AND_STORECODE", GUID_VALUE, store.getCode()))
				.thenReturn(singletonList(order));

		final boolean retrieveFullInfo = true;
		assertThat(orderServiceImpl.findOrdersByCustomerGuidAndStoreCode(GUID_VALUE, store.getCode(), retrieveFullInfo))
				.as(ORDERS_SHOULD_COME_FROM_RETRIVIAL_CALL_STRING)
				.isEqualTo(singletonList(order));
		assertStoreIsPopulated(store, order);
		verify(persistenceEngine).retrieveByNamedQuery("ORDER_SELECT_BY_CUSTOMER_GUID_AND_STORECODE", GUID_VALUE, store.getCode());
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.findByCreatedDate'.
	 */
	@Test
	public void testFindByCreatedDate() {
		final Date date = new Date();
		final List<Order> orders = singletonList(order);

		when(persistenceEngine.retrieveByNamedQuery("ORDER_SELECT_BY_CREATED_DATE", date)).thenAnswer(answer -> orders);
		assertThat(orderServiceImpl.findByCreatedDate(date)).isEqualTo(orders);
		assertStoreIsPopulated(store, order);
		verify(persistenceEngine).retrieveByNamedQuery("ORDER_SELECT_BY_CREATED_DATE", date);
	}

	@Test
	public void testFindOrdersBySearchCriteria() {
		when(beanFactory.getPrototypeBean(FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class)).thenReturn(new FetchGroupLoadTunerImpl());
		when(persistenceEngine.retrieve(anyString(), anyInt(), anyInt())).thenReturn(asList(order, order2));

		assertThat(orderServiceImpl.findOrdersBySearchCriteria(new OrderSearchCriteria(), 1, 1)).isEqualTo(asList(order, order2));
		assertStoreIsPopulated(store, order2);
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.get'.
	 */
	@Test
	public void testGet() {
		when(persistenceEngine.get(OrderImpl.class, order.getUidPk())).thenAnswer(answer -> order);

		final Order loadedOrder = orderServiceImpl.get(order.getUidPk());
		assertThat(loadedOrder).isSameAs(order);
		assertStoreIsPopulated(store, order);

		verify(persistenceEngine).get(OrderImpl.class, order.getUidPk());
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.getOrderDetail'.
	 */
	@Test
	public void testGetOrderDetail() {
		final FetchGroupLoadTuner mockFGLoadTuner = mock(FetchGroupLoadTuner.class);
		orderServiceImpl.setDefaultLoadTuner(mockFGLoadTuner);

		final Map<Class<?>, String> lazyFields = new HashMap<>();
		lazyFields.put(AbstractOrderShipmentImpl.class, SHIPMENT_ORDER_SKUS_INTERNAL);
		lazyFields.put(OrderSkuImpl.class, PRODUCT_SKU);

		when(persistenceEngine.get(OrderImpl.class, order.getUidPk())).thenAnswer(answer -> order);

		final Order loadedOrder = orderServiceImpl.getOrderDetail(order.getUidPk());
		assertThat(loadedOrder).isSameAs(order);
		assertStoreIsPopulated(store, order);

		verify(persistenceEngine).get(OrderImpl.class, order.getUidPk());
		verify(fetchPlanHelper).setLoadTuners(mockFGLoadTuner);
		verify(fetchPlanHelper).setLazyFields(lazyFields);
	}

	private OrderSku createOrderSku(final String guid, final long uid, final int qty) {
		OrderSku orderSku = new OrderSkuImpl();
		orderSku.setGuid(guid);
		orderSku.setUidPk(uid);
		orderSku.setPrice(qty, null);
		return orderSku;
	}

	private OrderReturnSku createOrderReturnSku(final String guid, final OrderSku orderSku, final int qtyReturned) {
		OrderReturnSku orderReturnSku = new OrderReturnSkuImpl();
		orderReturnSku.setGuid(guid);
		orderReturnSku.setOrderSku(orderSku);
		orderReturnSku.setQuantity(qtyReturned);
		return orderReturnSku;
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.getOrderSkuReturnQtyMap(orderUid)'.
	 */
	@Test
	public void testGetOrderSkuReturnQtyMap() {
		//Create an orderSku for 2 items
		final long skuUid1 = 111L;
		final int skuQty1 = 2;
		OrderSku orderSku1 = createOrderSku("OrderSku1", skuUid1, skuQty1);
		//Create a return for 1 of the first 2 items
		final int returnSku1Qty1 = 1;
		OrderReturnSku orderReturnSku1 = createOrderReturnSku("OrderReturnSku1", orderSku1, returnSku1Qty1);
		OrderReturn orderReturn1 = new OrderReturnImpl();
		orderReturn1.setRmaCode("rmaCode1");
		Set<OrderReturnSku> orderReturnSkus1 = new HashSet<>();
		orderReturnSkus1.add(orderReturnSku1);
		orderReturn1.setOrderReturnSkus(orderReturnSkus1);

		//Create a return for the second of the first OrderSku's 2 items
		final int returnSku1Qty2 = 1;
		OrderReturnSku orderReturnSku2 = createOrderReturnSku("OrderReturnSku2", orderSku1, returnSku1Qty2);

		//Create a second orderSku for 3 items
		final long skuUid2 = 222L;
		final int skuQty2 = 3;
		OrderSku orderSku2 = createOrderSku("orderSku2", skuUid2, skuQty2);
		//Create a return for 1 of the second orderSku's 3 items
		final int returnSku2Qty1 = 1;
		OrderReturnSku orderReturnSku3 = createOrderReturnSku("orderReturnSku3", orderSku2, returnSku2Qty1);

		//At this point the first OrderSku's 2 items should both be returned, and one of the second OrderSku's 3 items should be returned.
		//Add the OrderReturnSkus to an OrderReturn
		Set<OrderReturnSku> orderReturnSkus2 = new HashSet<>();
		orderReturnSkus2.add(orderReturnSku2);
		orderReturnSkus2.add(orderReturnSku3);
		OrderReturn orderReturn2 = new OrderReturnImpl();
		orderReturn2.setRmaCode("rmaCode2");
		orderReturn2.setOrderReturnSkus(orderReturnSkus2);

		final Order mockOrder = mock(Order.class);
		final long uid = 1234L;

		final Set<OrderReturn> orderReturns = new HashSet<>();
		orderReturns.add(orderReturn1);
		orderReturns.add(orderReturn2);
		when(mockOrder.getReturns()).thenReturn(orderReturns);

		OrderServiceImpl service = new OrderServiceImpl() {
			@Override
			public Order get(final long orderUid) {
				return mockOrder;
			}

		};

		Map<Long, Integer> orderSkuReturnedMap = service.getOrderSkuReturnQtyMap(uid);
		assertThat(orderSkuReturnedMap.get(skuUid1).intValue())
				.as("Two items should be returned from the first OrderSku.")
				.isEqualTo(returnSku1Qty1 + returnSku1Qty2);
		assertThat(orderSkuReturnedMap.get(skuUid2).intValue())
				.as("One item should be returned from the second OrderSku.")
				.isEqualTo(returnSku2Qty1);
	}

	/**
	 * Test method for 'com.elasticpath.service.OrderServiceImpl.findByUids(orderUids)'.
	 */
	@Test
	public void testFindByUids() {
		final List<Long> orderUids = asList(1L, 2L);

		// expectations
		final List<Order> orders = asList(order, order2);
		when(persistenceEngine.retrieveByNamedQueryWithList("ORDER_BY_UIDS", "list", orderUids)).thenAnswer(answer -> orders);
		when(persistenceEngine.withLazyFields(anyMap())).thenReturn(persistenceEngine);
		assertThat(this.orderServiceImpl.findByUids(orderUids)).isEqualTo(orders);
		assertStoreIsPopulated(store, order);

		// Should return an empty list if no product uid is given.
		List<Order> result = this.orderServiceImpl.findByUids(new ArrayList<>());
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(0);
		verify(persistenceEngine).retrieveByNamedQueryWithList("ORDER_BY_UIDS", "list", orderUids);
	}

	private PhysicalOrderShipmentImpl getMockPhysicalOrderShipment() {

		final TaxCalculationResult result = getTaxCalculationResult();

		return new PhysicalOrderShipmentImpl() {
			private static final long serialVersionUID = 7907342381488460247L;

			@Override
			public TaxCalculationResult calculateTaxes() {
				return result;
			}

			@Override
			public String getShipmentNumber() {
				return SHIPMENT_NUMBER;
			}

			@Override
			public long getUidPk() {
				return 2;
			}
		};

	}

	private ServiceOrderShipmentImpl getMockServiceOrderShipment() {

		final TaxCalculationResult result = getTaxCalculationResult();

		return new ServiceOrderShipmentImpl() {
			private static final long serialVersionUID = 7907342381488460247L;

			@Override
			public TaxCalculationResult calculateTaxes() {
				return result;
			}

			@Override
			public String getShipmentNumber() {
				return SHIPMENT_NUMBER;
			}

			@Override
			public long getUidPk() {
				return 2;
			}
		};

	}


	private ElectronicOrderShipmentImpl getMockElectronicOrderShipment() {

		final TaxCalculationResult result = getTaxCalculationResult();

		return new ElectronicOrderShipmentImpl() {
			private static final long serialVersionUID = 7907342381488460247L;

			@Override
			public TaxCalculationResult calculateTaxes() {
				return result;
			}

			@Override
			public String getShipmentNumber() {
				return SHIPMENT_NUMBER;
			}

			@Override
			public long getUidPk() {
				return 2;
			}
		};

	}

	private TaxCalculationResult getTaxCalculationResult() {
		final TaxCalculationResult result = new TaxCalculationResultImpl() {

			private static final long serialVersionUID = 4841309847005158665L;

			@Override
			public Money getBeforeTaxShippingCost() {
				return Money.valueOf(BigDecimal.ONE, Currency.getInstance(Locale.US));
			}

			@Override
			public Money getBeforeTaxSubTotal() {
				return Money.valueOf(BigDecimal.TEN, Currency.getInstance(Locale.US));
			}

		};
		result.setDefaultCurrency(Currency.getInstance(Locale.US));
		return result;
	}

	/**
	 * Test that when a capture fails while attempting to complete a shipment we deal with it nicely.
	 */
	@Test
	public void testCompleteShipmentCaptureFails() {
		final String orderNumber = "1";
		final String trackingCode = "code";
		final boolean captureFund = true;
		final boolean sendConfEmail = false;
		final List<OrderShipment> shipmentList = new ArrayList<>();
		PhysicalOrderShipmentImpl physicalOrderShipmentImpl = this.getMockPhysicalOrderShipment();

		shipmentList.add(physicalOrderShipmentImpl);

		Order order = new OrderImpl() {
			private static final long serialVersionUID = -2159834083423268104L;

			@Override
			public String getOrderNumber() {
				return orderNumber;
			}

		};
		order.setCurrency(CAD);
		order.setUidPk(1);
		order.setStoreCode(store.getCode());
		order.addShipment(physicalOrderShipmentImpl);

		when(beanFactory.getSingletonBean(ORDER_SERVICE, OrderService.class)).thenReturn(orderServiceImpl);
		when(persistenceEngine.retrieveByNamedQuery(eq("PHYSICAL_SHIPMENT_BY_SHIPMENT_NUMBER"), anyString()))
				.thenAnswer(answer -> shipmentList);

		doThrow(new RuntimeException("expected")).when(mockOrderPaymentApiService).shipmentCompleted(any(OrderShipment.class));

		Throwable thrown = catchThrowable(() -> orderServiceImpl.completeShipment(SHIPMENT_NUMBER, trackingCode, captureFund, null,
				sendConfEmail, eventOriginatorHelper.getSystemOriginator()));

		assertThat(thrown).isInstanceOf(CompleteShipmentFailedException.class);
		assertThat(thrown).isNotNull();
	}


	@Test
	public void testCancelOrder() {
		// Mock out getEventOriginator to make the test easier
		orderServiceImpl = new OrderServiceImpl() {
			@Override
			public Order update(final Order order) throws EpServiceException {
				return order;  // ignore persistence - not testing that
			}
		};
		orderServiceImpl.setEventMessageFactory(eventMessageFactory);
		EventMessage eventMessage = mock(EventMessage.class);
		given(eventMessageFactory.createEventMessage(eq(OrderEventType.ORDER_CANCELLED), any(), any())).willReturn(eventMessage);
		orderServiceImpl.setEventMessagePublisher(eventMessagePublisher);
		orderServiceImpl.setTaxOperationService(taxOperationService);
		PhysicalOrderShipmentImpl physicalOrderShipmentImpl = this.getMockPhysicalOrderShipment();

		// This order is cancellable
		Order order = new OrderImpl() {
			private static final long serialVersionUID = -392157231837612308L;

			@Override
			public boolean isCancellable() {
				return true;
			}

			@Override
			public List<OrderShipment> getAllShipments() {
				return singletonList(physicalOrderShipmentImpl);
			}
		};

		when(elasticPath.getSingletonBean(ORDER_SERVICE, OrderService.class)).thenReturn(orderServiceImpl);

		Order processedOrder = orderServiceImpl.cancelOrder(order);
		assertThat(processedOrder.getStatus()).isEqualTo(CANCELLED);
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void testCancelOrderWhenOrderIsNotCancellable() {
		// This order is cancellable
		Order order = new OrderImpl() {
			private static final long serialVersionUID = -392157231837612308L;

			@Override
			public boolean isCancellable() {
				return false;
			}
		};
		assertThatThrownBy(() -> orderServiceImpl.cancelOrder(order)).isInstanceOf(EpServiceException.class);
	}

	/**
	 * Test that when a capture fails we deal with it nicely.
	 */
	@Test
	public void testProcessOrderShipment() {
		when(beanFactory.getSingletonBean(ORDER_SERVICE, OrderService.class)).thenReturn(orderServiceImpl);

		final String orderNumber = "1";
		final String shipmentNumber = "2";
		final List<OrderShipment> shipmentList = new ArrayList<>();

		final PhysicalOrderShipmentImpl physicalOrderShipmentImpl = this.getMockPhysicalOrderShipment();
		physicalOrderShipmentImpl.setShipmentNumber(shipmentNumber);

		shipmentList.add(physicalOrderShipmentImpl);

		final Order order = new OrderImpl() {
			private static final long serialVersionUID = 5193999827719720358L;

			@Override
			public String getOrderNumber() {
				return orderNumber;
			}

		};
		order.setUidPk(1);
		order.setStoreCode(store.getCode());
		order.setCurrency(CAD);
		order.releaseOrder();
		order.addShipment(physicalOrderShipmentImpl);

		when(persistenceEngine.retrieveByNamedQuery(eq("PHYSICAL_SHIPMENT_BY_SHIPMENT_NUMBER"), anyString()))
				.thenAnswer(answer -> shipmentList);
		when(persistenceEngine.merge(any(Persistable.class))).thenReturn(order);
		final Order result = orderServiceImpl.processOrderShipment(shipmentNumber, null, null, eventOriginatorHelper.getSystemOriginator());
		assertThat(result.getStatus()).as("Order should now be in the COMPLETED state.").isEqualTo(COMPLETED);
		assertThat(result.getAllShipments().iterator().next()
				.getShipmentStatus()).as("Shipment should not be in the SHIPPED state.").isEqualTo(SHIPPED);
	}

	/**
	 * Test method for {@link OrderServiceImpl#get(long, FetchGroupLoadTuner).
	 */
	@Test
	public void testGetWithFGLoadTuner() {
		final FetchGroupLoadTuner mockFGLoadTuner = mock(FetchGroupLoadTuner.class);

		when(persistenceEngine.get(OrderImpl.class, order.getUidPk())).thenAnswer(answer -> order);

		assertThat(orderServiceImpl.get(order.getUidPk(), mockFGLoadTuner)).isSameAs(order);
		assertStoreIsPopulated(store, order);

		final long nonExistUid = 3456L;
		when(persistenceEngine.get(OrderImpl.class, nonExistUid)).thenReturn(null);

		assertThat(orderServiceImpl.get(nonExistUid, mockFGLoadTuner)).isNull();
		assertThat(orderServiceImpl.get(0, mockFGLoadTuner).getUidPk()).isEqualTo(0);
		verify(persistenceEngine).get(OrderImpl.class, order.getUidPk());
	}

	/**
	 * Tests adding order return and appropriate index update notification sending.
	 */
	@Test
	public void testAddOrderReturn() {
		final OrderReturn orderReturn = new OrderReturnImpl();

		when(persistenceEngine.merge(order)).thenReturn(order);

		order.setStoreCode(store.getCode());
		final Order returned = orderServiceImpl.addOrderReturn(order, orderReturn);
		assertThat(returned.getReturns()).as("Return should have been added").isEqualTo(singleton(orderReturn));
		assertThat(returned.getStore()).as("Store should be populated on return").isEqualTo(store);
		verify(persistenceEngine).merge(order);
	}

	@Test
	public void testRefundOrderPayment() {
		final Money refundAmount = Money.valueOf(BigDecimal.TEN, order.getCurrency());
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(SHIPPED);
		final OrderPaymentAmounts amounts = new OrderPaymentAmounts();
		amounts.setAmountRefundable(refundAmount);
		when(mockOrderPaymentApiService.getOrderPaymentAmounts(order)).thenReturn(amounts);

		orderServiceImpl.refundOrderPayment(order, ImmutableList.of(), refundAmount, new EventOriginatorImpl());

		verify(mockOrderPaymentApiService).refund(order, emptyList(), refundAmount);
	}

	@Test
	public void testManualRefundOrderPayment() {
		final Money refundAmount = Money.valueOf(BigDecimal.TEN, order.getCurrency());
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(SHIPPED);
		final OrderPaymentAmounts amounts = new OrderPaymentAmounts();
		amounts.setAmountRefundable(refundAmount);
		when(mockOrderPaymentApiService.getOrderPaymentAmounts(order)).thenReturn(amounts);

		orderServiceImpl.manualRefundOrderPayment(order, refundAmount, new EventOriginatorImpl());

		verify(mockOrderPaymentApiService).manualRefund(order, refundAmount);
	}

	@Test(expected = IncorrectRefundAmountException.class)
	public void testRefundOrderPaymentFailsForIncorrectRefundAmount() {
		final Money refundAmount = Money.valueOf(BigDecimal.TEN, order.getCurrency());
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(SHIPPED);
		final OrderPaymentAmounts amounts = new OrderPaymentAmounts();
		amounts.setAmountRefundable(refundAmount.subtract(Money.valueOf(BigDecimal.ONE, order.getCurrency())));
		when(mockOrderPaymentApiService.getOrderPaymentAmounts(order)).thenReturn(amounts);

		orderServiceImpl.refundOrderPayment(order, ImmutableList.of(), refundAmount, new EventOriginatorImpl());
	}

	@Test(expected = IncorrectRefundAmountException.class)
	public void testManualRefundOrderPaymentFailsForIncorrectRefundAmount() {
		final Money refundAmount = Money.valueOf(BigDecimal.TEN, order.getCurrency());
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(SHIPPED);
		final OrderPaymentAmounts amounts = new OrderPaymentAmounts();
		amounts.setAmountRefundable(refundAmount.subtract(Money.valueOf(BigDecimal.ONE, order.getCurrency())));
		when(mockOrderPaymentApiService.getOrderPaymentAmounts(order)).thenReturn(amounts);

		orderServiceImpl.manualRefundOrderPayment(order, refundAmount, new EventOriginatorImpl());
	}

	@Test(expected = EpServiceException.class)
	public void testRefundOrderPaymentFailsForShipmentAwaitingInventory() {
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(AWAITING_INVENTORY);

		orderServiceImpl.refundOrderPayment(order,
				ImmutableList.of(),
				Money.valueOf(BigDecimal.TEN, order.getCurrency()),
				new EventOriginatorImpl());
	}

	@Test(expected = EpServiceException.class)
	public void testRefundOrderPaymentFailsForReleasedShipment() {
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(RELEASED);

		orderServiceImpl.refundOrderPayment(order,
				ImmutableList.of(),
				Money.valueOf(BigDecimal.TEN, order.getCurrency()),
				new EventOriginatorImpl());
	}

	@Test(expected = EpServiceException.class)
	public void testManualRefundOrderPaymentFailsForReleasedShipment() {
		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);
		shipment.setStatus(RELEASED);

		orderServiceImpl.manualRefundOrderPayment(order,
				Money.valueOf(BigDecimal.TEN, order.getCurrency()),
				new EventOriginatorImpl());
	}

	@Test
	public void verifyFailureToReleaseOrderShipmentSendsEventMessage() {
		final String exceptionMessage = "Boom!";

		final OrderServiceImpl releaseShipmentFailingOrderServiceImpl = new OrderServiceImpl() {
			@Override
			protected OrderShipment processReleaseShipmentInternal(final OrderShipment orderShipment) throws ReleaseShipmentFailedException {
				throw new ReleaseShipmentFailedException(exceptionMessage);
			}
		};
		releaseShipmentFailingOrderServiceImpl.setEventMessageFactory(eventMessageFactory);
		releaseShipmentFailingOrderServiceImpl.setEventMessagePublisher(eventMessagePublisher);

		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		order.addShipment(shipment);

		final Map<String, Object> expectedEventMessageData = new HashMap<>();
		expectedEventMessageData.put("orderGuid", order.getGuid());
		expectedEventMessageData.put("shipmentType", shipment.getOrderShipmentType().toString());
		expectedEventMessageData.put("errorMessage", exceptionMessage);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(OrderEventType.ORDER_SHIPMENT_RELEASE_FAILED,
				shipment.getShipmentNumber(), expectedEventMessageData)
		).thenReturn(eventMessage);

		releaseShipmentFailingOrderServiceImpl.processReleaseShipment(shipment);
		verify(eventMessageFactory).createEventMessage(OrderEventType.ORDER_SHIPMENT_RELEASE_FAILED,
				shipment.getShipmentNumber(), expectedEventMessageData);
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void verifyCompleteShipmentSendsEventMessage() {
		final OrderService mockOrderService = mock(OrderService.class);

		when(beanFactory.getSingletonBean(ORDER_SERVICE, OrderService.class)).thenReturn(mockOrderService);

		final String shipmentNumber = "shipment-1";
		final String trackingCode = "TRACK-001";
		final Date shipmentDate = new Date();

		final EventOriginator eventOriginator = eventOriginatorHelper.getSystemOriginator();

		final boolean sendConfEmail = true;

		final Order order = mock(Order.class);

		when(mockOrderService.processOrderShipment(shipmentNumber, trackingCode, shipmentDate, eventOriginator))
				.thenReturn(order);
		when(order.getGuid()).thenReturn(ORDER_NUMBER);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(OrderEventType.ORDER_SHIPMENT_SHIPPED, shipmentNumber,
				ImmutableMap.of("orderGuid", ORDER_NUMBER))
		).thenReturn(eventMessage);


		orderServiceImpl.completeShipment(shipmentNumber, trackingCode, false, shipmentDate, sendConfEmail, eventOriginator);
		verify(eventMessagePublisher).publish(eventMessage);
		verify(eventMessageFactory).createEventMessage(OrderEventType.ORDER_SHIPMENT_SHIPPED, shipmentNumber,
				ImmutableMap.of("orderGuid", ORDER_NUMBER));
	}

	@Test
	public void shouldProcessOrderOnCheckoutAndMarkNewCustomerAsFirstTimeBuyer() {

		when(persistenceEngine.merge(order)).thenReturn(order);

		final Customer customer = new CustomerImpl();
		final EventOriginator originator = new EventOriginatorImpl();
		originator.setCustomer(customer);

		order.setCustomer(customer);
		order.setModifiedBy(originator);

		assertThat(customer.isFirstTimeBuyer()).as("New customer must be first time buyer").isTrue();

		final Order persistedOrder = orderServiceImpl.processOrderOnCheckout(order, false);

		assertThat(persistedOrder.getCustomer().isFirstTimeBuyer()).as("Customer with at least one order is not a first time buyer").isFalse();
	}

	@Test
	public void verifyReleaseOrderReturnsPersistedOrder() {
		final Order persistedOrder = mock(Order.class);
		when(persistenceEngine.merge(order)).thenReturn(persistedOrder);

		final Order actual = orderServiceImpl.releaseOrder(this.order);

		assertThat(actual).isSameAs(persistedOrder);
		verify(persistenceEngine).merge(order);
	}

	@Test
	public void verifyReleaseOrderSendsEventMessage() {
		ignorePersistence();

		EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(OrderEventType.ORDER_RELEASED, ORDER_NUMBER, null)).thenReturn(eventMessage);

		orderServiceImpl.releaseOrder(order);
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void verifyReleaseOrderLogsOrderHoldReleasedEventWhenInHoldState() {
		ignorePersistence();

		order.holdOrder();

		orderServiceImpl.releaseOrder(order);
		verify(orderEventHelper).logOrderHoldReleased(order);
	}

	@Test
	public void verifyReleaseOrderLogsOrderReleasedEvent() {
		ignorePersistence();

		orderServiceImpl.releaseOrder(order);
		verify(orderEventHelper).logOrderReleasedForFulfilment(order);
	}

	@Test
	public void verifyReleaseOrderThrowsExceptionWhenOrderNotInReleasableState() {
		order = createNonReleasableOrder();
		assertThatExceptionOfType(EpServiceException.class).isThrownBy(
				() -> orderServiceImpl.releaseOrder(order)).withMessageStartingWith("Cannot release order with status ");
	}

	@Test
	public void verifyShipmentsAreReleasedWhenOrderIsReleased() {
		final OrderService mockOrderService = mock(OrderService.class);
		when(beanFactory.getSingletonBean(ORDER_SERVICE, OrderService.class)).thenReturn(mockOrderService);

		ignorePersistence();

		final PhysicalOrderShipmentImpl shipment = getMockPhysicalOrderShipment();
		shipment.setCreatedDate(new Date(0L));
		shipment.setStatus(INVENTORY_ASSIGNED);

		order.addShipment(shipment);

		orderServiceImpl.releaseOrder(order);
		verify(mockOrderService).processReleaseShipment(shipment);
	}

	@Test
	public void verifyReleaseOrderCapturesElectronicShipmentPayments() {
		ignorePersistence();

		OrderShipment electronicShipment = getMockElectronicOrderShipment();
		order.addShipment(electronicShipment);

		// Given we are trying to release an order previously held.
		order.holdOrder();

		orderServiceImpl.releaseOrder(this.order);
		verify(mockOrderPaymentApiService).shipmentCompleted(electronicShipment);
	}

	@Test
	public void testCaptureNonPhysicalShipments() {
		ignorePersistence();

		OrderShipment electronicShipment = getMockElectronicOrderShipment();
		OrderShipment serviceShipment = getMockServiceOrderShipment();
		OrderShipment physicalShipment = getMockPhysicalOrderShipment();

		order.addShipment(electronicShipment);
		order.addShipment(serviceShipment);
		order.addShipment(physicalShipment);

		// Given we are trying to release an order previously held.
		order.holdOrder();

		orderServiceImpl.captureImmediatelyShippableShipments(order);
		verify(mockOrderPaymentApiService).shipmentCompleted(electronicShipment);
		verify(mockOrderPaymentApiService).shipmentCompleted(serviceShipment);
		verify(mockOrderPaymentApiService, never()).shipmentCompleted(physicalShipment);
	}

	@Test(expected = EpServiceException.class)
	public void testNullGetAccountGuidAssociatedWithOrderNumber() {
		orderServiceImpl.getAccountGuidAssociatedWithOrderNumber(null);
	}

	@Test
	public void testGetAccountGuidAssociatedWithOrderNumber() {
		when(persistenceEngine.retrieveByNamedQuery(anyString(), anyString())).thenReturn(emptyList());
		assertThat(orderServiceImpl.getAccountGuidAssociatedWithOrderNumber(ORDER_NUMBER)).isEqualTo(null);

		when(persistenceEngine.retrieveByNamedQuery(anyString(), anyString())).thenReturn(Collections.nCopies(2, GUID_VALUE));
		assertThatThrownBy(() -> orderServiceImpl.getAccountGuidAssociatedWithOrderNumber(ORDER_NUMBER)).isInstanceOf(EpServiceException.class);

		when(persistenceEngine.retrieveByNamedQuery(anyString(), anyString())).thenReturn(singletonList(GUID_VALUE));
		assertThat(orderServiceImpl.getAccountGuidAssociatedWithOrderNumber(ORDER_NUMBER)).isEqualTo(GUID_VALUE);
	}

	@Test
	public void verifyReleaseDoesntCapturePaymentsForElectronicItemsWhenOrderIsNotOnHold() {
		ignorePersistence();

		OrderShipment electronicShipment = getMockElectronicOrderShipment();
		order.addShipment(electronicShipment);

		// Given the order was not on hold.

		// When
		orderServiceImpl.releaseOrder(this.order);

		// Then there should be no call to orderPaymentApiService.shipmentCompleted
		verify(mockOrderPaymentApiService, never()).shipmentCompleted(electronicShipment);
	}

	@Test(expected = DuplicateOrderException.class)
	public void verifyDuplicateOrderExceptionIsThrownMySQL() {
		mockDuplicatePersistenceException(MYSQL_DUPLICATE_ENTRY_ERROR_CODE);
		orderServiceImpl.add(duplicateOrder);
	}

	@Test(expected = DuplicateOrderException.class)
	public void verifyDuplicateOrderExceptionIsThrownOracle() {
		mockDuplicatePersistenceException(ORACLE_DUPLICATE_ENTRY_ERROR_CODE);
		orderServiceImpl.add(duplicateOrder);
	}

	private void mockDuplicatePersistenceException(final int mySqlOracleErrorCode) {
		PersistenceException[] nestedThrowables = {nestedPersistenceException};
		when(duplicateSQLException.getErrorCode()).thenReturn(mySqlOracleErrorCode);
		when(duplicateSQLException.getSQLState()).thenReturn(UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE);
		when(persistenceException.getNestedThrowables()).thenReturn(nestedThrowables);
		when(nestedPersistenceException.getCause()).thenReturn(duplicateSQLException);
		doThrow(persistenceException).when(persistenceEngine).save(duplicateOrder);
	}

	private void assertStoreIsPopulated(final Store expectedStore, final Order order) {
		//  Pointless PMD-enforced craziness
		assertThat(order.getStore()).as("Store should be populated").isEqualTo(expectedStore);
	}

	private void ignorePersistence() {
		when(persistenceEngine.merge(order)).thenReturn(order);
	}

	private Order createNonReleasableOrder() {
		return new OrderImpl() {
			private static final long serialVersionUID = -8676337089930838989L;

			@Override
			public boolean isReleasable() {
				return false;
			}
		};
	}

}

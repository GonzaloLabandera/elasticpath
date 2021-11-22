/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.order.impl; // NOPMD All Imports required

import static com.elasticpath.domain.order.OrderReturnStatus.CANCELLED;
import static com.elasticpath.provider.payment.service.PaymentsExceptionMessageId.PAYMENT_FAILED;
import static com.elasticpath.service.order.ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Test;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductLocaleDependantFieldsImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.event.impl.EventOriginatorHelperImpl;
import com.elasticpath.domain.event.impl.EventOriginatorImpl;
import com.elasticpath.domain.event.impl.OrderEventHelperImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderEvent;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.order.OrderReturnSkuReason;
import com.elasticpath.domain.order.OrderReturnStatus;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.OrderTaxValue;
import com.elasticpath.domain.order.impl.OrderAddressImpl;
import com.elasticpath.domain.order.impl.OrderEventImpl;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.OrderReturnImpl;
import com.elasticpath.domain.order.impl.OrderReturnSkuImpl;
import com.elasticpath.domain.order.impl.OrderReturnSkuReasonImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.order.impl.OrderTaxValueImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartMementoImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCategory;
import com.elasticpath.domain.tax.TaxCategoryTypeEnum;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.domain.tax.TaxRegion;
import com.elasticpath.domain.tax.TaxValue;
import com.elasticpath.domain.tax.impl.TaxCategoryImpl;
import com.elasticpath.domain.tax.impl.TaxCodeImpl;
import com.elasticpath.domain.tax.impl.TaxJurisdictionImpl;
import com.elasticpath.domain.tax.impl.TaxRegionImpl;
import com.elasticpath.domain.tax.impl.TaxValueImpl;
import com.elasticpath.inventory.impl.InventoryDtoImpl;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.money.StandardMoneyFormatter;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.support.OrderCriterion;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.persistence.support.impl.OrderCriterionImpl;
import com.elasticpath.plugin.tax.domain.TaxAddress;
import com.elasticpath.plugin.tax.domain.TaxDocument;
import com.elasticpath.plugin.tax.domain.TaxOperationContext;
import com.elasticpath.plugin.tax.manager.TaxManager;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.misc.impl.DatabaseServerTimeServiceImpl;
import com.elasticpath.service.order.IllegalReturnStateException;
import com.elasticpath.service.order.OrderReturnValidator;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shipping.impl.ShippingOptionResultImpl;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.OrderSkuFactory;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.ShoppingItemSubtotalCalculator;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.impl.BundleApportioningCalculatorImpl;
import com.elasticpath.service.shoppingcart.impl.OrderSkuFactoryImpl;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.ReturnTaxOperationService;
import com.elasticpath.service.tax.TaxCalculationResult;
import com.elasticpath.service.tax.TaxCalculationService;
import com.elasticpath.service.tax.TaxCodeRetriever;
import com.elasticpath.service.tax.TaxJurisdictionService;
import com.elasticpath.service.tax.adapter.TaxAddressAdapter;
import com.elasticpath.service.tax.impl.DiscountApportioningCalculatorImpl;
import com.elasticpath.service.tax.impl.ReturnTaxOperationServiceImpl;
import com.elasticpath.service.tax.impl.TaxCalculationResultImpl;
import com.elasticpath.service.tax.impl.TaxCalculationServiceImpl;
import com.elasticpath.service.tax.impl.TaxJurisdictionServiceImpl;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.shipping.connectivity.dto.impl.ShippingOptionImpl;
import com.elasticpath.test.factory.TestCustomerSessionFactory;
import com.elasticpath.test.factory.TestShopperFactory;
import com.elasticpath.test.factory.TestTaxCalculationServiceImpl;
import com.elasticpath.test.jmock.AbstractEPServiceTestCase;

/**
 * Test cases for <code>ReturnAndExchangeServiceImpl</code>.
 */
@SuppressWarnings({"PMD.ExcessiveClassLength", "PMD.TooManyMethods", "PMD.TooManyFields"})
public class ReturnAndExchangeServiceImplOldTest extends AbstractEPServiceTestCase {

	private static final String ORDER_NUMBER = "10000";

	private static final BigDecimal ITEM_TAX = new BigDecimal("5.72");

	private static final String SALES_TAX_CODE_GOODS = "GOODS";

	private static final Currency CURRENCY = Currency.getInstance("CAD");

	private static final BigDecimal PRODUCT_PRICE = new BigDecimal("44");

	private static final String PST_TAX_CODE = "PST";

	private static final String GST_TAX_CODE = "GST";

	private static final String REGION_CODE_CA = "CA";

	private static final Locale DEFAULT_LOCALE = Locale.CANADA;

	private static final String REGION_CODE_BC = "BC";

	private static final String FAKE_ORDER_NUMBER = ORDER_NUMBER;

	private static final long FAKE_UIDPK = 10000L;
	private static final String DATA_FIELD_1 = "testField1";
	private static final String DATA_FIELD_2 = "testField2";
	private static final String DATA_VALUE_1 = "testValue1";
	private static final String DATA_VALUE_2 = "testValue2";

	private Order orderImpl;

	private ReturnAndExchangeServiceImpl exchangeService;

	private OrderServiceImpl orderServiceImpl;

	private CheckoutService mockCheckoutService;

	private CustomerSessionService mockCustomerSessionService;

	private ShopperService mockShopperService;

	private TimeService mockTimeService;

	private OrderEventHelperImpl dummyOrderEventHelper;

	private ProductSkuLookup mockProductSkuLookup;

	private ShippingOption shippingOption;

	private ProductSku productSku;

	private static final String CART_CURRENCY = "CAD";
	private StoreService storeService;

	private OrderReturnValidator mockOrderReturnValidator;

	@Mock
	private PricingSnapshotService pricingSnapshotService;

	@Mock
	private TaxSnapshotService taxSnapshotService;

	@Mock
	private ShoppingItemSubtotalCalculator shoppingItemSubtotalCalculator;

	@Mock
	private TaxManager taxManager;

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private EventMessagePublisher eventMessagePublisher;

	@Mock
	private OrderService orderService;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartOrderService cartOrderService;

	/**
	 * Prepare for the tests.
	 *
	 * @throws Exception on error
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		mockProductSkuLookup = context.mock(ProductSkuLookup.class);
		stubGetSingletonBean(ContextIdNames.MONEY_FORMATTER, StandardMoneyFormatter.class, StandardMoneyFormatter.class);
		stubGetSingletonBean(ContextIdNames.PRODUCT_SKU_LOOKUP, ProductSkuLookup.class, mockProductSkuLookup);
		stubGetSingletonBean(ContextIdNames.SHOPPING_ITEM_SUBTOTAL_CALCULATOR, ShoppingItemSubtotalCalculator.class, shoppingItemSubtotalCalculator);
		stubGetPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class, ModifierFieldsMapWrapper.class);
		/* setup order. */
		setupOrder();
		/* setup order service impl. */
		setupOrderService();
		/* setup checkout service: */
		setupCheckoutService();
		/* set up exchange service */
		setupReturnService();
		/* setup for order auditing */
		setUpForOrderAuditing();

		/* setup return tax operation service. */
		setUpTaxCalculationService();

		dummyOrderEventHelper = new OrderEventHelperImpl() {
			@Override
			public void logOrderExchangeCreated(final Order order) {
				// skip logging of the event
			}
		};
		dummyOrderEventHelper.setBeanFactory(getBeanFactory());
		dummyOrderEventHelper.setTimeService(new DummyTimeService());
		stubGetSingletonBean(ContextIdNames.ORDER_EVENT_HELPER, OrderEventHelper.class, dummyOrderEventHelper);

		setUpOrderSkuFactory();
	}

	private void setUpTaxCalculationService() {
		TaxCalculationServiceImpl taxCalculationService = new TestTaxCalculationServiceImpl();
		taxCalculationService.setBeanFactory(getBeanFactory());

		final TaxJurisdictionService taxJurisdictionService = new TaxJurisdictionServiceImpl() {
			@Override
			public TaxJurisdiction retrieveEnabledInStoreTaxJurisdiction(final String storeCode, final TaxAddress address) throws EpServiceException {
				return getTaxJurisdiction();
			}
		};

		taxCalculationService.setTaxJurisdictionService(taxJurisdictionService);
		taxCalculationService.setStoreService(storeService);

		ReturnTaxOperationServiceImpl taxOperationService = new ReturnTaxOperationServiceImpl();
		taxOperationService.setTaxCalculationService(taxCalculationService);
		taxOperationService.setBeanFactory(getBeanFactory());
		taxOperationService.setProductSkuLookup(mockProductSkuLookup);
		taxOperationService.setPricingSnapshotService(pricingSnapshotService);
		taxOperationService.setTaxManager(taxManager);
		TaxAddressAdapter adapter = new TaxAddressAdapter();
		stubGetSingletonBean(ContextIdNames.TAX_ADDRESS_ADAPTER, TaxAddressAdapter.class, adapter);
		taxOperationService.setAddressAdapter(adapter);
		exchangeService.setReturnTaxOperationService(taxOperationService);

		stubGetSingletonBean(ContextIdNames.RETURN_TAX_OPERATION_SERVICE, ReturnTaxOperationService.class, taxOperationService);
		stubGetSingletonBean(ContextIdNames.TAX_CALCULATION_SERVICE, TaxCalculationService.class, taxCalculationService);
		stubGetPrototypeBean(ContextIdNames.TAX_CALCULATION_RESULT, TaxCalculationResult.class, TaxCalculationResultImpl.class);
		stubGetPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class, LocalizedPropertiesImpl.class);
		stubGetPrototypeBean(ContextIdNames.ORDER_TAX_VALUE, OrderTaxValue.class, OrderTaxValueImpl.class);
		stubGetPrototypeBean(ContextIdNames.PRICE, Price.class, PriceImpl.class);

		context.checking(new Expectations() {
			{
				allowing(taxManager).commitDocument(with(any(TaxDocument.class)), with(any(TaxOperationContext.class)));
				allowing(taxManager).commitDocument(with(aNull(TaxDocument.class)), with(any(TaxOperationContext.class)));
			}
		});
	}

	private void setupOrder() {
		orderImpl = new TestOrderImpl();
		orderImpl.initialize();
		final Store store = getMockedStore();
		final Set<TaxCode> taxCodes = new HashSet<>();
		taxCodes.add(createTaxCode(TaxCode.TAX_CODE_SHIPPING));
		taxCodes.add(createTaxCode(SALES_TAX_CODE_GOODS));
		store.setTaxCodes(taxCodes);
		orderImpl.setStoreCode(store.getCode());


		orderImpl.setUidPk(FAKE_UIDPK);
		orderImpl.setOrderNumber(FAKE_ORDER_NUMBER);
		orderImpl.setCurrency(CURRENCY);
		orderImpl.setLocale(DEFAULT_LOCALE);
		orderImpl.setCustomer(getCustomer());
		orderImpl.setBillingAddress(mockOrderAddress());
		final OrderShipment shipment = mockOrderShipment();
		orderImpl.addShipment(shipment);
		orderImpl.setModifiedBy(new EventOriginatorImpl());
		orderImpl.getModifierFields().put(DATA_FIELD_1, DATA_VALUE_1);
		orderImpl.getModifierFields().put(DATA_FIELD_2, DATA_VALUE_2);
	}

	private void setupCheckoutService() {
		mockCheckoutService = context.mock(CheckoutService.class);
	}

	private void setUpForOrderAuditing() {
		stubGetPrototypeBean(ContextIdNames.ORDER_EVENT, OrderEvent.class, OrderEventImpl.class);
		stubGetSingletonBean(ContextIdNames.EVENT_ORIGINATOR, EventOriginator.class, EventOriginatorImpl.class);
		stubGetSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class, EventOriginatorHelperImpl.class);
	}

	private void setupOrderService() {
		orderServiceImpl = new OrderServiceImpl() {

			@Override
			public Order get(final long orderUid) throws EpServiceException {
				return new OrderImpl();
			}

			@Override
			public Order add(final Order order) throws EpServiceException {
				final Order savedOrder = super.add(order);
				savedOrder.setUidPk(1);
				savedOrder.setStoreCode(getMockedStore().getCode());
				return savedOrder;
			}

			@Override
			public Order update(final Order order) throws EpServiceException {
				order.setStoreCode(getMockedStore().getCode());
				return order;
			}

			@Override
			public void refundOrderPayment(final Order order,
										   final List<PaymentInstrumentDTO> paymentInstruments,
										   final Money refundAmount,
										   final EventOriginator eventOriginator) {
				// nothing
			}

			@Override
			public void manualRefundOrderPayment(final Order order,
												 final Money refundAmount,
												 final EventOriginator eventOriginator) {
				// nothing
			}
		};
		orderServiceImpl.setPersistenceEngine(getPersistenceEngine());

		OrderCriterion orderCriterion = new OrderCriterionImpl();
		stubGetPrototypeBean(ContextIdNames.ORDER_CRITERION, OrderCriterion.class, orderCriterion);

		mockTimeService = context.mock(TimeService.class);
		stubGetSingletonBean(ContextIdNames.TIME_SERVICE, TimeService.class, mockTimeService);
		context.checking(new Expectations() {
			{
				allowing(mockTimeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});
		orderServiceImpl.setTimeService(mockTimeService);

		stubGetSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class, orderServiceImpl);

	}

	@SuppressWarnings("unchecked")
	private void setupReturnService() {
		exchangeService = new ReturnAndExchangeServiceImpl() {
			@Override
			protected ReturnAndExchangeService getReturnAndExchangeService() {
				return exchangeService;
			}

			@Override
			protected OrderEventHelper getOrderEventHelper() {
				return dummyOrderEventHelper;
			}
		};
		exchangeService.setPersistenceEngine(getPersistenceEngine());

		exchangeService.setEventMessageFactory(eventMessageFactory);
		exchangeService.setEventMessagePublisher(eventMessagePublisher);

		mockCustomerSessionService = context.mock(CustomerSessionService.class);
		exchangeService.setCustomerSessionService(mockCustomerSessionService);

		mockShopperService = context.mock(ShopperService.class);
		exchangeService.setShopperService(mockShopperService);

		mockOrderReturnValidator = context.mock(OrderReturnValidator.class);
		exchangeService.setOrderReturnValidator(mockOrderReturnValidator);

		exchangeService.setShoppingCartService(shoppingCartService);
		exchangeService.setCartOrderService(cartOrderService);

		context.checking(new Expectations() {
			{
				allowing(mockTimeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});
		exchangeService.setTimeService(mockTimeService);
		exchangeService.setProductSkuLookup(mockProductSkuLookup);

		shippingOption = getShippingOption();

		final ShippingOptionService shippingOptionService = context.mock(ShippingOptionService.class);
		context.checking(new Expectations() {
			{
				allowing(shippingOptionService).getShippingOptions(with(any(ShoppingCart.class)));
				will(returnValue(createShippingOptionResult(singletonList(shippingOption))));
			}
		});

		exchangeService.setShippingOptionService(shippingOptionService);

		stubGetPrototypeBean(ContextIdNames.CUSTOMER_SESSION, CustomerSession.class, new CustomerSessionImpl());

		storeService = context.mock(StoreService.class);
		context.checking(new Expectations() {
			{
				allowing(storeService).findStoreWithCode(getMockedStore().getCode());
				will(returnValue(getMockedStore()));
			}
		});
		stubGetSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class, storeService);

		final OrderReturnSkuReason orderReturnSkuReason = new OrderReturnSkuReasonImpl();
		orderReturnSkuReason.setPropertiesMap(new HashMap<>());
		stubGetPrototypeBean(ContextIdNames.ORDER_RETURN_SKU_REASON, OrderReturnSkuReason.class, orderReturnSkuReason);

		exchangeService.setCheckoutService(mockCheckoutService);
		exchangeService.setOrderService(orderServiceImpl);
		exchangeService.setStoreService(storeService);

		exchangeService.setPricingSnapshotService(pricingSnapshotService);
		stubGetSingletonBean(ContextIdNames.PRICING_SNAPSHOT_SERVICE, PricingSnapshotService.class, pricingSnapshotService);
		exchangeService.setTaxSnapshotService(taxSnapshotService);
		stubGetSingletonBean(ContextIdNames.TAX_SNAPSHOT_SERVICE, TaxSnapshotService.class, taxSnapshotService);
		final OrderSkuImpl orderSku = (OrderSkuImpl) orderImpl.getAllShipments().get(0).getShipmentOrderSkus().iterator().next();
		context.checking(new Expectations() {
			{
				allowing(pricingSnapshotService).getPricingSnapshotForOrderSku(with(any(OrderSkuImpl.class)));
				will(returnValue(orderSku));

				allowing(taxSnapshotService).getTaxSnapshotForOrderSku(with(any(OrderSkuImpl.class)), with(any(OrderSkuImpl.class)));
				will(returnValue(orderSku));
			}
		});

		context.checking(new Expectations() {
			{
				allowing(shoppingItemSubtotalCalculator).calculate(
						with(any(Stream.class)),
						with(any(ShoppingCartPricingSnapshot.class)),
						with(any(Currency.class))
				);
				will(returnValue(Money.valueOf(BigDecimal.ZERO, CURRENCY)));
			}
		});
	}

	private void setUpOrderSkuFactory() {
		final TaxCodeImpl taxCode = new TaxCodeImpl();
		taxCode.setCode(SALES_TAX_CODE_GOODS);

		final TaxCodeRetriever taxCodeRetriever = context.mock(TaxCodeRetriever.class);
		context.checking(new Expectations() {
			{
				allowing(taxCodeRetriever).getEffectiveTaxCode(with(any(ProductSku.class)));
				will(returnValue(taxCode));

				allowing(mockTimeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});

		OrderSkuFactoryImpl orderSkuFactory = new OrderSkuFactoryImpl() {
			@Override
			protected OrderSku createSimpleOrderSku() {
				return new OrderSkuImpl();
			}
		};
		orderSkuFactory.setTaxCodeRetriever(taxCodeRetriever);
		orderSkuFactory.setProductSkuLookup(mockProductSkuLookup);
		orderSkuFactory.setBundleApportioner(new BundleApportioningCalculatorImpl());
		orderSkuFactory.setDiscountApportioner(new DiscountApportioningCalculatorImpl());
		orderSkuFactory.setTimeService(mockTimeService);
		stubGetSingletonBean(ContextIdNames.ORDER_SKU_FACTORY, OrderSkuFactory.class, orderSkuFactory);

		stubGetPrototypeBean(ContextIdNames.ORDER_RETURN_SKU, OrderReturnSku.class, OrderReturnSkuImpl.class);
		stubGetSingletonBean(ContextIdNames.ORDER_RETURN_SERVICE, ReturnAndExchangeService.class, exchangeService);
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.add()'.
	 */
	@Test
	public void testAdd() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		orderReturn.setOrder(new OrderImpl());
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).save(with(same(orderReturn)));
			}
		});
		exchangeService.add(orderReturn);
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.add() with dummy payment'.
	 */
	@Test
	public void testAdd2() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		orderReturn.setOrder(new OrderImpl());
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).save(with(same(orderReturn)));
			}
		});

		exchangeService.add(orderReturn);
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.findAllUids()'.
	 */
	@Test
	public void testFind() {
		final List<Long> listOfUids = new ArrayList<>();
		listOfUids.add(FAKE_UIDPK);
		context.checking(new Expectations() {
			{
				allowing(getMockPersistenceEngine()).retrieveByNamedQuery(with("ORDER_RETURN_UIDS_LIST"), with(any(Object[].class)));
				will(returnValue(listOfUids));
			}
		});

		assertEquals(listOfUids, exchangeService.findAllUids());
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.update()'.
	 */
	@Test
	public void testUpdate() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		orderReturn.setOrder(new OrderImpl());
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).merge(with(same(orderReturn)));
				will(returnValue(null));
			}
		});
		exchangeService.update(orderReturn);
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.list(final long uidPk)'.
	 */
	@Test
	public void testList() {
		final long uid = 1L;
		final OrderReturn orderReturn = new OrderReturnImpl();
		final List<OrderReturn> orderReturns = new ArrayList<>();
		orderReturns.add(orderReturn);
		// expectations
		context.checking(new Expectations() {
			{
				allowing(getMockPersistenceEngine()).retrieveByNamedQuery("ORDER_RETURN_LIST", uid);
				will(returnValue(orderReturns));
			}
		});
		assertSame(orderReturns, exchangeService.list(uid));
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.list(final long uidPk, final OrderReturnType returnType)'.
	 */
	@Test
	public void testList2() {
		final long uid = 1L;
		final OrderReturn orderReturn = new OrderReturnImpl();
		final List<OrderReturn> orderReturns = new ArrayList<>();
		orderReturns.add(orderReturn);
		// expectations
		context.checking(new Expectations() {
			{
				allowing(getMockPersistenceEngine()).retrieveByNamedQuery(
						"ORDER_RETURN_LIST_BY_RETURN_TYPE", uid, OrderReturnType.RETURN);
				will(returnValue(orderReturns));
			}
		});
		assertSame(orderReturns, exchangeService.list(uid, OrderReturnType.RETURN));
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.list()'.
	 */
	@Test
	public void testList3() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		final List<OrderReturn> orderReturns = new ArrayList<>();
		orderReturns.add(orderReturn);
		// expectations
		context.checking(new Expectations() {
			{
				allowing(getMockPersistenceEngine()).retrieveByNamedQuery(
						with("ORDER_EXCHANGE_AND_RETURN_LIST_BY_ORDER_UID"), with(any(Object[].class)));
				will(returnValue(orderReturns));
			}
		});

		assertSame(orderReturns, exchangeService.list());
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.get(uid)'.
	 */
	@Test
	public void testGet() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		stubGetPrototypeBean(ContextIdNames.ORDER_RETURN, OrderReturn.class, OrderReturnImpl.class);

		stubGetPrototypeBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class, FetchGroupLoadTunerImpl.class);
		exchangeService.setFetchPlanHelper(getMockFetchPlanHelper());

		context.checking(new Expectations() {
			{
				oneOf(getMockFetchPlanHelper()).setFetchMode(with(FetchMode.JOIN));
			}
		});
		assertSame(orderReturn.getUidPk(), exchangeService.get(0).getUidPk());
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.get(uid)'.
	 */
	@Test
	public void testGet2() {
		stubGetPrototypeBean(ContextIdNames.FETCH_GROUP_LOAD_TUNER, FetchGroupLoadTuner.class, FetchGroupLoadTunerImpl.class);
		stubGetPrototypeBean(ContextIdNames.ORDER_RETURN, OrderReturn.class, OrderReturnImpl.class);

		final long uidPk = 1;
		final OrderReturn orderReturn = new OrderReturnImpl();
		orderReturn.setUidPk(uidPk);
		exchangeService.setFetchPlanHelper(getMockFetchPlanHelper());

		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).get(with(same(OrderReturnImpl.class)), with(uidPk));
				will(returnValue(orderReturn));

				oneOf(getMockFetchPlanHelper()).setLoadTuners(with(any(LoadTuner[].class)));
				oneOf(getMockFetchPlanHelper()).setFetchMode(with(FetchMode.JOIN));
			}
		});

		assertSame(orderReturn, exchangeService.get(uidPk));
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.populateShoppingCart()'.
	 */
	@Test
	public void testPopulateShoppingCartContainsCheckoutDetailsFromOriginalOrder() {
		stubGetPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class, ShoppingCartImpl.class);
		stubGetPrototypeBean(ContextIdNames.SHOPPING_CART_MEMENTO, ShoppingCartMemento.class, ShoppingCartMementoImpl.class);

		final Shopper shopper = TestShopperFactory.getInstance().createNewShopperWithMemento();
		final CustomerSession customerSession = TestCustomerSessionFactory.getInstance().createNewCustomerSessionWithContext(shopper);
		context.checking(new Expectations() {
			{
				allowing(mockCustomerSessionService).createWithShopper(with(any(Shopper.class)));
				will(returnValue(customerSession));

				allowing(mockCustomerSessionService)
						.initializeCustomerSessionForPricing(with(equal(customerSession)), with(any(String.class)), with(any(Currency.class)));

				allowing(mockShopperService).findOrCreateShopper(with(any(Customer.class)), with(any(String.class)));
				will(returnValue(shopper));

				allowing(pricingSnapshotService).getPricingSnapshotForCart(with(any(ShoppingCart.class)));
				will(returnValue(new ShoppingCartImpl()));

				allowing(taxSnapshotService).getTaxSnapshotForCart(with(any(ShoppingCart.class)), with(any(ShoppingCartPricingSnapshot.class)));
				will(returnValue(new ShoppingCartImpl()));

				allowing(mockTimeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});

		final OrderReturn exchangeOrder = new OrderReturnImpl();

		exchangeOrder.populateOrderReturn(orderImpl, orderImpl.getAllShipments().get(0), OrderReturnType.RETURN);

		final List<ShoppingItem> cartItemList = mockCartItemList();

		final Address shippingAddress = mockShippingAddress();
		final ShoppingCart shoppingCart = exchangeService.populateShoppingCart(exchangeOrder, cartItemList, shippingOption, shippingAddress);
		final Optional<ShippingOption> selectedShippingOption = shoppingCart.getSelectedShippingOption();

		assertEquals("Incorrect shipping address set on the exchange shopping cart", shippingAddress, shoppingCart.getShippingAddress());
		assertTrue("No shipping option set on the exchange shopping cart", selectedShippingOption.isPresent());
		assertEquals("Incorrect shipping option set on the exchange shopping cart", shippingOption, selectedShippingOption.get());
		assertEquals("Incorrect list of cart items set on the exchange shopping cart", cartItemList, shoppingCart.getAllShoppingItems());
		assertEquals("Incorrect cart data size", 2, shoppingCart.getModifierFields().getMap().size());
		assertEquals("Incorrect cart data of Field1", DATA_VALUE_1, shoppingCart.getModifierFields().get(DATA_FIELD_1));
		assertEquals("Incorrect cart data of Field2", DATA_VALUE_2, shoppingCart.getModifierFields().get(DATA_FIELD_2));
	}

	/**
	 * Test that you can't edit an OrderReturn with a Completed status -
	 * an {@link IllegalReturnStateException} should be thrown.
	 */
	@Test
	public void testEditReturnCompletedStateThrowsException() {
		final OrderReturn mockOrderReturn = context.mock(OrderReturn.class);
		context.checking(new Expectations() {
			{
				allowing(mockOrderReturn).getReturnStatus();
				will(returnValue(OrderReturnStatus.COMPLETED));

				allowing(mockOrderReturn).isInTerminalState();
				will(returnValue(true));
			}
		});
		try {
			exchangeService.editReturn(mockOrderReturn);
			fail("Should never rich here.");
		} catch (final IllegalReturnStateException e) { //NOPMD
			//empty
		}
	}

	/**
	 * Test that when an OrderReturn is received, the order event helper logs the receipt and
	 * then the OrderReturn is saved by retrieving the ReturnAndExchange service again from Spring
	 * (for transactions).
	 */
	@Test
	public void testReceiveReturn() {
		final OrderReturn mockOrderReturn = context.mock(OrderReturn.class);
		final OrderEventHelper mockOrderEventHelper = context.mock(OrderEventHelper.class);
		final ReturnAndExchangeService mockReturnAndExchangeService = context.mock(ReturnAndExchangeService.class);
		context.checking(new Expectations() {
			{
				oneOf(mockOrderReturn).getOrder();

				oneOf(mockOrderEventHelper).logOrderReturnReceived(with(any(Order.class)), with(any(OrderReturn.class)));

				oneOf(mockReturnAndExchangeService).update(with(same(mockOrderReturn)));
			}
		});

		final ReturnAndExchangeServiceImpl service = new ReturnAndExchangeServiceImpl() {
			@Override
			protected OrderEventHelper getOrderEventHelper() {
				return mockOrderEventHelper;
			}

			@Override
			protected ReturnAndExchangeService getReturnAndExchangeService() {
				return mockReturnAndExchangeService;
			}

		};
		service.receiveReturn(mockOrderReturn);
	}

	private static TaxCode createTaxCode(final String taxCodeName) {
		final TaxCode taxCode = new TaxCodeImpl();
		taxCode.setCode(taxCodeName);
		taxCode.setGuid(System.currentTimeMillis() + taxCodeName);
		return taxCode;
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.manualRefundOrderReturn()'.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateShipmentReturn() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		final OrderShipment orderShipment = orderImpl.getAllShipments().get(0);
		orderReturn.populateOrderReturn(orderImpl, orderShipment, OrderReturnType.RETURN);
		final OrderReturnSku orderReturnSku = orderReturn.getOrderReturnSkus().iterator().next();
		orderReturnSku.setQuantity(1);
		orderReturnSku.setOrderSku(orderShipment.getShipmentOrderSkus().iterator().next());

		context.checking(new Expectations() {
			{
				allowing(mockOrderReturnValidator).validate(orderReturn, null);

				allowing(getMockPersistenceEngine()).retrieveByNamedQuery("ORDER_RETURN_LIST", FAKE_UIDPK);
				will(returnValue(emptyList()));

				allowing(getMockPersistenceEngine()).save(orderReturn);

				allowing(getMockPersistenceEngine()).merge(orderReturn);
				will(returnValue(orderReturn));

				allowing(eventMessageFactory).createEventMessage(
						with(OrderEventType.RETURN_CREATED),
						with(aNull(String.class)),
						with(any(Map.class)));
				allowing(eventMessagePublisher).publish(with(any(EventMessage.class)));
			}
		});

		exchangeService.createShipmentReturn(
				orderReturn,
				ReturnExchangeRefundTypeEnum.MANUAL_REFUND,
				null,
				new EventOriginatorImpl());
	}

	/**
	 * Test method for 'com.elasticpath.service.ReturnAndExchangeServiceImpl.completeExchange()'.
	 */
	@Test
	public void testCompleteUnspecifiedOrderExchangeThrowsException() {
		final OrderReturn orderReturn = new OrderReturnImpl();
		try {
			exchangeService.completeExchange(orderReturn, ReturnExchangeRefundTypeEnum.PHYSICAL_RETURN_REQUIRED);
			fail("EpServiceException must be thrown");
		} catch (final EpServiceException ex) {
			// Success
			assertNotNull(ex);
		}
	}

	@Test
	public void createExchangeShouldReturnExchangeWithCancelledStatusWhenRefundOrderPaymentThrowPaymentsException() {
		final String guid = "Guid";
		final BigDecimal amount = BigDecimal.TEN;
		final Currency currency = Currency.getInstance("CAD");

		final ShoppingCartMemento shoppingCartMemento = new ShoppingCartMementoImpl();
		shoppingCartMemento.setGuid(guid);
		final ShoppingCartImpl shoppingCart = new ShoppingCartImpl();
		shoppingCart.setShoppingCartMemento(shoppingCartMemento);
		final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot = new ShoppingCartImpl();
		final OrderShipment orderShipment = new PhysicalOrderShipmentImpl();
		final OrderSku orderSku = new OrderSkuImpl();
		orderSku.setShipment(orderShipment);
		final EventOriginator eventOriginator = new EventOriginatorImpl();
		final Order order = new OrderImpl();
		order.setCurrency(currency);
		order.setModifiedBy(eventOriginator);
		final OrderReturnSku orderReturnSku = new OrderReturnSkuImpl();
		orderReturnSku.setOrderSku(orderSku);
		final OrderReturnImpl orderReturn = new OrderReturnImpl();
		orderReturn.setOrder(order);
		orderReturn.setReturnTotal(amount);
		orderReturn.setOrderReturnSkus(new HashSet<>(Collections.singleton(orderReturnSku)));
		orderReturn.setExchangeShoppingCart(shoppingCart, shoppingCartTaxSnapshot);

		exchangeService.setOrderService(orderService);
		context.checking(new Expectations() {
			{
				allowing(shoppingCartService).saveIfNotPersisted(with(shoppingCart));
				allowing(cartOrderService).createOrderIfPossible(with(shoppingCart));
				allowing(cartOrderService).findByShoppingCartGuid(with(guid));
				allowing(shoppingCartService).saveOrUpdate(with(shoppingCart));
				allowing(mockCheckoutService).checkoutExchangeOrder(orderReturn, false);
				allowing(orderService).refundOrderPayment(with(order), with(emptyList()), with(Money.valueOf(amount, currency)),
						with(eventOriginator));
				will(throwException(new PaymentsException(PAYMENT_FAILED, emptyMap())));
				allowing(orderService).cancelOrder(with(shoppingCart.getCompletedOrder()));
			}
		});

		final OrderReturn exchange = exchangeService.createExchange(orderReturn, REFUND_TO_ORIGINAL, emptyList());

		assertThat(exchange.getReturnStatus()).isEqualTo(CANCELLED);
	}

	private Set<OrderSku> mockOrderSkuList() {
		final Set<OrderSku> orderSkus = new HashSet<>();
		// Create mock cart items
		final OrderSku orderSku = new OrderSkuImpl() {
			private static final long serialVersionUID = 3596418723515022523L;

			@Override
			public Currency getCurrency() {
				return CURRENCY;
			}
		};
		orderSku.setUidPk(Calendar.getInstance().getTimeInMillis());
		final ProductSku productSkuImpl = mockProductSku();
		orderSku.setSkuGuid(productSkuImpl.getGuid());
		orderSku.setPrice(1, null);
		orderSku.setUnitPrice(BigDecimal.ONE);
		//Tax amount needs to be set, normally set by TaxCalculationService
		orderSku.setTaxAmount(ITEM_TAX);
		orderSkus.add(orderSku);
		return orderSkus;
	}

	private List<ShoppingItem> mockCartItemList() {
		final List<ShoppingItem> orderSkus = new ArrayList<>();
		// Create mock cart items
		final ShoppingItem cartItem = new ShoppingItemImpl();
		cartItem.setUidPk(Calendar.getInstance().getTimeInMillis());
		final ProductSku mockProductSku = mockProductSku();
		cartItem.setSkuGuid(mockProductSku.getGuid());
		cartItem.setGuid(new RandomGuidImpl().toString());
		context.checking(new Expectations() {
			{
				allowing(mockProductSkuLookup).findByUid(mockProductSku.getUidPk());
				will(returnValue(mockProductSku));
			}
		});

		final Price price = new PriceImpl();
		price.setListPrice(Money.valueOf(PRODUCT_PRICE, CURRENCY));

		// any product sku price for the current cart item can be set up here.
		cartItem.setPrice(1, getPrice(CURRENCY, PRODUCT_PRICE, PRODUCT_PRICE));

		orderSkus.add(cartItem);
		return orderSkus;
	}

	private OrderAddress mockOrderAddress() {
		final OrderAddress address = new OrderAddressImpl();

		address.setFirstName("Joe");
		address.setLastName("Doe");
		address.setCountry(REGION_CODE_CA);
		address.setStreet1("1295 Charleston Road");
		address.setCity("Mountain View");
		address.setSubCountry(REGION_CODE_BC);
		address.setZipOrPostalCode("94043");
		return address;
	}

	private Address mockShippingAddress() {
		final Address address = new CustomerAddressImpl();

		address.setFirstName("Joe");
		address.setLastName("Doe");
		address.setCountry(REGION_CODE_CA);
		address.setStreet1("1295 Charleston Road");
		address.setCity("Mountain View");
		address.setSubCountry(REGION_CODE_BC);
		address.setZipOrPostalCode("94043");
		return address;
	}

	private ProductSku mockProductSku() {
		productSku = new ProductSkuImpl();
		productSku.setUidPk(Calendar.getInstance().getTimeInMillis());
		productSku.setGuid("Irrelevant sku code");

		productSku.setWeight(BigDecimal.ONE);

		final Product productImpl = new ProductImpl() {
			private static final long serialVersionUID = 5000000001L;

			@Override
			public String getDisplayName(final Locale locale) {
				return "Test Display Name";
			}
		};
		productImpl.setGuid("Irrelevant product guid");
		productImpl.setProductSkus(new HashMap<>());
		final ProductTypeImpl productTypeImpl = new ProductTypeImpl();
		final TaxCode taxCode = new TaxCodeImpl();
		taxCode.setCode(SALES_TAX_CODE_GOODS);
		productTypeImpl.setTaxCode(taxCode);
		productImpl.setProductType(productTypeImpl);
		productImpl.setTaxCodeOverride(taxCode);
		productSku.setProduct(productImpl);
		productImpl.addOrUpdateSku(productSku);

		context.checking(new Expectations() {
			{
				allowing(mockProductSkuLookup).findByGuid(productSku.getGuid());
				will(returnValue(productSku));
			}
		});

		final LocaleDependantFields ldf = new ProductLocaleDependantFieldsImpl();
		ldf.setDisplayName("TestDisplayName");
		ldf.setDescription("TestDescription");
		ldf.setLocale(DEFAULT_LOCALE);
		productImpl.addOrUpdateLocaleDependantFields(ldf);

		final InventoryDtoImpl inventoryDto = new InventoryDtoImpl();
		inventoryDto.setWarehouseUid(getMockedStore().getWarehouse().getUidPk());
		inventoryDto.setQuantityOnHand(1);
		inventoryDto.setSkuCode(productSku.getSkuCode());

		return productSku;
	}

	private OrderShipment mockOrderShipment() {
		final PhysicalOrderShipmentImpl orderShipment = getMockPhysicalOrderShipment();
		orderShipment.setUidPk(BigDecimal.ONE.longValue());

		orderShipment.setStatus(OrderShipmentStatus.SHIPPED);
		orderShipment.setItemTax(BigDecimal.ONE);
		orderShipment.setSubtotalDiscount(BigDecimal.ZERO);
		orderShipment.setBeforeTaxShippingCost(BigDecimal.ZERO);
		orderShipment.setShippingCost(BigDecimal.ZERO);
		orderShipment.setShippingTax(BigDecimal.ZERO);

		orderShipment.setShipmentAddress(mockOrderAddress());

		final Set<OrderSku> orderSkuSet = mockOrderSkuList();
		for (final OrderSku sku : orderSkuSet) {
			orderShipment.addShipmentOrderSku(sku);
		}

		return orderShipment;
	}

	private PhysicalOrderShipmentImpl getMockPhysicalOrderShipment() {

		final TaxCalculationResult result = new TaxCalculationResultImpl() {

			private static final long serialVersionUID = 3596418723515022523L;

			@Override
			public Money getBeforeTaxShippingCost() {
				return Money.valueOf(BigDecimal.ONE, Currency.getInstance(Locale.US));
			}

			@Override
			public Money getBeforeTaxSubTotal() {
				return Money.valueOf(BigDecimal.TEN, Currency.getInstance(Locale.US));
			}

			@Override
			public void applyTaxes(final Collection<? extends ShoppingItem> shoppingItems) { //NOPMD
			}

		};
		result.setDefaultCurrency(Currency.getInstance(Locale.US));

		return new PhysicalOrderShipmentImpl() {
			private static final long serialVersionUID = -8478099243134627014L;

			@Override
			public TaxCalculationResult calculateTaxes() {
				return result;
			}

			@Override
			public BigDecimal getItemSubtotal() {
				return BigDecimal.TEN;
			}
		};

	}

	/**
	 * Creates tax jurisdiction jurisdiction. That jurisdiction is:<br>
	 * Region code: CA<br>
	 * Country category: GST<br>
	 * Tax Region: CA<br>
	 * Tax Values: SHIPPING==6%, GOODS==6%<br>
	 * Subcountry category: PST<br>
	 * Tax Region: BC<br>
	 * Tax Values: SHIPPING==7%, GOODS==7%<br>
	 * Subcountry category: ANOTHER_CATEGORY<br>
	 * Tax Region: VANCOUVER<br>
	 * Tax Values: SHIPPING==7%, GOODS==7%<br>
	 * The last category musn't be taken into account while calculating taxes. This category doesn't match the shipping address.
	 */
	private TaxJurisdiction getTaxJurisdiction() {
		final TaxJurisdiction taxJurisdiction = new TaxJurisdictionImpl();
		taxJurisdiction.setPriceCalculationMethod(TaxJurisdiction.PRICE_CALCULATION_EXCLUSIVE);
		taxJurisdiction.setRegionCode(REGION_CODE_CA);

		// 1) category
		TaxCategory taxCategory = new TaxCategoryImpl();
		taxCategory.setFieldMatchType(TaxCategoryTypeEnum.FIELD_MATCH_COUNTRY);
		taxCategory.setName(GST_TAX_CODE);

		TaxRegion taxRegion = new TaxRegionImpl();

		Map<String, TaxValue> taxValueMap = new HashMap<>();

		TaxValue taxValue = new TaxValueImpl();
		taxValue.setTaxCode(createTaxCode(SALES_TAX_CODE_GOODS));
		taxValue.setTaxValue(new BigDecimal("6"));
		taxValueMap.put(taxValue.getTaxCode().getCode(), taxValue);

		taxValue = new TaxValueImpl();
		taxValue.setTaxCode(createTaxCode(TaxCode.TAX_CODE_SHIPPING));
		taxValue.setTaxValue(new BigDecimal("6"));
		taxValueMap.put(taxValue.getTaxCode().getCode(), taxValue);

		taxRegion.setTaxValuesMap(taxValueMap);
		taxRegion.setRegionName(REGION_CODE_CA);

		taxCategory.addTaxRegion(taxRegion);

		taxJurisdiction.addTaxCategory(taxCategory);

		// 2) category
		taxCategory = new TaxCategoryImpl();
		taxCategory.setFieldMatchType(TaxCategoryTypeEnum.FIELD_MATCH_SUBCOUNTRY);
		taxCategory.setName(PST_TAX_CODE);

		taxValueMap = new HashMap<>();

		taxRegion = new TaxRegionImpl();
		taxValue = new TaxValueImpl();
		taxValue.setTaxCode(createTaxCode(SALES_TAX_CODE_GOODS));
		taxValue.setTaxValue(new BigDecimal("7"));
		taxValueMap.put(taxValue.getTaxCode().getCode(), taxValue);

		taxValue = new TaxValueImpl();
		taxValue.setTaxCode(createTaxCode(TaxCode.TAX_CODE_SHIPPING));
		taxValue.setTaxValue(new BigDecimal("7"));
		taxValueMap.put(taxValue.getTaxCode().getCode(), taxValue);

		taxRegion.setTaxValuesMap(taxValueMap);

		taxRegion.setRegionName(REGION_CODE_BC);

		taxCategory.addTaxRegion(taxRegion);

		taxJurisdiction.addTaxCategory(taxCategory);

		return taxJurisdiction;
	}


	/**
	 * Dummy implementation of the TimeService.
	 */
	public static final class DummyTimeService extends DatabaseServerTimeServiceImpl {
		/**
		 * Gets the current time.
		 *
		 * @return current date
		 */
		@Override
		public Date getCurrentTime() {
			return new Date();
		}
	}

	/**
	 * Implementation of Order with auto-recalculation enabled by default for testing purposes.
	 */
	class TestOrderImpl extends OrderImpl {
		private static final long serialVersionUID = -4395039923898026316L;

		/**
		 * Override default constructor to enable auto-recalculation.
		 */
		TestOrderImpl() {
			super();
			enableRecalculation();
		}
	}

	/**
	 * Returns a new <code>Customer</code> instance.
	 *
	 * @return a new <code>Customer</code> instance.
	 */
	protected Customer getCustomer() {
		final Customer customer = new CustomerImpl();
		customer.setGuid(new RandomGuidImpl().toString());
		customer.initialize();
		return customer;
	}

	protected ShippingOption getShippingOption() {
		final ShippingOptionImpl option = new ShippingOptionImpl();
		option.setCode("fedex1");
		option.setCarrierCode("Fed Ex");
		return option;
	}

	/**
	 * Returns a new <code>Price</code> instance with the currency, listPrice, and salePrice
	 * set.
	 *
	 * @param currency  Currency
	 * @param listPrice the listPrice for this <code>Price</code>
	 * @param salePrice the salePrice for this <code>Price</code>
	 * @return a new <code>Price</code> instance.
	 */
	protected Price getPrice(final Currency currency, final BigDecimal listPrice, final BigDecimal salePrice) {
		Price price = getPrice();
		price.setCurrency(currency);

		Money listPriceMoney = Money.valueOf(listPrice, currency);
		price.setListPrice(listPriceMoney);

		Money salePriceMoney = Money.valueOf(salePrice, currency);
		price.setSalePrice(salePriceMoney);

		return price;
	}

	/**
	 * @return a new <code>Price</code> instance.
	 */
	private Price getPrice() {
		PriceImpl price = new PriceImpl();
		price.addOrUpdatePriceTier(getPriceTier());
		price.setCurrency(Currency.getInstance(CART_CURRENCY));
		price.initialize();
		return price;
	}

	/**
	 * @return a new <code>PriceTier</code> instance.
	 */
	private PriceTier getPriceTier() {
		PriceTier priceTier = new PriceTierImpl();
		priceTier.initialize();
		return priceTier;
	}

	private ShippingOptionResult createShippingOptionResult(final List<ShippingOption> availableShippingOptions) {
		final ShippingOptionResultImpl result = new ShippingOptionResultImpl();
		result.setAvailableShippingOptions(availableShippingOptions);
		return result;
	}
}

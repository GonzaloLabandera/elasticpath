/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.core.messaging.giftcertificate.GiftCertificateEventType;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.GiftCertificateImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.GiftCertificateService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.GiftCertificateFactory;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.store.StoreService;

/**
 * Test class for {@link CreateGiftCertificatesCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateGiftCertificatesCheckoutActionTest {

	private static final Currency CURRENCY = Currency.getInstance(Locale.CANADA);
	private static final String CUSTOMER_EMAIL = "sender@domain.com";
	private static final String ORDER_GUID = "100000";
	private static final String STORE_CODE = "MOBEE";
	private static final String ORDER_SHIPMENT_NUMBER = "12345-1";
	private static final ShipmentType SHIPMENT_TYPE = ShipmentType.ELECTRONIC;
	private static final BigDecimal SKU_AMOUNT = BigDecimal.valueOf(20.00);
	private static final String GIFT_CERTIFICATE_GUID_1 = "giftCertificateGuid1";
	private static final String GIFT_CERTIFICATE_GUID_2 = "giftCertificateGuid2";


	@Mock
	private GiftCertificateFactory giftCertificateFactory;

	@Mock
	private GiftCertificateService giftCertificateService;

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private PricingSnapshotService pricingSnapshotService;

	@Mock
	private PriceCalculator priceCalculator;

	@Mock
	private Customer customer;

	@Mock
	private Order order;

	@Mock
	private OrderShipment orderShipment;

	@Mock
	private Store store;

	@Mock
	private StoreService storeService;

	@Mock
	private OrderService orderService;

	@Mock
	private PostCaptureCheckoutActionContext checkoutContext;

	@Mock
	private EventMessage eventMessage1;

	@Mock
	private EventMessage eventMessage2;

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private CreateGiftCertificatesCheckoutAction checkoutAction;

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	@Before
	public void setUp() {
		elasticPath.setBeanFactory(beanFactory);

		when(order.getGuid()).thenReturn(ORDER_GUID);
		when(order.getCurrency()).thenReturn(CURRENCY);
		when(order.getStoreCode()).thenReturn(STORE_CODE);
		when(customer.getEmail()).thenReturn(CUSTOMER_EMAIL);
		when(checkoutContext.getOrder()).thenReturn(order);
		when(checkoutContext.getCustomer()).thenReturn(customer);
		when(storeService.findStoreWithCode(STORE_CODE)).thenReturn(store);
		when(orderService.update(order)).thenReturn(order);
		when(orderShipment.getShipmentNumber()).thenReturn(ORDER_SHIPMENT_NUMBER);
		when(orderShipment.getOrderShipmentType()).thenReturn(SHIPMENT_TYPE);
		when(priceCalculator.withCartDiscounts()).thenReturn(priceCalculator);
		when(priceCalculator.getAmount()).thenReturn(SKU_AMOUNT);
		when(priceCalculator.getMoney()).thenReturn(Money.valueOf(SKU_AMOUNT, CURRENCY));
		when(eventMessageFactory.createEventMessage(eq(GiftCertificateEventType.GIFT_CERTIFICATE_CREATED), eq(GIFT_CERTIFICATE_GUID_1), anyMap()))
				.thenReturn(eventMessage1);
		when(eventMessageFactory.createEventMessage(eq(GiftCertificateEventType.GIFT_CERTIFICATE_CREATED), eq(GIFT_CERTIFICATE_GUID_2), anyMap()))
				.thenReturn(eventMessage2);

		when(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class))
				.thenAnswer(invocation -> new ModifierFieldsMapWrapper());
	}

	@Test
	public void testExecuteAddsGiftCertificateCodeToShoppingItemData() {
		final OrderSku giftCertificateOrderSku1 = createOrderSkuWithGuid("sku1", GiftCertificate.KEY_PRODUCT_TYPE);
		final OrderSku giftCertificateOrderSku2 = createOrderSkuWithGuid("sku2", GiftCertificate.KEY_PRODUCT_TYPE);
		final OrderSku nonGiftCertificateOrderSku = createOrderSkuWithGuid("nonGiftCertificateSku", "NonGiftCertificate");

		givenOrderContainsOrderShipment(order, orderShipment);
		givenOrderShipmentContainsOrderSkus(orderShipment, giftCertificateOrderSku1, giftCertificateOrderSku2, nonGiftCertificateOrderSku);

		expectOrderSkuGeneratesGiftCertificate(giftCertificateOrderSku1, GIFT_CERTIFICATE_GUID_1, "GIFTCERT001", order.getCurrency());
		expectOrderSkuGeneratesGiftCertificate(giftCertificateOrderSku2, GIFT_CERTIFICATE_GUID_2, "GIFTCERT002", order.getCurrency());

		checkoutAction.execute(checkoutContext);

		assertEquals("Unexpected gift certificate GUID", GIFT_CERTIFICATE_GUID_1,
				giftCertificateOrderSku1.getModifierFields().get(GiftCertificate.KEY_GUID));
		assertEquals("Unexpected gift certificate GUID", GIFT_CERTIFICATE_GUID_2,
				giftCertificateOrderSku2.getModifierFields().get(GiftCertificate.KEY_GUID));
		assertNull("Non-Gift-Certificate OrderSkus should not contain a gift certificate GUID",
				nonGiftCertificateOrderSku.getModifierFields().get(GiftCertificate.KEY_GUID));
		verify(eventMessagePublisher).publish(eventMessage1);
		verify(eventMessagePublisher).publish(eventMessage2);
	}

	private OrderSku createOrderSkuWithGuid(final String orderSkuGuid, final String productTypeName) {
		final ProductType productType = new ProductTypeImpl();
		productType.setName(productTypeName);

		final Product product = new ProductImpl();
		product.setProductType(productType);

		final ProductSku productSku = new ProductSkuImpl();
		productSku.initialize();
		productSku.setProduct(product);

		final OrderSku orderSku = new OrderSkuImpl();
		orderSku.setGuid(orderSkuGuid);
		orderSku.setSkuGuid(productSku.getGuid());
		orderSku.setShipment(orderShipment);

		when(productSkuLookup.findByGuid(productSku.getGuid())).thenReturn(productSku);

		return orderSku;
	}

	private void givenOrderShipmentContainsOrderSkus(final OrderShipment shipment, final OrderSku... giftCertificateOrderSkus) {
		when(shipment.getShipmentOrderSkus()).thenReturn(new HashSet<>(Arrays.asList(giftCertificateOrderSkus)));
	}

	private void givenOrderContainsOrderShipment(final Order order, final OrderShipment... shipments) {
		when(order.getAllShipments()).thenReturn(Arrays.asList(shipments));
	}

	private void expectOrderSkuGeneratesGiftCertificate(final OrderSku giftCertificateOrderSku, final String giftCertificateGuid,
														final String giftCertificateCode, final Currency currency) {
		final GiftCertificate giftCertificate = new GiftCertificateImpl();
		giftCertificate.setGuid(giftCertificateGuid);
		giftCertificate.setGiftCertificateCode(giftCertificateCode);
		giftCertificate.setPurchaser(customer);

		final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot = mock(ShoppingItemPricingSnapshot.class, "Snapshot-" + UUID.randomUUID());
		when(pricingSnapshotService.getPricingSnapshotForOrderSku(giftCertificateOrderSku)).thenReturn(shoppingItemPricingSnapshot);
		when(shoppingItemPricingSnapshot.getPriceCalc()).thenReturn(priceCalculator);
		when(giftCertificateFactory.createGiftCertificate(giftCertificateOrderSku, shoppingItemPricingSnapshot, customer, store, currency))
				.thenReturn(giftCertificate);
		when(giftCertificateService.add(giftCertificate)).thenReturn(giftCertificate);
	}

}

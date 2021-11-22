/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.tax;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shipping.Region;
import com.elasticpath.domain.shipping.ShippingRegion;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.tax.domain.TaxDocumentId;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.TaxCodeService;
import com.elasticpath.tax.OrderTaxTestVerifier;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

public abstract class AbstractBasicTaxOperationTest extends BasicSpringContextTest {

	@Autowired
	protected CheckoutService checkoutService;
	
	protected Store store;

	protected Customer customer;

	protected CustomerAddress address;

	protected Shopper shopper;

	protected SimpleStoreScenario scenario;

	protected TestDataPersisterFactory persisterFactory;

	@Autowired
	protected OrderService orderService;

	@Autowired
	protected CartDirector cartDirector;

	List<Product> shippableProducts;
	
	List<Product> nonShippableProducts;

	@Autowired
	protected ProductSkuLookup productSkuLookup;

	@Autowired
	private StoreService storeService;
	
	@Autowired
	private OrderTaxTestVerifier orderTaxTestVerifier;

	@Autowired
	protected PricingSnapshotService pricingSnapshotService;

	@Autowired
	protected TaxSnapshotService taxSnapshotService;

	@Autowired
	protected OrderPaymentService orderPaymentService;

	protected CheckoutHelper checkoutHelper;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@Before
	public void setUp() {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
		persisterFactory = getTac().getPersistersFactory();
		store = scenario.getStore();
		
		setUpTaxJurisdictionForStore(store, scenario.getShippingRegion());
		
		shippableProducts = persisterFactory.getCatalogTestPersister().persistDefaultShippableProducts(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
		
		nonShippableProducts = persisterFactory.getCatalogTestPersister().persistDefaultNonShippableProducts(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());

		customer = persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
		address = persisterFactory.getStoreTestPersister().createCustomerAddress("Bond", "James", "1234 Pine Street", "", "Vancouver", "CA", "BC",
				"V6J5G4", "891312345007");
		shopper = persisterFactory.getStoreTestPersister().persistShopperWithAssociatedEntities(customer);

		checkoutHelper = new CheckoutHelper(getTac());
	}
	
	public void setUpTaxJurisdictionForStore(final Store store, final ShippingRegion shippingRegion) {
		// Tax jurisdictions
		TaxTestPersister taxTestPersister = getTac().getPersistersFactory().getTaxTestPersister();
		taxTestPersister.persistDefaultTaxJurisdictions();
		
		// Tax code
		final StoreTestPersister storePersister = getTac().getPersistersFactory().getStoreTestPersister();
		final TaxCodeService taxCodeService = getBeanFactory().getSingletonBean(ContextIdNames.TAX_CODE_SERVICE, TaxCodeService.class);
		final TaxCode goodTaxCode = taxCodeService.findByCode("GOODS");
		storePersister.updateStoreTaxCodes(store, new HashSet<>(Collections.singletonList(goodTaxCode)));
		
		Map<String, Region> regionMap = shippingRegion.getRegionMap();
		Region region = regionMap.values().iterator().next();
		
		Set<TaxJurisdiction> taxJurisdictionsSet = new HashSet<>();
		TaxJurisdiction jurisdiction = taxTestPersister.getTaxJurisdiction(region.getCountryCode());		
		taxJurisdictionsSet.add(jurisdiction);
		store.setTaxJurisdictions(taxJurisdictionsSet);
		storeService.saveOrUpdate(store);
	}
	
	public void verifyTaxDocumentForOrderShipment(final OrderShipment orderShipment, final Store store) {
		orderTaxTestVerifier.verifyTaxDocumentForOrderShipment(orderShipment, store);
	}
	
	public void verifyTaxDocumentForOrderReturn(final OrderReturn orderReturn, final Store store) {
		orderTaxTestVerifier.verifyTaxDocumentForOrderReturn(orderReturn, store);
	}
	
	/**
	 * Verify the cancelled order shipment has two sets of tax journals, one is purchase, another one is return.
	 */
	public void verifyTaxDocumentReversal(final TaxDocumentId taxDocumentId) {
		orderTaxTestVerifier.verifyTaxDocumentReversal(taxDocumentId);
	}
	
	public OrderSku getNewProductOrderSku(final SimpleStoreScenario scenario, final String orderSkuCode, final long orderId) {
		Product newProduct = getTac().getPersistersFactory().getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
																						scenario.getCatalog(),
																						scenario.getCategory(), 
																						scenario.getWarehouse());

		final OrderSku orderSku = getBeanFactory().getPrototypeBean(ContextIdNames.ORDER_SKU, OrderSku.class);

		final ProductSku productSku = newProduct.getDefaultSku();
		final Price price = getBeanFactory().getPrototypeBean(ContextIdNames.PRICE, Price.class);
		final Money amount = Money.valueOf(BigDecimal.ONE, Currency.getInstance("USD"));
		price.setListPrice(amount);
		orderSku.setPrice(1, price);
		orderSku.setUnitPrice(BigDecimal.ONE);
		final Date now = new Date();
		orderSku.setCreatedDate(now);
		final int qty3 = 3;
		orderSku.setQuantity(qty3);
		orderSku.setSkuCode(orderSkuCode);
		orderSku.setSkuGuid(productSku.getGuid());
		orderSku.setDigitalAsset(productSku.getDigitalAsset());
		orderSku.setTaxCode(newProduct.getTaxCodeOverride().getCode());
		orderSku.setTaxAmount(BigDecimal.ONE);
		orderSku.setAllocatedQuantity(qty3);

		if (productSku.getImage() != null) {
			orderSku.setImage(productSku.getImage());
		}

		orderSku.setDisplayName("product_name2");
		orderSku.setOrderUidPk(orderId);

		return orderSku;
	}
	
	/**
	 * getEventOriginatorHelper.
	 * @return EventOriginatorHelper
	 */
	public EventOriginatorHelper getEventOriginatorHelper() {
		return getBeanFactory().getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
	}
}

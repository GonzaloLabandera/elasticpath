/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.money.Money;
import com.elasticpath.sellingchannel.ShoppingItemFactory;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.sellingchannel.director.ShoppingItemAssembler;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartMerger;
import com.elasticpath.service.shoppingcart.impl.ShoppingCartMergerImpl;
import com.elasticpath.service.tax.TaxCodeService;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.CouponTestPersister;
import com.elasticpath.test.persister.PromotionTestPersister;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.ShoppingCartSimpleStoreScenario;

/**
 * Tests to ensure the <code>ShoppingCartMergerImpl</code> will correctly merge to <code>ShoppingCart</code>s.
 */
@SuppressWarnings("PMD.TooManyFields")
public class ShoppingCartMergerImplTest extends BasicSpringContextTest {

	private static final String NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART = "Number of items in the shopping cart";
	private static final String CURRENT_CART_NAME = "current";
	private static final String PREVIOUS_CART_NAME = "previous";
	private static final String BUNDLE_SKU_CODE = "bundleskucode";
	private static final String SHOPPING_START_TIME_TAG = "SHOPPING_START_TIME";

	private Product camera;
	private Product bag;
	private Product memoryCard;
	private Product multiSkuItem;
	private ProductBundle cameraBagCard;
	private Product cameraWarranty;

	private ProductSku cameraSku;
	private ProductSku bagSku;
	private ProductSku memoryCardSku;
	private ProductSku multiSkuItemSku;
	private ProductSku cameraWarrantySku;

	private ShoppingItem siCameraQty1;
	private ShoppingItem siBagQty1;
	private ShoppingItem siMemoryCardQty1;
	private ShoppingItem siCfgMultiSkuItem1;
	private ShoppingItem siCfgMultiSkuItem2;
	private ShoppingItem bundleCameraBagCard;
	private ShoppingItem cameraWarrantyQty1;
	private ShoppingItem siCameraWithWarranty;

	private ShoppingCartMergerImpl merger;
	private ShoppingCartSimpleStoreScenario scenario;
	private Shopper currentShopper;
	private Shopper previousShopper;
	private CatalogTestPersister catalogPersister;
	private CustomerSession currentCustomerSession;
	private CustomerSession previousCustomerSession;

	@Autowired
	private ShoppingItemFactory shoppingItemFactory;
	@Autowired
	private CartDirector cartDirector;
	@Autowired
	private ProductService productService;
	@Autowired
	private ProductSkuLookup productSkuLookup;
	@Autowired
	private ShopperService shopperService;
	@Autowired
	private CustomerService customerService;

	/**
	 * Setup the tests.
	 */
	@Before
	public void setUp() {

		// Create the merger we will actually be testing
		// - we mock out the price lookup facade to keep the test as simple as possible, as
		//   price isn't important in the merging case.
		merger = (ShoppingCartMergerImpl) getBeanFactory().getSingletonBean("shoppingCartMerger", ShoppingCartMerger.class);
		merger.setCartDirector(cartDirector);

		scenario = getTac().useScenario(ShoppingCartSimpleStoreScenario.class);

		currentCustomerSession = createCustomerSession();
		previousCustomerSession = createCustomerSession();

		currentShopper = createShopper(currentCustomerSession, CustomerType.REGISTERED_USER);
		previousShopper = createShopper(previousCustomerSession, CustomerType.SINGLE_SESSION_USER);

		final StoreTestPersister storePersister = getTac().getPersistersFactory().getStoreTestPersister();
		final TaxCodeService taxCodeService = getBeanFactory().getSingletonBean(ContextIdNames.TAX_CODE_SERVICE, TaxCodeService.class);
		final TaxCode goodTaxCode = taxCodeService.findByCode("GOODS");
		storePersister.updateStoreTaxCodes(scenario.getStore(), new HashSet<>(Arrays.asList(goodTaxCode)));

		catalogPersister = getTac().getPersistersFactory().getCatalogTestPersister();

		// Create the products to add to carts.
		camera = catalogPersister.createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		cameraSku = camera.getDefaultSku();

		bag = catalogPersister.createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		bagSku = bag.getDefaultSku();

		multiSkuItem = catalogPersister.createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse
				());
		multiSkuItem.getProductType().setMultiSku(true); // Make the ShoppingItems multi-sku
		multiSkuItem = productService.saveOrUpdate(multiSkuItem);
		multiSkuItemSku = multiSkuItem.getDefaultSku();

		memoryCard = catalogPersister.createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse
				());
		memoryCardSku = memoryCard.getDefaultSku();

		cameraWarranty = catalogPersister.createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
		cameraWarranty.setNotSoldSeparately(true);
		cameraWarranty = productService.saveOrUpdate(cameraWarranty);
		cameraWarrantySku = cameraWarranty.getDefaultSku();

		// Create shopping items that we'll be putting into the cart.
		Currency currency = TestDataPersisterFactory.DEFAULT_CURRENCY;
		PriceImpl price = new PriceImpl();
		price.setCurrency(currency);
		price.setListPrice(Money.valueOf(BigDecimal.ONE, currency));

		siCameraQty1 = shoppingItemFactory.createShoppingItem(cameraSku, price, 1, 1, null);
		siBagQty1 = shoppingItemFactory.createShoppingItem(bagSku, price, 1, 1, null);
		siMemoryCardQty1 = shoppingItemFactory.createShoppingItem(memoryCardSku, price, 1, 1, null);
		siCfgMultiSkuItem1 = shoppingItemFactory.createShoppingItem(multiSkuItemSku, price, 1, 1, null);
		siCfgMultiSkuItem2 = shoppingItemFactory.createShoppingItem(multiSkuItemSku, price, 1, 1, null);

		cameraWarrantyQty1 = shoppingItemFactory.createShoppingItem(cameraWarrantySku, price, 1, 1, null);
		siCameraWithWarranty = shoppingItemFactory.createShoppingItem(cameraSku, price, 1, 1, null);

		siCameraWithWarranty.addChildItem(cameraWarrantyQty1);
	}

	private CustomerSession createCustomerSession() {
		final CustomerSession customerSession = new CustomerSessionImpl();
		customerSession.setCurrency(TestDataPersisterFactory.DEFAULT_CURRENCY);
		customerSession.setLocale(Locale.CANADA);
		TagSet tagSet = new TagSet();
		tagSet.addTag(SHOPPING_START_TIME_TAG, new Tag(new Date().getTime()));
		customerSession.setCustomerTagSet(tagSet);
		return customerSession;
	}

	private Shopper createShopper(final CustomerSession customerSession, final CustomerType customerType) {
		Customer customer = createPersistedCustomer(customerType, scenario.getStore());
		Shopper shopper = shopperService.findOrCreateShopper(customer, scenario.getStore().getCode());
		shopper.setCustomerSession(customerSession);
		return shopper;
	}

	private Customer createPersistedCustomer(final CustomerType customerType, final Store store) {
		final Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId("shared-id-" + customer.getGuid());
		customer.setCustomerType(customerType);
		customer.setEmail("email");
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());

		return customerService.add(customer);
	}

	private ShoppingCart buildShoppingCart(final String cartName, final ShoppingItem... items) {
		final ShoppingCart shoppingCart = new ShoppingCartImpl();

		for (final ShoppingItem item : items) {
			shoppingCart.addShoppingCartItem(item);
		}
		shoppingCart.setStore(scenario.getStore());

		if (cartName.equals(CURRENT_CART_NAME)) {
			currentShopper.setCurrentShoppingCart(shoppingCart);
			shoppingCart.setShopper(currentShopper);
		} else {
			previousShopper.setCurrentShoppingCart(shoppingCart);
			shoppingCart.setShopper(previousShopper);
		}

		return shoppingCart;
	}

	/**
	 * The merge should only keep one copy of the same product.
	 * Should this bump the quantity?
	 */
	@DirtiesDatabase
	@Test
	public void testMergeSameProduct() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siCameraQty1);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(1);

		assertItemAndQuantity(mergedList, 0, cameraSku, 1);
	}

	/**
	 * The merge should keep both Products if they are different.
	 */
	@DirtiesDatabase
	@Test
	public void testMergeDifferentProduct() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siBagQty1);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(2);

		assertItemAndQuantity(mergedList, 0, cameraSku, 1);
		assertItemAndQuantity(mergedList, 1, bagSku, 1);
	}

	/**
	 * The merge should keep the current quantity when merging the same product.
	 */
	@DirtiesDatabase
	@Test
	public void testMergeSameProductKeepsCurrentCartQuantityWhenCurrentCartHasGreaterQuantity() {
		final ShoppingItem siCameraQty2 = shoppingItemFactory.createShoppingItem(cameraSku, null, 2, 1, null);

		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty2);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siCameraQty1);

		final ShoppingCart mergedCart = merger.merge(previousCart, currentCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(1);

		assertItemAndQuantity(mergedList, 0, cameraSku, 2);
	}

	/**
	 * The merge should keep the current quantity when merging the same product.
	 */
	@DirtiesDatabase
	@Test
	public void testMergeSameProductKeepsCurrentCartQuantityWhenCurrentCartHasLesserQuantity() {
		final ShoppingItem siCameraQty2 = shoppingItemFactory.createShoppingItem(cameraSku, null, 2, 1, null);

		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siCameraQty2);

		final ShoppingCart mergedCart = merger.merge(previousCart, currentCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(1);

		assertItemAndQuantity(mergedList, 0, cameraSku, 1);
	}

	/**
	 * The merge should keep both multi-sku products.
	 */
	@DirtiesDatabase
	@Test
	public void testMergeKeepsBothMultiSkuProducts() {
		final String nameField = "name";

		siCfgMultiSkuItem1.getModifierFields().put(nameField, "bob");
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCfgMultiSkuItem1);

		siCfgMultiSkuItem2.getModifierFields().put(nameField, "jill");
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siCfgMultiSkuItem2);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(2);

		assertItemAndQuantity(mergedList, 0, multiSkuItemSku, 1);
		assertThat(mergedList.get(0).getModifierFields().get(nameField)).isSameAs("bob");

		assertItemAndQuantity(mergedList, 1, multiSkuItemSku, 1);
		assertThat(mergedList.get(1).getModifierFields().get(nameField)).isSameAs("jill");
	}


	/**
	 * When merging a bundle, the bundle itself is treated as an independent item
	 * and the bundle contents will merge and not be affected by any
	 * other conflicting items or quantities that may already exist outside of the bundle.
	 */
	@DirtiesDatabase
	@Test
	public void testBundleMergesAsIndependantItemAndQuantity() {

		setUpBundles();

		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siMemoryCardQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, bundleCameraBagCard);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(2);

		assertItemAndQuantity(mergedList, 0, memoryCardSku, 1);

		assertThat(cameraBagCard.getDefaultSku().getGuid()).isSameAs(mergedList.get(1).getSkuGuid());
		assertThat(mergedList.get(1).isBundle(productSkuLookup)).isTrue();
	}

	private void setUpBundles() {
		cameraBagCard = catalogPersister.createSimpleProductBundle(
				"Bundle", "Bundle1", scenario.getCatalog(), scenario.getCategory(), scenario.getStore().getTaxCodes().iterator().next());

		cameraBagCard.addConstituent(catalogPersister.createSimpleBundleConstituent(camera, 1));
		cameraBagCard.addConstituent(catalogPersister.createSimpleBundleConstituent(bag, 1));
		cameraBagCard.addConstituent(catalogPersister.createSimpleBundleConstituent(memoryCard, 1));

		cameraBagCard.setCalculated(true);
		final ProductSku sku = new ProductSkuImpl();
		sku.setStartDate(new Date());
		sku.setSkuCode(BUNDLE_SKU_CODE);
		sku.initialize();
		cameraBagCard.setDefaultSku(sku);

		cameraBagCard = (ProductBundle) productService.saveOrUpdate(cameraBagCard);

		bundleCameraBagCard = shoppingItemFactory.createShoppingItem(cameraBagCard.getDefaultSku(), null, 1, 1, null);

		// Derive a ShoppingItemDTO, select all the constituents and convert back to a ShoppingItem.
		final ShoppingItemAssembler shoppingItemAssembler = getBeanFactory().getSingletonBean("shoppingItemAssembler", ShoppingItemAssembler.class);
		final ShoppingItemDto dto = shoppingItemAssembler.assembleShoppingItemDtoFrom(bundleCameraBagCard);
		dto.setSelected(true);
		for (final ShoppingItemDto child : dto.getConstituents()) {
			child.setSelected(true);
		}
		bundleCameraBagCard = shoppingItemAssembler.createShoppingItem(dto);
	}

	/**
	 * .
	 */
	@DirtiesDatabase
	@Test
	public void testOneProductInCurrentCartTwoInPreviousNoConflict() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siBagQty1, siMemoryCardQty1);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();

		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(3);

		assertItemAndQuantity(mergedList, 0, cameraSku, 1);
		assertItemAndQuantity(mergedList, 1, bagSku, 1);
		assertItemAndQuantity(mergedList, 2, memoryCardSku, 1);
	}

	/**
	 * .
	 */
	@DirtiesDatabase
	@Test
	public void testTwoProductInCurrentCartOneInPreviousNoConflict() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1, siBagQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siMemoryCardQty1);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(3);

		assertItemAndQuantity(mergedList, 0, cameraSku, 1);
		assertItemAndQuantity(mergedList, 1, bagSku, 1);
		assertItemAndQuantity(mergedList, 2, memoryCardSku, 1);
	}


	/**
	 * .
	 */
	@DirtiesDatabase
	@Test
	public void testMergePopulatedCartIntoEmptyCart() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siMemoryCardQty1);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();

		assertItemAndQuantity(mergedList, 0, memoryCardSku, 1);
	}

	/**
	 * .
	 */
	@DirtiesDatabase
	@Test
	public void testMergeEmptyCartIntoPopulatedCart() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siMemoryCardQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME);

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		final List<ShoppingItem> mergedList = mergedCart.getRootShoppingItems();
		assertThat(mergedList)
				.size()
				.as(NUMBER_OF_ITEMS_IN_THE_SHOPPING_CART)
				.isEqualTo(1);
		assertThat(memoryCardSku.getGuid()).isSameAs(mergedList.get(0).getSkuGuid());
	}

	/**
	 * Test merge preserves coupons.
	 */
	@DirtiesDatabase
	@Test
	public void testMergePreservesCoupons() {
		final ShoppingCart currentCart = buildShoppingCart(CURRENT_CART_NAME, siCameraQty1);
		final ShoppingCart previousCart = buildShoppingCart(PREVIOUS_CART_NAME, siBagQty1);

		PromotionTestPersister promotionTestPersister = getTac().getPersistersFactory().getPromotionTestPersister();
		Rule promotion = promotionTestPersister.createAndPersistSimpleShoppingCartPromotion("Test Promo", scenario.getStore().getCode(),
				"promo1", true);

		CouponTestPersister couponTestPersister = getTac().getPersistersFactory().getCouponTestPersister();
		CouponConfig couponConfig = couponTestPersister.createAndPersistCouponConfig(promotion.getCode(), 1, CouponUsageType.LIMIT_PER_COUPON);
		couponTestPersister.createAndPersistCoupon(couponConfig, "COUPON");

		previousCart.applyPromotionCode("COUPON");

		final ShoppingCart mergedCart = merger.merge(currentCart, previousCart);

		assertThat(mergedCart.getPromotionCodes())
				.size()
				.as("There should be a coupon code in the cart")
				.isEqualTo(1);

		assertThat(mergedCart.getPromotionCodes().iterator())
				.first()
				.as("The coupon code should be code applied to the previous cart")
				.isEqualTo("COUPON");
	}

	private void assertItemAndQuantity(final List<ShoppingItem> mergedList, final int position, final ProductSku sku, final int expectedQuantity) {
		assertThat(sku.getGuid())
				.as("Expected sku guids to match")
				.isSameAs(mergedList.get(position).getSkuGuid());
		assertThat(expectedQuantity)
				.as("Quantity not as expected")
				.isEqualTo(mergedList.get(position).getQuantity());
	}

}

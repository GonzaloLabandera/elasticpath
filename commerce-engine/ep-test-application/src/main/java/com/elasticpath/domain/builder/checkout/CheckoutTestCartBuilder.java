/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.domain.builder.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.ProductSkuService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Builder for shopping carts to facilitate testing of checkout.
 */
public class CheckoutTestCartBuilder {
	private Store store;

	private Customer customer;

	private CustomerAddress address;

	private CustomerSession customerSession;

	private SimpleStoreScenario scenario;

	private final List<ShoppingItemDto> shoppingItemDtos = new ArrayList<>();

	private Map<String, String> cartData = new HashMap<>();

	@Autowired
	private TestDataPersisterFactory persisterFactory;

	@Autowired
	private CartDirector cartDirector;

	@Autowired
	private StoreService storeService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductSkuService productSkuService;

	/**
	 * Select the scenario to use with the builder.
	 *
	 * @param scenario the scenario
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withScenario(final SimpleStoreScenario scenario) {
		this.scenario = scenario;
		store = scenario.getStore();

		address = persisterFactory.getStoreTestPersister().createCustomerAddress("Bond", "James", "1234 Pine Street", "", "Vancouver", "CA", "BC",
				"V6J5G4", "891312345007");

		return this;
	}

	/**
	 * Set the customer session.
	 * @param customerSession the session.
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withCustomerSession(final CustomerSession customerSession) {
		this.customerSession = customerSession;
		return this;
	}

	/**
	 * Set customer.
	 *
	 * @param customer the customer.
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withCustomer(final Customer customer) {
		this.customer = customer;
		return this;
	}

	/**
	 * Add a physical product to the cart.
	 *
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withPhysicalProduct() {
		final Product physicalProduct = persisterFactory.getCatalogTestPersister().persistDefaultShippableProducts(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse()).get(0);
		return withProductSku(physicalProduct.getDefaultSku());
	}

	/**
	 * Add an electronic product to the cart.
	 *
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withElectronicProduct() {
		final Product electronicProduct = persisterFactory.getCatalogTestPersister()
				.persistDefaultNonShippableProducts(scenario.getCatalog(),
													scenario.getCategory(),
													scenario.getWarehouse()).get(0);
		return withProductSku(electronicProduct.getDefaultSku());
	}

	/**
	 * Add a free electronic product to the cart.
	 *
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withFreeElectronicProduct() {
		final Product freeElectronicProduct = persisterFactory.getCatalogTestPersister()
				.persistNonShippablePersistedProductWithSku(scenario.getCatalog(),
															scenario.getCategory(),
															scenario.getWarehouse(),
															BigDecimal.ZERO,
															"Free Electronic Product",
															"Free Electronic Product");
		return withProductSku(freeElectronicProduct.getDefaultSku());
	}

	/**
	 * Add a Gift Certificate product to the cart.
	 *
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withGiftCertificateProduct() {
		return withGiftCertificateProduct(customer.getFullName(),
										  "Gift Certificate Recipient",
										  "giftcertificate.recipient." + Math.random() + "@elasticpath.com");
	}

	/**
	 * Add a Gift Certificate product to the cart.
	 *
	 * @param recipientEmailAddress the email address of the gift certificate recipient
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withGiftCertificateProduct(final String senderName, final String recipientName,
															  final String recipientEmailAddress) {
		final Product giftCertificateProduct = persisterFactory.getCatalogTestPersister()
				.persistProductWithSku(scenario.getCatalog(),
									   scenario.getCategory(),
									   scenario.getWarehouse(),
									   "Gift Certificates",
									   BigDecimal.TEN,
									   store.getDefaultCurrency(),
									   "Store",
									   "giftCertificate_" + System.currentTimeMillis() + "_" + Math.random(),
									   "Gift Certificate",
									   "hummingbird_" + System.currentTimeMillis() + "_" + Math.random(),
									   "NONE",
									   null,
									   false,
									   AvailabilityCriteria.ALWAYS_AVAILABLE,
									   0,
									   false,
									   null,
									   -1,
									   -1
				);

		final Map<String, String> itemFields = new HashMap<>();

		itemFields.put(GiftCertificate.KEY_MESSAGE, "Please enjoy this testing Gift Certificate!");
		itemFields.put(GiftCertificate.KEY_RECIPIENT_EMAIL, recipientEmailAddress);
		itemFields.put(GiftCertificate.KEY_RECIPIENT_NAME, recipientName);
		itemFields.put(GiftCertificate.KEY_SENDER_NAME, senderName);

		return withProductSku(giftCertificateProduct.getDefaultSku(), itemFields);
	}

	/**
	 * Adds a product to the cart.  The product will be persisted if necessary.
	 *
	 * @param product the product to add to the cart
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withProduct(final Product product) {
		final Product productToAddToCart;

		if (product.isPersisted()) {
			productToAddToCart = product;
		} else {
			productToAddToCart = productService.saveOrUpdate(product);
		}

		return withProductSku(productToAddToCart.getDefaultSku());
	}

	/**
	 * Adds a product SKU to the cart.  The SKU will be persisted if necessary.
	 *
	 * @param productSku the product sku to add to the cart
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withProductSku(final ProductSku productSku) {
		return withProductSku(productSku, null);
	}

	/**
	 * Adds a product SKU to the cart, along with associated Shopping Item Fields.  The SKU will be persisted if necessary.
	 *
	 * @param productSku the product sku to add to the cart
	 * @param itemFields the shopping item fields
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withProductSku(ProductSku productSku, final Map<String, String> itemFields) {
		final ProductSku productSkuToAddToCart;

		if (productSku.isPersisted()) {
			productSkuToAddToCart = productSku;
		} else {
			productSkuToAddToCart = productSkuService.saveOrUpdate(productSku);
		}

		final ShoppingItemDto shoppingItemDto = new ShoppingItemDto(productSkuToAddToCart.getSkuCode(), 1);

		if (itemFields != null) {
			shoppingItemDto.setItemFields(itemFields);
		}

		shoppingItemDtos.add(shoppingItemDto);

		return this;
	}

	/**
	 * Populates meta data of shopping cart.
	 * @param cartData the meta data of shopping cart.
	 * @return the checkout test cart builder
	 */
	public CheckoutTestCartBuilder withCartData(final Map<String, String> cartData) {

		this.cartData.putAll(cartData);
		return this;
	}

	/**
	 * Builds the shopping cart.
	 *
	 * @return the shopping cart
	 */
	public ShoppingCart build() {
		final ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(address, address, customerSession,
																											scenario.getShippingOption(), store);
		cartData.forEach(shoppingCart::setCartDataFieldValue);

		for (final ShoppingItemDto dto : shoppingItemDtos) {
			cartDirector.addItemToCart(shoppingCart, dto);
		}

		return shoppingCart;
	}

}

/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.sellingchannel.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.InvalidProductStructureException;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;

/**
 * Tests for ShoppingItemFactoryImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemFactoryImplTest {

	public static final String SKU_GUID = "sku-guid";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ShoppingItem shoppingItem;

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	/**
	 * Test that if a ProductSku has a null Product, an InvalidProductStructureException will be thrown.
	 */
	@Test
	public void testCreateShoppingItemWithNoProductCode() {
		ProductSku productSku = mock(ProductSku.class);
		Price price = mock(Price.class);
		when(productSku.getProduct()).thenReturn(null);

		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl();

		assertThatThrownBy(() -> factory.createShoppingItem(productSku, price, 1, 0, null))
				.isInstanceOf(InvalidProductStructureException.class);
	}

	/**
	 * Test that if ProductSku.Product has an empty ProductCode, an InvalidProductStructureException will be thrown.
	 */
	@Test()
	public void testCreateShoppingItemWithEmptyProductCode() {
		ProductSku productSku = mock(ProductSku.class, RETURNS_DEEP_STUBS);
		Price price = mock(Price.class);

		when(productSku.getProduct().getCode()).thenReturn(null);

		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl();

		assertThatThrownBy(() -> factory.createShoppingItem(productSku, price, 1, 0, null))
				.isInstanceOf(InvalidProductStructureException.class);
	}

	/**
	 * Test that GCs get a special delegate.
	 */
	@Test
	public void testCreateGiftCertificateShoppingItem() {
		final ProductSku productSku = mock(ProductSku.class);
		final Product product = mock(Product.class);
		final ProductType productType = createGiftCertificateProductType();
		Price price = new PriceImpl();
		price.addOrUpdatePriceTier(new PriceTierImpl());
		final BeanFactory beanFactory = mock(BeanFactory.class);

		when(productSku.getProduct()).thenReturn(product);
		when(product.getProductType()).thenReturn(productType);

		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl();
		factory.setBeanFactory(beanFactory);
		assertTrue(productSku.getProduct().getProductType().isGiftCertificate());
	}

	/**
	 * Test that Bundles get a special delegate that says its configurable.
	 */
	@Test
	public void testCreateBundleShoppingItem() {
		final ProductSku productSku = mock(ProductSku.class);
		final ProductBundle bundle = mock(ProductBundle.class);
		final ProductType productType = createGiftCertificateProductType();
		Price price = new PriceImpl();
		price.addOrUpdatePriceTier(new PriceTierImpl());
		final BeanFactory beanFactory = mock(BeanFactory.class);

		when(productSku.getProduct()).thenReturn(bundle);
		when(bundle.getProductType()).thenReturn(productType);

		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl();
		factory.setBeanFactory(beanFactory);
		assertTrue(productSku.getProduct().getProductType().isGiftCertificate());
	}

	/** */
	@Test
	public void testCreateShoppingItemWithOrderingSet() {
		final BeanFactory mockBeanFactory = mock(BeanFactory.class);
		elasticPath.setBeanFactory(mockBeanFactory);

		when(mockBeanFactory.getPrototypeBean(ContextIdNames.SHOPPING_ITEM, ShoppingItem.class))
				.thenAnswer(invocationOnMock -> ShoppingItemImpl.class.newInstance());
		when(mockBeanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class))
				.thenAnswer(invocationOnMock -> ModifierFieldsMapWrapper.class.newInstance());

		int anyOrdering = 2;
		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl() {
			@Override
			protected void sanityCheck(final ProductSku sku, final Price price) {  /*do nothing*/ }

			@Override
			protected int getMinQuantity(final ProductSku sku) {
				return 1;
			}
		};
		factory.setBeanFactory(mockBeanFactory);

		ShoppingItem item = factory.createShoppingItem(null, null, 1, anyOrdering, null);

		assertEquals(anyOrdering, item.getOrdering());
	}

	/**
	 * Tests that gift certificates create and set a gift certificate delegate and that passing fields to the method results in the fields being set
	 * on the cart item.
	 */
	@Test
	public void testCopyFields() {
		ShoppingItemFactoryImpl factory = new ShoppingItemFactoryImpl();

		final Product product = mock(Product.class);

		final BeanFactory beanFactory = mock(BeanFactory.class);
		factory.setBeanFactory(beanFactory);

		final Price price = mock(Price.class);

		final ProductSku productSku = mock(ProductSku.class);

		final Map<String, String> itemFields = new HashMap<>();
		itemFields.put("field1", "value1");
		itemFields.put("field2", "value2");

		final String skuGuid = new RandomGuidImpl().toString();

		when(productSku.getProduct()).thenReturn(product);
		when(productSku.getGuid()).thenReturn(skuGuid);
		when(product.getCode()).thenReturn("ProductA");
		when(beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_ITEM, ShoppingItem.class))
				.thenReturn(shoppingItem);

		ShoppingItem returnedShoppingItem = factory.createShoppingItem(productSku, price, 1, 0, itemFields);

		assertEquals("Returned shoppingCartItem should be the one we created", shoppingItem, returnedShoppingItem);

	}

	private ProductType createGiftCertificateProductType() {
		ProductType productType = new ProductTypeImpl();
		productType.setName(GiftCertificate.KEY_PRODUCT_TYPE);
		return productType;
	}

}

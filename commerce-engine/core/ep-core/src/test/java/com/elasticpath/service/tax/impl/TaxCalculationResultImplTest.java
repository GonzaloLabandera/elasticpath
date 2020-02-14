/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.service.tax.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.tax.TaxCategory;
import com.elasticpath.domain.tax.impl.TaxCategoryImpl;
import com.elasticpath.money.Money;
import com.elasticpath.service.tax.TaxCalculationResult;

/**
 * Tests that the TaxCalculationResultImpl methods work as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class TaxCalculationResultImplTest {

	private static final String OTHERSKU = "othersku";
	private static final String MYSKU = "mysku";

	private static final Currency CAD = Currency.getInstance(Locale.CANADA);

	@Mock
	private BeanFactory beanFactory;

	private TaxCalculationResultImpl taxCalculationResult;

	/**
	 * Sets up the test case for execution.
	 *
	 * @throws java.lang.Exception if error occurs
	 */
	@Before
	public void setUp() throws Exception {
		when(beanFactory.getPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class))
				.thenAnswer(invocation -> new LocalizedPropertiesImpl());

		taxCalculationResult = new TaxCalculationResultImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			protected <T> T getPrototypeBean(final String beanName, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(beanName, clazz);
			}
		};
		taxCalculationResult.setDefaultCurrency(CAD);
	}

	/**
	 * Tests that addTaxValue adds properly the amount to the total.
	 */
	@Test
	public void testAddTaxValue() {
		TaxCategory taxCategory = new TaxCategoryImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(name, clazz);
			}
		};
		Money amount = newMoney("10");

		taxCalculationResult.addTaxValue(taxCategory, amount);

		assertThat(taxCalculationResult.getTaxCategoriesIterator()).containsOnly(taxCategory);

		assertThat(taxCalculationResult.getTaxValue(taxCategory).getAmount()).isEqualTo(amount.getAmount());
		assertThat(taxCalculationResult.getTotalTaxes().getAmount()).isEqualTo(amount.getAmount());
	}

	private Money newMoney(final String value) {
		return Money.valueOf(new BigDecimal(value).setScale(2), CAD);
	}

	/**
	 * Test that addShippingTax() adds properly taxes to the total shipping tax amount.
	 */
	@Test
	public void testAddShippingTax() {
		Money shippingTax = newMoney("3");
		taxCalculationResult.addShippingTax(shippingTax);

		assertThat(taxCalculationResult.getShippingTax().getAmount()).isEqualTo(shippingTax.getAmount());

		// add a little bit more
		taxCalculationResult.addShippingTax(shippingTax);

		assertThat(taxCalculationResult.getShippingTax().getAmount()).isEqualTo(shippingTax.getAmount().multiply(new BigDecimal("2")));

	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#getTaxValue(com.elasticpath.domain.tax.TaxCategory)}.
	 */
	@Test
	public void testGetTaxValue() {
		assertThat(taxCalculationResult.getTaxValue(null)).isNotNull();
	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#addToTaxInItemPrice(com.elasticpath.money.Money)}.
	 */
	@Test
	public void testAddToTaxInItemPrice() {
		taxCalculationResult.addToTaxInItemPrice(newMoney("6"));
		taxCalculationResult.addToTaxInItemPrice(newMoney("3"));

		assertThat(taxCalculationResult.getTaxInItemPrice()).isEqualTo(newMoney("9"));
	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#setDefaultCurrency(java.util.Currency)}.
	 */
	@Test
	public void testSetDefaultCurrency() {
		taxCalculationResult = new TaxCalculationResultImpl();

		assertThatThrownBy(() -> taxCalculationResult.getTotalTaxes())
				.as("If no default currency has been set then no calculations should be possible.")
				.isInstanceOf(EpServiceException.class);
	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#addItemTax(String, com.elasticpath.money.Money)}.
	 */
	@Test
	public void testAddItemTax() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		taxCalculationResult.addItemTax(OTHERSKU, newMoney("5"));

		assertThat(taxCalculationResult.getTotalItemTax().getAmount()).isEqualTo("8.00");
	}

	/**
	 * Multiple calls to addItemTax with the same order sku should replace the old value for the sku with the new value for the sku.
	 */
	@Test
	public void testAddItemTaxMultipleCalls() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		taxCalculationResult.addItemTax(MYSKU, newMoney("5"));
		taxCalculationResult.addItemTax(OTHERSKU, newMoney("5"));

		assertThat(taxCalculationResult.getTotalItemTax().getAmount())
				.as("expected getTotalItemTax to be the sum of the latest calls to addItemTax for each sku.")
				.isEqualTo("10.00");
		assertThat(taxCalculationResult.getLineItemTax(MYSKU))
				.as("expected getLineItemTax to return last value set for sku.")
				.isEqualTo(newMoney("5"));
		assertThat(taxCalculationResult.getLineItemTax(OTHERSKU))
				.as("getLineItemTax should return value set by addItemTax")
				.isEqualTo(newMoney("5"));
	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#getLineItemTax(java.lang.String)}. Makes sure
	 * that we can look up the values we set for the skus.
	 */
	@Test
	public void testGetItemTax() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		taxCalculationResult.addItemTax(OTHERSKU, newMoney("5"));

		assertThat(taxCalculationResult.getLineItemTax(MYSKU))
				.as("getLineItemTax should return value set by addItemTax")
				.isEqualTo(newMoney("3"));
		assertThat(taxCalculationResult.getLineItemTax(OTHERSKU))
				.as("getLineItemTax should return value set by addItemTax")
				.isEqualTo(newMoney("5"));
	}

	/**
	 * Test method for {@link com.elasticpath.service.tax.impl.TaxCalculationResultImpl#getLineItemTax(java.lang.String)} where
	 * get is given an unknown sku.
	 */
	@Test
	public void testGetLineItemTaxNoSku() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		taxCalculationResult.addItemTax(OTHERSKU, newMoney("5"));

		assertThat(taxCalculationResult.getLineItemTax("someothersku"))
				.as("expected requesting tax for unknown sku to return null")
				.isNull();
	}

	/**
	 * Test method for applyTaxes.
	 */
	@Test
	public void testApplyTaxes() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		taxCalculationResult.addItemTax(OTHERSKU, newMoney("5"));
		Collection<ShoppingItem> lineItems = new ArrayList<>(2);

		ProductSku myProductSku = new ProductSkuImpl();
		myProductSku.setSkuCode(MYSKU);

		ProductSku otherProductSku = new ProductSkuImpl();
		otherProductSku.setSkuCode(OTHERSKU);

		OrderSkuImpl myOrderSku = new OrderSkuImpl();
		myOrderSku.setSkuGuid(myProductSku.getGuid());
		myOrderSku.setGuid(MYSKU);
		Price price = new PriceImpl();
		price.setListPrice(newMoney("10"));
		myOrderSku.setPrice(1, price);

		OrderSkuImpl otherOrderSku = new OrderSkuImpl();
		otherOrderSku.setSkuGuid(otherProductSku.getGuid());
		otherOrderSku.setGuid(OTHERSKU);
		otherOrderSku.setPrice(1, price);

		lineItems.add(myOrderSku);
		lineItems.add(otherOrderSku);
		taxCalculationResult.applyTaxes(lineItems);

		final BigDecimal myTaxAmount = myOrderSku.getTaxAmount();
		assertThat(myTaxAmount)
				.as("expected tax for ordersku to be set to value given to addItemTax")
				.isEqualTo("3.00");

		final BigDecimal otherTaxAmount = otherOrderSku.getTaxAmount();
		assertThat(otherTaxAmount)
				.as("expected tax for ordersku to be set to value given to addItemTax")
				.isEqualTo("5.00");
	}

	/**
	 * test applyTaxes with a line item that wasn't included in calculation. Should not throw exception.
	 */
	@Test
	public void testApplyTaxesSkuNoPriceCodeSuccess() {
		taxCalculationResult.addItemTax(MYSKU, newMoney("3"));
		Collection<ShoppingItem> lineItems = new ArrayList<>(2);

		ProductSku myProductSku = new ProductSkuImpl();
		myProductSku.setSkuCode(MYSKU);
		myProductSku.setGuid(MYSKU);

		ProductSku otherProductSku = new ProductSkuImpl();
		otherProductSku.setSkuCode(OTHERSKU);
		otherProductSku.setGuid(OTHERSKU);

		final ShoppingItem myOrderSku = mock(ShoppingItem.class, "My Order SKU");
		final ShoppingItem otherOrderSku = mock(ShoppingItem.class, "Other Order SKU");

		when(myOrderSku.hasPrice()).thenReturn(false);
		when(otherOrderSku.hasPrice()).thenReturn(false);

		lineItems.add(myOrderSku);
		lineItems.add(otherOrderSku);

		// in order exchange can be added item without price
		taxCalculationResult.applyTaxes(lineItems);
	}

	/**
	 * Test that addBeforeTaxItemPrice() properly adds the amount to the total value.
	 */
	@Test
	public void testAddBeforeTaxItemPrice() {
		taxCalculationResult.addBeforeTaxItemPrice(newMoney("5"));

		assertThat(taxCalculationResult.getBeforeTaxSubTotal().getAmount()).isEqualTo("5.00");
	}

	/**
	 * Test method for {@link TaxCalculationResultImpl#addBeforeTaxShippingCost(com.elasticpath.money.Money)}.
	 */
	@Test
	public void testAddBeforeTaxShippingCost() {
		taxCalculationResult.addBeforeTaxShippingCost(newMoney("5"));

		assertThat(taxCalculationResult.getBeforeTaxShippingCost().getAmount()).isEqualTo("5.00");
	}

	/**
	 * Tests that equals() works properly.
	 */
	@Test
	public void testEquals() {
		Currency cAD = Currency.getInstance(Locale.CANADA);

		TaxCalculationResultImpl obj1 = newTaxCalculationResult(cAD);

		TaxCalculationResultImpl obj2 = newTaxCalculationResult(cAD);

		assertThat(obj2).isEqualTo(obj1);

		assertThat(obj1)
				.isNotNull()
				.isEqualTo(obj2)
				.isEqualTo(obj1);

		// make objects not equal
		obj1.setBeforeTaxShippingCost(newMoney("34"));

		assertThat(obj1).isNotEqualTo(obj2);
	}

	/**
	 * @return
	 */
	private TaxCalculationResultImpl newTaxCalculationResult(final Currency defaultCurrency) {
		TaxCalculationResultImpl result = new TaxCalculationResultImpl();
		result.setDefaultCurrency(defaultCurrency);
		return result;
	}

	/**
	 * Tests that hashCode() is implemented properly for it to be
	 * used in a HashSet for example.
	 */
	@Test
	public void testHashCode() {
		Set<TaxCalculationResult> testSet = new HashSet<>();
		Currency cAD = Currency.getInstance(Locale.CANADA);

		// add to identical objects
		TaxCalculationResultImpl result1 = newTaxCalculationResult(cAD);
		TaxCalculationResultImpl result2 = newTaxCalculationResult(cAD);

		testSet.add(result1);
		testSet.add(result2);

		assertThat(testSet)
				.as("Only one of the objects should be in the Set")
				.hasSize(1);

		TaxCalculationResult taxCalcResult = result1;

		taxCalcResult.setDefaultCurrency(CAD);
		testSet.add(taxCalcResult);

		assertThat(testSet).hasSize(2);
	}

	/**
	 * Tests setTaxValue.
	 */
	@Test
	public void testSetTaxValue() {
		TaxCategory taxCategory = new TaxCategoryImpl() {
			private static final long serialVersionUID = 740L;

			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(name, clazz);
			}
		};
		taxCalculationResult.addTaxValue(taxCategory, newMoney("3"));
		assertThat(taxCalculationResult.getTaxValue(taxCategory)).isEqualTo(newMoney("3"));
	}

	/**
	 * Tests getTotalTaxes.
	 */
	@Test
	public void testGetTotalTaxes() {

		TaxCategory taxCategory = new TaxCategoryImpl();
		taxCategory.setName("tax_category1");
		taxCategory.setLocalizedProperties(new LocalizedPropertiesImpl());
		TaxCategory anotherTaxCategory = new TaxCategoryImpl();
		anotherTaxCategory.setName("tax_category2");
		anotherTaxCategory.setLocalizedProperties(new LocalizedPropertiesImpl());

		taxCalculationResult.addTaxValue(anotherTaxCategory, newMoney("15"));
		taxCalculationResult.addTaxValue(taxCategory, newMoney("5"));
		assertThat(taxCalculationResult.getTotalTaxes()).isEqualTo(newMoney("20"));

		taxCalculationResult.addTaxValue(anotherTaxCategory, newMoney("5"));
		taxCalculationResult.addTaxValue(taxCategory, newMoney("5"));
		assertThat(taxCalculationResult.getTotalTaxes()).isEqualTo(newMoney("30"));
	}

	/**
	 * Test tax calculation result representation as a string.
	 */
	@Test
	public void testToString() {
		assertThat(taxCalculationResult.toString()).isNotNull();

	}
}

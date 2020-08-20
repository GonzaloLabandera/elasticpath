package com.elasticpath.test.integration.shoppingcart.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.impl.ShopperImpl;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.service.shoppingcart.validation.AddOrUpdateShoppingItemDtoToCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ShoppingItemDtoValidationContext;
import com.elasticpath.service.shoppingcart.validation.impl.ShoppingItemDtoValidationContextImpl;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

public class AddOrUpdateShoppingItemDtoToCartValidationServiceImplTest extends BasicSpringContextTest {
	private static final String SKU_WITH_INVENTORY = "skuWithInventory";
	private static final String SKU_WITHOUT_INVENTORY = "skuWithoutInventory";
	private static final String SKU_NOT_SOLD_SEPARATELY = "skuNotSoldSeparately";

	@Autowired
	private AddOrUpdateShoppingItemDtoToCartValidationService addOrUpdateShoppingItemDtoToCartValidationService;

	@Autowired
	@Qualifier("catalogTestPersister")
	private CatalogTestPersister catalogTestPersister;

	private SimpleStoreScenario scenario;

	private Product productWithInventory;
	private Product productWithoutInventory;
	private Product productNotSoldSeparately;
	private ProductBundle productWithConstituentWithoutInventory;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 *
	 * @throws Exception on error.
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);

		productWithoutInventory = catalogTestPersister.persistProductWithSku(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse(),
				BigDecimal.TEN,
				Currency.getInstance("USD"), null, "productWithoutInventory", "Product without Inventory", SKU_WITHOUT_INVENTORY,
				TaxTestPersister.TAX_CODE_GOODS, AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK, 0);

		productWithInventory = catalogTestPersister.persistProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(),
				scenario.getWarehouse(), BigDecimal.TEN, "Product With Inventory", SKU_WITH_INVENTORY);

		productNotSoldSeparately = catalogTestPersister.persistProductWithSku(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse(),
				BigDecimal.TEN,
				Currency.getInstance("USD"), null, "productNotSoldSeparately", "Product without Inventory", SKU_NOT_SOLD_SEPARATELY,
				TaxTestPersister.TAX_CODE_GOODS, BigDecimal.ZERO, true, AvailabilityCriteria.ALWAYS_AVAILABLE, 0, true, true);

		productWithConstituentWithoutInventory = (ProductBundle) catalogTestPersister.persistProductBundle(scenario.getCatalog(),
				scenario.getCategory(), "productWithConstituentWithoutInventory", "productWithoutInventory");
	}

	@Test
	@DirtiesDatabase
	public void testValidShoppingItemDto() {
		ShoppingItemDtoValidationContext context = buildContext(productWithInventory);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(0).isEqualTo(structuredErrorMessages.size());
	}

	@Test
	@DirtiesDatabase
	public void testMissingInventory() {
		ShoppingItemDtoValidationContext context = buildContext(productWithoutInventory);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(1).isEqualTo(structuredErrorMessages.size());
		assertThat("item.insufficient.inventory").isEqualTo(structuredErrorMessages.iterator().next().getMessageId());
	}

	@Test
	@DirtiesDatabase
	public void testConstituentMissingInventory() {
		ShoppingItemDtoValidationContext context = buildContext(productWithoutInventory);
		addParentToContext(context);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(1).isEqualTo(structuredErrorMessages.size());
		assertThat("item.insufficient.inventory").isEqualTo(structuredErrorMessages.iterator().next().getMessageId());
	}

	@Test
	@DirtiesDatabase
	public void testBundleConstituentMissingInventory() {
		ShoppingItemDtoValidationContext context = buildContext(productWithConstituentWithoutInventory);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(1).isEqualTo(structuredErrorMessages.size());
		assertThat("item.insufficient.inventory").isEqualTo(structuredErrorMessages.iterator().next().getMessageId());
	}

	@Test
	@DirtiesDatabase
	public void testNotSoldSeparately() {
		ShoppingItemDtoValidationContext context = buildContext(productNotSoldSeparately);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(1).isEqualTo(structuredErrorMessages.size());
		assertThat("item.not.sold.separately").isEqualTo(structuredErrorMessages.iterator().next().getMessageId());
	}

	@Test
	@DirtiesDatabase
	public void testConstituentNotSoldSeparately() {
		ShoppingItemDtoValidationContext context = buildContext(productNotSoldSeparately);
		addParentToContext(context);

		Collection<StructuredErrorMessage> structuredErrorMessages = addOrUpdateShoppingItemDtoToCartValidationService.validate(context);
		assertThat(0).isEqualTo(structuredErrorMessages.size());
	}

	private ShoppingItemDtoValidationContext buildContext(final Product productWithInventory) {
		ShoppingItemDtoValidationContext context = new ShoppingItemDtoValidationContextImpl();
		ShoppingItemDto shoppingItemDto = new ShoppingItemDto(productWithInventory.getDefaultSku().getSkuCode(), 1);
		context.setShoppingItemDto(shoppingItemDto);
		context.setProductSku(productWithInventory.getDefaultSku());
		context.setStore(scenario.getStore());
		ShoppingCart shoppingCart = new ShoppingCartImpl();
		context.setShoppingCart(shoppingCart);
		Shopper shopper = new ShopperImpl();
		CustomerSession customerSession = new CustomerSessionImpl();
		TagSet tagSet = new TagSet();
		tagSet.addTag("SHOPPING_START_TIME", new Tag(System.currentTimeMillis()));
		customerSession.setCustomerTagSet(tagSet);
		customerSession.setCurrency(Currency.getInstance("USD"));
		shopper.updateTransientDataWith(customerSession);
		context.setShopper(shopper);
		Price price = new PriceImpl();
		context.setPromotedPrice(price);
		return context;
	}

	private void addParentToContext(ShoppingItemDtoValidationContext context) {
		ShoppingItemDto parentShoppingItemDto = new ShoppingItemDto(SKU_WITH_INVENTORY, 1);
		context.setParentShoppingItem(parentShoppingItemDto);
		context.setParentProductSku(productWithInventory.getDefaultSku());
	}
}

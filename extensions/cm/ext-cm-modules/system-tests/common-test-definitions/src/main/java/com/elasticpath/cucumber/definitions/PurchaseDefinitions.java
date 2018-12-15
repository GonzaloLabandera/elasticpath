package com.elasticpath.cucumber.definitions;

import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import com.elasticpath.cortex.dce.CommonMethods;
import com.elasticpath.cortex.dce.LoginSteps;
import com.elasticpath.cortexTestObjects.FindItemBy;
import com.elasticpath.cortexTestObjects.Item;
import com.elasticpath.cortexTestObjects.Order;
import com.elasticpath.selenium.domainobjects.Product;

/**
 * Purchase step definitions.
 */
public class PurchaseDefinitions {
	private final Product product;

	/**
	 * Constructor.
	 *
	 * @param product Product.
	 */
	public PurchaseDefinitions(final Product product) {
		this.product = product;
	}

	/**
	 * Given Step Details to look test more clear
	 *
	 * @param promoName  the Promotion Name
	 * @param couponCode the Coupon Code Name
	 * @param skuCode    the Sku Code
	 * @param skuPrice   the Sku Price
	 */
	@And("^there is a (.+) cart subtotal coupon (.+) and sku (.+) has purchase price of price (.+)$")
	public void givenDefinitionStep(final String promoName, final String couponCode, final String skuCode, final String skuPrice) {
		// Given Implementation Step
	}

	/**
	 * Given Step Details to make test look more clear
	 *
	 * @param promoName  the Promotion Name
	 * @param couponCode the Coupon Code Name
	 * @param products   the Map of products
	 */
	@And("^there is a (.+) coupon (.+) and the following products$")
	public void givenDefinitionStep(final String promoName, final String couponCode, final Map<String, String> products) {
		// Given Implementation Step
	}

	/**
	 * Creates purchase for newly created product for given scope.
	 *
	 * @param scope the store.
	 */
	@When("^I purchase the newly created product for scope (.+)$")
	public void purchaseNewProduct(final String scope) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope);
		FindItemBy.skuCode(this.product.getSkuCode());
		Item.addItemToCart(1);
		Order.addDefaultTokenAndBillingAddress();
		Order.submitPurchase();
	}

	/**
	 * Creates order with registered user using cortex.
	 *
	 * @param customerName Customer User Name
	 * @param scope        the scope
	 * @param dataTable    the data table
	 */
	@And("^I authenticate as a registered user (.+) for scope (.+) to create an order with following sku$")
	public void createRegUserOrder(final String customerName, final String scope, final DataTable dataTable) {
		LoginSteps.loginAsRegisteredShopperOnScope(customerName, scope);
		CommonMethods.addItemsToCart(dataTable);
		Order.submitPurchase();
	}

	/**
	 * Given Step Details to look test more clear.
	 *
	 * @param shippingMethod for Purchase
	 * @param taxGST         the GST Shipping Tax
	 * @param taxPST         the PST Shipping Tax
	 */
	@And("^Shipping Method as (.+) with GST (.+) and PST (.+) for Shipping Tax$")
	public void givenShippingTaxDefinitionStep(final String shippingMethod, final String taxGST, final String taxPST) {
		// Given Implementation Step
	}

	/**
	 * Given Step Details to look test more clear.
	 *
	 * @param shippingMethod for Shipping Method
	 * @param shippingCost   pre-defined shipping cost
	 */
	@And("^Shipping Method (.+) cost is (.+)$")
	public void givenShippingCostStep(final String shippingMethod, final String shippingCost) {
		// Given Implementation Step
	}

	/**
	 * Given Step Details to look test more clear.
	 *
	 * @param productName the Product Name
	 * @param productCost the Product Cost
	 */
	@And("^Product (.+) cost is (.+)$")
	public void givenProductCostStep(final String productName, final String productCost) {
		// Given Implementation Step
	}

	/**
	 * Given Step Details to look test more clear.
	 *
	 * @param promotionName promotion name
	 */
	@And("^Promotion (.+) applied to the purchase$")
	public void givenPromotionStep(final String promotionName) {
		// Given Implementation Step
	}
}

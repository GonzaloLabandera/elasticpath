package com.elasticpath.cucumber.definitions;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import com.elasticpath.cucumber.functions.params.AddCartItemParams;
import com.elasticpath.cucumber.macros.PurchaseMacro;
import com.elasticpath.selenium.domainobjects.Product;

/**
 * Purchase step definitions.
 */
public class PurchaseDefinitions {
	private final Product product;

	/**
	 * Constructor.
	 * @param product Product.
	 */
	public PurchaseDefinitions(final Product product) {
		this.product = product;
	}

	/**
	 * Creates order using cortex.
	 *
	 * @param scope      the scope
	 * @param paramsList the sku parameter list
	 */
	@And("^I create an order for scope (.+) with following sku$")
	public void createOrder(final String scope, final List<AddCartItemParams> paramsList) {
		PurchaseMacro purchaseMacro = new PurchaseMacro();
		purchaseMacro.createOrder(scope, paramsList);
	}

	/**
	 * Creates purchase for newly created product for given scope.
	 * @param scope the store.
	 */
	@When("^I purchase the newly created product for scope (.+)$")
	public void purchaseNewProduct(final String scope) {
		AddCartItemParams addCartItemParams = new AddCartItemParams();
		addCartItemParams.setQuantity(1);
		addCartItemParams.setSkuCode(this.product.getSkuCode());
		List<AddCartItemParams> itemList = new ArrayList<>();
		itemList.add(addCartItemParams);
		createOrder(scope, itemList);
	}

}

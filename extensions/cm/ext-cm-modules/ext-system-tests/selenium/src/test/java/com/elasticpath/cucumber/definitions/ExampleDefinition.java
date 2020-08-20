package com.elasticpath.cucumber.definitions;


import java.util.List;

import com.elasticpath.selenium.example.ExampleCatalogManagement;
import com.elasticpath.selenium.example.ExampleProductListingPane;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;

/**
 * Example step definitions.
 */
public class ExampleDefinition {

	private final ExampleCatalogManagement exampleCatalogManagement;
	private final ExampleProductListingPane exampleProductListingPane;
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public ExampleDefinition() {
		driver = SeleniumDriverSetup.getDriver();
		exampleCatalogManagement = new ExampleCatalogManagement(driver);
		exampleProductListingPane = new ExampleProductListingPane(driver);
	}

	/**
	 * Double click category.
	 *
	 * @param categoryName the category name.
	 */
	@When("^Example Definition - I open category (.+) to view products list$")
	public void doubleClickCategory(final String categoryName) {
		exampleCatalogManagement.doubleClickTheCategory(categoryName);
	}

	/**
	 * Verify product in product listing.
	 *
	 * @param productNameList the product name list.
	 */
	@Then("^Example Definition - Product Listing should contain following products$")
	public void verifyProductInProductListing(final List<String> productNameList) {
		for (String productName : productNameList) {
			exampleProductListingPane.verifyProductNameExists(productName);
		}
		exampleProductListingPane.closeProductListingPane();

	}

}

package com.elasticpath.cucumber.definitions.dst;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.cucumber.definitions.PriceListDefinition;
import com.elasticpath.cucumber.definitions.ProductAndBundleDefinition;
import com.elasticpath.selenium.domainobjects.DST;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.sync.SyncConfig;

/**
 * DST steps.
 */
public class DSTDefinition {

	private final DST dst;
	private final PriceListDefinition priceListDefinition;
	private final ActivityToolbar activityToolbar;
	private final ProductAndBundleDefinition productAndBundleDefinition;


	/**
	 * Constructor.
	 *
	 * @param dst the DST class
	 */
	public DSTDefinition(final DST dst, final PriceListDefinition priceListDefinition, final ProductAndBundleDefinition productAndBundleDefinition) {
		this.dst = dst;
		this.priceListDefinition = priceListDefinition;
		activityToolbar = new ActivityToolbar(SeleniumDriverSetup.getDriver());
		this.productAndBundleDefinition = productAndBundleDefinition;
	}

	/**
	 * Runs sync command.
	 */
	@And("^I run the data sync tool$")
	public void runSyncTool() {
		SyncConfig syncConfig = new SyncConfig();
		syncConfig.updateSyncConfigFiles();
		syncConfig.runDataSync(dst.getChangeSetGuid());
	}

	/**
	 * Click the publish environment price list manager.
	 */
	@When("^I go to the publish environment Price List Manager$")
	public void clickPublishPriceListManager() {
		activityToolbar.clickPriceListManagementButton();
	}

	/**
	 * Click the publish environment catalog management.
	 */
	@When("^I go to the publish environment Catalog Management$")
	public void clickPublishCatalogManagement() {
		activityToolbar.clickCatalogManagementButton();
	}

	/**
	 * Verify price list in publish environment.
	 */
	@Then("^I should see the new price list in the publish environment")
	public void verifyNewlyCreatedPriceList() {
		priceListDefinition.clickSearchForPriceLists();
		priceListDefinition.verifyPriceLists(dst.getPriceListName());
	}

	/**
	 * Verify price list is deleted.
	 */
	@Then("^the deleted price list no longer exists in publish environment$")
	public void verifyPriceListIsDeleted() {
		priceListDefinition.verifyPriceListDeleted(dst.getPriceListName());
	}

	/**
	 * Verifies newly created product exists in publish environment.
	 */
	@Then("^I should see the new (?:product|bundle) in publish environment$")
	public void verifyNewlyCreatedProductExists() {
		productAndBundleDefinition.verifyProductByName(dst.getProductName());
	}

	/**
	 * Verify product is deleted.
	 */
	@Then("^the deleted (?:product|bundle) no longer exists in publish environment$")
	public void verifyProductIsDeleted() {
		activityToolbar.closePane("Product Search Results");
		productAndBundleDefinition.verifyProductIsDeleted(dst.getProductName());
	}

	/**
	 * Search and open an existing Product to go specific Tab.
	 *
	 * @param productCode the product code
	 * @param tabName     for product
	 */
	@And("^in publish environment I am viewing the (.+) tab of product with code (.+)$")
	public void searchOpenProductEditorTabWithCode(final String tabName, final String productCode) {
		productAndBundleDefinition.searchOpenProductEditorTabWithCode(tabName, productCode);
	}

}

package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateShippingServiceLevelDialog;
import com.elasticpath.selenium.dialogs.EditShippingServiceLevelDialog;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.PromotionsShipping;
import com.elasticpath.selenium.resultspane.ShippingServiceLevelSearchResultPane;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;

/**
 * Promotions Shipping step definitions.
 */
public class ShippingServiceLevelDefinition {

	private static final String DASH = " - ";
	private static final int SLEEP_TIME = 500;
	private final PromotionsShipping promotionsShipping;
	private ShippingServiceLevelSearchResultPane shippingServiceLevelSearchResultPane;
	private CreateShippingServiceLevelDialog createShippingServiceLevelDialog;
	private EditShippingServiceLevelDialog editShippingServiceLevelDialog;
	private String shippingServiceLevelCode = "";

	/**
	 * Constructor.
	 */
	public ShippingServiceLevelDefinition() {
		promotionsShipping = new PromotionsShipping(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Click shipping service level tab.
	 */
	@When("^I click Shipping Service Levels tab$")
	public void clickShippingServiceLevelTab() {
		promotionsShipping.clickShippingServiceLevelTab();
	}

	/**
	 * Click shipping service level search button.
	 */
	@When("^I click Search button in Shipping Service Levels tab$")
	public void clickShippingServiceLevelSearchButton() {
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = promotionsShipping.clickShippingServiceSearchButton();
	}

	/**
	 * Verify shipping service level search result.
	 *
	 * @param serviceLevelCodeList the code list.
	 */
	@Then("^Shipping Service Level Search Results should contain following service level codes?$")
	public void verifyShippingServiceLevelSearchResult(final List<String> serviceLevelCodeList) {
		for (String serviceLevelCode : serviceLevelCodeList) {
			shippingServiceLevelSearchResultPane.verifyShippingServiceLevelExists(serviceLevelCode);
		}
	}

	/**
	 * Verify shipping service level name sarch results.
	 *
	 * @param serviceLevelNameList the name list.
	 */
	@Then("^Shipping Service Level Search Results should contain following service level names?$")
	public void verifyShippingServiceLevelNameSearchResult(final List<String> serviceLevelNameList) {
		for (String serviceLevelName : serviceLevelNameList) {
			shippingServiceLevelSearchResultPane.verifyShippingServiceLevelExistsByName(serviceLevelName);
		}
	}

	/**
	 * Create shipping service level.
	 *
	 * @param shippingServiceLevelMap the shipping service levels.
	 */
	@When("^I create shipping service level with following values$")
	public void createShippingServiceLevel(final Map<String, String> shippingServiceLevelMap) {
		this.shippingServiceLevelCode = "SSL-" + Utility.getRandomUUID();

		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(SeleniumDriverSetup.getDriver());
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.selectStore(shippingServiceLevelMap.get("store"));
		createShippingServiceLevelDialog.selectShippingRegion(shippingServiceLevelMap.get("shipping region"));
		createShippingServiceLevelDialog.selectCarrier(shippingServiceLevelMap.get("carrier"));
		createShippingServiceLevelDialog.enterUniqueCode(this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterName(shippingServiceLevelMap.get("name") + DASH + this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterPropertyValue(shippingServiceLevelMap.get("property value"));
		createShippingServiceLevelDialog.clickSaveButton();
	}

	/**
	 * Create shipping service level.
	 */
	@When("^I have a shipping service level$")
	public void createShippingServiceLevel() {
		this.shippingServiceLevelCode = "SSL-" + Utility.getRandomUUID();
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(SeleniumDriverSetup.getDriver());
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.selectStore("SearchStore");
		createShippingServiceLevelDialog.selectShippingRegion("USA");
		createShippingServiceLevelDialog.selectCarrier("Fed Ex");
		createShippingServiceLevelDialog.enterUniqueCode(this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterName("name" + DASH + this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterPropertyValue("10");
		createShippingServiceLevelDialog.clickSaveButton();
	}

	/**
	 * Create shipping service level.
	 */
	@When("^I attempt to create a new shipping service level with the same code$")
	public void attemptToCreateShippingServiceLevelWithSameCode() {
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(SeleniumDriverSetup.getDriver());
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.enterUniqueCode(this.shippingServiceLevelCode);
	}

	/**
	 * Create shipping service level.
	 */
	@When("^I can create a new shipping service level with the same code$")
	public void createShippingServiceLevelWithSameCode() {
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(SeleniumDriverSetup.getDriver());
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.selectStore("SearchStore");
		createShippingServiceLevelDialog.selectShippingRegion("USA");
		createShippingServiceLevelDialog.selectCarrier("Fed Ex");
		createShippingServiceLevelDialog.enterUniqueCode(this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterName("name" + DASH + this.shippingServiceLevelCode);
		createShippingServiceLevelDialog.enterPropertyValue("10");
		createShippingServiceLevelDialog.clickSaveButton();
	}


	/**
	 * Verify error message.
	 *
	 * @param errMsgList the list of messages.
	 */
	@Then("^I should see following create shipping service level validation alert?$")
	public void verifyErrorAlert(final List<String> errMsgList) {
		for (String errMsg : errMsgList) {
			if (errMsg.length() > 0) {
				createShippingServiceLevelDialog.verifyValidationErrorIsPresent(errMsg);
			}
		}
		createShippingServiceLevelDialog.clickCancel();
	}

	/**
	 * Edit shipping service level.
	 *
	 * @param shippingServiceLevelName the service level name to edit.
	 */
	@When("^I edit the shipping service level name to (.+)$")
	public void editShippingServiceLevel(final String shippingServiceLevelName) {
		editShippingServiceLevelDialog.enterName(shippingServiceLevelName);
		editShippingServiceLevelDialog.clickSaveButton();
	}

	/**
	 * Verify new shipping service level.
	 */
	@And("^I verify newly created shipping service level exists$")
	public void verifyNewShippingServiceLevel() {
		isShippingServiceLevelInList(this.shippingServiceLevelCode);
	}

	/**
	 * Delete new shipping service level.
	 */
	@When("^I delete the newly created shipping service level$")
	public void deleteNewShippingServiceLevel() {
		isShippingServiceLevelInList(this.shippingServiceLevelCode);
		shippingServiceLevelSearchResultPane.clickDeleteServiceLevelButton();
		new ConfirmDialog(SeleniumDriverSetup.getDriver()).clickOKButton("ShippingLevelsMessages.ConfirmDeleteShippingLevel");
	}

	/**
	 * Verify shipping service level is deleted.
	 */
	@And("^I verify shipping service level is deleted$")
	public void verifyShippingServiceLevelIsDeleted() {
		shippingServiceLevelSearchResultPane = promotionsShipping.clickShippingServiceSearchButton();
		shippingServiceLevelSearchResultPane.verifyShippingServiceLevelIsDeleted(this.shippingServiceLevelCode);
	}

	/**
	 * Open shipping service level editor.
	 *
	 * @param serviceLevelName the shipping service level name.
	 */
	@Then("^I open the shipping service level editor for (.+)$")
	public void openShippingServiceLevelEditor(final String serviceLevelName) {
		shippingServiceLevelSearchResultPane.selectServiceLevelByCode(serviceLevelName);
		editShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickOpenServiceLevelResultsTab();
	}

	/**
	 * Open newly created shipping service level editor.
	 */
	@Then("^I open the newly created shipping service level$")
	public void openNewlyCreatedShippingServiceLevelEditor() {
		shippingServiceLevelSearchResultPane.selectServiceLevelByCode(this.shippingServiceLevelCode);
		editShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickOpenServiceLevelResultsTab();
	}

	/**
	 * Is shipping service level in list.
	 *
	 * @param shippingServiceLevelCode the shipping service level code.
	 */
	public void isShippingServiceLevelInList(final String shippingServiceLevelCode) {
		shippingServiceLevelSearchResultPane = promotionsShipping.clickShippingServiceSearchButton();
		boolean isServiceLevelInList = shippingServiceLevelSearchResultPane.isShippingServiceLevelInList(shippingServiceLevelCode);

		int index = 0;
		while (!isServiceLevelInList && index < Constants.UUID_END_INDEX) {
			shippingServiceLevelSearchResultPane.sleep(SLEEP_TIME);
			promotionsShipping.clickShippingServiceSearchButton();
			isServiceLevelInList = shippingServiceLevelSearchResultPane.isShippingServiceLevelInList(shippingServiceLevelCode);
			index++;
		}

		assertThat(isServiceLevelInList)
				.as("Shipping Service Level does not exist in search result - " + shippingServiceLevelCode)
				.isTrue();
	}
}

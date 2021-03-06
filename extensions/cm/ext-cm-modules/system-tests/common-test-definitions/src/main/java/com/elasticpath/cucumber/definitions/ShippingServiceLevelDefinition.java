package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateShippingServiceLevelDialog;
import com.elasticpath.selenium.dialogs.EditShippingServiceLevelDialog;
import com.elasticpath.selenium.domainobjects.ShippingServiceLevel;
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
	private ShippingServiceLevel shippingServiceLevel;
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public ShippingServiceLevelDefinition() {
		driver = SeleniumDriverSetup.getDriver();
		promotionsShipping = new PromotionsShipping(driver);
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
	 * @param shippingServiceLevelList the shipping service levels.
	 */
	@When("^I create shipping service level with following values$")
	public void createShippingServiceLevel(final List<ShippingServiceLevel> shippingServiceLevelList) {
		shippingServiceLevel = shippingServiceLevelList.get(0);
		shippingServiceLevel.setShippingServiceLevelCode("SSL-" + Utility.getRandomUUID());
		shippingServiceLevel.setName(shippingServiceLevel.getName() + DASH + shippingServiceLevel.getShippingServiceLevelCode());
		createShippingServiceLevelHelper();
	}

	/**
	 * Shipping service level creation helper method.
	 */
	private void createShippingServiceLevelHelper() {
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(driver);
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.selectStore(shippingServiceLevel.getStore());
		createShippingServiceLevelDialog.selectShippingRegion(shippingServiceLevel.getShippingRegion());
		createShippingServiceLevelDialog.selectCarrier(shippingServiceLevel.getCarrier());
		createShippingServiceLevelDialog.enterUniqueCode(shippingServiceLevel.getShippingServiceLevelCode());
		createShippingServiceLevelDialog.enterName(shippingServiceLevel.getName());
		createShippingServiceLevelDialog.enterPropertyValue(shippingServiceLevel.getPropertyValue());
		createShippingServiceLevelDialog.clickSaveButton();
	}

	/**
	 * Create shipping service level.
	 */
	@When("^I attempt to create a new shipping service level with the same code$")
	public void attemptToCreateShippingServiceLevelWithSameCode() {
		promotionsShipping.clickShippingServiceLevelTab();
		shippingServiceLevelSearchResultPane = new ShippingServiceLevelSearchResultPane(driver);
		shippingServiceLevelSearchResultPane.clickCreateServiceLevelResultsTab();
		createShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickCreateServiceLevelButton();
		createShippingServiceLevelDialog.enterUniqueCode(shippingServiceLevel.getShippingServiceLevelCode());
	}

	/**
	 * Create shipping service level with the same code as created before in this scenerio.
	 */
	@When("^I can create a new shipping service level with the same code$")
	public void createShippingServiceLevelWithSameCode() {
		createShippingServiceLevelHelper();
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
	 *
	 * @param searchFilters a list of search filters parameters
	 */
	@And("^I verify newly created shipping service level exists$")
	public void verifyNewShippingServiceLevel(final List<String> searchFilters) {
		isShippingServiceLevelInList(shippingServiceLevel.getShippingServiceLevelCode(), searchFilters.get(1), searchFilters.get(0));
	}

	/**
	 * Delete new shipping service level.
	 *
	 * @param searchFilters a list of search filters parameters
	 */
	@When("^I delete the newly created shipping service level$")
	public void deleteNewShippingServiceLevel(final List<String> searchFilters) {
		isShippingServiceLevelInList(shippingServiceLevel.getShippingServiceLevelCode(), searchFilters.get(1), searchFilters.get(0));
		shippingServiceLevelSearchResultPane.clickDeleteServiceLevelButton();
		new ConfirmDialog(driver).clickOKButton("ShippingLevelsMessages.ConfirmDeleteShippingLevel");
	}

	/**
	 * Verify shipping service level is deleted.
	 *
	 * @param searchFilters a list of search filters parameters
	 */
	@And("^I verify shipping service level is deleted$")
	public void verifyShippingServiceLevelIsDeleted(final List<String> searchFilters) {
		searchShippingServiceLevel(searchFilters.get(1), searchFilters.get(0));
		shippingServiceLevelSearchResultPane.verifyShippingServiceLevelIsDeleted(shippingServiceLevel.getShippingServiceLevelCode());
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
		shippingServiceLevelSearchResultPane.selectServiceLevelByCode(shippingServiceLevel.getShippingServiceLevelCode());
		editShippingServiceLevelDialog = shippingServiceLevelSearchResultPane.clickOpenServiceLevelResultsTab();
	}

	/**
	 * Is shipping service level in list.
	 *
	 * @param shippingServiceLevelCode the shipping service level code.
	 * @param region which should be set in Shipping Region filter
	 * @param store  which should be set in Store filter
	 */
	private void isShippingServiceLevelInList(final String shippingServiceLevelCode, final String region, final String store) {
		searchShippingServiceLevel(region, store);
		boolean isServiceLevelInList = shippingServiceLevelSearchResultPane.isShippingServiceLevelInList(shippingServiceLevelCode);

		int index = 0;
		while (!isServiceLevelInList && index < Constants.UUID_END_INDEX) {
			shippingServiceLevelSearchResultPane.sleep(SLEEP_TIME);
			searchShippingServiceLevel(region, store);
			isServiceLevelInList = shippingServiceLevelSearchResultPane.isShippingServiceLevelInList(shippingServiceLevelCode);
			index++;
		}

		assertThat(isServiceLevelInList)
				.as("Shipping Service Level does not exist in search result - " + shippingServiceLevelCode)
				.isTrue();
	}

	/**
	 * Fills Shipping Service Levels search filters and performs search
	 *
	 * @param region which should be selected in Shipping Region filter
	 * @param store which should be selected in Store filter
	 */
	private void searchShippingServiceLevel(final String region, final String store){
		promotionsShipping.setShippingRegionFilter(region);
		promotionsShipping.setStoreFilter(store);
		shippingServiceLevelSearchResultPane = promotionsShipping.clickShippingServiceSearchButton();
	}
}

package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortex.dce.LoginSteps;
import com.elasticpath.cortexTestObjects.FindItemBy;
import com.elasticpath.cortexTestObjects.Item;
import com.elasticpath.selenium.dialogs.AddCustomerSegmentMembershipDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.editor.CustomerSegmentEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.CustomerSegmentResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.toolbars.CustomerServiceActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;

/**
 * Customer Segment step definitions.
 */
public class CustomerSegmentDefinition {
	private static final Logger LOGGER = LogManager.getLogger(CustomerSegmentDefinition.class);
	private CustomerSegmentResultPane customerSegmentResultPane;
	private final ConfigurationActionToolbar configurationActionToolbar;
	private CustomerSegmentEditor customerSegmentEditor;
	private final ActivityToolbar activityToolbar;
	private String uniqueCustomerSegmentName;
	private final CustomerEditor customerEditor;
	private AddCustomerSegmentMembershipDialog addCustomerSegmentMembershipDialog;
	private String customerSegmentName;
	private final CustomerServiceActionToolbar customerServiceActionToolbar;
	private String customerID = "";
	private final NavigationDefinition navigationDefinition;
	private final CustomerDefinition customerDefinition;
	private final static String TEXT_DISPLAY = "display";
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public CustomerSegmentDefinition() {
		driver = SeleniumDriverSetup.getDriver();
		configurationActionToolbar = new ConfigurationActionToolbar(driver);
		customerEditor = new CustomerEditor(driver);
		addCustomerSegmentMembershipDialog = new AddCustomerSegmentMembershipDialog(driver);
		activityToolbar = new ActivityToolbar(driver);
		customerServiceActionToolbar = new CustomerServiceActionToolbar(driver);
		navigationDefinition = new NavigationDefinition();
		customerDefinition = new CustomerDefinition();
	}

	/**
	 * Clicks on Customer Segments.
	 */
	@When("^I go to Customer Segments$")
	public void clickCustomerSegments() {
		activityToolbar.clickConfigurationButton();
		customerSegmentResultPane = configurationActionToolbar.clickCustomerSegments();
	}

	/**
	 * Create new Customer Segment.
	 *
	 * @param description the new Customer Segments description.
	 */
	@When("^I create a customer segment with description (.+)$")
	public void createCustomerSegment(final String description) {
		uniqueCustomerSegmentName = "CS" + Utility.getRandomUUID();
		customerSegmentEditor = customerSegmentResultPane.clickCreateCustomerSegmentButton();
		customerSegmentEditor.verifyCustomerSegmentEditor();
		customerSegmentEditor.enterCustomerSegmentName(this.uniqueCustomerSegmentName);
		if (description != null) {
			customerSegmentEditor.enterCustomerSegmentDescription(description);
		}
		configurationActionToolbar.saveAll();
		this.customerSegmentName = this.uniqueCustomerSegmentName + " - " + description;
		customerSegmentEditor.closeCustomerSegmentEditor(this.uniqueCustomerSegmentName);
	}

	/**
	 * Verify newly created Customer Segment.
	 */
	@Then("^I should see the newly created customer segment$")
	public void verifyNewCustomerSegmentExists() {
		customerSegmentResultPane.verifyCustomerSegmentsExists(this.uniqueCustomerSegmentName);
	}

	/**
	 * Verify newly created Customer Segment present in the segment list.
	 */
	@Then("^the newly created Customer Segment is available in the segment list")
	public void verifyCustomerSegmentInList() {
		addCustomerSegmentMembershipDialog = customerEditor.clickAddSegmentButton();
		addCustomerSegmentMembershipDialog.verifyCustomerSegmentExists(this.customerSegmentName);
		addCustomerSegmentMembershipDialog.clickSave();
		customerEditor.selectCustomerSegment(this.uniqueCustomerSegmentName);
		customerEditor.clickRemoveSegmentButton();
		new ConfirmDialog(driver).clickOKButton("FulfillmentMessages.CustomerSegmentsPageDialog_RemoveConfirm");
		customerEditor.closeCustomerEditor();
		new ConfirmDialog(driver).clickNoButton("CoreMessages.AbstractCmClientFormEditor_OkTitle_save");
	}

	/**
	 * Delete newly created Customer Segment.
	 */
	@When("^I delete newly created customer segment$")
	public void deleteNewCustomerSegment() {
		activityToolbar.clickConfigurationButton();
		customerSegmentResultPane.deleteCustomerSegment(this.uniqueCustomerSegmentName);
	}

	/**
	 * Verify new Customer Segment no longer exists.
	 */
	@Then("^newly created customer segment no longer exists$")
	public void verifyNewCustomerSegmentIsDeleted() {
		customerSegmentResultPane.verifyCustomerSegmentIsNotInList(this.uniqueCustomerSegmentName);
	}

	/**
	 * Editing the new Customer Segment Enabled checkbox.
	 */
	@When("^I enable newly created customer segment$")
	public void editCustomerSegment() {
		customerSegmentEditor = customerSegmentResultPane.openCustomerSegmentEditor(this.uniqueCustomerSegmentName);
		customerSegmentEditor.verifyCustomerSegmentEditor();
		customerSegmentEditor.clickCustomerSegmentEnabledCheckBox();
		configurationActionToolbar.saveAll();
		customerSegmentEditor.closeCustomerSegmentEditor(this.uniqueCustomerSegmentName);
	}

	/**
	 * Verifies item price that is depending on customer segment.
	 *
	 * @param customerID          Customer ID.
	 * @param customerSegmentName Customer segment.
	 * @param price               item price.
	 * @param sku                 item sku code.
	 * @param store               store.
	 */
	@Given("^a customer (.+) who is member of segment (.+) that has item price (.+) for sku (.+) in store (.+)$")
	public void verifyItemPricePerCustomerSegment(final String customerID, final String customerSegmentName, final String price, final String sku,
												  final String store) {
		verifyItemPriceFromCortex(customerID, price, sku, store);
		this.customerID = customerID;
	}

	/**
	 * Verifies item price for registered customer.
	 *
	 * @param sku        item sku code.
	 * @param price      item price.
	 * @param customerID Customer ID.
	 * @param store      store.
	 */
	@Then("^the item price for sku (.+) is (.+) when customer (.+) retrieve the item price in store (.+)$")
	public void verifyItemPrice(final String sku, final String price, final String customerID, final String store) {
		verifyItemPriceFromCortex(customerID, price, sku, store);
	}

	private void verifyItemPriceFromCortex(final String customerID, final String price, final String sku,
										   final String store) {
		LoginSteps.loginAsRegisteredShopperOnScope(customerID, store);
		FindItemBy.skuCode(sku);
		Item.price();

		assertThat(Item.getPurchasePrice(TEXT_DISPLAY))
				.as("Expected item price not match.")
				.isEqualTo(price);
	}

	/**
	 * Verifies change in item price for registered customer.
	 *
	 * @param sku        item sku code.
	 * @param price      item price.
	 * @param customerID Customer ID.
	 * @param store      store.
	 */
	@Then("^the new item price for sku (.+) is (.+) when customer (.+) retrieve the item price in store (.+)$")
	public void verifyItemPriceChange(final String sku, final String price, final String customerID, final String store) {
		verifyItemPriceChangeFromCortex(customerID, price, sku, store);
	}

	private void verifyItemPriceChangeFromCortex(final String customerID, final String price, final String sku,
												 final String store) {
		LoginSteps.loginAsRegisteredShopperOnScope(customerID, store);

		int counter = 0;
		do {
			FindItemBy.skuCode(sku);
			Item.price();
			activityToolbar.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			LOGGER.info(counter + ". actual item price: " + Item.getPurchasePrice(TEXT_DISPLAY) + " expected item price: " + price);
			counter++;
		} while (!Item.getPurchasePrice(TEXT_DISPLAY).equals(price) && counter < Constants.RETRY_COUNTER_40);

		assertThat(Item.getPurchasePrice(TEXT_DISPLAY))
				.as("Expected item price not match.")
				.isEqualTo(price);
	}

	/**
	 * Remove Customer Segment.
	 *
	 * @param segmentName Segment Name.
	 * @param customerID  Customer ID.
	 */
	@When("^I remove customer segment (.+) for the customer (.+)$")
	public void removeCustomerSegment(final String segmentName, final String customerID) {
		navigationDefinition.clickCustomerService();
		customerDefinition.openCustomerEditor(customerID);
		customerDefinition.selectCustomerEditorTab("Customer Segments");

		this.customerSegmentName = segmentName;
		this.customerID = customerID;
		customerEditor.selectCustomerSegment(segmentName);
		customerEditor.clickRemoveSegmentButton();
		new ConfirmDialog(driver).clickOKButton("FulfillmentMessages.CustomerSegmentsPageDialog_RemoveConfirm");
		customerServiceActionToolbar.clickSaveButton();
		configurationActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Add Customer Segment.
	 *
	 * @param segmentName Segment Name.
	 */
	@When("^I add segment (.+)$")
	public void addCustomerSegment(final String segmentName) {
		addCustomerSegmentMembershipDialog = customerEditor.clickAddSegmentButton();
		addCustomerSegmentMembershipDialog.selectCustomerSegment(segmentName);
		addCustomerSegmentMembershipDialog.clickSave();
		customerServiceActionToolbar.clickSaveButton();
		configurationActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Resets customer segment back to the default state in case of wrong state caused by test failures.
	 */
	@After(value = "@resetCustomerSegment", order = Constants.CLEANUP_ORDER_FIRST)
	public void resetCustomerSegment() {
		customerEditor.closeCustomerEditor();
		customerDefinition.openCustomerEditor(this.customerID);
		customerDefinition.selectCustomerEditorTab("Customer Segments");
		if (!customerEditor.isCustomerSegmentExists(this.customerSegmentName)) {
			//This is hardcoded because the add segment is expecting the segment name + segment description.
			addCustomerSegment("ITEST_SEGMENT - ITEST_SEGMENT");
		}
		customerEditor.selectCustomerSegment(this.customerSegmentName);
	}

}

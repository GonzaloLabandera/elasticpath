package com.elasticpath.cucumber.definitions;

import static com.elasticpath.selenium.setup.SetUp.getDriver;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.editor.InventoryEditor;
import com.elasticpath.selenium.navigations.ShippingReceiving;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ShippingReceivingActionToolbar;

/**
 * Shipping/Receiving steps.
 */
public class ShippingReceivingDefinition {
	private final ActivityToolbar activityToolbar;
	private final ShippingReceiving shippingReceiving;
	private final ShippingReceivingActionToolbar shippingReceivingActionToolbar;
	private InventoryEditor inventoryEditor;
	private int quantityOnHand;
	private static final String ADD_STOCK_ADJUSTMENT_TYPE = "Add Stock";

	/**
	 * Cosntructor.
	 */
	public ShippingReceivingDefinition() {
		activityToolbar = new ActivityToolbar((getDriver()));
		shippingReceiving = new ShippingReceiving(getDriver());
		shippingReceivingActionToolbar = new ShippingReceivingActionToolbar(getDriver());
	}

	/**
	 * Add stock.
	 *
	 * @param units   number of units.
	 * @param skuCode sku code.
	 */
	@When("^I add (.+) units? to the stock of sku (.+)$")
	public void addStock(final String units, final String skuCode) {
		adjustQuantityOnHand(units, skuCode, ADD_STOCK_ADJUSTMENT_TYPE);
	}

	/**
	 * Makes one unit available in the stock.
	 *
	 * @param skuCode sku code.
	 */
	@When("^I make one unit of sku (.+) available in the stock$")
	public void addOneToStock(final String skuCode) {
		activityToolbar.clickShippingReceivingButton();
		shippingReceiving.enterSkuCode(skuCode);
		inventoryEditor = shippingReceiving.clickRetrieveButton();
		do {
			inventoryEditor.selectAdjustment(ADD_STOCK_ADJUSTMENT_TYPE);
			inventoryEditor.enterQuantity("1");
			shippingReceivingActionToolbar.saveAll();
			shippingReceivingActionToolbar.clickReloadActiveEditor();
		} while (inventoryEditor.getAvailableQuantity() < 1);
	}

	/**
	 * Remove stock.
	 *
	 * @param units   number of unites.
	 * @param skuCode sku code.
	 */
	@When("^I remove (.+) units? from the stock of sku (.+)$")
	public void removeStock(final String units, final String skuCode) {
		adjustQuantityOnHand(units, skuCode, "Remove Stock");
	}

	/**
	 * Verify quantity on hand increases.
	 *
	 * @param quantity the quantity to verify.
	 */
	@Then("^on hand quantity should increase by (.+)$")
	public void verifyOnHandQuantityIncrease(final int quantity) {
		inventoryEditor.verifyQuantityOnHand(quantityOnHand + quantity);
	}

	/**
	 * Verify quantity on hand decreases.
	 *
	 * @param quantity the quantity.
	 */
	@Then("^on hand quantity should decrease by (.+)$")
	public void verifyOnHandQuantityDecrease(final int quantity) {
		inventoryEditor.verifyQuantityOnHand(quantityOnHand - quantity);
	}

	private void adjustQuantityOnHand(final String units, final String skuCode, final String adjustmentType) {
		shippingReceiving.enterSkuCode(skuCode);
		inventoryEditor = shippingReceiving.clickRetrieveButton();
		quantityOnHand = inventoryEditor.getQuantityOnHand();
		inventoryEditor.selectAdjustment(adjustmentType);
		inventoryEditor.enterQuantity(units);
		shippingReceivingActionToolbar.saveAll();
		shippingReceivingActionToolbar.clickReloadActiveEditor();

		if (ADD_STOCK_ADJUSTMENT_TYPE.equals(adjustmentType)) {
			inventoryEditor.verifyQuantityUpdate(quantityOnHand + Integer.valueOf(units));
		} else {
			inventoryEditor.verifyQuantityUpdate(quantityOnHand - Integer.valueOf(units));
		}

	}

	/**
	 * Verify complete shipment button is present.
	 */
	@And("^I can view Complete Shipment button")
	public void verifyCompleteShipmentButtonIsPresent() {
		shippingReceivingActionToolbar.verifyCompleteShipmentButtonIsPresent();
	}

	/**
	 * Selects warehouse.
	 *
	 * @param warehouseName the warehouse name
	 */
	@And("^I select (.+) warehouse")
	public void selectWarehouse(final String warehouseName) {
		shippingReceivingActionToolbar.selectWarehouse(warehouseName);
	}

}

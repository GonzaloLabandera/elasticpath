package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Complete Shipment Dialog.
 */
public class CompleteShipmentDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String COMPLETE_SHIPMENT_PARENT_CSS = "div[widget-id='Complete Shipment'][widget-type='Shell'] ";
	private static final String SHIPMENT_ID_INPUT_CSS = COMPLETE_SHIPMENT_PARENT_CSS + "div[widget-id='Shipment ID'] input";
	private static final String VALIDATE_BUTTON_CSS = COMPLETE_SHIPMENT_PARENT_CSS + "div[widget-id='Validate'][widget-type='Button']";
	private static final String COMPLETE_BUTTON_CSS = COMPLETE_SHIPMENT_PARENT_CSS + "div[widget-id='Complete'][widget-type='Button']";
	private static final String FORCE_COMPLETION_BUTTON_CSS = COMPLETE_SHIPMENT_PARENT_CSS
			+ "div[widget-id='Force Completion'][widget-type='Button']";
	private static final String FORCE_COMPLETE_SHIPMENT_CONFIRM_DIALOG = "com.elasticpath.cmclient.warehouse.WarehouseMessages"
			+ ".CompleteShipment_ShipmentForceCompletionOkDialogTitle";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CompleteShipmentDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs shipment id.
	 *
	 * @param shipmentId the code.
	 */
	public void enterShipmentId(final String shipmentId) {
		clearAndType(SHIPMENT_ID_INPUT_CSS, shipmentId);
	}

	/**
	 * Clicks validate button.
	 */
	public void clickValidateButton() {
		clickButton(VALIDATE_BUTTON_CSS, "Validate");
	}

	/**
	 * Clicks complete button and wait while dialog disappears.
	 */
	public void clickCompleteButtonAndWaitDialogDisappears() {
		clickCompleteButton();
		waitTillElementDisappears(By.cssSelector(COMPLETE_SHIPMENT_PARENT_CSS));
	}

	/**
	 * Clicks complete button.
	 */
	public void clickCompleteButton() {
		clickButton(COMPLETE_BUTTON_CSS, "Complete");
	}

	/**
	 * Completes shipment.
	 *
	 * @param shipmentId shipment ID
	 */
	public void completeShipment(final String shipmentId) {
		enterShipmentId(shipmentId);
		clickValidateButton();
		clickCompleteButtonAndWaitDialogDisappears();
	}

	/**
	 * Force complete shipment.
	 * @param shipmentID shipment ID
	 * @return ShipmentCompletionErrorDialog
	 */
	public ShipmentCompletionErrorDialog forceCompleteShipment(final String shipmentID) {
		enterShipmentId(shipmentID);
		clickValidateButton();
		clickButton(COMPLETE_BUTTON_CSS, "Complete");
		return new ShipmentCompletionErrorDialog(getDriver());
	}

	/**
	 * Click force completion button.
	 * @return ConfirmDialog
	 */
	public void clickForceCompletionButton() {
		clickButton(FORCE_COMPLETION_BUTTON_CSS, "Force Completion");
		ConfirmDialog confirmDialog = new ConfirmDialog(getDriver());
		confirmDialog.clickOKButton(FORCE_COMPLETE_SHIPMENT_CONFIRM_DIALOG);
		waitTillElementDisappears(By.cssSelector(COMPLETE_SHIPMENT_PARENT_CSS));
	}

}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.wizards;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Create Return Wizard.
 */
public class CreateReturnWizard extends AbstractWizard {

	private static final String DIV_PREFIX_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String CREATE_RETURN_PARENT_CSS = DIV_PREFIX_CSS + ".ReturnWizard_Create_Title'] ";
	private static final String ELECTRONIC_RETURN_PARENT_TABLE_CSS
			= CREATE_RETURN_PARENT_CSS + "div[widget-id='Return Electronic Order Table'][widget-type='Table'] ";
	private static final String ELECTRONIC_RETURN_COLUMN_CSS = ELECTRONIC_RETURN_PARENT_TABLE_CSS + "div[row-id='%s'] div[column-id='0']";
	private static final String ELECTRONIC_RETURN_COLUMN_INPUT_CSS = ELECTRONIC_RETURN_PARENT_TABLE_CSS + " input:not([readonly])";
	private static final String PHYSICAL_RETURN_PARENT_TABLE_CSS
			= CREATE_RETURN_PARENT_CSS + "div[widget-id='Return Subject Page Table'][widget-type='Table'] ";
	private static final String PHYSICAL_RETURN_COLUMN_CSS = PHYSICAL_RETURN_PARENT_TABLE_CSS + "div[row-id='%s'] div[column-id='0']";
	private static final String PHYSICAL_RETURN_COLUMN_INPUT_CSS = PHYSICAL_RETURN_PARENT_TABLE_CSS + " input:not([readonly])";
	private static final String REQUIRED_RETURN_CHECKBOX_CSS = CREATE_RETURN_PARENT_CSS + "div[appearance-id='check-box']";
	private static final String DIV_PREFIX_XPATH = "//div[@automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String NOTES_INPUT_XPATH
			= DIV_PREFIX_XPATH + ".ReturnWizard_Notes_Section']/following-sibling::div[1]//div[@widget-type='Text']/textarea";
	private static final String REFUND_TO_ORIGINAL_RADIO_BUTTON_CSS
			= DIV_PREFIX_CSS + ".RefundOptionsComposite_ReturnToOriginal_RadioButton'][widget-type='Button']";
	private static final String MANUAL_REFUND_RADIO_BUTTON_CSS
			= DIV_PREFIX_CSS + ".RefundOptionsComposite_ManualRefund_RadioButton'][widget-type='Button']";
	private static final String SHIPPING_COST_INPUT_XPATH
			= DIV_PREFIX_XPATH + ".ReturnWizard_ItemShippingCost_Label'][@widget-type='Text']/following-sibling::div[1]/input";
	private static final String LESS_SHIPMENT_DISCOUNT_INPUT_XPATH
			= DIV_PREFIX_XPATH + ".ReturnWizard_ShippingCostDiscount_Label'][@widget-type='Text']/following-sibling::div[1]/input";
	private static final String LESS_RESTOCKING_FEE_INPUT_XPATH
			= DIV_PREFIX_XPATH + ".ReturnWizard_LessRestockingFee_Label'][@widget-type='Text']/following-sibling::div[1]/input";
	private static final String ERROR_MESSAGE_CSS = DIV_PREFIX_CSS + ".ReturnWizard_SubjectPage_Message'] textarea";
	private static final String ERROR_ICON_XPATH
			= DIV_PREFIX_XPATH + ".ReturnWizard_SubjectPage_Message']/preceding-sibling::div[1][@seeable='true']";
	private static final String REFUND_BUTTON = "div[widget-id='Refund']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateReturnWizard(final WebDriver driver) {
		super(driver);
		setWizardType("Create return");
		setWizardStepCss("Step %s of 2 ");
	}

	/**
	 * Enters returned shipping cost (increasing the return total).
	 *
	 * @param shippingCost shipping cost
	 */
	public void enterShippingCost(final BigDecimal shippingCost) {
		final By xpath = By.xpath(SHIPPING_COST_INPUT_XPATH);
		//scrollIntoView(CREATE_RETURN_PARENT_CSS, xpath);
		clearAndType(getDriver().findElement(xpath), shippingCost.toString());
	}

	/**
	 * Enters shipment discount that was used for original shipment (decreasing the return total).
	 *
	 * @param shipmentDiscount less shipment discount
	 */
	public void enterLessShipmentDiscount(final BigDecimal shipmentDiscount) {
		final By xpath = By.xpath(LESS_SHIPMENT_DISCOUNT_INPUT_XPATH);
		//scrollIntoView(CREATE_RETURN_PARENT_CSS, xpath);
		clearAndType(getDriver().findElement(xpath), shipmentDiscount.toString());
	}

	/**
	 * Enters restocking fee (decreasing the return total).
	 *
	 * @param restockingFee restocking fee
	 */
	public void enterLessRestockingFee(final BigDecimal restockingFee) {
		final By xpath = By.xpath(LESS_RESTOCKING_FEE_INPUT_XPATH);
		//scrollIntoView(CREATE_RETURN_PARENT_CSS, xpath);
		clearAndType(getDriver().findElement(xpath), restockingFee.toString());
	}

	/**
	 * Enters digital return quantity.
	 *
	 * @param quantity int
	 * @param skuCode  String
	 */
	public void enterDigitalReturnQuantity(final int quantity, final String skuCode) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(ELECTRONIC_RETURN_COLUMN_CSS, skuCode))));
		clearAndType(ELECTRONIC_RETURN_COLUMN_INPUT_CSS, String.valueOf(quantity));
	}

	/**
	 * Creates digital return.
	 *
	 * @param quantity         int
	 * @param skuCode          String
	 * @param shipmentDiscount less shipment discount
	 * @param manualRefund     uses manual refund option
	 */
	public void createDigitalReturn(final int quantity,
									final String skuCode,
									final BigDecimal shipmentDiscount,
									final boolean manualRefund) {
		enterDigitalReturnQuantity(quantity, skuCode);
		enterLessShipmentDiscount(shipmentDiscount);
		enterNotes("test return of " + skuCode);
		if (!hasError()) {
			clickNextInDialog();
			waitForWizardStep("2");
			if (manualRefund) {
				selectManualRefund();
			} else {
				selectRefundToOriginal();
			}
			clickRefundInDialog();
			waitForWizardStep(null);
			clickFinish();
			waitTillElementDisappears(By.cssSelector(CREATE_RETURN_PARENT_CSS));
		}
	}

	/**
	 * Enters physical return quantity.
	 *
	 * @param quantity int
	 * @param skuCode  String
	 */
	public void enterPhysicalReturnQuantity(final int quantity, final String skuCode) {
		click(getDriver().findElement(By.cssSelector(String.format(PHYSICAL_RETURN_COLUMN_CSS, skuCode))));
		clearAndType(PHYSICAL_RETURN_COLUMN_INPUT_CSS, String.valueOf(quantity));
	}

	/**
	 * Creates physical return.
	 *
	 * @param quantity             int
	 * @param skuCode              String
	 * @param shippingCostReturned shipping cost being returned
	 * @param shipmentDiscount     less shipment discount
	 * @param restockingFee        less re-stocking fee
	 */
	public void createPhysicalReturn(final int quantity, final String skuCode,
									 final BigDecimal shippingCostReturned,
									 final BigDecimal shipmentDiscount,
									 final BigDecimal restockingFee,
									 final boolean returnRequired,
									 final boolean manualRefund) {
		enterPhysicalReturnQuantity(quantity, skuCode);
		enterShippingCost(shippingCostReturned);
		enterLessShipmentDiscount(shipmentDiscount);
		enterLessRestockingFee(restockingFee);
		enterNotes("test return of " + skuCode);
		if (!hasError()) {
			clickNextInDialog();
			waitForWizardStep("2");
			if (!returnRequired || manualRefund) {
				clickRequiredReturnCheckbox();
				if (manualRefund) {
					selectManualRefund();
				} else {
					selectRefundToOriginal();
				}
			}
			clickRefundInDialog();
			waitForWizardStep(null);
			clickFinish();
			waitTillElementDisappears(By.cssSelector(CREATE_RETURN_PARENT_CSS));
		}
	}

	/**
	 * Clicks Physical Return Required Before Refund checkbox.
	 */
	public void clickRequiredReturnCheckbox() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(REQUIRED_RETURN_CHECKBOX_CSS)));
	}

	/**
	 * Selects "Refund to original source" radio button.
	 */
	public void selectRefundToOriginal() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(REFUND_TO_ORIGINAL_RADIO_BUTTON_CSS)));
	}

	/**
	 * Selects "Manual refund" radio button.
	 */
	public void selectManualRefund() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MANUAL_REFUND_RADIO_BUTTON_CSS)));
	}

	/**
	 * Adds return notes.
	 *
	 * @param notesText notes
	 */
	public void enterNotes(final String notesText) {
		final By xpath = By.xpath(NOTES_INPUT_XPATH);
		//scrollIntoView(CREATE_RETURN_PARENT_CSS, xpath);
		clearAndType(getDriver().findElement(xpath), notesText);
	}

	/**
	 * Verifies if wizard has an error.
	 *
	 * @return true if error message is displayed
	 */
	public boolean hasError() {
		setWebDriverImplicitWait(1);
		final List<WebElement> errorElements = getDriver().findElements(By.xpath(ERROR_ICON_XPATH));
		setWebDriverImplicitWaitToDefault();
		return !errorElements.isEmpty();
	}


	/**
	 * Gets an error message in the wizard. It is implied that error message is present, otherwise it's throwing exception.
	 *
	 * @return error message or IllegalStateException if there are none
	 */
	public String getErrorMessage() {
		if (!hasError()) {
			throw new IllegalStateException("No errors were found");
		}
		setWebDriverImplicitWait(1);
		final List<WebElement> errorElements = getDriver().findElements(By.cssSelector(ERROR_MESSAGE_CSS));
		setWebDriverImplicitWaitToDefault();
		if (errorElements.isEmpty()) {
			throw new IllegalStateException("Error message was empty");
		}
		return errorElements.get(0).getAttribute("value");
	}

	/**
	 * Wait for wizard step to load.
	 *
	 * @param wizardCurrentStep the upcoming step.
	 */
	@Override
	public void waitForWizardStep(final String wizardCurrentStep) {
		if (!StringUtils.isEmpty(wizardCurrentStep)) {
			super.waitForWizardStep(wizardCurrentStep);
			return;
		}
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format("div[widget-id='" + getWizardType()
				+ "'][seeable='true']", wizardCurrentStep)));
	}

	/**
	 * Clicks Refund.
	 */
	public void clickRefundInDialog() {
		clickButton(REFUND_BUTTON, "Refund");
	}
}
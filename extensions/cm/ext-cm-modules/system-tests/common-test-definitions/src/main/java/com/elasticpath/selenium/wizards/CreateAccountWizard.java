package com.elasticpath.selenium.wizards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Account Wizard
 */
public class CreateAccountWizard extends AbstractWizard {

	private static final String BUSINESS_NAME_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.CreateAccountWizard_BusinessName'][widget-id='Business Name'] > input";
	private static final String ATTRIBUTE_PARENT_CSS = "div[widget-id='Attributes View'][widget-type='Table'] ";
	private static final String ATTRIBUTE_COLUMN_CSS = ATTRIBUTE_PARENT_CSS + "div[column-id='%s']";
	private static final String INPUT_ATTRIBUTE_CSS = "div[widget-type='Text'][tabindex='5']> input:not([readonly])";
	private static final String EDIT_ATTRIBUTE_CSS = "div[widget-id='Edit Attribute Value...']";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_DECIMAL_VALUE = "decimal";

	/**
	 * Constructor.
	 *
	 * @param driver the driver.
	 */
	public CreateAccountWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Fills in required fields.
	 *
	 * @param businessName business name.
	 */
	public void fillRequiredFields(final String businessName) {
		clearAndType(BUSINESS_NAME_INPUT_CSS, businessName);
		clickNextInDialog();
	}

	/**
	 * Fills in attribute.
	 *
	 * @param attribute attribute value.
	 */
	public void fillDecimalAttribute(final String attribute) {
		selectItemInDialog(ATTRIBUTE_PARENT_CSS, ATTRIBUTE_COLUMN_CSS, COLUMN_DECIMAL_VALUE, COLUMN_NAME);
		click(By.cssSelector(EDIT_ATTRIBUTE_CSS));
		clearAndType(INPUT_ATTRIBUTE_CSS, attribute);
		clickOk();
	}

	/**
	 * Saves account
	 */
	public void saveAccount() {
		clickFinish();
	}
}

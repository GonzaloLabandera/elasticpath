package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Add Edit MerchandisingAssociations Dialog.
 */
public class AddEditMerchandisingAssociationsDialog extends AbstractDialog {

	private static final String ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS =
			"div[automation-id*='com.elasticpath.cmclient.catalog.CatalogMessages.ProductMerchandisingAssociationDialog'][widget-type='Shell'] ";
	private static final String MERCHANDISING_ADD_ITEM_DIALOG_PRODUCT_FIELD = ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS
			+ "div[widget-id='Product Code'] > input";
	private static final String ENABLE_DATE_BUTTON_CSS = ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS
			+ "div[widget-id='Enable Date'] > div[automation-id*='DateSelectTooltip']";
	private static final String CALENDAR_PARENT_CSS =
			"div[automation-id='com.elasticpath.cmclient.core.CoreMessages.DateTimeDialog_EditWindowTitle'] ";
	private static final String DATE_OK_BUTTON_CSS = CALENDAR_PARENT_CSS + "> div[automation-id*='ButtonOK'][seeable='true']";
	private static final String PREVIOUS_MONTH_BUTTON = CALENDAR_PARENT_CSS + "div[widget-id='Previous month']";

	private static final String MERCHANDISING_DIALOG_ITEM_ADD_BUTTON = ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS
			+ "div[widget-id='Add'][widget-type='Button'][seeable='true']";
	private static final String MERCHANDISING_DIALOG_ITEM_SET_BUTTON = ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS
			+ "div[widget-id='Set'][widget-type='Button'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AddEditMerchandisingAssociationsDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs product Code.
	 *
	 * @param productCode the product code
	 */
	public void enterProductCode(final String productCode) {
		clearAndType(MERCHANDISING_ADD_ITEM_DIALOG_PRODUCT_FIELD, productCode);
	}

	/**
	 * Selects previous month's date.
	 */
	public void selectPreviousMonthEnableDate() {
		click(ENABLE_DATE_BUTTON_CSS);
		waitForElementToLoad(getDriver().findElement(By.cssSelector(DATE_OK_BUTTON_CSS)));
		click(PREVIOUS_MONTH_BUTTON);
		click(DATE_OK_BUTTON_CSS);
		waitTillElementDisappears(By.cssSelector(DATE_OK_BUTTON_CSS));
	}

	/**
	 * Clicks add button.
	 */
	public void clickAddButton() {
		clickButton(MERCHANDISING_DIALOG_ITEM_ADD_BUTTON, "Add");
		waitTillElementDisappears(By.cssSelector(ADD_EDIT_MERCHANDISING_ASSOCIATIONS_PARENT_CSS));
	}

	/**
	 * Clicks Set button.
	 */
	public void clickSetButton() {
		clickButton(MERCHANDISING_DIALOG_ITEM_SET_BUTTON, "Set");
		waitTillElementDisappears(By.cssSelector(MERCHANDISING_DIALOG_ITEM_SET_BUTTON));
	}
}

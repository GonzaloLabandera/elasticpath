package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("nls")
public class AddAccountAssociateDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String ADD_ACCOUNT_ASSOCIATES_DIALOG_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.AssociatesDialog_AddAssociateTitle'] ";

	private static final String USER_EMAIL_TEXT_FIELD_CSS = ADD_ACCOUNT_ASSOCIATES_DIALOG_CSS + "div[widget-id='User Email'] input";

	/**
	 * CSS selector used to identify the save button.
	 */
	public static final String SAVE_BUTTON_CSS = ADD_ACCOUNT_ASSOCIATES_DIALOG_CSS
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonSave']";
	
	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AddAccountAssociateDialog(final WebDriver driver) {
		super(driver);
	}
	
	/**
	 * Enter User Email.
	 *
	 * @param userEmail the email of the user to add.
	 */
	public void enterUserEmail(final String userEmail) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(ADD_ACCOUNT_ASSOCIATES_DIALOG_CSS));
		clearAndType(USER_EMAIL_TEXT_FIELD_CSS, userEmail);
	}
	
	/**
	 * Clicks save.
	 */
	@Override
	public void clickSave() {
		clickButton(SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(ADD_ACCOUNT_ASSOCIATES_DIALOG_CSS));
	}
}

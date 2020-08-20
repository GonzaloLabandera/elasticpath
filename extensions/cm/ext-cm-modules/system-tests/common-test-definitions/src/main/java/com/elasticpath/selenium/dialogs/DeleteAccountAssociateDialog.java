/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Delete Account Dialog for Selenium.
 */
public class DeleteAccountAssociateDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String DELETE_ACCOUNT_ASSOCIATE_DIALOG_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.AssociatesPageDialog_RemoveMessage'] "; //$NON-NLS-1$

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public DeleteAccountAssociateDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks OK.
	 */
	@Override
	public void clickOK() {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(DELETE_ACCOUNT_ASSOCIATE_DIALOG_CSS));
		super.clickOK();
		waitTillElementDisappears(By.cssSelector(DELETE_ACCOUNT_ASSOCIATE_DIALOG_CSS));
	}
}
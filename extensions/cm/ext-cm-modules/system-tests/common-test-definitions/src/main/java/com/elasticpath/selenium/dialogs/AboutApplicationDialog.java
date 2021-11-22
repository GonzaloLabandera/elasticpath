/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * About Application Dialog.
 */
public class AboutApplicationDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String ABOUT_APPLICATION_PARENT = "div[widget-id='About'][widget-type='Shell'] ";
	private static final String CLOSE_BUTTON_CSS = ABOUT_APPLICATION_PARENT + "div[widget-id='Close']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AboutApplicationDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks close button.
	 */
	public void clickCloseButton() {
		clickButton(CLOSE_BUTTON_CSS, "Cancel");
		waitTillElementDisappears(By.cssSelector(ABOUT_APPLICATION_PARENT));
	}
}
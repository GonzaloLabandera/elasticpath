/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.WebDriver;

/**
 * Logical representation of the order hold dialog within the order editor.
 */
public class OrderHoldDialog extends AbstractDialog {

	private static final String ORDER_HOLD_COMMENT_TEXTAREA_CSS =
			"div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderHoldDialog_DialogLabelComment'] textarea";

	/**
	 * Constructor.
	 * @param driver the web driver
	 */
	public OrderHoldDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Enter a comment in the comment textarea
	 * @param comment the comment to enter
	 */
	public void enterComment(final String comment) {
		clearAndType(ORDER_HOLD_COMMENT_TEXTAREA_CSS, comment);
	}
}

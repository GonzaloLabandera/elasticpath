/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Note Editor Dialog.
 */
public class OpenNoteDialog extends AbstractDialog {
	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String NOTE_PARENT = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderNoteNotes_DialogTitleOpen']";
	public static final String NOTE_ORIGINATOR = NOTE_PARENT + " div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderNoteNotes_DialogLabelCreatedBy'][widget-type='Text'] input";
	public static final String NOTE_TEXT = NOTE_PARENT + " div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderNoteNotes_DialogLabelNote'][widget-type='Text'] textarea";
	private static final String OK_BUTTON_CSS = NOTE_PARENT + " div[widget-id='OK'][seeable='true'] ";
	private static final String CANCEL_BUTTON_CSS = NOTE_PARENT + " div[widget-id='Cancel'][seeable='true'] ";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public OpenNoteDialog(final WebDriver driver) {
		super(driver);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(NOTE_PARENT));
	}

	/**
	 * Verifies originator value matches.
	 *
	 * @param originator expected value
	 */
	public void verifyOriginator(final String originator) {
		assertThat(getDriver().findElement(By.cssSelector(NOTE_ORIGINATOR)).getAttribute("value"))
				.as("Expected originator value does not match")
				.isEqualTo(originator);
	}

	/**
	 * Verifies note text value matches.
	 *
	 * @param noteParts expected parts
	 */
	public void verifyNote(final Collection<String> noteParts) {
		for (String value : noteParts) {
			assertThat(getDriver().findElement(By.cssSelector(NOTE_TEXT)).getAttribute("value"))
					.as("Expected note text value does not match")
					.contains(value);
		}
	}

	/**
	 * Inputs note value.
	 *
	 * @param noteText String
	 */
	public void enterNoteValue(final String noteText) {
		clearAndType(NOTE_TEXT, noteText);
	}

	/**
	 * Clicks OK button.
	 */
	public void clickOk() {
		clickButton(OK_BUTTON_CSS, "OK");
	}

	/**
	 * Clicks Cancel button.
	 */
	public void clickCancel() {
		clickButton(CANCEL_BUTTON_CSS, "Cancel");
	}
}

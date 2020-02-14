package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.util.Constants;

/**
 * RMA Editor.
 */
public class RmaEditor extends AbstractPageObject {
	/**
	 * RMA editor parent css.
	 */
	public static final String EDITOR_PANE_PARENT_CSS = "div[pane-location='editor-pane'] div[automation-id='com.elasticpath.cmclient.warehouse"
			+ ".editors.orderreturn.OrderReturnPage'][active-editor='true'][seeable='true'] ";
	private static final String EDITOR_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='%s'][seeable='true']";
	private static final String ITEMS_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order Return Details Table'] ";
	private static final String RETURNED_QUANTITY_COLUMN_CSS = ITEMS_TABLE_PARENT_CSS + "div[row-id='%s'] div[column-num='4']";
	private static final String RETURNED_QUANTITY_COLUMN_INPUT_CSS = ITEMS_TABLE_PARENT_CSS
			+ "div:not([appearance-id='ccombo-field']) > input:not([readonly])";
	private static final String RETURNED_QUANTITY_COLUMN_DISABLED_INPUT_CSS = ITEMS_TABLE_PARENT_CSS
			+ "div:not([appearance-id='ccombo-field'])[style*='display: none'] > input:not([readonly])";
	private static final String RETURNED_STATE_COLUMN_CSS = ITEMS_TABLE_PARENT_CSS + "div[row-id='%s'] div[column-num='5']";
	private static final String RMA_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='RMA Status'] > input";
	private static final String ATTRIBUTE_VALUE = "value";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public RmaEditor(final WebDriver driver) {
		super(driver);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(EDITOR_PANE_PARENT_CSS));
	}

	/**
	 * Enters returned quantity and default state.
	 *
	 * @param returnedSku      String
	 * @param returnedQuantity String
	 */
	public void setReturnedQuantity(final String returnedSku, final String returnedQuantity) {
		click(getDriver().findElement(By.cssSelector(String.format(RETURNED_QUANTITY_COLUMN_CSS, returnedSku))));
		int count = 0;
		setWebDriverImplicitWait(1);
		while (isElementPresent(By.cssSelector(String.format(RETURNED_QUANTITY_COLUMN_DISABLED_INPUT_CSS, returnedSku))) && count < Constants.RETRY_COUNTER_3) {
			sleep(500);
			click(getDriver().findElement(By.cssSelector(String.format(RETURNED_QUANTITY_COLUMN_CSS, returnedSku))));
			count++;
		}
		setWebDriverImplicitWaitToDefault();
		clearAndType(RETURNED_QUANTITY_COLUMN_INPUT_CSS, returnedQuantity);
		click(getDriver().findElement(By.cssSelector(String.format(RETURNED_STATE_COLUMN_CSS, returnedSku))));
	}

	/**
	 * Clicks button.
	 *
	 * @param buttonWidgetId String the widget id
	 */
	public void clickEditorButton(final String buttonWidgetId) {
		scrollWidgetIntoView(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		getWaitDriver().waitForElementToBeInteractable(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		clickButton(String.format(EDITOR_BUTTON_CSS, buttonWidgetId), buttonWidgetId);
	}

	/**
	 * Clicks Open Original Order button.
	 *
	 * @return OrderEditor
	 */
	public OrderEditor clickOpenOriginalOrderButton() {
		clickEditorButton("Open Original Order...");
		return new OrderEditor(getDriver());
	}

	/**
	 * Verifies RMA status.
	 *
	 * @param expectedRMAStatus the expected RMA status.
	 */
	public void verifyRMAStatus(final String expectedRMAStatus) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(RMA_STATUS_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("RMA Status validation failed")
				.isEqualTo(expectedRMAStatus);
	}
}

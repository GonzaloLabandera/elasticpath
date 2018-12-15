package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditConfigurationValueDialog;
import com.elasticpath.selenium.util.Constants;

/**
 * System Configuration Result Pane.
 */
public class SystemConfigurationResultPane extends AbstractPageObject {
	private static final String SETTING_NAME_FILTER_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.filterLabel'] + div > input";
	private static final String EXPECTED_SETTING_RESULT_CSS = "div[row-id='%s']";
	private static final String DEFINED_CONTEXT_NAME_ROW_VALUE_CSS = "div[row-id='%s'][widget-type='row'] ";
	private static final String DEFINED_CONTEXT_NAME_COLUMN_VALUE_CSS = "div[column-id='%s']+ div[column-num='1']";
	private static final String MAXIMIZE_WINDOW_CSS = "div[pane-location='center-pane-outer'] div[appearance-id='ctabfolder-button']"
			+ "[widget-id='Maximize'][seeable='true']";
	private static final String RESTORE_WINDOW_CSS = "div[pane-location='left-pane-outer'] div[appearance-id='ctabfolder-button']"
			+ "[widget-id='Restore'][seeable='true']";
	private static final String DEFINED_VALUES_PARENT_CSS = "div[appearance-id='label-wrapper'][widget-id='Defined Values'][seeable='true'] + div";
	private static final String NEW_DEFINED_VALUE_BUTTON_CSS = DEFINED_VALUES_PARENT_CSS + " div[appearance-id='push-button'][widget-id='New...'][seeable='true']";
	private static final String EDIT_DEFINED_VALUE_BUTTON_CSS = DEFINED_VALUES_PARENT_CSS + " div[appearance-id='push-button'][widget-id='Edit...'][seeable='true']";
	private static final String REMOVE_DEFINED_VALUE_BUTTON_CSS = DEFINED_VALUES_PARENT_CSS + " div[appearance-id='push-button'][widget-id='Remove'][seeable='true']";
	private static final String DEFINED_VALUES_TABLE_CSS = DEFINED_VALUES_PARENT_CSS + " div[appearance-id='table'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public SystemConfigurationResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Enters setting name in filter input box.
	 *
	 * @param settingName String
	 */
	public void enterSettingNameFilterInput(final String settingName) {
		clearAndType(SETTING_NAME_FILTER_INPUT_CSS, settingName);
	}

	/**
	 * Maximize System Configuration window.
	 */
	public void maximizeSystemConfigurationWindow() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		if (getDriver().findElements(By.cssSelector(MAXIMIZE_WINDOW_CSS)).size() != 0) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MAXIMIZE_WINDOW_CSS)));
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Restore System Configuration window.
	 */
	public void restoreSystemConfigurationWindow() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		if (getDriver().findElements(By.cssSelector(RESTORE_WINDOW_CSS)).size() != 0) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(RESTORE_WINDOW_CSS)));
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Click New... button to enter defined value
	 */
	public AddEditConfigurationValueDialog clickNewDefinedValueButton() {
		click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(NEW_DEFINED_VALUE_BUTTON_CSS)));
		return new AddEditConfigurationValueDialog(getDriver());
	}

	/**
	 * Click Edit... button to modify defined value
	 */
	public AddEditConfigurationValueDialog clickEditDefinedValueButton() {
		click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(EDIT_DEFINED_VALUE_BUTTON_CSS)));
		return new AddEditConfigurationValueDialog(getDriver());
	}

	/**
	 * Click Remove button to delete selected defined value
	 */
	public void clickRemoveDefinedValueButton() {
		click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(REMOVE_DEFINED_VALUE_BUTTON_CSS)));
	}

	/**
	 * Verifies and Select if setting name shows in filter.
	 *
	 * @param settingName String
	 */
	public void selectSettingName(final String settingName) {
		assertThat(getWaitDriver().waitForElementToBeInteractable(String.format(EXPECTED_SETTING_RESULT_CSS, settingName)))
				.as("Expected setting name not exist - " + settingName)
				.isTrue();
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(EXPECTED_SETTING_RESULT_CSS, settingName))));
	}

	/**
	 * Verifies Defined setting Value.
	 *
	 * @param storeCode    the store code
	 * @param settingValue String
	 */
	public void verifyDefinedSettingValue(final String storeCode, final String settingValue) {
		assertThat(getDriver().findElement(By.cssSelector(String.format(DEFINED_CONTEXT_NAME_ROW_VALUE_CSS, storeCode)
				+ String.format(DEFINED_CONTEXT_NAME_COLUMN_VALUE_CSS, storeCode))).getText())
				.as("Unexpected Defined Value for Store Data Policy " + storeCode)
				.isEqualTo(settingValue);
	}

	/**
	 * Verifies quantity of Defined Values records for selected system setting
	 *
	 * NOTE: Method is implemented with limitation due to the way Defined Values table behaves
	 * The method will count only those records whose Context or Value column values contain 'e' character
	 * For example: truE, falsE, mobEE etc.
	 *
	 * @param count expected number of records
	 */
	public void verifyNumberOfDefinedValuesRecords(final Integer count) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		List<WebElement> allRecords = getWaitDriver().waitForElementToBeVisible(
				By.cssSelector(DEFINED_VALUES_TABLE_CSS)).findElements(By.cssSelector("div[widget-id*='e'][widget-type='row']"));
		assertThat(allRecords.size())
				.as("Unexpected number of Defined Values record(s)")
				.isEqualTo(count);
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Selects specific record and then clicks Remove button
	 *
	 * @param context Defined Values table Context column value
	 * @param value Defined Values table Value column value
	 */
	public void removeDefinedValueRecord(final String context, final String value) {
		selectDefinedValueRecord(context, value);
		clickRemoveDefinedValueButton();
	}

	/**
	 * Selects Defined Values record based on Context and Value pair
	 * If Context value = null then record is selected based only on value column
	 *
	 * @param context
	 * @param value
	 */
	private void selectDefinedValueRecord(final String context, final String value) {
		try {
			List<WebElement> allRecords = getWaitDriver().waitForElementToBeVisible(
					By.cssSelector(DEFINED_VALUES_TABLE_CSS)).findElements(By.cssSelector("div[widget-id][widget-type='row']"));

			for (WebElement record : allRecords) {
				if ("null".equalsIgnoreCase(context)) {		// no context provided
					if (value.equalsIgnoreCase(record.findElement(By.cssSelector("div[column-num='1']")).getText())) {
						click(record);
						break;
					} else {	// both context and value provided
						if (context.equalsIgnoreCase(record.findElement(By.cssSelector("div[column-num='0']")).getText())
								&& value.equalsIgnoreCase(record.findElement(By.cssSelector("div[column-num='1']")).getText())) {
							click(record);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			fail("No Defined Value records found!");
		}
	}
}

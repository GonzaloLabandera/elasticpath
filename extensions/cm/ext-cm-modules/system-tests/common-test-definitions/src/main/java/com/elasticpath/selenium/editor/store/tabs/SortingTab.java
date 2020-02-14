package com.elasticpath.selenium.editor.store.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditSortAttributeDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.domainobjects.SortAttribute;
import com.elasticpath.selenium.util.Constants;

public class SortingTab extends AbstractPageObject {

	private static final String SORTING_CONFIGURATION_PAGE_CSS = "div[automation-id='sortConfigurationPage'] ";
	private static final String SORTING_ADD_BUTTON_CSS = SORTING_CONFIGURATION_PAGE_CSS + "div[widget-id='Add']";
	private static final String SORTING_EDIT_BUTTON_CSS = SORTING_CONFIGURATION_PAGE_CSS + "div[widget-id='Edit']";
	private static final String SORTING_REMOVE_BUTTON_CSS = SORTING_CONFIGURATION_PAGE_CSS + "div[widget-id='Remove']";
	private static final String SORTING_REMOVE_CONFIRMATION_DIALOG_CSS = "com.elasticpath.cmclient.admin.stores.AdminStoresMessages.SortDeleteTitle";

	private static final String SORTING_TABLE_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".StoreSortAttributeConfiguration'][widget-type='Table'] ";
	private static final String SORTING_TABLE_FIRST_ROW_CSS = SORTING_TABLE_CSS + "div[row-num='0'] ";
	private static final String SORTING_TABLE_ATTRIBUTE_KEY_CSS = "[column-num='0']";
	private static final String SORTING_TABLE_DEFAULT_SORT_CSS = "div:nth-child(4)";
	private static final String SORTING_TABLE_SORT_ORDER_CSS = "[column-num='2']";
	private static final String SORTING_TABLE_SORT_GROUP_CSS = "[column-num='3']";
	private static final String SORTING_TABLE_TYPE_CSS = "[column-num='4']";
	private static final String SORTING_TABLE_DISPLAY_NAME_CSS = "[column-num='5']";

	private static final String SORTING_TABLE_SELECTED_ROW_CSS = ".//div[@automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".StoreSortAttributeConfiguration']//div[contains(@style, 'rgb(252, 214, 105)')][not(contains(@style, 'display: none'))]/..";

	/**
	 * constructor.
	 *
	 * @param driver WebDriver which drives this webpage.
	 */
	public SortingTab(final WebDriver driver) {
		super(driver);
	}

	public AddEditSortAttributeDialog clickAddSortAttributeButton() {
		click(By.cssSelector(SORTING_ADD_BUTTON_CSS));
		return new AddEditSortAttributeDialog(getDriver());
	}

	public AddEditSortAttributeDialog clickEditSortAttributeButton() {
		click(By.cssSelector(SORTING_EDIT_BUTTON_CSS));
		return new AddEditSortAttributeDialog(getDriver());
	}

	private WebElement getSelectedSortAttribute() {
		getWaitDriver().waitForElementToBePresent(By.xpath(SORTING_TABLE_SELECTED_ROW_CSS));
		return getDriver().findElement(By.xpath(SORTING_TABLE_SELECTED_ROW_CSS));
	}

	private String getSelectedSortAttributeDisplayName() {
		if (isElementPresent(getSelectedSortAttribute(), By.cssSelector(SORTING_TABLE_DISPLAY_NAME_CSS))) {
			return getSelectedSortAttribute().findElement(By.cssSelector(SORTING_TABLE_DISPLAY_NAME_CSS)).getText();
		}
		return "";
	}

	private String clickFirstSortAttributeRow() {
		String selectedRow = "";
		int retryCounter = 0;
		do {
			click(SORTING_TABLE_FIRST_ROW_CSS);
			sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
			selectedRow = getSelectedSortAttributeDisplayName();
			retryCounter++;
		} while ("".equals(selectedRow) && retryCounter < Constants.RETRY_COUNTER_5);
		return selectedRow;
	}

	private WebElement scrollToSortAttributeUsingDownArrowKey(final String displayName) {
		String previousRow = "";
		String currentRow = clickFirstSortAttributeRow();

		do {
			if (currentRow.equals(displayName)) {
				return getSelectedSortAttribute();
			}
			previousRow = currentRow;
			scrollDownWithDownArrowKey(getDriver().findElement(By.cssSelector(SORTING_TABLE_CSS)), 1);
			currentRow = getSelectedSortAttributeDisplayName();
		} while (!currentRow.equals(previousRow));

		return null;
	}

	public void selectSortAttribute(final String displayName) {
		assertThat(scrollToSortAttributeUsingDownArrowKey(displayName))
				.as("Sort Attribute not found")
				.isNotNull();
	}

	public void clickDefaultSortButton(final String displayName) {
		selectSortAttribute(displayName);
		WebElement element = getSelectedSortAttribute();
		doubleClick(element.findElement(By.cssSelector(SORTING_TABLE_DEFAULT_SORT_CSS)));
	}

	public void verifySortAttributeExistsInTable(final SortAttribute sortAttribute) {
		assertThat(isElementPresent(By.cssSelector(SORTING_TABLE_CSS)))
				.as("Sorting table is not present as expected")
				.isTrue();
		selectSortAttribute(sortAttribute.getDisplayName());
		WebElement row = getSelectedSortAttribute();

		if (sortAttribute.getAttributeKey() != null) {
			assertThat(row.findElement(By.cssSelector(String.format(SORTING_TABLE_ATTRIBUTE_KEY_CSS))).getText())
					.as("Attribute Key is not as expected")
					.isEqualTo(sortAttribute.getAttributeKey());
		}

		if (sortAttribute.getSortOrder() != null) {
			assertThat(row.findElement(By.cssSelector(String.format(SORTING_TABLE_SORT_ORDER_CSS))).getText())
					.as("Sort Order is not as expected")
					.isEqualTo(sortAttribute.getSortOrder());
		}

		if (sortAttribute.getSortGroup() != null) {
			assertThat(row.findElement(By.cssSelector(String.format(SORTING_TABLE_SORT_GROUP_CSS))).getText())
					.as("Sort Group is not as expected")
					.isEqualTo(sortAttribute.getSortGroup());
		}

		if (sortAttribute.getType() != null) {
			assertThat(row.findElement(By.cssSelector(String.format(SORTING_TABLE_TYPE_CSS))).getText())
					.as("Type is not as expected")
					.isEqualTo(sortAttribute.getType());
		}
	}

	public void verifySortAttributeNotInTable(final String displayName) {
		assertThat(scrollToSortAttributeUsingDownArrowKey(displayName))
				.as("Sort Attribute was found")
				.isNull();
	}

	public void removeSortAttribute(final String displayName) {
		scrollToSortAttributeUsingDownArrowKey(displayName);

		click(SORTING_REMOVE_BUTTON_CSS);
		ConfirmDialog dialog = new ConfirmDialog(getDriver());
		dialog.clickOKButton(SORTING_REMOVE_CONFIRMATION_DIALOG_CSS);
	}
}

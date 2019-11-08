package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditLongTextAttributeDialog;
import com.elasticpath.selenium.dialogs.SelectAFeaturedProductDialog;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;

/**
 * Category Editor.
 */
public class CategoryEditor extends AbstractPageObject {

	private static final String ATTRIBUTE_PARENT_CSS = "div[widget-id='Attributes'][widget-type='Table'] ";
	private static final String ATTRIBUTE_COLUMN_CSS = ATTRIBUTE_PARENT_CSS + "div[column-id='%s']";
	private static final String CATEGORY_EDITOR_TAB_PARENT = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages";
	private static final String CATEGORY_TYPE_INPUT_CSS = CATEGORY_EDITOR_TAB_PARENT
			+ ".CategoryEditorOverviewSection_CategoryType'][widget-id='Category Type'] div[widget-id='%s']";
	private static final String CATEGORY_SUMMARY_PARENT_CSS = "div[automation-id='categorySummary'][seeable='true'] ";
	private static final String CATEGORY_NAME_INPUT_CSS = CATEGORY_SUMMARY_PARENT_CSS + "div[widget-id='Category Name'] input";
	private static final String CATEGORY_STORE_VISIBLE_CHECKBOX_CSS = CATEGORY_SUMMARY_PARENT_CSS
			+ "div[widget-type='Button'][appearance-id='check-box']";
	private static final String CATEGORY_TYPE_COMBOMBOX_CSS = CATEGORY_SUMMARY_PARENT_CSS + "div[widget-id='Category Type'][widget-type='CCombo']";
	private static final String LANGUAGE_COMBOMBOX_CSS = CATEGORY_SUMMARY_PARENT_CSS + "div[widget-id='Language: '][widget-type='CCombo']";
	private static final String ENABLE_DATE_TIME_INPUT_CSS = CATEGORY_SUMMARY_PARENT_CSS + "div[widget-id='Enable Date/Time'] input";
	private static final String DISABLE_DATE_TIME_INPUT_CSS = CATEGORY_SUMMARY_PARENT_CSS + "div[widget-id='Disable Date/Time'] input";
	private static final String ATTRIBUTE_EDIT_VALUE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages"
			+ ".AttributePage_ButtonEdit']";
	private static final String CLEAR_ATTRIBUTE_VALUE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages."
			+ "AttributePage_ButtonReset']";
	private static final String STORE_VISIBLE_UNCHECKED = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.Item_StoreVisible'] "
			+ "+ div[appearance-id='check-box'] > div[style*='e53cf03a.png']";
	private static final String STORE_VISIBLE_CHECKED = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.Item_StoreVisible'] "
			+ "+ div[appearance-id='check-box'] > div[style*='1882de9d.png']";
	private static final String FEATURED_PRODUCTS_ADD_BUTTON_CSS = "div[automation-id="
			+ "'com.elasticpath.cmclient.catalog.CatalogMessages.CategoryFeaturedProductsSection_Add'][seeable='true']";
	private static final String TAB_CSS = "div[widget-id='%s'][appearance-id='ctab-item'][seeable='true']";
	private static final String FEATURED_PRODUCTS_PARENT_CSS = "div[automation-id='categoryFeaturedProductsPage'][seeable='true'] ";
	private static final String FEATURED_PRODUCTS_TABLE_ROW_CSS = FEATURED_PRODUCTS_PARENT_CSS + "div[widget-type='table_row']";
	private static final String FEATURED_PRODUCTS_COLUMN_CSS = FEATURED_PRODUCTS_PARENT_CSS + "div[column-id='%s']";
	private static final String FEATURED_PRODUCTS_NAME_COLUMN_CSS = "div[column-num='1']";
	private static final String FEATURED_PRODUCTS_MOVE_UP_BUTTON = CATEGORY_EDITOR_TAB_PARENT + ".CategoryFeaturedProductsSection_Move_Up']";
	private static final String FEATURED_PRODUCTS_REMOVE_BUTTON = CATEGORY_EDITOR_TAB_PARENT + ".CategoryFeaturedProductsSection_Remove']";
	private static final String REMOVE_FEATURED_PRODUCT_PARENT_CSS = CATEGORY_EDITOR_TAB_PARENT + ".CategoryFeaturedDialog_RemoveTitle'] ";
	private static final String REMOVE_FEATURED_PRODUCT_OK_BUTTON = REMOVE_FEATURED_PRODUCT_PARENT_CSS + "div[widget-id='OK'][seeable='true'] ";
	private static final String COLUMN_NAME = "Name";
	private static final String UNABLE_TO_FIND_SETTING = "Unable to find setting - ";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CategoryEditor(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	public void selectTab(final String tabName) {
		String cssSelector = String.format(TAB_CSS, tabName);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		setWebDriverImplicitWait(1);
		if (!isElementPresent(By.cssSelector(cssSelector + "[active-tab='true']"))) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies attribute value.
	 *
	 * @param attributValue the attribute value.
	 */
	public void verifyAttributeValue(final String attributValue) {
		assertThat(selectItemInEditorPane(ATTRIBUTE_PARENT_CSS, ATTRIBUTE_COLUMN_CSS, attributValue, ""))
				.as("Unable to find attribute value - " + attributValue)
				.isTrue();
		new ConfirmDialog(getDriver()).clickCancelButton();
	}

	/**
	 * Verifies category type value.
	 *
	 * @param categoryType the category type.
	 */
	public void verifyCategoryTypeValue(final String categoryType) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(CATEGORY_TYPE_INPUT_CSS, categoryType))).isDisplayed())
				.as("Expected category type not present.")
				.isTrue();
	}

	/**
	 * Edits category name.
	 *
	 * @param newCategoryName String
	 */
	public void enterNewCategoryName(final String newCategoryName) {
		clearAndType(CATEGORY_NAME_INPUT_CSS, newCategoryName);
	}

	/**
	 * @return category name for selected previously language.
	 */
	public String getCategoryName() {
		return getDriver().findElement(By.cssSelector(CATEGORY_NAME_INPUT_CSS)).getAttribute("value");
	}

	/**
	 * Selects a category type.
	 *
	 * @param newCategoryType String
	 */
	public void selectCategoryType(final String newCategoryType) {
		assertThat(selectComboBoxItem(CATEGORY_TYPE_COMBOMBOX_CSS, newCategoryType))
				.as("Unable to find category type - " + newCategoryType)
				.isTrue();
	}

	/**
	 * Clicks store visibility checkbox.
	 */
	public void changeStoreVisibility() {
		if (getDriver().findElement(By.cssSelector(STORE_VISIBLE_CHECKED)).isDisplayed()) {

			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CATEGORY_STORE_VISIBLE_CHECKBOX_CSS)));
		}
	}

	/**
	 * Clicks store visibility checkbox if it is unchecked.
	 *
	 * @return true if status was changed, otherwise returns false.
	 */
	public boolean makeCategoryStoreVisible() {
		boolean isChanged = false;
		setWebDriverImplicitWait(1);
		List<WebElement> notCheckedElements = getDriver().findElements(By.cssSelector(STORE_VISIBLE_UNCHECKED));
		if (!notCheckedElements.isEmpty()) {

			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CATEGORY_STORE_VISIBLE_CHECKBOX_CSS)));
			isChanged = true;
		}
		setWebDriverImplicitWaitToDefault();
		return isChanged;
	}

	/**
	 * Verifies category name.
	 *
	 * @param expCategoryName String
	 */
	public void verifyCategoryName(final String expCategoryName) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(CATEGORY_NAME_INPUT_CSS, expCategoryName))).isDisplayed())
				.as("Expected category name not present.")
				.isTrue();
	}

	/**
	 * Checks Category Store Visibility status.
	 */
	public void checkCategoryStoreVisibilityFalse() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		try {
			getDriver().findElement(By.cssSelector(CATEGORY_NAME_INPUT_CSS)).click();
			getDriver().findElement(By.cssSelector(STORE_VISIBLE_UNCHECKED));
		} catch (Exception e) {
			assertThat(false)
					.as("Store visible is still selected.")
					.isTrue();
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks edit value for a category attributes.
	 *
	 * @param attributeName String
	 * @param newValue      String
	 * @return Edit Long Text Attribute dialog
	 */
	public EditLongTextAttributeDialog editCategoryAttributeValue(final String attributeName, final String newValue) {
		assertThat(selectItemInDialog(ATTRIBUTE_PARENT_CSS, ATTRIBUTE_COLUMN_CSS, attributeName, COLUMN_NAME))
				.as(UNABLE_TO_FIND_SETTING + attributeName)
				.isTrue();
		clickButton(ATTRIBUTE_EDIT_VALUE_BUTTON_CSS, "Edit Value Button", EditLongTextAttributeDialog.PARENT_EDIT_LONG_TEXT_CSS);
		return new EditLongTextAttributeDialog(getDriver());
	}

	/**
	 * Clears the value of a given attribute.
	 *
	 * @param attributeName String
	 */
	public void clickClearAttribute(final String attributeName) {
		assertThat(selectItemInDialog(ATTRIBUTE_PARENT_CSS, ATTRIBUTE_COLUMN_CSS, attributeName, COLUMN_NAME))
				.as(UNABLE_TO_FIND_SETTING + attributeName)
				.isTrue();
		clickButton(CLEAR_ATTRIBUTE_VALUE_BUTTON_CSS, "Clear Atrribute Value");
	}

	/**
	 * Clicks add featured product for a category.
	 *
	 * @param categoryName name of the category.
	 * @return {@link SelectAFeaturedProductDialog}.
	 */
	public SelectAFeaturedProductDialog clickAddFeaturedProductButton(final String categoryName) {
		clickButton(FEATURED_PRODUCTS_ADD_BUTTON_CSS, "Add");

		return new SelectAFeaturedProductDialog(getDriver(), categoryName);
	}

	/**
	 * Move selected product up in featured products list.
	 *
	 * @param productName product name
	 */
	public void moveUpFeaturedProductByName(final String productName) {
		assertThat(selectItemInDialog(FEATURED_PRODUCTS_PARENT_CSS, FEATURED_PRODUCTS_COLUMN_CSS, productName, COLUMN_NAME))
				.as(UNABLE_TO_FIND_SETTING + productName)
				.isTrue();
		clickButton(FEATURED_PRODUCTS_MOVE_UP_BUTTON, "Move Up");
	}

	/**
	 * Removes all feature products from opened category.
	 */
	public void removeAllFeaturedProducts() {
		List<String> featuredProductsNames = new ArrayList<>();
		setWebDriverImplicitWait(1);
		List<WebElement> rows = getDriver().findElements(By.cssSelector(FEATURED_PRODUCTS_TABLE_ROW_CSS));
		setWebDriverImplicitWaitToDefault();
		if (!rows.isEmpty()) {
			for (WebElement row : rows) {
				featuredProductsNames.add(row.findElement(By.cssSelector(FEATURED_PRODUCTS_NAME_COLUMN_CSS)).getText());
			}
		}
		for (String name : featuredProductsNames) {
			removeFeaturedProductByName(name);
		}
	}

	/**
	 * Remove product from featured products list.
	 *
	 * @param productName product name
	 */
	public void removeFeaturedProductByName(final String productName) {
		assertThat(selectItemInDialog(FEATURED_PRODUCTS_PARENT_CSS, FEATURED_PRODUCTS_COLUMN_CSS, productName, COLUMN_NAME))
				.as("Unable to find feature product " + productName + " in opened category.")
				.isTrue();

		clickButton(FEATURED_PRODUCTS_REMOVE_BUTTON, "Remove");
		clickButton(REMOVE_FEATURED_PRODUCT_OK_BUTTON, "OK");
	}

	/**
	 * Enter enable date time.
	 *
	 * @param datePlus number that we add to the current date
	 */
	public void enterEnableDateTime(final Integer datePlus) {
		clearAndType(ENABLE_DATE_TIME_INPUT_CSS, Utility.getDateTimeWithPlus(datePlus));
	}

	/**
	 * Enters enable date time.
	 *
	 * @param formattedDate formatted category enable date
	 */
	public void enterFormattedEnableDate(final String formattedDate) {
		clearAndTypeTextWithoutLoadedCheck(ENABLE_DATE_TIME_INPUT_CSS, formattedDate);
	}

	/**
	 * Enter disable date time.
	 *
	 * @param datePlus number that we add to the current date
	 */
	public void enterDisableDateTime(final Integer datePlus) {
		clearAndType(DISABLE_DATE_TIME_INPUT_CSS, Utility.getDateTimeWithPlus(datePlus));
	}

	/**
	 * Enters disable date time.
	 *
	 * @param formattedDate formatted category disable date
	 */
	public void enterFormattedDisableDate(final String formattedDate) {
		clearAndType(DISABLE_DATE_TIME_INPUT_CSS, formattedDate);
	}

	/**
	 * Enter empty disable date time.
	 */
	public void enterEmptyDisableDateTime() {
		clearField(getDriver().findElement(By.cssSelector(DISABLE_DATE_TIME_INPUT_CSS)));
	}

	/**
	 * Clears disable date time field.
	 */
	public void clearDisableDateTime() {
		WebElement dateField = getDriver().findElement(By.cssSelector(DISABLE_DATE_TIME_INPUT_CSS));
		dateField.clear();
		dateField.sendKeys(Keys.SPACE);
	}

	/**
	 * @return category disable date.
	 */
	public String getDisableDate() {
		return getDriver().findElement(By.cssSelector(DISABLE_DATE_TIME_INPUT_CSS)).getAttribute("value");
	}

	/**
	 * Enter empty enable date time.
	 */
	public void enterEmptyEnableDateTime() {
		clearField(getDriver().findElement(By.cssSelector(ENABLE_DATE_TIME_INPUT_CSS)));
	}

	/**
	 * Select language.
	 *
	 * @param language language which should be chosen
	 * @return true if input language found in select box, false - if not found
	 */
	public boolean selectLanguage(final String language) {
		return selectComboBoxItem(LANGUAGE_COMBOMBOX_CSS, language);
	}

	/**
	 * Click on checked box.
	 */
	public void clickVisibleBox() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(STORE_VISIBLE_CHECKED)));
	}

	/**
	 * Click on unchecked box.
	 */
	public void clickVisibleUncheckedBox() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(STORE_VISIBLE_UNCHECKED)));
	}
}

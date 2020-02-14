package com.elasticpath.selenium.editor.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.SelectACategoryDialog;
import com.elasticpath.selenium.editor.SkuDetailsEditor;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.AddSkuWizard;

/**
 * Product Editor.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class ProductEditor extends AbstractPageObject {

	/**
	 * Page Object Id.
	 */
	public static final String PRODUCT_EDITOR_PARENT_CSS = "div[pane-location='editor-pane'] div[active-editor='true'] ";
	private static final String PRODUCT_NAME_INPUT_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.catalog"
			+ ".CatalogMessages.ProductEditorSummaySection_ProductName'][widget-type='Text'] > input";
	private static final String TAB_CSS = "div[widget-id='%s'][appearance-id='ctab-item'][seeable='true']";
	private static final String CATALOG_ASSIGNMENT_TAB_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String CATEGORY_ASSIGNMENT_MERCHANDISING_CATALOG_TAB_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[widget-id='%s'][seeable='true']";
	private static final String PRODUCT_EDITOR_WITH_PRODUCT_CODE_CLOSE_ICON_CSS = "div[pane-location='editor-pane'] "
			+ "[widget-id*='%s'][appearance-id='ctab-item'][active-tab='true'] > div[style*='.gif']";
	private static final String ADD_SKU_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Add SKU...'][seeable='true']";
	private static final String OPEN_SKU_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Open SKU...'][seeable='true']";
	private static final String REMOVE_SKU_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Remove SKU...'][seeable='true']";
	private static final String SKU_DETAILS_ITEM_PARENT_CSS = "div[widget-id='Multi Sku'][widget-type='Table'] ";
	private static final String SKU_DETAILS_ITEM_COLUMN_CSS = SKU_DETAILS_ITEM_PARENT_CSS + "div[column-id='%s']";
	private static final String ADD_CATEGORY_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Add Category...'][seeable='true']";
	private static final String REMOVE_CATEGORY_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Remove Category...'][seeable='true']";
	private static final String INCLUDE_CATEGORY_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Add/Include "
			+ "Category...'][seeable='true']";
	private static final String EXCLUDE_CATEGORY_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Exclude Category...'][seeable='true']";
	private static final String CATEGORY_DETAILS_ITEM_PARENT_CSS = "div[widget-id='Product Category'][widget-type='Table'] ";
	private static final String CATEGORY_DETAILS_ITEM_COLUMN_CSS = CATEGORY_DETAILS_ITEM_PARENT_CSS + "div[column-id='%s']";
	private static final String PRODUCT_EDITOR = "div[widget-id='%s'][appearance-id='ctab-item']";
	private static final String SKU_CODE_FIELD_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='%s'][widget-type='Text']>input";
	private static final String OPEN_PARENT_PRODUCT_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.MultipleSku_OpenParentProduct'][seeable='true']";
	private static final String ENABLE_DATE_FIELD_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[widget-id='Enable Date/Time']>div[widget-type='Text']>input";
	private static final String DISABLE_DATE_FIELD_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[widget-id='Disable Date/Time']>div[widget-type='Text']>input";
	public static final String SKU_DETAILS_TAB_ID = "SKU Details";
	public static final String SKU_CODE_FIELD_ID = "SKU Code";
	private static final String PRODUCT_LANGUAGE_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.LanguagePulldownLabelText'][widget-type='CCombo']";
	private static final String TAX_CODE_COMBO_PARENT_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Tax Code'][widget-type='CCombo']";
	private static final String PRODUCT_MINIMUM_ORDER_QUANTITY_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorStoreRuleSection_MinOrderQty'] input";
	private static final String SHIPPABLE_TYPE_SHIPPABLE_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuOverview_Shippable'][seeable='true']";
	private static final String SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuOverview_DigitalAsset'][seeable='true']";
	private static final String PRIMARY_CATEGORY_TAX_CODE = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Primary Category'][widget-type='CCombo']";
	private static final String VALUE = "value";
	private static final String AVAILABILITY_RULE_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Availability Rule'][widget-type='CCombo']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ProductEditor(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verify product name.
	 *
	 * @param productName the product name.
	 */
	public void verifyProductName(final String productName) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(PRODUCT_NAME_INPUT_CSS)).getAttribute(VALUE))
				.as("Product name validation failed")
				.isEqualTo(productName);
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	@Override
	public void selectTab(final String tabName) {
		String cssSelector = String.format(TAB_CSS, tabName);
		resizeWindow(cssSelector);
		getWaitDriver().waitForElementToBeInteractable(cssSelector);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		setWebDriverImplicitWait(1);
		if (!isElementPresent(By.cssSelector(cssSelector + "[active-tab='true']"))) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	public void selectTabInOpenedEditor(final String tabName) {
		String cssSelector = String.format(TAB_CSS, tabName);
		String selectedTabCss = cssSelector + "[active-tab='true']";
		resizeWindow(cssSelector);
		getWaitDriver().waitForElementToBeInteractable(cssSelector);
		clickWithoutScrollWidgetIntoViewByCss(cssSelector, selectedTabCss);
		assertThat(isElementPresent(By.cssSelector(selectedTabCss)))
				.as("Tab " + tabName + " was not clicked!")
				.isTrue();
	}

	public void selectVirtualCatalogAssignment(final String catalogName) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(CATALOG_ASSIGNMENT_TAB_CSS, catalogName))));
	}

	/**
	 * Verifies Catalog Tab is not present.
	 *
	 * @param catalogName the catalog name.
	 */
	public void verifyCatalogTabIsNotPresent(final String catalogName) {
		setWebDriverImplicitWait(1);
		assertThat((isElementPresent(By.cssSelector(String.format(CATEGORY_ASSIGNMENT_MERCHANDISING_CATALOG_TAB_CSS, catalogName)))))
				.as("Category Assignment tab shows unexpected catalog tab -" + catalogName)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies Catalog Tab is present.
	 *
	 * @param catalogName the catalog name.
	 */
	public void verifyCatalogTabIsPresent(final String catalogName) {
		getWaitDriver().waitForElementToBeInteractable(PRODUCT_EDITOR_PARENT_CSS);
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		assertThat((isElementPresent(By.cssSelector(String.format(CATEGORY_ASSIGNMENT_MERCHANDISING_CATALOG_TAB_CSS, catalogName)))))
				.as("Category Assignment tab does not show catalog tab " + catalogName)
				.isTrue();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Inputs product name.
	 *
	 * @param productName the product name.
	 */
	public void enterProductName(final String productName) {
		clearAndType(PRODUCT_NAME_INPUT_CSS, productName);
	}

	/**
	 * Inputs product name.
	 *
	 * @param productName the product name.
	 */
	public void enterProductNameWithoutLoadedCheck(final String productName) {
		clearAndTypeTextWithoutLoadedCheck(PRODUCT_NAME_INPUT_CSS, productName);
	}

	/**
	 * Selects a product type in combo box.
	 *
	 * @param language the product type.
	 */
	public void selectLanguage(final String language) {
		assertThat(selectComboBoxItem(PRODUCT_LANGUAGE_CSS, language))
				.as("Unable to find product type - " + language)
				.isTrue();
	}

	/**
	 * Selects a tax code in combo box.
	 *
	 * @param taxCode the tax code.
	 */
	public void selectTaxCode(final String taxCode) {
		assertThat(selectComboBoxItem(TAX_CODE_COMBO_PARENT_CSS, taxCode))
				.as("Unable to find tax code - " + taxCode)
				.isTrue();
	}

	/**
	 * Inputs minimum order quantity.
	 *
	 * @param minimumOrderQuantity the minimum order quantity.
	 */
	public void enterMinimumOrderQuantity(final String minimumOrderQuantity) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(PRODUCT_MINIMUM_ORDER_QUANTITY_CSS)), minimumOrderQuantity);
	}

	/**
	 * Check not sold separately box.
	 */
	public void checkNotSoldSeparatelyBox() {
		click(getWaitDriver().waitForElementToBeClickable(By.xpath("//div[contains(text(), "
				+ "'Not Sold Separately')]/../following-sibling::div[1]/div")));
	}

	/**
	 * Select primary category in ComboBox.
	 *
	 * @param categoryCode code of category.
	 */
	public void selectPrimaryCategory(final String categoryCode) {
		assertThat(selectComboBoxItem(PRIMARY_CATEGORY_TAX_CODE, categoryCode))
				.as("Unable to find category code - " + categoryCode)
				.isTrue();
	}

	/**
	 * Checks that product name is there and is readOnly.
	 */
	public void tryProductNameChange() {
		getWaitDriver().waitForElementToBePresent(By.cssSelector(PRODUCT_NAME_INPUT_CSS + "[readOnly]"));
	}

	/**
	 * Close Product Editor with the given product code.
	 *
	 * @param productCode the product code.
	 */
	public void closeProductEditor(final String productCode) {
		String formattedTabCSS = String.format(PRODUCT_EDITOR_WITH_PRODUCT_CODE_CLOSE_ICON_CSS, productCode);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(formattedTabCSS)));
		waitTillElementDisappears(By.cssSelector(formattedTabCSS));
	}

	/**
	 * Close Product Editor with the given product code without saving changes.
	 *
	 * @param productCode the product code.
	 */
	public void closeProductEditorWithoutSave(final String productCode) {
		String formattedTabCSS = String.format(PRODUCT_EDITOR_WITH_PRODUCT_CODE_CLOSE_ICON_CSS, productCode);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(formattedTabCSS)));
		new ConfirmDialog(getDriver()).clickNoButton("com.elasticpath.cmclient.core.CoreMessages.AbstractCmClientFormEditor_OkTitle_save");
		waitTillElementDisappears(By.cssSelector(formattedTabCSS));
	}

	/**
	 * Click add sku button.
	 *
	 * @return the sku dialog.
	 */
	public AddSkuWizard clickAddSkuButton() {
		clickButton(ADD_SKU_BUTTON_CSS, "Add SKU...");
		return new AddSkuWizard(getDriver());
	}

	/**
	 * Selects sku from product.
	 *
	 * @param skuDetails the sku details
	 */
	public void selectSkuDetails(final String skuDetails) {
		verifySkuDetails(skuDetails);
	}

	/**
	 * Verifies sku details.
	 *
	 * @param skuDetails the product sku
	 */
	public void verifySkuDetails(final String skuDetails) {
		assertThat(selectItemInEditorPaneWithScrollBar(SKU_DETAILS_ITEM_PARENT_CSS, SKU_DETAILS_ITEM_COLUMN_CSS, skuDetails))
				.as("Unable to find sku option - " + skuDetails)
				.isTrue();
	}

	/**
	 * Click open sku button.
	 *
	 * @return the sku editor.
	 */
	public SkuDetailsEditor clickOpenSkuButton() {
		clickButton(OPEN_SKU_BUTTON_CSS, "Open SKU...");
		return new SkuDetailsEditor(getDriver());
	}

	/**
	 * Clicks remove sku button.
	 */
	public void clickRemoveSkuDetailsButton() {
		clickButton(REMOVE_SKU_BUTTON_CSS, "Remove Selection");
	}

	/**
	 * Verifies Product Sku is deleted.
	 *
	 * @param skuDetails String
	 */
	public void verifyProductSkuIsDeleted(final String skuDetails) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(verifyItemIsNotInEditorPaneWithScrollBar(SKU_DETAILS_ITEM_PARENT_CSS, SKU_DETAILS_ITEM_COLUMN_CSS, skuDetails))
				.as("Delete failed, sku is still in the list - " + skuDetails)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Click add category button.
	 *
	 * @return the Selects a Category dialog.
	 */
	public SelectACategoryDialog clickAddCategoryButton() {
		clickButton(ADD_CATEGORY_BUTTON_CSS, "Add Category...");
		return new SelectACategoryDialog(getDriver());
	}

	/**
	 * Selects category from product.
	 *
	 * @param categoryDetails the category details
	 */
	public void selectCategoryDetails(final String categoryDetails) {
		verifyCategoryDetails(categoryDetails);
	}

	/**
	 * Verifies category details.
	 *
	 * @param categoryDetails the product category assignment
	 */
	public void verifyCategoryDetails(final String categoryDetails) {
		assertThat(selectItemInEditorPaneWithScrollBar(CATEGORY_DETAILS_ITEM_PARENT_CSS, CATEGORY_DETAILS_ITEM_COLUMN_CSS, categoryDetails))
				.as("Unable to find category details - " + categoryDetails)
				.isTrue();
	}

	/**
	 * Clicks remove category button.
	 */
	public void clickRemoveCategoryButton() {
		clickButton(REMOVE_CATEGORY_BUTTON_CSS, "Remove Category");
	}

	/**
	 * Clicks exclude category button.
	 */
	public void clickExcludeCategoryButton() {
		clickButton(EXCLUDE_CATEGORY_BUTTON_CSS, "Exclude Category");
	}

	/**
	 * Clicks include category button.
	 */
	public void clickIncludeCategoryButton() {
		clickButton(INCLUDE_CATEGORY_BUTTON_CSS, "Include Category");
	}

	/**
	 * Selects product's editor.
	 *
	 * @param productName the change set name
	 */
	public void selectproductEditor(final String productName) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(PRODUCT_EDITOR, productName))));
	}

	/**
	 * Returns text from SKU Code field of SKU Details tab
	 *
	 * @return text from SKU Code field of a SKU Details tab
	 */
	public String getSkuCode() {
		getWaitDriver().waitForElementToBeInteractable(String.format(SKU_CODE_FIELD_CSS, SKU_CODE_FIELD_ID));
		return getDriver().findElement(By.cssSelector(String.format(SKU_CODE_FIELD_CSS, SKU_CODE_FIELD_ID))).getAttribute(VALUE);
	}

	/**
	 * Returns text from Enable Date/Time field of Summary tab
	 *
	 * @return text from Enable Date/Time field of Summary tab
	 */
	public String getEnableDate() {
		scrollWidgetIntoView(By.cssSelector(ENABLE_DATE_FIELD_CSS), 5);
		getWaitDriver().waitForElementToBeInteractable(ENABLE_DATE_FIELD_CSS);
		return getDriver().findElement(By.cssSelector(ENABLE_DATE_FIELD_CSS)).getAttribute(VALUE);
	}

	/**
	 * Returns text from Disable Date/Time field of Summary tab
	 *
	 * @return text from Disable Date/Time field of Summary tab
	 */
	public String getDisableDate() {
		scrollWidgetIntoView(By.cssSelector(DISABLE_DATE_FIELD_CSS), 5);
		getWaitDriver().waitForElementToBeInteractable(DISABLE_DATE_FIELD_CSS);
		return getDriver().findElement(By.cssSelector(DISABLE_DATE_FIELD_CSS)).getAttribute(VALUE);
	}

	/**
	 * Types new value in Date/Time field of Summary tab
	 *
	 * @param formattedDateTime formatted value for Date/Time field of Summary tab
	 */
	public void setEnableDate(final String formattedDateTime) {
		scrollWidgetIntoView(By.cssSelector(ENABLE_DATE_FIELD_CSS), 5);
		getWaitDriver().waitForElementToBeInteractable(ENABLE_DATE_FIELD_CSS);
		clearAndType(ENABLE_DATE_FIELD_CSS, formattedDateTime);
	}

	/**
	 * Types new value in disable Date/Time field of Summary tab
	 *
	 * @param formattedDateTime formatted value for disable Date/Time field of Summary tab
	 */
	public void setDisableDate(final String formattedDateTime) {
		scrollWidgetIntoView(By.cssSelector(DISABLE_DATE_FIELD_CSS), 5);
		getWaitDriver().waitForElementToBeInteractable(DISABLE_DATE_FIELD_CSS);
		clearAndType(DISABLE_DATE_FIELD_CSS, formattedDateTime);
	}

	/**
	 * Clicks Store Visible checkbox of Summary tab.
	 */
	public void clickStoreVisible() {
		click(getWaitDriver().waitForElementToBeClickable(By.xpath("//div[contains(text(), 'Store Visible')]/../following-sibling::div[1]/div")));
	}

	/**
	 * Returns true if Open Parent Product is present on a page, else returns false
	 *
	 * @return true if Open Parent Product is present on a page, else returns false
	 */
	public boolean isOpenParentProductPresent() {
		//we assume that if Sku Code field is rendered we can check if Open Parent Product button is rendered
		return isElementPresentInCurrentEditor(String.format(SKU_CODE_FIELD_CSS, SKU_CODE_FIELD_ID), OPEN_PARENT_PRODUCT_CSS);
	}

	/**
	 * Returns true if Product Editor tab is present on a page, else returns false
	 *
	 * @return true if Product Editor tab is present on a page, else returns false
	 */
	public boolean isTabPresent(final String tabName) {
		//we assume that if Product editor parent element is rendered we can check if tab is rendered
		return isElementPresentInCurrentEditor(PRODUCT_EDITOR_PARENT_CSS, String.format(TAB_CSS, tabName));
	}

	/**
	 * Returns true if element is present on a page, else returns false
	 *
	 * @param isPresentElement css selector of an element which should be rendered before check
	 * @param waitForElement   css selector of an element for which a check is performed
	 * @return true if element is present on a page, else returns false
	 */
	private boolean isElementPresentInCurrentEditor(final String waitForElement, final String isPresentElement) {
		getWaitDriver().waitForElementToBeInteractable(waitForElement);
		setWebDriverImplicitWait(1);
		boolean isPresent = isElementPresent(By.cssSelector(isPresentElement));
		setWebDriverImplicitWaitToDefault();
		return isPresent;
	}


	/**
	 * Selects shippable type.
	 *
	 * @param shippableType the shippabe type.
	 */
	public void selectShippableType(final String shippableType) {
		if ("Shippable".equalsIgnoreCase(shippableType)) {
			click(getDriver().findElement(By.cssSelector(SHIPPABLE_TYPE_SHIPPABLE_BUTTON_CSS)));
		} else if ("Digital Asset".equalsIgnoreCase(shippableType)) {
			click(getDriver().findElement(By.cssSelector(SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS)));
			click(getDriver().findElement(By.cssSelector(SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS)));
		} else {
			assertThat("Shippable".equalsIgnoreCase(shippableType) || "Digital Asset".equalsIgnoreCase(shippableType))
					.as("Invalid shippable type entered - " + shippableType)
					.isTrue();
		}
	}

	/**
	 * Selects an availability rule.
	 *
	 * @param availabilityRule Availability Rule name.
	 */
	public void selectAvailabilityRule(final String availabilityRule) {
		assertThat(selectComboBoxItem(AVAILABILITY_RULE_CSS, availabilityRule))
				.as("Unable to find availability rule - " + availabilityRule)
				.isTrue();
	}

	/**
	 * Verifies the product's availability rule.
	 *
	 * @param availabilityRule Availability Rule name.
	 */
	public void verifyAvailabilityRule(final String availabilityRule) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(AVAILABILITY_RULE_CSS + " input")).getAttribute(VALUE))
				.as("Availability rule validation failed")
				.isEqualTo(availabilityRule);
	}
}

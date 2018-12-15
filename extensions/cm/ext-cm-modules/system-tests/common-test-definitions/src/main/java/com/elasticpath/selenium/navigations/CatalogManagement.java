package com.elasticpath.selenium.navigations;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.elasticpath.selenium.dialogs.CategoryFinderDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateEditVirtualCatalogDialog;
import com.elasticpath.selenium.dialogs.SelectAProductDialog;
import com.elasticpath.selenium.editor.CategoryEditor;
import com.elasticpath.selenium.editor.catalog.CatalogEditor;
import com.elasticpath.selenium.resultspane.CatalogProductListingPane;
import com.elasticpath.selenium.resultspane.CatalogSearchResultPane;
import com.elasticpath.selenium.resultspane.CatalogSkuSearchResultPane;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CreateBundleWizard;
import com.elasticpath.selenium.wizards.CreateCategoryWizard;
import com.elasticpath.selenium.wizards.CreateProductWizard;

/**
 * Catalog Management Page.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class CatalogManagement extends AbstractNavigation {

	private static final String ACTION_BUTTONS_CSS_PREFIX = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.";
	private static final String ACTION_BUTTONS_CSS_POSTFIX = "[seeable='true']";
	private static final String LEFT_PANE_INNER_PARENT_CSS = "div[pane-location='left-pane-inner'] ";
	private static final String CATALOG_BROWSE_TREE_PARENT_CSS
			= LEFT_PANE_INNER_PARENT_CSS + "div[widget-id='Catalog Browse Tree'][widget-type='Tree'] ";
	private static final String CATALOG_BROWSE_TREE_ITEM_POSTFIX_CSS = "div[widget-id='%s'][widget-type='row']";
	private static final String CATALOG_BROWSE_TREE_ITEM_CSS = CATALOG_BROWSE_TREE_PARENT_CSS + CATALOG_BROWSE_TREE_ITEM_POSTFIX_CSS;
	private static final String CATALOG_BROWSE_TREE_ITEM_WITH_PRECEDED_ELEMENT_CSS = CATALOG_BROWSE_TREE_ITEM_CSS
			+ " ~ " + CATALOG_BROWSE_TREE_ITEM_POSTFIX_CSS;
	private static final String CATALOG_BROWSE_TREE_ITEM_WITH_TWO_PRECEDED_ELEMENTS_CSS = CATALOG_BROWSE_TREE_ITEM_CSS
			+ " ~ " + CATALOG_BROWSE_TREE_ITEM_POSTFIX_CSS + " ~ " + CATALOG_BROWSE_TREE_ITEM_POSTFIX_CSS;
	private static final String CATALOG_EXPAND_ICON_CSS = CATALOG_BROWSE_TREE_ITEM_CSS + " div[expand-icon='']";
	private static final String CATALOG_EXPAND_ICON_WITH_PRECEDED_ELEMENT_CSS = CATALOG_BROWSE_TREE_ITEM_WITH_PRECEDED_ELEMENT_CSS
			+ " div[expand-icon='']";
	private static final String CREATE_PRODUCT_BUTTON_CSS = "div[widget-id='Create Product'][widget-type='ToolItem']";
	private static final String CREATE_BUNDLE_BUTTON_CSS = "div[widget-id='Create Bundle'][widget-type='ToolItem']";
	private static final String PRODUCT_SEARCH_INPUT_CSS = LEFT_PANE_INNER_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_SearchTermsGroup'][seeable='true'] "
			+ "div[automation-id='%s'] > input";
	private static final String NAME_INPUT_AUTOMATION_ID = "com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_Search_Label_ProductName";
	private static final String CODE_INPUT_AUTOMATION_ID = "com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_Search_Label_ProductCode";
	private static final String SKU_INPUT_AUTOMATION_ID = "com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_Search_Label_SkuCode";
	private static final String DELETE_CSS = "div[widget-id='Delete'][seeable='true']";
	private static final String REMOVE_LINKED_CATEGORY_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages."
			+ "CatalogBrowseView_Action_RemoveLinkedCategory'][seeable='true']";
	private static final String RIGHT_CLICK_DELETE_CSS = "div[appearance-id='menu'] div[widget-id='Delete'][seeable='true']";
	private static final String RIGHT_CLICK_ADD_EXISTING_PRODUCT_CSS
			= "div[appearance-id='menu'] div[widget-id='Add Existing Product'][seeable='true']";
	private static final String OPEN_CATALOG_CATEGORY_EDITOR_ICON_CSS
			= "div[widget-id='Catalog Browse ToolBar'][widget-type='ToolBar'] div[widget-id='Open...']";
	private static final String CATALOG_BROWSE_TOOLBAR = "div[widget-id='Catalog Browse ToolBar'][widget-type='ToolBar'] ";
	private static final String CATALOG_SEARCH_TAB_CSS = "div[widget-id^='Sea'][appearance-id='ctab-item'][seeable='true']";
	private static final String CATALOG_BROWSE_TAB_CSS = "div[pane-location='left-pane-outer'] "
			+ "div[widget-id^='Ca'][appearance-id='ctab-item'][seeable='true']";
	private static final String SKU_SEARCH_TAB_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.SkuBundle_Tab_Title']";
	private static final String PRODUCT_SKU_SEARCH_BUTTON_CSS = LEFT_PANE_INNER_PARENT_CSS + "div[widget-id='Search'][seeable='true']";
	private static final String CLEAR_INPUTS_BUTTON_CSS = LEFT_PANE_INNER_PARENT_CSS + "div[widget-id='Clear'][seeable='true']";
	private static final String ACTIVE_PRODUCTS_FILTER_CSS = LEFT_PANE_INNER_PARENT_CSS
			+ "div[widget-id='Show Active Products Only']";
	private static final String ACTIVE_SKUS_FILTER_CSS =
			"div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_Filter_Label_SKUActiveOnly'][seeable='true']";
	private static final String SKU_OPTIONS_FILTER_CSS = LEFT_PANE_INNER_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.SearchView_SkuOptionFiltersGroup']";
	private static final String SKU_OPTION_COMBO_BOX_CSS = SKU_OPTIONS_FILTER_CSS + " div[appearance-id='ccombo']";
	private static final String SKU_OPTION_VALUE_ITEM_CSS = SKU_OPTIONS_FILTER_CSS
			+ " div[widget-id='Sku Option'] div[widget-id='%s']";
	private static final String CREATE_CATEGORY_BUTTON_CSS = CATALOG_BROWSE_TOOLBAR + "div[automation-id='com.elasticpath.cmclient.catalog"
			+ ".CatalogMessages.CatalogBrowseView_Action_CreateCategory'][seeable='true']";
	private static final String CREATE_SUBCATEGORY_BUTTON_CSS = CATALOG_BROWSE_TOOLBAR + ACTION_BUTTONS_CSS_PREFIX
			+ "CatalogBrowseView_Action_CreateSubCategory']" + ACTION_BUTTONS_CSS_POSTFIX;
	private static final String ADD_LINKED_CATEGORY_BUTTON_CSS = CATALOG_BROWSE_TOOLBAR + "div[automation-id='com.elasticpath.cmclient"
			+ ".catalog.CatalogMessages.CatalogBrowseView_Action_AddLinkedCategory'][seeable='true']";
	private static final String OPEN_CATALOG_CATEGORY_BUTTON_CSS
			= "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.CatalogBrowseView_Action_OpenCatalogCategory']"
			+ "[widget-type='ToolItem']";
	private static final String CATALOG_BROWSE_REFRESH_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages"
			+ ".CatalogBrowseView_Action_Refresh']";
	private static final String SKU_SEARCH_SCROLL_BAR_CSS = LEFT_PANE_INNER_PARENT_CSS
			+ "div[appearance-id='scrolledcomposite'][seeable='true']>div[appearance-id='scrollbar'] div[appearance-id='scrollbar-thumb']";
	private static final int SLEEP_TIME_IN_MILLI = 1000;

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CatalogManagement(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks Catalog Browse tab.
	 */
	public void clickCatalogBrowseTab() {
		getWaitDriver().waitForElementToBeInteractable(CATALOG_BROWSE_TAB_CSS);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CATALOG_BROWSE_TAB_CSS)));
	}

	/**
	 * Clicks on Catalog Search tab.
	 */
	public void clickCatalogSearchTab() {
		getWaitDriver().waitForElementToBeInteractable(CATALOG_SEARCH_TAB_CSS);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(CATALOG_SEARCH_TAB_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CATALOG_SEARCH_TAB_CSS)));
	}

	/**
	 * Clicks on SKUs search tab.
	 */
	public void clickSkuSearchTab() {
		getWaitDriver().waitForElementToBeInteractable(SKU_SEARCH_TAB_CSS);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(SKU_SEARCH_TAB_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SKU_SEARCH_TAB_CSS)));
	}

	/**
	 * Enters product name for search.
	 *
	 * @param productName the product name.
	 */
	public void enterProductName(final String productName) {
		clearAndType(String.format(PRODUCT_SEARCH_INPUT_CSS, NAME_INPUT_AUTOMATION_ID), productName);
	}

	/**
	 * Enters product code for search.
	 *
	 * @param productCode the product code.
	 */
	public void enterProductCode(final String productCode) {
		clearAndType(String.format(PRODUCT_SEARCH_INPUT_CSS, CODE_INPUT_AUTOMATION_ID), productCode);
	}

	/**
	 * Enters product name for sku search.
	 *
	 * @param productName a product name to be entered in a product name field of a sku search tab.
	 */
	public void enterProductNameSkuSearch(final String productName) {
		scrollSkuSearchSectionUp();
		enterProductName(productName);
	}

	/**
	 * Enters product sku for search.
	 *
	 * @param productSku a sku code to be entered in a sku code field of a sku search tab.
	 */
	public void enterProductSku(final String productSku) {
		clearAndType(String.format(PRODUCT_SEARCH_INPUT_CSS, SKU_INPUT_AUTOMATION_ID), productSku);
	}

	/**
	 * Enters product name for sku search.
	 *
	 * @param productName a product name to be entered in a product name field of a sku search tab.
	 */
	public void enterProductSkuSkuSearch(final String productName) {
		scrollSkuSearchSectionUp();
		enterProductSku(productName);
	}

	/**
	 * Clicks on catalog search.
	 *
	 * @return CatalogSearchResultPane the search result pane.
	 */
	public CatalogSearchResultPane clickCatalogSearch() {
		clickButtonAndWaitForPaneToOpen(PRODUCT_SKU_SEARCH_BUTTON_CSS, "Search", CatalogSearchResultPane.getSearchProductResultParentCss());
		return new CatalogSearchResultPane(getDriver());
	}

	/**
	 * Clicks on catalog Sku search button.
	 *
	 * @return CatalogSkuSearchResultPane search result pane.
	 */
	public CatalogSkuSearchResultPane clickCatalogSkuSearch() {
		clickButtonAndWaitForPaneToOpen(PRODUCT_SKU_SEARCH_BUTTON_CSS, "Search", CatalogSkuSearchResultPane.getResultTableCss());
		return new CatalogSkuSearchResultPane(getDriver());
	}

	/**
	 * Expand the catalog.
	 *
	 * @param catalogName the catalog name.
	 */
	public void expandCatalog(final String catalogName) {
		click(getWaitDriver().waitForElementToBeClickableNonJSCheck(By.cssSelector(String.format(CATALOG_EXPAND_ICON_CSS, catalogName))));
		sleep(SLEEP_TIME_IN_MILLI);
	}

	/**
	 * Expands the catalog and verifies the catalog was expanded by looking for a category in the catalog.
	 *
	 * @param catalogName  the catalog name.
	 * @param categoryName the category name
	 */
	public void expandCatalogAndVerifyCategory(final String catalogName, final String categoryName) {
		expandTreeAndVerifyItem(String.format(CATALOG_EXPAND_ICON_CSS, catalogName), String.format(CATALOG_BROWSE_TREE_ITEM_CSS,
				categoryName));
	}

	/**
	 * Expands a Category and verifies that a Subcategory is placed under this Category
	 *
	 * @param catalogName     a catalog name.
	 * @param categoryName    a category name
	 * @param subcategoryName a subcategory nabe
	 */
	public void expandCategoryAndVerifySubcategory(final String catalogName, final String categoryName, final String subcategoryName) {
		expandTreeAndVerifyItem(
				String.format(CATALOG_EXPAND_ICON_CSS, catalogName),
				String.format(CATALOG_BROWSE_TREE_ITEM_WITH_PRECEDED_ELEMENT_CSS, catalogName, categoryName)
		);
		expandTreeAndVerifyItem(
				String.format(CATALOG_EXPAND_ICON_WITH_PRECEDED_ELEMENT_CSS, catalogName, categoryName),
				String.format(CATALOG_BROWSE_TREE_ITEM_WITH_TWO_PRECEDED_ELEMENTS_CSS, catalogName, categoryName, subcategoryName)
		);
	}

	/**
	 * Verify catalog exists.
	 *
	 * @param catalogName the catalog name.
	 */
	public void verifyCatalogExists(final String catalogName) {
		assertThat(selectCatalogTreeItem(catalogName))
				.as("Unable to find catalog - " + catalogName)
				.isTrue();
	}

	/**
	 * Select catalog.
	 *
	 * @param catalogName the catalog name.
	 */
	public void selectCatalog(final String catalogName) {
		assertThat(selectCatalogTreeItem(catalogName))
				.as("Unable to find catalog - " + catalogName)
				.isTrue();
	}

	/**
	 * Selects Sku Option in a Sku Options Filter on Search Sku tab.
	 *
	 * @param skuOption sku Option which should be selected in a Sku Options Filter.
	 */
	public void selectSkuOption(final String skuOption) {
		selectComboBoxItem(SKU_OPTION_COMBO_BOX_CSS, skuOption);
	}

	/**
	 * Selects Sku Option Value in a Sku Options Filter on Search Sku tab.
	 *
	 * @param skuOptionValue sku Option Value which should be selected in a Sku Options Filter.
	 */
	public void selectSkuOptionValue(final String skuOptionValue) {
		click(By.cssSelector(String.format(SKU_OPTION_VALUE_ITEM_CSS, skuOptionValue)));
	}

	/**
	 * Select category in catalog.
	 *
	 * @param catalogName  the catalog name.
	 * @param categoryName the category name.
	 */
	public void selectCategoryInCatalog(final String catalogName, final String categoryName) {
		expandCatalogAndVerifyCategory(catalogName, categoryName);
		assertThat(selectCatalogTreeItem(categoryName))
				.as("Unable to find category - " + categoryName)
				.isTrue();
	}

	/**
	 * Select category.
	 *
	 * @param categoryName the category name.
	 */
	public void selectCategory(final String categoryName) {
		assertThat(selectCatalogTreeItem(categoryName))
				.as("Unable to find category - " + categoryName)
				.isTrue();
	}

	/**
	 * Clicks Create Product icon.
	 *
	 * @return CreateProductWizard the wizard.
	 */
	public CreateProductWizard clickCreateProductButton() {
		clickButton(CREATE_PRODUCT_BUTTON_CSS, "Create Product");
		return new CreateProductWizard(getDriver());
	}

	/**
	 * Clicks Create Bundle icon.
	 *
	 * @return CreateBundleWizard the wizard.
	 */
	public CreateBundleWizard clickCreateBundleButton() {
		clickButton(CREATE_BUNDLE_BUTTON_CSS, "Create Bundle");
		return new CreateBundleWizard(getDriver());
	}

	/**
	 * Right click and select 'Delete'.
	 *
	 * @return The confirm dialog
	 */
	public ConfirmDialog rightClickDelete() {
		rightClick(By.cssSelector(RIGHT_CLICK_DELETE_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(RIGHT_CLICK_DELETE_CSS)));
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Right click and select 'Add Existing Product'.
	 *
	 * @return The Select Product dialog
	 */
	public SelectAProductDialog rightClickAddExistingProduct() {
		rightClick(By.cssSelector(RIGHT_CLICK_ADD_EXISTING_PRODUCT_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(RIGHT_CLICK_ADD_EXISTING_PRODUCT_CSS)));
		return new SelectAProductDialog(getDriver());
	}

	/**
	 * Right click and select 'Delete' will display unable to delete error dialog.
	 */
	public void rightClickDeleteDisplaysError() {
		rightClick(By.cssSelector(RIGHT_CLICK_DELETE_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(RIGHT_CLICK_DELETE_CSS)));
	}

	/**
	 * Verifies Catalog is not in the list.
	 *
	 * @param catalogName the catalog name.
	 */
	public void verifyCatalogIsDeleted(final String catalogName) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(selectCatalogTreeItem(catalogName))
				.as("Delete failed, catalog is still in the list - " + catalogName)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks Create Category icon.
	 *
	 * @return the category wizard.
	 */
	public CreateCategoryWizard clickCreateCategoryIcon() {
		clickButton(CREATE_CATEGORY_BUTTON_CSS, "Create Category");
		return new CreateCategoryWizard(getDriver());
	}

	/**
	 * Clicks Create Subcategory icon.
	 *
	 * @return the category wizard.
	 */
	public CreateCategoryWizard clickCreateSubcategoryIcon() {
		clickButton(CREATE_SUBCATEGORY_BUTTON_CSS, "Create Sub-Category");
		return new CreateCategoryWizard(getDriver());
	}

	/**
	 * Clicks Add Linked Category icon.
	 *
	 * @return the Category Finder dialog.
	 */
	public CategoryFinderDialog clickAddLinkedCategoryIcon() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(ADD_LINKED_CATEGORY_BUTTON_CSS)));
		return new CategoryFinderDialog(getDriver());
	}

	/**
	 * Right click and select create category menu item.
	 *
	 * @return the category wizard.
	 */
	public CreateCategoryWizard rightClickAndSelectCreateCategory() {
		rightClick(By.cssSelector(CREATE_CATEGORY_BUTTON_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CREATE_CATEGORY_BUTTON_CSS)));
		return new CreateCategoryWizard(getDriver());
	}

	/**
	 * Double click category.
	 *
	 * @param categoryName the category name.
	 * @return the pane.
	 */
	public CatalogProductListingPane doubleClickCategory(final String categoryName) {
		waitForElementToLoad(getDriver().findElement(By.cssSelector(String.format(CATALOG_BROWSE_TREE_ITEM_CSS, categoryName))));
		setWebDriverImplicitWait(1);
		if (isElementPresent(By.cssSelector(CatalogProductListingPane.PRODUCT_TABLE_PARENT_CSS.trim()))) {
			closePane("Product Listing");
		}
		setWebDriverImplicitWaitToDefault();

		getWaitDriver().waitForElementToBeInteractable(String.format(CATALOG_BROWSE_TREE_ITEM_CSS, categoryName));
		doubleClick(getDriver().findElement(By.cssSelector(String.format(CATALOG_BROWSE_TREE_ITEM_CSS, categoryName))));

		int counter = 0;
		while (!isResultPanePresent(CatalogProductListingPane.PRODUCT_TABLE_PARENT_CSS.trim()) && counter < Constants.RETRY_COUNTER_3) {
			sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			getWaitDriver().waitForElementToBeInteractable(String.format(CATALOG_BROWSE_TREE_ITEM_CSS, categoryName));
			doubleClick(getDriver().findElement(By.cssSelector(String.format(CATALOG_BROWSE_TREE_ITEM_CSS, categoryName))));
			counter++;
		}

		return new CatalogProductListingPane(getDriver());
	}

	/**
	 * Double click a Subcategory.
	 *
	 * @param catalogName     a Catalog name
	 * @param categoryName    a Category name
	 * @param subcategoryName a Subcategory name
	 * @return the pane.
	 */
	public CatalogProductListingPane doubleClickSubcategory(final String catalogName, final String categoryName, final String subcategoryName) {
		return waitAndDoubleClickCatalogTreeItem(
				String.format(CATALOG_BROWSE_TREE_ITEM_WITH_TWO_PRECEDED_ELEMENTS_CSS, catalogName, categoryName, subcategoryName)
		);
	}

	/**
	 * Verify if category exists.
	 *
	 * @param categoryName the category name.
	 */
	public void verifyCategoryExists(final String categoryName) {
		assertThat(selectCatalogTreeItem(categoryName))
				.as("Unable to find category -  " + categoryName)
				.isTrue();
	}

	/**
	 * Clicks Catalog/Category Delete icon.
	 *
	 * @return the confirm dialog.
	 */
	public ConfirmDialog clickDeleteCategoryIcon() {
		rightClick(By.cssSelector(DELETE_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(DELETE_CSS)));
		return new ConfirmDialog(getDriver());
	}


	/**
	 * Clicks Remove Linked Category icon.
	 *
	 * @return the confirm dialog.
	 */
	public ConfirmDialog clickRemoveLinkedCategoryIcon() {
		rightClick(By.cssSelector(REMOVE_LINKED_CATEGORY_CSS));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(REMOVE_LINKED_CATEGORY_CSS)));
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Verifies Category is deleted.
	 *
	 * @param categoryName the category name.
	 */
	public void verifyCategoryIsNotInList(final String categoryName) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(selectCatalogTreeItem(categoryName))
				.as("Delete failed, Category is still present - " + categoryName)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks Open Category Editor icon.
	 *
	 * @return the category editor.
	 */
	public CategoryEditor clickOpenCategoryIcon() {
		clickButton(OPEN_CATALOG_CATEGORY_EDITOR_ICON_CSS, "Open...");
		return new CategoryEditor(getDriver());
	}

	/**
	 * Clicks Open Catalog/Category button.
	 *
	 * @return The catalog editor
	 */
	public CatalogEditor clickOpenCatalogCategoryButton() {
		clickButton(OPEN_CATALOG_CATEGORY_BUTTON_CSS, "Open...");
		return new CatalogEditor(getDriver());
	}

	/**
	 * Clicks Open Catalog/Category button.
	 *
	 * @return The virtual catalog dialog
	 */
	public CreateEditVirtualCatalogDialog clickOpenVirtualCatalogButton() {
		final String dialogName = "Edit";
		clickButton(OPEN_CATALOG_CATEGORY_BUTTON_CSS, "Open...", String.format(CreateEditVirtualCatalogDialog
				.CREATE_EDIT_VIRTUAL_CATALOG_PARENT_CSS_TEMPLATE, dialogName));
		return new CreateEditVirtualCatalogDialog(getDriver(), dialogName);
	}

	/**
	 * Clicks on Catalog browse refresh button.
	 */
	public void clickCatalogRefreshButton() {
		clickButton(CATALOG_BROWSE_REFRESH_BUTTON_CSS, "Refresh Catalog Tree");
	}

	/**
	 * Clear the input fields in product search tab.
	 */
	public void clearInputFieldsInCatalogSearchTab() {
		clickButton(CLEAR_INPUTS_BUTTON_CSS, "Clear");
	}

	/**
	 * Selects active products filter for product search.
	 */
	public void selectActiveProductsFilter() {
		if (!isSelected(ACTIVE_PRODUCTS_FILTER_CSS)) {
			clickCheckBox(ACTIVE_PRODUCTS_FILTER_CSS);
		}
	}

	/**
	 * Selects active skus filter for sku search.
	 */
	public void selectActiveSkusFilter() {
		if (!isSelected(ACTIVE_SKUS_FILTER_CSS)) {
			clickCheckBox(ACTIVE_SKUS_FILTER_CSS);
		}
	}

	/**
	 * Waits for availability and doubleclicks specified Catalog Tree Item
	 *
	 * @param catalogItemCSS css expression to select Catalog Tree Item
	 */
	private CatalogProductListingPane waitAndDoubleClickCatalogTreeItem(final String catalogItemCSS) {
		waitForElementToLoad(getDriver().findElement(By.cssSelector(catalogItemCSS)));
		setWebDriverImplicitWait(1);
		if (isElementPresent(By.cssSelector(CatalogProductListingPane.PRODUCT_TABLE_PARENT_CSS.trim()))) {
			closePane("Product Listing");
		}
		setWebDriverImplicitWaitToDefault();

		getWaitDriver().waitForElementToBeInteractable(catalogItemCSS);
		doubleClick(getDriver().findElement(By.cssSelector(catalogItemCSS)));

		int counter = 0;
		while (!isResultPanePresent(CatalogProductListingPane.PRODUCT_TABLE_PARENT_CSS.trim()) && counter < Constants.RETRY_COUNTER_3) {
			sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			getWaitDriver().waitForElementToBeInteractable(catalogItemCSS);
			doubleClick(getDriver().findElement(By.cssSelector(catalogItemCSS)));
			counter++;
		}

		return new CatalogProductListingPane(getDriver());
	}

	/**
	 * Scrolling sku search input section up by 200 px.
	 *
	 */
	public void scrollSkuSearchSectionUp() {
		setWebDriverImplicitWait(1);
		if(getDriver().findElements(By.cssSelector(SKU_SEARCH_SCROLL_BAR_CSS)).size() > 0){
			Actions action = new Actions(getDriver());
			WebElement activeSkuFilter = getDriver().findElement(By.cssSelector(SKU_SEARCH_SCROLL_BAR_CSS));
			action.moveToElement(activeSkuFilter).clickAndHold().moveByOffset(0, -200).release().build().perform();
		}
		setWebDriverImplicitWaitToDefault();
	}
}

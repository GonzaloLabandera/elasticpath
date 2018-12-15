package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.editor.product.ProductEditor;
import com.elasticpath.selenium.util.Constants;

/**
 * Catalog search results pane.
 */
public class CatalogSearchResultPane extends AbstractPageObject {

	private static final String SEARCH_RESULTS_TAB_WIDGET_ID = "Product Search Results";
	private static final String CENTER_PANE_PARENT_CSS = "div[pane-location='center-pane-inner'] ";
	private static final String DELETE_PRODUCT_BUTTON_CSS = CENTER_PANE_PARENT_CSS + "div[widget-id='Delete Product'][seeable='true']";
	private static final String PRODUCT_SEARCH_RESULT_PARENT_CSS = "div[widget-id='Search Product List'][widget-type='Table'] ";
	private static final String PRODUCT_SEARCH_RESULT_ROW_CSS = PRODUCT_SEARCH_RESULT_PARENT_CSS + "div[parent-widget-id='Search Product List']";
	private static final String PRODUCT_SEARCH_RESULT_COLUMN_CSS = PRODUCT_SEARCH_RESULT_PARENT_CSS + "div[column-id='%s']";
	private static final String PRODUCT_SEARCH_RESULT_FIRST_CELL_CSS = PRODUCT_SEARCH_RESULT_PARENT_CSS
			+ "div[widget-type='table_row'] div[column-num='1']";
	private static final String PRODUCT_NAME_COLUMN_NAME = "Product Name";
	private static final String PRODUCT_CODE_COLUMN_NAME = "Product Code";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CatalogSearchResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies if product exists.
	 *
	 * @param productName String
	 */
	public void verifyProductNameExists(final String productName) {
		assertThat(isProductNameInList(productName))
				.as("Expected Product does not exist in search result - " + productName)
				.isTrue();
	}

	/**
	 * Verifies if product exists.
	 *
	 * @param productCode String
	 */
	public void verifyProductCodeExists(final String productCode) {
		assertThat(isProductCodeInList(productCode))
				.as("Expected Product does not exist in search result - " + productCode)
				.isTrue();
	}

	/**
	 * Verifies if product is present in search result table by product name.
	 *
	 * @param productName product name
	 * @return true if product is present in a search result table, else - false
	 */
	public boolean isProductNameInList(final String productName) {
		return isProductInList(productName, PRODUCT_NAME_COLUMN_NAME);
	}

	/**
	 * Verifies if product is present in search result table by product code.
	 *
	 * @param productCode product code
	 * @return true if product is present in a search result table, else - false
	 */
	public boolean isProductCodeInList(final String productCode) {
		return isProductInList(productCode, PRODUCT_CODE_COLUMN_NAME);
	}

	/**
	 * Verifies if Product is deleted.
	 *
	 * @param productName String
	 */
	public void verifyProductIsDeleted(final String productName) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(selectItemInCenterPane(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_COLUMN_CSS, productName, PRODUCT_NAME_COLUMN_NAME))
				.as("Delete failed, Product is still in the list - " + productName)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks Delete Product button.
	 *
	 * @return DeleteConfirmDialog
	 */
	public ConfirmDialog clickDeleteProductButton() {
		clickButton(DELETE_PRODUCT_BUTTON_CSS, "Delete Product");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Returns parent table css.
	 *
	 * @return PRODUCT_SEARCH_RESULT_PARENT_CSS the parent table css
	 */
	public static String getSearchProductResultParentCss() {
		return PRODUCT_SEARCH_RESULT_PARENT_CSS.trim();
	}

	/**
	 * Select product and open editor.
	 *
	 * @param productName Product Name.
	 * @return the product editor.
	 */
	public ProductEditor openProductEditorWithProductName(final String productName) {
		verifyProductNameExists(productName);
		doubleClick(getSelectedElement(), ProductEditor.PRODUCT_EDITOR_PARENT_CSS);
		return new ProductEditor(getDriver());
	}

	/**
	 * Select product with given product code and open editor.
	 *
	 * @param productCode Product Code.
	 * @return the product editor.
	 */
	public ProductEditor openProductEditorWithProductCode(final String productCode) {
		verifyProductCodeExists(productCode);
		doubleClick(getSelectedElement(), ProductEditor.PRODUCT_EDITOR_PARENT_CSS);
		return new ProductEditor(getDriver());
	}

	/**
	 * Selects first product from search result table and opens editor.
	 *
	 * @return the product editor.
	 */
	public ProductEditor openProductEditorForFirstSearchResultEntry() {
		assertThat(selectItemInCenterPaneFirstPageByCell(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_FIRST_CELL_CSS))
				.as("Failed to open product editor for the first search result entry")
				.isTrue();
		doubleClick(getSelectedElement(), ProductEditor.PRODUCT_EDITOR_PARENT_CSS);
		return new ProductEditor(getDriver());
	}

	/**
	 * Ensures that search result table is rendered
	 */
	public void waitForSearchResultTableIsRendered() {
		getWaitDriver().waitForElementToBeInteractable(PRODUCT_SEARCH_RESULT_PARENT_CSS);
	}

	/**
	 * Verifies if product is present in search result table by provided value.
	 *
	 * @param verificationCriteria value used for verification of a product presence in a search result table
	 * @return true if product is present in a search result table, else - false
	 */
	private boolean isProductInList(final String verificationCriteria, final String columnName) {
		return selectItemInCenterPane(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_COLUMN_CSS, verificationCriteria, columnName);
	}

	/**
	 * Verifies if a search result table empty.
	 *
	 * @return true if the customer was not found in a search result list
	 */
	public boolean isSearchResultTableEmpty() {
		getWaitDriver().waitForElementToBeInteractable(CENTER_PANE_PARENT_CSS);
		setWebDriverImplicitWait(1);
		int rows = getDriver().findElements(By.cssSelector(PRODUCT_SEARCH_RESULT_ROW_CSS)).size();
		setWebDriverImplicitWaitToDefault();
		if (rows == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Closes Product Search Results tab.
	 */
	public void closeProductSearchResultsPane() {
		closePane(SEARCH_RESULTS_TAB_WIDGET_ID);
	}
}

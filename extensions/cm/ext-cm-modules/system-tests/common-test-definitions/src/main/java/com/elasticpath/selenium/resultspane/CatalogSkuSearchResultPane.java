package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.editor.product.ProductEditor;

/**
 * Sku search results pane.
 */
public class CatalogSkuSearchResultPane extends AbstractPageObject {

	private static final String TABLE_WIDGET_ID = "Search Sku List";
	private static final String TABLE_CSS = "div[widget-id='" + TABLE_WIDGET_ID + "'][widget-type='Table']";
	private static final String COLUMN_CSS = TABLE_CSS + " div[column-id='%s']";
	private static final String TABLE_ROW_CSS = TABLE_CSS + " div[parent-widget-id='" + TABLE_WIDGET_ID + "']";
	private static final String TABLE_CELL_CSS = TABLE_ROW_CSS + " div[column-id='%s'][column-num='%s']";
	private static final String FIRST_CELL_CSS = TABLE_CSS + " div[widget-type='table_row'] div[column-num='1']";
	private static final String SKU_CODE_COLUMN_NAME = "SKU Code";
	public static final int PRODUCT_NAME_COLUMN_INDEX = 2;

	public CatalogSkuSearchResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Returns a search result table css selector.
	 *
	 * @return a table css
	 */
	public static String getResultTableCss() {
		return TABLE_CSS;
	}

	/**
	 * Ensures that search result table is rendered
	 */
	public void waitForSearchResultTableIsRendered() {
		getWaitDriver().waitForElementToBeInteractable(TABLE_CSS);
	}

	/**
	 * Checks if an entity with provided sku code is present in a search result table.
	 *
	 * @param skuCode sku code as a verification criteria
	 * @return true if product is present in a search result table, else - false
	 */
	public boolean isSkuCodeInList(final String skuCode) {
		return isEntityInList(skuCode, SKU_CODE_COLUMN_NAME);
	}

	/**
	 * Verifies if product is present in search result table by provided value.
	 *
	 * @param verificationCriteria value used for verification of a product presence in a search result table
	 * @return true if product is present in a search result table, else - false
	 */
	private boolean isEntityInList(final String verificationCriteria, final String columnName) {
		return selectItemInCenterPane(TABLE_CSS, COLUMN_CSS, verificationCriteria, columnName);
	}

	/**
	 * Selects first product from search result table and opens editor.
	 *
	 * @return the product editor.
	 */
	public ProductEditor openProductEditorForFirstSearchResultEntry() {
		assertThat(selectItemInCenterPaneFirstPageByCell(FIRST_CELL_CSS))
				.as("There are no entries in a result table")
				.isTrue();
		doubleClick(getSelectedElement(), ProductEditor.PRODUCT_EDITOR_PARENT_CSS);
		return new ProductEditor(getDriver());
	}

	/**
	 * Verifies if provided value is in all the table entries.
	 *
	 * @param expectedValue expected value to find in all the table entries
	 * @param columnIndex   an index of the table column for verification
	 */
	public void isValueInAllListEntries(final String expectedValue, final int columnIndex) {
		assertThat(getSearchResultTableElements(String.format(TABLE_CELL_CSS, expectedValue, columnIndex)).size())
				.as("Unexpected amount of search result entries which satisfy search criteria")
				.isEqualTo(getSearchResultTableElements(TABLE_ROW_CSS).size());
	}

	/**
	 * Returns collection of table elements using provided Css selector
	 *
	 * @param elementsCss Css selector used for elements collection
	 * @return collection of table elements using provided Css selector
	 */
	private List<WebElement> getSearchResultTableElements(final String elementsCss) {
		getWaitDriver().waitForElementToBeInteractable(TABLE_CSS);
		setWebDriverImplicitWait(1);
		List<WebElement> tableElements = getDriver().findElements(By.cssSelector(elementsCss));
		setWebDriverImplicitWaitToDefault();
		return tableElements;
	}

}

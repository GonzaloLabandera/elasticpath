package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.util.Constants;

/**
 * Catalog search results pane.
 */
public class CatalogSearchResultPane extends AbstractPageObject {

	private static final String CENTER_PANE_PARENT_CSS = "div[pane-location='center-pane-inner'] ";
	private static final String DELETE_PRODUCT_BUTTON_CSS = CENTER_PANE_PARENT_CSS + "div[widget-id='Delete Product'][seeable='true']";
	private static final String PRODUCT_SEARCH_RESULT_PARENT_CSS = "div[widget-id='Search Product List'][widget-type='Table'] ";
	private static final String PRODUCT_SEARCH_RESULT_COLUMN_CSS = PRODUCT_SEARCH_RESULT_PARENT_CSS + "div[column-id='%s']";
	private static final String PRODUCT_NAME_COLUMNNAME = "Product Name";


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
		assertThat(selectItemInCenterPane(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_COLUMN_CSS, productName, PRODUCT_NAME_COLUMNNAME))
				.as("Expected Product does not exist in search result - " + productName)
				.isTrue();
	}

	/**
	 * Verifies if product exists.
	 *
	 * @param productName String
	 * @return boolean
	 */
	public boolean isProductInList(final String productName) {
		setWebDriverImplicitWait(1);
		boolean isProdcuctInList = selectItemInCenterPane(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_COLUMN_CSS, productName,
				PRODUCT_NAME_COLUMNNAME);
		setWebDriverImplicitWaitToDefault();
		return isProdcuctInList;
	}

	/**
	 * Verifies if Product is deleted.
	 *
	 * @param productName String
	 */
	public void verifyProductIsDeleted(final String productName) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(selectItemInCenterPane(PRODUCT_SEARCH_RESULT_PARENT_CSS, PRODUCT_SEARCH_RESULT_COLUMN_CSS, productName, PRODUCT_NAME_COLUMNNAME))
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
}

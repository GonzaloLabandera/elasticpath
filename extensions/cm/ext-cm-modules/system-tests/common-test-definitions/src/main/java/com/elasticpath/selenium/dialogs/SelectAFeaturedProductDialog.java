package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Select a Product Dialog in featured product tab in category editor.
 */
public class SelectAFeaturedProductDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	private static final String SELECT_A_PRODUCT_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages."
			+ "ProductFinderDialog_ByCategory_WindowTitle'][widget-type='Shell'] ";
	private static final String PRODUCT_CODE_INPUT_CSS = SELECT_A_PRODUCT_PARENT_CSS + "div[widget-id='Product Code'] > input";
	private static final String PRODUCT_NAME_INPUT_CSS = SELECT_A_PRODUCT_PARENT_CSS + "div[widget-id='Product Name'] > input";
	private static final String SEARCH_BUTTON_CSS = SELECT_A_PRODUCT_PARENT_CSS + "div[widget-id='Search'][widget-type='Button']";
	private static final String PRODUCT_RESULT_PARENT_CSS = "div[widget-id='Product Finder'][widget-type='Table'] ";
	private static final String PRODUCT_RESULT_COLUMN_CSS = PRODUCT_RESULT_PARENT_CSS + "div[column-id='%s']";
	private final String categoryName;

	/**
	 * Constructor.
	 *
	 * @param driver       WebDriver which drives this page.
	 * @param categoryName name of the category for which dialog is opened.
	 */
	public SelectAFeaturedProductDialog(final WebDriver driver, final String categoryName) {
		super(driver);
		this.categoryName = categoryName;
	}

	/**
	 * Inputs product code.
	 *
	 * @param productCode the product code.
	 */
	public void enterProductCode(final String productCode) {
		final String cssString = String.format(PRODUCT_CODE_INPUT_CSS, categoryName);
		clearAndType(cssString, productCode);
	}

	/**
	 * Inputs product name.
	 *
	 * @param productName the product name.
	 */
	public void enterProductName(final String productName) {
		final String cssString = String.format(PRODUCT_NAME_INPUT_CSS, categoryName);
		clearAndType(cssString, productName);
	}

	/**
	 * Clicks search button.
	 */
	public void clickSearchButton() {
		clickButton(SEARCH_BUTTON_CSS, "Search");
	}

	/**
	 * Selects the item in search result list.
	 *
	 * @param productName the product name.
	 */
	public void selectProductByName(final String productName) {
		assertThat(selectItemInDialog(PRODUCT_RESULT_PARENT_CSS, PRODUCT_RESULT_COLUMN_CSS, productName, "Product Name"))
				.as("Unable to find product name - " + productName)
				.isTrue();
	}

	/**
	 * Clicks OK.
	 */
	public void clickOKButton() {
		clickButton(OK_BUTTON_CSS, "OK");
		waitTillElementDisappears(By.cssSelector(SELECT_A_PRODUCT_PARENT_CSS));
	}

}

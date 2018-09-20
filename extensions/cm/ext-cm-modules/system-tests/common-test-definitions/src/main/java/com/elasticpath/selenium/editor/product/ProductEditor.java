package com.elasticpath.selenium.editor.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.util.Constants;

/**
 * Product Editor.
 */
public class ProductEditor extends AbstractPageObject {

	private static final String PRODUCT_EDITOR_PARENT_CSS = "div[pane-location='editor-pane'] div[active-editor='true'] ";
	private static final String PRODUCT_NAME_INPUT_CSS = PRODUCT_EDITOR_PARENT_CSS + "div[widget-id='Product Name'][widget-type='Text'] > input";
	private static final String TAB_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages"
			+ ".Product%sPage_Title'][seeable='true']";
	private static final String CATEGORY_ASSIGNMENT_MERCHANDISING_CATALOG_TAB_CSS = PRODUCT_EDITOR_PARENT_CSS
			+ "div[widget-id='%s'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ProductEditor(final WebDriver driver) {
		super(driver);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(PRODUCT_EDITOR_PARENT_CSS));
	}

	/**
	 * Verify product name.
	 *
	 * @param productName the product name.
	 */
	public void verifyProductName(final String productName) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(PRODUCT_NAME_INPUT_CSS)).getAttribute("value"))
				.as("Product name validation failed")
				.isEqualTo(productName);
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	public void selectTab(final String tabName) {
		getWaitDriver().waitForElementToBeInteractable(String.format(TAB_CSS, tabName));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(TAB_CSS, tabName))));
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
	 * Verifies Catalog Tab is not present.
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

}

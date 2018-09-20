package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.editor.product.ProductEditor;
import com.elasticpath.selenium.util.Constants;

/**
 * Catalog product listing pane.
 */
public class CatalogProductListingPane extends AbstractPageObject {

	private static final String PRODUCT_TABLE_PARENT_CSS = "div[widget-id='Browse Product List'][widget-type='Table'] ";
	private static final String PRODUCT_ROW_COLUMN_CSS = PRODUCT_TABLE_PARENT_CSS + "div[column-id='%s']";
	private static final String EXCLUDE_PRODUCT_BUTTON_CSS = "div[widget-id='Exclude Product'][seeable='true']";
	private static final String INCLUDE_PRODUCT_BUTTON_CSS = "div[widget-id='Include Product'][seeable='true']";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CatalogProductListingPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies if product exists.
	 *
	 * @param expectedProductName the expected product name.
	 */
	public void verifyProductNameExists(final String expectedProductName) {
		assertThat(selectItemInCenterPane(PRODUCT_TABLE_PARENT_CSS, PRODUCT_ROW_COLUMN_CSS, expectedProductName, "Product Name"))
				.as("Expected Product does not exist in product listing - " + expectedProductName)
				.isTrue();
	}

	/**
	 * Close the Catalog product list ipane.
	 *
	 * @param tabName The tab name to close.
	 */
	public void close(final String tabName) {
		getWaitDriver().waitForElementToBeInteractable("[widget-id='" + tabName + "'][active-tab='true'][appearance-id='ctab-item']");
		String closeCSS = "[widget-id='" + tabName + "'][active-tab='true'][appearance-id='ctab-item'] :nth-toolTipTextChild(3)";
		WebElement element = getWaitDriver().waitForElementToBeVisible(By.cssSelector(closeCSS));
		click(element);
	}

	/**
	 * Select order and open editor.
	 *
	 * @return the order editor.
	 */
	public ProductEditor selectProductAndOpenProductEditor() {
		doubleClick(getSelectedElement());
		return new ProductEditor(getDriver());
	}

	/**
	 * Clicks Exclude Product.
	 */
	public void clickExcludeProductButton() {
		clickButton(EXCLUDE_PRODUCT_BUTTON_CSS, "Exclude Product");
	}

	/**
	 * Clicks Include Product.
	 */
	public void clickIncludeProductButton() {
		int retry = 0;
		while (isButtonEnabled(INCLUDE_PRODUCT_BUTTON_CSS) && retry < Constants.RETRY_COUNTER) {
			getDriver().findElement(By.cssSelector(INCLUDE_PRODUCT_BUTTON_CSS)).click();

			if (!isButtonEnabled(INCLUDE_PRODUCT_BUTTON_CSS)) {
				sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
				retry++;
			}
		}
	}

	/**
	 * Returns parent table css.
	 *
	 * @return PRODUCT_TABLE_PARENT_CSS the parent table css
	 */
	public static String getProductTableParentCss() {
		return PRODUCT_TABLE_PARENT_CSS.trim();
	}

	/**
	 * Checks if Exclude Product button enabled.
	 *
	 * @return boolean.
	 */
	public boolean isExcludeProductButtonEnabled() {
		boolean isEnabled = false;
		setWebDriverImplicitWait(1);
		try {
			getDriver().findElement(By.cssSelector(EXCLUDE_PRODUCT_BUTTON_CSS));
			isEnabled = true;
			setWebDriverImplicitWaitToDefault();
		} catch (Exception e) {
			isEnabled = false;
			setWebDriverImplicitWaitToDefault();
		}
		return isEnabled;
	}

	/**
	 * Checks if Include Product button enabled.
	 *
	 * @return boolean.
	 */
	public boolean isIncludeProductButtonEnabled() {
		boolean isEnabled = false;
		setWebDriverImplicitWait(1);
		try {
			getDriver().findElement(By.cssSelector(INCLUDE_PRODUCT_BUTTON_CSS));
			isEnabled = true;
			setWebDriverImplicitWaitToDefault();
		} catch (Exception e) {
			isEnabled = false;
			setWebDriverImplicitWaitToDefault();
		}
		return isEnabled;
	}
}

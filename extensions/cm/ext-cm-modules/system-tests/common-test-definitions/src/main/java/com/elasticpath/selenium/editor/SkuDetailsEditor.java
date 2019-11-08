package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Sku Details Editor.
 */
public class SkuDetailsEditor extends AbstractPageObject {
	private static final String SKU_EDITOR_PARENT_CSS = "div[pane-location='editor-pane'] div[active-editor='true'] ";
	private static final String SKU_DETAILS_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages."
			+ "ProductEditorSingleSkuOverview_SkuConfiguration'] > input";
	private static final String SKU_EDITOR_CLOSE_ICON_CSS = "[appearance-id='ctab-item'][active-tab='true'] > "
			+ "div[style*='.gif']";
	private static final String ENABLE_DATE_TIME_CSS = SKU_EDITOR_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.catalog"
			+ ".CatalogMessages.ProductEditorSingleSkuOverview_EnableDateTime'] input";
	private static final String DISABLE_DATE_TIME_CSS = SKU_EDITOR_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.catalog"
			+ ".CatalogMessages.ProductEditorSingleSkuOverview_DisableDateTime'] input";
	private static final String TAX_CODE_COMBO_PARENT_CSS = SKU_EDITOR_PARENT_CSS + "div[widget-id='Tax Code'][widget-type='CCombo']";
	private static final String SHIPPABLE_TYPE_SHIPPABLE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages"
			+ ".ProductEditorSingleSkuOverview_ShippableType'][seeable='true']";
	private static final String SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages"
			+ ".ProductEditorSingleSkuOverview_DigitalAsset'][seeable='true']";
	private static final String TAB_CSS = "div[widget-id='%s'][appearance-id='ctab-item'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public SkuDetailsEditor(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies sku details.
	 *
	 * @param skuDetails the product sku
	 */
	public void verifySkuDetails(final String skuDetails) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(SKU_DETAILS_INPUT_CSS)).getAttribute("value"))
				.as("Sku Code validation failed")
				.isEqualTo(skuDetails);
	}

	/**
	 * Close Sku Details Editor.
	 */
	public void closeSkuDetailsEditor() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SKU_EDITOR_CLOSE_ICON_CSS)));
	}

	/**
	 * Close Sku Details Editor with check.
	 *
	 * @param skuCode sku code of the sku which is opened in an editor.
	 */
	public void closeSkuDetailsEditorWithCheck(final String skuCode) {
		closeSkuDetailsEditor();
		waitTillElementDisappears(By.cssSelector(String.format(TAB_CSS, skuCode)));
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	public void selectTab(final String tabName) {
		String cssSelector = String.format(TAB_CSS, tabName);
		resizeWindow(cssSelector);
		getWaitDriver().waitForElementToBeInteractable(cssSelector);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
	}

	/**
	 * Inputs enable dateTime.
	 *
	 * @param enableDateTime the enable dateTime.
	 */
	public void enterEnableDateTime(final String enableDateTime) {
		clearAndType(ENABLE_DATE_TIME_CSS, enableDateTime);
	}

	/**
	 * Inputs disable dateTime.
	 *
	 * @param disableDateTime the enable dateTime.
	 */
	public void enterDisableDateTime(final String disableDateTime) {
		clearAndType(DISABLE_DATE_TIME_CSS, disableDateTime);
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
	 * Selects shippable type.
	 *
	 * @param shippableType the shippable type.
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
}

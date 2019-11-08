package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.EditShortTextAttributeDialog;

/**
 * Add and Edit Product SKU Wizard.
 */
public class AddSkuWizard extends AbstractWizard {

	private static final String ADD_SKU_PARENT_CSS = "div[widget-id='Add SKU'][widget-type='Shell'] ";
	private static final String SKU_CODE_INPUT_CSS = ADD_SKU_PARENT_CSS + "div[widget-id='SKU Code'] > input";
	private static final String SKU_OPTIONS_COMBO_CSS = ADD_SKU_PARENT_CSS + "div[widget-id='%s'][widget-type='CCombo']";
	private static final String SHIPPABLE_TYPE_SHIPPABLE_BUTTON_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuOverview_Shippable'][seeable='true']";
	private static final String SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuOverview_DigitalAsset'][seeable='true']";
	private static final String NEXT_BUTTON = ADD_SKU_PARENT_CSS + "div[widget-id='Next >'][seeable='true']";
	private static final String FINISH_BUTTON = ADD_SKU_PARENT_CSS + "div[widget-id='Finish'][seeable='true']";
	private static final String SHIPPING_WEIGHT_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuShipping_ShippingWeight'] input";
	private static final String SHIPPING_WIDTH_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuShipping_ShippingWidth'] input";
	private static final String SHIPPING_LENGTH_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuShipping_ShippingLength'] input";
	private static final String SHIPPING_HEIGHT_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductEditorSingleSkuShipping_ShippingHeight'] input";
	private static final String TAX_CODE_COMBO_PARENT_CSS = ADD_SKU_PARENT_CSS + "div[widget-id='Tax Code'][widget-type='CCombo']";
	private static final String ENABLE_DATE_TIME_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductSkuCreateDialog_EnableDate'] textarea";
	private static final String DISABLE_DATE_TIME_CSS = ADD_SKU_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.ProductSkuCreateDialog_DisableDate'] textarea";
	private static final String CREATE_ATTRIBUTE_PARENT_CSS = "div[widget-id='Attributes'] ";
	private static final String CREATE_ATTRIBUTE_COLUMN_CSS = CREATE_ATTRIBUTE_PARENT_CSS + "div[column-id='%s']";
	private static final String ATTRIBUTE_NAME_COLUMN_NAME = "Name";
	private static final String EDIT_ATTRIBUTE_VALUE_BUTTON_CSS = ADD_SKU_PARENT_CSS + "div[widget-id='Edit Attribute Value...']";

	/**
	 * Inputs shipping weight.
	 *
	 * @param shippingWeight the shipping weight.
	 */
	public void enterShippingWeight(final String shippingWeight) {
		clearAndType(SHIPPING_WEIGHT_CSS, shippingWeight);
	}

	/**
	 * Inputs shipping width.
	 *
	 * @param shippingWidth the shipping width.
	 */
	public void enterShippingWidth(final String shippingWidth) {
		clearAndType(SHIPPING_WIDTH_CSS, shippingWidth);
	}

	/**
	 * Inputs shipping length.
	 *
	 * @param shippingLength the shipping length.
	 */
	public void enterShippingLength(final String shippingLength) {
		clearAndType(SHIPPING_LENGTH_CSS, shippingLength);
	}

	/**
	 * Inputs shipping height.
	 *
	 * @param shippingHeight the shipping height.
	 */
	public void enterShippingHeight(final String shippingHeight) {
		clearAndType(SHIPPING_HEIGHT_CSS, shippingHeight);
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
	 * @param disableDateTime the disable dateTime.
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
	 * Enter attribute short text.
	 *
	 * @param value         the value.
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeShortText(final String value, final String attributeName) {
		assertThat(selectItemInDialog(CREATE_ATTRIBUTE_PARENT_CSS, CREATE_ATTRIBUTE_COLUMN_CSS, attributeName,
				ATTRIBUTE_NAME_COLUMN_NAME))
				.as("Unable to find attribute - " + attributeName)
				.isTrue();
		EditShortTextAttributeDialog editShortTextAttributeDialog = clickEditAttributeButtonShortText();
		editShortTextAttributeDialog.enterShortTextValue(value);
		editShortTextAttributeDialog.clickOKButton();
	}

	/**
	 * Click edit attribute button for short text.
	 *
	 * @return the dialog.
	 */
	private EditShortTextAttributeDialog clickEditAttributeButtonShortText() {
		clickEditAttributeValueButton(EditShortTextAttributeDialog.PARENT_EDIT_SHORT_TEXT_CSS);
		return new EditShortTextAttributeDialog(getDriver());
	}


	/**
	 * Clicks the Edit Attribute Value button.
	 *
	 * @param pageObjectId page object id value.
	 */
	private void clickEditAttributeValueButton(final String pageObjectId) {
		clickButton(EDIT_ATTRIBUTE_VALUE_BUTTON_CSS, "Edit Attribute Value", pageObjectId);
	}

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AddSkuWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs sku code.
	 *
	 * @param skuCode the sku code.
	 */
	public void enterSkuCode(final String skuCode) {
		clearAndType(SKU_CODE_INPUT_CSS, skuCode);
	}

	/**
	 * Selects SKU Options in combo box.
	 *
	 * @param skuOption      for sku
	 * @param skuOptionValue for sku
	 */
	public void selectSkuOptions(final String skuOption, final String skuOptionValue) {
		assertThat(selectComboBoxItem(String.format(SKU_OPTIONS_COMBO_CSS, skuOption), skuOptionValue))
				.as("Unable to find SKU Options - " + skuOption)
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
			//TODO - will remove once button is enabled on click
			click(getDriver().findElement(By.cssSelector(SHIPPABLE_TYPE_DIGITAL_ASSET_BUTTON_CSS)));
		} else {
			assertThat("Shippable".equalsIgnoreCase(shippableType) || "Digital Asset".equalsIgnoreCase(shippableType))
					.as("Invalid shippable type entered - " + shippableType)
					.isTrue();
		}
	}

	/**
	 * Clicks Next.
	 */
	public void clickNextButton() {
		clickButton(NEXT_BUTTON, "Next");
	}

	/**
	 * Clicks Finish.
	 */
	public void clickFinish() {
		clickButton(FINISH_BUTTON, "Finish");
		waitTillElementDisappears(By.cssSelector(FINISH_BUTTON));
	}
}

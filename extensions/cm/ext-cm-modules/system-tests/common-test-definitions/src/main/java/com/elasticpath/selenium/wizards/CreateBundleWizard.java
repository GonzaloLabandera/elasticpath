package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddItemDialog;
import com.elasticpath.selenium.dialogs.AddShortTextAttributeDialog;
import com.elasticpath.selenium.dialogs.BasePriceEditorDialog;
import com.elasticpath.selenium.dialogs.EditDecimalValueAttributeDialog;
import com.elasticpath.selenium.dialogs.EditIntegerValueAttributeDialog;
import com.elasticpath.selenium.dialogs.EditShortTextMultiValueAttributeDialog;

/**
 * Create Bundle Dialog.
 */
public class CreateBundleWizard extends AbstractWizard {

	private static final String CREATE_BUNDLE_PARENT_CSS = "div[widget-id='Create Bundle'][widget-type='Shell'] ";
	private static final String PRODUCT_CODE_INPUT_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Product Code'] > input";
	private static final String PRODUCT_NAME_INPUT_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Product Name'] > input";
	private static final String BUNDLE_PRICING_COMBO_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Bundle Pricing']";
	private static final String PRODUCT_TYPE_COMBO_PARENT_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Product Type'][widget-type='CCombo']";
	private static final String BRAND_COMBO_PARENT_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Brand'][widget-type='CCombo']";
	private static final String SKU_CODE_INPUT_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='SKU Code'] > input";
	private static final String ADD_BASE_PRICE_BUTTON_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Add Base Price...']";
	private static final String EDIT_ATTRIBUTE_VALUE_BUTTON_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Edit Attribute Value...']";
	private static final String CLEAR_ATTRIBUTE_VALUE_BUTTON_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Clear Attribute Value']";
	private static final String ADD_ITEM_BUTTON_CSS = CREATE_BUNDLE_PARENT_CSS + "div[widget-id='Add Item']";
	private static final String CREATE_PRODUCT_ATTRIBUTE_PARENT_CSS = "div[widget-id='Attributes View'] ";
	private static final String CREATE_PRODUCT_ATTRIBUTE_COLUMN_CSS = CREATE_PRODUCT_ATTRIBUTE_PARENT_CSS + "div[column-id='%s']";
	private static final String ATTRIBUTE_NAME_COLUMNNAME = "Name";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateBundleWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs product code.
	 *
	 * @param productCode the product code.
	 */
	public void enterProductCode(final String productCode) {
		clearAndType(PRODUCT_CODE_INPUT_CSS, productCode);
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
	 * Inputs product name.
	 *
	 * @param productName the product name.
	 */
	public void enterProductName(final String productName) {
		clearAndType(PRODUCT_NAME_INPUT_CSS, productName);
	}

	/**
	 * Selects a product type in combo box.
	 *
	 * @param productType the product type.
	 */
	public void selectProductType(final String productType) {
		assertThat(selectComboBoxItem(PRODUCT_TYPE_COMBO_PARENT_CSS, productType))
				.as("Unable to find product type - " + productType)
				.isTrue();

	}

	/**
	 * Selects a tax code in combo box.
	 *
	 * @param taxCode the tax code.
	 */
	public void selectBundlePricing(final String taxCode) {
		assertThat(selectComboBoxItem(BUNDLE_PRICING_COMBO_CSS, taxCode))
				.as("Unable to find tax code - " + taxCode)
				.isTrue();
	}

	/**
	 * Selects a brand in combo box.
	 *
	 * @param brand the brand.
	 */
	public void selectBrand(final String brand) {
		assertThat(selectComboBoxItem(BRAND_COMBO_PARENT_CSS, brand))
				.as("Unable to find brand - " + brand)
				.isTrue();
	}

	/**
	 * Check store visible box.
	 */
	public void checkStoreVisibleBox() {
		click(getDriver().findElement(By.xpath("//div[contains(text(), 'Store Visible')]/../../following-sibling::div[1]/div")));
	}

	/**
	 * Enter attribute short text multi value.
	 *
	 * @param value         the value.
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeShortTextMultiValue(final String value, final String attributeName) {
		assertThat(selectItemInDialog(CREATE_PRODUCT_ATTRIBUTE_PARENT_CSS, CREATE_PRODUCT_ATTRIBUTE_COLUMN_CSS, attributeName,
				ATTRIBUTE_NAME_COLUMNNAME))
				.as("Unable to find attribute - " + attributeName)
				.isTrue();

		EditShortTextMultiValueAttributeDialog editShortTextMultiValueAttributeDialog = clickEditAttributeButtonShortTextMultiValue();
		AddShortTextAttributeDialog addShortTextAttributeDialog = editShortTextMultiValueAttributeDialog.clickAddValueButton();
		addShortTextAttributeDialog.enterShortTextValue(value);
		addShortTextAttributeDialog.clickOKButton();
		editShortTextMultiValueAttributeDialog.clickOKButton();
	}

	/**
	 * Enter attribute integer value.
	 *
	 * @param value         the value.
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeIntegerValue(final String value, final String attributeName) {
		assertThat(selectItemInDialog(CREATE_PRODUCT_ATTRIBUTE_PARENT_CSS, CREATE_PRODUCT_ATTRIBUTE_COLUMN_CSS, attributeName,
				ATTRIBUTE_NAME_COLUMNNAME))
				.as("Unable to find attribute - " + attributeName)
				.isTrue();

		EditIntegerValueAttributeDialog editIntegerValueAttributeDialog = clickEditAttributeButtonIntegerValue();
		editIntegerValueAttributeDialog.enterIntegerValue(value);
		editIntegerValueAttributeDialog.clickOKButton();
	}

	/**
	 * Enter attribute decimal value.
	 *
	 * @param value         the value.
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeDecimalValue(final String value, final String attributeName) {
		assertThat(selectItemInDialog(CREATE_PRODUCT_ATTRIBUTE_PARENT_CSS, CREATE_PRODUCT_ATTRIBUTE_COLUMN_CSS, attributeName,
				ATTRIBUTE_NAME_COLUMNNAME))
				.as("Unable to find attribute - " + attributeName)
				.isTrue();

		EditDecimalValueAttributeDialog editDecimalValueAttributeDialog = clickEditAttributeButtonDecimalValue();
		editDecimalValueAttributeDialog.enterDecimalValue(value);
		editDecimalValueAttributeDialog.clickOKButton();
	}

	/**
	 * Click edit attribute button for short text multi value.
	 *
	 * @return the dialog.
	 */
	public EditShortTextMultiValueAttributeDialog clickEditAttributeButtonShortTextMultiValue() {
		clickButton(EDIT_ATTRIBUTE_VALUE_BUTTON_CSS, "Edit Attribute Value");
		return new EditShortTextMultiValueAttributeDialog(getDriver());
	}

	/**
	 * Click edit attribute button for integer value.
	 *
	 * @return the dialog.
	 */
	public EditIntegerValueAttributeDialog clickEditAttributeButtonIntegerValue() {
		clickButton(EDIT_ATTRIBUTE_VALUE_BUTTON_CSS, "Edit Attribute Value");
		return new EditIntegerValueAttributeDialog(getDriver());
	}

	/**
	 * Click edit attribute button for decimal value.
	 *
	 * @return the dialog.
	 */
	public EditDecimalValueAttributeDialog clickEditAttributeButtonDecimalValue() {
		clickButton(EDIT_ATTRIBUTE_VALUE_BUTTON_CSS, "Edit Attribute Value");
		return new EditDecimalValueAttributeDialog(getDriver());
	}

	/**
	 * Click clear attribute button.
	 */
	public void clickClearAttributeButton() {
		clickButton(CLEAR_ATTRIBUTE_VALUE_BUTTON_CSS, "Clear Attribute Value");
	}

	/**
	 * Click add base price button.
	 *
	 * @return the dialog.
	 */
	public BasePriceEditorDialog clickAddBasePriceButton() {
		clickButton(ADD_BASE_PRICE_BUTTON_CSS, "Add Base Price");
		return new BasePriceEditorDialog(getDriver());
	}

	/**
	 * Click add item button.
	 *
	 * @return the dialog.
	 */
	public AddItemDialog clickAddItemButton() {
		clickButton(ADD_ITEM_BUTTON_CSS, "Add Item");
		return new AddItemDialog(getDriver());
	}

}

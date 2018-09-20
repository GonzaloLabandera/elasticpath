package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Add Item Dialog.
 */
public class AddItemDialog extends AbstractDialog {

	private static final String ADD_ITEM_PARENT_CSS = "div[widget-id='Add Item'][widget-type='Shell'] ";
	private static final String TYPE_COMBO_CSS = ADD_ITEM_PARENT_CSS + "div[widget-id='Type']";
	private static final String SELECT_PRODUCT_IMAGE_LINK_CSS = ADD_ITEM_PARENT_CSS + "div[widget-type='ImageHyperlink'] div[style*='.png']";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AddItemDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects type.
	 */
	public void selectType() {

		WebElement comboBox = getWaitDriver().waitForElementToBeClickable(By.cssSelector(TYPE_COMBO_CSS));
		click(comboBox);
		scrollDownWithDownArrowKey(comboBox, 0);
		click(comboBox);


		//TODO : selecting first item in list, once we have id's on parent div, then we will be able to select item based on parameter
//        String optionListXPath = "//div[contains(text(), '%s')]/../../div/div";
//        assertThat(selectComboBoxItem(comboBox, "Kobee Price List (CAD)", optionListXPath , priceList))
//                .as("Unable to find price list - " + priceList)
//                .isTrue();
	}

	/**
	 * Clicks Select Product link.
	 *
	 * @return the dialog.
	 */
	public SelectAProductDialog clickSelectProductImageLink() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SELECT_PRODUCT_IMAGE_LINK_CSS)));
		return new SelectAProductDialog(getDriver());
	}

	/**
	 * Clicks OK button.
	 */
	public void clickOKButton() {
		clickButton(OK_BUTTON_CSS, "OK");
		waitTillElementDisappears(By.cssSelector(ADD_ITEM_PARENT_CSS));
	}
}

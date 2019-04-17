package com.elasticpath.selenium.editor.product.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditMerchandisingAssociationsDialog;

/**
 * Merchandising Tab class.
 */
public class MerchandisingTab extends AbstractPageObject {

	private static final String MERCHANDISING_ITEM_PARENT_CSS = "div[widget-id='Product Merchandising'][widget-type='Table'][seeable='true'] ";
	private static final String MERCHANDISING_ITEM_COLUMN_CSS = MERCHANDISING_ITEM_PARENT_CSS + "div[column-id='%s']";
	private static final String MERCHANDISING_TAB_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String MERCHANDISING_TAB_SELECTED_CSS = "div[widget-id='%s'][seeable='true'][active-tab='true']";
	private static final String MERCHANDISING_ITEM_ADD_BUTTON = "div[widget-id='Add...'][widget-type='Button'][seeable='true']";
	private static final String MERCHANDISING_ITEM_EDIT_BUTTON = "div[widget-id='Edit...'][widget-type='Button'][seeable='true']";
	private static final String MERCHANDISING_ITEM_REMOVE_BUTTON = "div[widget-id='Remove...'][widget-type='Button'][seeable='true']";
	private static final String MERCHANDISING_CATALOG_TAB_CSS = "div[automation-id='productMerchandisingAssociation'][seeable='true'] "
			+ "div[widget-id='%s']";

	/**
	 * constructor.
	 *
	 * @param driver WebDriver which drives this webpage.
	 */
	public MerchandisingTab(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks 'Add Item' button.
	 *
	 * @return AddEditMerchandisingAssociationsDialog
	 */
	public AddEditMerchandisingAssociationsDialog clickAddMerchandisingAssociationsButton() {
		clickButton(MERCHANDISING_ITEM_ADD_BUTTON, "Add..");
		return new AddEditMerchandisingAssociationsDialog(getDriver());
	}

	/**
	 * Clicks Edit Item button.
	 *
	 * @return AddEditMerchandisingAssociationsDialog
	 */
	public AddEditMerchandisingAssociationsDialog clickEditMerchandisingAssociationsButton() {
		clickButton(MERCHANDISING_ITEM_EDIT_BUTTON, "Edit..");
		return new AddEditMerchandisingAssociationsDialog(getDriver());
	}

	/**
	 * Selects Added product row.
	 *
	 * @param productCode the Product Code
	 */
	public void verifySelectProductCode(final String productCode) {
		assertThat(selectItemInEditorPaneWithScrollBar(MERCHANDISING_ITEM_PARENT_CSS, MERCHANDISING_ITEM_COLUMN_CSS, productCode))
				.as("Unable to find product code - " + productCode)
				.isTrue();
	}

	/**
	 * Verify product is deleted.
	 *
	 * @param productCode the Product Code
	 */
	public void verifyProductCodeIsDeleted(final String productCode) {
		setWebDriverImplicitWait(1);
		assertThat(verifyItemIsNotInEditorPaneWithScrollBar(MERCHANDISING_ITEM_PARENT_CSS, MERCHANDISING_ITEM_COLUMN_CSS, productCode))
				.as("Product code - " + productCode + " should not be present as expected")
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks Remove Item button.
	 */
	public void clickRemoveMerchandisingAssociationsButton() {
		clickButton(MERCHANDISING_ITEM_REMOVE_BUTTON, "Remove");
	}

	/**
	 * Clicks on tab to select it.
	 *
	 * @param tabName the tab name.
	 */
	public void clickMerchandisingTab(final String tabName) {
		assertThat(getWaitDriver().waitForElementToBeNotStale(String.format(MERCHANDISING_TAB_CSS, tabName))).as(tabName + " is stale").isTrue();
		click(String.format(MERCHANDISING_TAB_CSS, tabName));
		getWaitDriver().waitForElementToBeInteractable(String.format(MERCHANDISING_TAB_SELECTED_CSS, tabName));
	}

	/**
	 * Clicks on the catalog tab.
	 *
	 * @param catalogName the catalog name
	 */
	public void selectCatalogTab(final String catalogName) {
		click(getDriver().findElement(By.cssSelector(String.format(MERCHANDISING_CATALOG_TAB_CSS, catalogName))));
	}

}

package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Virtual Catalog Dialog.
 */
public class CreateVirtualCatalogDialog extends AbstractDialog {

	private static final String CREATE_VIRTUAL_CATALOG_PARENT_CSS = "div[widget-id='Create Virtual Catalog'][widget-type='Shell'] ";
	private static final String CATALOG_CODE_INPUT_CSS = CREATE_VIRTUAL_CATALOG_PARENT_CSS + "div[widget-id='Catalog Code'] > input";
	private static final String CATALOG_NAME_INPUT_CSS = CREATE_VIRTUAL_CATALOG_PARENT_CSS + "div[widget-id='Catalog Name'] > input";
	private static final String DEFAULT_LANGUAGE_COMBO_CSS = CREATE_VIRTUAL_CATALOG_PARENT_CSS + "div[widget-id='Default "
			+ "Language'][widget-type='CCombo']";
	private static final String SAVE_BUTTON_CSS = CREATE_VIRTUAL_CATALOG_PARENT_CSS + "div[widget-id='Save'][style*='opacity: 1']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateVirtualCatalogDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs catalog code.
	 *
	 * @param catalogCode the code.
	 */
	public void enterCatalogCode(final String catalogCode) {
		clearAndType(CATALOG_CODE_INPUT_CSS, catalogCode);
	}

	/**
	 * Inputs catalog name.
	 *
	 * @param catalogName the catalog name.
	 */
	public void enterCatalogName(final String catalogName) {
		clearAndType(CATALOG_NAME_INPUT_CSS, catalogName);
	}

	/**
	 * Selects a default language in combo box.
	 *
	 * @param defaultLanguage the default language.
	 */
	public void selectDefaultLanguage(final String defaultLanguage) {
		assertThat(selectComboBoxItem(DEFAULT_LANGUAGE_COMBO_CSS, defaultLanguage))
				.as("Unable to find defaultLanguage - " + defaultLanguage)
				.isTrue();
	}

	/**
	 * Clicks save button.
	 */
	public void clickSaveButton() {
		clickButton(SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(CREATE_VIRTUAL_CATALOG_PARENT_CSS));
	}
}

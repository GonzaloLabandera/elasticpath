package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;

public class ConfigureFacetDialog extends AbstractDialog {

	private static final String CONFIGURE_FACET_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".ConfigureFacet']";
	private static final String LOCALE_DROPDOWN_CSS = CONFIGURE_FACET_PARENT_CSS + " div[widget-type='CCombo']";
	private static final String DISPLAY_NAME_FIELD_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages.DisplayName']"
			+ " input";
	private static final String SAVE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonSave']";

	/**
	 * constructor.
	 *
	 * @param driver the driver.
	 */
	public ConfigureFacetDialog(final WebDriver driver) {
		super(driver);
	}

	public void changeLocale(final String locale) {
		assertThat(selectComboBoxItem(LOCALE_DROPDOWN_CSS, locale))
				.as("Unable to find language - " + locale)
				.isTrue();
	}

	public void enterDisplayName(final String displayName) {
		clearAndType(DISPLAY_NAME_FIELD_CSS, displayName);
	}

	public void clickSave() {
		getWaitDriver().waitForButtonToBeEnabled(SAVE_BUTTON_CSS);
		clickButton(SAVE_BUTTON_CSS, "Save");
	}
}

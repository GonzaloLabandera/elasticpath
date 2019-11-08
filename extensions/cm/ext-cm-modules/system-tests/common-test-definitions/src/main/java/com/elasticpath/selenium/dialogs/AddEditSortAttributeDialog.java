package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.domainobjects.SortAttribute;

public class AddEditSortAttributeDialog extends AbstractDialog {

	public static final String ADMIN_STORES_MESSAGES_SORT = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages";
	public static final String ATTRIBUTE_KEY_INPUT_CSS = ADMIN_STORES_MESSAGES_SORT + ".SortAttributeKeyDialogLabel']";
	public static final String ATTRIBUTE_KEY_COMBO_CSS = ATTRIBUTE_KEY_INPUT_CSS + "[widget-type='CCombo']";
	public static final String LANGUAGE_COMBO_CSS = ADMIN_STORES_MESSAGES_SORT + ".SortLanguage'][widget-type='CCombo']";
	public static final String SORT_ORDER_COMBO_CSS = ADMIN_STORES_MESSAGES_SORT + ".SortOrder'][widget-type='CCombo']";
	public static final String DISPLAY_NAME_FIELD_CSS = ADMIN_STORES_MESSAGES_SORT + ".SortDisplayName'] input";
	private static final String SAVE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonSave']";

	/**
	 * constructor.
	 *
	 * @param driver the driver.
	 */
	public AddEditSortAttributeDialog(final WebDriver driver) {
		super(driver);
	}

	private String prefixSortGroup(final String sortGroup) {
		if ("Attribute".equals(sortGroup)) {
			return "(ATR) ";
		} else if ("Field".equals(sortGroup)) {
			return "(FLD) ";
		} else {
			return null;
		}
	}

	public void selectAttributeKey(final String sortGroup, final String attributeKey) {
		String value = prefixSortGroup(sortGroup) + attributeKey;
		assertThat(selectComboBoxItem(ATTRIBUTE_KEY_COMBO_CSS, value))
				.as("Unable to find attribute key - " + value)
				.isTrue();
	}

	public void selectLanguage(final String language) {
		assertThat(selectComboBoxItem(LANGUAGE_COMBO_CSS, language))
				.as("Unable to find language - " + language)
				.isTrue();
	}

	public void selectSortOrder(final String sortOrder) {
		assertThat(selectComboBoxItem(SORT_ORDER_COMBO_CSS, sortOrder))
				.as("Unable to find sort order - " + sortOrder)
				.isTrue();
	}

	public void enterDisplayName(final String displayName) {
		clearAndType(DISPLAY_NAME_FIELD_CSS, displayName);
	}

	public void setSortAttributeValues(final SortAttribute sortAttribute) {
		if (sortAttribute.getSortGroup() != null && sortAttribute.getAttributeKey() != null) {
			selectAttributeKey(sortAttribute.getSortGroup(), sortAttribute.getAttributeKey());
		}
		if (sortAttribute.getSortOrder() != null) {
			selectSortOrder(sortAttribute.getSortOrder());
		}
		if (sortAttribute.getLanguage() != null) {
			selectLanguage(sortAttribute.getLanguage());
		}
		enterDisplayName(sortAttribute.getDisplayName());
	}

	public void clickSave() {
		getWaitDriver().waitForButtonToBeEnabled(SAVE_BUTTON_CSS);
		clickButton(SAVE_BUTTON_CSS, "Save");
	}

}

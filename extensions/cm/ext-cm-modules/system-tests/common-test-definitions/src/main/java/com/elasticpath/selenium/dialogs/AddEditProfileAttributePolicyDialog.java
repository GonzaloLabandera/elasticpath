package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Add Edit Profile Attribute Policy Dialog.
 */
public class AddEditProfileAttributePolicyDialog extends AbstractDialog  {

	private static final String ADD_EDIT_VALUE_DIALOG_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores"
			+ ".AdminStoresMessages.Store_AttributePolicies_%sValue'][widget-type='Shell'] ";
	private static final String STORE_PROFILE_ATTRIBUTE_COMBO_CSS =
			"div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages.Store_AttributePolicies_Attribute'][widget-id='"
			+ "Attribute'][widget-type='CCombo']";
	private static final String STORE_PROFILE_POLICY_COMBO_CSS =
			"div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages.Store_AttributePolicies_Policy'][widget-id='"
			+ "Policy'][widget-type='CCombo']";
	private static final String SAVE_BUTTON_CSS_TEMPLATE = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages"
			+ ".AbstractEpDialog_ButtonSave'][seeable='true']";

	private final String storeProfileAttributeComboCss;
	private final String storeProfilePolicyComboCss;
	private final String saveButtonCss;

	/**
	 * Constructor for AddEditProfileAttributePolicyDialog.
	 * @param driver     WebDriver which drives this page
	 */
	public AddEditProfileAttributePolicyDialog(final WebDriver driver, final String action) {
		super(driver);
		final String parentCss = String.format(ADD_EDIT_VALUE_DIALOG_PARENT_CSS, action);
		storeProfileAttributeComboCss = parentCss + STORE_PROFILE_ATTRIBUTE_COMBO_CSS;
		storeProfilePolicyComboCss = parentCss + STORE_PROFILE_POLICY_COMBO_CSS;
		saveButtonCss = parentCss + SAVE_BUTTON_CSS_TEMPLATE;
	}

	/**
	 * Selects attribute.
	 *
	 * @param attribute the attribute.
	 */
	public void selectAttribute(final String attribute) {
		selectComboBoxItem(storeProfileAttributeComboCss, attribute);
	}

	/**
	 * Selects policy.
	 *
	 * @param policy the policy.
	 */
	public void selectPolicy(final String policy) {
		selectComboBoxItem(storeProfilePolicyComboCss, policy);
	}

	/**
	 * Clicks Save.
	 */
	public void clickSaveButton() {
		clickButton(SAVE_BUTTON_CSS_TEMPLATE, "Save");
		waitTillElementDisappears(By.cssSelector(saveButtonCss));
	}

}

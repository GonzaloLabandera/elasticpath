package com.elasticpath.selenium.editor.store.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditProfileAttributePolicyDialog;
import com.elasticpath.selenium.util.Constants;

/**
 * Support for the profile attribute policies tab.
 */
public class ProfileAttributePolicyTab extends AbstractPageObject {
	private static final String DIV_COLUMN_ID_S = "div[column-id='%s']";

	private static final String DIV_ADMIN_STORES_MESSAGES = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages";
	private static final String STORE_PROFILE_ATTRIBUTE_TABLE_PARENT_CSS = "div[widget-id='Store Customer Attribute Policies'][widget-type='Table'] ";
	private static final String STORE_PROFILE_ATTRIBUTE_ADD_BUTTON_CSS = DIV_ADMIN_STORES_MESSAGES
			+ ".Store_AttributePolicies_AddValue'][seeable='true']";
	private static final String STORE_PROFILE_ATTRIBUTE_EDIT_BUTTON_CSS = DIV_ADMIN_STORES_MESSAGES
			+ ".Store_AttributePolicies_EditValue'][seeable='true']";
	private static final String STORE_PROFILE_ATTRIBUTE_DELETE_BUTTON_CSS = DIV_ADMIN_STORES_MESSAGES
			+ ".Store_AttributePolicies_DeleteValue'][seeable='true']";
	private static final String OK_BUTTON_CSS = "div[widget-id='OK'][seeable='true']";
	private static final String CONFIRM_DELETE_DIALOG_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores"
			+ ".AdminStoresMessages.ConfirmDeleteAttributePolicyMsgBoxTitle'] ";

	/**
	 * constructor.
	 *
	 * @param driver WebDriver which drives this webpage.
	 */
	public ProfileAttributePolicyTab(final WebDriver driver) {
		super(driver);
	}


	/**
	 * Verify Profile Attribute Policy does not exist.
	 * @param attribute the attribute to verify
	 */
	public void verifyStoreProfileAttributePolicyDoesNotExist(final String attribute) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		assertThat(verifyItemIsNotInEditorPane(STORE_PROFILE_ATTRIBUTE_TABLE_PARENT_CSS, DIV_COLUMN_ID_S, attribute, null))
				.as("Able to find attribute row")
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verify Profile Attribute Policy exists.
	 * @param attribute the attribute to verify
	 */
	public void verifyStoreProfileAttributePolicyExists(final String attribute, final String policy) {
		assertThat(selectItemInEditorPane(STORE_PROFILE_ATTRIBUTE_TABLE_PARENT_CSS, DIV_COLUMN_ID_S, attribute, null))
				.as("Unable to find attribute row")
				.isTrue();

		assertThat(selectItemInEditorPane(STORE_PROFILE_ATTRIBUTE_TABLE_PARENT_CSS, DIV_COLUMN_ID_S, policy, null))
				.as("Unable to find policy row")
				.isTrue();
	}

	/**
	 * Select Profile Attribute Policy.
	 * @param attribute the attribute to select
	 */
	public void selectStoreProfileAttributePolicy(final String attribute) {
		assertThat(selectItemInEditorPane(STORE_PROFILE_ATTRIBUTE_TABLE_PARENT_CSS, DIV_COLUMN_ID_S, attribute, null))
				.as("Unable to find attribute row")
				.isTrue();
	}

	/**
	 * Launch the add policy dialog.
	 */
	public AddEditProfileAttributePolicyDialog clickAddPolicyButton(){
		clickButton(STORE_PROFILE_ATTRIBUTE_ADD_BUTTON_CSS, "Add Policy Button");
		return new AddEditProfileAttributePolicyDialog(getDriver(), "Add");
	}

	/**
	 * Launch the edit policy dialog.
	 */
	public AddEditProfileAttributePolicyDialog clickEditPolicyButton(){
		clickButton(STORE_PROFILE_ATTRIBUTE_EDIT_BUTTON_CSS, "Edit Policy Button");
		return new AddEditProfileAttributePolicyDialog(getDriver(), "Edit");
	}

	/**
	 * Click and confirm delete policy button.
	 */
	public void clickDeletePolicyButton(){
		clickButton(STORE_PROFILE_ATTRIBUTE_DELETE_BUTTON_CSS, "Delete Policy Button");
		clickButton(OK_BUTTON_CSS, "OK Button");
		waitTillElementDisappears(By.cssSelector(CONFIRM_DELETE_DIALOG_PARENT_CSS));
	}
}

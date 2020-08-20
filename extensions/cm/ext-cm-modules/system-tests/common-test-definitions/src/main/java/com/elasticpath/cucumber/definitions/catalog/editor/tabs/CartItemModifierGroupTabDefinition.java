/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddEditCartItemModifierFieldOptionDialog;
import com.elasticpath.selenium.dialogs.AddEditCartItemModifierGroupDialog;
import com.elasticpath.selenium.dialogs.AddEditCartItemModifierGroupFieldDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.domainobjects.CartItemModiferGroupField;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.containers.CartModifierGroupContainer;
import com.elasticpath.selenium.editor.catalog.CatalogEditor;
import com.elasticpath.selenium.editor.catalog.tabs.CartItemModifierGroupsTab;
import com.elasticpath.selenium.editor.catalog.tabs.ProductTypeTab;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.AddEditProductTypeWizard;

/**
 * Cart Item Modifier Group Tab Definition.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class CartItemModifierGroupTabDefinition {
	private static final String TYPE = "Type";
	private static final String MAX_SIZE = "Max size";
	private static final String REQUIRED = "Required";
	private static final String SHORT_TEXT = "Short Text";
	private static final String MULTI_SELECT_OPTION = "Multi Select Option";
	private static final String FIELD_SHORT_TEXT_TYPE = "Short Text";
	private static final String FIELD_EXPECTED_SHORT_TEXT_TYPE = "ShortText";
	private static final String FIELD_DECIMAL_TYPE = "Decimal";
	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
	private final CartItemModifierGroupsTab cartItemModifierGroupsTab;
	private final CatalogEditor catalogEditor;
	private final ProductTypeTab productTypeTab;
	private final CartItemModifierGroup cartItemModifierGroup;
	private final CartItemModiferGroupField cartItemModiferGroupField;
	private AddEditCartItemModifierGroupDialog addEditModifierGroupDialog;
	private AddEditCartItemModifierGroupFieldDialog fieldDialog;
	private AddEditCartItemModifierFieldOptionDialog addEditModifierFieldOptionDialog;
	private final CartModifierGroupContainer cartModifierGroupContainer;
	private final WebDriver driver;

	/**
	 * Constructor.
	 *
	 * @param cartItemModifierGroup     Cart Item Modifier Group object to be used in this session.
	 * @param cartItemModiferGroupField Cart Item Modifier Group Field to be used in this session.
	 * @param cartModifierGroupContainer Cart Item Modifier Group Field container.
	 */
	public CartItemModifierGroupTabDefinition(final CartItemModifierGroup cartItemModifierGroup, final CartItemModiferGroupField
			cartItemModiferGroupField, final CartModifierGroupContainer cartModifierGroupContainer) {
		driver = SeleniumDriverSetup.getDriver();
		this.catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		this.cartItemModifierGroupsTab = new CartItemModifierGroupsTab(driver);
		this.catalogEditor = new CatalogEditor(driver);
		this.productTypeTab = new ProductTypeTab(driver);
		this.cartItemModifierGroup = cartItemModifierGroup;
		this.cartItemModiferGroupField = cartItemModiferGroupField;
		this.cartModifierGroupContainer = cartModifierGroupContainer;
	}

	/**
	 * Create Cart Item Modifier Group.
	 *
	 * @param code      the group code
	 * @param groupName the group name
	 */
	@When("^I create a new modifier group with group code (.*) and group name (.*)$")
	public void createCartItemModifierGroup(final String code, final String groupName) {
		addEditModifierGroupDialog = cartItemModifierGroupsTab.clickAddGroupButton();
		this.cartItemModifierGroup.setGroupCode(code + Utility.getRandomUUID());
		this.cartItemModifierGroup.setGrouopName(groupName);
		addEditModifierGroupDialog.enterModifierGroupCode(this.cartItemModifierGroup.getGroupCode());
		addEditModifierGroupDialog.enterModifierGroupName(this.cartItemModifierGroup.getGrouopName());
		addEditModifierGroupDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
	}

	/**
	 * Create field with short text.
	 *
	 * @param fieldInfoList the field details.
	 */
	@And("^I add a new field to this group with following details$")
	public void createFieldWithShortText(final List<CartItemModiferGroupField> fieldInfoList) {
		for (CartItemModiferGroupField fieldInfo : fieldInfoList) {
			cartItemModifierGroupsTab.selectGroup(this.cartItemModifierGroup.getGroupCode());
			cartItemModifierGroupsTab.clickEditGroupButton();
			fieldDialog = addEditModifierGroupDialog.clickAddFieldButton();
			this.cartItemModiferGroupField.setFieldCode(fieldInfo.getFieldCode() + Utility.getRandomUUID());
			this.cartItemModiferGroupField.setFieldName(fieldInfo.getFieldName());
			fieldDialog.enterModifierFieldCode(this.cartItemModiferGroupField.getFieldCode());
			fieldDialog.enterDisplayName(this.cartItemModiferGroupField.getFieldName());
			fieldDialog.selectFieldType(fieldInfo.getFieldType());
			this.cartItemModiferGroupField.setFieldType(fieldInfo.getFieldType());
			switch (fieldInfo.getFieldType()) {
				case SHORT_TEXT:
					if (fieldInfo.getShortTextSize() != null) {

						createShortTextField(fieldInfo.getShortTextSize());
					}
					break;

				case MULTI_SELECT_OPTION:
					if (fieldInfo.getOptionValue() != null && fieldInfo.getOptionName() != null) {

						createMultiSelectOptionField(fieldInfo.getOptionValue(), fieldInfo.getOptionName());
					}
					break;

				default:
					fail("No field type given.");
					return;
			}
			addEditModifierGroupDialog.clickOkButton();
			catalogManagementActionToolbar.saveAll();
			catalogManagementActionToolbar.clickReloadActiveEditor();
		}
	}

	/**
	 * Create short text field.
	 *
	 * @param fieldSize the max field size.
	 */
	private void createShortTextField(final String fieldSize) {
		fieldDialog.enterMaxSize(fieldSize);
		fieldDialog.clickAddButton();
	}

	/**
	 * Create multi select option field.
	 *
	 * @param optionlValue option value.
	 * @param optionName   option name.
	 */
	private void createMultiSelectOptionField(final String optionlValue, final String optionName) {
		String optionalValueRandom = optionlValue + Utility.getRandomUUID();
		String optionNameRandom = optionName + Utility.getRandomUUID();
		fieldDialog.createOption(optionalValueRandom, optionNameRandom);
		this.cartItemModiferGroupField.setOptionValue(optionalValueRandom);
		this.cartItemModiferGroupField.setOptionName(optionNameRandom);
		fieldDialog.clickAddButton();

	}

	/**
	 * Edit option name.
	 */
	@When("^I edit and verify the edited option name$")
	public void editOptionName() {
		addEditModifierGroupDialog.selectField(this.cartItemModiferGroupField.getFieldCode());
		addEditModifierGroupDialog.clickEditFieldButton();
		fieldDialog.editAndVerifyOptionName(this.cartItemModiferGroupField.getOptionName());
		fieldDialog.clickOkButton();
		addEditModifierGroupDialog.clickOkButton();
	}

	/**
	 * verify newly created group exists.
	 */
	@Then("^(?:newly created|mentioned) group is in the list$")
	public void verifyNewlyCreatedGroupExists() {
		cartItemModifierGroupsTab.verifyGroup(this.cartItemModifierGroup.getGroupCode());
	}

	/**
	 * verify group with updated name exists.
	 */
	@Then("^updated group name is in the list$")
	public void verifyUpdatedGroupNameExists() {
		cartItemModifierGroupsTab.verifyGroup(this.addEditModifierGroupDialog.getGroupName());
	}

	/**
	 * Edit group name.
	 */
	@When("^I edit the group name$")
	public void editGroupName() {
		cartItemModifierGroupsTab.selectGroup(this.cartItemModifierGroup.getGroupCode());
		this.addEditModifierGroupDialog.setGroupName("EditGroupName" + "_" + Utility.getRandomUUID());
		cartItemModifierGroupsTab.clickEditGroupButton();
		addEditModifierGroupDialog.enterModifierGroupName(this.addEditModifierGroupDialog.getGroupName());
		addEditModifierGroupDialog.clickOkButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Edit field name.
	 */
	@When("^I edit and verify the edited field name$")
	public void editFieldName() {
		cartItemModifierGroupsTab.selectGroup(this.cartItemModifierGroup.getGroupCode());
		cartItemModifierGroupsTab.clickEditGroupButton();
		addEditModifierGroupDialog.selectField(this.cartItemModiferGroupField.getFieldName());
		addEditModifierGroupDialog.clickEditFieldButton();
		this.cartItemModiferGroupField.setFieldName("EditFieldName" + "_" + Utility.getRandomUUID());
		fieldDialog.enterDisplayName(this.cartItemModiferGroupField.getFieldName());
		fieldDialog.clickOkButton();
		addEditModifierGroupDialog.verifyField(this.cartItemModiferGroupField.getFieldName());
	}

	/**
	 * Delete new field.
	 */
	@When("^I delete the newly created field$")
	public void deleteNewField() {
		cartItemModifierGroupsTab.selectGroup(this.cartItemModifierGroup.getGroupCode());
		cartItemModifierGroupsTab.clickEditGroupButton();
		addEditModifierGroupDialog.selectField(this.cartItemModiferGroupField.getFieldName());
		addEditModifierGroupDialog.clickRemoveFieldButton();

	}

	/**
	 * Verify field deleted.
	 */
	@Then("^the field is deleted$")
	public void verifyFieldDeleted() {
		addEditModifierGroupDialog.verfiyFieldDelete(this.cartItemModiferGroupField.getFieldName());
		addEditModifierGroupDialog.clickOkButton();
	}

	/**
	 * Delete new group.
	 */
	@When("^I delete the newly created group$")
	public void deleteNewGroup() {
		cartItemModifierGroupsTab.selectTab("CartItemModifierGroups");
		cartItemModifierGroupsTab.selectGroup(this.cartItemModifierGroup.getGroupCode());
		cartItemModifierGroupsTab.clickRemoveGroupButton();
		new ConfirmDialog(driver).clickOKButton("CatalogCartItemModifierGroupsSection_RemoveDialog");
	}

	/**
	 * Verify newly created group is deleted.
	 */
	@Then("^the newly created group should be deleted$")
	public void verifyNewGroupDelete() {
		cartItemModifierGroupsTab.verifyGroupDelete(this.addEditModifierGroupDialog.getGroupName());
	}

	/**
	 * Verify cart item modifier group exists when creating a new product type.
	 */
	@Then("^this new cart item modifier group should be available as a selection when creating Product Type$")
	public void verifyGroupInProductTypeWizard() {
		catalogEditor.selectTab("ProductTypes");
		AddEditProductTypeWizard productTypeWizard = productTypeTab.clickAddProductTypeButton();
		productTypeWizard.selectAvailableGroup(this.cartItemModifierGroup.getGroupCode());
		productTypeWizard.clickCancel();
		catalogEditor.selectTab("CartItemModifierGroups");
	}

	/**
	 * Verify cart item modifier group does not exist when creating a new product type.
	 */
	@Then("^the deleted group should not appear when creating a new Product Type$")
	public void verifyGroupDeletedInProductType() {
		catalogEditor.selectTab("ProductTypes");
		AddEditProductTypeWizard productTypeWizard = productTypeTab.clickAddProductTypeButton();
		productTypeWizard.verifyGroupAbsence(this.cartItemModifierGroup.getGroupCode());
		productTypeWizard.clickCancel();
		catalogEditor.selectTab("CartItemModifierGroups");
	}

	/**
	 * Create Cart Item Modifier Group.
	 *
	 * @param cartItemModifierGroupCode the cart item modifier group code
	 * @param names                     values for the new localized names
	 */
	@When("^I add a new cart item modifier group with cart item modifier group code (.*) without saving with the following "
			+ "names$")
	public void addCartItemModifierGroupWithCodeAndLanguageList(final String cartItemModifierGroupCode, final Map<String, String> names) {
		final CartItemModifierGroup cartItemModifierGroup = new CartItemModifierGroup();
		this.addEditModifierGroupDialog = this.cartItemModifierGroupsTab.clickAddGroupButton();
		String codeRandom = cartItemModifierGroupCode + Utility.getRandomUUID();
		this.cartItemModifierGroup.setGroupCode(codeRandom);
		cartItemModifierGroup.setGroupCode(codeRandom);
		this.addEditModifierGroupDialog.enterModifierGroupCode(codeRandom);
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			this.fillCartItemModifierGroupValues(localizedName.getValue(), localizedName.getKey());
		}
		this.addEditModifierGroupDialog.clickAddButton();
		cartModifierGroupContainer.addCartItemModifierGroups(cartItemModifierGroup);
	}

	/**
	 * Create Cart Item Modifier Group Field.
	 *
	 * @param cartItemModifierGroupFieldCode the cart item modifier group field code
	 * @param field                          values for the values
	 */
	@When("^I add a new cart item modifier group field with cart item modifier group field code (.*) and the "
			+ "following names without saving$")
	public void addCartItemModifierGroupFieldWithCodeAndFieldValues(final String cartItemModifierGroupFieldCode, final Map<String, String> field) {
		CartItemModiferGroupField newField = new CartItemModiferGroupField();
		this.fieldDialog = this.addEditModifierGroupDialog.clickAddFieldButton();
		String codeRandom = cartItemModifierGroupFieldCode + Utility.getRandomUUID();
		this.fieldDialog.selectFieldType(field.get(TYPE));
		this.fieldDialog.enterModifierFieldCode(codeRandom);
		if (Boolean.valueOf(field.get(REQUIRED))) {
			this.fieldDialog.setRequiredStatus();
		}
		if (field.get(TYPE).equals(SHORT_TEXT)) {
			this.fieldDialog.enterMaxSize(field.get(MAX_SIZE));
		}
		setCartItemModifierGroupField(field, codeRandom, newField);
		if (!field.get(TYPE).equals(MULTI_SELECT_OPTION)) {
			this.fieldDialog.clickAddButton();
		}
		this.cartItemModifierGroup.addField(newField);
	}

	@When("^I open selected Cart item modifier group$")
	public void openSelectedGroup() {
		this.addEditModifierGroupDialog = this.cartItemModifierGroupsTab.clickEditGroupButton();
	}

	/**
	 * Create Cart Item Modifier Group Field Option.
	 *
	 * @param cartGroupFieldCode       the cart item modifier group field code
	 * @param cartGroupFieldOptionCode the cart item modifier group field option code
	 * @param names                    localized names
	 */
	@When("^I add a new option for field with field code (.*) with cart item modifier group field option value (.*) and the following names without "
			+ "saving$")
	public void addCartItemModifierGroupFieldOptionWithCodeAndOptionValues(final String cartGroupFieldCode,
																		   final String cartGroupFieldOptionCode,
																		   final Map<String,
																				   String> names) {
		CartItemModiferGroupField groupField = new CartItemModiferGroupField();
		addEditModifierFieldOptionDialog = this.fieldDialog.clickAddOptionButton();
		addEditModifierFieldOptionDialog.enterFieldOptionCode(cartGroupFieldOptionCode);
		String code = cartItemModifierGroup.getGroupFieldCodeByPartialCode(cartGroupFieldCode);
		for (int i = 0; i < cartItemModifierGroup.getCartGroupsFields().size(); i++) {
			if (cartItemModifierGroup.getCartGroupsFields().get(i).getFieldCode().equals(code)) {
				groupField = cartItemModifierGroup.getCartGroupsFields().get(i);
			}
		}
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			this.fieldDialog.selectLanguageForAddOptionField(localizedName.getKey());
			addEditModifierFieldOptionDialog.enterFieldOptionDisplayName(localizedName.getValue());
			groupField.setOption(cartGroupFieldOptionCode, localizedName.getKey(), localizedName.getValue());

		}
		addEditModifierFieldOptionDialog.clickAddButton();
	}

	/**
	 * Edits 5 last characters of cart item modifier group field names without saving.
	 *
	 * @param partialCode the cart item modifier group field partial or full code
	 */
	@And("^I edit 5 last characters of cart item modifier group field (.*) names with random characters without saving$")
	public void editCartItemModifierGroupFieldWithCodeAndFieldValues(final String partialCode, final List<String> languages) {
		String oldName;
		String editedName;
		String code = cartItemModifierGroup.getGroupFieldCodeByPartialCode(partialCode);
		CartItemModiferGroupField field = cartItemModifierGroup.getFieldByCode(code);
		for (String language : languages) {
			this.fieldDialog.selectLanguage(language);
			oldName = this.fieldDialog.getDisplayName();
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			this.fieldDialog.enterDisplayName(editedName);
			field.setName(language, editedName);
		}
	}

	/**
	 * Changes field type from Short text to Decimal or from Decimal to Short text with provided Max size depending on current field type.
	 *
	 * @param partialCode the cart item modifier group field partial or full code
	 * @param maxSize     max size parameter if new field type is set for
	 */
	@And("^I change field (.+) type as Short text -> Decimal or Decimal -> Short text with Max size (.+)$")
	public void editTypeShortTextDecimal(final String partialCode, final String maxSize) {
		String oldType;
		String code = cartItemModifierGroup.getGroupFieldCodeByPartialCode(partialCode);
		CartItemModiferGroupField field = cartItemModifierGroup.getFieldByCode(code);
		oldType = this.fieldDialog.getFieldType();
		if (FIELD_SHORT_TEXT_TYPE.equals(oldType)) {
			this.fieldDialog.selectFieldType(FIELD_DECIMAL_TYPE);
			field.setOldFieldType(FIELD_SHORT_TEXT_TYPE);
			field.setFieldType(FIELD_DECIMAL_TYPE);
			field.setShortTextSize("");
		} else if (FIELD_DECIMAL_TYPE.equals(oldType)) {
			this.fieldDialog.selectFieldType(FIELD_SHORT_TEXT_TYPE);
			this.fieldDialog.enterMaxSize(maxSize);
			field.setOldFieldType(FIELD_DECIMAL_TYPE);
			field.setFieldType(FIELD_EXPECTED_SHORT_TEXT_TYPE);
			field.setShortTextSize(maxSize);
		}
	}

	/**
	 * Opens dialog for editing cart item modifier group field for selected group and provided field.
	 *
	 * @param code cart item modifier group field code.
	 */
	@And("^I open cart item modifier group field (.*) for selected group$")
	public void openFieldForSelectedGroup(final String code) {
		this.addEditModifierGroupDialog = this.cartItemModifierGroupsTab.clickEditGroupButton();
		addEditModifierGroupDialog.selectField(code);
		this.fieldDialog = addEditModifierGroupDialog.clickEditFieldButton();
		}

	/**
	 * Closes cart item modifier group field and group dialogs keeping made changes.
	 */
	@And("^I close field and group dialogs keeping made changes$")
	public void closeFieldAndGroupDialogsKeepingChanges() {
			this.fieldDialog.clickOkButton();
		this.addEditModifierGroupDialog.clickOkButton();
	}

	/**
	 * Edits 5 last cart item modifier group field option names with random characters without saving.
	 *
	 * @param languages        list of languages for names
	 * @param partialFieldCode the cart item modifier group field full or partial code
	 * @param optionCode       the cart item modifier group field option code
	 */
	@And("^I edit 5 last characters of cart item modifier group field (.*) option (.*) names with random characters the following languages"
			+ " without saving$")
	public void editFieldOptionNamesRandomCharacters(
			final String partialFieldCode, final String optionCode, final List<String> languages) {
		String oldName;
		String editedName;
		String code = cartItemModifierGroup.getGroupFieldCodeByPartialCode(partialFieldCode);
		CartItemModiferGroupField field = cartItemModifierGroup.getFieldByCode(code);
		fieldDialog.selectOption(optionCode);
		addEditModifierFieldOptionDialog = this.fieldDialog.clickEditOptionButton();
		for (String language : languages) {
			this.fieldDialog.selectLanguageForEditOptionField(language);
			oldName = this.addEditModifierFieldOptionDialog.getFieldOptionDisplayName();
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			this.addEditModifierFieldOptionDialog.enterFieldOptionDisplayName(editedName);
			field.setOption(optionCode, language, editedName);
		}
		addEditModifierFieldOptionDialog.clickOkButton();
	}

	/**
	 * Apply changes made for newly Cart Item Modifier Group and refresh editor
	 */
	@And("^I save new Multi Select Option$")
	public void saveNewlyMultiSelectOption() {
		fieldDialog.clickAddButton();
		this.addEditModifierGroupDialog.clickOkButton();
	}


	/**
	 * Fill cart item modifier group values in opened dialog.
	 *
	 * @param name     new cart item modifier groupn name
	 * @param language localization
	 */
	private void fillCartItemModifierGroupValues(final String name, final String language) {
		this.addEditModifierGroupDialog.selectLanguage(language);
		this.addEditModifierGroupDialog.enterModifierGroupName(name);
		this.cartItemModifierGroup.setGrouopName(name);
		this.cartItemModifierGroup.setName(language, name);
	}

	/**
	 * Set cart item modifier group field from opened dialog
	 *
	 * @param field                          value from opened dialog
	 * @param cartItemModifierGroupFieldCode cart item modifier group field code
	 */
	private void setCartItemModifierGroupField(final Map<String, String> field, final String cartItemModifierGroupFieldCode,
											   final CartItemModiferGroupField newField) {
		for (Map.Entry<String, String> localizedName : field.entrySet()) {
			if (fieldDialog.selectLanguage(localizedName.getKey())) {
				this.fieldDialog.enterDisplayName(localizedName.getValue());
				newField.setName(localizedName.getKey(), localizedName.getValue());
			}
		}
		newField.setFieldCode(cartItemModifierGroupFieldCode);
		newField.setFieldType(field.get(TYPE));
		newField.setShortTextSize(field.get(MAX_SIZE));
		newField.setRequired(Boolean.valueOf(field.get(REQUIRED)));
	}
}

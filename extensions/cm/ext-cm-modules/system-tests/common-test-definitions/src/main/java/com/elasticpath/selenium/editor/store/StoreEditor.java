package com.elasticpath.selenium.editor.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.cucumber.definitions.StoresDefinition;
import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AbstractDialog;
import com.elasticpath.selenium.dialogs.ConfigureFacetDialog;
import com.elasticpath.selenium.dialogs.ConfigureRangeFacetDialog;
import com.elasticpath.selenium.dialogs.ErrorDialog;
import com.elasticpath.selenium.util.Constants;

/**
 * Store Editor.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class StoreEditor extends AbstractPageObject {
	private static final Logger LOGGER = LogManager.getLogger(AbstractPageObject.class);
	private static final String DIV_ADMIN_STORES_MESSAGES = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages";
	private static final String DIV_COLUMN_ID_S = "div[column-id='%s']";

	private static final String STORE_EDITOR_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores"
			+ ".AdminStoresMessages";
	private static final String STORE_CODE_VALUE_CSS = STORE_EDITOR_PARENT_CSS + ".StoreCode'] > input";
	private static final String STORE_NAME_VALUE_CSS = STORE_EDITOR_PARENT_CSS + ".StoreName'] > input";
	private static final String STORE_URL_VALUE_CSS = STORE_EDITOR_PARENT_CSS + ".StoreUrl'] > input";
	private static final String STORE_TIMEZONE_COMBO_CSS = STORE_EDITOR_PARENT_CSS + ".StoreTimeZone'][widget-id='Operational "
			+ "Timezone'][widget-type='CCombo']";
	private static final String STORE_REGISTERED_USER_ROLE_COMBO_CSS = STORE_EDITOR_PARENT_CSS
			+ ".RegisteredUserRole'][widget-id='Default Registered Shopper Role'][widget-type='CCombo']";
	private static final String STORE_UNREGISTERED_USER_ROLE_COMBO_CSS = STORE_EDITOR_PARENT_CSS
			+ ".SingleSessionUserRole'][widget-id='Default Single-Session Shopper Role'][widget-type='CCombo']";
	private static final String STORE_COUNTRY_COMBO_CSS = STORE_EDITOR_PARENT_CSS + ".StoreCountry'][widget-id='Store "
			+ "Country'][widget-type='CCombo']";
	private static final String STORE_SUB_COUNTRY_COMBO_CSS = STORE_EDITOR_PARENT_CSS + ".StoreSubCountry'][widget-id='Store "
			+ "Sub-Country'][widget-type='CCombo']";
	private static final String PAYMENT_PROVIDER_CONFIGURATION_TABLE = "div[widget-id='Store Payment Provider Configurations'][widget-type='Table']";
	private static final String PAYMENT_PROVIDER_CONFIGURATION_ROW = PAYMENT_PROVIDER_CONFIGURATION_TABLE + " div[row-id='%s']";
	private static final String PAYMENT_CONFIGURATION_CHECKBOX_CSS = PAYMENT_PROVIDER_CONFIGURATION_ROW + "+div div:nth-child(2)";
	private static final String TAB_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String STORE_RADIO_BUTTON_CSS = "div[widget-id='%s'][appearance-id='radio-button']";
	private static final String STORE_ADD_LANGUAGE_BUTTON_CSS = "div[widget-id='Language Selection']~ div div div[widget-id='Add']";
	private static final String STORE_DEFAULT_LANGUAGE_COMBO_CSS = "div[widget-id='Default Language'][widget-type='CCombo']";
	private static final String STORE_ADD_CURRENCY_BUTTON_CSS = "div[widget-id='Currency Selection']~ div div div[widget-id='Add']";
	private static final String STORE_DEFAULT_CURRENCY_COMBO_CSS = "div[widget-id='Default Currency'][widget-type='CCombo']";
	private static final String AVAILABLE_LANGUAGES_PARENT_CSS = "div[widget-id='Available Languages'][widget-type='Table'] ";
	private static final String AVAILABLE_LANGUAGES_COLUMN_CSS = AVAILABLE_LANGUAGES_PARENT_CSS + DIV_COLUMN_ID_S;
	private static final String AVAILABLE_CURRENCIES_PARENT_CSS = "div[widget-id='Available Currencies'][widget-type='Table'] ";
	private static final String AVAILABLE_CURRENCIES_COLUMN_CSS = AVAILABLE_CURRENCIES_PARENT_CSS + DIV_COLUMN_ID_S;
	private static final String SELECTED_LANGUAGES_PARENT_CSS = "div[widget-id='Selected Languages'][widget-type='Table'] ";
	private static final String SELECTED_LANGUAGES_COLUMN_CSS = SELECTED_LANGUAGES_PARENT_CSS + DIV_COLUMN_ID_S;
	private static final String WAREHOUSE_NAME_BUTTON_CSS = "div[widget-id='%s'][widget-type='Button'][seeable='true']";
	private static final String STORE_MARKETING_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages";
	private static final String STORE_MARKETING_EDIT_VALUE_BUTTON_CSS = STORE_MARKETING_PARENT_CSS + ".Store_Marketing_EditValue'][seeable='true']";
	private static final String STORE_MARKETING_SETTINGS_PARENT_CSS = "div[widget-type='Table'][widget-id='Store Marketing Table']";
	private static final String STORE_MARKETING_SETTINGS_COLUMN_CSS = STORE_MARKETING_SETTINGS_PARENT_CSS + " div[column-id='%s']";
	private static final String STORE_MARKETING_SETTINGS_EDIT_VALUE_INPUT_CSS = "div[widget-id='Edit Setting'] div[widget-id='Value'] textarea";
	private static final String STORE_MARKETING_SETTINGS_EDIT_VALUE_SAVE_BUTTON_CSS = STORE_MARKETING_PARENT_CSS
			+ ".AbstractEpDialog_ButtonSave']";
	private static final String STORE_SYSTEM_SETTINGS_PARENT_CSS = "div[widget-type='Table'][widget-id='Store System']";
	private static final String STORE_SYSTEM_SETTINGS_COLUMN_CSS = STORE_SYSTEM_SETTINGS_PARENT_CSS + " div[column-id='%s']";
	private static final String STORE_SYSTEM_SETTINGS_EDIT_VALUE_INPUT_CSS = "div[widget-id='Edit Setting'] div[widget-id='Value'] textarea";
	private static final String STORE_SYSTEM_SETTINGS_EDIT_VALUE_SAVE_BUTTON_CSS = STORE_MARKETING_PARENT_CSS + ".AbstractEpDialog_ButtonSave']";
	private static final String STORE_SYSTEM_EDIT_VALUE_BUTTON_CSS = STORE_MARKETING_PARENT_CSS + ".Store_Marketing_EditValue']";

	private static final String STORE_CHANGE_STATE_BUTTON_CSS = DIV_ADMIN_STORES_MESSAGES
			+ ".ChangeStoreState'][seeable='true']";
	private static final String STORE_STATE_OPTION_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String STORE_STATE_CHANGE_CONFIRMATION_BUTTON_CSS = "div[widget-id='Confirm Store State Change'] div[widget-id='OK']";
	private static final String STORE_ENABLED_DATA_POLICY_CSS = DIV_ADMIN_STORES_MESSAGES
			+ ".EnableDataPolicies'] +div";

	private static final String STORE_EDITOR_MAXIMIZE_BUTTON_CSS =
			"div[pane-location='editor-pane'] div[appearance-id='ctabfolder-button'][widget-id='Maximize'][seeable='true']";
	private static final String STORE_EDITOR_MINIMIZE_BUTTON_CSS =
			"div[pane-location='left-pane-inner'] div[appearance-id='ctabfolder-button'][widget-id='Minimize'][seeable='true']";

	private static final String FACET_CONFIGURATION_PAGE_CSS = "div[automation-id='facetConfigurationPage'] ";
	private static final String FACET_FILTER_TEXT_FIELD_CSS = FACET_CONFIGURATION_PAGE_CSS + "div[widget-type='Text'] input";
	private static final String FACET_TABLE_FACET_GROUP_COMBO_CSS = FACET_CONFIGURATION_PAGE_CSS + "div[widget-type='CCombo']";
	private static final String FACET_EDIT_BUTTON_CSS = FACET_CONFIGURATION_PAGE_CSS + "div[widget-id='Edit'";

	private static final String FACET_TABLE_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".StoreFacetConfiguration'] ";
	private static final String FACET_TABLE_ROW_CSS = FACET_TABLE_CSS + "div[row-id=\"%s\"] ";
	private static final String FACET_TABLE_ROW_COLUMN_VALUE_CSS = FACET_TABLE_ROW_CSS + "div[column-id='%s']";
	private static final String FACET_TABLE_SEARCHABLE_CHECKBOX_CSS = FACET_TABLE_ROW_CSS + "div:nth-child(8)";
	public static final String FACET_TABLE_FACETABLE_COLUMN_CSS = FACET_TABLE_ROW_CSS + "div[column-num='4']";
	private static final String FACET_TABLE_FACETABLE_DROPDOWN_CSS = FACET_TABLE_CSS + "div[appearance-id='ccombo'][seeable='true']";
	private static final String FACET_TABLE_FACETABLE_DROPDOWN_OPTIONS_CSS = "div[appearance-id='ccombo-list-popup'] ";
	private static final String FACET_TABLE_FACETABLE_DROPDOWN_OPTION_VALUES_CSS = FACET_TABLE_FACETABLE_DROPDOWN_OPTIONS_CSS + "div[row-id='%s"
			+ "'][widget-type='row']";
	private static final String FACET_TABLE_FACET_GROUP_COLUMN_VALUE_CSS = FACET_TABLE_CSS + "div[column-num='1']:not([column-id=''])";
	private static final String STORE_TAB_LABEL = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages.%s']"
			+ "[appearance-id='label-wrapper'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public StoreEditor(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies if store editor is visible.
	 */
	public void verifyStoreEditor() {
		assertThat(getWaitDriver().waitForElementToBeInteractable(STORE_CODE_VALUE_CSS))
				.as("Store Editor is not visible")
				.isTrue();
	}

	/**
	 * Maximizes store editor.
	 */
	public void maximizeStoreEditor() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		if (isElementPresent(By.cssSelector(STORE_EDITOR_MAXIMIZE_BUTTON_CSS))) {
			click(STORE_EDITOR_MAXIMIZE_BUTTON_CSS);
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Minimizes store editor.
	 */
	public void minimizeStoreEditor() {
		click(STORE_EDITOR_MINIMIZE_BUTTON_CSS);
	}

	/**
	 * Verify Store Code.
	 *
	 * @param storeCode the Store Code.
	 */
	public void verifyStoreCode(final String storeCode) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(STORE_CODE_VALUE_CSS)).getAttribute("value"))
				.as("Store code does not match in editor")
				.isEqualTo(storeCode);
	}

	/**
	 * Enter store code.
	 *
	 * @param storeCode String
	 */
	public void enterStoreCode(final String storeCode) {
		clearAndType(STORE_CODE_VALUE_CSS, storeCode);
	}

	/**
	 * Enter store name.
	 *
	 * @param storeName String
	 */
	public void enterStoreName(final String storeName) {
		clearAndType(STORE_NAME_VALUE_CSS, storeName);
	}

	/**
	 * Enter store url.
	 *
	 * @param storeUrl String
	 */
	public void enterStoreUrl(final String storeUrl) {
		clearAndType(STORE_URL_VALUE_CSS, storeUrl);
	}

	/**
	 * Select store timezone.
	 *
	 * @param timezone String
	 */
	public void selectStoreTimezone(final String timezone) {
		assertThat(selectComboBoxItem(STORE_TIMEZONE_COMBO_CSS, timezone))
				.as("Unable to find timezone - " + timezone)
				.isTrue();
	}

	/**
	 * Select registered user role.
	 *
	 * @param role String
	 */
	public void selectRegisteredUser(final String role) {
		assertThat(selectComboBoxItem(STORE_REGISTERED_USER_ROLE_COMBO_CSS, role))
				.as("Unable to find shopper role - " + role)
				.isTrue();
	}

	/**
	 * Select unregistered user role.
	 *
	 * @param role String
	 */
	public void selectUnregisteredUser(final String role) {
		assertThat(selectComboBoxItem(STORE_UNREGISTERED_USER_ROLE_COMBO_CSS, role))
				.as("Unable to find shopper role - " + role)
				.isTrue();
	}


	/**
	 * Select store country.
	 *
	 * @param country String
	 */
	public void selectStoreCountry(final String country) {
		assertThat(selectComboBoxItem(STORE_COUNTRY_COMBO_CSS, country))
				.as("Unable to find country - " + country)
				.isTrue();
	}

	/**
	 * Select store sub country.
	 *
	 * @param subCountry String
	 */
	public void selectStoreSubCountry(final String subCountry) {
		assertThat(selectComboBoxItem(STORE_SUB_COUNTRY_COMBO_CSS, subCountry))
				.as("Unable to find sub country - " + subCountry)
				.isTrue();
	}

	/**
	 * Select store payment configuration.
	 *
	 * @param paymentConfiguration String
	 */
	public void selectStorePaymentConfiguration(final String paymentConfiguration) {
		if (!("").equals(paymentConfiguration) || !paymentConfiguration.isEmpty()) {
			assertThat(selectItemInEditorPaneWithScrollBar(PAYMENT_PROVIDER_CONFIGURATION_TABLE, "div[column-id='%s']",
					paymentConfiguration, "Configuration Name"))
					.as("Unable to find payment configuration - " + paymentConfiguration)
					.isTrue();

			clickCheckBox(String.format(PAYMENT_CONFIGURATION_CHECKBOX_CSS, paymentConfiguration));
			LOGGER.debug("Selected Payment configuration for store - " + paymentConfiguration);
		}
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
	}

	/**
	 * verifies inactive payment configuration not available for store.
	 * @param inactivePaymentConfiguration String
	 */
	public void verifyPaymentConfigurationNotExist(final String inactivePaymentConfiguration) {
		maximizeStoreEditor();
		assertThat(isElementPresent(By.cssSelector(String.format(PAYMENT_CONFIGURATION_CHECKBOX_CSS, inactivePaymentConfiguration))))
				.as("Inactive payment configuration should not be available for store - " + inactivePaymentConfiguration)
				.isFalse();
	}

	/**
	 * Clicks to select tab.
	 *
	 * @param tabName the tab name.
	 */
	public void clickTab(final String tabName) {
		String cssSelector = String.format(TAB_CSS, tabName);
		resizeWindow(cssSelector);
		click(getDriver().findElement(By.cssSelector(cssSelector)));
		switch (tabName) {
			case "Summary":
				verifyTabIsSelected(tabName, "StoreEditor_SummaryPage_Title");
				break;
			case "Localization":
				verifyTabIsSelected(tabName, "StoreEditor_Localization_Title");
				break;
			case "Catalog":
				verifyTabIsSelected(tabName, "StoreCatalog");
				break;
			case "Warehouse":
				verifyTabIsSelected(tabName, "StoreWarehouse");
				break;
			case "Taxes":
				verifyTabIsSelected(tabName, "StoreEditor_TaxesPage_Title");
				break;
			case "Payments":
				verifyTabIsSelected(tabName, "StorePaymentProviderConfigurations");
				break;
			case "Shared Customer Accounts ":
				verifyTabIsSelected(tabName, "SharedCustomerAccounts");
				break;
			case "Marketing":
				verifyTabIsSelected(tabName, "StoreMarketingSettings");
				break;
			case "System":
				verifyTabIsSelected(tabName, "StoreSystemSettings");
				break;
			case "Profile Attribute Policies":
				verifyTabIsSelected(tabName, "StoreProfileAttributePolicies");
				break;
			case "Facets":
				verifyTabIsSelected(tabName, "StoreFacetConfiguration");
				break;
			case "Sorting":
				verifyTabIsSelected(tabName, "StoreSortAttributeConfiguration");
				break;
			default:
				fail("Store editor tab '" + tabName + "' does not exist");
				break;
		}
	}

	/**
	 * Verifies tab selection.
	 *
	 * @param tabName  the tab name
	 * @param tabLabel the tab label
	 */
	public void verifyTabIsSelected(final String tabName, final String tabLabel) {
		int counter = 0;
		String labelCss = String.format(STORE_TAB_LABEL, tabLabel);
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		while (!isElementPresent(By.cssSelector(labelCss)) && counter < Constants.RETRY_COUNTER_3) {
			sleep(Constants.SLEEP_THREE_SECONDS_IN_MILLIS);
			if (isElementPresent(By.cssSelector(labelCss))) {
				break;
			}
			click(getDriver().findElement(By.cssSelector(String.format(TAB_CSS, tabName))));
			counter++;
		}
		assertThat(isElementPresent(By.cssSelector(labelCss)))
				.as("Failed to select store editor tab: " + tabLabel)
				.isTrue();

		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Select radio button by displayed name.
	 *
	 * @param radioButtonName String
	 */
	public void selectRadioButton(final String radioButtonName) {
		clickButton(String.format(STORE_RADIO_BUTTON_CSS, radioButtonName), radioButtonName);
	}

	/**
	 * Add store language.
	 *
	 * @param language String
	 */
	public void selectAndAddAvailableLanguage(final String language) {
		assertThat(selectItemInDialog(AVAILABLE_LANGUAGES_PARENT_CSS, AVAILABLE_LANGUAGES_COLUMN_CSS, language, ""))
				.as("Unable to find language - " + language)
				.isTrue();
		clickButton(STORE_ADD_LANGUAGE_BUTTON_CSS, "Add Language Button");
	}

	/**
	 * Set default store language.
	 *
	 * @param language String
	 */
	public void selectDefaultLanguage(final String language) {
		assertThat(selectComboBoxItem(STORE_DEFAULT_LANGUAGE_COMBO_CSS, language))
				.as("Unable to find language - " + language)
				.isTrue();
	}

	/**
	 * Add currency to store.
	 *
	 * @param currency String
	 */
	public void selectAndAddAvailableCurrency(final String currency) {
		assertThat(selectItemInDialog(AVAILABLE_CURRENCIES_PARENT_CSS, AVAILABLE_CURRENCIES_COLUMN_CSS, currency, ""))
				.as("Unable to find currency - " + currency)
				.isTrue();
		clickButton(STORE_ADD_CURRENCY_BUTTON_CSS, "Add Currency Button");
	}

	/**
	 * Set default store currency.
	 *
	 * @param currency String
	 */
	public void selectDefaultCurrency(final String currency) {
		assertThat(selectComboBoxItem(STORE_DEFAULT_CURRENCY_COMBO_CSS, currency))
				.as("Unable to find currency - " + currency)
				.isTrue();
	}

	/**
	 * Edit Store marketing setting.
	 *
	 * @param settingName String setting name
	 * @param newValue    String new value for setting
	 */
	public void editStoreMarketingSetting(final String settingName, final String newValue) {
		assertThat(selectItemInDialog(STORE_MARKETING_SETTINGS_PARENT_CSS, STORE_MARKETING_SETTINGS_COLUMN_CSS, settingName, "Name"))
				.as("Unable to find setting - " + settingName)
				.isTrue();
		clickButton(STORE_MARKETING_EDIT_VALUE_BUTTON_CSS, "Edit Value Button");
		clearAndType(STORE_MARKETING_SETTINGS_EDIT_VALUE_INPUT_CSS, newValue);
		clickButton(STORE_MARKETING_SETTINGS_EDIT_VALUE_SAVE_BUTTON_CSS, "Edit Value Save Button");
	}

	/**
	 * Verify selected language exists.
	 *
	 * @param expLanguage String
	 */
	public void verifySelectedLanguage(final String expLanguage) {
		getWaitDriver().waitForElementToBeNotStale(String.format(SELECTED_LANGUAGES_COLUMN_CSS, expLanguage));
		assertThat(selectItemInDialog(SELECTED_LANGUAGES_PARENT_CSS, SELECTED_LANGUAGES_COLUMN_CSS, expLanguage, ""))
				.as("Unable to find selected language - " + expLanguage)
				.isTrue();
	}

	/**
	 * Edit Store system setting.
	 *
	 * @param settingName String setting name
	 * @param newValue    String new value for setting
	 */
	public void editStoreSystemSetting(final String settingName, final String newValue) {
		assertThat(selectItemInDialog(STORE_SYSTEM_SETTINGS_PARENT_CSS, STORE_SYSTEM_SETTINGS_COLUMN_CSS, settingName, "Name"))
				.as("Unable to find setting - " + settingName)
				.isTrue();
		clickButton(STORE_SYSTEM_EDIT_VALUE_BUTTON_CSS, "Edit Value Button");
		clearAndType(STORE_SYSTEM_SETTINGS_EDIT_VALUE_INPUT_CSS, newValue);
		clickButton(STORE_SYSTEM_SETTINGS_EDIT_VALUE_SAVE_BUTTON_CSS, "Edit Value Save Button");
	}

	/**
	 * Change a stores State and confirm.
	 *
	 * @param newStoreState String
	 */
	public void changeStoreStateAndConfirm(final String newStoreState) {
		changeStoreState(newStoreState);
		clickButton(STORE_STATE_CHANGE_CONFIRMATION_BUTTON_CSS, "Store State Change OK Button");
	}

	/**
	 * Verifies warehouseName.
	 *
	 * @param warehouseName is present
	 */
	public void verifyWarehouseName(final String warehouseName) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(WAREHOUSE_NAME_BUTTON_CSS, warehouseName))).isDisplayed())
				.as("Expected warehouse name is not present.")
				.isTrue();
	}

	/**
	 * Verify Data Policies is enabled/checked.
	 */
	public void verifyDataPolicyEnabled() {
		assertThat(isSelected(STORE_ENABLED_DATA_POLICY_CSS))
				.as("Data Policies is not enabled.")
				.isTrue();
	}

	public void changeStoreState(final String newStoreState) {
		clickButton(STORE_CHANGE_STATE_BUTTON_CSS, "Store Change State Button");
		clickButton(String.format(STORE_STATE_OPTION_CSS, newStoreState), newStoreState + " Button");
	}

	/**
	 * Filters the facets in facet table.
	 *
	 * @param facetName name of facet
	 */
	public void filterFacetTableByName(final String facetName) {
		clearAndType(FACET_FILTER_TEXT_FIELD_CSS, facetName);
		getWaitDriver().waitForElementToBeVisible((By.cssSelector(String.format(FACET_TABLE_ROW_CSS, facetName))));
	}

	public void filterFacetTableByGroup(final String facetGroup) {
		assertThat(selectComboBoxItem(FACET_TABLE_FACET_GROUP_COMBO_CSS, facetGroup))
				.as("Unable to find facet group - " + facetGroup)
				.isTrue();
	}

	/**
	 * Click searchable checkbox associated with a facet
	 *
	 * @param facetName
	 */
	public void toggleFacetSearcable(final String facetName) {
		clickCheckBox(String.format(FACET_TABLE_SEARCHABLE_CHECKBOX_CSS, facetName));
	}

	public void clickFacetFromTable(final String facetName) {
		click(By.cssSelector(String.format(FACET_TABLE_ROW_CSS, facetName)));
	}

	public void selectFacetableOption(final String facetName, final String option) {
		getWaitDriver().waitForElementToBeVisible((By.cssSelector(String.format(FACET_TABLE_ROW_CSS, facetName))));
		click(String.format(FACET_TABLE_FACETABLE_COLUMN_CSS, facetName));
		click(FACET_TABLE_FACETABLE_DROPDOWN_CSS);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(FACET_TABLE_FACETABLE_DROPDOWN_OPTIONS_CSS));
		click(String.format(FACET_TABLE_FACETABLE_DROPDOWN_OPTION_VALUES_CSS, option));
	}

	public void verifyFacetExistsInTable(final String facetName, final Map<String, String> facetInfoMap) {
		getWaitDriver().waitForElementToBeVisibleAndToBeNotStale(String.format(FACET_TABLE_ROW_CSS, facetName));

		for (Map.Entry<String, String> entry : facetInfoMap.entrySet()) {
			assertThat(getDriver().findElement(By.cssSelector(String.format(FACET_TABLE_ROW_COLUMN_VALUE_CSS, facetName, entry.getValue())))
					.isDisplayed())
					.as("Facet value is not as expected")
					.isTrue();
		}
	}

	public AbstractDialog clickEdiFacetButton(final String facetType) {
		click(By.cssSelector(FACET_EDIT_BUTTON_CSS));
		if (facetType.equals(StoresDefinition.FACET)) {
			return new ConfigureFacetDialog(getDriver());
		} else if (facetType.equals(StoresDefinition.RANGE_FACET)) {
			return new ConfigureRangeFacetDialog(getDriver());
		}

		assertThat(false)
				.as("Unexpected facet option specified")
				.isTrue();

		return null;
	}

	public List<String> getVisibleFacetGroups() {
		List<WebElement> facetGroupElements = getDriver().findElements(By.cssSelector(FACET_TABLE_FACET_GROUP_COLUMN_VALUE_CSS));
		return facetGroupElements.stream().map(WebElement::getText).collect(Collectors.toList());
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param expErrorMessage String
	 */
	public void verifyErrorMessageDisplayed(final String expErrorMessage) {
		new ErrorDialog(getDriver()).verifyErrorMessage(expErrorMessage);
	}
}
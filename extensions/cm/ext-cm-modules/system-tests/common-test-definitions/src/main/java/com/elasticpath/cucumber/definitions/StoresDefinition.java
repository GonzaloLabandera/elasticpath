package com.elasticpath.cucumber.definitions;

import static com.elasticpath.selenium.framework.util.SeleniumDriverSetup.getDriver;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddEditProfileAttributePolicyDialog;
import com.elasticpath.selenium.dialogs.AddEditSortAttributeDialog;
import com.elasticpath.selenium.dialogs.ConfigureFacetDialog;
import com.elasticpath.selenium.dialogs.ConfigureRangeFacetDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.domainobjects.Catalog;
import com.elasticpath.selenium.domainobjects.SortAttribute;
import com.elasticpath.selenium.domainobjects.Store;
import com.elasticpath.selenium.editor.store.StoreEditor;
import com.elasticpath.selenium.editor.store.tabs.ProfileAttributePolicyTab;
import com.elasticpath.selenium.editor.store.tabs.SortingTab;
import com.elasticpath.selenium.resultspane.StoresResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;
import com.elasticpath.selenium.util.Utility;

/**
 * System Configuration step definitions.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
public class StoresDefinition {
	private static final String PROFILE_ATTRIBUTE_POLICIES = "Profile Attribute Policies";

	public static final String FACET = "Facet";
	public static final String RANGE_FACET = "Range Facet";

	private final Store store;
	private final Catalog catalog;
	private final ConfigurationActionToolbar configurationActionToolbar;
	private final ProfileAttributePolicyTab profileAttributePolicyTab;
	private StoresResultPane storesResultPane;
	private StoreEditor storeEditor;
	private String storeName;
	private String storeCode;
	private String language;
	private String currency;
	private String newLanguage;
	private AddEditProfileAttributePolicyDialog addEditProfileAttributePolicyDialog;
	private String facetName;
	private String initialFacetOption;
	private SortAttribute sortAttribute;
	private final SortingTab sortingTab;

	/**
	 * Constructor.
	 */
	public StoresDefinition(final Store store, final Catalog catalog) {
		final WebDriver driver = SetUp.getDriver();

		configurationActionToolbar = new ConfigurationActionToolbar(driver);
		this.storeCode = "";
		this.store = store;
		this.catalog = catalog;
		this.profileAttributePolicyTab = new ProfileAttributePolicyTab(driver);
		this.sortingTab = new SortingTab(driver);
	}

	/**
	 * Clicks on Stores.
	 */
	@When("^I go to Stores$")
	public void clickStores() {
		storesResultPane = configurationActionToolbar.clickStores();
	}

	/**
	 * Edit store.
	 *
	 * @param storeCode the store code
	 */
	@When("^I edit store (.+) in editor$")
	public void editStore(final String storeCode) {
		storeEditor = storesResultPane.editStore(storeCode);
	}

	/**
	 * Verifies store code in editor.
	 *
	 * @param storeCode the store code
	 */
	@Then("^The store code (.+) should match in the store editor$")
	public void verifyStoreCode(final String storeCode) {
		storeEditor.verifyStoreCode(storeCode);
	}

	@When("^I click create store button")
	public void clickCreateStoreButton() {
		storeEditor = storesResultPane.clickCreateStoreButton();
	}

	/**
	 * Creates a new store.
	 *
	 * @param storeMap The store map
	 */
	@When("^I create store with following values$")
	public void fillInStoreSummary(final Map<String, String> storeMap) {
		storeEditor = storesResultPane.clickCreateStoreButton();
		storeEditor.maximizeStoreEditor();
		this.storeCode = "st" + Utility.getRandomUUID();
		store.setCode(this.storeCode);
		this.storeName = storeMap.get("store name") + this.storeCode;
		store.setName(this.storeName);
		this.language = storeMap.get("language");
		this.currency = storeMap.get("currency");
		storeEditor.enterStoreCode(this.storeCode);
		storeEditor.enterStoreName(this.storeName);
		storeEditor.enterStoreUrl("http://" + this.storeCode + ".elasticpath.com");
		storeEditor.selectStoreTimezone(storeMap.get("timezone"));
		storeEditor.selectStoreCountry(storeMap.get("store country"));
		storeEditor.selectStoreSubCountry(storeMap.get("store sub country"));
		storeEditor.clickTab("System");
		storeEditor.editStoreSystemSetting("Store HTML Encoding", "UTF-8");
		storeEditor.clickTab("Payments");
		storeEditor.selectStorePrimaryPaymentGateway(storeMap.get("payment gateway"));
		configurationActionToolbar.clickSaveButton();
		storeEditor.clickTab("Warehouse");
		storeEditor.selectRadioButton(storeMap.get("warehouse"));
		storeEditor.clickTab("Catalog");
		String catalogName = Optional.ofNullable(storeMap.get("catalog"))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> catalog.getCatalogName());
		storeEditor.selectRadioButton(catalogName);
		storeEditor.clickTab("Localization");
		storeEditor.selectAndAddAvailableLanguage(this.language);
		storeEditor.selectDefaultLanguage(this.language);
		storeEditor.selectAndAddAvailableCurrency(this.currency);
		storeEditor.selectDefaultCurrency(this.currency);
		storeEditor.clickTab("Marketing");
		storeEditor.editStoreMarketingSetting("Store Admin Email Address", "testadmin@example.com");
		storeEditor.editStoreMarketingSetting("Store From Email (Friendly Name)", "Test Admin");
		storeEditor.editStoreMarketingSetting("Store From Email (Sender Address)", "testadmin@example.com");
		configurationActionToolbar.clickSaveButton();
		storeEditor.minimizeStoreEditor();
	}

	/**
	 * Verify store exists in list.
	 */
	@Then("^the store should exist in the list$")
	public void verifyNewStoreExistsInList() {
		storesResultPane.verifyStoreExists(this.storeName);
	}

	/**
	 * Open the store.
	 */
	@When("^I open the store$")
	public void openStore() {
		storeEditor = storesResultPane.editStore(this.storeCode);
	}

	/**
	 * Edit store name.
	 *
	 * @param newStoreName String
	 */
	@When("^I edit the store name to (.+)$")
	public void editStoreName(final String newStoreName) {
		storeEditor = storesResultPane.editStore(this.storeCode);
		storeEditor.clickTab("Summary");
		this.storeName = newStoreName + this.storeCode;
		storeEditor.enterStoreName(this.storeName);
		configurationActionToolbar.clickSaveButton();
	}

	/**
	 * Adds a new language to the selected languages table.
	 *
	 * @param newLanguage String
	 */
	@When("^I add new language (.+) to the store$")
	public void addLanguageToStore(final String newLanguage) {
		storeEditor = storesResultPane.editStore(this.storeCode);
		storeEditor.clickTab("Localization");
		storeEditor.selectAndAddAvailableLanguage(newLanguage);
		configurationActionToolbar.clickSaveButton();
		this.newLanguage = newLanguage;
	}

	/**
	 * Verifies new language added to selected language table.
	 */
	@Then("^I should see the new language in the selected languages list$")
	public void verifyNewLanguageSelected() {
		storeEditor.verifySelectedLanguage(this.newLanguage);
	}

	/**
	 * Change store state.
	 *
	 * @param newStoreState String
	 */
	@When("^I change the store state to (.+)$")
	public void changeStoreState(final String newStoreState) {
		storeEditor.clickTab("Summary");
		storeEditor.changeStoreStateAndConfirm(newStoreState);
	}

	/**
	 * Change store state without clicking confirmation button.
	 *
	 * @param newStoreState String
	 */
	@When("^I change the store state without confirmation to (.+)$")
	public void changeStoreStateWithoutConfirmation(final String newStoreState) {
		storeEditor.clickTab("Summary");
		storeEditor.changeStoreState(newStoreState);
	}

	/**
	 * Verifies store state in list.
	 *
	 * @param expState String
	 */
	@Then("^the store list should show (.+) state$")
	public void verifyStoreState(final String expState) {
		storesResultPane.verifyStoreState(expState, this.storeCode);
	}

	/**
	 * Deletes the store.
	 */
	@When("^I delete the store$")
	public void deleteStore() {
		storesResultPane.deleteStore(this.storeCode);
	}

	/**
	 * Verifies store no longer exists.
	 */
	@Then("^store should not exist$")
	public void verifyStoreDoesntExist() {
		storesResultPane.closePane("Stores");
		clickStores();
		storesResultPane.verifyStoreDoesntExists(this.storeCode);
	}

	/**
	 * Verifies store data policies enable state is true.
	 */
	@Then("^Data Policies for the store is enabled$")
	public void verifyStoreDataPolicyEnabled() {
		storeEditor.verifyDataPolicyEnabled();
	}

	/**
	 * Delete the newly created store after test.
	 */
	@After("@cleanupStore")
	public void cleanupStore() {
		deleteStore();
		verifyStoreDoesntExist();
	}

	/**
	 * Delete the newly created store after test using DB
	 */
	@After(value = "@cleanUpStoreDB", order = Constants.CLEANUP_ORDER_FIRST)
	public void deleteNewlyCreatedStore() {
		DBConnector dbc = new DBConnector();
		dbc.deleteStore(this.storeCode);
	}

	/**
	 * Get list exist store codes
	 */
	@When("^I take all exist store codes$")
	public void getSoreCodesList() {
		store.setStoreCodesList(storesResultPane.getExistStoresCodesList());
	}

	/**
	 * View profile attribute tab.
	 */
	@When("^I view the store profile attribute policies list$")
	public void viewStoreProfileAttributes() {
		storeEditor.clickTab(PROFILE_ATTRIBUTE_POLICIES);
	}

	/**
	 * Verifies profile attribute policy does not exist.
	 */
	@Then("^the store profile attribute list should not have a policy for (.+)$")
	public void verifyStoreProfileAttributePolicyDoesNotExist(final String attribute) {
		profileAttributePolicyTab.verifyStoreProfileAttributePolicyDoesNotExist(attribute);
	}

	/**
	 * Verifies profile attribute policy exists.
	 */
	@Then("^the store profile attribute list should have a policy for attribute (.+) with a policy of (.+)$")
	public void verifyStoreProfileAttributePolicyDoesExist(final String attribute, final String policy) {
		profileAttributePolicyTab.verifyStoreProfileAttributePolicyExists(attribute, policy);
	}

	/**
	 * Add profile attribute policy.
	 */
	@When("^I add the store profile attribute (.+) with a policy of (.+)$")
	public void addStoreAttributePolicy(final String attribute, final String policy) {
		storeEditor.clickTab(PROFILE_ATTRIBUTE_POLICIES);
		addEditProfileAttributePolicyDialog = profileAttributePolicyTab.clickAddPolicyButton();
		addEditProfileAttributePolicyDialog.selectAttribute(attribute);
		addEditProfileAttributePolicyDialog.selectPolicy(policy);
		addEditProfileAttributePolicyDialog.clickSaveButton();
		configurationActionToolbar.clickSaveButton();
	}

	/**
	 * Edit profile attribute policy.
	 */
	@When("^I edit the store profile attribute (.+) and change the policy to (.+)$")
	public void editStoreAttributePolicy(final String attribute, final String policy) {
		storeEditor.clickTab(PROFILE_ATTRIBUTE_POLICIES);
		profileAttributePolicyTab.selectStoreProfileAttributePolicy(attribute);
		addEditProfileAttributePolicyDialog = profileAttributePolicyTab.clickEditPolicyButton();
		addEditProfileAttributePolicyDialog.selectPolicy(policy);
		addEditProfileAttributePolicyDialog.clickSaveButton();
		configurationActionToolbar.clickSaveButton();
	}

	/**
	 * Remove profile attribute policy.
	 */
	@When("^I remove the store profile attribute (.+)$")
	public void removeStoreAttributePolicy(final String attribute) {
		storeEditor.clickTab(PROFILE_ATTRIBUTE_POLICIES);
		profileAttributePolicyTab.selectStoreProfileAttributePolicy(attribute);
		profileAttributePolicyTab.clickDeletePolicyButton();
		configurationActionToolbar.clickSaveButton();
	}


	@When("^I toggle Searchable property of facet (.+)")
	public void changeFacetToSearchable(final String facetName) {
		storeEditor.filterFacetTableByName(facetName);
		storeEditor.toggleFacetSearcable(facetName);
		configurationActionToolbar.clickSaveButton();
	}

	@Then("^I should see newly created attribute as a facet with the following details$")
	public void verifyFacetExistsInTable(final Map<String, String> facetInfoMap) {
		verifyFacetInTable(catalog.getAttributeName(), facetInfoMap);
	}

	@Then("^I should see facet (.+) with the following details$")
	public void verifyFacetInTable(final String facetName, final Map<String, String> facetInfoMap) {
		storeEditor.filterFacetTableByName(facetName);
		storeEditor.verifyFacetExistsInTable(facetName, facetInfoMap);
	}

	@When("^I select (.+) tab in the Store Editor$")
	public void clickTab(final String tabName) {
		storeEditor.clickTab(tabName);
	}

	@When("^I configure facet (.+) with facetable option (.+)")
	public void setFacetableValue(final String facetName, final String option) {
		this.facetName = facetName;
		storeEditor.filterFacetTableByName(facetName);
		storeEditor.selectFacetableOption(facetName, option);
		configurationActionToolbar.clickSaveButton();
	}

	@When("^I configure facet (.+) with the following ranges$")
	public void configureRangeFacet(final String facetName, final List<Map<String, String>> facetRangeValues) {
		this.facetName = facetName;
		storeEditor.filterFacetTableByName(facetName);
		storeEditor.selectFacetableOption(facetName, RANGE_FACET);

		ConfigureRangeFacetDialog configureRangeFacetDialog = (ConfigureRangeFacetDialog) storeEditor.clickEdiFacetButton(RANGE_FACET);
		configureRangeFacetDialog.setRangeValues(facetRangeValues);
		configureRangeFacetDialog.clickSave();

		configurationActionToolbar.clickSaveButton();
	}

	@When("I configure facet (.+) with the following display names")
	public void configureFacetDisplayName(final String facetName, final Map<String, String> displayNameInfoMap) {
		storeEditor.filterFacetTableByName(facetName);
		storeEditor.clickFacetFromTable(facetName);

		ConfigureFacetDialog configureRangeFacetDialog = (ConfigureFacetDialog) storeEditor.clickEdiFacetButton(FACET);
		for (Map.Entry<String, String> entry : displayNameInfoMap.entrySet()) {
			configureRangeFacetDialog.changeLocale(entry.getKey());
			configureRangeFacetDialog.enterDisplayName(entry.getValue());
		}
		configureRangeFacetDialog.clickSave();

		configurationActionToolbar.clickSaveButton();
	}

	@Given("a facet (.+) that is configured with Facetable value (.+)$")
	public void givenFacetableState(final String facetName, final String facetableState) {
		this.facetName = facetName;
		this.initialFacetOption = facetableState;
	}

	@Given("a facet (.+) that is configured with the following ranges$")
	public void givenFacetableState(final String facetName, final List<Map<String, String>> facetRangeValues) {
		this.facetName = facetName;
	}

	@After("@cleanupEnabledFacet")
	public void cleanupEnabledFacet() {
		setFacetableValue(this.facetName, "No Facet");
	}

	@After("@cleanupPriceFacet")
	public void cleanupPriceFacet() {
		DBConnector dbConnector = new DBConnector();

		String query = "UPDATE TFACET SET RANGE_FACET_VALUES = \"["
				+ "{'start':0,'end':5,'displayNameMap':{'en':'Below $5','fr_CA':'0 - 100','fr':'0 - 100'}},"
				+ "{'start':5,'end':20,'displayNameMap':{'en':'$5 to $20','fr_CA':'100 - 200','fr':'100 - 200'}},"
				+ "{'start':20,'end':50, 'displayNameMap':{'en':'$20 to $50','fr_CA':'200 - 300','fr':'200 - 300'}},"
				+ "{'start':50,'end':200,'displayNameMap':{'en':'$50 to $200','fr_CA':'300 +','fr':'300 +'}},"
				+ "{'start':200,'end':null,'displayNameMap':{'en':'$200 and Above','fr_CA':'','fr':''}}"
				+ "]\" WHERE FACET_NAME = 'Price' AND STORECODE = 'MOBEE';";

		dbConnector.executeUpdateQuery(query);
		dbConnector.closeAll();
	}

	@After("@cleanupDisabledFacet")
	public void cleanupDisabledFacet() {
		setFacetableValue(this.facetName, this.initialFacetOption);
	}

	@Then("^I should see (.+) dialog$")
	public void verifyDialogDisplayed(final String dialogTitle) {
		String automationId = "";
		switch (dialogTitle) {
			case "Cannot Configure Facets":
				automationId = "com.elasticpath.cmclient.admin.stores.AdminStoresMessages.StoreFacetErrorDialogHeader";
				break;
			case "Cannot Configure Sort Attributes":
				automationId = "com.elasticpath.cmclient.admin.stores.AdminStoresMessages.StoreSortAttributeErrorDialogHeader";
				break;
			default:
				assertThat(false)
						.as("Invalid dialog specified: " + dialogTitle)
						.isTrue();
				break;
		}

		assertThat(new ConfirmDialog(getDriver()).isDialogDisplayed(automationId))
				.as("Dialog was not visible")
				.isTrue();
	}

	@When("^I filter facet table by group (.+)$")
	public void filterFacetTableByGroup(final String facetGroup) {
		storeEditor.filterFacetTableByGroup(facetGroup);
	}

	@Then("^I should only see facets with group (.+)$")
	public void verifyVisibleFacetsGroup(final String facetGroup) {
		storeEditor.maximizeStoreEditor();
		assertThat(storeEditor.getVisibleFacetGroups())
				.as("List contained unexpected item")
				.containsOnly(facetGroup);
		storeEditor.minimizeStoreEditor();
	}

	@Then("^I should see newly created catalog attribute$")
	public void verifySortingTabContainsCatalogAttribute() {
		AddEditSortAttributeDialog dialog = sortingTab.clickAddSortAttributeButton();
		dialog.selectAttributeKey("Attribute", catalog.getAttributeKey());
	}

	@When("^I add the following sort attribute$")
	public void addSortAttribute(final List<SortAttribute> sortAttributeList) {
		this.sortAttribute = sortAttributeList.get(0);
		AddEditSortAttributeDialog addSortAttributeDialog = sortingTab.clickAddSortAttributeButton();
		addSortAttributeDialog.setSortAttributeValues(sortAttribute);
		addSortAttributeDialog.clickSave();
		configurationActionToolbar.clickSaveButtonNoWait();
	}

	@When("^I edit sort attribute with display name (.+) to have the following values$")
	public void editSortAttribute(final String displayName, final List<SortAttribute> updateSortInfo) {
		sortingTab.selectSortAttribute(displayName);
		AddEditSortAttributeDialog dialog = sortingTab.clickEditSortAttributeButton();
		dialog.setSortAttributeValues(updateSortInfo.get(0));
		dialog.clickSave();
		configurationActionToolbar.clickSaveButton();
		sortAttribute = updateSortInfo.get(0);
	}

	@When("^I remove sort attribute with display name (.+)")
	public void removeSortAttribute(final String displayName) {
		sortAttribute = new SortAttribute();
		sortAttribute.setLanguage("English");
		sortAttribute.setDisplayName(displayName);
		sortingTab.removeSortAttribute(sortAttribute.getDisplayName());
		configurationActionToolbar.clickSaveButtonNoWait();
	}

	@Then("^I should see (?:newly added|recently updated) sort attribute in sorting table$")
	public void verifySortAttributeExistsInTable() {
		configurationActionToolbar.clickReloadActiveEditor();
		sortingTab.verifySortAttributeExistsInTable(sortAttribute);
	}

	@Then("^I should not see recently removed sort attribute in sorting table$")
	public void verifySortAttributeNotInTable() {
		configurationActionToolbar.clickReloadActiveEditor();
		sortingTab.verifySortAttributeNotInTable(sortAttribute.getDisplayName());
	}

	@When("^I make sort attribute with display name (.+) the default$")
	public void changeDefaultSortAttribute(final String displayName) {
		sortingTab.clickDefaultSortButton(displayName);
		configurationActionToolbar.clickSaveButtonNoWait();
	}

	@After("@cleanupAddedSortAttribute")
	public void cleanupAddedSortAttribute() {
		removeSortAttribute(sortAttribute.getDisplayName());
	}

	@After("@cleanupRatingSortAttribute")
	public void cleanupRatingSortAttribute() {
		SortAttribute originalInfo = new SortAttribute(SortAttribute.ATTRIBUTE, "A00009", SortAttribute.ASCENDING, "English", "rating low to high");
		addSortAttribute(Arrays.asList(originalInfo));
	}

	@After("@cleanupModifiedSortAttribute")
	public void cleanupModifiedSortAttribute() {
		SortAttribute originalInfo = new SortAttribute(SortAttribute.ASCENDING, "English", "name A-Z");
		editSortAttribute(sortAttribute.getDisplayName(), Arrays.asList(originalInfo));
	}

	@After("@cleanupDefaultSortAttribute")
	public void cleanupDefaultSortAttribute() {
		changeDefaultSortAttribute("Best Match");
	}
}

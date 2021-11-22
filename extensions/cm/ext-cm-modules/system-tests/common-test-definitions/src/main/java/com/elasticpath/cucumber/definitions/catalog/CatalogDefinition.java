package com.elasticpath.cucumber.definitions.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cucumber.definitions.NavigationDefinition;
import com.elasticpath.cucumber.definitions.ProductAndBundleDefinition;
import com.elasticpath.cucumber.definitions.catalog.editor.tabs.CartItemModifierGroupTabDefinition;
import com.elasticpath.cucumber.definitions.catalog.editor.tabs.CategoryTypesTabDefinition;
import com.elasticpath.cucumber.definitions.catalog.editor.tabs.ProductTypesTabDefinition;
import com.elasticpath.selenium.dialogs.AddAttributeDialog;
import com.elasticpath.selenium.dialogs.AddEditBrandDialog;
import com.elasticpath.selenium.dialogs.CategoryFinderDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateCatalogDialog;
import com.elasticpath.selenium.dialogs.CreateEditVirtualCatalogDialog;
import com.elasticpath.selenium.dialogs.EditAttributeDialog;
import com.elasticpath.selenium.dialogs.EditGlobalAttributesDialog;
import com.elasticpath.selenium.dialogs.SelectAProductDialog;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.Catalog;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.CategoryType;
import com.elasticpath.selenium.domainobjects.LinkedCategory;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.editor.CategoryEditor;
import com.elasticpath.selenium.editor.catalog.CatalogEditor;
import com.elasticpath.selenium.editor.catalog.tabs.BrandsTab;
import com.elasticpath.selenium.editor.product.ProductEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CatalogManagement;
import com.elasticpath.selenium.resultspane.CatalogProductListingPane;
import com.elasticpath.selenium.resultspane.CatalogSearchResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.toolbars.ChangeSetActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.CreateCategoryWizard;

/**
 * Catalog Search step definitions.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.ExcessiveParameterList",
		"PMD.ExcessivePublicCount"})
public class CatalogDefinition {
	private static final String ENGLISH = "English";
	private static final String EMPTY_STRING = "";
	private static final String FRENCH = "French";
	private static final String CATALOG_MESSAGE_DELETE_DIALOG = "CatalogMessages.CatalogBrowseView_Action_DeleteCategoryDialog";
	private final CatalogManagement catalogManagement;
	private final ActivityToolbar activityToolbar;
	private final ChangeSetActionToolbar changeSetActionToolbar;
	private final CatalogSearchResultPane catalogSearchResultPane;
	private CatalogProductListingPane catalogProductListingPane;
	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
	private CreateCatalogDialog createCatalogDialog;
	private CreateEditVirtualCatalogDialog createEditVirtualCatalogDialog;
	private CreateCategoryWizard createCategoryWizard;
	private CategoryFinderDialog categoryFinderDialog;
	private EditGlobalAttributesDialog editGlobalAttributesDialog;
	private AddAttributeDialog addAttributeDialog;
	private EditAttributeDialog editAttributeDialog;
	private CatalogEditor catalogEditor;
	private ProductEditor productEditor;
	private AddEditBrandDialog addEditBrandDialog;
	private final BrandsTab brandsTab;
	private final Catalog catalog;
	private final Category category;
	private final LinkedCategory linkedCategory;
	private final ProductType productType;
	private final CategoryContainer categoryContainer;
	private CategoryEditor categoryEditor;
	private String globalAttributeName = "";
	private String virtualProductName = "";
	private String virtualCategoryName = "";
	private String virtualCatalogName = "";
	private final WebDriver driver;
	private final ProductTypesTabDefinition productTypesTabDefinition;
	private final CartItemModifierGroup cartItemModifierGroup;
	private final CartItemModifierGroupTabDefinition cartItemModifierGroupTabDefinition;
	private final CategoryTypesTabDefinition categoryTypesTabDefinition;
	private final ProductAndBundleDefinition productAndBundleDefinition;
	private final static String CATEGORY_ASSIGNMENT = "Category Assignment";
	private final static String MERCHANDISING_ASSIGNMENT = "Merchandising Associations";
	private final static String DASH = "-";
	private static final String EMPTY = "empty";
	private final static String TRUE = "true";
	private final CategoryType categoryType;
	private final AttributeContainer attributeContainer;
	private static final Logger LOGGER = LogManager.getLogger(CatalogDefinition.class);
	private static final Pattern DATE_PATTERN = Pattern.compile("[A-Za-z]{3}\\s[0-9]{1,2},\\s[0-9]{4}\\s[0-9]+:[0-9]+\\sPM|AM");
	private static final String DISABLE_DATE_PARAM_NAME = "disableDateTime";
	private static final String ENABLE_DATE_PARAM_NAME = "enableDateTime";


	/**
	 * Constructor.
	 *
	 * @param catalog                            catalog shared object between CategoryTypesTabDefinition and CatalogDefinition
	 * @param category                           Category object.
	 * @param productType                        ProductType object.
	 * @param categoryContainer                  CategoryContainer object.
	 * @param productTypesTabDefinition          ProductTypesTabDefinition object.
	 * @param cartItemModifierGroup              Cart Item Modifier Group object.
	 * @param cartItemModifierGroupTabDefinition Cart Item Modifier Group Tab Definition object.
	 * @param categoryTypesTabDefinition         CategoryTypesTabDefinition object.
	 * @param productAndBundleDefinition         ProductAndBundleDefinition object.
	 * @param categoryType                       CategoryType object.
	 * @param attributeContainer                 attribute container.
	 */
//	CHECKSTYLE:OFF: checkstyle:too many parameters
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public CatalogDefinition(final Catalog catalog, final Category category, final LinkedCategory linkedCategory, final ProductType productType,
							 final CategoryContainer categoryContainer, final ProductTypesTabDefinition productTypesTabDefinition,
							 final CartItemModifierGroup cartItemModifierGroup,
							 final CartItemModifierGroupTabDefinition cartItemModifierGroupTabDefinition,
							 final CategoryTypesTabDefinition categoryTypesTabDefinition,
							 final ProductAndBundleDefinition productAndBundleDefinition,
							 final CategoryType categoryType, final AttributeContainer attributeContainer) {
		driver = SeleniumDriverSetup.getDriver();
		catalogManagement = new CatalogManagement(driver);
		catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		activityToolbar = new ActivityToolbar(driver);
		brandsTab = new BrandsTab(driver);
		changeSetActionToolbar = new ChangeSetActionToolbar(driver);
		catalogSearchResultPane = new CatalogSearchResultPane(driver);
		this.catalog = catalog;
		this.category = category;
		this.linkedCategory = linkedCategory;
		this.productType = productType;
		this.cartItemModifierGroup = cartItemModifierGroup;
		this.productTypesTabDefinition = productTypesTabDefinition;
		this.cartItemModifierGroupTabDefinition = cartItemModifierGroupTabDefinition;
		this.categoryTypesTabDefinition = categoryTypesTabDefinition;
		this.productAndBundleDefinition = productAndBundleDefinition;
		this.categoryType = categoryType;
		this.attributeContainer = attributeContainer;
		this.categoryContainer = categoryContainer;
		this.categoryEditor = new CategoryEditor(driver);
	}

	/**
	 * Expand Catalog.
	 *
	 * @param catalogName the catalog Name.
	 */
	@When("^I expand (.+) catalog$")
	public void expandCatalog(final String catalogName) {
		catalogManagement.expandCatalog(catalogName);
		catalogManagement.clickCatalogBrowseTab();
	}

	/**
	 * Expand Catalog.
	 *
	 * @param catalogName the catalog Name.
	 */
	@When("^I expand catalog with catalog name (.+)$")
	public void expandCatalogWithCatalogName(final String catalogName) {
		catalogManagement.expandCatalog(catalogName);
		catalogManagement.clickCatalogBrowseTab();
		this.catalog.setCatalogName(catalogName);
	}

	/**
	 * Expand catalog that was created in the same scenario.
	 */
	@When("^I expand catalog created in this scenario$")
	public void expandNewlyCreatedCatalog() {
		catalogManagement.expandCatalog(this.catalog.getCatalogName());
	}

	/**
	 * Double click category.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I open category (.+) to view products list$")
	public void doubleClickCategory(final String categoryName) {
		catalogProductListingPane = catalogManagement.doubleClickCategory(categoryName);
	}

	/**
	 * Open Category Editor.
	 *
	 * @param categoryName the category name.
	 * @param catalogName  the catalog name.
	 */
	@When("^I open category (.+) in editor for catalog (.*)$")
	public void openCategoryEditor(final String categoryName, final String catalogName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		LOGGER.log(Level.WARN, "name: " + fullName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		LOGGER.log(Level.WARN, "code: " + category.getCategoryCode());
		String parent = this.categoryContainer.getCategoryMap().get(fullName).getParentCategory();
		LOGGER.log(Level.WARN, "parent: " + parent);
		if (parent != null) {
			List<String> path = categoryContainer.getPathLocalizedName(fullName, ENGLISH);
			path.add(category.getName(ENGLISH));
			catalogManagement.expandCategoryVerifySubcategory(catalogName, path);
		}
		catalogManagement.selectCategoryPartialName(category.getName(ENGLISH));
		categoryEditor = catalogManagement.clickOpenCategoryIcon();
	}

	/**
	 * Edit last 5 characters with random values of category names without saving.
	 *
	 * @param categoryName the category name.
	 * @param languages    category languages.
	 */
	@When("^I edit last 5 characters of category (.+) names with random characters for the following languages without saving")
	public void editCategoryNameRandom(final String categoryName, final List<String> languages) {
		String oldName;
		String oldNameEn = "";
		String editedName;
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		List<String> children = categoryContainer.getChildNames(fullName);
		for (String language : languages) {
			categoryEditor.selectLanguage(language);
			oldName = categoryEditor.getCategoryName();
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			categoryEditor.enterNewCategoryName(editedName);
			category.setName(language, editedName);
			if (ENGLISH.equals(language)) {
				category.setCategoryName(editedName);
				oldNameEn = oldName;
				for (String child : children) {
					categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(child)).setParentCategory(editedName);
				}
			}
		}
		for (String lang : languages) {
			if (ENGLISH.equals(lang)) {
				categoryEditor.selectLanguage(lang);
			}
		}
		categoryContainer.addCategory(category);
		categoryContainer.removeCategory(oldNameEn);
	}

	/**
	 * Edits category enable and disable dates.
	 *
	 * @param categoryName the category name.
	 * @param dates        category enable and disable dates.
	 */
	@When("^I edit category (.+) enable and disable dates without saving")
	public void editCategoryDates(final String categoryName, final Map<String, String> dates) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		String enableDate = dates.get(ENABLE_DATE_PARAM_NAME);
		String disableDate = dates.get(DISABLE_DATE_PARAM_NAME);
		if (DATE_PATTERN.matcher(enableDate).matches()) {
			categoryEditor.enterFormattedEnableDate(enableDate);
			category.setEnableDateTime(enableDate);
		} else {
			categoryEditor.enterEnableDateTime(Integer.valueOf(enableDate));
			category.setEnableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(enableDate)));
		}
		if (DATE_PATTERN.matcher(disableDate).matches()) {
			categoryEditor.enterFormattedDisableDate(disableDate);
			category.setDisableDateTime(disableDate);
		} else {
			if (disableDate.equals(EMPTY_STRING)) {
				categoryEditor.clearDisableDateTime();
				category.setDisableDateTime(null);
			} else {
				categoryEditor.enterDisableDateTime(Integer.valueOf(disableDate));
				category.setDisableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(disableDate)));
			}
		}
		categoryContainer.getCategoryMap().replace(fullName, category);
	}

	/**
	 * Edit category name.
	 *
	 * @param categoryName    the category name.
	 * @param newCategoryName new category name.
	 */
	@When("^I edit the name of a category with a name (.*) to a (.*)")
	public void editCategoryNameForCategory(final String categoryName, final String newCategoryName) {
		categoryEditor.enterNewCategoryName(newCategoryName);
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		category.setName(ENGLISH, newCategoryName);
		category.setName(FRENCH, newCategoryName);
		categoryContainer.getCategoryMap().replace(fullName, category);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Saves an ordered list of children for specified category.
	 *
	 * @param name category name.
	 */
	@When("^I save the ordered list of (\\d+) children for expanded category (.+)")
	public void saveOrderedChildren(final int childrenAmount, final String name) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(name);
		categoryContainer.addCategoryChildren(fullName, catalogManagement.getExpandedCategoryChildren(fullName, childrenAmount));
	}

	/**
	 * Moves the last child of category one position up.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I move the last child of category (.+) one position up in catalog")
	public void moveCategoryUp(final String categoryName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		ImmutableList<String> children = categoryContainer.getCategoryChildren(fullName);
		catalogManagement.selectCategory(children.get(children.size() - 1));
		catalogManagement.clickMoveUpCategoryIcon();
		List<String> newChildren = new LinkedList<>(children.subList(0, children.size() - 2));
		newChildren.add(children.get(children.size() - 1));
		newChildren.add(children.get(children.size() - 2));
		ImmutableList<String> newOrderedChildren = new ImmutableList.Builder<String>().addAll(newChildren).build();
		categoryContainer.addCategoryChildren(fullName, newOrderedChildren);
		assertThat(categoryContainer.getCategoryChildren(fullName))
				.as("The last child was not moved up")
				.contains(catalogManagement.getExpandedCategoryChildren(fullName, 2).toArray(new String[children.size()]));
	}

	/**
	 * Change store visible status.
	 *
	 * @param categoryName the category name.
	 * @param status       new status.
	 */
	@When("^I edit the store visible to (.+) for category (.*)")
	public void editStoreVisible(final String status, final String categoryName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		if (TRUE.equalsIgnoreCase(status)) {
			categoryEditor.clickVisibleUncheckedBox();
		} else {
			categoryEditor.clickVisibleBox();
		}
		category.setStoreVisible(status);
		categoryContainer.getCategoryMap().replace(fullName, category);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Select language.
	 *
	 * @param language language which should be chosen.
	 */
	@When("^I select (.*) language")
	public void selectLanguage(final String language) {
		categoryEditor.selectLanguage(language);
	}

	/**
	 * Edit category date time.
	 *
	 * @param categoryInfoList Category info.
	 */
	@When("^I edit the category date time")
	public void editCategoryDateTime(final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			if (category.getDisableDateTime() != null && !category.getDisableDateTime().isEmpty() && !category.getDisableDateTime().equals(
					EMPTY)) {
				categoryEditor.enterDisableDateTime(Integer.valueOf(category.getDisableDateTime()));
			}
			if (category.getEnableDateTime() != null && !category.getEnableDateTime().isEmpty() && !category.getEnableDateTime().equals(EMPTY)) {
				categoryEditor.enterEnableDateTime(Integer.valueOf(category.getEnableDateTime()));
			}
			if (category.getDisableDateTime().equals(EMPTY)) {
				categoryEditor.enterEmptyDisableDateTime();
			}
			if (category.getEnableDateTime().equals(EMPTY)) {
				categoryEditor.enterEmptyEnableDateTime();
			}
			catalogManagementActionToolbar.clickSaveButton();
		}
	}

	/**
	 * Edit category disable date time.
	 *
	 * @param categoryName the category name.
	 * @param datePlus     number that we add to the current date
	 */
	@When("^I edit the category (.*) disable date time to (.*)")
	public void editCategoryDisableDateTime(final String categoryName, final String datePlus) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		if (datePlus.equals(EMPTY)) {
			categoryEditor.enterEmptyDisableDateTime();
			category.setDisableDateTime(null);
		} else {
			categoryEditor.enterDisableDateTime(Integer.valueOf(datePlus));
			category.setDisableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(datePlus)));
		}
		categoryContainer.getCategoryMap().replace(fullName, category);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Removes category disable date time.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I remove disable date time for category (.*)")
	public void removeCategoryDisableDateTime(final String categoryName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		categoryEditor.clearDisableDateTime();
		category.setDisableDateTime(null);
		categoryContainer.getCategoryMap().replace(fullName, category);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Checks if category is store visible and if not makes it visible.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I make sure opened category (.+) is visible for its store")
	public void assureCategoryIsVisible(final String categoryName) {
		boolean isChanged;
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		isChanged = categoryEditor.makeCategoryStoreVisible();
		category.setStoreVisible("true");
		categoryContainer.getCategoryMap().replace(fullName, category);
		if (isChanged) {
			catalogManagementActionToolbar.clickSaveButton();
			catalogManagementActionToolbar.clickReloadActiveEditor();
		}
	}

	/**
	 * Checks if category is store visible and if not makes it visible.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I make sure category (.+) has empty disable date")
	public void assureCategoryEmptyDisableDate(final String categoryName) {
		if (!categoryEditor.getDisableDate().isEmpty()) {
			removeCategoryDisableDateTime(categoryName);
		}
	}

	/**
	 * Verify product in product listing.
	 *
	 * @param productNameList the product name list.
	 */
	@Then("^Product Listing should contain following products$")
	public void verifyProductInProductListing(final List<String> productNameList) {
		for (String productName : productNameList) {
			catalogProductListingPane.verifyProductNameExists(productName);
		}
	}

	/**
	 * Create a new catalog with the given details.
	 *
	 * @param catalogInfoList the catalog details list.
	 */
	@When("^(?:I create a|a) new catalog with following details$")
	public void createNewCatalog(final List<Catalog> catalogInfoList) {
		for (Catalog catalog : catalogInfoList) {
			createCatalogDialog = catalogManagementActionToolbar.clickCreateCatalogButton();
			String catalogCode = Utility.getRandomUUID();
			createCatalogDialog.enterCatalogCode(catalogCode);
			this.catalog.setCatalogName(catalog.getCatalogName() + DASH + catalogCode);
			createCatalogDialog.enterCatalogName(this.catalog.getCatalogName());
			createCatalogDialog.selectAvailableLanguage(catalog.getLanguage());
			createCatalogDialog.clickMoveRightButton();
			createCatalogDialog.verifySelectedLanguage(catalog.getLanguage());
			createCatalogDialog.selectDefaultLanguage(catalog.getLanguage());
			createCatalogDialog.clickSaveButton();
			if (catalog.getBrand() != null) {
				addCatalogBrand(catalog.getBrand());
			}
		}
	}

	/**
	 * Add catalog brand.
	 *
	 * @param brandName brand name.
	 */
	private void addCatalogBrand(final String brandName) {
		selectCatalogEditorTab("Brands");
		addEditBrandDialog = brandsTab.clickAddBrandButton();
		String brandCode = Utility.getRandomUUID();
		addEditBrandDialog.enterBrandCode(brandCode);
		this.catalog.setBrand(brandName + brandCode);
		addEditBrandDialog.enterBrandName(this.catalog.getBrand());
		addEditBrandDialog.clickAddButton();
		brandsTab.verifyAndSelectBrand(addEditBrandDialog.getBrandCode());
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete new brand.
	 */
	@When("^I delete the newly created brand")
	public void deleteNewBrand() {
		selectCatalogEditorTab("Brands");
		brandsTab.verifyAndSelectBrand(this.catalog.getBrand());
		brandsTab.clickRemoveBrandButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete given brand.
	 *
	 * @param brandCode brand code to be deleted.
	 */
	@When("^I delete the brand (.+)$")
	public void deleteNewBrandCode(final String brandCode) {
		deleteBrandCode(brandCode);
	}

	/**
	 * Delete newly created brand.
	 */
	@When("^I delete the newly create brand$")
	public void deleteNewBrandCode() {
		deleteBrandCode(this.catalog.getBrand());
		this.catalog.setBrand(null);
	}

	private void deleteBrandCode(final String brandCode) {
		brandsTab.verifyAndSelectBrand(brandCode);
		brandsTab.clickRemoveBrandButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}


	/**
	 * Verify brand deleted.
	 */
	@Then("^The brand is deleted$")
	public void verifyBrandDeleted() {
		brandsTab.verifyBrandDelete(this.catalog.getBrand());
	}

	/**
	 * Verify catalog exists.
	 */
	@Then("^newly (?:created|edited) (?:virtual catalog|catalog) is in the list$")
	public void verifyCatalogExists() {
		catalogManagement.clickCatalogRefreshButton();
		catalogManagement.verifyCatalogExists(this.catalog.getCatalogName());
	}

	/**
	 * Create virtual catalog with language.
	 *
	 * @param catalogInfoList catalog details.
	 */
	@And("^(?:I create a|a) new virtual catalog with following details$")
	public void createVirtualCatalogWithLanguage(final List<Catalog> catalogInfoList) {
		activityToolbar.clickCatalogManagementButton();
		for (Catalog catalog : catalogInfoList) {
			String virtualCatalogCode = Utility.getRandomUUID();
			this.catalog.setCatalogName(catalog.getCatalogName() + DASH + virtualCatalogCode);
			this.catalog.setLanguage(catalog.getLanguage());
			createEditVirtualCatalogDialog = catalogManagementActionToolbar.clickCreateVirtualCatalogButton();
			createEditVirtualCatalogDialog.enterCatalogCode(virtualCatalogCode);
			createEditVirtualCatalogDialog.enterCatalogName(this.catalog.getCatalogName());
			createEditVirtualCatalogDialog.selectDefaultLanguage(this.catalog.getLanguage());
			createEditVirtualCatalogDialog.clickSaveButton();
		}
	}

	/**
	 * select new catalog.
	 */
	@And("^I select newly created (?:virtual catalog|catalog) in the list$")
	public void selectNewCatalog() {
		catalogManagement.selectCatalog(this.catalog.getCatalogName());
	}

	/**
	 * select existing catalog.
	 *
	 * @param catalogName the catalog name.
	 */
	@And("^I select catalog (.+) in the list$")
	public void selectExistingCatalog(final String catalogName) {
		catalogManagement.selectCatalog(catalogName);
	}

	/**
	 * select and delete existing catalog.
	 *
	 * @param catalogName the catalog name.
	 */
	@And("^I delete selected catalog (.+) in the list$")
	public void selectDeleteExistingCatalog(final String catalogName) {
		activityToolbar.clickCatalogManagementButton();
		catalogManagement.selectCatalog(catalogName);
		catalogManagement.rightClickDelete();
	}

	/**
	 * Delete newly created catalog.
	 */
	@And("^I delete newly created (?:virtual catalog|catalog)$")
	public void deleteNewlyCreatedCatalog() {
		selectNewCatalog();
		catalogManagement.rightClickDelete().clickOK();
		catalogManagement.clickCatalogRefreshButton();
	}

	/**
	 * Verify newly created catalog is deleted.
	 */
	@And("^newly created (?:virtual catalog|catalog) is deleted$")
	public void verifyNewlyCreatedCatalogIsDeleted() {
		catalogManagement.verifyCatalogIsDeleted(this.catalog.getCatalogName());
	}

	/**
	 * Clicks next button in create category wizard.
	 */
	private void clickNextButtonCreateCategory() {
		createCategoryWizard.clickNextInDialog();
	}

	/**
	 * Select category.
	 *
	 * @param catalogName  the catalog name.
	 * @param categoryName the category name.
	 */
	@And("^I select (.+) category in (.+) catalog$")
	public void selectCategory(final String catalogName, final String categoryName) {
		catalogManagement.selectCategoryInCatalog(catalogName, categoryName);
	}

	/**
	 * Verify Category exists.
	 */
	@Then("^the newly created category exists$")
	public void verifyCategoryExists() {
		catalogManagement.verifyCategoryExists(this.category.getCategoryName());
	}

	/**
	 * Verify Linked Category exists.
	 *
	 * @param linkedCategory the linked category
	 * @param catalog        the catalog
	 */
	@And("^the linked category (.+) (?:should be|is) added to catalog (.+)$")
	public void verifyLinkedCategoryExists(final String linkedCategory, final String catalog) {
		catalogManagement.expandCatalogAndVerifyCategory(catalog, linkedCategory);
		catalogManagement.verifyCategoryExists(linkedCategory);
	}

	/**
	 * Select new category.
	 */
	@And("^I select newly created category$")
	public void selectNewCategory() {
		catalogManagement.selectCategory(this.category.getCategoryName());
	}

	/**
	 * Select new linked category.
	 */
	@And("^I select newly created linkedcategory$")
	public void selectNewLinkedcategory() {
		catalogManagement.selectCategory(this.linkedCategory.getLinkedCategoryName());
	}

	/**
	 * Delete new category.
	 */
	@And("^I delete newly created (?:category|subcategory)")
	public void deleteNewCategory() {
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.clickCatalogRefreshButton();
		selectNewCategory();
		catalogManagement.clickDeleteCategoryIcon();
		new ConfirmDialog(driver).clickOKButton(CATALOG_MESSAGE_DELETE_DIALOG);
	}

	/**
	 * Delete category.
	 *
	 * @param categoryName category name.
	 */
	@And("^I delete specified category (.*)")
	public void deleteCategory(final String categoryName) {
		Category category = findCategory(categoryName);
		catalogManagement.selectCategory(category.getName(ENGLISH));
		catalogManagement.clickDeleteCategoryIcon();
		new ConfirmDialog(driver).clickOKButton(CATALOG_MESSAGE_DELETE_DIALOG);
	}

	/**
	 * Delete linked category.
	 *
	 * @param categoryName category name.
	 */
	@And("^I delete linked category (.*)")
	public void deleteLinkedCategory(final String categoryName) {
		deleteLinkedCategoryFromCatalog(categoryName, this.catalog.getCatalogName());
	}

	/**
	 * Delete linked category.
	 *
	 * @param categoryName category name.
	 * @param catalogName  linked category's catalog name.
	 */
	@And("^I remove linked category with name (.*) from catalog (.+)")
	public void deleteLinkedCategoryFromCatalog(final String categoryName, final String catalogName) {
		Category category = findCategory(categoryName);
		catalogManagement.selectCategoryInCatalog(catalogName, category.getName(ENGLISH));
		catalogManagement.clickRemoveLinkedCategoryIcon();
		new ConfirmDialog(driver).clickOKButton("CatalogMessages.CatalogBrowseView_Action_RemoveLinkedCatDialogTitle");
	}

	/**
	 * Delete new category.
	 */
	@And("^I delete newly created linked category")
	public void deleteNewLinkedCategory() {
		selectNewLinkedcategory();
		deleteLinkedCategory();
	}

	/**
	 * Remove linked category from Virtual Catalog.
	 *
	 * @param linkedCategoryName name of linked category to be deleted.
	 * @param virtualCatalog     name of Virtual Catalog in which linked category will be deleted.
	 */
	@And("^I remove linked category (.+) from virtual catalog (.+)")
	public void deleteLinkedCategoryFromVirtualCatalog(final String linkedCategoryName, final String virtualCatalog) {
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.clickCatalogRefreshButton();
		catalogManagement.selectCategoryInCatalog(virtualCatalog, linkedCategoryName);
		deleteLinkedCategory();
	}

	/**
	 * Helper method for deleting already selected linked category.
	 */
	public void deleteLinkedCategory() {
		catalogManagement.clickRemoveLinkedCategoryIcon();
		new ConfirmDialog(driver).clickOKButton("CatalogMessages.CatalogBrowseView_Action_RemoveLinkedCatDialogTitle");
	}

	/**
	 * Verify newly created category is deleted.
	 */
	@And("^(.+) (?:category|subcategory) is deleted$")
	public void verifyNewlyCreatedCategoryIsDeleted(final String categoryName) {
		if ("newly created".equalsIgnoreCase(categoryName)) {
			catalogManagement.verifyCategoryIsNotInList(this.category.getCategoryName());
		} else {
			catalogManagement.verifyCategoryIsNotInList(categoryName);
		}
	}

	/**
	 * Create new category for existing catalog.
	 *
	 * @param catalog          the catalog.
	 * @param categoryInfoList the category info list.
	 */
	@When("^I create new category for (.+) with following data")
	public void createNewCategoryForExistingCatalog(final String catalog, final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			selectExistingCatalog(catalog);
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			this.category.setCategoryCode(Utility.getRandomUUID());
			createCategoryWizard.enterCategoryCode(this.category.getCategoryCode());
			this.category.setCategoryName(category.getCategoryName() + DASH + this.category.getCategoryCode());
			createCategoryWizard.enterCategoryName(this.category.getCategoryName());
			this.category.setCategoryType(category.getCategoryType());
			createCategoryWizard.selectCategoryType(this.category.getCategoryType());
			createCategoryWizard.enterCurrentEnableDateTime();
			if (category.getStoreVisible().equalsIgnoreCase(TRUE)) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			clickNextButtonCreateCategory();
			createCategoryWizard.enterAttributeLongText(category.getAttrLongTextValue(), category.getAttrLongTextName());
			createCategoryWizard.enterAttributeDecimalValue(category.getAttrDecimalValue(), category.getAttrDecimalName());
			createCategoryWizard.enterAttributeShortText(category.getAttrShortTextValue(), category.getAttrShortTextName());
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Create new subcategory for an existing category in an existing catalog.
	 *
	 * @param category         a parent category for a new subcategory
	 * @param catalog          a catalog for a new subcategory.
	 * @param categoryInfoList a subcategory info list.
	 */
	@When("^I create new subcategory for category (.+) in catalog (.+) with following data")
	public void createNewSubcategoryForExistingCatalog(final String category, final String catalog, final List<Category> categoryInfoList) {
		for (Category subcategory : categoryInfoList) {
			catalogManagement.expandCatalog(catalog);
			catalogManagement.verifyCategoryExists(category);
			createCategoryWizard = catalogManagement.clickCreateSubcategoryIcon();
			this.category.setCategoryCode(Utility.getRandomUUID());
			createCategoryWizard.enterCategoryCode(this.category.getCategoryCode());
			this.category.setCategoryName(subcategory.getCategoryName() + DASH + this.category.getCategoryCode());
			createCategoryWizard.enterCategoryName(this.category.getCategoryName());
			this.category.setCategoryType(subcategory.getCategoryType());
			createCategoryWizard.selectCategoryType(this.category.getCategoryType());
			createCategoryWizard.enterCurrentEnableDateTime();
			if (subcategory.getStoreVisible().equalsIgnoreCase(TRUE)) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			clickNextButtonCreateCategory();
			createCategoryWizard.enterAttributeLongText(subcategory.getAttrLongTextValue(), subcategory.getAttrLongTextName());
			createCategoryWizard.enterAttributeDecimalValue(subcategory.getAttrDecimalValue(), subcategory.getAttrDecimalName());
			createCategoryWizard.enterAttributeShortText(subcategory.getAttrShortTextValue(), subcategory.getAttrShortTextName());
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Create new category.
	 *
	 * @param catalogName      name of catalog, where category will created.
	 * @param categoryInfoList information about category to comparing with.
	 */
	@When("^I create new category of the category type for (.*)$")
	public void createNewCategoryForCatalog(final String catalogName, final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			catalogManagement.selectCatalog(catalogName);
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			String categoryCode = Utility.getRandomUUID();
			createCategoryWizard.enterCategoryCode(categoryCode);
			createCategoryWizard.enterCategoryName(category.getCategoryName() + DASH + categoryCode);
			createCategoryWizard.selectCategoryType(this.categoryType.getCategoryTypeName());
			createCategoryWizard.enterEnableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(category.getEnableDateTime())));
			createCategoryWizard.enterDisableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(category.getDisableDateTime())));
			if (category.getStoreVisible().equalsIgnoreCase("true")) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			Category newCategory = setCategory(null, category, categoryCode);
			this.category.setCategoryName(newCategory.getCategoryName());
			this.category.setParentCategory(newCategory.getParentCategory());
			categoryContainer.addCategory(newCategory);
			createCategoryWizard.clickNextInDialog();
			createCategoryWizard.enterAttributeLongText(newCategory.getAttrLongTextValue(), newCategory.getAttrLongTextName());
			createCategoryWizard.enterAttributeShortText(newCategory.getAttrShortTextValue(), newCategory.getAttrShortTextName());
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Create new category in an existing catalog.
	 *
	 * @param catalogName      a catalog for a new subcategory.
	 * @param categoryInfoList a subcategory info list.
	 */
	@When("^I create category for (.*)$")
	public void createCategoryForCatalog(final String catalogName, final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			catalogManagement.selectCatalog(catalogName);
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			String categoryCode = Utility.getRandomUUID();
			createCategoryWizard.enterCategoryCode(categoryCode);
			createCategoryWizard.enterCategoryName(category.getCategoryName() + DASH + categoryCode);
			createCategoryWizard.selectCategoryType(this.categoryType.getCategoryTypeName());
			createCategoryWizard.enterEnableDateTime(Integer.valueOf(category.getEnableDateTime()));
			if (!category.getDisableDateTime().equals(EMPTY_STRING)) {
				createCategoryWizard.enterDisableDateTime(Integer.valueOf(category.getDisableDateTime()));
			}
			if (TRUE.equalsIgnoreCase(category.getStoreVisible())) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			Category newCategory = setCategory(null, category, categoryCode);
			categoryContainer.addCategory(newCategory);
			createCategoryWizard.clickNextInDialog();
			createCategoryWizard.enterAttributeShortText(newCategory.getAttrShortTextValue(),
					attributeContainer.getAttributeNameByPartialCodeAndLanguage(newCategory.getAttrShortTextName(), ENGLISH));
			createCategoryWizard.enterAttributeLongText(newCategory.getAttrLongTextValue(),
					attributeContainer.getAttributeNameByPartialCodeAndLanguage(newCategory.getAttrLongTextName(), ENGLISH));
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * verifies that newly created subcategory is present in a Catalog under Category
	 */
	@When("^newly created subcategory is present in catalog (.+) under category (.+)")
	public void createNewSubcategoryForExistingCatalog(final String catalogName, final String parentCategoryName) {
		catalogManagement.expandCategoryAndVerifySubcategory(catalogName, parentCategoryName, this.category.getCategoryName());
	}

	/**
	 * Create new category for newly created catalog.
	 *
	 * @param categoryInfoList the category info list.
	 */
	@When("^I create new category for newly created catalog with the following data")
	public void createNewCategoryForNewlyCreatedCatalog(final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			selectNewCatalog();
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			this.category.setCategoryCode(Utility.getRandomUUID());
			createCategoryWizard.enterCategoryCode(this.category.getCategoryCode());
			this.category.setCategoryName(category.getCategoryName() + DASH + this.category.getCategoryCode());
			createCategoryWizard.enterCategoryName(this.category.getCategoryName());
			this.category.setCategoryType(category.getCategoryType());
			createCategoryWizard.selectCategoryType(this.category.getCategoryType());
			createCategoryWizard.enterCurrentEnableDateTime();
			if (category.getStoreVisible().equalsIgnoreCase(TRUE)) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			clickNextButtonCreateCategory();
			createCategoryWizard.enterAttributeLongText(category.getAttrLongTextValue(), category.getAttrLongTextName());
			createCategoryWizard.enterAttributeDecimalValue(category.getAttrDecimalValue(), category.getAttrDecimalName());
			createCategoryWizard.enterAttributeShortText(category.getAttrShortTextValue(), category.getAttrShortTextName());
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Add new linked category to existing virtual catalog.
	 *
	 * @param virtualCatalog         the virtual catalog.
	 * @param linkedCategoryInfoList the linked category info list.
	 */
	@When("^I add new linked category to virtual catalog (.+) with following data")
	public void addLinkedCategoryToVirtualCatalog(final String virtualCatalog, final List<String> linkedCategoryInfoList) {
		selectExistingCatalog(virtualCatalog);
		addLinkedCategory(linkedCategoryInfoList.get(0), linkedCategoryInfoList.get(1));
	}

	/**
	 * Add new linked category to newly created virtual catalog.
	 *
	 * @param linkedCategoryInfoList the linked category info list.
	 */
	@When("^I add new linked category to virtual catalog with following data")
	public void addLinkedCategoryToVirtualCatalog(final List<String> linkedCategoryInfoList) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(linkedCategoryInfoList.get(1));
		selectExistingCatalog(this.catalog.getCatalogName());
		addLinkedCategory(linkedCategoryInfoList.get(0), fullName);
	}

	/**
	 * Add new linked category to existing virtual catalog.
	 *
	 * @param virtualCatalogName     existing virtual catalog code.
	 * @param linkedCategoryInfoList the linked category info list.
	 */
	@When("^I add new linked category to existing virtual catalog (.+) with following data")
	public void addLinkedCategoryExistingVirtualCatalog(final String virtualCatalogName, final List<String> linkedCategoryInfoList) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(linkedCategoryInfoList.get(1));
		selectExistingCatalog(virtualCatalogName);
		addLinkedCategory(linkedCategoryInfoList.get(0), fullName);
	}

	/**
	 * Adds linked category in pre-selected virtual catalog.
	 *
	 * @param masterCatalogName master catalog name.
	 * @param categoryName      category name.
	 */
	private void addLinkedCategory(final String masterCatalogName, final String categoryName) {
		boolean isSelected = false;
		int repetition = 0;
		categoryFinderDialog = catalogManagement.clickAddLinkedCategoryIcon();
		categoryFinderDialog.selectCatalog(masterCatalogName);
		categoryFinderDialog.enterCategoryName(categoryName);
		while (!isSelected && repetition < 5) {
			try {
				repetition++;
				categoryFinderDialog.clickSearchButton();
				categoryFinderDialog.selectCategory(categoryName);
				isSelected = true;
			} catch (TimeoutException e) {
				LOGGER.log(Level.WARN, "Trying to find and select category. Attempt #: " + repetition);
			}
		}
		categoryFinderDialog.clickOK();
	}

	/**
	 * Create new linked category for existing catelog.
	 *
	 * @param catalog                the catelog.
	 * @param linkedCategoryInfoList the linked category info list.
	 */
	@When("^I add new linked category with changeset to (.+) with following data")
	public void createNewLinkedCategoryChangeset(final String catalog, final List<String> linkedCategoryInfoList) {
		selectExistingCatalog(catalog);
		changeSetActionToolbar.clickAddItemToChangeSet();
		this.linkedCategory.setCatalog(catalog);
		selectExistingCatalog(catalog);
		categoryFinderDialog = catalogManagement.clickAddLinkedCategoryIcon();
		this.linkedCategory.setMasterCatalog(linkedCategoryInfoList.get(0));
		categoryFinderDialog.selectCatalog(this.linkedCategory.getMasterCatalog());
		this.linkedCategory.setLinkedCategoryName(linkedCategoryInfoList.get(1));
		categoryFinderDialog.enterCategoryName(this.linkedCategory.getLinkedCategoryName());
		categoryFinderDialog.clickSearchButton();
		categoryFinderDialog.selectCategory(this.linkedCategory.getLinkedCategoryName());
		categoryFinderDialog.clickOK();
		selectCategory(catalog, this.linkedCategory.getLinkedCategoryName());
		changeSetActionToolbar.clickAddItemToChangeSet();
	}

	/**
	 * Create enw global attribute.
	 *
	 * @param globalAttributeName the global attribute name.
	 * @param usage               the usage.
	 * @param type                the type.
	 * @param required            required flag.
	 */
	@When("^I create a new global attribute with name (.+) for (.+) of type (.+) with required (.+)$")
	public void createNewGlobalAttribute(final String globalAttributeName, final String usage, final String type, final boolean required) {
		this.globalAttributeName = globalAttributeName + "_" + Utility.getRandomUUID();
		editGlobalAttributesDialog = catalogManagementActionToolbar.clickEditGlobalAttributesButton();
		addAttributeDialog = editGlobalAttributesDialog.clickAddAttributeButton();
		addAttributeDialog.enterAttributeKey("GA_" + Utility.getRandomUUID());
		addAttributeDialog.enterAttributeName(this.globalAttributeName);
		addAttributeDialog.selectAttributeUsage(usage);
		addAttributeDialog.selectAttributeType(type);

		if (required) {
			addAttributeDialog.clickRequiredAttributeCheckBox();
		}

		addAttributeDialog.clickAddButton();
		editGlobalAttributesDialog.clickSaveButton();
	}

	/**
	 * verify newly created global attribute exists.
	 */
	@Then("^newly (?:created|edited) global attribute is in the list$")
	public void verifyNewlyCreatedGlobalAttributeExists() {
		editGlobalAttributesDialog = catalogManagementActionToolbar.clickEditGlobalAttributesButton();
		editGlobalAttributesDialog.verifyGlobalAttributeValue(this.globalAttributeName);
	}

	/**
	 * verify newly created global attribute exists.
	 */
	@When("^I edit newly created global attribute name to (.+)$")
	public void editCreatedGlobalAttribute(final String newGlobalAttributeName) {
		editGlobalAttributesDialog.selectGlobalAttributeRow(this.globalAttributeName);
		editAttributeDialog = editGlobalAttributesDialog.clickEditAttributeButton();
		this.globalAttributeName = newGlobalAttributeName + " " + Utility.getRandomUUID();
		editAttributeDialog.enterAttributeName(this.globalAttributeName);
		editAttributeDialog.clickOKButton();
		editGlobalAttributesDialog.clickSaveButton();
	}

	/**
	 * Select new global attribute.
	 */
	@And("^I select newly (?:created|edited) global attribute in the list$")
	public void selectNewGlobalAttribute() {
		editGlobalAttributesDialog.selectGlobalAttributeRow(this.globalAttributeName);
	}

	/**
	 * Delete new global attribute.
	 */
	@And("^I delete newly created global attribute$")
	public void deleteNewGlobalAttribute() {
		editGlobalAttributesDialog.deleteGlobalAttribute();
		editGlobalAttributesDialog.clickSaveButton();
	}

	/**
	 * Verify newly created global attribute is deleted.
	 */
	@And("^I verify newly created global attribute is deleted$")
	public void verifyNewlyCreatedGlobalAttributeIsDeleted() {
		editGlobalAttributesDialog = catalogManagementActionToolbar.clickEditGlobalAttributesButton();
		editGlobalAttributesDialog.verifyGlobalAttributeValueIsNotInList(this.globalAttributeName);
	}

	/**
	 * Verify create catalog button is present.
	 */
	@And("^I can view Create Catalog button")
	public void verifyCreateCatalogButtonIsPresent() {
		catalogManagementActionToolbar.verifyCreateCatalogButtonIsPresent();
	}

	/**
	 * Open newly created catalog.
	 */
	@And("^I open the newly created catalog editor")
	public void openNewCatalogEditor() {
		selectNewCatalog();
		catalogEditor = catalogManagement.clickOpenCatalogCategoryButton();
	}


	/**
	 * Open selected catalog.
	 */
	@And("^I open the selected catalog")
	public void openSelectedCatalog() {
		catalogEditor = catalogManagement.clickOpenCatalogCategoryButton();
	}

	/**
	 * Select editor tab for created catalog.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I select (.+) tab in the Catalog Editor$")
	public void selectCatalogEditorTab(final String tabName) {
		openNewCatalogEditor();
		selectCatalogEditorTabOpenedCatalog(tabName);
	}

	@When("^I select (.+) tab in the Catalog Editor of currently open Catalog$")
	public void selectCatalogEditorTabOpenCatalog(final String tabName) {
		selectCatalogEditorTabOpenedCatalog(tabName);
	}

	/**
	 * Select editor tab for opened in previous steps catalog.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I select (.+) tab in the Catalog Editor for opened Catalog$")
	public void selectCatalogEditorTabOpenedCatalog(final String tabName) {
		if (catalogEditor == null) {
			catalogEditor = new CatalogEditor(driver);
		}
		catalogEditor.selectTab(tabName);
	}

	/**
	 * Open editor for specified catalog and select specified editor tab.
	 *
	 * @param tabName a name of a tab which should be selected in catalog editor
	 * @param catalog catalog name which should be opened in catalog editor
	 */
	@When("^I select (.+) tab in a catalog editor for catalog (.+)$")
	public void selectCatalogEditorTabSpecifiedCatalog(final String tabName, final String catalog) {

		selectExistingCatalog(catalog);
		openSelectedCatalog();
		if (catalogEditor == null) {
			catalogEditor = new CatalogEditor(driver);
		}
		catalogEditor.selectTab(tabName);
	}

	/**
	 * Verify catalog attribute.
	 *
	 * @param attributeValueList the attribute value list.
	 */
	@And("^it should have following catalog attributes?$")
	public void verifyCatalogAttribute(final List<String> attributeValueList) {
		for (String attributeValue : attributeValueList) {
			catalogEditor.verifyCatalogAttributeValue(attributeValue);
		}
	}

	/**
	 * Create new subcategory for an existing category in an existing catalog.
	 *
	 * @param categoryName     a parent category for a new subcategory
	 * @param catalogName      a catalog for a new subcategory.
	 * @param categoryInfoList a subcategory info list.
	 */
	@When("^I create subcategory for category (.+) in catalog (.+) with following data")
	public void createSubcategoryForExistingCatalog(final String categoryName, final String catalogName,
													final List<Category> categoryInfoList) {
		for (Category subcategory : categoryInfoList) {
			String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
			String categoryCode = Utility.getRandomUUID();
			catalogManagement.expandCatalog(catalogName);
			String parent = categoryContainer.getCategoryMap().get(fullName).getParentCategory();
			if (parent != null) {
				catalogManagement.expandCatalog(catalogName);
				catalogManagement.expandCategoryAndVerifySubcategory(catalogName, categoryContainer.getFullCategoryNameByPartialName(parent),
						fullName);
			}
			catalogManagement.verifyCategoryExists(fullName);
			createCategoryWizard = catalogManagement.clickCreateSubcategoryIcon();
			createCategoryWizard.enterCategoryCode(categoryCode);
			createCategoryWizard.enterCategoryName(subcategory.getCategoryName() + DASH + categoryCode);
			createCategoryWizard.selectCategoryType(categoryType.getCategoryTypeName());
			createCategoryWizard.enterEnableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(subcategory.getEnableDateTime())));
			if (!subcategory.getDisableDateTime().equals(EMPTY_STRING)) {
				createCategoryWizard.enterDisableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(subcategory.getDisableDateTime())));
			}
			if (TRUE.equalsIgnoreCase(subcategory.getStoreVisible())) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			Category newCategory = setCategory(categoryName, subcategory, categoryCode);
			categoryContainer.addCategory(newCategory);
			this.category.setCategoryName(newCategory.getCategoryName());
			this.category.setParentCategory(newCategory.getParentCategory());
			clickNextButtonCreateCategory();
			String attrLongTextNameEng = Optional.of(attributeContainer.getAttributeNameByPartialCodeAndLanguage
					(subcategory.getAttrLongTextValue(), ENGLISH))
					.filter(StringUtils::isNotEmpty)
					.orElse(subcategory.getAttrLongTextName());
			createCategoryWizard.enterAttributeLongText(newCategory.getAttrLongTextValue(), attrLongTextNameEng);
			String attrShortTextNameEng = Optional.of(attributeContainer.getAttributeNameByPartialCodeAndLanguage
					(subcategory.getAttrShortTextValue(), ENGLISH))
					.filter(StringUtils::isNotEmpty)
					.orElse(subcategory.getAttrShortTextName());
			createCategoryWizard.enterAttributeShortText(newCategory.getAttrShortTextValue(), attrShortTextNameEng);
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Create new subcategory for an existing category in an existing catalog.
	 *
	 * @param categoryName     a parent category for a new subcategory
	 * @param catalogName      a catalog for a new subcategory.
	 * @param categoryInfoList a subcategory info list.
	 */
	@When("^I create subcategory for category (.+) in catalog (.+) with following values")
	public void createNewSubcategoryForExistingCatalogWithValues(final String categoryName, final String catalogName,
																 final List<Category> categoryInfoList) {
		for (Category subcategory : categoryInfoList) {
			String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
			String categoryCode = Utility.getRandomUUID();
			catalogManagement.expandCatalog(catalogName);
			String parent = categoryContainer.getCategoryMap().get(fullName).getParentCategory();
			if (parent != null) {
				catalogManagement.expandCatalog(catalogName);
				catalogManagement.expandCategoryAndVerifySubcategory(catalogName, categoryContainer.getFullCategoryNameByPartialName(parent),
						fullName);
			}
			catalogManagement.verifyCategoryExists(fullName);
			createCategoryWizard = catalogManagement.clickCreateSubcategoryIcon();
			createCategoryWizard.enterCategoryCode(categoryCode);
			createCategoryWizard.enterCategoryName(subcategory.getCategoryName() + DASH + categoryCode);
			createCategoryWizard.selectCategoryType(categoryType.getCategoryTypeName());
			if (DATE_PATTERN.matcher(subcategory.getEnableDateTime()).matches()) {
				createCategoryWizard.enterEnableDateTime(subcategory.getEnableDateTime());
			} else {
				createCategoryWizard.enterEnableDateTime(Integer.valueOf(subcategory.getEnableDateTime()));
			}
			if (!subcategory.getDisableDateTime().equals(EMPTY_STRING)) {
				if (DATE_PATTERN.matcher(subcategory.getDisableDateTime()).matches()) {
					createCategoryWizard.enterDisableDateTime(subcategory.getDisableDateTime());
				} else {
					createCategoryWizard.enterDisableDateTime(Integer.valueOf(subcategory.getDisableDateTime()));
				}
			}
			if (TRUE.equalsIgnoreCase(subcategory.getStoreVisible())) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			Category newCategory = setCategory(categoryName, subcategory, categoryCode);
			categoryContainer.addCategory(newCategory);
			clickNextButtonCreateCategory();
			createCategoryWizard.enterShortTextAttribute(newCategory.getAttrShortTextValue(),
					attributeContainer.getAttributeNameByPartialCodeAndLanguage(newCategory.getAttrShortTextName(), ENGLISH));
			createCategoryWizard.enterLongTextAttribute(newCategory.getAttrLongTextValue(),
					attributeContainer.getAttributeNameByPartialCodeAndLanguage(newCategory.getAttrLongTextName(), ENGLISH));
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * verify newly created catalog attribute exists.
	 */
	@Then("^(?:newly created|edited) catalog attribute is in the list$")
	public void verifyNewlyCreatedCatalogAttributeExists() {
		catalogEditor.verifyCatalogAttributeValue(this.catalog.getAttributeName());
	}

	/**
	 * Create new catalog attribute.
	 *
	 * @param catalogInfoList Catalog attribute details.
	 */
	@When("^I create a new catalog attribute with following details$")
	public void createNewCatalogAttribute(final List<Catalog> catalogInfoList) {
		for (Catalog catalog : catalogInfoList) {
			this.catalog.setAttributeName(catalog.getAttributeName() + "_" + Utility.getRandomUUID());
			this.catalog.setAttributeUsage(catalog.getAttributeUsage());
			this.catalog.setAttributeType(catalog.getAttributeType());
			this.catalog.setAttributeRequired(catalog.isAttributeRequired());
			this.catalog.setAttributeKey("CA_" + Utility.getRandomUUID());

			addAttributeDialog = catalogEditor.clickAddAttributeButton();

			addAttributeDialog.enterAttributeKey(this.catalog.getAttributeKey());
			addAttributeDialog.enterAttributeName(this.catalog.getAttributeName());
			addAttributeDialog.selectAttributeUsage(this.catalog.getAttributeUsage());
			addAttributeDialog.selectAttributeType(this.catalog.getAttributeType());

			if (this.catalog.isAttributeRequired()) {
				addAttributeDialog.clickRequiredAttributeCheckBox();
			}

			addAttributeDialog.clickAddButton();
			catalogManagementActionToolbar.saveAll();
		}
	}

	/**
	 * Delete new catalog.
	 */
	@When("^I close the editor and try to delete the newly created catalog$")
	public void deleteNewCatalogError() {
		catalogManagement.closePane(this.catalog.getCatalogName());
		selectNewCatalog();
		catalogManagement.rightClickDelete();
	}

	/**
	 * Delete new catalog.
	 */
	@When("^I close the editor and delete the newly created catalog$")
	public void deleteNewCatalog() {
		catalogManagement.closePane(this.catalog.getCatalogName());
		selectNewCatalog();
		catalogManagement.rightClickDelete().clickOK();
	}

	/**
	 * Delete new catalog attribute.
	 */
	@When("^I delete the newly created catalog attribute$")
	public void deleteNewCatalogAttribute() {
		catalogEditor.selectTab("Attributes");
		catalogEditor.selectCatalogAttributeValue(this.catalog.getAttributeName());
		catalogEditor.clickRemoveAttributeButton();
		new ConfirmDialog(driver).clickOKButton("CatalogMessages.CatalogAttributesSection_RemoveDialog");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify newly created catalog attribute is deleted.
	 */
	@Then("^the newly created catalog attribute is deleted$")
	public void verifyNewAttributeDelete() {
		catalogEditor.verifyCatalogAttributeDelete(this.catalog.getAttributeName());
	}

	/**
	 * Edit catalog attribute name.
	 */
	@When("^I edit the catalog attribute name$")
	public void editCatalogAttributeName() {
		catalogEditor.selectCatalogAttributeValue(this.catalog.getAttributeName());
		this.catalog.setAttributeName("Edit Prod Desc" + "_" + Utility.getRandomUUID());
		editAttributeDialog = catalogEditor.clickEditAttributeButton();
		editAttributeDialog.enterAttributeName(this.catalog.getAttributeName());
		editAttributeDialog.clickOKButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Edit virtual catalog name.
	 */
	@When("^I edit the virtual catalog name to a different name$")
	public void editVirtualCatalogName() {
		createEditVirtualCatalogDialog = catalogManagement.clickOpenVirtualCatalogButton();
		String virtualCatalogCode = Utility.getRandomUUID();
		this.catalog.setCatalogName(catalog.getCatalogName() + DASH + virtualCatalogCode);
		createEditVirtualCatalogDialog.enterCatalogName(this.catalog.getCatalogName());
		createEditVirtualCatalogDialog.clickSaveButton();
	}

	/**
	 * Verify virtual catalog tab present in product tabs
	 *
	 * @param product  Product Name.
	 * @param category Category Name.
	 * @param catalog  Catalog Name.
	 */
	@And("^there is an existing product (.+) in category (.+) in virtual catalog (.+)$")
	public void verifyProductExistsInVirtualCatalog(final String product, final String category, final String catalog) {
		virtualProductName = product;
		virtualCategoryName = category;
		virtualCatalogName = catalog;
		activityToolbar.clickCatalogManagementButton();
		catalogManagement.expandCatalogAndVerifyCategory(virtualCatalogName, virtualCategoryName);
		catalogProductListingPane = catalogManagement.doubleClickCategory(virtualCategoryName);
		catalogProductListingPane.verifyProductNameExists(virtualProductName);
	}

	/**
	 * Clicks Exclude Product button.
	 */
	@When("^I exclude the product from the virtual catalog$")
	public void clickExcludeProduct() {
		catalogProductListingPane.clickExcludeProductButton();
	}

	/**
	 * Verify that Product (productCode) does not have Category Assignment.
	 *
	 * @param productCode the Product code.
	 * @param catalogName the catalog name.
	 */
	@Then("^the product with code (.+) does not have (.+) in category assignment and merchandising associations$")
	public void verifyVirtualCatalogNotInProductTabs(final String productCode, final String catalogName) {
		productAndBundleDefinition.searchForProductByCode(productCode);
		productAndBundleDefinition.verifyProductSearchResultCode(productCode);
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(productCode);
		verifyCategoryMerchandisingAssignments(catalogName, false);
	}

	/**
	 * @param catalogName the catalog name.
	 */
	@Then("^the product does not have (.+) in category assignment and merchandising associations$")
	public void verifyVirtualCatalogNotInProductTabs(final String catalogName) {
		productEditor = catalogProductListingPane.selectProductAndOpenProductEditor();
		verifyCategoryMerchandisingAssignments(catalogName, false);
	}

	/**
	 * Verify virtual catalog tab is not present in product tabs
	 *
	 * @param productCode the Product code.
	 * @param catalogName the catalog name.
	 */
	@Then("^the product with code (.+) has (.+) in category assignment and merchandising associations$")
	public void verifyVirtualCatalogInProductTabs(final String productCode, final String catalogName) {
		productAndBundleDefinition.searchForProductByCode(productCode);
		productAndBundleDefinition.verifyProductSearchResultCode(productCode);
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(productCode);
		if ("newly created".equalsIgnoreCase(catalogName)) {
			verifyCategoryMerchandisingAssignments(this.catalog.getCatalogName(), true);
		} else {
			verifyCategoryMerchandisingAssignments(catalogName, true);
		}
		productEditor.closeProductEditor(productCode);
	}

	/**
	 * Verify virtual catalog tab is not present in product tabs
	 *
	 * @param catalogName the catalog name.
	 */
	@Then("^the product has (.+) in category assignment and merchandising associations$")
	public void verifyVirtualCatalogInProductTabs(final String catalogName) {
		productEditor = catalogProductListingPane.selectProductAndOpenProductEditor();
		verifyCategoryMerchandisingAssignments(catalogName, true);
	}

	/**
	 * @param catalogName  the catalog name.
	 * @param isTabPresent switch to verify the tab is present or not.
	 */
	private void verifyCategoryMerchandisingAssignments(final String catalogName, final Boolean isTabPresent) {
		productEditor.selectTab(CATEGORY_ASSIGNMENT);
		if (isTabPresent) {
			catalogManagementActionToolbar.clickReloadActiveEditor();
			int count = 0;
			while (!productEditor.isTabPresent(catalogName) && count < Constants.RETRY_COUNTER_40) {
				sleepReloadAndSelectProduct();
				count++;
			}
			productEditor.verifyCatalogTabIsPresent(catalogName);
		} else {
			int count = 0;
			while (productEditor.isTabPresent(catalogName) && count < Constants.RETRY_COUNTER_40) {
				sleepReloadAndSelectProduct();
				count++;
			}
			productEditor.verifyCatalogTabIsNotPresent(catalogName);
		}
		productEditor.selectTab(MERCHANDISING_ASSIGNMENT);
		if (isTabPresent) {
			productEditor.verifyCatalogTabIsPresent(catalogName);
		} else {
			productEditor.verifyCatalogTabIsNotPresent(catalogName);
		}
	}

	private void sleepReloadAndSelectProduct() {
		try {
			Thread.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			productEditor.selectTab(CATEGORY_ASSIGNMENT);
			catalogManagementActionToolbar.clickReloadActiveEditor();
			catalogProductListingPane.selectLastSelectedProduct();
		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * Clicks Include Product button.
	 */
	@When("^I include the product in the virtual catalog$")
	public void clickProductState() {
		catalogProductListingPane.clickIncludeProductButton();
	}

	/**
	 * Deletes new catalog.
	 */
	@After(value = "@cleanupCatalog", order = Constants.CLEANUP_ORDER_THIRD)
	public void cleanupCatalog() {
		NavigationDefinition navigationDefinition = new NavigationDefinition();
		navigationDefinition.clickCatalogManagement();
		catalogManagement.clickCatalogBrowseTab();
		if (catalogEditor == null) {
			selectNewCatalog();
			deleteNewlyCreatedCatalog();
		} else {

			if (this.catalog.getBrand() != null) {
				selectNewCatalog();
				openSelectedCatalog();
				deleteNewBrand();
				verifyBrandDeleted();
			}
			catalogEditor.selectCatalogEditor(this.catalog.getCatalogName());
			catalogManagement.closePane(this.catalog.getCatalogName());
			activityToolbar.clickCatalogManagementButton();
			catalogManagement.clickCatalogRefreshButton();
			deleteNewlyCreatedCatalog();
		}
	}

	/**
	 * Deletes new product.
	 */
	@After(value = "@cleanupProduct", order = Constants.CLEANUP_ORDER_FIRST)
	public void cleanupProduct() {
		createCatalogDialog = new CreateCatalogDialog(driver);
		createCatalogDialog.closeDialogIfOpened();
		activityToolbar.clickCatalogManagementButton();
		this.productAndBundleDefinition.deleteNewlyCreatedProduct();
		this.productAndBundleDefinition.verifyProductIsDeleted();
		catalogManagement.clickCatalogBrowseTab();
		if (this.productType.getProductTypeName() != null) {
			selectNewCatalog();
			openSelectedCatalog();
			this.productTypesTabDefinition.deleteNewProductType();
			this.productTypesTabDefinition.verifyNewProductTypeDelete();
		}
		if (this.cartItemModifierGroup.getGrouopName() != null) {
			selectNewCatalog();
			openSelectedCatalog();
			this.cartItemModifierGroupTabDefinition.deleteNewGroup();
			this.cartItemModifierGroupTabDefinition.verifyNewGroupDelete();
		}
	}

	/**
	 * Deletes all created products.
	 */
	@After(value = "@cleanupAllProducts", order = Constants.CLEANUP_ORDER_FIRST)
	public void cleanupAllProduct() {
		createCatalogDialog = new CreateCatalogDialog(driver);
		createCatalogDialog.closeDialogIfOpened();
		activityToolbar.clickCatalogManagementButton();
		this.productAndBundleDefinition.deleteAllCreatedProducts();
		this.productAndBundleDefinition.verifyAllProductIsDeleted();
		catalogManagement.clickCatalogBrowseTab();
		if (this.productType.getProductTypeName() != null) {
			selectNewCatalog();
			openSelectedCatalog();
			this.productTypesTabDefinition.deleteNewProductType();
			this.productTypesTabDefinition.verifyNewProductTypeDelete();
		}
		if (this.cartItemModifierGroup.getGrouopName() != null) {
			selectNewCatalog();
			openSelectedCatalog();
			this.cartItemModifierGroupTabDefinition.deleteNewGroup();
			this.cartItemModifierGroupTabDefinition.verifyNewGroupDelete();
		}
	}

	/**
	 * Delete new category and category type.
	 */
	@After(value = "@cleanUpCategory", order = Constants.CLEANUP_ORDER_SECOND)
	public void deleteNewlyCreatedCategory() {
//		Need to select category again before right click delete.
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.selectCategory(this.category.getCategoryName());
		catalogManagement.clickDeleteCategoryIcon();
		new ConfirmDialog(driver).clickOKButton(CATALOG_MESSAGE_DELETE_DIALOG);
		if (this.catalog.getCatalogName() != null) {
			this.categoryTypesTabDefinition.deleteNewCategoryType();
			this.categoryTypesTabDefinition.verifyNewCategoryTypeDelete();
		}
	}

	/**
	 * Delete all categories, that contains in CategoryContainer.
	 */
	@After(value = "@cleanUpAllCategories", order = Constants.CLEANUP_ORDER_SECOND)
	public void deleteAllCreatedCategories() {
		final Map<String, Category> categories = categoryContainer.getCategoryMap();


		categories.values()
				.stream()
				.filter(item -> StringUtils.isNotEmpty(item.getParentCategory()))
				.peek(item -> catalogManagement.expandCategoryAndVerifySubcategory(this.catalog.getCatalogName(), item.getParentCategory(),
						item.getCategoryName()))
				.forEach(this::deleteCategory);

		categories.values()
				.stream()
				.filter(item -> StringUtils.isEmpty(item.getParentCategory()))
				.forEach(this::deleteCategory);

		if (this.catalog.getCatalogName() != null) {
			this.categoryTypesTabDefinition.deleteNewCategoryType();
			this.categoryTypesTabDefinition.verifyNewCategoryTypeDelete();
		}
	}

	/**
	 * Delete category.
	 *
	 * @param categoryForDelete category for delete.
	 */
	private void deleteCategory(final Category categoryForDelete) {
//		Need to select category again before right click delete.
		catalogManagement.selectCategory(categoryForDelete.getCategoryName());
		catalogManagement.clickDeleteCategoryIcon();
		new ConfirmDialog(driver).clickOKButton(CATALOG_MESSAGE_DELETE_DIALOG);
	}

	/**
	 * Resets the virtual catalog product to its default state.
	 */
	@After(value = "@resetVirtualCatalogProduct", order = Constants.CLEANUP_ORDER_FIRST)
	public void resetVirtualCatalogProduct() {
		assertThat(this.virtualProductName)
				.as("It's not possible to reset a virtual product. A virtual product name was not assigned")
				.isNotBlank();
		//any dialog which inherits Abstract dialog can be used here
		addEditBrandDialog = new AddEditBrandDialog(driver, "Edit");
		addEditBrandDialog.closeDialogIfOpened();
		catalogProductListingPane.verifyProductNameExists(this.virtualProductName);
		catalogProductListingPane.clickIncludeProductButtonIfEnabled();
	}

	@After(value = "@cleanupVirtualCatalog", order = Constants.CLEANUP_ORDER_FIRST)
	public void cleanupVritualCatalog() {
		deleteNewlyCreatedCatalog();
		verifyNewlyCreatedCatalogIsDeleted();
	}

	/**
	 * Verifies brand name exists.
	 *
	 * @param brandName String
	 */
	@Then("^the brand (.+) is displayed in the brands table$")
	public void verifyBrandNameExists(final String brandName) {
		catalogEditor.selectBrandsTab();
		brandsTab.verifyAndSelectBrandByName(this.catalog.getBrand());
	}

	/**
	 * Edit the brand name for a given brand code.
	 *
	 * @param newBrandName New brand name.
	 */
	@And("^I edit brand name to (.+) for the newly added brand$")
	public void editBrandNameForCode(final String newBrandName) {
		brandsTab.verifyAndSelectBrand(this.catalog.getBrand());
		addEditBrandDialog = brandsTab.clickEditBrandButton();
		addEditBrandDialog.enterBrandName(newBrandName);
		this.catalog.setBrand(newBrandName);
		addEditBrandDialog.clickAddButton();
		saveChanges();
	}


	/**
	 * Deletes a given brand.
	 *
	 * @param brandName String
	 */
	@And("^I delete brand (.+)$")
	public void deleteBrand(final String brandName) {
		deleteBrandHelper(brandName);
		this.catalog.setBrand(null);
		saveChanges();
	}

	/**
	 * Attempt to delete an existing brand.
	 *
	 * @param brandName String
	 */
	@And("^I attempt to delete an existing brand (.+) used by product$")
	public void deleteExistingBrand(final String brandName) {
		deleteBrandHelper(brandName);
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param errorMessage String
	 */
	@Then("^an error message of (.+) is displayed$")
	public void verifyErrorMessageDisplayed(final String errorMessage) {
		brandsTab.verifyErrorMessageDisplayed(errorMessage);
	}

	/**
	 * Adds a brand with supplied name.
	 *
	 * @param brandName String
	 */
	@When("^I add a brand (.+)$")
	public void addBrand(final String brandName) {
		catalogEditor.selectBrandsTab();
		addEditBrandDialog = brandsTab.clickAddBrandButton();
		String brandUuid = Utility.getRandomUUID();
		this.catalog.setBrand(brandName);
		addEditBrandDialog.enterBrandName(this.catalog.getBrand());
		addEditBrandDialog.enterBrandCode(this.catalog.getBrand() + brandUuid);
		addEditBrandDialog.clickAddButtonNoWait();
		saveChanges();
	}

	/**
	 * Adds an existing brand.
	 *
	 * @param brandName String
	 */
	@When("^I add an existing brand (.+)$")
	public void addExistingBrand(final String brandName) {
		catalogEditor.selectBrandsTab();
		addEditBrandDialog = brandsTab.clickAddBrandButton();
		addEditBrandDialog.enterBrandName(brandName);
		addEditBrandDialog.enterBrandCode(brandName);
		addEditBrandDialog.clickAddButtonNoWait();
	}

	/**
	 * Verifies error message is displayed in the add/edit dialog.
	 *
	 * @param errorMessage String
	 */
	@Then("^an error message of (.+) is displayed in the add dialog$")
	public void verifyErrorMessageDisplayedInAddDialog(final String errorMessage) {
		addEditBrandDialog.verifyErrorMessageDisplayedInAddEditDialog(errorMessage);
	}

	/**
	 * Saves changes in catalog editor.
	 */
	@When("^I save my changes$")
	public void saveChanges() {
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify named brand was deleted.
	 *
	 * @param brandName String
	 */
	@Then("^The brand (.+) is deleted$")
	public void verifyBrandNameDeleted(final String brandName) {
		brandsTab.verifyBrandDelete(brandName);
	}

	/**
	 * Double clicks the category and adds all category items to a change set.
	 *
	 * @param categoryList list of category names
	 */
	@And("^I add all products from the following (?:category|categories) to a changeset$")
	public void addAllCategoryItemsToAChangeSet(final List<String> categoryList) {
		CatalogProductListingPane.setNumberOfCategoryItems(0);
		for (String category : categoryList) {
			catalogProductListingPane = catalogManagement.doubleClickCategory(category);
			catalogProductListingPane.addAllCategoryItemsToAChangeSet();
		}
	}

	/**
	 * Opens catalog management tab
	 */
	@And("^I open catalog management tab$")
	public void clickCatalogManagementTab() {
		catalogManagement.clickCatalogBrowseTab();
	}

	/**
	 * Right-clicks newly created category and adds an existing product to it.
	 */
	@And("^I add existing product (.+) to newly created category$")
	public void rightClickToAddExistingProductToNewCategory(final String productCode) {
		selectNewCategory();
		SelectAProductDialog dialog = catalogManagement.rightClickAddExistingProduct();

		dialog.enterProductCode(productCode);
		dialog.clickSearchButton();
		dialog.selectProductByCode(productCode);
		dialog.clickOKButton();
	}

	/**
	 * Delete brand method.
	 *
	 * @param brandName String
	 */
	private void deleteBrandHelper(final String brandName) {
		catalogEditor.selectBrandsTab();
		brandsTab.verifyAndSelectBrandByName(brandName);
		brandsTab.clickRemoveBrandButton();
	}

	/**
	 * Create category method.
	 *
	 * @param catalogName      name of catalog.
	 * @param categoryInfoList category info.
	 */
	@When("^I create category of the category type and required fields for (.*)$")
	public void createCategoryWithRequiredFieldsForCatalog(final String catalogName, final List<Category> categoryInfoList) {
		final Category newCategory = categoryInfoList.get(0);
		catalogManagement.selectCatalog(catalogName);
		createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
		final String categoryCode = Utility.getRandomUUID();
		createCategoryWizard.enterCategoryCode(categoryCode);
		createCategoryWizard.enterCategoryName(newCategory.getCategoryName() + DASH + categoryCode);
		createCategoryWizard.selectCategoryType(this.categoryType.getCategoryTypeName());
		final String enableDateTime = Utility.getDateTimeWithPlus(Integer.valueOf(newCategory.getEnableDateTime()));
		final String disableDateTime = Utility.getDateTimeWithPlus(Integer.valueOf(newCategory.getDisableDateTime()));
		createCategoryWizard.enterEnableDateTime(enableDateTime);
		createCategoryWizard.enterDisableDateTime(disableDateTime);
		if (newCategory.getStoreVisible().equalsIgnoreCase(TRUE)) {
			createCategoryWizard.checkStoreVisibleBox();
		}
		category.setCategoryName(newCategory.getCategoryName() + DASH + categoryCode);
		createCategoryWizard.clickNextInDialog();
		createCategoryWizard.enterAttributeLongText(newCategory.getAttrLongTextValue(), newCategory.getAttrLongTextName());
		createCategoryWizard.clickFinish();
		category.setCategoryCode(categoryCode);
		category.setEnableDateTime(enableDateTime);
		category.setDisableDateTime(disableDateTime);
	}


	/**
	 * Create category method.
	 *
	 * @param code            atribute code.
	 * @param catalogInfoList catalog info.
	 */
	@When("^I create a new catalog attribute with code (.*) with following details$")
	public void createNewCatalogAttribute(final String code, final Map<String, String> catalogInfoList) {
		final Attribute attribute = new Attribute();
		addAttributeDialog = catalogEditor.clickAddAttributeButton();
		final String attributeCode = code + Utility.getRandomUUID();
		attribute.setAttributeUsage(catalogInfoList.get("attributeUsage"));
		attribute.setAttributeType(catalogInfoList.get("attributeType"));
		attribute.setAttributeRequired(Boolean.parseBoolean(catalogInfoList.get("attributeRequired")));
		attribute.setAttributeKey(attributeCode);
		addAttributeDialog.enterAttributeKey(attributeCode);
		for (Map.Entry<String, String> localizedName : catalogInfoList.entrySet()) {
			if (addAttributeDialog.selectLanguage(localizedName.getKey())) {
				this.addAttributeDialog.enterAttributeName(attributeCode + localizedName.getValue());
				attribute.setName(localizedName.getKey(), attributeCode + localizedName.getValue());
			}
		}
		addAttributeDialog.selectAttributeType(attribute.getAttributeType());
		addAttributeDialog.selectAttributeUsage(attribute.getAttributeUsage());
		if (attribute.isAttributeRequired()) {
			addAttributeDialog.clickCheckBox("Required Attribute");
		}
		this.attributeContainer.addAtribute(attribute);
		addAttributeDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
	}

	/**
	 * Create new category with parameters from given category.
	 *
	 * @param categoryName name of the category.
	 * @param subcategory  name of the subcategory.
	 * @param categoryCode category code.
	 * @return new Category.
	 */
	private Category setCategory(final String categoryName, final Category subcategory, final String categoryCode) {
		Category newCategory = new Category();
		if (DATE_PATTERN.matcher(subcategory.getEnableDateTime()).matches()) {
			newCategory.setEnableDateTime(subcategory.getEnableDateTime());
		} else {
			newCategory.setEnableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(subcategory.getEnableDateTime())));
		}
		if (!subcategory.getDisableDateTime().equals(EMPTY_STRING)) {
			if (DATE_PATTERN.matcher(subcategory.getDisableDateTime()).matches()) {
				newCategory.setDisableDateTime(subcategory.getDisableDateTime());
			} else {
				newCategory.setDisableDateTime(Utility.getDateTimeWithPlus(Integer.valueOf(subcategory.getDisableDateTime())));
			}
		}
		newCategory.setCategoryName(subcategory.getCategoryName() + DASH + categoryCode);
		newCategory.setCategoryType(categoryType.getCategoryTypeName());
		newCategory.setCategoryCode(categoryCode);
		newCategory.setStoreVisible(String.valueOf(Boolean.valueOf(subcategory.getStoreVisible())));
		if (categoryName != null) {
			newCategory.setParentCategory(categoryContainer.getFullCategoryNameByPartialName(categoryName));
		}
		String attrLongTextNameEng = Optional.of(attributeContainer.getAttributeKeyByPartialCode(
				subcategory.getAttrLongTextName()))
				.filter(StringUtils::isNotEmpty)
				.orElse(subcategory.getAttrLongTextName());
		newCategory.setAttrLongTextName(attrLongTextNameEng);
		newCategory.setAttrLongTextValue(subcategory.getAttrLongTextValue());
		String attrShortTextNameEng = Optional.of(attributeContainer.getAttributeKeyByPartialCode(
				(subcategory.getAttrShortTextName())))
				.filter(StringUtils::isNotEmpty)
				.orElse(subcategory.getAttrShortTextName());
		newCategory.setAttrShortTextName(attrShortTextNameEng);
		newCategory.setAttrShortTextValue(subcategory.getAttrShortTextValue());
		newCategory.setName(ENGLISH, newCategory.getCategoryName());
		newCategory.setName(FRENCH, newCategory.getCategoryName());
		return newCategory;
	}

	private Category findCategory(final String categoryName) {
		String fullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		Category category = categoryContainer.getCategoryMap().get(fullName);
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.clickCatalogRefreshButton();
		return category;
	}
}
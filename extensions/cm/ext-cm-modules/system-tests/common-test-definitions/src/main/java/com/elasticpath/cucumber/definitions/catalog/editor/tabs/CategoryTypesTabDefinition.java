package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddEditCategoryTypeDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditLongTextAttributeDialog;
import com.elasticpath.selenium.dialogs.SelectAFeaturedProductDialog;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.Catalog;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.CategoryType;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.domainobjects.containers.ProductContainer;
import com.elasticpath.selenium.editor.CategoryEditor;
import com.elasticpath.selenium.editor.catalog.tabs.CategoryTypesTab;
import com.elasticpath.selenium.navigations.CatalogManagement;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.CreateCategoryWizard;

/**
 * Category Types Tab Definitions.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class CategoryTypesTabDefinition {

	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
	private final CategoryTypesTab categoryTypesTab;
	private AddEditCategoryTypeDialog addEditCategoryTypeDialog;
	private EditLongTextAttributeDialog editLongTextAttributeDialog;
	private final CatalogManagement catalogManagement;
	private CategoryEditor categoryEditor;
	private CreateCategoryWizard createCategoryWizard;
	private final Catalog catalog;
	private String categoryCode;
	private final Category category;
	private final CategoryType categoryType;
	private final CategoryContainer categoryContainer;
	private final ProductContainer productContainer;
	private SelectAFeaturedProductDialog selectAFeaturedProductDialog;
	private final AttributeContainer attributeContainer;

	public CategoryType getCategoryType() {
		return categoryType;
	}

	/**
	 * Constructor.
	 *
	 * @param catalog            the catalog object to be used in current session.
	 * @param category           the category object to be used in current session.
	 * @param categoryType       Category Type object.
	 * @param categoryContainer  CategoryContainer object.
	 * @param productContainer   ProductContainer object.
	 * @param attributeContainer Attribute Container object.
	 */
	public CategoryTypesTabDefinition(final Catalog catalog, final Category category, final CategoryType categoryType,
									  final CategoryContainer categoryContainer, final ProductContainer productContainer,
									  final AttributeContainer attributeContainer) {
		this.productContainer = productContainer;
		final WebDriver driver = SetUp.getDriver();
		this.categoryTypesTab = new CategoryTypesTab(driver);
		this.catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		catalogManagement = new CatalogManagement(driver);
		this.catalog = catalog;
		this.category = category;
		this.categoryEditor = new CategoryEditor(driver);
		this.categoryType = categoryType;
		this.categoryContainer = categoryContainer;
		this.attributeContainer = attributeContainer;
	}

	/**
	 * Create category type.
	 *
	 * @param categoryTypeName the category type name
	 * @param attributeList    the list of attributes
	 */
	@When("^I create a new category type (.*) with following attributes?$")
	public void createCategoryType(final String categoryTypeName, final List<String> attributeList) {
		addEditCategoryTypeDialog = categoryTypesTab.clickAddCategoryTypeButton();
		this.categoryType.setCategoryTypeName(categoryTypeName + Utility.getRandomUUID());
		addEditCategoryTypeDialog.enterCategoryTypeName(this.categoryType.getCategoryTypeName());
		this.categoryType.setAttribute(attributeList);
		for (String attribute : this.categoryType.getAttribute()) {
			addEditCategoryTypeDialog.selectAvailableAttribute(attribute);
			addEditCategoryTypeDialog.clickMoveRightButton();
			addEditCategoryTypeDialog.verifyAssignedAttribute(attribute);
		}
		addEditCategoryTypeDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Create category type.
	 *
	 * @param categoryTypeName the category type name
	 * @param attributeList    the list of attributes
	 */
	@When("^I create a category type (.*) with following attributes?$")
	public void createNewCategoryType(final String categoryTypeName, final List<String> attributeList) {
		addEditCategoryTypeDialog = categoryTypesTab.clickAddCategoryTypeButton();
		this.categoryType.setCategoryTypeName(categoryTypeName + Utility.getRandomUUID());
		addEditCategoryTypeDialog.enterCategoryTypeName(this.categoryType.getCategoryTypeName());
		List<String> newAttributeList = new ArrayList<>();
		for (String s : attributeList) {
			newAttributeList.add(this.attributeContainer.getAttributeKeyByPartialCode(s));
		}
		this.categoryType.setAttribute(newAttributeList);
		for (String attribute : this.categoryType.getAttribute()) {
			String attributeName = this.attributeContainer.getAttributeNameByPartialCodeAndLanguage(attribute, "English");
			addEditCategoryTypeDialog.selectAvailableAttribute(attributeName);
			addEditCategoryTypeDialog.clickMoveRightButton();
			addEditCategoryTypeDialog.verifyAssignedAttribute(attributeName);
		}
		addEditCategoryTypeDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Create new category for existing catelog.
	 *
	 * @param categoryInfoList the category info list.
	 */
	@When("^I create new category of the new category type")
	public void createNewCategoryforNewCatalog(final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			catalogManagement.selectCatalog(this.catalog.getCatalogName());
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			categoryCode = Utility.getRandomUUID();
			createCategoryWizard.enterCategoryCode(categoryCode);
			this.category.setCategoryName(category.getCategoryName() + " - " + categoryCode);
			createCategoryWizard.enterCategoryName(this.category.getCategoryName());
			createCategoryWizard.selectCategoryType(this.categoryType.getCategoryTypeName());
			createCategoryWizard.enterCurrentEnableDateTime();
			if (category.getStoreVisible().equalsIgnoreCase("true")) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			createCategoryWizard.clickNextInDialog();
			createCategoryWizard.enterAttributeLongText(category.getAttrLongTextValue(), category.getAttrLongTextName());
			createCategoryWizard.enterAttributeShortText(category.getAttrShortTextValue(), category.getAttrShortTextName());
			createCategoryWizard.clickFinish();
		}
	}


	@When("^I create category of the category type for (.*)")
	public void createCategoryForCatalog(final String catalogName, final List<Category> categoryInfoList) {
		for (Category category : categoryInfoList) {
			catalogManagement.selectCatalog(catalogName);
			createCategoryWizard = catalogManagement.clickCreateCategoryIcon();
			categoryCode = Utility.getRandomUUID();
			createCategoryWizard.enterCategoryCode(categoryCode);
			this.category.setCategoryName(category.getCategoryName() + " - " + categoryCode);
			createCategoryWizard.enterCategoryName(this.category.getCategoryName());
			createCategoryWizard.selectCategoryType(this.categoryType.getCategoryTypeName());
			createCategoryWizard.enterCurrentEnableDateTime();
			if (category.getStoreVisible().equalsIgnoreCase("true")) {
				createCategoryWizard.checkStoreVisibleBox();
			}
			createCategoryWizard.clickNextInDialog();
			createCategoryWizard.enterAttributeLongText(category.getAttrLongTextValue(), category.getAttrLongTextName());
			createCategoryWizard.clickFinish();
		}
	}

	/**
	 * Open new category editor.
	 */
	@When("^I open newly created category in editor$")
	public void openNewCategoryEditor() {
		catalogManagement.selectCategory(this.category.getCategoryName());
		categoryEditor = catalogManagement.clickOpenCategoryIcon();
	}

	/**
	 * Select editor tab.
	 *
	 * @param tabName the tab name.
	 */
	@And("^I select editor's (.+) tab$")
	public void selectEditorTab(final String tabName) {
		categoryEditor.selectTab(tabName);
	}

	/**
	 * Verify category attribute.
	 *
	 * @param attributeValueList the attribute value list.
	 */
	@And("^it should have following category attributes?$")
	public void verifyCategoryAttribute(final List<String> attributeValueList) {
		for (String attributeValue : attributeValueList) {
			categoryEditor.verifyAttributeValue(attributeValue);
		}
	}

	/**
	 * Open Category Editor.
	 *
	 * @param categoryName the category name.
	 */
	@When("^I edit category (.+) in editor$")
	public void openCategoryEditor(final String categoryName) {
		catalogManagement.selectCategory(categoryName);
		categoryEditor = catalogManagement.clickOpenCategoryIcon();
	}

	/**
	 * Verify Category exists.
	 */
	@Then("^the newly created category with new category type exists$")
	public void verifyCategoryExists() {
		catalogManagement.verifyCategoryExists(this.category.getCategoryName());
		openCategoryEditor(this.category.getCategoryName());
		categoryEditor.verifyCategoryTypeValue(this.categoryType.getCategoryTypeName());
		catalogManagement.closePane(categoryCode);
	}

	/**
	 * Add two featured products to opened category.
	 *
	 * @param productPartialCode partial code for first product.
	 */
	@When("^I add (.+) as the featured product to opened category")
	public void addFeaturedProductToCategory(final String productPartialCode) {
		selectAFeaturedProductDialog = categoryEditor.clickAddFeaturedProductButton(this.category.getCategoryName());
		final String fullProductName = productContainer.getProductByPartialName(productPartialCode).getProductName();

		selectAFeaturedProductDialog.enterProductName(fullProductName);
		selectAFeaturedProductDialog.clickSearchButton();
		selectAFeaturedProductDialog.selectProductByName(fullProductName);
		selectAFeaturedProductDialog.clickOKButton();

		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Removes all feature products in previously opened category editor.
	 */
	@When("^I remove all feature products for opened category")
	public void removeAllFeatureProducts() {
		categoryEditor.removeAllFeaturedProducts();
	}

	/**
	 * Moves featured product up in product list.
	 *
	 * @param productPartialName partial name of the product.
	 * @param count              how many times move the featured product up.
	 */
	@When("^I move (.+) featured product up (\\d+) times in product list in opened category$")
	public void changeOrderOfFeaturedProducts(final String productPartialName, final Integer count) {
		for (int i = 0; i < count; i++) {
			categoryEditor.moveUpFeaturedProductByName(productContainer.getProductByPartialName(productPartialName).getProductName());
		}

		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Remove product from featured product list.
	 *
	 * @param productPartialName partial name of the product.
	 */
	@When("^I remove (.+) from featured products list in opened category$")
	public void removeProductFromFeaturedProductList(final String productPartialName) {
		categoryEditor.removeFeaturedProductByName(productContainer.getProductByPartialName(productPartialName).getProductName());

		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Close category editor by category name.
	 *
	 * @param partialName partial name of the category.
	 */
	@When("^I close category editor for (.+) category$")
	public void closeCategoryEditor(final String partialName) {
		String categoryEditorTabName =
				categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(partialName)).getCategoryCode();
		categoryEditor.closePane(categoryEditorTabName);
	}

	/**
	 * verify newly created category type exists.
	 */
	@Then("^(?:newly created|updated) category type is in the list$")
	public void verifyNewlyCreatedCategoryTypeExists() {
		categoryTypesTab.verifyCategoryType(this.categoryType.getCategoryTypeName());
	}

	/**
	 * Open category editor for.
	 *
	 * @param categoryPartialName partial name of the category.
	 */
	@When("^I open category in editor by partial name (.+)$")
	public void openCategoryEditorForSpecificCategory(final String categoryPartialName) {
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.selectCategory(categoryContainer.getFullCategoryNameByPartialName(categoryPartialName));
		categoryEditor = catalogManagement.clickOpenCategoryIcon();
	}

	/**
	 * Edit category type name.
	 */
	@When("^I edit the category type name$")
	public void editCategoryTypeName() {
		categoryTypesTab.selectCategoryType(this.categoryType.getCategoryTypeName());
		this.categoryType.setCategoryTypeName("Edit Cat Type" + "_" + Utility.getRandomUUID());
		categoryTypesTab.clickEditCategoryTypeButton();
		addEditCategoryTypeDialog.enterCategoryTypeName(this.categoryType.getCategoryTypeName());
		addEditCategoryTypeDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete new category type.
	 */
	@When("^I delete the newly created category type$")
	public void deleteNewCategoryType() {
		categoryTypesTab.selectTab("CategoryTypes");
		categoryTypesTab.selectCategoryType(this.categoryType.getCategoryTypeName());
		categoryTypesTab.clickRemoveCategoryTypeButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.CatalogCategoryTypesSection_RemoveDialog");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify newly created category type is deleted.
	 */
	@Then("^the newly created category type is deleted$")
	public void verifyNewCategoryTypeDelete() {
		categoryTypesTab.verifyCategoryTypeDelete(this.categoryType.getCategoryTypeName());
	}

	/**
	 * Edit category name.
	 *
	 * @param newCategoryName String
	 */
	@When("^I edit the category name to (.+)")
	public void editCategoryName(final String newCategoryName) {
		categoryEditor.enterNewCategoryName(newCategoryName);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Edit category's type.
	 *
	 * @param newCategoryType String
	 */
	@When("^I edit the category type to (.+)$")
	public void editCategoryType(final String newCategoryType) {
		categoryEditor.selectCategoryType(newCategoryType);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Edits Category's store visible flag.
	 */
	@When("^I change store visible to invisible")
	public void changeCategoryStoreVisibleFlag() {
		categoryEditor.changeStoreVisibility();
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies category name.
	 *
	 * @param expCategoryName String
	 */
	@Then("^the category name should display as (.+)$")
	public void verifyCategoryName(final String expCategoryName) {
		categoryEditor.verifyCategoryName(expCategoryName);
		this.category.setCategoryName(expCategoryName);
	}

	/**
	 * Verifies Category's category type.
	 *
	 * @param expCategoryType String
	 */
	@Then("^category type displays as (.+)$")
	public void verifyCategoryType(final String expCategoryType) {
		categoryEditor.verifyCategoryTypeValue(expCategoryType);
	}

	/**
	 * Verifies category's store visible setting.
	 */
	@Then("^the store visibility for the category is invisible")
	public void verifyCategoryStoreVisibleUnticked() {
		categoryEditor.checkCategoryStoreVisibilityFalse();
	}

	/**
	 * Verifies attribute value exists.
	 *
	 * @param newAtrributeValue String
	 */
	@Then("^I should see the new attribute value (.+)$")
	public void verifyNewAttributeValue(final String newAtrributeValue) {
		categoryEditor.verifyAttributeValue(newAtrributeValue);
	}

	/**
	 * Edits an category attributes value.
	 *
	 * @param attributeName     String
	 * @param newAttributeValue String
	 */
	@When("^I edit (.+) to have a value of (.+)$")
	public void updatedAttributeValue(final String attributeName, final String newAttributeValue) {
		editLongTextAttributeDialog = categoryEditor.editCategoryAttributeValue(attributeName, newAttributeValue);
		editLongTextAttributeDialog.enterLongTextValue(newAttributeValue);
		editLongTextAttributeDialog.clickOKButton();
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Clears attribute value.
	 *
	 * @param attributeName String
	 */
	@When("^I clear (.+) attribute value$")
	public void clearAttributeValue(final String attributeName) {
		categoryEditor.clickClearAttribute(attributeName);
		catalogManagementActionToolbar.clickSaveButton();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}
}

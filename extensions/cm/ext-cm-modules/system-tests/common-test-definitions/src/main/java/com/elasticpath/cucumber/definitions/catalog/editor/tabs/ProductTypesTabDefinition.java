package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import java.util.List;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;
import com.elasticpath.selenium.domainobjects.containers.CartModifierGroupContainer;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.domainobjects.containers.SkuOptionContainer;
import com.elasticpath.selenium.editor.catalog.CatalogEditor;
import com.elasticpath.selenium.editor.catalog.tabs.ProductTypeTab;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.AddEditProductTypeWizard;

/**
 * Product Types Tab Definitions.
 */
public class ProductTypesTabDefinition {

	private final CatalogEditor catalogEditor;
	private AddEditProductTypeWizard addEditProductTypeWizard;
	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
	private final ProductTypeTab productTypeTab;
	private final ProductType productType;
	private final CartItemModifierGroup cartItemModifierGroup;
	private final AttributeContainer attributeContainer;
	private final CartModifierGroupContainer cartModifierGroupContainer;
	private final SkuOptionContainer skuOptionContainer;

	/**
	 * Constructor.
	 *
	 * @param cartItemModifierGroup      Cart Item Modifier Group.
	 * @param productType                ProductType to be used in current session.
	 * @param attributeContainer         attribute container.
	 * @param cartModifierGroupContainer cartModiferGroup container.
	 * @param skuOptionContainer         skuOption container.
	 */
	public ProductTypesTabDefinition(final CartItemModifierGroup cartItemModifierGroup, final ProductType productType,
									 final AttributeContainer attributeContainer, final CartModifierGroupContainer cartModifierGroupContainer,
									 final SkuOptionContainer skuOptionContainer) {
		final WebDriver driver = SetUp.getDriver();
		this.productTypeTab = new ProductTypeTab(driver);
		this.catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		this.catalogEditor = new CatalogEditor(driver);
		this.cartItemModifierGroup = cartItemModifierGroup;
		this.productType = productType;
		this.attributeContainer = attributeContainer;
		this.cartModifierGroupContainer = cartModifierGroupContainer;
		this.skuOptionContainer = skuOptionContainer;
	}

	/**
	 * Create product type.
	 *
	 * @param productTypeName the category type name
	 * @param attributeList   the list of attributes
	 */
	@When("^I create a new product type (.*) with following attributes$")
	public void createProductType(final String productTypeName, final List<String> attributeList) {
		addEditProductTypeWizard = productTypeTab.clickAddProductTypeButton();
		this.productType.setProductTypeName(productTypeName + Utility.getRandomUUID());
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());
		for (String attribute : attributeList) {
			addEditProductTypeWizard.selectAvailableAttribute(attribute);
			addEditProductTypeWizard.clickMoveRightButton();
			addEditProductTypeWizard.verifyAssignedAttribute(attribute);
		}
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Select attributes in product type.
	 *
	 * @param attributeName attribute name.
	 */
	private void selectAttribute(final String attributeName) {
		addEditProductTypeWizard.selectAvailableAttribute(attributeName);
		addEditProductTypeWizard.clickMoveRightButton();
		addEditProductTypeWizard.verifyAssignedAttribute(attributeName);
	}

	/**
	 * Select cart item modifier group in product type.
	 *
	 * @param cartItemModifierGroupCode cart item modifier group code.
	 */
	private void selectCartItemModifierGroup(final String cartItemModifierGroupCode) {
		addEditProductTypeWizard.selectAvailableGroup(cartItemModifierGroupCode);
		addEditProductTypeWizard.clickMoveRightForCartItemModiferGroup();
		addEditProductTypeWizard.verifyAssignedGroup(cartItemModifierGroupCode);
	}

	/**
	 * Set cart item modifier group in product type object.
	 *
	 */
	private void setCartItemModifierGroupCodes() {
		this.productType.setCartItemModifierGroup(cartModifierGroupContainer.getCartItemModifierGroups()
				.stream()
				.map(CartItemModifierGroup::getGroupCode)
				.collect(Collectors.toList()));
	}

	/**
	 * Create product type with attributes and cart item modifier.
	 *
	 * @param productTypeName the category type name
	 */
	@When("^I create a new product type (.*) with attributes and cart item modifier")
	public void createProductTypeWithExistAttributesAndCartItemModifierGroup(final String productTypeName) {
		addEditProductTypeWizard = productTypeTab.clickAddProductTypeButton();
		this.productType.setProductTypeName(productTypeName + Utility.getRandomUUID());
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());
		for (Attribute attribute : attributeContainer.getAttributes()) {
			if (attribute.getAttributeUsage().equals("Product")) {
				selectAttribute(attribute.getAttributeKey() + "EN");
			}
		}
		cartModifierGroupContainer.getCartItemModifierGroups()
				.forEach(cartItemModifierGroup -> selectCartItemModifierGroup(cartItemModifierGroup.getGroupCode()));
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
		setCartItemModifierGroupCodes();
		this.productType.setAttribute(attributeContainer.getAttributes().stream().map(Attribute::getKey).collect(Collectors.toList()));
	}

	/**
	 * Create product type with attributes, cart item modifier and with sku option.
	 *
	 * @param productTypeName the category type name
	 */
	@When("^I create a new product type (.*) with attributes, cart item modifier and with sku option")
	public void createProductTypeWithSku(final String productTypeName) {
		addEditProductTypeWizard = productTypeTab.clickAddProductTypeButton();
		this.productType.setProductTypeName(productTypeName + Utility.getRandomUUID());
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());
		for (Attribute attribute : attributeContainer.getAttributes()) {
			if (attribute.getAttributeUsage().equals("Product")) {
				selectAttribute(attribute.getAttributeKey() + "EN");
			}
		}
		cartModifierGroupContainer.getCartItemModifierGroups()
				.forEach(cartItemModifierGroup ->selectCartItemModifierGroup(cartItemModifierGroup.getGroupCode()));

		setCartItemModifierGroupCodes();
		addEditProductTypeWizard.checkMultipleSkuBox();
		addEditProductTypeWizard.clickNextInDialog();
		for (SkuOption skuOption : skuOptionContainer.getSkuOptions()) {
			addEditProductTypeWizard.selectAvailableSkuOption(skuOption.getCode());
			addEditProductTypeWizard.clickMoveRightButtonForSkuOption();
			addEditProductTypeWizard.verifyAssignedSkuOption(skuOption.getCode());
		}
		for (Attribute attribute : attributeContainer.getAttributes()) {
			if (attribute.getAttributeUsage().equals("SKU")) {
				addEditProductTypeWizard.selectAvailableSkuAttribute(attribute.getAttributeKey() + "EN");
				addEditProductTypeWizard.clickMoveRightButtonForSkuAttribute();
				addEditProductTypeWizard.verifyAssignedSkuAttribute(attribute.getAttributeKey() + "EN");
			}
		}
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
	}


	/**
	 * verify newly created product type exists.
	 */
	@Then("^(?:newly created|updated) product type is in the list$")
	public void verifyNewlyCreatedProductTypeExists() {
		productTypeTab.verifyProductType(this.productType.getProductTypeName());
	}

	/**
	 * Edit product type name.
	 */
	@When("^I edit the product type name$")
	public void editProductTypeName() {
		productTypeTab.selectProductType(this.productType.getProductTypeName());
		this.productType.setProductTypeName("Edit Prod Type" + "_" + Utility.getRandomUUID());
		productTypeTab.clickEditProductTypeButton();
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete new product type.
	 */
	@When("^I delete the newly created product type$")
	public void deleteNewProductType() {
		productTypeTab.selectTab("ProductTypes");
		productTypeTab.selectProductType(this.productType.getProductTypeName());
		productTypeTab.clickRemoveProductTypeButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.CatalogProductTypesSection_RemoveDialog");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify newly created product type is deleted.
	 */
	@Then("^the newly created product type is deleted$")
	public void verifyNewProductTypeDelete() {
		productTypeTab.verifyProductTypeDelete(this.productType.getProductTypeName());
	}

	/**
	 * Create new product type with newly created cart item modifier group.
	 *
	 * @param productTypeName the product type name.
	 */
	@When("^I create a new product type (.*) with newly created cart item modifier group$")
	public void createProductTypeWithCartItemModiferGroup(final String productTypeName) {
		catalogEditor.selectTab("ProductTypes");
		addEditProductTypeWizard = productTypeTab.clickAddProductTypeButton();
		this.productType.setProductTypeName(productTypeName + Utility.getRandomUUID());
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());
		addEditProductTypeWizard.selectAvailableGroup(this.cartItemModifierGroup.getGroupCode());
		addEditProductTypeWizard.clickMoveRightForCartItemModiferGroup();
		addEditProductTypeWizard.verifyAssignedGroup(this.cartItemModifierGroup.getGroupCode());
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Create new product type with newly created cart item modifier group and attribute.
	 *
	 * @param productTypeName the product type name.
	 */
	@When("^I create a new product type (.*) with newly created cart item modifier group and attribute$")
	public void createProductTypeWithCartItemModiferGroupAndAttribute(final String productTypeName, final List<String> attributeList) {
		catalogEditor.selectTab("ProductTypes");
		addEditProductTypeWizard = productTypeTab.clickAddProductTypeButton();
		this.productType.setProductTypeName(productTypeName + Utility.getRandomUUID());
		addEditProductTypeWizard.enterProductTypeName(this.productType.getProductTypeName());

		for (String attribute : attributeList) {
			addEditProductTypeWizard.selectAvailableAttribute(attribute);
			addEditProductTypeWizard.clickMoveRightButton();
			addEditProductTypeWizard.verifyAssignedAttribute(attribute);
		}

		addEditProductTypeWizard.selectAvailableGroup(this.cartItemModifierGroup.getGroupCode());
		addEditProductTypeWizard.clickMoveRightForCartItemModiferGroup();
		addEditProductTypeWizard.verifyAssignedGroup(this.cartItemModifierGroup.getGroupCode());
		addEditProductTypeWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}
}

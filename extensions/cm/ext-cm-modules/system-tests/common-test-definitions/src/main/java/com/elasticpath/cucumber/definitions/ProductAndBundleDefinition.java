package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.time.DateUtils;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortexTestObjects.Purchase;
import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditMerchandisingAssociationsDialog;
import com.elasticpath.selenium.dialogs.AddItemDialog;
import com.elasticpath.selenium.dialogs.AddPriceTierDialog;
import com.elasticpath.selenium.dialogs.BasePriceEditorDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditDecimalValueAttributeDialog;
import com.elasticpath.selenium.dialogs.SelectACategoryDialog;
import com.elasticpath.selenium.dialogs.SelectAProductDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.Catalog;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.DST;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.ProductSkuItem;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.containers.AttributeContainer;
import com.elasticpath.selenium.domainobjects.containers.CategoryContainer;
import com.elasticpath.selenium.domainobjects.containers.ProductContainer;
import com.elasticpath.selenium.domainobjects.containers.SkuOptionContainer;
import com.elasticpath.selenium.editor.OrderEditor;
import com.elasticpath.selenium.editor.SkuDetailsEditor;
import com.elasticpath.selenium.editor.product.ProductEditor;
import com.elasticpath.selenium.editor.product.tabs.AttributesTab;
import com.elasticpath.selenium.editor.product.tabs.BundleItemsTab;
import com.elasticpath.selenium.editor.product.tabs.MerchandisingTab;
import com.elasticpath.selenium.editor.product.tabs.PricingTab;
import com.elasticpath.selenium.navigations.CatalogManagement;
import com.elasticpath.selenium.resultspane.CatalogProductListingPane;
import com.elasticpath.selenium.resultspane.CatalogSearchResultPane;
import com.elasticpath.selenium.resultspane.CatalogSkuSearchResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.toolbars.ChangeSetActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.AddSkuWizard;
import com.elasticpath.selenium.wizards.CreateBundleWizard;
import com.elasticpath.selenium.wizards.CreateProductWizard;

/**
 * Product Definition.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.ExcessiveParameterList",
		"PMD.ExcessiveImports"})
public class ProductAndBundleDefinition {
	private static final String FALSE_VALUE = "false";
	private final CatalogManagement catalogManagement;
	private CreateBundleWizard createBundleWizard;
	private AddItemDialog addItemDialog;
	private AddPriceTierDialog addPriceTierDialog;
	private AddEditMerchandisingAssociationsDialog addEditMerchandisingAssociationsDialog;
	private AddSkuWizard addSkuWizard;
	private SkuDetailsEditor skuDetailsEditor;
	private SelectAProductDialog selectAProductDialog;
	private SelectACategoryDialog selectACategoryDialog;
	private SelectASkuDialog selectASkuDialog;
	private CatalogSearchResultPane catalogSearchResultPane;
	private CreateProductWizard createProductWizard;
	private BasePriceEditorDialog basePriceEditorDialog;
	private EditDecimalValueAttributeDialog editDecimalValueAttributeDialog;
	private final ActivityToolbar activityToolbar;
	private final BundleItemsTab bundleItemsTab;
	private final PricingTab priceTierTab;
	private final MerchandisingTab merchandisingTab;
	private final AttributesTab attributesTab;
	private final Product product;
	private final Catalog catalog;
	private final Category category;
	private final ProductType productType;
	private final DST dst;
	private static String uniqueSkuCode = "";
	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
	private CatalogProductListingPane catalogProductListingPane;
	private ProductEditor productEditor;
	private final ChangeSetActionToolbar changeSetActionToolbar;
	private CatalogSkuSearchResultPane catalogSkuSearchResultPane;
	private static String dependentLineItem;
	private final ProductContainer container;
	private final SkuOptionContainer skuOptionContainer;
	private final AttributeContainer attributeContainer;
	private final Brand brand;
	private final CategoryContainer categoryContainer;
	private final static String CATEGORY_ASSIGNMENT = "Category Assignment";
	private final static String ENGLISH = "English";
	private final static String ENABLE_DATE = "enableDate";
	private final static String DISABLE_DATE = "disableDate";
	private final static String SKU_DETAILS = "SKU Details";

	/**
	 * Constructor.
	 *
	 * @param catalog            Catalog object.
	 * @param category           Category object.
	 * @param productType        ProductType object.
	 * @param product            Product object.
	 * @param dst                DST object.
	 * @param categoryContainer  CategoryContainer object.
	 * @param skuOptionContainer SkuOptionContainer object.
	 * @param attributeContainer AttributeContainer object.
	 */
	public ProductAndBundleDefinition(final Catalog catalog, final Category category, final ProductType productType, final Product product,
									  final DST dst, final ProductContainer container, final Brand brand,
									  final AttributeContainer attributeContainer, final CategoryContainer categoryContainer,
									  final SkuOptionContainer skuOptionContainer) {
		final WebDriver driver = SetUp.getDriver();
		catalogManagement = new CatalogManagement(driver);
		bundleItemsTab = new BundleItemsTab(driver);
		addPriceTierDialog = new AddPriceTierDialog(driver);
		priceTierTab = new PricingTab(driver);
		merchandisingTab = new MerchandisingTab(driver);
		addEditMerchandisingAssociationsDialog = new AddEditMerchandisingAssociationsDialog(driver);
		attributesTab = new AttributesTab(driver);
		activityToolbar = new ActivityToolbar(driver);
		editDecimalValueAttributeDialog = new EditDecimalValueAttributeDialog(driver);
		changeSetActionToolbar = new ChangeSetActionToolbar(driver);
		productEditor = new ProductEditor(driver);
		this.catalog = catalog;
		this.category = category;
		this.productType = productType;
		this.product = product;
		this.product.setProductName("");
		this.catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		this.dst = dst;
		this.container = container;
		this.brand = brand;
		this.attributeContainer = attributeContainer;
		this.categoryContainer = categoryContainer;
		this.skuOptionContainer = skuOptionContainer;
	}

	/**
	 * Create new product for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^(?:I create|a) new product with following attributes$")
	public void createNewProduct(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		if (product.getCatalog() == null || product.getCategory() == null) {
			catalogManagement.expandCatalogAndVerifyCategory(this.catalog.getCatalogName(), this.category.getCategoryName());
			catalogManagement.doubleClickCategory(this.category.getCategoryName());
		} else {
			catalogManagement.expandCatalogAndVerifyCategory(product.getCatalog(), product.getCategory());
			catalogManagement.doubleClickCategory(product.getCategory());
		}
		createProduct(product);
	}

	/**
	 * Create new product for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^(?:I create|a) new product with following attributes for current catalog and created category$")
	public void createNewProductAndRefreshCatalog(final List<Product> productInfoList) {
		Optional.ofNullable(this.catalog).ifPresent(currentCatalog -> currentCatalog.setCatalogName(productInfoList.get(0).getCatalog()));
		Optional.ofNullable(this.catalog).ifPresent(currentCatalog -> currentCatalog.setBrand(productInfoList.get(0).getBrand()));

		createNewProduct(productInfoList);
	}

	/**
	 * Create new product for existing subcategory.
	 *
	 * @param parentCategory  a parent category for subcategory
	 * @param productInfoList a product info list.
	 */
	@When("^(?:I create|a) new product for a newly created subcategory where parent category is (.+) with following attributes$")
	public void createNewProductForSubcategory(final String parentCategory, final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		product.setCategory(this.category.getCategoryName());
		this.product.setCategory(this.category.getCategoryName());
		navigateToSubcategoryAndOpenProductList(product.getCatalog(), parentCategory, product.getCategory());
		createProduct(product);
	}

	/**
	 * Create new product for specific category.
	 *
	 * @param subCategory     subCategory name.
	 * @param parentCategory  parent Category name.
	 * @param productInfoList information about product to comparing with.
	 */
	@When("^(?:I create|a) new product for created subcategory (.+) where parent category is (.+) with following attributes$")
	public void createNewProductForSpecificSubcategory(final String subCategory, final String parentCategory, final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		String subCategoryFullName = categoryContainer.getFullCategoryNameByPartialName(subCategory);
		String parentCategoryFullName = categoryContainer.getFullCategoryNameByPartialName(parentCategory);
		product.setCategory(subCategoryFullName);
		catalog.setBrand(product.getBrand());
		this.product.setCategory(subCategoryFullName);
		navigateToSubcategoryAndOpenProductList(product.getCatalog(), parentCategoryFullName, product.getCategory());
		createProduct(product);
		final Product productForContainer = createProductForContainer();
		container.addProducts(productForContainer);
	}

	/**
	 * Create new product for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^(?:I create|a) new product for category (.+) with following attributes$")
	public void createNewProductForCategory(final String categoryName, final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		String categoryFullName = categoryContainer.getFullCategoryNameByPartialName(categoryName);
		this.category.setCategoryName(categoryFullName);
		if (product.getCatalog() == null || product.getCategory() == null) {
			catalogManagement.expandCatalogAndVerifyCategory(this.catalog.getCatalogName(), this.category.getCategoryName());
			catalogManagement.doubleClickCategory(this.category.getCategoryName());
		} else {
			catalogManagement.expandCatalogAndVerifyCategory(product.getCatalog(), product.getCategory());
			catalogManagement.doubleClickCategory(product.getCategory());
		}
		createProduct(product);
		final Product productForContainer = createProductForContainer();
		container.addProducts(productForContainer);
	}

	/**
	 * Verifies newly created product exists.
	 */
	@Then("^the newly created product is in the list$")
	public void verifyNewlyCreatedProductExists() {
		verifyProductByName(this.product.getProductName());
	}

	/**
	 * Deletes newly created product.
	 */
	@When("^I delete the newly created (?:product|bundle)$")
	public void deleteNewlyCreatedProduct() {
		assertThat(this.product.getProductName())
				.as("It's not possible to delete a Product. The product name was not assigned")
				.isNotBlank();
		verifyProductByName(this.product.getProductName());
		if (activityToolbar.isChangeSetEnabled()) {
			changeSetActionToolbar.clickAddItemToChangeSet();
			catalogSearchResultPane.isProductNameInList(this.product.getProductName());
		}
		catalogSearchResultPane.clickDeleteProductButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.DeleteProduct");
	}

	/**
	 * Deletes all created products, that exists in ProductContainer.
	 */
	@When("^I delete all created (?:products|bundles)$")
	public void deleteAllCreatedProducts() {
		final List<Product> products = container.getProducts();
		for (final Product item : products) {
			verifyProductByName(item.getProductName());
			if (activityToolbar.isChangeSetEnabled()) {
				changeSetActionToolbar.clickAddItemToChangeSet();
				catalogSearchResultPane.isProductNameInList(item.getProductName());
			}
			catalogSearchResultPane.clickDeleteProductButton();
			new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.DeleteProduct");
		}

	}

	/**
	 * Searches for product and verifies it is displayed in a result list.
	 *
	 * @param productName product name.
	 */
	public void verifyProductByName(final String productName) {
		searchForProductByName(productName);
		int index = 0;
		while (!catalogSearchResultPane.isProductNameInList(productName) && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchForProductByName(productName);
			index++;
		}
		catalogSearchResultPane.verifyProductNameExists(productName);
	}


	/**
	 * Verify product is deleted.
	 */
	@Then("^the (?:product|bundle) is deleted$")
	public void verifyProductIsDeleted() {
		verifyProductIsDeleted(this.product.getProductName());
	}

	/**
	 * Verify, that all products is deleted.
	 */
	@Then("^the all (?:products|bundles) is deleted$")
	public void verifyAllProductIsDeleted() {
		final List<Product> products = container.getProducts();
		products.forEach(item -> verifyProductIsDeleted(item.getProductName()));
	}

	/**
	 * Verify product is deleted.
	 *
	 * @param prodName the product name
	 */
	public void verifyProductIsDeleted(final String prodName) {
		searchForProductByName(prodName);
		catalogSearchResultPane.verifyProductIsDeleted(prodName);
	}

	/**
	 * Delete Newly Created product.
	 */
	@Then("^the newly created bundle exists and contains the added items$")
	public void verifyNewlyCreatedBundle() {
		verifyProductByName(this.product.getProductName());
		catalogSearchResultPane.doubleClick(catalogSearchResultPane.getSelectedElement(), String.format(AbstractPageObject
				.EDITOR_PANE_TAB_CSS_TEMPLATE, this.product.getProductCode()));
		bundleItemsTab.selectTab("Constituents");

		verifyBundleProductCodesListExistInTestData();
		for (String productCode : this.product.getBundleProductSKUList()) {
			bundleItemsTab.verifyBundleItemExists(productCode);

		}
	}

	/**
	 * Delete Newly Created product.
	 *
	 * @param productCode product code
	 */
	@When("^I delete item with product code (.+) from the bundle$")
	public void deleteBundleItem(final String productCode) {
		bundleItemsTab.removeBundleItem(productCode);
	}

	/**
	 * verifies bundle item deleted.
	 *
	 * @param productCode product code
	 */
	@Then("^the bundle item with product code (.+) is deleted$")
	public void verifyBundleItemDeleted(final String productCode) {
		bundleItemsTab.verifyBundleItemDeleted(productCode);
	}

	/**
	 * Update quantity of a bundle product.
	 *
	 * @param productCode product code
	 * @param quantity    quantity
	 */
	@And("^I update bundle item with product code (.+) quantity to (.+)$")
	public void editBundleItemQuantity(final String productCode, final String quantity) {
		bundleItemsTab.editBundleItemQuantityAndVerify(productCode, quantity);
	}

	/**
	 * Add bundle item.
	 *
	 * @param productCode product code
	 */
	@And("^I add new Item with product code (.+) to the bundle$")
	public void addBundleItemFromTab(final String productCode) {
		bundleItemsTab.clickAddBundleItemButton();
		addBundleItem(productCode);
	}

	/**
	 * Verifies bundle item exists.
	 *
	 * @param productCode prduct code
	 */
	@Then("^the bundle item with product code (.+) exists in the bundle$")
	public void verifyBundleItemExists(final String productCode) {
		bundleItemsTab.verifyBundleItemExists(productCode);
	}

	/**
	 * Verifies bundle item quantity.
	 *
	 * @param productCode product code
	 * @param quantity    quantity
	 */
	@Then("^the bundle item with product code (.+) has quantity of (.+)$")
	public void verifyBundleItemQuantity(final String productCode, final String quantity) {
		bundleItemsTab.verifyBundleItemQuantity(productCode, quantity);
	}

	/**
	 * Verify bundle selection rule parameter.
	 *
	 * @param expSelectionParameter Expected selection rule parameter.
	 */
	@Then("^the bundle selection rule parameter is where n is (.+)$")
	public void verifyBundleSelectionRule(final String expSelectionParameter) {
		bundleItemsTab.verifyBundleSelectionParameter(expSelectionParameter);
	}

	/**
	 * Verify bundle selection rule.
	 *
	 * @param expSelectionRule expected selection rule.
	 */
	@Then("the bundle selection rule is (.+)$")
	public void verifyBundleSelectionRuleOnly(final String expSelectionRule) {
		bundleItemsTab.verifyBundleSelectionRule(expSelectionRule);
	}

	/**
	 * Edit  bundle Selection rule and parameter.
	 *
	 * @param selectionRule      New selection rule.
	 * @param selectionParameter New selection parameter
	 */
	@When("^I edit the selection rule to be (.+) where n is (.+)$")
	public void editBundleSelectionRule(final String selectionRule, final String selectionParameter) {
		bundleItemsTab.editSelectionRule(selectionRule);
		bundleItemsTab.editSelectionParameter(selectionParameter);
		activityToolbar.saveAll();
	}

	/**
	 * Create new bundle with selection rule for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^I create a new (?:dynamic bundle|bundle) with following attributes$")
	public void createNewBundleForExistingCategory(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		catalogManagement.expandCatalogAndVerifyCategory(product.getCatalog(), product.getCategory());
		catalogManagement.doubleClickCategory(product.getCategory());
		createBundleWizard = catalogManagement.clickCreateBundleButton();
		String productCode = Utility.getRandomUUID();
		this.product.setProductCode(productCode);
		String productName = product.getProductName() + "-" + productCode;
		this.product.setProductName(productName);
		if (dst != null) {
			dst.setProductCode(productCode);
			dst.setProductName(productName);
		}
		createBundleWizard.enterProductCode(productCode);
		createBundleWizard.enterProductName(this.product.getProductName());
		createBundleWizard.selectBundlePricing(product.getBundlePricing());
		createBundleWizard.selectProductType(product.getProductType());
		createBundleWizard.selectBrand(product.getBrand());

		if (product.getStoreVisible().equalsIgnoreCase("true")) {
			createBundleWizard.checkStoreVisibleBox();
		} else {
			assertThat(product.getStoreVisible())
					.as("Store visible value is invalid - " + product.getStoreVisible())
					.isEqualToIgnoringCase(FALSE_VALUE);
		}

		createBundleWizard.clickNextInDialog();

		this.product.setBundleProductSKUCodes(product.getBundleProductSKUCodes());
		verifyBundleProductCodesListExistInTestData();
		for (String bundleProductCode : this.product.getBundleProductSKUList()) {
			if (bundleProductCode.contains("sku")) {
				addItemDialog = createBundleWizard.clickAddItemButton();
				addItemDialog.selectType("SKU");
				addBundleItemSku(bundleProductCode);

			} else {

				addItemDialog = createBundleWizard.clickAddItemButton();
				addBundleItem(bundleProductCode);
			}

		}
		if (product.getBundleSelectionRule() != null) {
			createBundleWizard.selectRule(product.getBundleSelectionRule());
			createBundleWizard.selectRuleParameter(product.getBundleSelectionRuleValue());
		}

		createBundleWizard.clickNextInDialog();
		createBundleWizard.enterAttributeShortTextMultiValue(product.getAttrShortTextMultiValue(), product.getAttrShortTextMulti());
		createBundleWizard.enterAttributeIntegerValue(product.getAttrIntegerValue(), product.getAttrInteger());
		createBundleWizard.enterAttributeDecimalValue(product.getAttrDecimalValue(), product.getAttrDecimal());
		createBundleWizard.clickNextInDialog();
		createBundleWizard.enterSkuCode(getUniqueSkuCode());
		addBundlePrice(product);
		createBundleWizard.clickFinish();
	}

	/**
	 * Assign cetegory for newly crated product.
	 *
	 * @param categoryName name of the category.
	 */
	@Then("^I assign (.+) category to the newly created product")
	public void assignCategoryToNewlyCreatedProduct(final String categoryName) {
		this.searchForProductByCode(this.product.getProductCode());
		this.verifyProductSearchResultCode(this.product.getProductCode());
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(this.product.getProductCode());
		productEditor.selectTab(CATEGORY_ASSIGNMENT);
		addCategoryAssignment(categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryName))
				.getCategoryCode());
	}

	/**
	 * Define primary category for newly created product.
	 *
	 * @param categoryName category name.
	 */
	@And("^I define (.+) as Primary category for newly created product")
	public void definePrimaryCategoryToNewlyCreatedProduct(final String categoryName) {
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(this.product.getProductCode());
		productEditor.selectPrimaryCategory(categoryContainer.getCategoryMap().get(categoryContainer.getFullCategoryNameByPartialName(categoryName))
				.getCategoryName());
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
		catalogSearchResultPane.closeProductSearchResultsPane();
	}


	/**
	 * Adds a bundle Item with the given product code.
	 *
	 * @param bundleProductCode product code.
	 */
	public void addBundleItem(final String bundleProductCode) {
		selectAProductDialog = addItemDialog.clickSelectProductImageLink();
		selectAProductDialog.enterProductCode(bundleProductCode);
		selectAProductDialog.clickSearchButton();
		selectAProductDialog.selectProductByCode(bundleProductCode);
		selectAProductDialog.clickOKButton();
		addItemDialog.clickOKButton();
	}

	/**
	 * Adds a bundle Item with the given sku code.
	 *
	 * @param bundleSkuCode product code.
	 */
	public void addBundleItemSku(final String bundleSkuCode) {
		selectASkuDialog = addItemDialog.clickSelectSkuImageLink();
		selectASkuDialog.enterSkuCode(bundleSkuCode);
		selectASkuDialog.clickSearchButton();
		selectASkuDialog.selectSkuCodeInSearchResult(bundleSkuCode);
		selectASkuDialog.clickOKButton();
		addItemDialog.clickOKButton();
	}

	/**
	 * Checks if test data has the sku codes.
	 */
	private void verifySKUCodesListExistInTestData() {
		assertThat(this.product.getSKUCodeList())
				.as("No SKU codes list provided in test data.")
				.isNotNull();
	}

	/**
	 * Checks if test data has the product codes.
	 */
	private void verifyBundleProductCodesListExistInTestData() {
		assertThat(this.product.getBundleProductSKUList())
				.as("No Product codes list provided in test data.")
				.isNotNull();
	}


	/**
	 * Searches for a product by name.
	 *
	 * @param productName product name.
	 */
	public void searchForProductByName(final String productName) {
		searchForActiveProduct(() -> catalogManagement.enterProductName(productName));
	}


	/**
	 * Searches for a product by code.
	 *
	 * @param productCode product code.
	 */
	public void searchForProductByCode(final String productCode) {
		searchForActiveProduct(() -> catalogManagement.enterProductCode(productCode));
	}

	private String getUniqueSkuCode() {
		uniqueSkuCode = this.product.getProductName() + Utility.getRandomUUID() + "_sku";
		this.product.setSkuCode(uniqueSkuCode);
		return uniqueSkuCode;
	}

	/**
	 * Selects Product Type.
	 *
	 * @param product Product.
	 */
	private void selectProductType(final Product product) {
		if (product.getProductType() == null || product.getBrand() == null) {
			createProductWizard.selectProductType(this.productType.getProductTypeName());
			createProductWizard.selectBrand(this.catalog.getBrand());
		} else {
			createProductWizard.selectProductType(product.getProductType());
			createProductWizard.selectBrand(product.getBrand());
		}
	}

	/**
	 * Selects store visiblity.
	 *
	 * @param product Product.
	 */
	private void selectStoreVisiblity(final Product product) {
		if (product.getStoreVisible().equalsIgnoreCase("true")) {
			createProductWizard.checkStoreVisibleBox();
		} else {
			assertThat(product.getStoreVisible())
					.as("Store visible value is invalid - " + product.getStoreVisible())
					.isEqualToIgnoringCase(FALSE_VALUE);
		}
	}

	/**
	 * Enters product attributes.
	 *
	 * @param product Product.
	 */
	private void enterProductAttribute(final Product product) {
		if (product.getAttrShortTextMultiValue() != null) {
			createProductWizard.enterAttributeShortTextMultiValue(product.getAttrShortTextMultiValue(), product.getAttrShortTextMulti());
			createProductWizard.enterAttributeIntegerValue(product.getAttrIntegerValue(), product.getAttrInteger());
			createProductWizard.enterAttributeDecimalValue(product.getAttrDecimalValue(), product.getAttrDecimal());
		}
	}

	/**
	 * Adds multi sku values.
	 *
	 * @param product Product.
	 */
	private void addMultiSkuValues(final Product product) {
		this.product.setProductSKUCodes(product.getProductSKUCodes());
		verifySKUCodesListExistInTestData();
		for (String productSkuCode : this.product.getSKUCodeList()) {
			addSkuWizard = createProductWizard.clickAddSkuButton();
			addSkuWizard.enterSkuCode(getUniqueSkuCode());
			addSkuWizard.selectSkuOptions("GC_Denominations", productSkuCode);
			addSkuWizard.selectSkuOptions("GC_Themes", product.getSKUOption());
			addSkuWizard.selectShippableType(product.getShippableType());
			addSkuWizard.clickNextButton();
			addSkuWizard.clickFinish();
		}
	}

	/**
	 * Adds product price.
	 *
	 * @param product Product.
	 */
	private void addPrice(final Product product) {
		if (product.getPriceList() != null && !activityToolbar.isChangeSetEnabled()) {
			createProductWizard.clickNextInDialog();
			createProductWizard.selectPriceList(product.getPriceList());
			basePriceEditorDialog = createProductWizard.clickAddBasePriceButton();
			basePriceEditorDialog.enterListPrice(product.getListPrice());
			basePriceEditorDialog.clickOKButton();
		}
	}

	/**
	 * Editing the newly created Product name.
	 *
	 * @param productName editing the product name to another
	 */
	@When("^I edit newly created product name to (.+)$")
	public void editProductName(final String productName) {
		productEditor = catalogSearchResultPane.openProductEditorWithProductName(this.product.getProductName());
		this.product.setProductName(productName);
		productEditor.enterProductName(this.product.getProductName());
		catalogManagementActionToolbar.saveAll();
		productEditor.closeProductEditor(this.product.getProductCode());
	}

	/**
	 * Attempts to edit the product name and verifies that it is unable to edit because
	 * product is locked in changeset.
	 *
	 * @param productCode    the product code of the product to edit
	 * @param newProductName the new name
	 */
	@When("^I should not be able to edit (.+) object's name to (.+)$")
	public void testEditProductName(final String productCode, final String newProductName) {
		productEditor.selectproductEditor(productCode);
		productEditor.tryProductNameChange();
	}

	/**
	 * Editing the newly created Product name.
	 *
	 * @param productCode    product code of the product to edit
	 * @param newProductName the new name
	 */
	@When("^I should be able to edit (.+) object's name to (.+)$")
	public void editProductName(final String productCode, final String newProductName) {
		productEditor.selectproductEditor(productCode);
		try {
			productEditor.enterProductName(newProductName);
			productEditor.closeProductEditorWithoutSave(productCode);
		} catch (InvalidElementStateException exception) {
			fail("Unable to edit object name even though we unlocked changeset that contains this object");
		}
	}

	/**
	 * Search and open an existing Product to go specific Tab.
	 *
	 * @param existingProductName the product name
	 * @param tabName             for product
	 */
	@When("^I am viewing the (.+) tab of an existing product with name (.+)$")
	public void searchOpenProductEditorTab(final String tabName, final String existingProductName) {
		searchOpenProductEditor(existingProductName);
		productEditor.selectTab(tabName);
	}

	/**
	 * Search for product by name.
	 *
	 * @param productName product name.
	 */
	@When("^I search for product by name (.*) and verify it appears in a result list$")
	public void searchForProductName(final String productName) {
		verifyProductByName(productName);
	}

	/**
	 * Search and open an existing Product.
	 *
	 * @param existingProductName the product name
	 */
	@When("^I search and open an existing product with name (.+)$")
	public void searchOpenProductEditor(final String existingProductName) {
		verifyProductByName(existingProductName);
		productEditor = catalogSearchResultPane.openProductEditorWithProductName(existingProductName);
	}

	/**
	 * Search and open an existing Product to go specific Tab.
	 *
	 * @param existingProductcode the product code
	 * @param tabName             for product
	 */
	@When("^I (?:am viewing|can see) the (.+) tab of an existing product with product code (.+)$")
	public void searchOpenProductEditorTabWithCode(final String tabName, final String existingProductcode) {
		searchOpenProductWithCode(existingProductcode);
		productEditor.selectTab(tabName);
	}

	/**
	 * Search for product by code.
	 *
	 * @param productCode product code.
	 */
	@When("^I search for (?:product|bundle) by code (.*)$")
	public void searchForProductCode(final String productCode) {
		searchForProductByCode(productCode);
		this.product.setProductCode(productCode);
	}

	/**
	 * Search for new product by code.
	 */
	@When("^I search for new (?:product|bundle) by code$")
	public void searchForNewProductByCode() {
		activityToolbar.clickCatalogManagementButton();
		searchForProductByCode(this.product.getProductCode());
	}

	/**
	 * Search for product by sku.
	 *
	 * @param productSku product sku.
	 */
	@When("^I search for product by sku (.*)$")
	public void searchForProductSku(final String productSku) {
		searchForActiveProduct(() -> catalogManagement.enterProductSku(productSku));
	}

	/**
	 * Search for sku by sku code.
	 *
	 * @param skuCode sku code as a search key.
	 */
	@When("^I search for sku by sku code (.*)$")
	public void searchForSkuBySkuCode(final String skuCode) {
		searchForSku(() -> catalogManagement.enterProductSkuSkuSearch(skuCode));
		this.product.setSkuCode(skuCode);
	}

	/**
	 * Search for sku by product name.
	 *
	 * @param productName product name as a search key.
	 */
	@When("^I search for sku by product name (.*)$")
	public void searchForSkuByProductName(final String productName) {
		searchForSku(() -> catalogManagement.enterProductNameSkuSearch(productName));
	}

	/**
	 * Search for sku by product code.
	 *
	 * @param productCode product code as a search key.
	 */
	@When("^I search for sku by product code (.*)$")
	public void searchForSkuByProductCode(final String productCode) {
		searchForSku(() -> catalogManagement.enterProductCode(productCode));
	}

	/**
	 * Search for sku by selecting Sku Options filter values.
	 *
	 * @param skuOption      sku Option which should be selected in a filter.
	 * @param skuOptionValue sku Option Value which should be selected in a filter.
	 */
	@When("^I search for sku by Sku Option (.+) and Sku Option value (.+)$")
	public void searchForSkuBySkuOptionsFilter(final String skuOption, final String skuOptionValue) {
		searchForSku(() -> {
			catalogManagement.selectSkuOption(skuOption);
			catalogManagement.selectSkuOptionValue(skuOptionValue);
		});
	}

	/**
	 * Verifies that product code appears in a search result table.
	 *
	 * @param expectedProductCode expected product code.
	 */
	@Then("^(?:Product|Bundle) code (.*) should appear in result$")
	public void verifyProductSearchResultCode(final String expectedProductCode) {
		int index = 0;
		while (!catalogSearchResultPane.isProductCodeInList(expectedProductCode) && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchForProductCode(expectedProductCode);
			index++;
		}
		catalogSearchResultPane.verifyProductCodeExists(expectedProductCode);
	}

	/**
	 * Verifies that product code is not in search result table.
	 */
	@Then("^the new product should not appear in the result$")
	public void verifyProductCodeIsNotPresent() {
		catalogSearchResultPane.sleep(Constants.SLEEP_THREE_SECONDS_IN_MILLIS);
		catalogSearchResultPane.setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(catalogSearchResultPane.isProductCodeInList(product.getProductCode()))
				.as("Product should not be present")
				.isFalse();
		catalogSearchResultPane.setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies that product with specified sku appears in a search result table.
	 *
	 * @param productCode the product code
	 * @param expectedSku expected product sku.
	 */
	@Then("^Product with code (.+) and sku (.*) should appear in result$")
	public void verifyProductSearchResultSku(final String productCode, final String expectedSku) {
		int index = 0;
		while (catalogSearchResultPane.isSearchResultTableEmpty() && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchForProductSku(expectedSku);
			index++;
		}
		productEditor = catalogSearchResultPane.openProductEditorForFirstSearchResultEntry(productCode);
		productEditor.selectTab(ProductEditor.SKU_DETAILS_TAB_ID);
		assertThat(productEditor.getSkuCode())
				.as("Unexpected sku code in the search results")
				.isEqualTo(expectedSku);
	}

	/**
	 * Verifies that entity (single sku product, sku associated with multi sku product or bundle)
	 * with specified sku code appears in a search result table.
	 *
	 * @param expectedSku expected sku code.
	 */
	@Then("^Entity with sku code (.*) should appear in result$")
	public void isEntityInResultTableSkuProduct(final String expectedSku) {
		int index = 0;
		while (!catalogSkuSearchResultPane.isSkuCodeInList(expectedSku) && index < Constants.UUID_END_INDEX) {
			catalogSkuSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchForSkuBySkuCode(expectedSku);
			index++;
		}
		assertThat(catalogSkuSearchResultPane.isSkuCodeInList(expectedSku))
				.as("Expected single sku product is not found")
				.isTrue();
	}

	/**
	 * Checks if all entries of sku search results table contain provided product name.
	 *
	 * @param productName expected product name for verification
	 */
	@Then("^All entries in result list have product name (.+)$")
	public void areAllEntriesInListHaveProductName(final String productName) {
		catalogSkuSearchResultPane.isValueInAllListEntries(productName, CatalogSkuSearchResultPane.PRODUCT_NAME_COLUMN_INDEX);
	}

	/**
	 * Opens Product Editor for the first entry in a sku search result table, checks that the editor is opened for a found product and
	 * verifies that Open Parent Product button is not rendered.
	 */
	@Then("^I should not see Open Parent Product button in Sku Details tab for found entity$")
	public void openParentProductNotPresent() {
		verifySkuCodeInProductEditor(this.product.getSkuCode());
		productEditor.selectTab(ProductEditor.SKU_DETAILS_TAB_ID);
		assertThat(productEditor.isOpenParentProductPresent())
				.as("Open Parent Product button is rendered, but expected to be absent for opened entity.")
				.isFalse();
	}

	/**
	 * Opens Product Editor for the first entry in a sku search result table, checks that the editor is opened for a found sku and
	 * verifies that Open Parent Product button is rendered.
	 */
	@Then("^I should see Open Parent Product button in Sku Details tab for found sku$")
	public void openParentProductIsPresent() {
		verifySkuCodeInProductEditor(this.product.getSkuCode());
		productEditor.selectTab(ProductEditor.SKU_DETAILS_TAB_ID);
		assertThat(productEditor.isOpenParentProductPresent())
				.as("Open Parent Product button is not rendered, but expected to be present for sku associated with multi sku product.")
				.isTrue();
	}

	/**
	 * Verifies that specified tab is not rendered in a currently opened Product Editor.
	 *
	 * @param tabName tab name which supposed to be absent in a currently opened Product Editor
	 */
	@Then("^I should not see (.+) tab in a currently opened Product Editor$")
	public void editorTabNotPresent(final String tabName) {
		assertThat(productEditor.isTabPresent(tabName))
				.as(tabName + " tab is rendered, but expected to be absent.")
				.isFalse();
	}

	/**
	 * Verifies that specified tab is rendered in a currently opened Product Editor.
	 *
	 * @param tabName tab name which supposed to be in a currently opened Product Editor
	 */
	@Then("^I should see (.+) tab in a currently opened Product Editor$")
	public void editorTabIsPresent(final String tabName) {
		assertThat(productEditor.isTabPresent(tabName))
				.as(tabName + " tab is not rendered, but expected to be present.")
				.isTrue();
	}

	/**
	 * Search and open an existing Product with the given code.
	 *
	 * @param existingProductcode the product code
	 */
	@When("^I search and open an existing product with product code (.+)$")
	public void searchOpenProductWithCode(final String existingProductcode) {
		searchForProductCode(existingProductcode);
		verifyProductSearchResultCode(existingProductcode);
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(existingProductcode);
	}

	/**
	 * Search and open an existing Product with the given name.
	 */
	@When("^I search and open an existing product with product name$")
	public void searchOpenProductWithName() {
		searchForProductName(product.getProductName());
		verifyProductSearchResultCode(product.getProductName());
		productEditor = catalogSearchResultPane.openProductEditorWithProductCode(product.getProductName());
	}

	/**
	 * Saves new future date in Enable Date/Time field
	 */
	@When("^I enter future date in Enable Date / Time field and save changes$")
	public void changeEnableDate() throws ParseException {
		this.product.setEnableDateTime(productEditor.getDateTimeFromEpFormattedString(productEditor.getEnableDate()));
		productEditor.setEnableDate(productEditor.getFormattedDateTime(2));
		clickSaveAllIcon();
	}

	/**
	 * Saves new past date in Disable Date/Time field
	 */
	@When("^I enter past date in Disable Date / Time field and save changes$")
	public void changeDisableDateToPast() throws ParseException {
		this.product.setEnableDateTime(productEditor.getDateTimeFromEpFormattedString(productEditor.getEnableDate()));
		productEditor.setEnableDate(productEditor.getFormattedDateTime(-2));

		productEditor.setDisableDate(productEditor.getFormattedDateTime(-1));
		this.product.setDisableDateTime(productEditor.getDateTimeFromEpFormattedString(productEditor.getDisableDate()));
		clickSaveAllIcon();
	}

	/**
	 * Saves new future date in Disable Date/Time field
	 */
	@When("^I enter future date in Disable Date / Time field and save changes$")
	public void changeDisableDateToFuture() throws ParseException {
		productEditor.setDisableDate(productEditor.getFormattedDateTime(1));
		this.product.setDisableDateTime(productEditor.getDateTimeFromEpFormattedString(productEditor.getDisableDate()));
		clickSaveAllIcon();
	}

	/**
	 * Unable / disable store visible
	 */
	@When("^I click store visible$")
	public void changeStoreVisibleUnchecked() {
		this.product.setStoreVisible(FALSE_VALUE);
		productEditor.clickStoreVisible();
		clickSaveAllIcon();
	}

	/**
	 * Add Price tier for an existing Product.
	 *
	 * @param listPrice the list price.
	 * @param quantity  quantity
	 */
	@When("^I add a new price tier with List Price of (.+) for quantity of (.+)$")
	public void addPriceTierData(final String listPrice, final String quantity) {
		priceTierTab.selectPriceList("Mobile Price List (CAD)");
		addPriceTierDialog = priceTierTab.clickAddPriceTierButton();
		addPriceTierDialog.addPriceTierData(listPrice, quantity);
		addPriceTierDialog.clickOKButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies added Price tier exists.
	 *
	 * @param listPrice the list price.
	 */
	@Then("^the new price tier with List Price of (.+) exists in the pricing table$")
	public void verifyAddedPriceTierExists(final String listPrice) {
		catalogManagementActionToolbar.clickReloadActiveEditor();
		priceTierTab.verifyAddedPriceTierExists(listPrice);
	}

	/**
	 * Edit created Price tier List Price.
	 *
	 * @param oldListPrice the list price.
	 * @param newListPrice the list price.
	 */
	@And("^I edit price tier List Price from (.+) to (.+)$")
	public void editPriceTierListPrice(final String oldListPrice, final String newListPrice) {
		priceTierTab.clickEditPriceTierButton(oldListPrice, newListPrice);
		catalogManagementActionToolbar.saveAll();
	}

	/**
	 * Delete newly created Price tier.
	 *
	 * @param listPrice the list price.
	 */
	@When("^I delete the price tier with List Price of (.+)$")
	public void deletePriceTierListPrice(final String listPrice) {
		priceTierTab.deletePriceTierListPrice(listPrice);
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify newly created Price tier is deleted.
	 *
	 * @param listPrice the list price.
	 */
	@Then("^the price tier with List Price of (.+) is deleted from the pricing table$")
	public void verifyPriceTierDelete(final String listPrice) {
		priceTierTab.verifyPriceTierDelete(listPrice);
	}

	/**
	 * Clears and Edit attribute value.
	 *
	 * @param attributeName String
	 * @param value         String
	 */
	@Then("^I clear and edit (.+) attribute value to (.+)$")
	public void clearAttributeValue(final String attributeName, final String value) {
		attributesTab.clickClearAttribute(attributeName);
		editDecimalValueAttributeDialog = attributesTab.clickEditAttributeButtonDecimalValue();
		editDecimalValueAttributeDialog.enterDecimalValue(value);
		editDecimalValueAttributeDialog.clickOKButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify updated attribute value.
	 *
	 * @param attributeName String
	 * @param value         String
	 */
	@Then("^the (.+) attribute value is updated to (.+)$")
	public void verifyAttributeValue(final String attributeName, final String value) {
		attributesTab.verifyProductAttributeValue(attributeName, value);
		productEditor.closeProductEditor(this.product.getProductCode());
	}

	/**
	 * Adding SKU to newly created Product.
	 *
	 * @param productSkuCode   for SKU
	 * @param skuShippableType for SKU
	 */
	@When("^I add SKU (.+) with (.+) shippable type$")
	public void addSkuToProduct(final String productSkuCode, final String skuShippableType) {
		productEditor = catalogSearchResultPane.openProductEditorWithProductName(this.product.getProductName());
		productEditor.selectTab(ProductEditor.SKU_DETAILS_TAB_ID);
		addSkuWizard = productEditor.clickAddSkuButton();
		addSkuWizard.enterSkuCode(getUniqueSkuCode());
		addSkuWizard.selectSkuOptions("GC_Denominations", productSkuCode);
		addSkuWizard.selectSkuOptions("GC_Themes", "berries_theme - Berries");
		addSkuWizard.selectShippableType(skuShippableType);
		addSkuWizard.clickNextButton();
		addSkuWizard.clickFinish();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify and Open newly created Product SKU.
	 *
	 * @param productSkuCode for SKU
	 */
	@Then("^the SKU (.+) is in the list and sku editor$")
	public void openSkuFromProduct(final String productSkuCode) {
		productEditor.selectSkuDetails(productSkuCode);
		skuDetailsEditor = productEditor.clickOpenSkuButton();
		skuDetailsEditor.verifySkuDetails(productSkuCode);
		skuDetailsEditor.closeSkuDetailsEditor();
	}

	/**
	 * Closes previously opened sku editor.
	 *
	 * @param skuCode sku code opened in editor.
	 *
	 */
	@Then("^I close opened previously sku editor for sku (.+)$")
	public void closeSkuEditor(final String skuCode) {
		final String productSkuOptionCode = this.product.getSkuOptionCodeByPartialCode(skuCode);
		skuDetailsEditor.closeSkuDetailsEditorWithCheck(productSkuOptionCode);
	}

	/**
	 * Delete SKU from newly created Product.
	 *
	 * @param productSkuCode for SKU
	 */
	@When("^I delete the SKU (.+)$")
	public void deleteSkuFromProduct(final String productSkuCode) {
		productEditor.selectSkuDetails(productSkuCode);
		productEditor.clickRemoveSkuDetailsButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.ProductEditorMultiSkuSection_RemoveConfirmation");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify SKU is deleted.
	 *
	 * @param productSkuCode for SKU
	 */
	@When("^the SKU (.+) is no longer in the list$")
	public void verifySkuIsDeleted(final String productSkuCode) {
		productEditor.verifyProductSkuIsDeleted(productSkuCode);
		productEditor.closeProductEditor(this.product.getProductCode());
	}

	/**
	 * Adds a category to category assignment list.
	 *
	 * @param categoryAssignmentCode category name.
	 */
	@When("^I add category (.+) to category assignment list$")
	public void addCategoryAssignment(final String categoryAssignmentCode) {
		selectACategoryDialog = productEditor.clickAddCategoryButton();
		selectACategoryDialog.enterCategoryCode(categoryAssignmentCode);
		selectACategoryDialog.clickSearchButton();
		selectACategoryDialog.selectCategoryByCode(categoryAssignmentCode);
		selectACategoryDialog.clickOKButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete a category from category assignment list.
	 *
	 * @param categoryAssignmentName category name.
	 */
	@When("^I delete category (.+) from category assignment list$")
	public void deleteCategoryAssignment(final String categoryAssignmentName) {
		productEditor.selectCategoryDetails(categoryAssignmentName);
		productEditor.clickRemoveCategoryButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.CategoryAssignmentPage_RemoveConfirmTitle");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
		productEditor.closeProductEditor(this.product.getProductCode());
	}

	/**
	 * Exclude a category from category assignment list for a particular catalog.
	 *
	 * @param categoryName category name.
	 * @param catalogName  catalog name.
	 */
	@When("^I exclude category (.+) in the (.+) catalog from category assignment list$")
	public void excludeCategoryAssignmentOfCatalog(final String categoryName, final String catalogName) {
		productEditor.selectVirtualCatalogAssignment(catalogName);

		if ("newly created".equalsIgnoreCase(categoryName)) {
			excludeCategoryAssignment(this.category.getCategoryName());
		} else {
			excludeCategoryAssignment(categoryName);
		}

	}

	private void excludeCategoryAssignment(final String categoryName) {
		productEditor.selectCategoryDetails(categoryName);
		productEditor.clickExcludeCategoryButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.CategoryAssignmentPage_RemoveConfirmTitle");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
		productEditor.closeProductEditor(this.product.getProductCode());
	}


	/**
	 * Verify product under assigned category for a catalog.
	 *
	 * @param productName  Product Name.
	 * @param categoryName Category Name.
	 * @param catalogName  Catalog Name.
	 */
	@When("^product (.+) present under category (.+) for (.+)$")
	public void verifyProductLinkedWithCategory(final String productName, final String categoryName, final String catalogName) {
		catalogManagement.clickCatalogBrowseTab();
		catalogManagement.expandCatalogAndVerifyCategory(catalogName, categoryName);
		catalogProductListingPane = catalogManagement.doubleClickCategory(categoryName);

		verifyProductIsInList(productName, categoryName);
	}

	/**
	 * Verify product under assigned subcategory for a catalog.
	 *
	 * @param categoryName Category Name.
	 * @param catalogName  Catalog Name.
	 */
	@When("^newly created product is present in newly created subcategory under category (.+) for (.+)$")
	public void verifyProductLinkedWithSubcategory(final String categoryName, final String catalogName) {
		catalogManagement.clickCatalogBrowseTab();
		navigateToSubcategoryAndOpenProductList(catalogName, categoryName, this.product.getCategory());

		verifyProductIsInList(this.product.getProductName(), this.product.getCategory());
	}

	/**
	 * Verify product is not linked with specified category for a catalog.
	 *
	 * @param productName  Product Name.
	 * @param categoryName Category Name.
	 */
	@When("^product (.+) is not linked with (.+) Category$")
	public void verifyProductNotLinkedWithCategory(final String productName, final String categoryName) {
		catalogProductListingPane = catalogManagement.doubleClickCategory(categoryName);
		catalogProductListingPane.verifyProductNameNotExists(productName, categoryName);
	}

	/**
	 * Add product to Merchandising Associations Tab.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^I add product code (.+) to merchandising association (.+)$")
	public void addProductToMerchandisingTab(final String productCode, final String tabName) {
		merchandisingTab.clickMerchandisingTab(tabName);
		addEditMerchandisingAssociationsDialog = merchandisingTab.clickAddMerchandisingAssociationsButton();
		addEditMerchandisingAssociationsDialog.enterProductCode(productCode);
		dependentLineItem = productCode;
		addEditMerchandisingAssociationsDialog.selectPreviousMonthEnableDate();
		addEditMerchandisingAssociationsDialog.clickAddButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify Added Merchandising Associations product.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^the product code (.+) exists under merchandising association (.+)$")
	public void verifyProductInMerchandisingTab(final String productCode, final String tabName) {
		merchandisingTab.clickMerchandisingTab(tabName);
		merchandisingTab.verifySelectProductCode(productCode);
	}

	/**
	 * Edit product from Merchandising Associations Tab.
	 *
	 * @param oldProductCode Old Product Code.
	 * @param newProductCode New Product Code.
	 */
	@When("^I edit product code (.+) to (.+)")
	public void editMerchandisingTabProduct(final String oldProductCode, final String newProductCode) {
		merchandisingTab.verifySelectProductCode(oldProductCode);
		addEditMerchandisingAssociationsDialog = merchandisingTab.clickEditMerchandisingAssociationsButton();
		addEditMerchandisingAssociationsDialog.enterProductCode(newProductCode);
		addEditMerchandisingAssociationsDialog.clickSetButton();
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Delete product from Merchandising Associations Tab.
	 *
	 * @param productCode Product Code.
	 */
	@When("^I delete product code (.+)$")
	public void deleteMerchandisingTabProduct(final String productCode) {
		merchandisingTab.verifySelectProductCode(productCode);
		merchandisingTab.clickRemoveMerchandisingAssociationsButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.ProductMerchandisingAssociationDialog_RemoveTitle");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Deletes dependent item.
	 */
	@After(value = "@cleanupDependentItem", order = Constants.CLEANUP_ORDER_FIRST)
	public void deleteCatalogMerchandisingTabProduct() {
		new OrderEditor(SetUp.getDriver()).closePane("#" + Purchase.getPurchaseNumber());
		merchandisingTab.selectCatalogTab("Mobile Virtual Catalog");
		merchandisingTab.clickMerchandisingTab("Dependent Item");
		deleteMerchandisingTabProduct(dependentLineItem);
	}

	/**
	 * Verify Merchandising Associations product is deleted.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^the product code (.+) is no longer in merchandising association (.+)$")
	public void verifyProductIsDeletedFromMerchandisingTab(final String productCode, final String tabName) {
		merchandisingTab.clickMerchandisingTab(tabName);
		activityToolbar.clickReloadActiveEditor();
		merchandisingTab.verifyProductCodeIsDeleted(productCode);
		productEditor.closeProductEditor(this.product.getProductCode());
	}

	/**
	 * Save bundle editor.
	 */
	@Then("^I save bundle changes$")
	public void clickSaveAllIcon() {
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Checks if search result table is empty.
	 */
	@Then("^I should see empty result list$")
	public void verifyResultListIsEmpty() {
		int index = 0;
		while (!catalogSearchResultPane.isSearchResultTableEmpty() && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchForProductCode(this.product.getProductCode());
			index++;
		}
		assertThat(catalogSearchResultPane.isSearchResultTableEmpty())
				.as("Search result table should be empty, but contains at least one row")
				.isTrue();
	}

	/**
	 * Closes product search results tab.
	 */
	@Then("^I close product search results tab$")
	public void closeSearchResultsTab() {
		catalogSearchResultPane.closeProductSearchResultsPane();
	}

	/**
	 * Selects the catalog tab.
	 */
	@And("^I select the catalog (.+) tab$")
	public void selectCatalogTab(final String catalogName) {
		merchandisingTab.selectCatalogTab(catalogName);
	}

	/**
	 * Add product to Merchandising Associations Tab.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^I add code (.+) to merchandising association (.+)$")
	public void addProductToMerchandisingTabWithSpecificCode(final String productCode, final String tabName) {
		final String code = container.getProductCodeByPartialCode(productCode);
		merchandisingTab.clickMerchandisingTab(tabName);
		addEditMerchandisingAssociationsDialog = merchandisingTab.clickAddMerchandisingAssociationsButton();
		addEditMerchandisingAssociationsDialog.enterProductCode(code);
		addEditMerchandisingAssociationsDialog.clickAddButton();
	}

	/**
	 * Select Merchandising Associations Tab.
	 */
	@When("^I select merchandising associations tab$")
	public void addProductToMerchandisingTabWithSpecificCode() {
		merchandisingTab.selectMerchandisingTab();
	}

	/**
	 * Add product to Merchandising Associations Tab with values.
	 *
	 * @param productCode   Product Code.
	 * @param productValues product values for adding.
	 */
	@When("^I add code (.+) to merchandising and with values?$")
	public void addProductToMerchandisingTabWithSpecificCodeAndValues(final String productCode, final Map<String, String> productValues) {
		final String code = container.getProductCodeByPartialCode(productCode);
		merchandisingTab.clickMerchandisingTab(productValues.get("association"));
		addEditMerchandisingAssociationsDialog = merchandisingTab.clickAddMerchandisingAssociationsButton();
		addEditMerchandisingAssociationsDialog.addEnableDate(productValues.get(ENABLE_DATE));
		addEditMerchandisingAssociationsDialog.addDisableDate(productValues.get(DISABLE_DATE));
		addEditMerchandisingAssociationsDialog.enterProductCode(code);
		addEditMerchandisingAssociationsDialog.clickAddButton();
	}

	/**
	 * Move product down in Merchandising Associations Tab.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^I move product (.+) down to merchandising association (.+)$")
	public void addProductToMerchandisingTabDown(final String productCode, final String tabName) {
		final String code = container.getProductCodeByPartialCode(productCode);
		merchandisingTab.clickMerchandisingTab(tabName);
		merchandisingTab.verifySelectProductCode(code);
		merchandisingTab.selectMerchandisingTabDown();
	}

	/**
	 * Update product in Merchandising Associations Tab.
	 *
	 * @param productCode   Product Code.
	 * @param tabName       Merchandising Associations Tab.
	 * @param productValues product values for updating.
	 */
	@When("^I update product (.+) merchandising association (.+) with values?$")
	public void addProductToMerchandisingChangeDate(final String productCode, final String tabName, final Map<String, String> productValues) {
		final String code = container.getProductCodeByPartialCode(productCode);
		merchandisingTab.clickMerchandisingTab(tabName);
		merchandisingTab.verifySelectProductCode(code);
		merchandisingTab.clickEditMerchandisingAssociationsButton();
		addEditMerchandisingAssociationsDialog.addEnableDate(productValues.get(ENABLE_DATE));
		addEditMerchandisingAssociationsDialog.addDisableDate(productValues.get(DISABLE_DATE));
		addEditMerchandisingAssociationsDialog.clickSetButton();
	}

	/**
	 * Delete product from Merchandising Associations Tab.
	 *
	 * @param productCode Product Code.
	 * @param tabName     Merchandising Associations Tab.
	 */
	@When("^I delete code (.+) merchandising association (.+)$")
	public void deleteMerchandisingTabProductWithSpecificCode(final String productCode, final String tabName) {
		final String code = container.getProductCodeByPartialCode(productCode);
		merchandisingTab.clickMerchandisingTab(tabName);
		merchandisingTab.verifySelectProductCode(code);
		merchandisingTab.clickRemoveMerchandisingAssociationsButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.ProductMerchandisingAssociationDialog_RemoveTitle");
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Clears and Edit attribute value.
	 *
	 * @param attributeName name of attribute.
	 */
	@Then("^I edit (.+) attribute value for product$")
	public void editAttributeValueForProduct(final String attributeName) {
		attributesTab.selectTab("Attribute");
		attributesTab.clickClearAttribute(attributeContainer.getAttributeNameByPartialCodeAndLanguage(attributeName, ENGLISH));
		editDecimalValueAttributeDialog = attributesTab.clickEditAttributeButtonDecimalValue();
		editDecimalValueAttributeDialog.enterValue("value");
		editDecimalValueAttributeDialog.clickOKOnAttributeDialog();
	}

	/**
	 * Create new product.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^I create new product with following values?$")
	public void createProduct(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		if (categoryCodeStartsWith(product.getCategory())) {
			catalogManagement.expandCatalogAndVerifyCategory(product.getCatalog(), category.getCategoryName());
			catalogManagement.doubleClickCategory(category.getCategoryName());
		}
		container.addProducts(createNewProduct(product));
	}

	/**
	 * Update product.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^I update product with following values?$")
	public void updateProduct(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		verifyProductByName(this.product.getProductName());
		productEditor = catalogSearchResultPane.openProductEditorWithProductName(this.product.getProductName());
		productEditor.selectLanguage("French");
		productEditor.enterProductNameWithoutLoadedCheck(product.getProductName());
		productEditor.selectTaxCode(product.getTaxCode());
		productEditor.enterMinimumOrderQuantity(product.getMinimumOrderQuantity());
		selectNotSoldSeparately(product);
		final Date enableDate = Utility.getDateTimePlusDays(Integer.parseInt(product.getEnableDateTimeDays()));
		this.product.setEnableDateTime(enableDate);
		productEditor.setEnableDate(Utility.convertToDateFormat(enableDate));
		final Date disableDate = Utility.getDateTimePlusDays(Integer.parseInt(product.getDisableDateTimeDays()));
		this.product.setDisableDateTime(disableDate);
		productEditor.setDisableDate(Utility.convertToDateFormat(disableDate));
		productEditor.selectTab(SKU_DETAILS);
		productEditor.selectShippableType(product.getShippableType());
	}

	/**
	 * Update sku in product.
	 *
	 * @param offer the product info list.
	 */
	@When("^I update sku in product with following values?$")
	public void updateSkuInProduct(final Map<String, String> offer) {
		verifyProductByName(this.product.getProductName());
		productEditor = catalogSearchResultPane.openProductEditorWithProductName(this.product.getProductName());
		productEditor.selectTabInOpenedEditor(SKU_DETAILS);
		final String productSkuOptionCode = this.product.getSkuOptionCodeByPartialCode(offer.get("skuCode"));
		productEditor.selectSkuDetails(productSkuOptionCode);
		skuDetailsEditor = productEditor.clickOpenSkuButton();
		if (offer.get(ENABLE_DATE) != null) {
			skuDetailsEditor.selectTab(SKU_DETAILS);
			updateSkuEnableDate(offer.get(ENABLE_DATE), productSkuOptionCode);
		}
		if (offer.get("taxCode") != null) {
			skuDetailsEditor.selectTaxCode(offer.get("taxCode"));
		}
		if (offer.get("shippableType") != null) {
			skuDetailsEditor.selectShippableType(offer.get("shippableType"));
		}
		if (offer.get("attributeCode") != null) {
			clearAttributeValue(offer.get("attributeCode"));
		}
		if (offer.get(DISABLE_DATE) != null) {
			skuDetailsEditor.selectTab(SKU_DETAILS);
			updateSkuDisableDate(offer.get(DISABLE_DATE), productSkuOptionCode);
		}
	}

	/**
	 * Clear attribute value.
	 *
	 * @param attributeCode attribute code.
	 */
	private void clearAttributeValue(final String attributeCode) {
		skuDetailsEditor.selectTab("Attributes");
		attributesTab.clickClearAttribute(attributeContainer.getAttributeNameByPartialCodeAndLanguage(attributeCode, ENGLISH));
	}

	/**
	 * Adds bundle product price.
	 *
	 * @param product Product.
	 */
	private void addBundlePrice(final Product product) {
		if (product.getPriceList() != null && !activityToolbar.isChangeSetEnabled()) {
			createBundleWizard.clickNextInDialog();
			createBundleWizard.selectPriceList(product.getPriceList());
			basePriceEditorDialog = createBundleWizard.clickAddBasePriceButton();
			basePriceEditorDialog.enterListPrice(product.getListPrice());
			basePriceEditorDialog.clickOKButton();
		}
	}

	/**
	 * creates a new product with preselected category
	 *
	 * @param product a product instance
	 */
	private void createProduct(final Product product) {
		createProductWizard = catalogManagement.clickCreateProductButton();
		String prodCode = Utility.getRandomUUID();
		this.product.setProductCode(prodCode);
		String prodName = product.getProductName() + "-" + prodCode;
		this.product.setProductName(prodName);
		if (dst != null) {
			dst.setProductCode(prodCode);
			dst.setProductName(prodName);
		}
		createProductWizard.enterProductCode(prodCode);
		createProductWizard.enterProductName(this.product.getProductName());

		selectProductType(product);

		createProductWizard.selectTaxCode(product.getTaxCode());

		selectStoreVisiblity(product);

		createProductWizard.selectAvailabilityRule(product.getAvailability());
		createProductWizard.clickNextInDialog();

		enterProductAttribute(product);

		createProductWizard.clickNextInDialog();

		if (createProductWizard.isAddSKUButtonPresent()) {
			addMultiSkuValues(product);

		} else {
			createProductWizard.enterSkuCode(getUniqueSkuCode());
			createProductWizard.selectShippableType(product.getShippableType());
		}

		addPrice(product);
		createProductWizard.clickFinish();
	}

	/**
	 * navigates to a subcategory and opens a products list
	 *
	 * @param catalogName     a name of a Catalog for navigation
	 * @param categoryName    a name of a Category for navigation
	 * @param subcategoryName a name of a Subcategory for navigation
	 */
	private void navigateToSubcategoryAndOpenProductList(final String catalogName, final String categoryName, final String subcategoryName) {
		catalogManagement.expandCategoryAndVerifySubcategory(catalogName, categoryName, subcategoryName);
		catalogProductListingPane = catalogManagement.doubleClickSubcategory(catalogName, categoryName, subcategoryName);
	}

	/**
	 * verifies that a Product is present in an opened products list for a given category
	 *
	 * @param productName     a name of a Product
	 * @param productCategory a name of Product's Category
	 */
	private void verifyProductIsInList(final String productName, final String productCategory) {
		int index = 0;
		while (!catalogProductListingPane.isProductNameInList(productName) && index < Constants.UUID_END_INDEX) {
			catalogProductListingPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			catalogManagement.doubleClickCategory(productCategory);
			index++;
		}

		catalogProductListingPane.verifyProductNameExists(productName);
	}

	/**
	 * Searches for product and verifies that result table appears.
	 *
	 * @param runnable function to enter search key in appropriate field.
	 */
	private void searchForActiveProduct(final Runnable runnable) {
		catalogManagement.clickCatalogSearchTab();
		catalogManagement.clearInputFieldsInCatalogSearchTab();
		runnable.run();
		catalogManagement.selectActiveProductsFilter();
		catalogSearchResultPane = catalogManagement.clickCatalogSearch();
		catalogSearchResultPane.waitForSearchResultTableIsRendered();
	}

	/**
	 * Searches for sku and verifies that result table appears.
	 *
	 * @param runnable function to enter search key in appropriate field
	 */
	private void searchForSku(final Runnable runnable) {
		catalogManagement.clickCatalogSearchTab();
		catalogManagement.clickSkuSearchTab();
		catalogManagement.clearInputFieldsInCatalogSearchTab();
		runnable.run();
		catalogManagement.selectActiveSkusFilter();
		catalogSkuSearchResultPane = catalogManagement.clickCatalogSkuSearch();
		catalogSkuSearchResultPane.waitForSearchResultTableIsRendered();
	}

	/**
	 * Opens product editor for the first entry in a sku search result table and verifies that opened entity has provided sku code
	 *
	 * @param expectedSku expected sku code for verification
	 */
	private void verifySkuCodeInProductEditor(final String expectedSku) {
		productEditor = catalogSkuSearchResultPane.openProductEditorForFirstSearchResultEntry();
		productEditor.selectTab(ProductEditor.SKU_DETAILS_TAB_ID);
		assertThat(productEditor.getSkuCode())
				.as("Unexpected sku code in the search results")
				.isEqualTo(expectedSku);
	}

	/**
	 * Creates new product.
	 *
	 * @return new product.
	 */
	private Product createProductForContainer() {
		final Product productToContainer = new Product();

		productToContainer.setProductCode(this.product.getProductCode());
		productToContainer.setProductName(this.product.getProductName());
		productToContainer.setCatalog(this.product.getCatalog());
		productToContainer.setCategory(this.product.getCategory());

		return productToContainer;
	}

	/**
	 * reverts changed Enable date for a product using DB
	 */
	@After(value = "@cleanUpProductEnableDateDB", order = Constants.CLEANUP_ORDER_FIRST)
	public void saveCurrentProductEnableDatePropertyUsingDb() {
		DBConnector dbc = new DBConnector();
		SimpleDateFormat dbDateFormat = new SimpleDateFormat("YYYY-M-d HH:mm:ss", Locale.ENGLISH);
		Calendar calendar = Calendar.getInstance();
		/*we add 1 day to "last_modified_date" value to resolve potential issue
		of having different time zones on jenkins server and app under test server*/
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		dbc.updateProductEnableDate(
				this.product.getProductCode(), dbDateFormat.format(this.product.getEnableDateTime()), dbDateFormat.format(calendar.getTime()));
	}

	/**
	 * Check not sold separately box.
	 *
	 * @param product a product instance.
	 */
	private void selectNotSoldSeparately(final Product product) {
		if (product.getNotSoldSeparately().equalsIgnoreCase("true")) {
			productEditor.checkNotSoldSeparatelyBox();
		} else {
			assertThat(product.getStoreVisible())
					.as("Store visible value is invalid - " + product.getStoreVisible())
					.isEqualToIgnoringCase(FALSE_VALUE);
		}
	}

	/**
	 * Check that category code exist with a partial code.
	 *
	 * @param partialCode a category partial code.
	 */
	private Boolean categoryCodeStartsWith(final String partialCode) {
		return category.getCategoryName().startsWith(partialCode);
	}

	/**
	 * Creates a new product.
	 *
	 * @param product a product instance.
	 */
	private Product createNewProduct(final Product product) {
		createProductWizard = catalogManagement.clickCreateProductButton();
		final String prodCode = product.getProductCode() + Utility.getRandomUUID();
		this.product.setProductCode(prodCode);
		product.setProductCode(prodCode);
		createProductWizard.enterProductCode(prodCode);
		enterProductName(product, prodCode);
		createProductWizard.selectProductType(this.productType.getProductTypeName());
		createProductWizard.selectBrand(this.brand.getName(ENGLISH));
		createProductWizard.selectTaxCode(product.getTaxCode());
		selectStoreVisiblity(product);
		createProductWizard.enterMinimumOrderQuantity(Optional.ofNullable(product.getMinimumOrderQuantity()).orElse(null));
		createProductWizard.selectAvailabilityRule(product.getAvailability());
		enterEnableDate(product);
		Optional.ofNullable(product.getDisableDateTimeDays()).ifPresent(this::enterDisableDate);
		Optional.ofNullable(product.getReleaseDateTimeDays()).ifPresent(this::enterReleaseDate);
		createProductWizard.clickNextInDialog();
		if (product.getAttributesCodeList() != null) {
			List<String> attributeNameList = getAttributeNames(product);
			attributeNameList.forEach(name -> createProductWizard.enterAttributeText("attributeValue", name));
		}
		createProductWizard.clickNextInDialog();
		if (!createProductWizard.isAddSKUButtonPresent()) {
			enterSkuCode(product);
			createProductWizard.selectShippableType(product.getShippableType());
			enterShippingDetails(product);
			createProductWizard.clickFinish();
		}
		return product;
	}

	/**
	 * Get all attribute names.
	 *
	 * @param product a product instance.
	 */
	private List<String> getAttributeNames(final Product product) {
		return product.getAttributesCodeList().stream()
				.map(attributeName -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(attributeName, ENGLISH))
				.collect(Collectors.toList());
	}

	/**
	 * Enter and create product name.
	 *
	 * @param product a product instance.
	 */
	private void enterProductName(final Product product, final String prodCode) {
		final String prodName = product.getProductName() + "-" + prodCode;
		this.product.setProductName(prodName);
		product.setProductName(prodName);
		createProductWizard.enterProductName(prodName);
	}

	/**
	 * Enter sku code.
	 *
	 * @param product a product instance.
	 */
	private void enterSkuCode(final Product product) {
		uniqueSkuCode = product.getSkuCode() + Utility.getRandomUUID();
		createProductWizard.enterSkuCode(uniqueSkuCode);
		this.product.setSkuCode(uniqueSkuCode);
	}

	/**
	 * Enter disable date.
	 *
	 * @param days count of days.
	 */
	private void enterDisableDate(final String days) {
		final Date disableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(days));
		this.product.setDisableDateTime(disableDateTime);
		createProductWizard.enterDisableDateTime(Utility.convertToDateFormat(disableDateTime));
	}

	/**
	 * Enter disable date.
	 *
	 * @param days count of days.
	 */
	private void enterReleaseDate(final String days) {
		final Date releaseDateTime = Utility.getDateTimePlusDays(Integer.parseInt(days));
		this.product.setReleaseDateTime(DateUtils.truncate(releaseDateTime, Calendar.DATE));
		createProductWizard.enterReleaseDateTime(Utility.convertToSimpleDateFormat(releaseDateTime));
	}

	/**
	 * Enter enable date.
	 *
	 * @param product a product instance.
	 */
	private void enterEnableDate(final Product product) {
		final Date enableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(Optional.ofNullable(product.getEnableDateTimeDays()).orElse("0")));
		this.product.setEnableDateTime(enableDateTime);
		createProductWizard.enterEnableDateTime(Utility.convertToDateFormat(enableDateTime));
	}

	/**
	 * Enter shipping details in creating product.
	 *
	 * @param product a product instance.
	 */
	private void enterShippingDetails(final Product product) {
		createProductWizard.enterShippingWeight(Optional.ofNullable(product.getShippingWeight()).orElse(null));
		createProductWizard.enterShippingWidth(Optional.ofNullable(product.getShippingWidth()).orElse(null));
		createProductWizard.enterShippingLength(Optional.ofNullable(product.getShippingLength()).orElse(null));
		createProductWizard.enterShippingHeight(Optional.ofNullable(product.getShippingHeight()).orElse(null));
	}

	/**
	 * Enter sku shipping details in creating product.
	 *
	 * @param names values for the new localized names.
	 */
	private void enterSkuShippingDetails(final Map<String, String> names) {
		addSkuWizard.enterShippingHeight(Optional.ofNullable(names.get("height")).orElse(null));
		addSkuWizard.enterShippingLength(Optional.ofNullable(names.get("length")).orElse(null));
		addSkuWizard.enterShippingWeight(Optional.ofNullable(names.get("weight")).orElse(null));
		addSkuWizard.enterShippingWidth(Optional.ofNullable(names.get("width")).orElse(null));
	}

	/**
	 * Create SkuOption.
	 *
	 * @param names values for the new localized names.
	 */
	@When("^I add sku option in product with following values$")
	public void addSkuOptionWithoutSaving(final Map<String, String> names) {
		final ProductSkuItem productSkuItem = new ProductSkuItem();
		addSkuWizard = createProductWizard.clickAddSkuButton();
		final String skuCode = names.get("prodSkuCode") + Utility.getRandomUUID();
		productSkuItem.setItemCode(skuCode);
		addSkuWizard.enterSkuCode(skuCode);
		for (Map.Entry<String, String> option : names.entrySet()) {
			final SkuOption productSku = skuOptionContainer.getSkuOptionByPartialCode(option.getKey());
			if (productSku != null) {
				final String skuValueCode = productSku.getSkuOptionValueCodeByPartialCode(option.getValue());
				final Map<String, String> skuOptionValueName = productSku.getSkuOptionValueName(skuValueCode);
				addSkuWizard.selectSkuOptions(productSku.getCode(), skuValueCode + " - " + skuOptionValueName.get(ENGLISH));
			}
		}
		enterSkuShippingDetails(names);
		final Date enableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(Optional.ofNullable(names.get("enableDateTimeDays"))
				.orElse("0")));
		addSkuWizard.enterEnableDateTime(Utility.convertToSimpleDateFormat(enableDateTime));
		productSkuItem.setEnableDate(DateUtils.truncate(enableDateTime, Calendar.DATE));
		if (names.get("disableDateTimeDays") != null) {
			final Date disableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(names.get("disableDateTimeDays")));
			addSkuWizard.enterDisableDateTime(Utility.convertToSimpleDateFormat(disableDateTime));
			productSkuItem.setDisableDate(DateUtils.truncate(disableDateTime, Calendar.DATE));
		}
		addSkuWizard.selectShippableType(names.get("shippableType"));
		addSkuWizard.selectTaxCode(names.get("taxCode"));
		addSkuWizard.clickNextButton();
		Optional.ofNullable(names.get("attributesCodeList")).ifPresent(this::enterAttributesValue);
		product.setSkuOption(productSkuItem);
		addSkuWizard.clickFinish();
	}

	/**
	 * Enter attribute values in creating sku option in offer.
	 *
	 * @param nameList list of names.
	 */
	private void enterAttributesValue(final String nameList) {
		Arrays.stream(nameList.split(","))
				.map(attributeName -> attributeContainer.getAttributeNameByPartialCodeAndLanguage(attributeName, ENGLISH))
				.forEach(attributeName -> addSkuWizard.enterAttributeShortText("attributeValue", attributeName));
	}

	/**
	 * Update enable date.
	 *
	 * @param days a count of days.
	 * @param code product item code.
	 */
	private void updateSkuEnableDate(final String days, final String code) {
		final Date enableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(days));
		this.product.findProductSkuOptionItemByCode(code).setEnableDate(enableDateTime);
		skuDetailsEditor.enterEnableDateTime(Utility.convertToDateFormat(enableDateTime));
	}

	/**
	 * Update disable date.
	 *
	 * @param days a count of days.
	 * @param code product item code.
	 */
	private void updateSkuDisableDate(final String days, final String code) {
		final Date disableDateTime = Utility.getDateTimePlusDays(Integer.parseInt(days));
		this.product.findProductSkuOptionItemByCode(code).setDisableDate(disableDateTime);
		skuDetailsEditor.enterDisableDateTime(Utility.convertToDateFormat(disableDateTime));
	}

	/**
	 * Tab finish button in product.
	 */
	@When("^I finish creating sku product")
	public void finishCreatingSkuProduct() {
		createProductWizard.clickFinish();
	}
}

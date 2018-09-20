package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddItemDialog;
import com.elasticpath.selenium.dialogs.BasePriceEditorDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.SelectAProductDialog;
import com.elasticpath.selenium.domainobjects.Catalog;
import com.elasticpath.selenium.domainobjects.Category;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.ProductType;
import com.elasticpath.selenium.editor.product.tabs.BundleItemsTab;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CatalogManagement;
import com.elasticpath.selenium.resultspane.CatalogSearchResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.AddSkuWizard;
import com.elasticpath.selenium.wizards.CreateBundleWizard;
import com.elasticpath.selenium.wizards.CreateProductWizard;

/**
 * Product Definition.
 */
public class ProductAndBundleDefinition {
	private final CatalogManagement catalogManagement;
	private CreateBundleWizard createBundleWizard;
	private AddItemDialog addItemDialog;
	private AddSkuWizard addSkuWizard;
	private SelectAProductDialog selectAProductDialog;
	private CatalogSearchResultPane catalogSearchResultPane;
	private CreateProductWizard createProductWizard;
	private BasePriceEditorDialog basePriceEditorDialog;
	private final ActivityToolbar activityToolbar;
	private final BundleItemsTab bundleItemsTab;
	private final Product product;
	private final Catalog catalog;
	private final Category category;
	private final ProductType productType;
	private static String uniqueSkuCode = "";

	/**
	 * Constructor.
	 *
	 * @param catalog     Catalog object.
	 * @param category    Category object.
	 * @param productType ProductType object.
	 * @param product     Product.
	 */
	public ProductAndBundleDefinition(final Catalog catalog, final Category category, final ProductType productType, final Product product) {
		final WebDriver driver = SeleniumDriverSetup.getDriver();
		catalogManagement = new CatalogManagement(driver);
		bundleItemsTab = new BundleItemsTab(driver);
		activityToolbar = new ActivityToolbar(driver);
		this.catalog = catalog;
		this.category = category;
		this.productType = productType;
		this.product = product;
	}

	/**
	 * Create new product for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^I create new product with following attributes$")
	public void createNewProduct(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		if (product.getCatalog() == null || product.getCategory() == null) {
			catalogManagement.expandCatalog(this.catalog.getCatalogName());
			catalogManagement.doubleClickCategory(this.category.getCategoryName());
		} else {
			catalogManagement.expandCatalog(product.getCatalog());
			catalogManagement.doubleClickCategory(product.getCategory());
		}
		createProductWizard = catalogManagement.clickCreateProductButton();
		String prodCode = Utility.getRandomUUID();
		createProductWizard.enterProductCode(prodCode);
		this.product.setProductName(product.getProductName() + "-" + prodCode);
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
	 * Create new bundle for existing category.
	 *
	 * @param productInfoList the product info list.
	 */
	@When("^I create new bundle with following attributes$")
	public void createNewBundleForExistingCategory(final List<Product> productInfoList) {
		Product product = productInfoList.get(0);
		catalogManagement.expandCatalog(product.getCatalog());
		catalogManagement.doubleClickCategory(product.getCategory());
		createBundleWizard = catalogManagement.clickCreateBundleButton();
		String productCode = Utility.getRandomUUID();
		createBundleWizard.enterProductCode(productCode);
		this.product.setProductName(product.getProductName() + "-" + productCode);
		createBundleWizard.enterProductName(this.product.getProductName());
		createBundleWizard.selectBundlePricing(product.getBundlePricing());
		createBundleWizard.selectProductType(product.getProductType());
		createBundleWizard.selectBrand(product.getBrand());

		if (product.getStoreVisible().equalsIgnoreCase("true")) {
			createBundleWizard.checkStoreVisibleBox();
		} else {
			assertThat(product.getStoreVisible())
					.as("Store visible value is invalid - " + product.getStoreVisible())
					.isEqualToIgnoringCase("false");
		}

		createBundleWizard.clickNextInDialog();

		this.product.setProductCodes(product.getProductCodes());
		verifyProductCodesListExistInTestData();
		for (String bundleProductCode : this.product.getProductCodeList()) {
			addItemDialog = createBundleWizard.clickAddItemButton();
			addBundleItem(bundleProductCode);
		}

		createBundleWizard.clickNextInDialog();
		createBundleWizard.enterAttributeShortTextMultiValue(product.getAttrShortTextMultiValue(), product.getAttrShortTextMulti());
		createBundleWizard.enterAttributeIntegerValue(product.getAttrIntegerValue(), product.getAttrInteger());
		createBundleWizard.enterAttributeDecimalValue(product.getAttrDecimalValue(), product.getAttrDecimal());
		createBundleWizard.clickNextInDialog();
		createBundleWizard.enterSkuCode(getUniqueSkuCode());
		createBundleWizard.clickNextInDialog();
		createBundleWizard.clickFinish();
	}

	/**
	 * Verifies newly created product exists.
	 */
	@Then("^the newly created product is in the list$")
	public void verifyNewlyCreatedProductExists() {
		int index = 0;
		searchForProductByName(this.product.getProductName());

		while (!catalogSearchResultPane.isProductInList(this.product.getProductName()) && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchNewlyCreatedProductByName(this.product.getProductName());
			index++;
		}
		catalogSearchResultPane.setWebDriverImplicitWait(1);
		catalogSearchResultPane.verifyProductNameExists(this.product.getProductName());
		catalogSearchResultPane.setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Deletes newly created product.
	 */
	@When("^I delete the newly created (?:product|bundle)$")
	public void deleteNewlyCreatedProduct() {
		verifyProductByName();
		if (activityToolbar.isChangeSetEnabled()) {
			activityToolbar.addSelectedObjectToChangeSet();
			catalogSearchResultPane.isProductInList(this.product.getProductName());
		}
		catalogSearchResultPane.clickDeleteProductButton();
		new ConfirmDialog(SeleniumDriverSetup.getDriver()).clickOKButton("CatalogMessages.DeleteProduct");
	}

	/**
	 * Verifies product exists with the name productName.
	 */
	public void verifyProductByName() {
		int index = 0;
		searchForProductByName(this.product.getProductName());

		while (!catalogSearchResultPane.isProductInList(this.product.getProductName()) && index < Constants.UUID_END_INDEX) {
			catalogSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			searchNewlyCreatedProductByName(this.product.getProductName());
			index++;
		}
		catalogSearchResultPane.setWebDriverImplicitWait(1);
		catalogSearchResultPane.verifyProductNameExists(this.product.getProductName());
		catalogSearchResultPane.setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verify product is deleted.
	 */
	@Then("^the (?:product|bundle) is deleted$")
	public void verifyProductIsDeleted() {
		searchNewlyCreatedProductByName(this.product.getProductName());
		catalogSearchResultPane.verifyProductIsDeleted(this.product.getProductName());
	}

	/**
	 * Delete Newly Created product.
	 */
	@Then("^the newly created bundle exists and contains the added items$")
	public void verifyNewlyCreatedBundle() {
		verifyProductByName();
		catalogSearchResultPane.doubleClick(catalogSearchResultPane.getSelectedElement());
		bundleItemsTab.selectTab("Constituents");

		verifyProductCodesListExistInTestData();
		for (String productCode : this.product.getProductCodeList()) {
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
	 * Delete Newly Created product.
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

	private void verifyProductCodesListExistInTestData() {
		assertThat(this.product.getProductCodeList())
				.as("No product codes list provided in test data.")
				.isNotNull();
	}

	/**
	 * Searches for a product by name.
	 *
	 * @param productName product name.
	 */
	private void searchForProductByName(final String productName) {
		catalogManagement.clickCatalogSearchTab();
		catalogManagement.enterProductName(productName);
		catalogSearchResultPane = catalogManagement.clickCatalogSearch();
	}

	/**
	 * Searches for a newly created product by name.
	 *
	 * @param productName product name.
	 */
	private void searchNewlyCreatedProductByName(final String productName) {
		catalogManagement.enterProductName(productName);
		catalogSearchResultPane = catalogManagement.clickCatalogSearch();
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
					.isEqualToIgnoringCase("false");
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
		this.product.setProductCodes(product.getProductCodes());
		verifyProductCodesListExistInTestData();
		for (String productSkuCode : this.product.getProductCodeList()) {
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

}

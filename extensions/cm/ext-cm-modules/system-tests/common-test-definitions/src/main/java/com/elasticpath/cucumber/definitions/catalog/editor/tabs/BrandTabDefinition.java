/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddEditBrandDialog;
import com.elasticpath.selenium.domainobjects.Brand;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.editor.catalog.tabs.BrandsTab;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.util.Utility;

/**
 * Brand Tab Definitions.
 */
public class BrandTabDefinition {

	private final BrandsTab brandsTab;
	private final Product product;
	private AddEditBrandDialog addEditBrandDialog;
	private final Brand brand;

	/**
	 * Constructor.
	 *
	 * @param product variable for the product.
	 */
	public BrandTabDefinition(final Product product, final Brand brand) {
		final WebDriver driver = SeleniumDriverSetup.getDriver();
		this.brandsTab = new BrandsTab(driver);
		this.product = product;
		this.brand = brand;
	}

	/**
	 * Create Brand.
	 *
	 * @param brandCode the brand code.
	 * @param names     values for the new localized names.
	 */
	@When("^I add a new brand with brand code (.*) with the following names$")
	public void addBrandWithSelectionOfLanguage(final String brandCode, final Map<String, String> names) {
		addEditBrandDialog = brandsTab.clickAddBrandButton();
		final String brandCodeRandom = brandCode + Utility.getRandomUUID();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			fillBrandValues(brandCodeRandom, localizedName.getValue() + brandCodeRandom, localizedName.getKey());
		}
		addEditBrandDialog.clickAddButton();
	}

	/**
	 * Create Brand.
	 *
	 * @param brandCode the brand code.
	 * @param names     values for the new localized names.
	 */
	@When("^I add a new brand with brand code (.*) with the following exactly names$")
	public void addBrandWithSelectionOfLanguageAndExactlyNames(final String brandCode, final Map<String, String> names) {
		addEditBrandDialog = brandsTab.clickAddBrandButton();
		final String brandCodeRandom = brandCode + Utility.getRandomUUID();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			fillBrandValues(brandCodeRandom, localizedName.getValue(), localizedName.getKey());
		}
		addEditBrandDialog.clickAddButton();
	}


	/**
	 * Fills name of brand.
	 *
	 * @param language  language which should be selected.
	 * @param brandCode new brand code.
	 * @param brandName new brand name.
	 */
	private void fillBrandValues(final String brandCode, final String brandName, final String language) {
		this.product.setBrand(brandCode);
		addEditBrandDialog.enterBrandCode(brandCode);
		addEditBrandDialog.selectLanguage(language);
		addEditBrandDialog.enterBrandName(brandName);
		brand.setCode(brandCode);
		brand.setName(language, brandName);
	}

	/**
	 * Verify newly created brand exists.
	 */
	@Then("^Newly created brand is in the list by code$")
	public void verifyNewlyCreatedBrandExists() {
		brandsTab.verifyAndSelectBrand(addEditBrandDialog.getBrandCode());
	}

	/**
	 * Edit brand name.
	 *
	 * @param names values for the new localized names.
	 */
	@When("^I edit the brand name with the following new names$")
	public void editBrandName(final Map<String, String> names) {
		brandsTab.verifyAndSelectBrand(brand.getCode());
		addEditBrandDialog = brandsTab.clickEditBrandButton();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			addEditBrandDialog.selectLanguage(localizedName.getKey());
			addEditBrandDialog.enterBrandName(localizedName.getValue());
			brand.setName(localizedName.getKey(), localizedName.getValue());
		}
		addEditBrandDialog.clickAddButton();
	}

	/**
	 * Edit last 5 characters with random values of brand names without saving.
	 *
	 * @param languages values for the new localized names
	 */
	@When("^I edit last 5 characters of brand names with random characters for the following languages without saving$")
	public void editRandomlyBrandNames(final List<String> languages) {
		String oldName;
		String editedName;
		brandsTab.verifyAndSelectBrand(brand.getCode());
		addEditBrandDialog = brandsTab.clickEditBrandButton();
		for (String language : languages) {
			addEditBrandDialog.selectLanguage(language);
			oldName = addEditBrandDialog.getBrandDisplayName();
			brand.setOldName(language, oldName);
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			addEditBrandDialog.enterBrandName(editedName);
			brand.setName(language, editedName);
		}
		addEditBrandDialog.clickAddButton();
	}

	/**
	 * Delete new brand.
	 */
	@When("^I delete the created brand by code$")
	public void deleteNewBrand() {
		brandsTab.verifyAndSelectBrand(brand.getCode());
		brandsTab.clickRemoveBrandButton();
	}
}

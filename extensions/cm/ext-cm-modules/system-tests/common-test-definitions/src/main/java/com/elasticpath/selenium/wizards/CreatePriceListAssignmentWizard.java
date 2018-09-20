package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Price List Assignment Dialog.
 */
public class CreatePriceListAssignmentWizard extends AbstractWizard {

	private static final String CREATE_PLA_PARENT_CSS = "div[widget-id='Create Price List Assignment'] ";
	private static final String NAME_INPUT = CREATE_PLA_PARENT_CSS + "div[widget-id='Name'] > input";
	private static final String DESCRIPTION_INPUT = "div[automation-id='com.elasticpath.cmclient.pricelistmanager.PriceListManagerMessages"
			+ ".Description_Label'] > textarea";
	private static final String PRICE_LIST_PARENT = "div[widget-id='Price List Assignment'][widget-type='Table'] ";
	private static final String PRICE_LIST_LIST = PRICE_LIST_PARENT + "div[parent-widget-id='Price List Assignment'] div[column-id='%s']";
	private static final String CATALOG_PARENT = "div[widget-id='Price List Catalog'][widget-type='Table'] ";
	private static final String CATALOG_LIST = CATALOG_PARENT + "div[parent-widget-id='Price List Catalog'] div[column-id='%s']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreatePriceListAssignmentWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs price list assignment name.
	 *
	 * @param name String
	 */
	public void enterPriceListAssignmentName(final String name) {
		clearAndType(NAME_INPUT, name);
	}

	/**
	 * Enter Price List Assigment Description.
	 *
	 * @param descriptionText the description
	 */
	public void enterPriceListAssignmentDescription(final String descriptionText) {
		clearAndType(DESCRIPTION_INPUT, descriptionText);
	}

	/**
	 * Get the pricelist assignment description.
	 *
	 * @return the pricelist assignment description.
	 */
	public String getPriceListAssignmentDescription() {
		return getDriver().findElement(By.cssSelector(DESCRIPTION_INPUT)).getAttribute("value");
	}

	/**
	 * Selects a price list from given name.
	 *
	 * @param priceListName String
	 */
	public void selectPriceList(final String priceListName) {
		assertThat(selectItemInDialog(PRICE_LIST_PARENT, PRICE_LIST_LIST, priceListName, "Price List"))
				.as("Given Price List not found - " + priceListName)
				.isTrue();
	}

	/**
	 * Selects a catalog name from given name.
	 *
	 * @param catalogName String
	 */
	public void selectCatalogName(final String catalogName) {
		assertThat(selectItemInDialog(CATALOG_PARENT, CATALOG_LIST, catalogName, "Catalog"))
				.as("Given Catalog not found - " + catalogName)
				.isTrue();
	}

}
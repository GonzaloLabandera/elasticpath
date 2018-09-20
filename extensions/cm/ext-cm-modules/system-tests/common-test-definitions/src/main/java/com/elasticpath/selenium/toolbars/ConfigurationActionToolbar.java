package com.elasticpath.selenium.toolbars;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.ManageTaxValueDialog;
import com.elasticpath.selenium.navigations.UserSearch;
import com.elasticpath.selenium.resultspane.CustomerSegmentResultPane;
import com.elasticpath.selenium.resultspane.ProfileAttributePane;
import com.elasticpath.selenium.resultspane.ShippingRegionsPane;
import com.elasticpath.selenium.resultspane.StoresResultPane;
import com.elasticpath.selenium.resultspane.TaxCodesPane;
import com.elasticpath.selenium.resultspane.TaxJurisdictionsPane;
import com.elasticpath.selenium.resultspane.UserRolesResultPane;
import com.elasticpath.selenium.resultspane.WarehousesPane;

/**
 * Configuration Toolbar.
 */
public class ConfigurationActionToolbar extends AbstractToolbar {

	private static final String USER_ROLES_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.users"
			+ ".AdminUsersMessages.UserAdminSection_RoleAdmin']";
	private static final String WAREHOUSE_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.warehouses.AdminWarehousesMessages"
			+ ".UserAdminSection_UserAdmin']";
	private static final String PROFILE_ATTRIBUTE_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.customers.AdminCustomersMessages"
			+ ".CustomerAdminSection_ProfileAttributes']";
	private static final String USERS_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.users.AdminUsersMessages"
			+ ".UserAdminSection_UserAdmin']";
	private static final String STORE_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".StoreAdminSection_StoreAdmin']";
	private static final String SHIPPINGREGIONS_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.shipping.AdminShippingMessages"
			+ ".ShippingAdminItemCompositeFactory_RegionsAdmin']";
	private static final String TAXCODE_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.taxes.TaxesMessages"
			+ ".TaxesAdminSection_TaxCodes']";
	private static final String TAXJURISDICTION_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.taxes.TaxesMessages"
			+ ".TaxesAdminSection_TaxJurisdictionAdmin']";
	private static final String TAXVALUE_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.taxes.TaxesMessages"
			+ ".TaxesAdminSection_ManageTaxValues']";
	private static final String CUSTOMER_SEGMENT_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.customers.AdminCustomersMessages"
			+ ".CustomerAdminSection_CustomerSegments']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ConfigurationActionToolbar(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks on User Roles link.
	 *
	 * @return UserRolesResultPane
	 */
	public UserRolesResultPane clickUserRoles() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(USER_ROLES_LINK_CSS)));
		return new UserRolesResultPane(getDriver());
	}

	/**
	 * Clicks on Warehouse link.
	 *
	 * @return WarehousePane
	 */
	public WarehousesPane clickWarehouses() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(WAREHOUSE_LINK_CSS)));
		return new WarehousesPane(getDriver());
	}

	/**
	 * Clicks on Shipping Regions link.
	 *
	 * @return ShippingRegionsPane
	 */
	public ShippingRegionsPane clickShippingRegions() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SHIPPINGREGIONS_LINK_CSS)));
		return new ShippingRegionsPane(getDriver());
	}

	/**
	 * Verifies User Roles link is present.
	 */
	public void verifyUserRolesLinkIsPresent() {
		assertThat(isElementPresent(By.cssSelector(USER_ROLES_LINK_CSS)))
				.as("Unable to find User Roles link")
				.isTrue();
	}

	/**
	 * Clicks on Users link.
	 *
	 * @return UserSearch
	 */
	public UserSearch clickUsers() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(USERS_LINK_CSS)));
		return new UserSearch(getDriver());
	}

	/**
	 * Clicks on Stores link.
	 *
	 * @return StoresResultPane
	 */
	public StoresResultPane clickStores() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(STORE_LINK_CSS)));
		return new StoresResultPane(getDriver());
	}

	/**
	 * Clicks on Profile Attribute link.
	 *
	 * @return ProfileAttributePane
	 */
	public ProfileAttributePane clickProfileAttributes() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(PROFILE_ATTRIBUTE_LINK_CSS)));
		return new ProfileAttributePane(getDriver());
	}

	/**
	 * Clicks on Customer Segments link.
	 *
	 * @return CustomerSegmentResultPane
	 */
	public CustomerSegmentResultPane clickCustomerSegments() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CUSTOMER_SEGMENT_LINK_CSS)));
		return new CustomerSegmentResultPane(getDriver());
	}

	/**
	 * Clicks on Tax Code link.
	 *
	 * @return TaxCodesPane
	 */
	public TaxCodesPane clickTaxCode() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(TAXCODE_LINK_CSS)));
		return new TaxCodesPane(getDriver());
	}

	/**
	 * Clicks on Tax Jurisdiction link.
	 *
	 * @return TaxJurisdictionPane
	 */
	public TaxJurisdictionsPane clickTaxJurisdiction() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(TAXJURISDICTION_LINK_CSS)));
		return new TaxJurisdictionsPane(getDriver());
	}

	/**
	 * Clicks on Tax Value link.
	 *
	 * @return TaxValueDialog
	 */
	public ManageTaxValueDialog clickTaxValue() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(TAXVALUE_LINK_CSS)));
		return new ManageTaxValueDialog(getDriver());
	}

	/**
	 * Clicks Save  button.
	 */
	public void clickSaveButton() {
		clickButton(SAVE_BUTTON_CSS, "Save Button");
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(RELOAD_ACTIVE_EDITOR_BUTTON_CSS));
	}
}

package com.elasticpath.selenium.navigations;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.resultspane.PromotionSearchResultPane;
import com.elasticpath.selenium.resultspane.ShippingServiceLevelSearchResultPane;
import com.elasticpath.selenium.util.Constants;

/**
 * Promotions Shipping.
 */
public class PromotionsShipping extends AbstractNavigation {

	private static final String LEFT_PANE_INNER_PARENT_CSS = "div[pane-location='left-pane-inner'] ";
	private static final String PROMOTION_NAME_INPUT_CSS = LEFT_PANE_INNER_PARENT_CSS + "div[widget-id='Promotion Name'] > input";
	private static final String SEARCH_BUTTON_CSS = LEFT_PANE_INNER_PARENT_CSS + "div[widget-id='Search'][seeable='true']";
	private static final String SHIPPING_SERVICE_LEVEL_TAB_CSS = "div[widget-id*='Sh'][appearance-id='ctab-item'][seeable='true']";
	private final static String COMBO_BOX_APPEARANCE_ID = "div[appearance-id='ccombo']";
	private static final String SEARCH_FILTER_SHIPPING_REGION_CSS = COMBO_BOX_APPEARANCE_ID
			+ "[automation-id='com.elasticpath.cmclient.store.shipping.ShippingLevelsMessages.ShippingLevelsShippingRegionLabel']";
	private static final String SEARCH_FILTER_STORE_CSS = COMBO_BOX_APPEARANCE_ID
			+ "[automation-id='com.elasticpath.cmclient.store.shipping.ShippingLevelsMessages.ShippingLevelsStoreLabel']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public PromotionsShipping(final WebDriver driver) {
		super(driver);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(PROMOTION_NAME_INPUT_CSS));
	}

	/**
	 * Clicks on promotion search button.
	 *
	 * @return PromotionSearchResultPane
	 */
	public PromotionSearchResultPane clickPromotionSearchButton() {
		clickButtonAndWaitForPaneToOpen(SEARCH_BUTTON_CSS, "Search", PromotionSearchResultPane.getPromotionSearchResultParentCss());
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
		return new PromotionSearchResultPane(getDriver());
	}

	/**
	 * Inputs promotion name.
	 *
	 * @param promotionName the promotion name.
	 */
	public void enterPromotionName(final String promotionName) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(PROMOTION_NAME_INPUT_CSS)));
		clearAndType(PROMOTION_NAME_INPUT_CSS, promotionName);
	}

	/**
	 * Clicks on shipping service level search button.
	 *
	 * @return ShippingServiceLevelSearchResultPane
	 */
	public ShippingServiceLevelSearchResultPane clickShippingServiceSearchButton() {
		clickButton(SEARCH_BUTTON_CSS, "Search");
		return new ShippingServiceLevelSearchResultPane(getDriver());
	}

	/**
	 * Clicks on shipping service level tab.
	 */
	public void clickShippingServiceLevelTab() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SHIPPING_SERVICE_LEVEL_TAB_CSS)));
	}

	/**
	 * Chooses a value in Shipping Region filter from a drop down element in Shipping tab.
	 *
	 * @param region which should be chosen in a filter
	 */
	public void setShippingRegionFilter(final String region) {
		selectComboBoxItem(SEARCH_FILTER_SHIPPING_REGION_CSS, region);
	}

	/**
	 * Chooses a value in Store filter from a drop down element in Shipping tab.
	 *
	 * @param store which should be chosen in a filter
	 */
	public void setStoreFilter(final String store) {
		selectComboBoxItem(SEARCH_FILTER_STORE_CSS, store);
	}
}

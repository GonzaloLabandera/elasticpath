package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Create Catalog Promotion Wizard.
 */
public class CreateCatalogPromotionWizard extends AbstractWizard {

	private static final String CREATE_CATALOG_PROMOTION_PARENT_CSS = "div[widget-id='Create Catalog Promotion'][widget-type='Shell'] ";
	private static final String CATALOG_COMBO_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id='Catalog'][widget-type='CCombo']";
	private static final String NAME_INPUT_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id='Promotion Name'] > input";
	private static final String DISPLAY_NAME_INPUT_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS
			+ "div[widget-id=''][widget-type='Text'] > input";
	private static final String ENABLE_DATE_TIME_INPUT_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id = 'Enable Date/Time'] input";
	private static final String CONDITION_ICON_CSS
			= CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id='of these conditions are true'][widget-type='ImageHyperlink']";
	private static final String CONDITION_MENU_ITEM_CSS = "div[widget-id*='%s']";
	private static final String DISCOUNT_MENU_ITEM_CSS = "div[widget-id='%s']";
	private static final String DISCOUNT_SUB_MENU_ITEM_CSS = "div[widget-id*='%s']";
	private static final String DISCOUNT_ICON_CSS
			= CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id='The discount applied is'][widget-type='ImageHyperlink']";
	private static final String DISCOUNT_VALUE_INPUT_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS + "div[widget-id*='Get'] > input";
	private static final String SHOPPER_CONDITIONS_RADIO_BUTTON_CSS = CREATE_CATALOG_PROMOTION_PARENT_CSS
			+ "div[widget-id='Only Shoppers who match the following conditions'] > div[style*='.png']";
	private static final String COMMON_STRING = "div[automation-id='com.elasticpath.cmclient.conditionbuilder.plugin.ConditionBuilderMessages";
	private static final String ADD_SHOPPER_STATEMENT_BLOCK_CSS = COMMON_STRING + ".ConditionBuilder_AddConditionButton']";
	private static final String ADD_STATEMENT_CSS = "div[automation-id='com.elasticpath.cmclient.conditionbuilder.plugin.ConditionBuilderMessages"
			+ ".ConditionBuilder_Add_Rule_label']";
	private static final String STATEMENT_MENU_CSS = "div[appearance-id='menu'] div[widget-id='%s']";
	private static final String CONDITION_COMBO_BOX_CSS = "div[widget-id='Create Catalog Promotion'] "
			+ "div[appearance-id='ccombo'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateCatalogPromotionWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects catalog in combo box.
	 *
	 * @param catalogName the catalog name.
	 */
	public void selectCatalog(final String catalogName) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(CATALOG_COMBO_CSS));
		assertThat(selectComboBoxItem(CATALOG_COMBO_CSS, catalogName))
				.as("Unable to find catalog - " + catalogName)
				.isTrue();
	}

	/**
	 * Inputs promotion name.
	 *
	 * @param promotionName the promotion name.
	 */
	public void enterPromotionName(final String promotionName) {
		clearAndType(NAME_INPUT_CSS, promotionName);
	}

	/**
	 * Inputs promotion display name.
	 *
	 * @param displayName the display name.
	 */
	public void enterPromotionDisplayName(final String displayName) {
		clearAndType(DISPLAY_NAME_INPUT_CSS, displayName);
	}

	/**
	 * Inputs promotion enable date/time.
	 *
	 * @param enableDateTime the enabled date time.
	 */
	public void enterEnableDateTime(final String enableDateTime) {
		clearAndType(ENABLE_DATE_TIME_INPUT_CSS, enableDateTime);
	}

	/**
	 * Opens condition menu.
	 */
	public void openConditionMenu() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CONDITION_ICON_CSS)));
	}

	/**
	 * Selects condition's menu item.
	 *
	 * @param conditionMenuItem the condition menu item.
	 */
	public void selectConditionMenuItem(final String conditionMenuItem) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(CONDITION_MENU_ITEM_CSS, conditionMenuItem))));
	}

	/**
	 * Opens discount menu.
	 *
	 * @param discountMenuItem the discount menu item
	 */
	public void openDiscountMenuAndSelectMenuItem(final String discountMenuItem) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(DISCOUNT_ICON_CSS)));
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(DISCOUNT_MENU_ITEM_CSS, discountMenuItem))));
	}

	/**
	 * Selects discount's sub menu item.
	 *
	 * @param discountSubMenuItem the discount menu item.
	 */
	public void selectDiscountSubMenuItem(final String discountSubMenuItem) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(DISCOUNT_SUB_MENU_ITEM_CSS, discountSubMenuItem))));
	}

	/**
	 * Inputs discount value.
	 *
	 * @param discountValue the discount value.
	 */
	public void enterDiscountValue(final String discountValue) {
		clearAndType(DISCOUNT_VALUE_INPUT_CSS, discountValue);
	}

	/**
	 * Selects shopper conditions Radio button.
	 */
	public void clickShopperConditionsRadioButton() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SHOPPER_CONDITIONS_RADIO_BUTTON_CSS)));
	}

	/**
	 * Clicks button to create new Statement Block
	 */
	public void createNewStatementBlock() {
		clickButton(ADD_SHOPPER_STATEMENT_BLOCK_CSS, "add statement block");
	}

	/**
	 * Clicks button to create new statement within statement block
	 */
	public void addNewStatement() {
		click(ADD_STATEMENT_CSS);
	}

	/**
	 * Creates new statement with conditions
	 *
	 * @param mainMenuValue  first combo (main menu) value to select
	 * @param subMenuValue   second combo (sub menu) value to select
	 * @param conditionRule  condition
	 * @param conditionValue condition value
	 */
	public void selectStatementConditions(final String mainMenuValue, final String subMenuValue, final String conditionRule,
			final String conditionValue) {
		WebElement mainMenu = getDriver().findElement(By.cssSelector(String.format(STATEMENT_MENU_CSS, mainMenuValue)));
		Actions action = new Actions(getDriver());
		action.moveToElement(mainMenu).build().perform();
		getDriver().findElement(By.cssSelector(String.format(STATEMENT_MENU_CSS, subMenuValue))).click();
		getDriver().findElement(By.cssSelector(CONDITION_COMBO_BOX_CSS)).click();
		if (!selectComboBoxItem(CONDITION_COMBO_BOX_CSS + "[style*='z-index: 4']", conditionRule)) {
			fail("Unable to select condition: " + conditionRule);
		}
		if (!selectComboBoxItem(CONDITION_COMBO_BOX_CSS + "[style*='z-index: 3']", conditionValue)) {
			fail("Unable to select value: " + conditionValue);
		}
	}
}
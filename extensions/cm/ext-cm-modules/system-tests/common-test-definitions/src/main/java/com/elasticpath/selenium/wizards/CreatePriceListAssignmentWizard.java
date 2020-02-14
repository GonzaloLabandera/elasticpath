package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.elasticpath.selenium.util.DBConnector;

/**
 * Create Price List Assignment Dialog.
 */
public class CreatePriceListAssignmentWizard extends AbstractWizard {

	private static final String CREATE_PLA_PARENT_CSS = "div[widget-id='Create Price List Assignment'] ";
	private static final String WIZARD_PAGE_TITLE = "div[automation-id='com.elasticpath.cmclient.pricelistmanager.PriceListManagerMessages"
			+ ".Name_Priorty_Page_Description']";
	private static final String NAME_INPUT = CREATE_PLA_PARENT_CSS + "div[widget-id='Name'] > input";
	private static final String DESCRIPTION_INPUT = "div[automation-id='com.elasticpath.cmclient.pricelistmanager.PriceListManagerMessages"
			+ ".Description_Label'] > textarea";
	private static final String PRICE_LIST_PARENT = "div[widget-id='Price List Assignment'][widget-type='Table'] ";
	private static final String PRICE_LIST_LIST = PRICE_LIST_PARENT + "div[parent-widget-id='Price List Assignment'] div[column-id='%s']";
	private static final String CATALOG_PARENT = "div[widget-id='Price List Catalog'][widget-type='Table'] ";
	private static final String CATALOG_LIST = CATALOG_PARENT + "div[parent-widget-id='Price List Catalog'] div[column-id='%s']";
	private static final String COMMON_STRING = "div[automation-id='com.elasticpath.cmclient.conditionbuilder.plugin.ConditionBuilderMessages";
	private static final String STORES_TABLE_PARENT_CSS = COMMON_STRING + ".SelectedStores_Label'] ";
	private static final String STORES_TABLE_ROW_CSS = STORES_TABLE_PARENT_CSS + "div[widget-type='table_row']";
	private static final String PLA_RADIO_BUTTON_CSS = "div[widget-id='%s'][appearance-id='radio-button']";
	private static final String PRIORITY_SLIDER_CSS = CREATE_PLA_PARENT_CSS + "div[widget-type='Scale']";
	private static final String ADD_PLA_STATEMENT_BLOCK_CSS = COMMON_STRING + ".ConditionBuilder_AddConditionButton']";
	private static final String REMOVE_PLA_STATEMENT_BLOCK_CSS = COMMON_STRING + ".ConditionBuilder_Remove_Rule_label']";
	private static final String ADD_STATEMENT_CSS = "div[automation-id='com.elasticpath.cmclient.conditionbuilder.plugin.ConditionBuilderMessages"
			+ ".ConditionBuilder_Add_Rule_label']";
	private static final String REMOVE_STATEMENT_CSS = "div[automation-id='com.elasticpath.cmclient.conditionbuilder.plugin.ConditionBuilderMessages"
			+ ".ConditionBuilder_Remove_Statement_label']";
	private static final String STATEMENT_MENU_CSS = "div[appearance-id='menu'] div[widget-id='%s']";
	private static final String CONDITION_COMBO_BOX_CSS = "div[widget-id='Create Price List Assignment'] "
			+ "div[appearance-id='ccombo'][seeable='true']";
	private static final String AVAILABLE_STORES_TABLE_CSS = COMMON_STRING + ".AvailableStores_Label'][appearance-id='table']";
	private static final String ADD_STORE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.button_Add']";
	private static final List<String> steps = Arrays.asList("priority", "price List", "catalog", "shoppers", "time", "stores");

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
	 * Helper method that retrieves current PLA priority through PLA wizard UI.
	 *
	 * @return the current Price List Assignment's priority
	 */
	private int getCurrentPriority() {
		int priority = 0;
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		priority = Integer.parseInt(jse.executeScript("var widget = document.querySelector(\"" + PRIORITY_SLIDER_CSS + "\").rwtWidget;"
				+ "return widget.getParent().getChildren()[0].getChildren()[widget._selection-1].__user$element.textContent").toString());
		return priority;
	}

	/**
	 * Moves the Price List Assignment priority slider.
	 *
	 * @param priority String
	 */
	public void selectPriceListAssignmentPriority(final String priority) {
		getDriver().findElement(By.cssSelector(PRIORITY_SLIDER_CSS)).click();
		int actual = getCurrentPriority();
		int desired = Integer.parseInt(priority);
		int difference = actual - desired;

		if (difference != 0) {
			CharSequence arrowKeyToClick = difference < 0 ? Keys.ARROW_LEFT : Keys.ARROW_RIGHT;
			for (int i = 0; i < Math.abs(difference); i++) {
				getDriver().findElement(By.cssSelector(PRIORITY_SLIDER_CSS)).sendKeys(arrowKeyToClick);
			}
		}

		actual = getCurrentPriority();
		assertThat(desired)
				.as("Expected priority did not match actual priority")
				.isEqualTo(actual);
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

	/**
	 * Skips to to a specific step in Price List Assignment creation wizard.
	 *
	 * @param step the step in the wizard to skip to.
	 */
	public void skipToStep(final String step) {
		int desiredPageIndex = steps.indexOf(step);
		int currentPageIndex = 0;
		for (String page : steps) {
			if (getWaitDriver().waitForElementToBeVisible(By.cssSelector(WIZARD_PAGE_TITLE)).getAttribute("widget-id").toLowerCase(Locale.ENGLISH).contains(page.toLowerCase(Locale.ENGLISH))) {
				currentPageIndex = steps.indexOf(page);
			}
		}

		int difference = currentPageIndex - desiredPageIndex;
		if (difference != 0) {
			String direction = difference < 0 ? "FORWARD" : "BACK";
			for (int i = 0; i < Math.abs(difference); i++) {
				switch (direction) {
					case "BACK":
						clickBackInDialog();
						break;
					case "FORWARD":
						clickNextInDialog();
						break;
					default:
						break;
				}
			}
		}
	}

	/**
	 * Selects Radio button by display name.
	 *
	 * @param radioButtonName String
	 */
	public void selectRadioButton(final String radioButtonName) {
		clickButton(String.format(PLA_RADIO_BUTTON_CSS, radioButtonName), radioButtonName);
	}

	/**
	 * Verify that the list of actual stores visible in UI matches those expected.
	 *
	 * @param storeList list of stores
	 */
	public void verifyAvailableStores(final List<String> storeList) {
		selectRadioButton("Assign Specific Stores");

		List<WebElement> actualStores = getDriver().findElements(By.cssSelector(STORES_TABLE_ROW_CSS));
		String[] actualStoreArray = new String[actualStores.size()];
		for (int i = 0; i < actualStoreArray.length; i++) {
			actualStoreArray[i] = actualStores.get(i).getText();
		}

		assertThat(storeList)
				.as("Store list is not as expected")
				.containsExactlyInAnyOrder(actualStoreArray);
	}

	/**
	 * Verify that the list of stores presented in UI matches those stored in the DB.
	 */
	public void verifyAllAvailableStores() {
		DBConnector dbConnector = new DBConnector();
		List<String> allStores = dbConnector.getAllStores();
		dbConnector.closeAll();

		verifyAvailableStores(allStores);
	}

	/**
	 * Clicks button to create new Statement Block
	 */
	public void createNewStatementBlock() {
		clickButton(ADD_PLA_STATEMENT_BLOCK_CSS, "add statement block");
	}

	/**
	 * Clicks button to remove a Statement Block
	 */
	public void removeStatementBlock() {
		clickButton(REMOVE_PLA_STATEMENT_BLOCK_CSS, "remove statement block");
	}

	/**
	 * Clicks button to create new statement within statement block
	 */
	public void addNewStatement() {
		click(ADD_STATEMENT_CSS);
	}

	/**
	 * Clicks button to remove statement in statement block
	 */
	public void removeStatemnt() {
		clickButton(REMOVE_STATEMENT_CSS, " remove statement");
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

	/**
	 * Assigns price list assignment to store(s)
	 *
	 * @param assignedStores list of stores to assign to
	 */
	public void assignPLAToStores(final List<String> assignedStores) {
		for (String storeName : assignedStores) {
			String storeRecordSCC = AVAILABLE_STORES_TABLE_CSS + " div[row-id='%s']";
			click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(storeRecordSCC, storeName))));
			clickAddStoreButton();
		}
	}

	/**
	 * Clicks button to assign selected store
	 */
	private void clickAddStoreButton() {
		clickButton(ADD_STORE_BUTTON_CSS, ">");
	}

}
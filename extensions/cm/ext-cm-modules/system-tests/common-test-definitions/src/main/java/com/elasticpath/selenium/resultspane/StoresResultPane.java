package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.editor.store.StoreEditor;
import com.elasticpath.selenium.util.Constants;

/**
 * Stores Result Pane.
 */
public class StoresResultPane extends AbstractPageObject {
	private static final String TARGET_STORE_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.views.StoreListView'][seeable='true'] "
			+ "div[row-id='%s']";
	private static final String EDIT_STORE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages.EditStore']";
	private static final String CREATE_STORE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".CreateStore']";
	private static final String STORE_LIST_PARENT_CSS = "div[widget-id='Store'][widget-type='Table'] ";
	private static final String STORE_LIST_CSS = STORE_LIST_PARENT_CSS + "div[column-id='%s']";
	private static final String STORE_STATE_CSS = "div[row-id='%s'] div[column-num='3']";
	private static final String DELETE_STORE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores.AdminStoresMessages"
			+ ".DeleteStore']";
	private static final String DELETE_STORE_OK_BUTTON_CSS = "div[widget-id='OK']";
	private static final String MAXIMIZE_WINDOW_CSS = "div[pane-location='center-pane-outer'] div[appearance-id='ctabfolder-button']"
			+ "[widget-id='Maximize'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public StoresResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Edits given store code.
	 *
	 * @param storeCode the store code
	 * @return StoreEditor
	 */
	public StoreEditor editStore(final String storeCode) {
        assertThat(selectItemInCenterPaneWithoutPagination(STORE_LIST_PARENT_CSS, STORE_LIST_CSS, storeCode, "Store Name"))
                .as("Store does not exist in the list - " + storeCode)
                .isTrue();
		clickButton(EDIT_STORE_BUTTON_CSS, "Edit Store Button");
		return new StoreEditor(getDriver());
	}

	/**
	 * Maximize store window and get all codes of exist stores.
	 *
	 * @return List of codes all exist stores.
	 */
	public List<String> getExistStoresCodesList() {
		if (!getDriver().findElements(By.cssSelector(MAXIMIZE_WINDOW_CSS)).isEmpty()) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MAXIMIZE_WINDOW_CSS)));
		}
		return findStoreCodesInTable();
	}

	/**
	 * Find all codes of exist stores.
	 *
	 * @return List of codes all exist stores.
	 */
	private List<String> findStoreCodesInTable() {
		return getDriver()
				.findElements(By.cssSelector("div[widget-type='table_row'][parent-widget-id='Store']>div[column-num='0']"))
				.stream()
				.map(WebElement::getText)
				.collect(Collectors.toList());
	}

	/**
	 * Clicks create store button.
	 *
	 * @return Store Editor
	 */
	public StoreEditor clickCreateStoreButton() {
		clickButton(CREATE_STORE_BUTTON_CSS, "Create Store Button");
		return new StoreEditor(getDriver());
	}

	/**
	 * Verifies if store exists.
	 *
	 * @param storeName String
	 */
	public void verifyStoreExists(final String storeName) {
		assertThat(selectItemInCenterPaneWithoutPagination(STORE_LIST_PARENT_CSS, STORE_LIST_CSS, storeName, "Store Name"))
				.as("Store does not exist in the list - " + storeName)
				.isTrue();
	}

	/**
	 * Verifies stores state.
	 *
	 * @param expState  String
	 * @param storeCode String
	 */
	public void verifyStoreState(final String expState, final String storeCode) {
		getWaitDriver().waitForTextInElement(String.format(STORE_STATE_CSS, storeCode), expState);
		assertThat(getDriver().findElement(By.cssSelector(String.format(STORE_STATE_CSS, storeCode))).getText().equals(expState))
				.as("Store state should be " + expState)
				.isTrue();
	}


	/**
	 * Verifies store no longer appears in list.
	 *
	 * @param storeCode String
	 */
	public void verifyStoreDoesntExists(final String storeCode) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(verifyItemIsNotInCenterPaneWithoutPagination(STORE_LIST_PARENT_CSS, STORE_LIST_CSS, storeCode, "Store Code"))
				.as("Delete failed, store is still in the list - " + storeCode)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Deletes a store.
	 *
	 * @param storeCode String
	 */
	public void deleteStore(final String storeCode) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(TARGET_STORE_CSS, storeCode))));
		clickButton(DELETE_STORE_BUTTON_CSS, "Delete Store Button");
		clickButton(DELETE_STORE_OK_BUTTON_CSS, "Delete Store OK button");
	}
}

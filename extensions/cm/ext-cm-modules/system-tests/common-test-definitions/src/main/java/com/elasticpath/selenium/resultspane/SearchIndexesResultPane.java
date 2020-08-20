package com.elasticpath.selenium.resultspane;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;

public class SearchIndexesResultPane extends AbstractPageObject {

	private static final String REBUILD_INDEX_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages.RebuildIndex']";
	private static final String INDEX_TABLE_CSS = "div[widget-id='Search Index'][widget-type='Table']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public SearchIndexesResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Initiate specified search index rebuild process
	 *
	 * @param indexName search index name
	 */
	public void rebuildIndexByName(final String indexName) {
		List<WebElement> allRecords = getAllIndexes();
		for (WebElement record : allRecords) {
			if (indexName.equals(record.getAttribute("widget-id"))) {
				click(record);
				break;
			}
		}
		clickRebuildIndexButton();
	}

	/**
	 * Verifies specified index status
	 *
	 * @param indexName index name to be checked
	 * @return actual status
	 */
	public String getSearchIndexStatus(final String indexName) {
		String status = "UNKNOWN";
		List<WebElement> allRecords = getAllIndexes();
		for (WebElement record : allRecords) {
			if (indexName.equals(record.getAttribute("widget-id"))) {
				click(record);
				status = record.findElement(By.cssSelector("div[column-num='2']")).getText();
				break;
			}
		}
		return status;
	}

	/**
	 * Clicks Rebuild Index button
	 */
	private void clickRebuildIndexButton() {
		getWaitDriver().waitForButtonToBeEnabled(REBUILD_INDEX_BUTTON_CSS);
		click(By.cssSelector(REBUILD_INDEX_BUTTON_CSS));
		new ConfirmDialog(SeleniumDriverSetup.getDriver()).clickOKButton("com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages.RebuildConfirmTitle");
	}

	/**
	 * Gets all records in Indexes table
	 *
	 * @return List of WebElements
	 */
	private List<WebElement> getAllIndexes() {
		return getWaitDriver().waitForElementToBeVisible(By.cssSelector(INDEX_TABLE_CSS)).findElements(By.cssSelector("div[row-id]"));
	}
}

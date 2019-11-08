package com.elasticpath.selenium.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ConfigureRangeFacetDialog extends ConfigureFacetDialog {

	private static final String FACET_CONFIGURE_RANGE_FACET_DIALOG_CSS = "div[automation-id='com.elasticpath.cmclient.admin.stores"
			+ ".AdminStoresMessages.ConfigureRangeFacet']";
	private static final String FACET_CONFIGURE_RANGE_FACET_DIALOG_REQUIRED_FIELDS_CSS = FACET_CONFIGURE_RANGE_FACET_DIALOG_CSS + " div[widget-id"
			+ "='Values'] ~ div[widget-type='Text'] input";
	private static final String ADD_FACET_RANGE_ROW_CSS = FACET_CONFIGURE_RANGE_FACET_DIALOG_CSS + " div[appearance-id='scrolledcomposite'] + div >"
			+ " div";
	private static final String LOWER_BOUND = "Lower Bound";
	private static final String UPPER_BOUND = "Upper Bound";
	private static final String LABEL = "Label";

	private final WebDriver driver;

	/**
	 * constructor.
	 *
	 * @param driver the driver.
	 */
	public ConfigureRangeFacetDialog(final WebDriver driver) {
		super(driver);
		this.driver = driver;
	}

	public void setRequiredRangeValues(final Map<String, String> facetRangeValues) {
		waitForElementToLoad(driver.findElement(By.cssSelector(FACET_CONFIGURE_RANGE_FACET_DIALOG_CSS)));
		List<WebElement> requiredFields = getDriver().findElements(By.cssSelector(FACET_CONFIGURE_RANGE_FACET_DIALOG_REQUIRED_FIELDS_CSS));

		moveFocusToElement(requiredFields.get(0));
		clearAndType(requiredFields.get(0), facetRangeValues.get(LOWER_BOUND));
		clearAndType(requiredFields.get(1), facetRangeValues.get(UPPER_BOUND));
		clearAndType(requiredFields.get(2), facetRangeValues.get(LABEL));
	}

	public void setFacetRangeOptionalRangeValues(final List<Map<String, String>> facetRangeValues) {
		for (int i = 0; i < facetRangeValues.size(); i++) {

			clearAndType(driver.findElements(By.xpath("//div[@widget-id='-']/../div[1]/input")).get(i), facetRangeValues.get(i).get(LOWER_BOUND));
			clearAndType(driver.findElements(By.xpath("//div[@widget-id='-']/../div[4]/input")).get(i), facetRangeValues.get(i).get(UPPER_BOUND));
			clearAndType(driver.findElements(By.xpath("//div[@widget-id='-']/../div[6]/input")).get(i), facetRangeValues.get(i).get(LABEL));
		}
	}

	private void addFacetRangeRows(final int rowsToAdd) {
		for (int i = 0; i < rowsToAdd; i++) {
			click(ADD_FACET_RANGE_ROW_CSS);
		}
	}

	private void removeFacetRangeRows(final int indexFrom, final int indexTo) {
		List<WebElement> removeRangeButtons = driver.findElements(By.xpath("//div[@widget-id='-']/../div[7]//div"));
		List<WebElement> removeRangeButtonsToClick = new ArrayList<>(removeRangeButtons.subList(indexFrom, indexTo));

		for(WebElement removeButton :removeRangeButtonsToClick ) {
			click(removeButton);
		}
	}

	public void setRangeValues(final List<Map<String, String>> facetRangeValues) {
		int numExpectedRows = facetRangeValues.size();
		int numActualRows = driver.findElements(By.xpath("//div[@widget-id='-']/..")).size();
		int difference = Math.abs(numExpectedRows - numActualRows);

		if (numActualRows < numExpectedRows) {
			addFacetRangeRows(difference);
		} else if (numActualRows > numExpectedRows) {
			removeFacetRangeRows(facetRangeValues.size() - difference - 1, facetRangeValues.size() - 1);
		}

		setRequiredRangeValues(facetRangeValues.get(0));

		List<Map<String, String>> copy = new ArrayList<>(facetRangeValues);
		copy.remove(0);

		setFacetRangeOptionalRangeValues(copy);

	}


}

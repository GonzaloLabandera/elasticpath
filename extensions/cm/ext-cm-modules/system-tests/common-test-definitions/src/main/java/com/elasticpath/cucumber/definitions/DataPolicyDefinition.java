/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cucumber.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortex.dce.datapolicies.DataPolicySteps;
import com.elasticpath.cortexTestObjects.Profile;
import com.elasticpath.selenium.dialogs.AddViewDataPointDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.editor.DataPolicyEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.DataPolicyResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.Utility;

/**
 * Data Policy step definitions.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class DataPolicyDefinition {
	private DataPolicyResultPane dataPolicyResultPane;
	private final ConfigurationActionToolbar configurationActionToolbar;
	private DataPolicyEditor dataPolicyEditor;
	private final ActivityToolbar activityToolbar;
	private AddViewDataPointDialog addViewDataPointDialog;
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public DataPolicyDefinition() {
		driver = SeleniumDriverSetup.getDriver();
		configurationActionToolbar = new ConfigurationActionToolbar(driver);
		activityToolbar = new ActivityToolbar(driver);
	}

	/**
	 * Clicks on Data Policies.
	 */
	@When("^I go to Data Policies$")
	public void clickDataPolicies() {
		activityToolbar.clickConfigurationButton();
		dataPolicyResultPane = configurationActionToolbar.clickDataPolicies();
	}

	/**
	 * Click on Create Data Policy Button and Enter invalid Retention Period.
	 *
	 * @param retentionPeriod as text instead on integer
	 */
	@When("^I Create Data Policy with invalid Retention Period (.+)$")
	public void enterRetentionPeriod(final String retentionPeriod) {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.enterDataPolicyRetentionPeriod(retentionPeriod);
	}

	/**
	 * Verify Retention Period Validation.
	 *
	 * @param retentionValidation message
	 */
	@Then("^I can see Retention Period validation displayed as (.+)$")
	public void verifyRetentionPeriodValidation(final String retentionValidation) {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.verifyRetentionPeriodValidation(retentionValidation);
	}

	/**
	 * Create Data Policy with End date before Start date.
	 */
	@When("^I Create Data Policy with End Date before Start Date$")
	public void enterInvalidStartEndDate() {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.enterDateTime("StartDate", 0);
		dataPolicyEditor.enterDateTime("EndDate", -1);
	}

	/**
	 * Verify End Date Validation.
	 *
	 * @param endDateValidation message
	 */
	@Then("^I can see Date validation displayed as (.+)$")
	public void verifyEndDateValidation(final String endDateValidation) {
		dataPolicyEditor.verifyEndDateValidation(endDateValidation);
	}

	/**
	 * Click on Create Data Policy Button and.
	 * Enter null value in Data Policy Name and click on Save.
	 */
	@When("^I Create Data Policy without entering required fields$")
	public void enterNullValuesForDataPolicy() {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.enterDataPolicyName(" ");
		configurationActionToolbar.saveAll();
		new ConfirmDialog(driver).clickOKButton("AbstractCmClientFormEditor_ErrorTitle_save");
	}

	/**
	 * Verify Validations displayed for Name Key Retention & Date.
	 *
	 * @param validationFieldName Expected number of Validations
	 */
	@Then("^I can see validation error messages for the following fields:$")
	public void verifyNumberOfValidations(final List<String> validationFieldName) {
		for (String nameField : validationFieldName) {
			dataPolicyEditor.verifyValidationsReturned(nameField);
		}
	}

	/**
	 * click on Create Data Policy Button.
	 * enter all required fields except Data Policy Segment and click on Save
	 */
	@Then("^I am unable to Create Data Policy without data policy segment$")
	public void enterRequiredValuesExceptSegment() {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.enterDataPolicyName("Test");
		dataPolicyEditor.enterDataPolicyReferenceKey("Test");
		dataPolicyEditor.enterDataPolicyRetentionPeriod("1");
		dataPolicyEditor.enterDateTime("StartDate", 0);
		configurationActionToolbar.saveAll();
		new ConfirmDialog(driver).clickOKButton("DataPolicyEditor_SegmentsPage_SegmentsRequiredTitle");
	}

	/**
	 * Create Data Policy.
	 *
	 * NOTE: Data policy name is appended with UUID. If you need to refer to it use {@link DataPolicyDefinition#selectNewlyCreatedDataPolicy()}.
	 *
	 * @param dataPolicyMap the Data Policy map.
	 */
	@When("^(?:I create a|a) new Data Policy with following values$")
	public void createDataPolicy(final Map<String, String> dataPolicyMap) {
		clickDataPolicies();
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		String dataPolicyUnique = "dp" + Utility.getRandomUUID();
		DataPolicyNameHolder.setName(dataPolicyMap.get("policy-name") + "-" + dataPolicyUnique);
		dataPolicyEditor.enterDataPolicyName(DataPolicyNameHolder.getName());
		dataPolicyEditor.enterDataPolicyReferenceKey(dataPolicyMap.get("policy-reference-key"));
		dataPolicyEditor.enterDataPolicyRetentionPeriod(dataPolicyMap.get("retention days"));
		dataPolicyEditor.enterDataPolicyState(dataPolicyMap.get("state"));
		dataPolicyEditor.enterDateTime("StartDate", 0);
		String dataPoints = dataPolicyMap.get("data points");
		for (String dataPointName : dataPoints.replace(", ", ",").split(",")) {
			dataPolicyEditor.enterAvailableDataPoint(dataPointName);
		}
		dataPolicyEditor.enterDataPolicySegment(dataPolicyMap.get("segment"));
		configurationActionToolbar.saveAll();
		dataPolicyEditor.closeNewDataPolicyEditor();
	}

	/**
	 * Create Data Point.
	 *
	 * @param dataPointMap the Data Point map.
	 */
	@When("^I Create Data points with following existing values$")
	public void createDataPoint(final Map<String, String> dataPointMap) {
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.clickTab("Data Points");
		addViewDataPointDialog = dataPolicyEditor.clickCreateDataPointButton();
		addViewDataPointDialog.enterDataPointName(dataPointMap.get("data point name"));
		addViewDataPointDialog.selectDataLocation(dataPointMap.get("location key"));
		addViewDataPointDialog.selectDataKey(dataPointMap.get("data key"));
		addViewDataPointDialog.enterDataPointDescription(dataPointMap.get("description"));
		addViewDataPointDialog.clickSave();
	}

	/**
	 * Create Data Point.
	 *
	 * @param dataPointMap the Data Point map.
	 */
	@When("^I ensure Data points with following values exists$")
	public void ensureDataPointExists(final Map<String, String> dataPointMap) {
		clickDataPolicies();
		dataPolicyEditor = dataPolicyResultPane.clickCreateDataPolicyButton();
		dataPolicyEditor.clickDataPointsTab();

		String dataPointName = dataPointMap.get("data point name");

		// Create data point if it doesn't exist
		if (dataPolicyEditor.checkDataPointIsMissing(dataPointName)) {
			createDataPoint(dataPointMap);
			dataPolicyEditor.closeNewDataPolicyEditor();
			ignoreValidationMessage();
		} else {
			dataPolicyEditor.closeNewDataPolicyEditor();
		}
	}

	private void ignoreValidationMessage() {
		new ConfirmDialog(driver).clickCancelButton();
	}

	/**
	 * Verify Data Point Validation pop message.
	 *
	 * @param dataPointValidation for data policy.
	 */
	@Then("^I can see Validation message for (.+)$")
	public void verifyDataPointNameKeyValidation(final String dataPointValidation) {
		new ConfirmDialog(driver).clickOKButton("DataPolicyEditor_DataPoints_" + dataPointValidation);
	}

	/**
	 * Verify newly created Data Policy.
	 */
	@Then("^the newly created Data Policy exists in the list$")
	public void verifyNewDataPolicyExists() {
		dataPolicyResultPane.verifyDataPolicyExists(DataPolicyNameHolder.getName());
	}

	/**
	 * Selects newly created data policy.
	 */
	@And("^I select newly created data policy$")
	public void selectNewlyCreatedDataPolicy() {
		Profile.selectDataPolicy(DataPolicyNameHolder.getName());
	}


	/**
	 * click On Edit Data Policy Button.
	 */
	@When("^I edit recent Data Policy$")
	public void clickEditDataPolicy() {
		dataPolicyEditor = dataPolicyResultPane.editDataPolicyEditor(DataPolicyNameHolder.getName());
	}

	/**
	 * Edit an exiting Draft Data Policy.
	 *
	 * @param policyName String
	 */
	@When("^I edit an existing Draft Data Policy (.+)$")
	public void editDraftDataPolicy(final String policyName) {
		dataPolicyEditor = dataPolicyResultPane.editDataPolicyEditor(policyName);
	}

	/**
	 * Select and Open an Existing Active Data Policy.
	 *
	 * @param dataPolicyName for data policy
	 */
	@When("^I open an existing Active data policy (.+)$")
	public void openActiveDataPolicyEditor(final String dataPolicyName) {
		dataPolicyEditor = dataPolicyResultPane.viewDataPolicyEditor(dataPolicyName);
	}

	/**
	 * Active Data Policy does not show Create Data Point button.
	 */
	@Then("^I can not Create Data Point for Active Policy$")
	public void verifyDataPointButton() {
		dataPolicyEditor.clickTab("Data Points");
		dataPolicyEditor.verifyDataPointButtonIsNotPresent();
	}

	/**
	 * click On Disable Data Policy Button.
	 */
	@When("^I click disable data policy button$")
	public void clickDisableDataPolicyButton() {
		dataPolicyResultPane.clickDisableDataPolicyButton(DataPolicyNameHolder.getName());
	}

	/**
	 * Edit newly created Data Policy.
	 *
	 * @param dataPolicyActivity for data policy
	 */
	@When("^I update Data Policy Activity with (.+)")
	public void editDataPolicy(final String dataPolicyActivity) {
		dataPolicyEditor.enterDataPolicyActivity(dataPolicyActivity);
		configurationActionToolbar.saveAll();
		dataPolicyEditor.closeNewDataPolicyEditor();
	}

	/**
	 * Edit newly created Data Policy State.
	 *
	 * @param dataPolicyState for data policy
	 */
	@When("^I update Data Policy State to (.+)")
	public void editDataPolicyState(final String dataPolicyState) {
		dataPolicyEditor.enterDataPolicyState(dataPolicyState);
		configurationActionToolbar.saveAll();
		dataPolicyEditor.closeOldDataPolicyEditor();
	}

	/**
	 * Verify Data Policy State.
	 *
	 * @param dataPolicyState for data policy
	 */
	@Then("^Data Policy State is (.+)")
	public void verifyDataPolicyState(final String dataPolicyState) {
		dataPolicyResultPane.verifyDataPolicyState(DataPolicyNameHolder.getName(), dataPolicyState);
	}

	/**
	 * Verifies the given data policy fields are disabled.
	 *
	 * @param fields List
	 */
	@Then("^the following data policy fields are disabled$")
	public void verifyDataPolicyFieldsDisabled(final List<String> fields) {
		openActiveDataPolicyEditor(DataPolicyNameHolder.getName());
		for (String nameField : fields) {
			dataPolicyEditor.verifyDataPolicyFieldsDisabled(nameField);
		}
	}

	/**
	 * Verify Data Policy End Date set when policy disabled.
	 */
	@Then("^Data Policy End Date is set to current time")
	public void verifyDataPolicyEndDate() {
		dataPolicyResultPane.verifyDataPolicyEndDateIsToday(DataPolicyNameHolder.getName());
	}

	/**
	 * Edit Data Policy End Date for Active policy.
	 */
	@When("^I update Data Policy End Date to future date")
	public void editDataPolicyEndDate() {
		dataPolicyEditor = dataPolicyResultPane.viewDataPolicyEditor(DataPolicyNameHolder.getName());
		dataPolicyEditor.enterDateTime("EndDate", 1);
		configurationActionToolbar.saveAll();
		configurationActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies Data Policy End Date is in future.
	 */
	@Then("^Data Policy End Date is in the future date")
	public void verifyDataPolicyEndDateFutureDate() {
		dataPolicyResultPane.verifyDataPolicyEndDateIsFuture(DataPolicyNameHolder.getName());
	}


	/**
	 * Verifies Create Data Policy button is present.
	 */
	@Then("^Data policies toolbar is visible$")
	public void verifyCreateDataPolicyButtonIsPresent() {
		dataPolicyResultPane.verifyCreateDataPolicyButtonIsPresent();
	}

	/**
	 * Verify configuration links are disabled.
	 *
	 * @param configurationLinks List
	 */
	@Then("^I should not have access to the following Configurations")
	public void verifyConfigLinksDisabled(final List<String> configurationLinks) {
		for (String configuration : configurationLinks) {
			configurationActionToolbar.verifyConfigLinksDisabled(configuration);
		}
	}

	/**
	 * Verify configuration links are enabled.
	 *
	 * @param configurationLinks List
	 */
	@Then("^I should have access to the following Configurations")
	public void verifyConfigLinksEnabled(final List<String> configurationLinks) {
		for (String configuration : configurationLinks) {
			configurationActionToolbar.verifyConfigLinksEnabled(configuration);
		}
	}

	/**
	 * Verifies newly created data policy in my profile.
	 *
	 * @param dataPolicyDetailMap map
	 */
	@Then("^I can see the newly created data policy in my profile with following details$")
	public void verifyDataPolicyDetails(final Map<String, String> dataPolicyDetailMap) {
		HashMap<String, String> dataPolicyHashMap = new HashMap<>(dataPolicyDetailMap);
		dataPolicyHashMap.put("policy-name", DataPolicyNameHolder.getName());
		DataPolicySteps.verifyDataPolicies(dataPolicyHashMap);
	}

	/**
	 * Disable existing Active Data Policy where Consent is given by customer.
	 *
	 * @param dataPolicyState for data policy.
	 * @param dataPolicyName  the Data Policy name.
	 * @param customerID      customerID.
	 */
	@When("^I (.+) existing data policy (.+) where consent is given by customer (.+)$")
	public void disableDataPolicyForCustomer(final String dataPolicyState, final String dataPolicyName, final String customerID) {
		clickDataPolicies();
		dataPolicyResultPane.clickDisableDataPolicyButton(dataPolicyName);
		dataPolicyResultPane.verifyDataPolicyState(dataPolicyName, dataPolicyState);
	}

	/**
	 * Disable newly created Active Data Policy.
	 *
	 * @param dataPolicyState for data policy.
	 */
	@When("^I (.+) newly created Data Policy$")
	public void disableDataPolicyForCustomer(final String dataPolicyState) {
		clickDataPolicies();
		dataPolicyResultPane.clickDisableDataPolicyButton(DataPolicyNameHolder.getName());
		dataPolicyResultPane.verifyDataPolicyState(DataPolicyNameHolder.getName(), dataPolicyState);
	}

	/**
	 * Disable existing Active Data Policy where Consent is given by customer.
	 *
	 * @param dataPolicyState for data policy.
	 */
	@When("^I (.+) newly create data policy$")
	public void changeDataPolicyState(final String dataPolicyState) {
		clickDataPolicies();
		dataPolicyResultPane.clickDisableDataPolicyButton(DataPolicyNameHolder.getName());
		dataPolicyResultPane.verifyDataPolicyState(DataPolicyNameHolder.getName(), dataPolicyState);
	}
}

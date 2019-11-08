package com.elasticpath.cucumber.definitions.importexport;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.cucumber.definitions.ChangeSetDefinition;
import com.elasticpath.importexport.ImportExport;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.util.DBConnector;

/**
 * Import Export steps.
 */
public class ImportExportDefinition {

	final private ImportExport importExport;
	final private Product product;
	final private ChangeSetDefinition changeSetDefinition;
	final private DBConnector dbConnector;

	public ImportExportDefinition(final Product product, final ChangeSetDefinition changeSetDefinition) {
		importExport = new ImportExport();
		this.product = product;
		this.changeSetDefinition = changeSetDefinition;
		dbConnector = new DBConnector();
	}

	/**
	 * Runs export command.
	 */
	@When("^I run the export$")
	public void runExport() {
		dbConnector.updateSearchUrl();
		importExport.modifyExportCliFiles();
		importExport.runExport();
		importExport.createExportedObjectsMap();
	}

	/**
	 * Runs import command.
	 */
	@When("^I run the import$")
	public void runImport() {
		dbConnector.updateChangeSetFalg();
		importExport.modifyImportCliFiles();
		importExport.runImport();
		importExport.createImportedObjectsMap();
	}

	/**
	 * Runs import command.
	 */
	@When("^I run the import in the same data base$")
	public void runImportSameDb() {
		importExport.runImport();
		importExport.createImportedObjectsMap();
	}

	/**
	 * Runs export command for new product.
	 */
	@When("^I run the export for newly created product?$")
	public void runNewCreatedItemExport() {
		dbConnector.updateSearchUrl();
		importExport.modifyExportCliFiles();
		importExport.updateSearchConfigXml(
				"<query for=\"Product\">FIND Product WHERE ProductCode='" + this.product.getProductCode() + "'</query>\n"
		);
		importExport.runExport();
		importExport.createExportedObjectsMap();
	}

	/**
	 * Verifies total number of exported objects.
	 */
	@Then("^the (.+) should be more than (\\d+)$")
	public void verifyTotalExportedObjects(final String resultType, final int result) {
		importExport.verifyResultGreaterThan(resultType, result);
	}

	/**
	 * Verifies exported failures.
	 */
	@Then("^the (.+) following should be (\\d+)$")
	public void verifyTotalExportedObjects(final String resultString, final int result, final List<String> resultTypeList) {
		for (String resultType : resultTypeList) {
			resultType = resultString + " " + resultType;
			importExport.verifyExportResult(resultType, result);
		}
	}

	/**
	 * Verifies import failures.
	 */
	@Then("^the import Total Number Of Failures should be 0$")
	public void verifyImportedFailures() {
		importExport.verifyImportResult("Total Number Of Failures", 0);
	}

	/**
	 * Verifies export failures.
	 */
	@Then("^the export Total Number Of Failures should be 0$")
	public void verifyExportedFailures() {
		importExport.verifyExportResult("Total Number Of Failures", 0);
	}

	/**
	 * Compares number of exported objects with imported objects.
	 */
	@Then("^the number of exported and imported objects should be same$")
	public void compareExportedWithImportedObjects() {
		importExport.compareExportedWithImportedObjects();
	}

	/**
	 * Verifies export objects.
	 */
	@Then("^the number of exported objects should be more than follows$")
	public void verifyExportedObjects(final Map<String, Integer> exportResultMap) {
		importExport.verifyExportedObjects(exportResultMap);
	}

	/**
	 * Runs the import with flag.
	 */
	@And("^I run the import with (.+) flag$")
	public void runImportWithFlag(final String flag) {
		dbConnector.updateChangeSetFalg();
		importExport.modifyImportCliFiles();
		importExport.runImportWithFlag(changeSetDefinition.getChangeSetGuid(), flag);
		importExport.createImportedObjectsMap();

	}

	/**
	 * Runs the import without flag.
	 */
	@And("^I run the import with change set guid$")
	public void runImportChangeSetGuid() {
		dbConnector.updateChangeSetFalg();
		importExport.modifyImportCliFiles();
		importExport.runImport(changeSetDefinition.getChangeSetGuid());
		importExport.createImportedObjectsMap();

	}

	/**
	 * Verifies product exists in db.
	 */
	@And("^the product exists in the data base$")
	public void verifyProductInDb() {
		DBConnector dbConnector = new DBConnector();
		assertThat(dbConnector.productExists(product.getProductCode()))
				.as("Product is not in DB as expected")
				.isTrue();
	}

	/**
	 * Verifies product does not exist in db.
	 */
	@And("^the product does not exist in the data base$")
	public void verifyProductIsNotInDb() {
		DBConnector dbConnector = new DBConnector();
		assertThat(dbConnector.productExists(product.getProductCode()))
				.as("Product should not be in DB as expected")
				.isFalse();
	}

	/**
	 * Copies the unzipped import export cli folder.
	 */
	@Before("@copyImportExportFolder")
	public void copyImportExportFolder() {
		importExport.copyFolder();
	}

	/**
	 * Deletes the copied folder.
	 */
	@After("@deleteCopiedFolder")
	public void deleteFolder() {
		importExport.deleteFolder();
	}

}
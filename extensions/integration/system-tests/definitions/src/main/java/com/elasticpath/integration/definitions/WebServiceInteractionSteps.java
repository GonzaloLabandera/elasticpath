/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.integration.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step definitions for interacting with the Integration Server Import/Export APIs.
 */
public class WebServiceInteractionSteps {
	private final IntegrationServerApiClient client;

	/**
	 * Constructor.
	 */
	public WebServiceInteractionSteps() {
		client = new IntegrationServerApiClient(System.getProperty("ep.integration.baseurl"));
	}

	/**
	 * Request an export of the specified job type.
	 * @param type the job type to export
	 */
	@When("^I export (\\w+) records from the API$")
	public void doExport(final String type) {
		client.doExport(type);
	}

	/**
	 * Request an export of the specified job type with the specified parent job type.
	 * @param type the job type to export
	 * @param parentType the parent job type
	 */
	@When("^I export (\\w+) records with parent (\\w+) from the API$")
	public void doExport(final String type, final String parentType) {
		client.doExport(type, parentType);
	}

	/**
	 * Request an export of the specified job type with the specified EPQL query.
	 * @param type the job type to export
	 * @param query the EPQL query to use to filter the results
	 */
	@When("^I export (\\w+) records with query \"(.+)\" from the API$")
	public void doExportWithQuery(final String type, final String query) {
		client.doExportWithQuery(type, query);
	}

	/**
	 * Request an export of the specified job type with the specified parent job type and EPQL query.
	 * @param type the job type to export
	 * @param parentType the parent job type
	 * @param query the EPQL query to use to filter the results
	 */
	@When("^I export (\\w+) records with parent (\\w+) and query \"(.+)\" from the API$")
	public void doExportWithQuery(final String type, final String parentType, final String query) {
		client.doExportWithQuery(type, parentType, query);
	}

	/**
	 * Ensure that the HTTP status code is as expected.
	 * @param expectedHttpStatus the expected HTTP status code
	 */
	@Then("^response has http status (\\d+)$")
	public void responseShouldHaveHttpStatus(final int expectedHttpStatus) {
		assertThat(client.getHttpStatus())
				.as("Expected http status " + expectedHttpStatus)
				.isEqualTo(expectedHttpStatus);
	}

	/**
	 * Ensure that the XML result from an export contains at least the expected number of XML tags.
	 * @param expectedCount the expected count
	 * @param elementName the XML element tag to count
	 * @throws XMLStreamException if there was an issue interpreting the XML
	 * @throws IOException if there is an issue reading from the input stream
	 */
	@Then("^response has at least (\\d+) (.+) elements$")
	public void responseShouldHaveAtLeastElements(final int expectedCount, final String elementName) throws XMLStreamException, IOException {
		assertThat(client.countXMLElementsInResponse(elementName))
				.as("Expected at least " + expectedCount + " XML elements of type " + elementName)
				.isGreaterThanOrEqualTo(expectedCount);
	}

	/**
	 * Ensure that the XML result from an export contains exactly the expected number of XML tags.
	 * @param expectedCount the expected count
	 * @param elementName the XML element tag to count
	 * @throws XMLStreamException if there was an issue interpreting the XML
	 * @throws IOException if there is an issue reading from the input stream
	 */
	@Then("^response has exactly (\\d+) (.+) elements$")
	public void responseShouldHaveExactlyElements(final int expectedCount, final String elementName) throws XMLStreamException, IOException {
		assertThat(client.countXMLElementsInResponse(elementName))
				.as("Expected at least " + expectedCount + " XML elements of type " + elementName)
				.isEqualTo(expectedCount);
	}

	/**
	 * Request an import using the contents of the passed filename.
	 * @param filename the filename containing the XML to import
	 * @throws IOException if there is an issue loading the XML file
	 */
	@When("^I import ([/.\\w]+) to the API$")
	public void doImport(final String filename) throws IOException {
		client.doImport(filename);
	}

	/**
	 * Request an import using the contents of the passed filename.
	 * @param filename the filename containing the XML to import
	 * @param changeSetGuid the change set GUID
	 * @throws IOException if there is an issue loading the XML file
	 */
	@When("^I import ([/.\\w]+) with change set guid ([-a-zA-Z0-9]{36}) to the API$")
	public void doImport(final String filename, final String changeSetGuid) throws IOException {
		client.doImport(filename, changeSetGuid);
	}

	/**
	 * Retrieve the summary object from the response.
	 */
	@Then("^summary object can be retrieved$")
	public void retrieveSummaryObject() {
		client.retrieveSummaryObjectFromResponse();
	}

	/**
	 * Ensure that the summary contains the expected number of successful entities.
	 * @param entryKey the entry key to check
	 * @param expectedValue the expected count
	 */
	@Then("^summary contains object (\\w+) with count (\\d+)$")
	public void summaryHasObjectWithCount(final String entryKey, final int expectedValue) {
		assertThat(client.countObjectsInSummary(entryKey))
				.as("Expected summary object counter with key " + entryKey + " to have value " + expectedValue)
				.isEqualTo(expectedValue);
	}

	/**
	 * Ensure that the summary contains no failures.
	 */
	@Then("^summary contains no failures$")
	public void summaryHasNoFailures() {
		assertThat(client.countFailuresInSummary())
				.as("Expected summary object to contain no failure messages.")
				.isZero();
	}

	/**
	 * Ensure that the summary contains no warnings.
	 */
	@Then("^summary contains no warnings")
	public void summaryHasNoWarningss() {
		assertThat(client.countWarningsInSummary())
				.as("Expected summary object to contain no warning messages.")
				.isZero();
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.integration.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;


/**
 * Step definitions for interacting with the Catalog Syndication APIs.
 */
public class CatalogSyndicationApiInteractionSteps {
	private static final int MAX = 10;
	private static final int MIN = 0;
	private static final String NAME_TO_REPLACE = "name_to_replace";
	private static final int MILLISECONDS_PER_SECOND = 1000;

	private final CatalogSyndicationApiClient syndicationApiClient;
	private final ImportExportApiClient importExportApiClient;
	private String name;

	/**
	 * Constructor.
	 */
	public CatalogSyndicationApiInteractionSteps() {
		syndicationApiClient = new CatalogSyndicationApiClient(System.getProperty("ep.integration.baseurl"));
		importExportApiClient = new ImportExportApiClient(System.getProperty("ep.integration.baseurl"));

	}

	/**
	 * Replace display name and request an import using the contents of the passed filename.
	 *
	 * @param filename the filename containing the XML to import
	 * @throws IOException if there is an issue loading the XML file
	 */
	@When("^I replace name and import ([/.\\w]+) to the API$")
	public void doReplaceAndImport(final String filename) throws IOException {
		name = generateName();
		importExportApiClient.doImportAndReplace(filename, Collections.singletonMap(NAME_TO_REPLACE, name));
	}

	/**
	 * Wait a specified number of seconds.
	 *
	 * @param delay the number of seconds
	 * @throws InterruptedException if the delay is interrupted
	 */
	@Then("^I wait ([/.\\d]+) seconds$")
	public void getProjection(final int delay) throws InterruptedException {
		Thread.sleep(delay * MILLISECONDS_PER_SECOND);
	}

	/**
	 * Receive Category projection.
	 *
	 * @param path the projection path
	 */
	@Then("^I get category projection from the API with ([/.\\w]+) path$")
	public void getProjection(final String path) {
		syndicationApiClient.getLatestVersionOfCategoryProjection(path);
	}

	/**
	 * Check if projection was updated.
	 *
	 * @throws IOException if there is an issue reading the projection
	 */
	@Then("^Projection was updated$")
	public void validateProjection() throws IOException {
		assertThat(syndicationApiClient.getProjectionAsString())
				.as("Expected updated name " + name + " in projection ")
				.contains(name);
	}

	private String generateName() {
		final Random random = new Random();
		return "replacedName" + random.nextInt((MAX - MIN) + 1) + MIN;
	}
}

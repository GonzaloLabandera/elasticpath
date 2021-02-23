/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.integration.definitions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

/**
 * A client for interacting with the Catalog Syndication APIs.
 */
public class CatalogSyndicationApiClient {
	private static final String API_USERNAME = "admin";
	private static final String API_PASSWORD = "111111";
	private static final int TWO_SECONDS = 2000;

	private final String baseUrl;
	private Response response;

	/**
	 * Constructor.
	 * @param baseUrl the base url of the Catalog Syndication Server instance.
	 */
	public CatalogSyndicationApiClient(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * Get category projection with store and code which specified in imports/category.xml.
	 *
	 * @param path the projection path
	 */
	public void getLatestVersionOfCategoryProjection(final String path) {
		sleep(TWO_SECONDS);
		Client client = getClient();
		response = client
				.target(baseUrl + "/api/syndication/v1/catalog")
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
	}

	/**
	 * Returns a projection as string.
	 *
	 * @return the projection
	 * @throws IOException if there is an issue reading the projection
	 */
	public String getProjectionAsString() throws IOException {
		try (InputStream inputStream = response.readEntity(InputStream.class)) {
			final Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	private Client getClient() {
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(API_USERNAME, API_PASSWORD);
		client.register(feature);
		return client;
	}

	/**
	 * Sleep for a number of milliseconds.
	 *
	 * @param mills number of milliseconds.
	 */
	public void sleep(final long mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}

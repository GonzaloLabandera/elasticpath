/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.integration.definitions;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.elasticpath.importexport.api.models.SummaryDto;
import com.elasticpath.importexport.api.models.SummaryEntryDto;

/**
 * A client for interacting with the Integration Server Import/Export APIs.
 */
public class IntegrationServerApiClient {
	private static final String EXPORT_PATH = "api/importexport/export";
	private static final String IMPORT_PATH = "api/importexport/import";
	private static final String API_USERNAME = "admin";
	private static final String API_PASSWORD = "111111";
	private static final String QUERY_PARAMETER_TYPE = "type";
	private static final String QUERY_PARAMETER_PARENT_TYPE = "parentType";
	private static final String QUERY_PARAMETER_QUERY = "query";
	private static final String QUERY_PARAMETER_CHANGE_SET_GUID = "changeSetGuid";
	private final String baseUrl;
	private Response response;
	private SummaryDto summary;

	/**
	 * Constructor.
	 * @param baseUrl the base url of the Integration Server instance.
	 */
	public IntegrationServerApiClient(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * Request an export of the specified job type.
	 * @param type the job type to export
	 */
	public void doExport(final String type) {
		Client client = getClient();
		response = client
				.target(baseUrl)
				.path(EXPORT_PATH)
				.queryParam(QUERY_PARAMETER_TYPE, type)
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
	}

	/**
	 * Request an export of the specified job type with the specified parent job type.
	 * @param type the job type to export
	 * @param parentType the parent job type
	 */
	public void doExport(final String type, final String parentType) {
		Client client = getClient();
		response = client
				.target(baseUrl)
				.path(EXPORT_PATH)
				.queryParam(QUERY_PARAMETER_TYPE, type)
				.queryParam(QUERY_PARAMETER_PARENT_TYPE, parentType)
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
	}

	/**
	 * Request an export of the specified job type with the specified EPQL query.
	 * @param type the job type to export
	 * @param epqlQuery the EPQL query to use to filter the results
	 */
	public void doExportWithQuery(final String type, final String epqlQuery) {
		Client client = getClient();
		response = client
				.target(baseUrl)
				.path(EXPORT_PATH)
				.queryParam(QUERY_PARAMETER_TYPE, type)
				.queryParam(QUERY_PARAMETER_QUERY, "{epqlQuery}")
				.resolveTemplate("epqlQuery", epqlQuery)
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
	}

	/**
	 * Request an export of the specified job type with the specified parent job type and EPQL query.
	 * @param type the job type to export
	 * @param parentType the parent job type
	 * @param epqlQuery the EPQL query to use to filter the results
	 */
	public void doExportWithQuery(final String type, final String parentType, final String epqlQuery) {
		Client client = getClient();
		response = client
				.target(baseUrl)
				.path(EXPORT_PATH)
				.queryParam(QUERY_PARAMETER_TYPE, type)
				.queryParam(QUERY_PARAMETER_PARENT_TYPE, parentType)
				.queryParam(QUERY_PARAMETER_QUERY, "{epqlQuery}")
				.resolveTemplate("epqlQuery", epqlQuery)
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
	}

	/**
	 * Request an import using the contents of the passed filename.
	 * @param filename the filename containing the XML to import
	 * @throws IOException if there is an issue loading the XML file
	 */
	public void doImport(final String filename) throws IOException {
		Client client = getClient();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream resourceAsStream = classLoader.getResourceAsStream(filename)) {
			response = client
					.target(baseUrl)
					.path(IMPORT_PATH)
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.entity(resourceAsStream, MediaType.APPLICATION_XML));
		}
	}

	/**
	 * Request an import using the contents of the passed filename and the passed change set GUID.
	 * @param filename the filename containing the XML to import
	 * @param changeSetGuid the change set GUID to import into
	 * @throws IOException if there is an issue loading the XML file
	 */
	public void doImport(final String filename, final String changeSetGuid) throws IOException {
		Client client = getClient();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream resourceAsStream = classLoader.getResourceAsStream(filename)) {
			response = client
					.target(baseUrl)
					.path(IMPORT_PATH)
					.queryParam(QUERY_PARAMETER_CHANGE_SET_GUID, changeSetGuid)
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.entity(resourceAsStream, MediaType.APPLICATION_XML));
		}
	}

	/**
	 * Returns the HTTP status code after doing an import or export.
	 * @return the HTTP status code
	 */
	public int getHttpStatus() {
		return response.getStatus();
	}

	/**
	 * Returns a count of the number of elements with the specified name after doing an export.
	 * @param elementName the name of the XML element tag to count
	 * @return the count of specified XML element tags
	 * @throws XMLStreamException if there was an issue interpreting the XML
	 * @throws IOException if there is an issue reading from the input stream
	 */
	public int countXMLElementsInResponse(final String elementName) throws XMLStreamException, IOException {
		try (InputStream inputStream = response.readEntity(InputStream.class)) {
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(inputStream);
			int count = 0;
			while (reader.hasNext()) {
				XMLEvent nextEvent = reader.nextEvent();
				if (nextEvent.isStartElement()) {
					StartElement startElement = nextEvent.asStartElement();
					if (startElement.getName().getLocalPart().equalsIgnoreCase(elementName)) {
						count++;
					}
				}
			}
			return count;
		}
	}

	/**
	 * Retrieves the summary object from the response.
	 */
	public void retrieveSummaryObjectFromResponse() {
		summary = response.readEntity(SummaryDto.class);
	}

	/**
	 * Returns a count of the number of entries that were successfully imported.
	 * @param jobType the job type to count
	 * @return the number of entries that were successfully imported
	 */
	public int countObjectsInSummary(final String jobType) {
		return summary.getObjectCounters().stream()
				.filter(entry -> entry.getJobType().equalsIgnoreCase(jobType))
				.map(SummaryEntryDto::getCounter)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(jobType + " not found in summary response."));
	}

	/**
	 * Returns a count of the number of failures in the summary.
	 * @return the number of failure messages in the summary
	 */
	public int countFailuresInSummary() {
		return summary.getFailures().size();
	}

	/**
	 * Returns a count of the number of warnings in the summary.
	 * @return the number of warning messages in the summary
	 */
	public int countWarningsInSummary() {
		return summary.getWarnings().size();
	}

	private Client getClient() {
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(API_USERNAME, API_PASSWORD);
		client.register(feature);
		return client;
	}
}
/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.search.index.status.servlet;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Helper class used to get the SORL index status.
 */
public final class SolrStatusCheckerHelper {

	private static final Logger LOG = Logger.getLogger(SolrStatusCheckerHelper.class);

	private static final String SOLR_QUERY_STRING = "/product/select?q=*:*";

	private static final String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

	private static final String PROPERTY_FILENAME = "module.properties";
	private static final String PROPERTY_KEY_EP_SEARCH_URL = "ep.search.url";

	private SolrStatusCheckerHelper() {
		// Do nothing.
	}

	/**
	 * Get the status of the solr server indexing process.
	 * @return status of the solr server indexing process.
	 */
	public static Boolean isSolrIndexingStarted() {
		Boolean status = false;
		try {
			String response = getSolrServerResponse();
			NodeList nodes = loadXmlFromString(response).getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element child = (Element) node;
					String attribute = child.getAttribute("numFound");
					if (!attribute.isEmpty() && Integer.parseInt(attribute) > 0) {
						status = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Exception generated while parsing XML response.", e);
		}
		return status;
	}

	private static String getSolrServerResponse() {
		String responseBody = "";
		HttpURLConnection httpURLConnection = null;
		try {
			URL searchURL = new URL(getSearchUrl());
			httpURLConnection = (HttpURLConnection) searchURL.openConnection();
			responseBody = IOUtils.toString(httpURLConnection.getInputStream(), UTF_8);
		} catch (MalformedURLException mue) {
			LOG.error("Invalid solr server url.", mue);
		} catch (IOException ioe) {
			LOG.error("Error getting response from solr server.", ioe);
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}
		return responseBody;
	}

	private static Document loadXmlFromString(final String stringXml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(FEATURE, true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(stringXml));
		return builder.parse(inputSource);
	}

	private static String getSearchUrl() throws IOException {
		Properties moduleProperties = new Properties();
		String resourcePath = Thread.currentThread().getContextClassLoader().getResource("").getPath().concat(PROPERTY_FILENAME);

		try (FileInputStream fileInputStream = new FileInputStream(resourcePath)) {
			moduleProperties.load(fileInputStream);
			String epSearchUrl = moduleProperties.getProperty(PROPERTY_KEY_EP_SEARCH_URL);
			return new StringBuilder().append(epSearchUrl).append(SOLR_QUERY_STRING).toString();
		}
	}
}

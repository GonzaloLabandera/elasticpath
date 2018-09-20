/**
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.ws.fixture.legacy.util

import com.elasticpath.rest.id.util.Base32Util
import com.elasticpath.rest.relos.testdriver.RelosRepresentationUtil
import com.elasticpath.rest.resource.dispatch.operator.annotation.patterns.UriPatterns
import com.jayway.jsonpath.InvalidPathException
import com.jayway.jsonpath.JsonPath

public class TestUtil {

	private static final Integer UPPER_BOUND = 9999999999

	public static String trimLeadingSlash(String uri) {
		if (uri.startsWith("/")) {
			return uri.substring(1)
		}
		return uri
	}

	public static def encode(String originalCode) {
		return Base32Util.encode(originalCode)
	}

	public static def decode(String originalCode) {
		return Base32Util.decode(originalCode)
	}

	/**
	 * Generates a random string.
	 *
	 * @return the generated random string
	 */
	public static def generateRandomString() {
		return new Random().nextInt(UPPER_BOUND)
	}

	/** We need this method to define variables within an itest.  Currently I couldn't find a way for FitNesse to set a variable
	 * from within a scenario. This is a hack so the value of a variable could be set and used from within the test
	 * @param text
	 * @return text
	 */
	public static def echo(String text) {
		return text
	}

	public static def getIdRegularExpression() {
		return UriPatterns.RESOURCE_ID_PATTERN
	}

	public static def getCurrentEpochTime() {
		return System.currentTimeMillis()
	}

	/**
	 * Extracts the id from the uri
	 *
	 * @param uri to extract id from
	 * @return extracted id
	 */
	public static def extractIdFromUri(String uri) {
		String extractedId = uri.split("/")[-1]
		return extractedId
	}

	/**
	 * Extracts the id from the uri given a position starting at 1.
	 * e.g. In the URI /carts/mobee/123456, the id is at position 3.
	 * Note that arrays usually start at position 0, but the leading "/" in the URI above creates a
	 * null in position 0 of the array when we split.
	 *
	 * @param uri to extract id from
	 * @param position from which to extract the id
	 * @return extracted id
	 */
	public static def extractIdFromUriAtPosition(String uri, Integer position) {
		String extractedId = uri.split("/")[position]
		return extractedId
	}

	/**
	 * This method executes a JsonPath query and formats the results. By default, JsonPath results are in the form of a javascript array as there could be many results. They are also escaped by default.
	 * Both the array [] and escaping are removed for readability.
	 *
	 * @param json JSON to search through
	 * @param jsonPath JsonPath query to execute
	 * @return formatted JSON string
	 */
	public static String executeJsonPath(def json, String jsonPath) {
		def results = JsonPath.read(json, jsonPath)
		return results.toString().replace("[", "").replace("]", "").replace("\\", "")
	}

	/**
	 * Takes a string reader and converts into a string
	 *
	 * @param stringReader stringReader
	 * @return String
	 */
	public static String readString(StringReader stringReader) {
		StringBuffer data = new StringBuffer(1000);
		char[] buf = new char[1024];
		int numRead;
		while ((numRead = stringReader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			data.append(readData);
			buf = new char[1024];
		}
		stringReader.close();
		return data.toString();
	}

	/**
	 * Takes two Lists for unordered comparison.
	 *
	 * @param a the first list
	 * @param b the second list
	 * @return "same", or an output of both lists, if different
	 */
	public static def compareTo(List a, List b) {
		def listsAreSame = a.containsAll(b) && b.containsAll(a)

		if (listsAreSame) {
			return "matches"
		}
		return "List a: " + a.toListString() + "\nList b: " + b.toListString()
	}

	/**
	 * Fitnesse appends pre and br tags when tables use preformatting markup. This method strips out those HTML tags so the data can be processed by the fixture.
	 *
	 * @param string string with HTML characters
	 * @return string with no HTML characters
	 */
	public static String stripHtmlTags(String string) {
		return string.replaceAll("\n", '').replaceAll("<.*?>", '')
	}

	/**
	 * Gets the field value.
	 *
	 * @param representation the json object
	 * @param fieldName the field name
	 * @return the field value
	 */
	public static def getFieldValue(def representation, String fieldName) {
		try {
			return RelosRepresentationUtil.getFieldWithName(representation, fieldName)
		} catch (InvalidPathException ipe) {
			return null
		}
	}
}
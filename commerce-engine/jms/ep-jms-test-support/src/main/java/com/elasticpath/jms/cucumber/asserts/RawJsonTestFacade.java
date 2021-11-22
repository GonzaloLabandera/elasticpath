/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.jms.cucumber.asserts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Class for testing raw json values.
 */
@SuppressWarnings({"PMD.UseUtilityClass"})

public final class RawJsonTestFacade {

	private static final Logger LOGGER = LogManager.getLogger(RawJsonTestFacade.class);
	private static final String UNABLE_TO_FIND_VALUE_MESSAGE = "Unable to find value: ";
	private static final String FOR_PATH_MESSAGE = " for path: ";

	/**
	 * Private constructor.
	 */
	private RawJsonTestFacade() {

	}

	/**
	 * Verifies that jsonObjectList contains a jsonObject which contains the given key and value.
	 *
	 * @param jsonObjectList the json object list
	 * @param parent         parent
	 * @param key            key
	 * @param value          value
	 */
	public static void verifyJsonValues(final List<JSONObject> jsonObjectList, final String parent, final String key, final String value) {

		boolean objectExists = false;
		boolean valueExists = false;
		String parentWithKey = "";

		for (JSONObject jsonObject : jsonObjectList) {

			if (parent.length() > 0) {
				parentWithKey = "$." + parent + "." + key;
			} else {
				parentWithKey = "$." + key;
			}

			LOGGER.debug("verifying " + parentWithKey + " for value: " + value);

			if (parent.length() > 0 && jsonObject.containsKey(parent) || jsonObject.containsKey(key)) {
				objectExists = true;
				if (parentKeyExists(jsonObject, parentWithKey)) {
					if (JsonPath.parse(jsonObject.toJSONString()).read(parentWithKey) instanceof String) {
						if (JsonPath.parse(jsonObject.toJSONString()).read(parentWithKey).toString().equals(value)) {
							valueExists = true;
						}

					} else {
						List<String> strList = JsonPath.parse(jsonObject.toJSONString()).read(parentWithKey);
						assertThat(strList)
								.as("JSONObject key '" + parentWithKey + "' doesn't exist")
								.isNotEmpty();

						if (!strList.isEmpty() && strList.get(0).equals(value)) {
							valueExists = true;
						}
					}
				}
				if (valueExists) {
					break;
				}
			}
		}

		if (parent.length() > 0) {
			assertThat(objectExists)
					.as("JSONObject key '" + parent + "' doesn't exist")
					.isTrue();
		} else {
			assertThat(objectExists)
					.as("JSONObject key '" + key + "' doesn't exist")
					.isTrue();
		}

		assertThat(valueExists)
				.as("JSONObject value '" + value + "' doesn't exist for " + parentWithKey)
				.isTrue();

	}

	private static boolean parentKeyExists(final JSONObject jsonObject, final String parentWithKey) {
		try {
			JsonPath.parse(jsonObject.toJSONString()).read(parentWithKey);
			return true;
		} catch (PathNotFoundException pathNotFoundException) {
			return false;
		}
	}

	/**
	 * Returns list of JsonObject based on path and value
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_CREATED"},"guid":"20001","data":{...
	 * to select message with eventType name = ORDER_CREATED use:
	 * path  eventType.name
	 * value ORDER_CREATED
	 * This method asserts that the list is not empty before returning.
	 * @param jsonObjectList list of messages
	 * @param path           path to key
	 * @param value          key value
	 * @return the JSONObject list
	 */
	public static List<JSONObject> getJsonObjectsByPathAndValueAndAssert(final List<JSONObject> jsonObjectList, final String path,
			final String value) {
		List<JSONObject> jsonObjects = getJsonObjectsByPathAndValue(jsonObjectList, path, value);

		assertThat(jsonObjects)
				.as(UNABLE_TO_FIND_VALUE_MESSAGE + value + FOR_PATH_MESSAGE + path)
				.isNotEmpty();

		return jsonObjects;
	}

	/**
	 * Returns list of JsonObject based on path and value
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_SHIPMENT_CREATED"},"guid":"80008-1","data":{...
	 * to select message with eventType name = ORDER_SHIPMENT_CREATED use:
	 * path  eventType.name
	 * value ORDER_SHIPMENT_CREATED
	 * This method does not assert that the object was found - it will return an empty list if the object is not found
	 * @param jsonObjectList list of messages
	 * @param path           path to key
	 * @param value          key value
	 * @return the JSONObject list - empty if the path and value are not found
	 */
	public static List<JSONObject> getJsonObjectsByPathAndValue(final List<JSONObject> jsonObjectList, final String path, final String value) {
		String jsonPath = "$." + path;
		List<JSONObject> jsonObjects = new ArrayList<>();
		for (JSONObject jsonObject : jsonObjectList) {
			if (JsonPath.parse(jsonObject.toJSONString()).read(jsonPath) instanceof String
					&& JsonPath.parse(jsonObject.toJSONString()).read(jsonPath).toString().equals(value)) {
				jsonObjects.add(jsonObject);
			}
		}
		return jsonObjects;
	}

	/**
	 * Returns JsonObject based on path and value
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_CREATED"},"guid":"20001","data":{...
	 * to select message with guid = 20001 use:
	 * path  guid
	 * value 20001
	 *
	 * @param jsonObjectList list of messages
	 * @param path           path to key
	 * @param value          key value
	 * @return the JSONObject - the object will be empty if the path and/or value are not found.
	 */
	public static JSONObject getJsonObjectByPathAndValue(final List<JSONObject> jsonObjectList, final String path, final String value) {
		String jsonPath = "$." + path;
		JSONObject selectedJsonObject = new JSONObject();
		try {
			for (JSONObject jsonObject : jsonObjectList) {
				if (JsonPath.parse(jsonObject.toJSONString()).read(jsonPath) instanceof String
						&& JsonPath.parse(jsonObject.toJSONString()).read(jsonPath).toString().equals(value)) {
					selectedJsonObject = jsonObject;
					break;
				}
			}
		} catch (PathNotFoundException pnfe) {
			LOGGER.error(UNABLE_TO_FIND_VALUE_MESSAGE + value + FOR_PATH_MESSAGE + path);
		}

		return selectedJsonObject;
	}

	/**
	 * Returns true if the value exists at the specified path in the object
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_SHIPMENT_CREATED"},"guid":"80008-1","data":{...
	 * to verify guid = 80008-1 use:
	 * path  guid
	 * value 80008-1
	 *
	 * @param jsonObject     the JsonObject to parse
	 * @param path           path to key
	 * @param value          key value
	 * @return true if the value at the specified path matches, false otherwise
	 */
	public static boolean valueMatches(final JSONObject jsonObject, final String path, final String value) {
		String jsonPath = "$." + path;
		try {
			if (JsonPath.parse(jsonObject.toJSONString()).read(jsonPath) instanceof String
					&& JsonPath.parse(jsonObject.toJSONString()).read(jsonPath).toString().equals(value)) {
				return true;
			}
		} catch (PathNotFoundException pnfe) {
			LOGGER.error(UNABLE_TO_FIND_VALUE_MESSAGE + value + FOR_PATH_MESSAGE + path, pnfe);
		}

		return false;
	}

	/**
	 * Returns JsonObject based on path and value
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_SHIPMENT_CREATED"},"guid":"80008-1","data":{...
	 * to select message with guid = 80008-1 use:
	 * path  guid
	 * value 80008-1
	 * Will assert that the returned object is not empty before returning.
	 *
	 * @param jsonObjectList list of messages
	 * @param path           path to key
	 * @param value          key value
	 * @return the JSONObject
	 */
	public static JSONObject getJsonObjectByPathAndValueAndAssert(final List<JSONObject> jsonObjectList, final String path, final String value) {
		JSONObject selectedJsonObject = RawJsonTestFacade.getJsonObjectByPathAndValue(jsonObjectList, path, value);

		assertThat(selectedJsonObject.isEmpty())
				.as(UNABLE_TO_FIND_VALUE_MESSAGE + value + FOR_PATH_MESSAGE + path)
				.isFalse();

		return selectedJsonObject;
	}

	/**
	 * Returns map based on path.
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_CREATED"},"guid":"20001","data":{...
	 * to get map of eventType use:
	 * path  eventType
	 *
	 * @param jsonObject the messages
	 * @param path       path to key
	 * @return the map
	 */
	public static Map<String, Object> getMapByPath(final JSONObject jsonObject, final String path) {
		String jsonPath = "$." + path;
		Map<String, Object> map = JsonPath.parse(jsonObject.toJSONString()).read(path);

		assertThat(map)
				.as("Unable to find JSONObject for path: " + jsonPath)
				.isNotEmpty();

		return map;
	}

	/**
	 * Returns list of map based on path.
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_CREATED"},"guid":"20001","data":{...
	 * ..."addresses":[{"addressUsage":"BILLING","street1":"123 Black Sea Rd.","street2":"100","city":"Blaine","region":"WA","country":"US",
	 * "zipPostalCode":"98230"},{"addressUsage":"SHIPPING","firstName":"User","lastName":"Test"...
	 * to get addresses list from a message use:
	 * path  addresses
	 *
	 * @param jsonObject the messages
	 * @param path       path to key
	 * @return the list of maps
	 */
	public static List<Map<String, Object>> getMapListByPath(final JSONObject jsonObject, final String path) {
		String jsonPath = "$." + path;
		List<Map<String, Object>> mapList = JsonPath.parse(jsonObject.toJSONString()).read(path);

		assertThat(mapList)
				.as("Unable to find JSONObject for path: " + jsonPath)
				.isNotEmpty();

		return mapList;
	}

	/**
	 * Returns map based on path.
	 * example message: {"eventType":{"@class":"OrderEventType","name":"ORDER_CREATED"},"guid":"20001","data":{...
	 * ..."addresses":[{"addressUsage":"BILLING","street1":"123 Black Sea Rd.","street2":"100","city":"Blaine","region":"WA","country":"US",
	 * "zipPostalCode":"98230"},{"addressUsage":"SHIPPING","firstName":"User","lastName":"Test"...
	 * to get address addressUsage = SHIPPING  from address map list use:
	 * mapList list of address maps
	 * key  addressUsage
	 * value SHIPPING
	 *
	 * @param mapList the messages
	 * @param key     path to key
	 * @param value   key value
	 * @return the map
	 */
	public static Map<String, Object> getMapByPath(final List<Map<String, Object>> mapList, final String key, final String value) {
		Map<String, Object> hashMap = null;
		for (Map<String, Object> map : mapList) {
			if (map.get(key).equals(value)) {
				hashMap = map;
				break;
			}
		}

		assertThat(hashMap)
				.as("Unable to find key: " + key + " with value: " + value)
				.isNotNull();

		return hashMap;
	}

}

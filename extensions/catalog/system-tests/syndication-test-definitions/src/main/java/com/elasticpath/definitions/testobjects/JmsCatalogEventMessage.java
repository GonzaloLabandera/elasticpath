package com.elasticpath.definitions.testobjects;

import java.util.List;

/**
 * Jms Catalog event message test object.
 */
public class JmsCatalogEventMessage {

	private static final String OPTION_EVENT_MESSAGE_NAME = "OPTIONS_UPDATED";
	private static final String BRAND_EVENT_MESSAGE_NAME = "BRANDS_UPDATED";
	private static final String OPTION_EVENT_MESSAGE_TYPE = "option";
	private static final String BRAND_EVENT_MESSAGE_TYPE = "brand";
	private static final String POST_BODY_EVENT_MESSAGE = "{\"eventType\":{\"@class\":\"CatalogEventType\",\"name\":\"%s\"},"
			+ "\"guid\":\"AGGREGATE\",\"data\":{\"codes\":[%s],\"modifiedDateTime\":\"2019-02-06T14:14:58-08:00\",\"store\":\"%s\","
			+ "\"type\":\"%s\"}}";

	/**
	 * Returns Jms formatted Catalog Event message.
	 *
	 * @param name Catalog event type name
	 * @param codes codes of catalog entities
	 * @param store store code
	 * @param type Catalog entity type
	 * @return Jms formatted Catalog Event message
	 */
	public String getPostBody(final String name, final List<String> codes, final String store, final String type) {
		return String.format(POST_BODY_EVENT_MESSAGE, name, getEventMessageCodes(codes), store, type);
	}

	public String getOptionMessageName() {
		return OPTION_EVENT_MESSAGE_NAME;
	}

	public String getBrandMessageName() {
		return BRAND_EVENT_MESSAGE_NAME;
	}

	public String getOptionEventType() {
		return OPTION_EVENT_MESSAGE_TYPE;
	}

	public String getBrandEventType() {
		return BRAND_EVENT_MESSAGE_TYPE;
	}

	/**
	 * Creates formatted string which contains sku option codes and which fits Catalog Event Message format.
	 *
	 * @param codes sku option codes list which should be converted to String
	 * @return Returns formatted string which contains sku option codes and which fits Catalog Event Message format
	 */
	private String getEventMessageCodes(final List<String> codes) {
		String formatted = "";
		for (String code : codes) {
			formatted = formatted + getWrappedCode(code) + ",";
		}
		return formatted.substring(0, formatted.length() - 1);
	}

	/**
	 * Returns code value surrounded by quotes.
	 *
	 * @param code codes which should be formatted
	 * @return Returns code value surrounded by quotes
	 */
	private String getWrappedCode(final String code) {
		return "\"" + code + "\"";
	}
}

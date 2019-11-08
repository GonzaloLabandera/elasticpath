package com.elasticpath.definitions.stateobjects;

import java.util.ArrayList;
import java.util.List;

import io.restassured.response.Response;

/**
 * Object to pass the state of API calls.
 */
public class Context {

	private final List<Response> responses = new ArrayList<>();
	private Response response;
	private String responseStartAfterCode;
	private String startAfter = "";

	public String getStartAfter() {
		return startAfter;
	}

	public void setStartAfter(final String startAfter) {
		this.startAfter = startAfter;
	}

	/**
	 * Returns startAfter code which is a part of Syndication API response pagination block.
	 *
	 * @return startAfter code which is a part of Syndication API response pagination block
	 */
	public String getResponseStartAfterCode() {
		return responseStartAfterCode;
	}

	/**
	 * Sets startAfter code.
	 *
	 * @param responseStartAfterCode startAfter code which is a part of Syndication API response pagination block
	 */
	public void setResponseStartAfterCode(final String responseStartAfterCode) {
		this.responseStartAfterCode = responseStartAfterCode;
	}

	public Response getResponse() {
		return response;
	}

	/**
	 * Adds response to the collection of created responses.
	 *
	 * @param response response which should be added
	 */
	public void setResponse(final Response response) {
		this.response = response;
		responses.add(response);
	}

	/**
	 * Returns response which was added firstly.
	 *
	 * @return response which was added firstly
	 */
	public Response getFirstResponse() {
		return this.responses.get(0);
	}

	/**
	 * Returns previously created response.
	 *
	 * @return previously created response
	 */
	public Response getPreviousResponse() {
		int previousResponseOffset = 2;
		if (responses.size() < previousResponseOffset) {
			return getFirstResponse();
		}
		return this.responses.get(responses.size() - previousResponseOffset);
	}
}

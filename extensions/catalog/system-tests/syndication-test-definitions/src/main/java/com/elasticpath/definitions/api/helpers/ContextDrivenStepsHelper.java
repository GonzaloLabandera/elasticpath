package com.elasticpath.definitions.api.helpers;

import static io.restassured.RestAssured.given;

import java.util.Map;
import java.util.Optional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.definitions.stateobjects.Context;
import com.elasticpath.definitions.utils.DataHelper;

/**
 * Helper class which has context related methods which are used by API step definition classes.
 */
public class ContextDrivenStepsHelper {

	private static final Logger LOGGER = LogManager.getLogger(ContextDrivenStepsHelper.class);
	private final Context context;
	private Response response;

	/**
	 * Constructor.
	 *
	 * @param context context state object.
	 */
	public ContextDrivenStepsHelper(final Context context) {
		this.context = context;
		this.response = this.context.getResponse();
	}

	/**
	 * Calls API to retrieve latest version of single projection populated in DB with If-None-Match header.
	 *
	 * @param url    url which is used in GET call
	 * @param store  store code to pass it in URL
	 * @param code   sku option code to pass it in URL
	 * @param status expected response status code
	 * @return context state object which reflects the latest state
	 * @throws InterruptedException waiting for response was interrupted
	 */
	public Context getLatestProjectionETag(
			final String url, final String store, final String code, final int status) throws InterruptedException {
		final int maxAttempts = 15;
		final int attemptsInterval = 100;
		int attempt = 1;
		while (attempt <= maxAttempts) {
			Thread.sleep(attemptsInterval);
			LOGGER.warn("Attempting to get option projection: " + attempt);
			response = given()
					.header("If-None-Match", context.getResponse().getHeader(Constants.ETAG))
					.when()
					.get(String.format(url, store, code));
			if (!response.getBody().equals(context.getResponse().getBody()) && response.getStatusCode() == status) {
				break;
			}
			attempt++;
		}
		context.setResponse(response);
		return context;
	}

	/**
	 * Sends POST request to retrieve projections.
	 *
	 * @param urlStore store code which should be used in POST request Url
	 * @param body     request body which should be sent
	 * @param postUrl  POST request Url
	 * @return context state object which reflects the latest state
	 */
	public Context getProjectionsPost(final String body, final String urlStore, final String postUrl) {
		response = given()
				.body(body)
				.when()
				.contentType(ContentType.JSON)
				.post(String.format(postUrl, urlStore));
		context.setResponse(response);
		return context;
	}

	/**
	 * Calls API to retrieve all projections of a specific type populated in DB.
	 *
	 * @param parameters URL query parameters
	 * @param startAfter sku brand code which response page should start after, "" if the first page is needed
	 * @param url        URL which should be used in API call
	 * @return context state object which reflects the latest state
	 */
	public Context getProjectionsPopulatedInDb(final Map<String, String> parameters, final String startAfter, final String url) {
		String limit = Optional.ofNullable(parameters.get("limit"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String modifiedSince = Optional.ofNullable(parameters.get("modifiedSince"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String modifiedSinceOffset = Optional.ofNullable(parameters.get("modifiedSinceOffset"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		String comleteUrl = url;
		if (!"".equals(limit)) {
			comleteUrl = comleteUrl + "?limit=" + limit;
		}
		if (!"".equals(startAfter)) {
			comleteUrl = comleteUrl + "&startAfter=" + startAfter;
			context.setStartAfter(startAfter);
		}
		if (!"".equals(modifiedSince)) {
			String formattedDate = modifiedSince;
			if (!"someRandomString".equals(modifiedSince)) {
				formattedDate = DataHelper.URL_DATE_FORMAT.format(DataHelper.getDateWithOffset(modifiedSince));
			}
			if ("".equals(limit)) {
				comleteUrl = comleteUrl + "?modifiedSince=" + formattedDate;
			} else {
				comleteUrl = comleteUrl + "&modifiedSince=" + formattedDate;
			}
		}
		if (!"".equals(modifiedSinceOffset)) {
			comleteUrl = comleteUrl + "&modifiedSinceOffset=" + modifiedSinceOffset;
		}
		StepsHelper.sleep(Constants.API_SLEEP_TIME);
		response = given().when().get(comleteUrl);
		context.setResponse(response);
		return context;
	}
}

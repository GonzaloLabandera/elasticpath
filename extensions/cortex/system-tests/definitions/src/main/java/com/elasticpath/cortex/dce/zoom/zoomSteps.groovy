package com.elasticpath.cortex.dce.zoom

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.assertj.core.data.Percentage

import com.elasticpath.cortex.dce.DatabaseAnalyzerClient

class ZoomSteps {

	static ZOOM_ITEM
	static SAVED_RESPONSE

	@When('^I start measuring db queries$')
	static void databaseAnalyzerStart() {
		DatabaseAnalyzerClient.instance.start()
	}

	@When('^I stop measuring db queries$')
	static void databaseAnalyzerStop() {
		DatabaseAnalyzerClient.instance.stop()
	}

	@Then('^total number of DB calls should be close to (\\d+) calls with allowed deviation of (\\d+.\\d+) percent$')
	static void verifyNumberOfDBCallsWithDeviation(int dbcalls, double percentage) {
		int totalDbCalls = DatabaseAnalyzerClient.instance.getOverallDBCalls()

		assertThat(totalDbCalls)
				.isCloseTo(dbcalls, Percentage.withPercentage(percentage))
	}

	@When('^I zoom to cart with a query (.+)$')
	static void zoomToCart(def zoom) {
		client.GET("/carts/mobee/default?zoom=$zoom")
	}

	@And('^number of DB calls for table (.+) should be close to (\\d+) calls with allowed deviation of (\\d+.\\d+) percent$')
	static void verifyNumberOfCallsToTableWithDeviation(String table, int dbcalls, double percentage) {
		int totalDbCalls = DatabaseAnalyzerClient.instance.getDBCallsByTableName(table)

		assertThat(totalDbCalls)
				.isCloseTo(dbcalls, Percentage.withPercentage(percentage))
	}

	@Then('^I inspect the zoom object (.+)$')
	static void inspectZoomObject(def jsonpath) {
		try {
			ZOOM_ITEM = JsonPath.read(client.body, "\$." + jsonpath)
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$jsonpath not found in $client.body")
					.isFalse()
		}
	}

	@Then('^the response does not contain path (.+)$')
	static void verifyResponseNotContainPath(def path) {
		try {
			JsonPath.read(client.body, "\$." + path)
			assertThat(true)
					.as("$client.body should not contain path $path")
					.isfalse()
		}
		catch (PathNotFoundException e) {
			// pass
		}
	}

	@Then('^the response contains path (.+)$')
	static void verifyRespopnseContainsPath(def path) {
		try {
			JsonPath.read(client.body, "\$." + path)
			//        pass
			assertThat(true).isTrue()
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$path not found in $client.body")
					.isFalse()
		}
	}

	@Then('^the zoom object does not contain path (.+)$')
	static void verifyZoomObjectNotContains(def path) {
		try {
			JsonPath.read(ZOOM_ITEM, "\$." + path)
			assertThat(true)
					.as("$ZOOM_ITEM should not contain path $path")
					.isFalse()
		}
		catch (PathNotFoundException e) {
			// pass
		}
	}

	@Then('^the zoom object contains path (.+)$')
	static void verifyZoomObjectContainsPath(def path) {
		try {
			JsonPath.read(ZOOM_ITEM, "\$." + path)
			//        pass
			assertThat(true).isTrue()
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$path not found in $ZOOM_ITEM")
					.isFalse()
		}
	}

	@Then('^the zoom object does not contain string (.+)$')
	static void verifyZoomObjectNotContainsString(def regex) {
		assertThat(ZOOM_ITEM.toString())
				.as("Zoom object is not as expected")
				.doesNotContain(regex)
	}

	@Then('^the zoom object contains string (.+)$')
	static void verifyZoomObjectContainsString(def regex) {
		assertThat(ZOOM_ITEM.toString())
				.as("Zoom object is not as expected")
				.contains(regex)
	}

	@Then('^save the zoom response$')
	static void saveZoomResponse() {
		SAVED_RESPONSE = client.body
	}

	@Then('^save the zoomed response$')
	static void saveZoomResponseVariableValue() {
		SAVED_RESPONSE = ZOOM_ITEM
	}

	@Then('^In the given format (.*) the response is identical to the saved response$')
	static void verifyResponseFormatIsIdentical(String format) {
		def body = client.body

		if (format.contains("zoom.noself")) {
			body.remove('self')
			SAVED_RESPONSE.remove('self')
		}

		assertThat(body)
				.as("The response is not as expected")
				.isEqualTo(SAVED_RESPONSE)
	}

	@Then('^the response is identical to the saved response$')
	static void verifyResponseIdenticalToSavedResponse() {
		assertThat(client.body)
				.as("The response is not as expected")
				.isEqualTo(SAVED_RESPONSE)
	}

	@Then('^the json path (.+) equals (.+)$')
	static void verifyJSONPathEqualsTo(def jsonpath, def expected) {
		try {
			assertThat(String.valueOf(JsonPath.read(client.body, "\$." + jsonpath)))
					.as("The result is not as expected")
					.isEqualTo(expected)
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$jsonpath not found in $client.body")
					.isFalse()
		}
	}

	@Then('^the zoomed object contains path (.+) equal to (.+)$')
	static void verifyZoomObjectContainsPathIdenticalTo(def jsonpath, def expected) {
		try {
			assertThat(JsonPath.read(ZOOM_ITEM, "\$." + jsonpath))
					.as("The zoomed object is not as expected")
					.isEqualTo(expected)
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$jsonpath not found in $client.body")
					.isFalse()
		}
	}

	@Then('^I go to the zoom object (.+) with field (.+) and value (.+)$')
	static void verifyZoomObjectFieldValue(String path, String field, String value) {
		def zoomedResult
		try {
			def zoomObject = JsonPath.read(client.body, "\$." + path)
			for (def zoomRepresentation : zoomObject) {
				if (zoomObject[field] == value) {
					zoomedResult = zoomRepresentation
				}
			}
			ZOOM_ITEM = zoomedResult
		}
		catch (PathNotFoundException e) {
			assertThat(true)
					.as("$path not found in $client.body")
					.isFalse()
		}
	}

	@Then('^I zoom the current url (.+)$')
	static void addZoomToCurrentURL(def zoom) {
		client.GET(client.body.self.uri + "?zoom=$zoom")
		client.stopIfFailure()
	}

	@When('^I zoom the (.+) with zoom (.+)$')
	static void openElementByFieldZoom(String elementName, String zoom) {
		zoomResources(elementName, zoom)
	}

	static void zoomResources(def resources, def zoom) {
		List<String> path = new ArrayList<String>()
		path.addAll(resources.split("->"))

		for (String rel : path) {
			def links = client.body.links.findAll { link ->
				link.rel == rel.trim()
			}
			assertThat(links)
					.size()
					.as("rel $rel not found")
					.isGreaterThan(0)

			String link = links.get(0).href
			if (link.contains('?')) {
				link = link + "&zoom=" + zoom
			} else {
				link = link + "?zoom=" + zoom
			}
			client.GET(link)
					.stopIfFailure()
		}
	}

	@When('^I zoom the facets elements with language (.+)$')
	static void zoomFacet(String locale) {
		client.headers.put("accept-language", locale)
		zoomResources("facets", "element")
	}
}

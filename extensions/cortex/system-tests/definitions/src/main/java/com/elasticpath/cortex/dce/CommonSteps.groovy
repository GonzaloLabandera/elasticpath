package com.elasticpath.cortex.dce

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.CommonAssertion.assertMap
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import com.jayway.jsonpath.JsonPath
import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.json.JSONArray

import com.elasticpath.cortexTestObjects.Facets

class CommonSteps {

	static String savedUri
	static String savedRel
	static final USER_TRAITS_HEADER = "x-ep-user-traits"

	static RESPONSE_MAP = ['OK'                 : 200, 'OK, created': 201, 'no content': 204, 'bad request': 400, 'unauthorized': 401,
						'forbidden'             : 403, 'not found': 404, 'method not allowed': 405, 'conflict': 409,
						'unsupported media type': 415, 'server failure': 500]

	
	@Then('^(?:the operation is identified as|the HTTP status is) (.+)$')
	static void verifyOperationUndefinedWithResponse(String response) {
		assertThat(client.response.status)
				.as("The response status is not as expected")
				.isEqualTo(RESPONSE_MAP[response])
	}

	@Then('^(?:.+) fails with status (.+)$')
	static void verifyActionFailsWithResponse(String response) {
		assertThat(client.response.status)
				.as("The response status is not as expected")
				.isEqualTo(RESPONSE_MAP[response])
	}

	@Then('^the HTTP status code is (.+)$')
	static void verifyStatusCode(int code) {
		assertThat(client.response.status)
				.as("The response status is not as expected")
				.isEqualTo(code)
	}

	@Then('^the response message is (.+)$')
	static void verifyResponseMessage(String expectedDebugMessage) {
		def actualDebugMessage = client.response.responseData.messages[0]."debug-message"
		assertThat(actualDebugMessage)
				.as("HTTP response message is not as expected")
				.isEqualTo(expectedDebugMessage)
	}

	@Then('^I follow location$')
	static void follow() {
		client.follow()
	}

	@Then('^I specify the (.*) and follow (?:the link|links) (.+)$')
	static void followLinks(String format, String resources) {
		/* this step will follow a list of links separated by '->'
		ie. When I follow links cart -> order -> paymentmethodinfo
		 */
		navigateResources(resources, format)
	}

	@Then('^I follow (?:the link|links) (.+)$')
	static void navigateToResource(String resources) {
		/* this step will follow a list of links separated by '->'
		ie. When I follow links cart -> order -> paymentmethodinfo
		 */
		navigateResources(resources)
	}

	@Then('^I follow the (\\d+) link with rel (.+)$')
	static void navigateToResource(int id, String resources) {
		navigateResources(resources, id)
	}

	@Then('^I navigate links (.+)$')
	static void navigateToResourceFromRoot(String resources) {
		/* this step will follow a list of links separated by '->'
		ie. When I follow links cart -> order -> paymentmethodinfo
		 */
		client.GET("/")
		navigateResources(resources)
	}

	@When('^I navigate to root$')
	static void getRoot() {
		client.GET("/")
				.stopIfFailure()
	}

	static void navigateResources(def resources) {
		navigateResources(resources, "")
	}

	static void navigateResources(def resources, def format) {
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
				link = link + "&format=" + format
			} else {
				link = link + "?format=" + format
			}
			client.GET(link)
					.stopIfFailure()
		}
	}

	@Then('^open the (element|child) with field (.+) of (.+)$')
	static void clickElementWithFieldValue(def linkrel, def field, def value) {
		CommonMethods.openLinkRelWithFieldWithValue(linkrel, field, value)
	}

	@Then('^open the (element|child) with field (.+) containing (.+)$')
	static void findElementByFieldValue(def linkrel, def field, def value) {
		def elementExists = false
		def elementResponse = ""
		client.body.links.find {
			if (it.rel == linkrel) {
				client.GET(it.href)
				if (client[field].toString().contains(value)) {
					elementResponse = client.save()
					elementExists = true
				}
			}
		}
		assertThat(elementExists)
				.as("$linkrel with $field = $value not found")
				.isTrue()
		client.resume(elementResponse)
	}

	@Then('^there is an (element|child) with field (.+) of (.+)$')
	static void verifyElementWithFieldValueExists(def linkrel, def field, def value) {
		def elementExists = false
		def uri = client.body.self.uri
		client.body.links.find {
			if (it.rel == linkrel) {
				client.GET(it.href)
				if (client[field] == value) {
					elementExists = true
				}
			}
		}

		assertThat(elementExists)
				.as("element with $field = $value not found")
				.isTrue()

		client.GET(uri)
	}

	@Then('^there is an element with field (.+) containing (.+)$')
	static void findElementByFieldValue(def field, def value) {
		def elementExists = false
		def uri = client.body.self.uri
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				if (client[field].toString().contains(value)) {
					elementExists = true
				}
			}
		}

		assertThat(elementExists)
				.as("element with $field = $value not found")
				.isTrue()

		client.GET(uri)
	}

	@Then('^there is no element with field (.+) of (.+)$')
	static void verifyElementWithFieldValueExists(def field, def value) {
		def elementExists = false
		def elementResponse
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				if (client[field] == value) {
					elementResponse = client.save()
					elementExists = true
				}
			}
		}

		assertThat(elementExists)
				.as("element with $field = $value found")
				.isFalse()
	}

	@Then('^the field (.+) matches (.+)$')
	static void verifyFieldMatches(String field, String regex) {
		assertThat(client[field].toString())
				.as("The field $field is not as expected")
				.matches(regex)
	}

	@Then('^the field (.+) contains value (.+)$')
	static void verifyFieldValue(String field, String regex) {
		assertThat(client[field].toString())
				.as("The field $field is not as expected")
				.contains(regex)
	}

	@Then('^the json path (.+) contains value (.+)$')
	static void verifyJsonPath(String field, String regex) {
		assertThat(JsonPath.read(client.getBody(), field))
				.as("The field $field is not as expected")
				.contains(regex)
	}


	@Then('^the field (.+) does not exist$')
	static void verifyFieldNotExists(def field) {
		assertThat(client[field])
				.as("The field $field is not as expected")
				.isEqualTo(null)
	}

	@Then('^there are no (.+) links$')
	static void verifyLinkNotExists(def rel) {
		assertLinkDoesNotExist(client, rel)
	}

	@Then('^the category (.+) that has no sub-category is at the expected category level and has items$')
	static void verifyCategoryIsAtExpectedLevel(def displayName) {
		def navigationDisplayName = client["display-name"]
		assertThat(navigationDisplayName)
				.isEqualTo(displayName)

		CommonMethods.verifyNavigationLinkDoesNotExist("parent")
		CommonMethods.verifyNavigationLinkDoesNotExist("child")
		assertLinkExists(client, "top")
		assertLinkExists(client, "items")
	}

	@Then('^the category (.+) is at the sub-category level and has items$')
	static void verifyCategoryIsSubLevel(def displayName) {
		def navigationDisplayName = client["display-name"]
		assertThat(navigationDisplayName)
				.isEqualTo(displayName)

		assertLinkExists(client, "parent")
		CommonMethods.verifyNavigationLinkDoesNotExist("child")
		assertLinkExists(client, "top")
		assertLinkExists(client, "items")
	}

	@Then('^the parent category (.+) that has sub-category is at the expected category level and has items$')
	static void verifyParentCategoryIsAtExpectedLevel(def displayName) {
		def navigationDisplayName = client["display-name"]
		assertThat(navigationDisplayName)
				.isEqualTo(displayName)

		CommonMethods.verifyNavigationLinkDoesNotExist("parent")
		assertLinkExists(client, "child")
		assertLinkExists(client, "top")
		assertLinkExists(client, "items")
	}

	@Then('^there is (?:a|an) (.+) link$')
	static void verifyLinkExists(String relValue) {
		assertLinkExists(client, relValue)
	}

	@Then('^there (?:is|are) (.+) link(?:s)? of rel (.+)$')
	static void verifyNumberOfLinks(Integer count, def rel) {
		assertThat(client.body.links.toString().count("rel:$rel"))
				.as("The number of links is not as expected")
				.isEqualTo(count)
	}

	@Then('^I should see the field links is empty$')
	static void verifyNoOtherLinks() {
		def actualLinks = getClient().body.links.collect{link -> link.rel}

		// Verifies if the list is exactly the same and in the same order.
		assertThat(actualLinks)
				.as("links list is not as expected")
				.isEmpty()
	}

	@Then('^I should see the following links$')
	static void verifyLinksListMatches(DataTable linksTable) {
		def linksList = linksTable.asList(String)
		def actualLinks = getClient().body.links.collect{link -> link.rel}

		assertThat(actualLinks)
				.as("links list is not as expected")
				.containsExactlyInAnyOrderElementsOf(linksList)
	}

	@Then('^I should not see the following links$')
	static void verifyLinksListDoesNotMatchAny(DataTable linksTable) {
		def linksList = linksTable.asList(String)
		def actualLinks = getClient().body.links.collect{link -> link.rel}

		assertThat(actualLinks)
				.as("links list is not as expected")
				.doesNotContainAnyElementsOf(linksList)
	}

	@Then('^I GET (.+)$')
	static void clickURI(def uri) {
		client.GET(uri)
				.stopIfFailure()
	}

	@Then('^save the (?:.*) uri$')
	static void getSelfURI() {
		savedUri = CommonMethods.getSelfUri()
	}

	@Then('^attempt to access the (?:.*)$')
	static void accessSavedURI() {
		client.GET(savedUri)
				.stopIfFailure()
	}

	@Then('^attempt to delete the (?:.*)$')
	static void deleteSavedURI() {
		client.DELETE(savedUri)
	}

	@Then('^return to the saved (?:.*)$')
	static void getSavedURI() {
		client.GET(savedUri)
				.stopIfFailure()
	}

	@Then('^save link rel (?:.*) uri$')
	static void saveURI() {
		savedRel = client.body.links[0].href
	}

	@Then('^post to the saved (?:.*) uri$')
	static void postToSavedURI() {
		client.POST(savedUri, [:])
	}

	@Then('^the uri of (.+) matches the uri of saved (.+) uri$')
	static void compareCurrentAndSavedURI(def currentResource, def savedResource) {
		assertThat(client.body.self.uri)
				.as("uri is not as expected")
				.isEqualTo(savedUri)
	}

	@Then('^try to access (.+) from scope (.+) on scope (.+)$')
	static void replaceScopeAndAccessURI(def uriType, String originalScope, String newScope) {
		client.GET(savedUri.toString().replace(originalScope, newScope))
				.stopIfFailure()
	}

	@Then('^the saved (.+) uri has rel (.+)$')
	static void verifySavedURIHasRel(def saveduritype, def newRel) {
		navigateResources(newRel)
		assertThat(client.body.self.uri)
				.as("uri is not as expected")
				.isEqualTo(savedUri)
	}

	@Then('^I see (?:a|an) (.+) with key-value pair[s]? of data indicated below:$')
	static void verifyResourceHasAttributesWithValues(String resourceName, DataTable table) {
		assertMap(client['address'], table.asMap(String.class, String.class))
	}

	@Then('^attempt to select the original shoppers address$')
	static void selectShoppersAddress() {
		def selectactionUri = client.body.self.uri
		client.GET(selectactionUri + savedUri)
	}

	@Then('^the field (.+) does not contain value (.+)$')
	static void verifyFieldNotContainsValue(String field, String regex) {
		assertThat(client[field].toString())
				.as("Field $field is not as expected")
				.doesNotContain(regex)
	}

	@Then('^the field (.+) is an empty array$')
	static void verifyFieldHasEmptyArray(String field) {
		assertThat(client[field].toString())
				.as("Field $field is not empty")
				.isEqualTo("[]")
	}

	@Then('^the fields have the following values$')
	static void verifyFieldHasValue(DataTable dataTable) {
		def map = dataTable.asMap(String, String)

		for (String key : map.keySet()) {
			assertThat(client[key])
					.as("Field " + key + " is not as expected")
					.isEqualTo(map.get(key))
		}
	}

	@Then('^the fields contain the following values$')
	static void verifyFieldContainsValues(DataTable dataTable) {
		def keyValueList = dataTable.asList(KeyValue)

		for (KeyValue keyValue : keyValueList) {
			def map = keyValue.getFormMap()
			map.each {
				assertThat(client[it.key].toString())
						.as("Field " + it.key + " is not as expected")
						.contains(it.value)
			}
		}
	}

	@When('^I submit request header with the user traits (.+?)$')
	static void submitRequestWithHeaders(String valueUserTraits) {
		client.headers.put(USER_TRAITS_HEADER, valueUserTraits)
	}

	@Then('^there is an item with display-name (.+)$')
	static void verifyItemHasName(def displayName) {
		CommonMethods.findItemByDisplayName(displayName)
		client.definition()
				.stopIfFailure()
		assertThat(client["display-name"])
				.as("Item display name is not as expected")
				.isEqualTo(displayName)
	}

	@Then('^there is not an item with display-name (.+)$')
	static void verifyItemShouldNotExist(def displayName) {
		Boolean found = false
		client.findElement {
			item ->
				def definition = item.definition()
				if (definition["display-name"] == displayName)
					found = true

		}
		assertThat(found)
				.as("Item $displayName should not exist in element list")
				.isFalse()
	}

	@Then('^the (.+) array field (.+) contains$')
	static void verifyArrayFieldContains(String description, String arrayField, DataTable dataTable) {
		def nameValueList = dataTable.asList(NameValue)

		JSONArray jsonArray = (JSONArray) client[arrayField]

		for (NameValue NameValue : nameValueList) {
			def map = NameValue.nameValueMap()
			map.each {
				boolean nameValueFound = false
				if (jsonArray != null) {
					int len = jsonArray.length()
					for (int i = 0; i < len; i++) {
						if (jsonArray.get(i).getAt("name") == it.key && jsonArray.get(i).getAt("value").toString().equals(it.value)) {
							nameValueFound = true
						}
					}
				}
				assertThat(nameValueFound)
						.as("name: " + it.key + " value: " + it.value + " not found")
						.isTrue()
			}
		}
	}

	@Then('^attempting a DELETE on the (.+)$')
	static void deleteOnLink(def description) {
		client.DELETE(client.body.self.uri)
	}

	@Then('^follow the response$')
	static void followResponse() {
		client.follow()
	}

	@Then('^I POST to (.+) with request body (.+)$')
	static void postToURIWithBody(def uri, def body) {
		client.POST(uri, body)
				.stopIfFailure()
	}

	@Then('^I POST request body (.+) to (.+)$')
	static void postBodyToURI(def body, def uri) {
		client.POST(uri, body)
	}

	@Then('^the response is empty$')
	static void verifyResponseEmpty() {
		assertThat(client.body)
				.as("The response is not empty")
				.isEqualTo(null)
	}

	@Then('^(?:I am shopping in|I switch to) locale (.+) with currency (.+)$')
	static void setLocaleCurrencyTraits(def locale, def currency) {
		client.headers.put(USER_TRAITS_HEADER, "LOCALE=$locale,CURRENCY=$currency")
	}

	@When('^I (?:unselect|select) the choice with field (.+) and value (.+)')
	static void selectChoiceWithField(String field, String choice) {
		boolean choiceExists = false

		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				client.GET(it.href)
				def choiceSelector = client.save()
				client.description()
				if (client[field] == choice) {
					client.resume(choiceSelector)
					client.selectaction()
							.follow()
					choiceExists = true
				}
			}
		}

		assertThat(choiceExists)
				.as("Unable to find choice - $choice")
				.isTrue()
	}

	static boolean isChoiceExists(String field, String choice) {
		boolean choiceExists = false

		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				client.GET(it.href)
				client.description()
				if (client[field] == choice) {
					choiceExists = true
				}
			}
		}
		return choiceExists
	}

	@Then('^I should see a choice with field (.+) and value (.+)')
	static void verifyChoiceExists(String field, String choice) {
		assertThat(isChoiceExists(field, choice))
				.as("Unable to find choice - $choice")
				.isTrue()
	}

	@Then('^I should not see a choice with field (.+) and value (.+)')
	static void verifyChoiceNotExists(String field, String choice) {
		assertThat(isChoiceExists(field, choice))
				.as("Unable to find choice - $choice")
				.isFalse()
	}

	@Then('^the list contains an element with the following (.+)')
	static void verifyElementListContains(String field, DataTable dataTable) {
		def elementList = dataTable.asList(String)
		def resultsUri = client.body.self.uri
		for (String value : elementList) {
			Boolean found = false
			client.GET(resultsUri)
			client.findElement {
				element ->
					def elementField = element."$field"()
					if (elementField["$field"] == value)
						found = true
			}
			assertThat(found)
					.as("Item $value was not in element list")
					.isTrue()
		}
	}

	@Then('^the expected list of (.+) matches the following list$')
	static void verifyListMatches(String fieldName, DataTable facetsListTable) {
		def resultsUri = client.body.self.uri
		def facetList = facetsListTable.asList(String)
		// Verifies if the list is exactly the same and in the same order.
		assertThat(Facets.getActualList("element", fieldName))
				.as("Facets list size/order is not as expected")
				.containsExactlyInAnyOrderElementsOf(facetList)
		//resume to the list of elements
		client.GET(resultsUri)
	}

	@When('^I open the (.+) with field (.+) and value (.+)$')
	static void openElementByField(String elementName, String fieldName, String fieldValue) {
		CommonMethods.findElementBy(elementName, fieldName, fieldValue)
	}

	@When('^I post the selectaction$')
	static void selectAndFollow() {
		client.selectaction()
				.follow()
				.stopIfFailure()
	}

	@Then('^there are the following facets$')
	static void thereAreTheFollowingFacets(DataTable dataTable) throws Throwable {
		dataTable.raw().each {
			def value = it.get(0)
			def valueField = "_element[?(@['display-name']==$value)].display-name"
			assertThat(JsonPath.read(client.getBody(), valueField))
					.as("The field $valueField doesn't contain $value")
					.contains(value)
		}
	}

	@Then('^there are the following facet values$')
	static void checkFacetValues(DataTable table) {
		table.raw().each {
			def value = it.get(0)
			def count = it.get(1)
			def valueField = "_choice[*]._description[?(@.value=='" + value + "')].value"
			def countField = "_choice[*]._description[?(@.value=='" + value + "')].count"
			assertThat(JsonPath.read(client.getBody(), valueField))
					.as("The field $valueField doesn't contain $value")
					.contains(value)
			assertThat(JsonPath.read(client.getBody(), countField))
					.as("The field $countField doesn't contain $count")
					.contains(count)
		}
	}

	@Then('^there are the following offers$')
	static void thereAreTheFollowingOffers(DataTable dataTable) throws Throwable {
		dataTable.raw().each {
			def value = it.get(0)
			def valueField = "_element[*]._code[?(@.code=='$value')].code"
			assertThat(JsonPath.read(client.getBody(), valueField))
					.as("The field $valueField doesn't contain $value")
					.contains(value)
		}
	}

	@And('^I attempt to get the uri (.+)$')
	public void getTheHardcodedUri(String uri) {

		client.GET(uri)
		.stopIfFailure();
	}

	@And('^I attempt to post to the uri (.+)$')
	static void postTheHardcodedUri(String uri) {

		def fields = [:]
		fields.put('name', "test")

		client.POST(uri, [
				descriptor: fields
		])				.stopIfFailure()

	}

	static class KeyValue {
		String key
		String value
	
		Map<String, String> formMap
	
		def getFormMap() {
			formMap = new HashMap<String, String>()
			formMap.put(key, value)
			return formMap
		}
	}

	static class NameValue {
		String name
		String value
	
		Map<String, String> nameValueMap
	
		def nameValueMap() {
			nameValueMap = new HashMap<String, String>()
			nameValueMap.put(name, value)
			return nameValueMap
		}
	}
}

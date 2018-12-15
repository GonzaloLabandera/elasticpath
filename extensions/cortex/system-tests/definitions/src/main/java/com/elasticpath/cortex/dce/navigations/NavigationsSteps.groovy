package com.elasticpath.cortex.dce.navigations

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.junit.Assert

import org.json.JSONArray

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Navigation

class NavigationsSteps {

	static final USER_TRAITS_HEADER = "x-ep-user-traits"

	@When('^I (?:open|follow) the root navigations$')
	static void clickNavigationLink() {
		Navigation.getNavigation()
	}

	@And('^the expected navigation list exactly matches the following$')
	static void verifyNavigationListMatches(DataTable navigationTable) { 
		def navigationList = navigationTable.asList(String)
		// Verifies if the list is exactly the same and in the same order.
		assertThat(Navigation.getActualList("element", "name"))
				.as("Navigation list size/order is not as expected")
				.containsExactlyElementsOf(navigationList)
	}

	@And('^the navigation node (.+) should contain exactly the following child nodes$') 
	static void verifyNodeContainsChildNode(String nodeName, DataTable childNodesTable) { 
		def childNodeList = childNodesTable.asList(String)
		Navigation.navigateToCategory(nodeName)
		assertThat(Navigation.getActualList("child", "name"))
				.as("Navigation node size/order is not as expected")
				.containsExactlyElementsOf(childNodeList)
	}

	@And('^the child node (.+) should contain the following sub child nodes$') 
	static void verifyNodeContainsSubNode(String childNode, DataTable subChildNodesTable) { 
		def subChildNodeList = subChildNodesTable.asList(String)
		def actualSubChildNodeList = []

		//	Navigate back to parent node to retrieve all child nodes since the previous step stops at the last child node.
		client.parent()

		client.body.links.findAll {
			if (it.rel == "child") {
				client.GET(it.href)
				if (client["name"] == childNode) {
					client.body.links.findAll {
						if (it.rel == "child") {
							client.GET(it.href)
							actualSubChildNodeList.add(client["name"])
						}
					}
				}
			}
		}
		assertThat(actualSubChildNodeList)
				.as("Child nodes size/order is not as expected")
				.containsExactlyElementsOf(subChildNodeList)
	}

	@Then('^the items are listed in the follow order$') 
	static void verifyItemOrder(DataTable dataTable) { 
		ArrayList<String> orderedDisplayName = dataTable.asList(String)
		JSONArray jsonArray = new JSONArray()

		int i = 0
		boolean nextExists = true

		while (nextExists) {
			nextExists = false
			client.body.links.find {
				if (it.rel == "element") {
					jsonArray.put(i, it)
					i++
					println("$i: $it")
				}
			}
			client.body.links.find {
				if (it.rel == "next") {
					client.next()
					nextExists = true
				}
			}
		}

		assertThat(orderedDisplayName)
				.size()
				.as("The number of items in list was less than the number of links")
				.isLessThanOrEqualTo(jsonArray.length())

		def listUri = client.body.self.uri
		i = 0

		for (def displayName : orderedDisplayName) {
			client.GET(jsonArray.get(i).getAt("href"))
					.definition()
			assertThat(client["display-name"])
					.as("Element $i was not as expected")
					.isEqualTo(displayName)
			i++
		}
		client.GET(listUri)
	}

	@Then('^I open the navigation category (.+)$') 
	static void clickNAvigationCategory(def value) { 
		Navigation.navigateToCategory(value)
	}

	@Then('^I request the navigation definition of (.+) in language (.+)$') 
	static void getCategoryNavigationInLanguage(def category, def language) { 
		client.headers.put(USER_TRAITS_HEADER, "LOCALE=$language")
		Navigation.navigateToCategory(category)
	}

	@Then('^I open the navigation subcategory (.+)$') 
	static void clickNavigationSubCategory(String resources) { 
		Navigation.navigateToCategory(resources.split("->"))
	}

	@Then('^the items link contains (.+) elements$') 
	static void verifyLinkContainsElement(Integer count) { 
		client.items()
				.stopIfFailure()

		assertThat(client.body.links.toString().count("rel:element"))
				.as("The number of links is not as expected")
				.isEqualTo(count)
	}

	@Given('^(?:.+) is missing a value for (?:.+)')
	static void isMissingValue() { }

	@Given('^category (?:.+) has a subcategory and no parent category$')
	static void verifyCategoryHasSubNoParent() { }

	@Given('^the (?:catalog|category) (?:.+) has (?:.+) (?:categories|subcategories|subcategory)$')
	static void verifyCategoryHas() { }

	@Given('^the category (?:.+) is a top level category with no subcategories')
	static void verifyCategoryIsTopLevel() { }

	@Given('^the category (?:.+) contains (?:.+) (?:item|items)')
	static void verifyCategoryContains() { }

	@Given('^the category (?:.+) contains (?:item|items) (?:.+)$')
	static void verifyCategoryContainsItem() { }

	@Given('^the item (?:.+) belongs to a subcategory of (?:.+)')
	static void verifyItemBelongsToCategory() { }

	@Given('^featured items are configured for the category (?:.+)')
	static void verifyFeaturedItemsForCategory() { }

	@Given('^that (?:.+) does not belong to the current scope')
	static void verifyNotBelongToScope() { }

	@When('^I look up navigation item with (?:category|sub-category) code (.*)$')
	static void lookupNavigationWithCode(final String categoryCode) {
		CommonMethods.navigationLookupCode(categoryCode)
	}

	@Then('^I should see navigation details has display-name (.*)$')
	static void verifyNavigationHasName(final String displayName) {
		def navigationDisplayName = client["display-name"]
		assertThat(navigationDisplayName)
				.isEqualTo(displayName)
	}

	@And('^I should see navigation details has (.+?) link $')
	static void verifyNavigationHasNode(final String nodeName) {
		def parentExist = false
		client.body.links.findAll {
			if (it.rel == nodeName) {
				parentExist = true
			}
		}
		Assert.assertTrue(parentExist)
	}

	@And('^I should not see navigation details has (.+?) link $')
	static void verifyNavigationDoesNotHaveNode(final String nodeName) {
		def parentExist = false
		client.body.links.findAll {
			if (it.rel == nodeName) {
				parentExist = true
			}
		}
		Assert.assertFalse(parentExist)
	}

	@When('^I look up navigation item that (?:is invalid|belongs to another scope) with category code (.*)$')
	static void getNavigationByInvalidCode(String categoryCode) {
		navigationLookupByInvalidCategoryCode(categoryCode)
	}

	static void navigationLookupByInvalidCategoryCode(final String categoryCode) {
		client.GET("/")
				.lookups().navigationlookupform()
				.navigationlookupaction([code: categoryCode])
				.stopIfFailure()
	}
}

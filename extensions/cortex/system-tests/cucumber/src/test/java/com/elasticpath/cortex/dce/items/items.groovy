package com.elasticpath.cortex.dce.items

import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists

import cucumber.api.DataTable
import org.json.JSONArray

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks

import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

Then(~/^the (SKU|navigation node) attributes contain$/) { def description, DataTable dataTable ->
	def nameValueList = dataTable.asList(NameValue)

	JSONArray jsonArray = (JSONArray) client["details"];

	for (NameValue NameValue : nameValueList) {
		Map<String, List<String>> map = NameValue.getAttributeMap()
		map.each {
			boolean attributeFound = false
			if (jsonArray != null) {
				int len = jsonArray.length();
				for (int i = 0; i < len; i++) {
					if (jsonArray.get(i).getAt("name") == it.key) {
						ArrayList<String> values = it.value
						def attribute = jsonArray.get(i)
						if (attribute.getAt("display-name").toString().contains(values.get(0))
								&& attribute.getAt("display-value").toString().contains(values.get(1))) {
							attributeFound = true;
						}
					}
				}
			}
			assertThat(attributeFound)
					.as("name: " + it.key + " value: " + it.value + " not found")
					.isTrue()
		}
	}
}

Then(~'I view the item definition for item with code (.+)') { def itemCode ->
	CommonMethods.lookup(itemCode)

	client.definition()
			.stopIfFailure()
}

Then(~'I request the item definition of (.+) in language (.+)') { def itemCode, def locale ->
	client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
	CommonMethods.lookup(itemCode)
	client.definition()
}

Then(~'I request item with code (.+) in language (.+)') { def itemCode, def locale ->
	client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
	CommonMethods.lookup(itemCode)
}

Given(~'item (?:.+) has a (?:.+) of (?:.+) in language (?:.+)') { -> }

Given(~'an item with code (?:.+) has an attribute with name (?:.+) with no value defined') { -> }

Given(~'item (?:.+) has an attribute (?:.+) with value (?:.+) in language (?:.+)') { -> }

Given(~'item (?:.+) has no attributes') { -> }

Then(~'the details do not contain an element with name (.+)') { def attributeName ->
	boolean found = false
	def details = client["details"]

	for (def detail : details) {
		if (detail["display-name"] == attributeName) {
			found = true
		}
	}
	assertThat(found)
			.as("Attribute $attributeName was found in the cortex response")
			.isFalse()
}

Then(~'the attribute with (.+) equal to (.+) has (.+) equal to (.+)') { def keyField, def keyValue, def valueField, def valueValue ->
	boolean found = false
	def details = client["details"]

	for (def detail : details) {
		if (detail[keyField] == keyValue) {
			found = true
			assertThat(detail[valueField])
					.as("$valueField should be $valueValue")
					.isEqualTo(valueValue)
		}
	}
	assertThat(found)
			.as("Attribute $keyValue was not found in the cortex response")
			.isTrue()
}

public class NameValue {
	String name
	String displayName
	String displayValue

	Map<String, List<String>> attributeMap
	List<String> valuesList

	def getAttributeMap() {
		attributeMap = new HashMap<String, List<String>>()
		valuesList = new ArrayList<String>()
		valuesList.add(displayName)
		valuesList.add(displayValue)
		attributeMap.put(name, valuesList)
		return attributeMap;
	}
}

Then(~'a bundle item with code (?:.+) that has (?:.+) constituents') { -> }

Then(~'a (?:nested bundle|bundle) with code (?:.+) that has a bundle constituent with name (?:.+)') { -> }

Then(~'nested bundle (?:.+) has a component (?:.+) that is not sold separately') { -> }

Then(~'the item has (.+) components') { int expected ->
	client.definition()
			.components()
			.stopIfFailure()
	CommonMethods.verifyNumberOfElements(expected)
}

Then(~'the (?:bundle|nested bundle component) has (.+) components') { int expected ->
	client.components()
			.stopIfFailure()
	CommonMethods.verifyNumberOfElements(expected)
}

Then(~'I examine the (?:bundle|nested bundle) component (.+)') { def nestedBundleName ->
	client.definition()
			.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
}

Then(~'the component has a reference to the standalone item (.+)') { def standaloneItem ->
	client.definition()
			.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", standaloneItem)
	assertLinkExists(client, "standaloneitem")

	client.standaloneitem()
	client.definition()
			.stopIfFailure()

	assertThat(client.body."display-name")
			.as("display-name is not as expected")
			.isEqualTo(standaloneItem)

}

Then(~'I open the nested bundle component (.+)') { def nestedBundleName ->
	client.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
}

Then(~'I attempt to add the nested bundle (.+) component (.+) to cart') { def nestedBundleName, def component ->
	client.definition()
			.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
	client.standaloneitem()
			.definition()
			.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", component)
	client.standaloneitem()
}

Then(~'the component has a reference to the standalone item <STANDALONE_ITEM>') { def standaloneItem ->
	client.definition()
			.components()
			.stopIfFailure()

	CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
	client.standaloneitem()
			.definition()
			.stopIfFailure()
}

Then(~'a product with items having codes (?:.+) and (?:.+) distinguished by option (?:.+)') { -> }

Then(~'item with code (?:.+) has (?:.+) value (?:.+)') { -> }

Then(~'I select option (.+) value (.+)') { def optionName, def optionValue ->

	client.options()
			.findElement {
		option ->
			option["display-name"] == optionName
	}

	client.selector()

			.findChoiceOrChosen {
		option ->
			def description = option.description()
			description["display-name"] == optionValue
	}

	client.selectaction()
			.follow()
			.stopIfFailure()
}

Then(~'I am presented with item having (.+)') { def itemCode ->
	client.code()
			.stopIfFailure()

	assertThat(client.body.code)
			.as("sku code is not as expected")
			.isEqualTo(itemCode)
}
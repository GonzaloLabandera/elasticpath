package com.elasticpath.definitions.importtool;

import cucumber.api.java.en.Given;

/**
 * Given steps which determine the state before the test.
 * This steps do not populate any data but are used for better human readability of the tests only.
 */
public class GivenDefinition {


	/**
	 * Indicates that import tool is successfully run as a part of data population mechanism.
	 */
	@Given("^Import tool is successfully run as a part of data population mechanism$")
	public void importToolIsRun() {
		//This step is used only to give a better understanding of pre-existing state of the system
	}

}

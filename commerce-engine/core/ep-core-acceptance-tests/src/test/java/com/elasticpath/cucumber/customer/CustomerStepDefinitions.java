/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.customer;

import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Store test step definitions class.
 * 
 */
@ContextConfiguration("/cucumber.xml")
public class CustomerStepDefinitions {
	
	@Autowired
	private CustomerStepDefinitionsHelper customerStepDefinitionsHelper;
	
	/**
	 * Sets up a default customer for the test environment.
	 *
	 */
	@Given("^(?:with )?a default customer$")
	public void setUpTaxCategories() {
		
		customerStepDefinitionsHelper.setUpDefaultCustomer();
	}

}
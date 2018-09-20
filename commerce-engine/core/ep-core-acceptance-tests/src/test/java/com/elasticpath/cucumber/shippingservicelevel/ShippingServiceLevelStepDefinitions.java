/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.shippingservicelevel;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * ShippingServiceLevel test step definitions class.
 * 
 */
@ContextConfiguration("/cucumber.xml")
public class ShippingServiceLevelStepDefinitions {
	
	@Autowired
	private ShippingServiceLevelStepDefinitionsHelper shippingServiceLevelStepDefinitionsHelper;
	
	/**
	 * Sets up shipping region for the current test environment.
	 *
	 * @param dataTable the data of shipping region
	 */
	@And("with shipping regions of$")
	public void setUpShippingRegions(final DataTable dataTable) {
		
		shippingServiceLevelStepDefinitionsHelper.setUpShippingRegions(dataTable.asMaps(String.class, String.class));
	}
	
	/**
	 * Sets up shipping service levels for the current test environment. 
	 *
	 * @param dataTable the data of shipping service levels
	 */
	@And("^with shipping service levels of$")
	public void setUpShippingServiceLevels(final DataTable dataTable) {
		
		shippingServiceLevelStepDefinitionsHelper.setUpShippingServiceLevels(dataTable.asMaps(String.class, String.class));
	}
}
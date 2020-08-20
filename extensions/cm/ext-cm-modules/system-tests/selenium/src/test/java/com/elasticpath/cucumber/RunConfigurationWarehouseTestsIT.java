package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/configuration/warehouses",
				"json:target/configuration/warehouses.json",
				"junit:target/cucumber-junit-reports/configuration/warehouses/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@warehouse"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/warehouses")
public class RunConfigurationWarehouseTestsIT {

}

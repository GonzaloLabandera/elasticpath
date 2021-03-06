package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/priceListAssignment",
				"json:target/priceListAssignment.json",
				"junit:target/cucumber-junit-reports/priceListAssignment/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@priceListManager", "@priceListAssignment"},
		features = "src/test/resources/com.elasticpath.cucumber/priceListManager/priceListAssignment")
public class RunPriceListAssignmentTestsIT {
}

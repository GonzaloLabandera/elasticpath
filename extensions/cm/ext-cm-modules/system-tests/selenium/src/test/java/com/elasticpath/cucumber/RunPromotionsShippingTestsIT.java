package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/promotionsShipping",
				"json:target/promotionsShipping.json",
				"junit:target/cucumber-junit-reports/promotionsShipping/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@promotionsShipping"},
		features = "src/test/resources/com.elasticpath.cucumber/promotionsShipping")
public class RunPromotionsShippingTestsIT {
}

package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/configuration/shipping",
				"json:target/configuration/shipping.json",
				"junit:target/cucumber-junit-reports/configuration/shipping/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@shipping"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/shipping")
public class RunConfigurationShippingTestsIT {

}

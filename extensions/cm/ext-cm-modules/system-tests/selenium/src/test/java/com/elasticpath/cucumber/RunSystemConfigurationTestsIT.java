package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(format = {"pretty", "html:target/cucumber-html-reports/configuration/systemConfiguration",
		"json:target/configuration/systemConfiguration.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@systemConfiguration"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/systemConfiguration")
public class RunSystemConfigurationTestsIT {

}

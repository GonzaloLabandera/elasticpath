package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/configuration/sortAttributes",
				"json:target/configuration/sortAttributes.json",
				"junit:target/cucumber-junit-reports/configuration/sortAttributes/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@sortAttributes"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/sortAttributes")
public class RunConfigurationSortAttributesTestsIT {

}
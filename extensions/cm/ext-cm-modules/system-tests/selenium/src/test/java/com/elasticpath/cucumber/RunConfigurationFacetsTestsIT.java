package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/configuration/facets",
				"json:target/configuration/facets.json",
				"junit:target/cucumber-junit-reports/configuration/facets/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@facets"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/facets")
public class RunConfigurationFacetsTestsIT {

}
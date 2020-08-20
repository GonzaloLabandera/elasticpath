package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/catalogManagement/navigation",
				"json:target/catalogManagement/navigation.json",
				"junit:target/cucumber-junit-reports/catalogManagement/navigation/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@catalogManagement"},
		features = "src/test/resources/com.elasticpath.cucumber/catalogManagement/navigation")
public class RunCatalogNavigationTestsIT {
}

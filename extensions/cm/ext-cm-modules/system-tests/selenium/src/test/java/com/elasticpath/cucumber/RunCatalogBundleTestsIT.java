package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(format = {"pretty", "html:target/cucumber-html-reports/catalogManagement/bundle", "json:target/catalogManagement/bundle.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@catalogManagement"},
		features = "src/test/resources/com.elasticpath.cucumber/catalogManagement/bundle")
public class RunCatalogBundleTestsIT {
}

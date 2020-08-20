package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/catalogManagement/category",
				"json:target/catalogManagement/category.json",
				"junit:target/cucumber-junit-reports/catalogManagement/category/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@catalogManagement"},
		features = "src/test/resources/com.elasticpath.cucumber/catalogManagement/category")
public class RunCatalogCategoryTestsIT {
}

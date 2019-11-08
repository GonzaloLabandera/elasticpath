package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/changesets/catalogManagement/category",
		"json:target/changesets/catalogManagement/category.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.jms.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@changeset"},
		features = "src/test/resources/com.elasticpath.cucumber/changesets/catalogManagement/category")
public class RunChangeSetCategoryTestsIT {

}

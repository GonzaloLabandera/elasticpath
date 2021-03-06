package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/api",
				"json:target/api.json",
				"junit:target/cucumber-junit-reports/api/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.jms.cucumber", "classpath:com.elasticpath.definitions"},
		tags = {"@api", "@regression"},
		features = "src/test/resources/com.elasticpath.cucumber/api")
public class RunApiTestsIT {
}

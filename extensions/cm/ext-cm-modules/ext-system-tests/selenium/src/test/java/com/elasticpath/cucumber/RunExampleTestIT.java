package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/example",
				"json:target/example.json",
				"junit:target/cucumber-junit-reports/example/cucumber.xml"},
		tags = {"@example"},
		features = "src/test/resources/com.elasticpath.cucumber")
public class RunExampleTestIT {
}

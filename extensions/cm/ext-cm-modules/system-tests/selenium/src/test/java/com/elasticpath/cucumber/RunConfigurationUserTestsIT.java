package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/configuration/users",
				"json:target/configuration/users.json",
				"junit:target/cucumber-junit-reports/configuration/users/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@configuration", "@user", "~@notReady"},
		features = "src/test/resources/com.elasticpath.cucumber/configuration/users")
public class RunConfigurationUserTestsIT {

}

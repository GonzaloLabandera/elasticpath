package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/dst", "json:target/catalogManagement/dst.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex", "classpath:com.elasticpath.jms.cucumber"},
		tags = {"@dst"},
		features = "src/test/resources/com.elasticpath.cucumber/dst")
public class RunDstTestsIT {
}

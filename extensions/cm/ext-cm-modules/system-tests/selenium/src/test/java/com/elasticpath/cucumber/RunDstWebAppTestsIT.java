package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/dstWebApp", "json:target/catalogManagement/dstWebApp.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex", "classpath:com.elasticpath.jms.cucumber"},
		tags = {"@dstWebApp"},
		features = "src/test/resources/com.elasticpath.cucumber/dstWebApp")
public class RunDstWebAppTestsIT {
}

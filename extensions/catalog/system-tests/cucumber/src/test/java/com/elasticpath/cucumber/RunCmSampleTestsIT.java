package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/cmSample", "json:target/cmSample.json"},
		glue = {"classpath:com.elasticpath.cucumber"},
		tags = {"not @cmsample"},
		features = "src/test/resources/com.elasticpath.cucumber/cmSample")
public class RunCmSampleTestsIT {
}

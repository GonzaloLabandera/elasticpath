package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/jmsSample", "json:target/jmsSample.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.jms.cucumber"},
		tags = {"not @jmssample"},
		features = "src/test/resources/com.elasticpath.cucumber/jmsSample")
public class RunJmsSampleTestsIT {

}

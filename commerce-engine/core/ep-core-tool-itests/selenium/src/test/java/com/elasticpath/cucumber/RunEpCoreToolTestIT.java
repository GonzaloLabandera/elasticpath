package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/epCoreTool",
				"json:target/epCoreTool.json",
				"junit:target/cucumber-junit-reports/epCoreTool/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.definitions"},
		tags = {"@epCoreTool"},
		features = "src/test/resources/com.elasticpath.cucumber")
public class RunEpCoreToolTestIT {
}

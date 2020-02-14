package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/customerService/order/backOrder", "json:target/customerService/order"
		+ "/backOrder.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@customerService", "@order", "@backOrder"},
		features = "src/test/resources/com.elasticpath.cucumber/customerService/order/backOrder")
public class RunCustomerServiceBackOrderTestsIT {

}

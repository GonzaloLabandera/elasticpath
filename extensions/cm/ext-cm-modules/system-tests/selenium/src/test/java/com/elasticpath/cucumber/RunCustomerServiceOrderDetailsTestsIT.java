package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-html-reports/customerService/order/orderDetails", "json:target/customerService/order"
		+ "/orderDetails.json"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@customerService", "@order", "@orderDetails"},
		features = "src/test/resources/com.elasticpath.cucumber/customerService/order/orderDetails")
public class RunCustomerServiceOrderDetailsTestsIT {

}

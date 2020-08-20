package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/customerService/order/orderReturn",
				"json:target/customerService/order/orderReturn.json",
				"junit:target/cucumber-junit-reports/customerService/order/orderReturn/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@customerService", "@orderReturn"},
		features = "src/test/resources/com.elasticpath.cucumber/customerService/order/orderReturn")
public class RunCustomerServiceOrderReturnTestsIT {

}

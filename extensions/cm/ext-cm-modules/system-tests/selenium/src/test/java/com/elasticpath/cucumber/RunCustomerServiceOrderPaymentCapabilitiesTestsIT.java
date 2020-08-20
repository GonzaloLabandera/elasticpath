package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/customerService/order/orderPaymentCapabilities",
				"json:target/customerService/order/orderPaymentCapabilities.json",
				"junit:target/cucumber-junit-reports/customerService/order/orderPaymentCapabilities/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@customerService", "@orderPaymentCapabilities"},
		features = "src/test/resources/com.elasticpath.cucumber/customerService/order/orderPaymentCapabilities")
public class RunCustomerServiceOrderPaymentCapabilitiesTestsIT {

}

package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run Cucumber Features test scenarios.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/priceList",
				"json:target/priceList.json",
				"junit:target/cucumber-junit-reports/priceList/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@regressionTest", "@priceListManager", "@priceList"},
		features = "src/test/resources/com.elasticpath.cucumber/priceListManager/priceList")
public class RunPriceListTestsIT {

}

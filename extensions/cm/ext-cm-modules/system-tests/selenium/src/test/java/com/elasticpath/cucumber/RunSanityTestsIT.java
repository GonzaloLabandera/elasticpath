package com.elasticpath.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class is used to run sanity tests.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-html-reports/sanity",
				"json:target/sanitytest.json",
				"junit:target/cucumber-junit-reports/sanitytest/cucumber.xml"},
		glue = {"classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.cortex"},
		tags = {"@sanityTest"},
		features = "src/test/resources/com.elasticpath.cucumber/sanity")
public class RunSanityTestsIT {
}

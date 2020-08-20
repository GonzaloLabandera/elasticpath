/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.uat.email.customer;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class bootstraps Cucumber.
 * <p/>
 * This will run all Cucumber features found on the classpath.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = { "pretty", "html:target/cucumber", "json:target/cucumber.json", "junit:target/cucumber-junit-reports/cucumber.xml" },
		glue = "com.elasticpath.uat")
public class RunCucumberIT {

}
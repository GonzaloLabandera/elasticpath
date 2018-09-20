/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This class bootstraps Cucumber.
 * <p/>
 * This will run all Cucumber features found on the classpath.
 */
@RunWith(Cucumber.class)
@Cucumber.Options(format = {"pretty", "html:target/cucumber", "json:target/cucumber.json"})
public class RunCucumberIT {

}
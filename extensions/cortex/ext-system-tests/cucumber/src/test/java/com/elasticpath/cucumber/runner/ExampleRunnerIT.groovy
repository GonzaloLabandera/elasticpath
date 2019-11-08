package com.elasticpath.cucumber.runner
/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */


import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

/**
 * Bridges Maven failsafe to Cucumber.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = ["pretty", "html:target/cucumber-html-report", "json:target/cucumber.json", "junit:target/cucumber-junit.xml"],
		features = ["src/test/resources/features", "classpath:features"],
		glue = ["classpath:com.elasticpath.cortex.dce", "classpath:com.elasticpath.jms.cucumber", "classpath:com.elasticpath.cucumber"],
		tags = ["@example"],
		strict = true
)
class ExampleRunnerIT {}

/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.cucumber.runner

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
		glue = ["classpath:com.elasticpath.performance.definitions", "classpath:com.elasticpath.integration.definitions",
				"classpath:com.elasticpath.cortex.dce"],
		tags = ["~@bug", "~@notReady", "~@customerZero"],
		strict = true
)
class CucumberRunnerIT {}

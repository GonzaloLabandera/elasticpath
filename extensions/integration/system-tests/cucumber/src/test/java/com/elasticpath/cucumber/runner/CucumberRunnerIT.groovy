/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
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
		features = ["src/test/resources/features"],
		glue = ["classpath:com.elasticpath.integration.definitions", "classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.jms.cucumber",
				"classpath:com.elasticpath.repo.cucumber"],
		tags = ["not @bug", "not @notReady", "not @customerZero"],
		strict = true
)
class CucumberRunnerIT {}

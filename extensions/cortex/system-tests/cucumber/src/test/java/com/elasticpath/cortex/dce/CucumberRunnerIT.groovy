/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cortex.dce

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

/**
 * Bridges Maven failsafe to Cucumber.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		format = ["pretty", "html:target/cucumber-html-report", "json:target/cucumber.json", "junit:target/cucumber-junit.xml"],
		features = ["src/test/resources/features"],
		glue = ["classpath:com.elasticpath.cortex.dce"],
		tags = ["~@bug", "~@notready", "~@jms-tests"],
		strict = true
)
class CucumberRunnerIT {}

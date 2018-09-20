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
		format = ["pretty", "html:target/cucumber-JMS-html-report", "json:target/cucumber-JMS.json", "junit:target/cucumber-JMS-junit.xml"],
		features = ["src/test/resources/features"],
		strict = true,
		glue = ["classpath:com.elasticpath.cucumber", "classpath:com.elasticpath.jms.cucumber"],
		tags = ["~@bug", "~@notready", "@jms-tests"]
)
class JMSRunnerIT {}
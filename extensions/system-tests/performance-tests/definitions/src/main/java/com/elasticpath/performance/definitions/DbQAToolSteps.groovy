/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.definitions

import cucumber.api.Scenario
import cucumber.api.java.Before
import cucumber.api.java.en.When

import com.elasticpath.performance.definitions.DatabaseAnalyzerClient
import com.elasticpath.performancetools.queryanalyzer.QueryAnalyzerConfigurator

class DbQAToolSteps {

	@Before
	static void before(Scenario scenario) {
		def testName = scenario.getName()
		def testId = scenario.getId().split(":")[1]

		QueryAnalyzerConfigurator.INSTANCE.clean()

		QueryAnalyzerConfigurator.INSTANCE
				.setTestId(testId)
				.setTestName(testName)
	}

	@When('^I start measuring db queries on (.+) server$')
	static void databaseAnalyzerStart(def applicationName) {
		QueryAnalyzerConfigurator.INSTANCE
				.setApplicationName(applicationName)

		DatabaseAnalyzerClient.instance.start(true)
	}
	@When('^I start measuring db queries on (.+) server without cleaning application cache$')
	static void databaseAnalyzerStartWithoutCleaningCache(def applicationName) {
		QueryAnalyzerConfigurator.INSTANCE
				.setApplicationName(applicationName)
		DatabaseAnalyzerClient.instance.start(false)
	}

	@When('^I stop measuring db queries$')
	static void databaseAnalyzerStop() {
		DatabaseAnalyzerClient.instance.stop()
	}
}

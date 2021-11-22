/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.definitions

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CSV_OUTPUT_FILE_EXTENSION
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.JSON_OUTPUT_FILE_EXTENSION

import com.google.common.collect.Lists

import com.elasticpath.performancetools.queryanalyzer.LogParser
import com.elasticpath.performancetools.queryanalyzer.QueryAnalyzerConfigurator

@Singleton
class DatabaseAnalyzerClient {

	boolean isRunning = false

	/**
	 * Starts database query analyzer. Clears application's ehcache.
	 */
	def start(def cleanCache) {
		if (isRunning) {
			throw new IllegalStateException("Database query analyzer has been already started!")
		}

		H2DbClient.resetH2Database()
		Thread.sleep(2000);

		QueryAnalyzerConfigurator.INSTANCE
				.init()
				.clearEhCache(cleanCache)
                .setOutputFileExtensions(Lists.newArrayList(JSON_OUTPUT_FILE_EXTENSION, CSV_OUTPUT_FILE_EXTENSION))

		Thread.sleep(3000) //wait a bit until running apps picks up the new db
		isRunning = true
	}

	/**
	 * Stops database query analyzer and generates db statistics
	 */
	def stop() {
		if (!isRunning) {
			throw new IllegalStateException("Database query analyzer is not running!")
		}

        LogParser.INSTANCE
				.printConfiguration()
				.restoreLogLevels()
                .parse()
                .generateStatistics()
		isRunning = false
	}
}

package com.elasticpath.cortex.dce

import static ClasspathFluentRelosClientFactory.createClient
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import java.nio.file.Files
import java.nio.file.Paths

import cucumber.api.Scenario
import cucumber.api.java.After
import cucumber.api.java.Before
import org.apache.log4j.Logger

class Setup {

	private static final Logger LOGGER = Logger.getLogger(Setup.class)
	private String cortexFileName
	private static String cortexLogFolderPath
	private static boolean propBuildDirExists

	/**
	 * For Before hooks, lower order number runs first.
	 *
	 * For After hooks, higher order number runs first.
	 * Default order value is 10000
	 */

	@Before(order = 0)
	void beforeScenario(final Scenario scenario) {
		String buildDirectory = PropertyManager.getInstance().getProperty("project.build.directory")
		if (buildDirectory != null && buildDirectory.length() > 0) {
			propBuildDirExists = true
			cortexLogFolderPath = buildDirectory + "/cortexLogs/"
			cortexFileName = scenario.getName() + "_" + UUID.randomUUID().toString().substring(0, 5) + ".log"
			File file = new File(cortexLogFolderPath)

			if (!file.exists()) {
				file.mkdir()
			}

			try {
				PrintStream fileOut = new PrintStream(cortexLogFolderPath + cortexFileName)
				System.setOut(fileOut)
			} catch (Exception ex) {
				LOGGER.info(ex.getMessage())
			}
		}
	}

	@Before(order = 1)
	static void before() {
		createClient()
	}

	@Before(value = "@HAL", order = 2)
	static beforeHAL() {
		client.getHeaders().Accept = "application/hal+json"
		def restClient = client.getRestClient()
		restClient.parser.'application/hal+json' = restClient.parser.'application/json'
	}

	@After(order = 10001)
	void afterScenario(final Scenario scenario) {
		if (scenario.isFailed()) {
			if (getCortexLog() != null && getCortexLog().length() > 0) {
				String cortexHtml = "<b>Cortex Logs:</b><br> <textarea rows='50' cols='175'>" + getCortexLog() + "</textarea> "
				scenario.embed(cortexHtml.getBytes(), "text/html")
			}
		}
	}

	/**
	 * @return cortex log file.
	 */
	private String getCortexLog() {
		try {
			if (propBuildDirExists) {
				return new String(Files.readAllBytes(Paths.get(cortexLogFolderPath + cortexFileName)))
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage())
		}
		return "cortex logs not recorded for this session";
	}
}

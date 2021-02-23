package com.elasticpath.coretool;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.elasticpath.selenium.domainobjects.ReplaceString;
import com.elasticpath.selenium.framework.util.PropertyManager;
import com.elasticpath.sync.SyncConfig;

/**
 * CoreTool class.
 */
@SuppressWarnings({"PMD.AvoidUsingHardCodedIP"})
public class CoreTool {

	private final PropertyManager propertyManager = PropertyManager.getInstance();
	private final String resourseFolderPath;
	private int exitValue = 1;
	private final String coreToolCliPath;
	private static final String CORE_TOOL_FILE_NAME = "coreTool.sh";
	private URL loc;
	private static final Logger LOGGER = Logger.getLogger(SyncConfig.class);
	private final ReplaceString replaceString;
	private static boolean isConfigUpdated;

	/**
	 * Constructor.
	 */
	public CoreTool() {
		URL loc = getClass().getClassLoader().getResource(CORE_TOOL_FILE_NAME);
		resourseFolderPath = new File(loc.getFile()).getParent();
		coreToolCliPath = resourseFolderPath + "/" + propertyManager.getProperty("ep.core.tool.cli");
		replaceString = new ReplaceString();
	}

	/**
	 * Updates the password.
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	public void updatePassword(final String userName, final String password) {
		runEpCoreTool("set-cmuser-password " + userName + "=" + password);
	}

	/**
	 * Rebuilds all the indexes.
	 */
	public void rebuildIndexesWithCoreTool() {
		runEpCoreTool("request-reindex");
	}

	/**
	 * Rebuilds the index.
	 *
	 * @param indexName the index name
	 */
	public void rebuildIndexWithCoreTool(final String indexName) {
		runEpCoreTool("request-reindex " + indexName);
	}

	/**
	 * Updates setting defined value.
	 *
	 * @param settingName the setting name
	 * @param value       the defined value
	 */
	public void updateSettingDefinedValue(final String settingName, final String value) {
		runEpCoreTool("set-setting " + settingName + "=" + value);
	}

	/**
	 * Updates setting metadata value
	 * @param metadataName the metadata name
	 * @param value the metadata value
	 */
	public void updateSettingMetadataValue(String settingName, String metadataName, String value) {
		runEpCoreTool("set-settings-metadata " + settingName + "@" + metadataName + "=" + value);
	}

	/**
	 * Gets the process exit value.
	 *
	 * @return process exit value
	 */
	public int getExitValue() {
		return this.exitValue;
	}

	/**
	 * Runs the coreTool.sh file.
	 *
	 * @param coreToolCommand the core tool command
	 */

	public void runEpCoreTool(final String coreToolCommand) {
		if (!isConfigUpdated) {
			modifyEpCoreToolConfig();
			isConfigUpdated = true;
		}
		Process process = null;
		BufferedReader bufferedReader = null;
		try {
			loc = getClass().getClassLoader().getResource(CORE_TOOL_FILE_NAME);
			Runtime.getRuntime().exec("chmod 777 " + loc.getPath()).waitFor();
			Runtime.getRuntime().exec("chmod 777 " + coreToolCliPath + "/epcoretool.sh").waitFor();
			String[] cmd = {loc.getPath(), resourseFolderPath + "/" + propertyManager.getProperty("ep.core.tool.cli"), coreToolCommand};
			LOGGER.info("cmd ...... : " + resourseFolderPath + "/" + propertyManager.getProperty("ep.core.tool.cli")
					+ "coreToolCommand: " + coreToolCommand);
			process = Runtime.getRuntime().exec(cmd);

			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				LOGGER.info(line);
			}
			exitValue = process.waitFor();
			LOGGER.info("Process exitValue: " + exitValue);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}

		assertThat(exitValue)
				.as("Ep Core Tool process failed")
				.isEqualTo(0);
	}

	/**
	 * Updates the epcoretool.config file.
	 */
	public void modifyEpCoreToolConfig() {
		String configFileName = "epcoretool.config";
		String filePath = coreToolCliPath + "/" + configFileName;
		replaceString.replaceString(filePath, "localhost", propertyManager.getProperty("db.connection.host"));
		replaceString.replaceString(filePath, "127.0.0.1", propertyManager.getProperty("db.connection.host"));
		replaceString.replaceString(filePath, "COMMERCEDB", propertyManager.getProperty("db.data.base.name"));

		Properties coreToolProperties = getCoreToolConfigProperties(filePath);

		if (coreToolProperties.getProperty("db.connection.username").equals("ep")) {
			replaceString.replaceString(filePath, "db.connection.username=ep", "db.connection.username="
					+ propertyManager.getProperty("db.connection.username"));
		}
		if (coreToolProperties.getProperty("db.connection.password").equals("ep")) {
			replaceString.replaceString(filePath, "db.connection.password=ep", "db.connection.password="
					+ propertyManager.getProperty("db.connection.password"));
		}
		printEpCoreToolConfig(configFileName);
	}

	private Properties getCoreToolConfigProperties(final String filePath) {
		Properties properties = new Properties();
		try (InputStream inputStream = new FileInputStream(filePath)) {
			properties.load(inputStream);
		} catch (Exception e) {
			LOGGER.error("Error occurred", e);
		}
		return properties;
	}

	/**
	 * Prints the config file.
	 *
	 * @param fileName the config file name
	 */
	public void printEpCoreToolConfig(final String fileName) {
		LOGGER.info("********** " + fileName + "**********");
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(coreToolCliPath + "/" + fileName))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				LOGGER.info(line);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred", e);
		}
	}
}
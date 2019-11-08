package com.elasticpath.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;

import com.elasticpath.selenium.domainobjects.ReplaceString;
import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * SyncConfig class.
 */
@SuppressWarnings({"PMD.AvoidUsingHardCodedIP"})
public class SyncConfig {

	private final PropertyManager propertyManager = PropertyManager.getInstance();
	private final String resourseFolderPath;
	private int exitValue = 1;
	private final String syncCliPath;
	private static final String SYNC_FILE_NAME = "sync.sh";
	private URL loc;
	private static final Logger LOGGER = Logger.getLogger(SyncConfig.class);
	private final ReplaceString replaceString;

	/**
	 * Constructor.
	 */
	public SyncConfig() {
		URL loc = getClass().getClassLoader().getResource(SYNC_FILE_NAME);
		resourseFolderPath = new File(loc.getFile()).getParent();
		syncCliPath = resourseFolderPath + "/" + propertyManager.getProperty("sync.cli");
		replaceString = new ReplaceString();
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
	 * Runs the data sync command in sync.sh file.
	 *
	 * @param changeSetGuid the change set guid
	 */

	public void runDataSync(final String changeSetGuid) {
		Process process = null;
		BufferedReader bufferedReader = null;
		try {
			loc = getClass().getClassLoader().getResource("sync.sh");
			Runtime.getRuntime().exec("chmod 777 " + loc.getPath()).waitFor();
			Runtime.getRuntime().exec("chmod 777 " + syncCliPath + "/synctool.sh").waitFor();
			String[] cmd = {loc.getPath(), resourseFolderPath + "/" + propertyManager.getProperty("sync.cli"), changeSetGuid};
			LOGGER.info("cmd ...... : " + resourseFolderPath + "/" + propertyManager.getProperty("sync.cli") + " changeSetGuid: " + changeSetGuid);
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
				.as("Sync process failed")
				.isEqualTo(0);
	}

	/**
	 * Updates source and target config.xml.
	 */
	public void updateSyncConfigFiles() {
		modifySourceConfig();
		modifytargetConfig();
	}

	private void modifySourceConfig() {
		String configFileName = "sourceconfig.xml";
		String filePath = syncCliPath + "/" + configFileName;
		modifySyncConfig(filePath, propertyManager.getProperty("source.db.host"), propertyManager.getProperty("source.db.port"),
				propertyManager.getProperty("sync.database.name"), propertyManager.getProperty("source.db.username"),
				propertyManager.getProperty("source.db.pwd"));
		printConfigFile(configFileName);
	}

	private void modifytargetConfig() {
		String configFileName = "targetconfig.xml";
		String filePath = syncCliPath + "/" + configFileName;
		modifySyncConfig(filePath, propertyManager.getProperty("target.db.host"), propertyManager.getProperty("target.db.port"),
				propertyManager.getProperty("sync.database.name"), propertyManager.getProperty("target.db.username"),
				propertyManager.getProperty("target.db.pwd"));
		printConfigFile(configFileName);
	}

	private void modifySyncConfig(final String filePath, final String dbHost, final String dbPort, final String dbName, final String dbUserName,
								  final String dbPassword) {
		replaceString.replaceString(filePath, "localhost", dbHost);
		replaceString.replaceString(filePath, "127.0.0.1", dbHost);
		replaceString.replaceString(filePath, "3306", dbPort);
		replaceString.replaceString(filePath, "COMMERCEDB", dbName);
		replaceString.replaceString(filePath, "(?s)<login[^>]*>.*?\n",
				"<login>" + dbUserName + "</login>\n");
		replaceString.replaceString(filePath, "(?s)<pwd[^>]*>.*?\n",
				"<pwd>" + dbPassword + "</pwd>\n");
	}

	/**
	 * Prints the config file.
	 *
	 * @param fileName the config file name
	 */
	public void printConfigFile(final String fileName) {
		LOGGER.info("********** " + fileName + "**********");
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(syncCliPath + "/" + fileName))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				LOGGER.info(line);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
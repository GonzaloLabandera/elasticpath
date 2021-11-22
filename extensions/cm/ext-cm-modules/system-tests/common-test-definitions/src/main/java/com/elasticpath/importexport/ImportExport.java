package com.elasticpath.importexport;

import static com.elasticpath.base.util.ProcessBuilderUtils.execWithRedirectedErrorStream;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.selenium.domainobjects.ReplaceString;
import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * SyncConfig class.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidUsingHardCodedIP"})
public class ImportExport {

	private final PropertyManager propertyManager = PropertyManager.getInstance();
	private final String resourseFolderPath;
	private int exitValue = 1;
	private static final String IMPORT_EXPORT_FILE_NAME = "impex.sh";
	private static final String COPY_FILE_NAME = "copy.sh";
	private static final String DELETE_FOLDER_FILE_NAME = "deleteFolder.sh";
	private static final String CHMOD_777 = "chmod 777 ";
	private static final String TOTAL_NUMBER_OF_OBJECTS = "Total Number Of Objects";
	private static final String ARE_NOT_AS_EXPECTED = " are not as expected";
	private final String importExportCli;
	private static final String NEW_IMPORT_EXPORT_CLI = "ep-importExport-cli";
	private final String newImportExportFolderPath;
	private URL loc;
	private static final Logger LOGGER = LogManager.getLogger(ImportExport.class);
	private final ReplaceString replaceString;
	private String outPutLog;
	private Map<String, Integer> exportedObjectsMap;
	private Map<String, Integer> importedObjectsMap;
	private String exportDbHost;
	private String exportDBPort;

	/**
	 * Constructor.
	 */
	public ImportExport() {
		loc = getClass().getClassLoader().getResource(IMPORT_EXPORT_FILE_NAME);
		resourseFolderPath = new File(loc.getFile()).getParent();
		importExportCli = propertyManager.getProperty("import.export.cli");
		newImportExportFolderPath = resourseFolderPath + "/" + NEW_IMPORT_EXPORT_CLI;
		replaceString = new ReplaceString();
	}

	/**
	 * Copies the import export cli unzipped folder.
	 */
	public void copyFolder() {
		try {
			loc = getClass().getClassLoader().getResource(COPY_FILE_NAME);
			execWithRedirectedErrorStream(CHMOD_777 + loc.getPath()).waitFor();
			String[] cmd = {loc.getPath(), resourseFolderPath, importExportCli, newImportExportFolderPath};
			LOGGER.info("cmd ...... : {} ------ {} ------- {} ------- {}", loc.getPath(), resourseFolderPath,
					importExportCli, newImportExportFolderPath);
			Process process = execWithRedirectedErrorStream(cmd);
			readLines(process);
		} catch (Exception e) {
			LOGGER.error(e);
		}

		assertThat(exitValue)
				.as("Copy process failed")
				.isEqualTo(0);
	}

	/**
	 * Delete new copied import export folder.
	 */
	public void deleteFolder() {
		try {
			loc = getClass().getClassLoader().getResource(DELETE_FOLDER_FILE_NAME);
			execWithRedirectedErrorStream(CHMOD_777 + loc.getPath()).waitFor();
			String[] cmd = {loc.getPath(), resourseFolderPath, newImportExportFolderPath};
			LOGGER.info("cmd ...... : {} ------ {} ------- {}", loc.getPath(), resourseFolderPath, newImportExportFolderPath);
			Process process = execWithRedirectedErrorStream(cmd);
			readLines(process);
		} catch (Exception e) {
			LOGGER.error(e);
		}

		assertThat(exitValue)
				.as("Delete process failed")
				.isEqualTo(0);
	}

	private void readLines(final Process process) {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

			String line;
			StringBuilder outputLogBuilder = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				LOGGER.info(line);
				outputLogBuilder.append(line);
				outputLogBuilder.append("#\n");
			}
			exitValue = process.waitFor();
			LOGGER.info("Process exitValue: {}", exitValue);
			outPutLog = outputLogBuilder.toString();
			LOGGER.info(outPutLog);
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Runs the export command in impex.sh file.
	 */
	public void runExport() {
		LOGGER.info("Running Export.....");
		printConfigFile();
		printSearchConfigurationFile();
		runImportExport("Export", "-e searchconfiguration.xml");
	}

	/**
	 * Runs the import command in impex.sh file.
	 */
	public void runImport() {
		LOGGER.info("Running Import.....");
		printConfigFile();
		runImportExport("Import", "-i -c importconfiguration.xml");
	}

	/**
	 * Runs the import command in impex.sh file.
	 */
	public void runImportWithFlag(final String changeSetGuid, final String flag) {
		LOGGER.info("Running Import with change set guid and flag (s1 or s2).....");
		printConfigFile();
		LOGGER.info("change set guid: {}", changeSetGuid);
		runImportExport("Import", "-i -c importconfiguration.xml -g " + changeSetGuid + " -s " + flag);
	}

	/**
	 * Runs the import command in impex.sh file.
	 */
	public void runImport(final String changeSetGuid) {
		LOGGER.info("Running Import with change set guid.....");
		printConfigFile();
		runImportExport("Import", "-i -c importconfiguration.xml -g " + changeSetGuid);
	}

	/**
	 * Runs the import/export command in impex.sh file.
	 */
	private void runImportExport(final String type, final String cliCmd) {
		try {
			loc = getClass().getClassLoader().getResource(IMPORT_EXPORT_FILE_NAME);
            execWithRedirectedErrorStream(CHMOD_777 + loc.getPath()).waitFor();
            execWithRedirectedErrorStream(CHMOD_777 + newImportExportFolderPath + "/importexport.sh").waitFor();
			String[] cmd = {loc.getPath(), resourseFolderPath + "/" + NEW_IMPORT_EXPORT_CLI, cliCmd};
			LOGGER.info("cmd ...... : {}/{}", resourseFolderPath, NEW_IMPORT_EXPORT_CLI);

			Process process = execWithRedirectedErrorStream(cmd);
			readLines(process);
		} catch (Exception e) {
			LOGGER.error(e);
		}

		assertThat(exitValue)
				.as(type + " process failed")
				.isEqualTo(0);
	}

	/**
	 * Modifies export cli files.
	 */
	public void modifyExportCliFiles() {
		modifyExportConfigFile();
		modifyImportExportConfigXmlFile();
	}

	/**
	 * Modifies import cli files.
	 */
	public void modifyImportCliFiles() {
		modifyImportExportToolConfigFile();
	}

	/**
	 * Verifies exported objects.
	 *
	 * @param exportResultMap the exported result map
	 */
	public void verifyExportedObjects(final Map<String, Integer> exportResultMap) {
		String[] actualResultArray = outPutLog.substring(outPutLog.indexOf(TOTAL_NUMBER_OF_OBJECTS)).split("#");

		exportResultMap.forEach((key, value) -> {
			boolean resultFound = false;
			for (String actualResult : actualResultArray) {
				String[] objectResultArray = actualResult.split(":");

				if (objectResultArray[0].trim().equals(key)) {
					resultFound = true;
					assertThat(Integer.valueOf(objectResultArray[1].trim()))
							.as("Number of exported objects for " + key + ARE_NOT_AS_EXPECTED)
							.isGreaterThan(value);
					break;
				}
			}
			assertThat(resultFound)
					.as("Unable to find export for object: " + key)
					.isTrue();
		});
	}

	/**
	 * Creates exported objects map.
	 */
	public void createExportedObjectsMap() {
		exportedObjectsMap = new HashMap<>();
		createObjectsMap(exportedObjectsMap);
		LOGGER.info("Number of exported objects:...  {}", exportedObjectsMap.size());
	}

	/**
	 * Creates imported objects map.
	 */
	public void createImportedObjectsMap() {
		importedObjectsMap = new HashMap<>();
		createObjectsMap(importedObjectsMap);
		LOGGER.info("Number of imported objects:...  {}", importedObjectsMap.size());
	}

	private void createObjectsMap(final Map<String, Integer> objectsMap) {
		String[] actualResultArray = outPutLog.substring(outPutLog.indexOf(TOTAL_NUMBER_OF_OBJECTS)).split("#");
		for (String actualResult : actualResultArray) {
			LOGGER.info("actual Result...............: {}", actualResult);
			String[] objectResultArray = actualResult.split(":");
			if (!objectResultArray[0].contains("EP-ImportExport")
					&& objectResultArray.length > 1 && objectResultArray[1].chars().allMatch(Character::isDigit)) {
				objectsMap.put(objectResultArray[0].trim(), Integer.valueOf(objectResultArray[1].trim()));
			}
		}
	}

	private void removeWarningsAndComments() {
		String warning = "Total Number Of Warnings";
		String comments = "Total Number Of Comments";
		importedObjectsMap.remove(warning);
		importedObjectsMap.remove(comments);
		exportedObjectsMap.remove(warning);
		exportedObjectsMap.remove(comments);
	}

	private void updateImportTotal() {
		int importTotal = importedObjectsMap.get(TOTAL_NUMBER_OF_OBJECTS) + exportedObjectsMap.get("data_policies")
				+ exportedObjectsMap.get("customer_consents") + exportedObjectsMap.get("payment_providers");
		importedObjectsMap.put(TOTAL_NUMBER_OF_OBJECTS, importTotal);
	}

	private void removeUnallowedImportObjects() {
		exportedObjectsMap.remove("data_policies");
		exportedObjectsMap.remove("customer_consents");
		exportedObjectsMap.remove("payment_providers");
	}

	/**
	 * Compares exported with imported objects.
	 */
	public void compareExportedWithImportedObjects() {
		removeWarningsAndComments();
		updateImportTotal();
		removeUnallowedImportObjects();

		assertThat(importedObjectsMap.size())
				.as("Number of exported object types are not same as imported object types")
				.isEqualTo(exportedObjectsMap.size());

		assertThat(exportedObjectsMap)
				.as("Exported objects are not same as imported objects")
				.containsAllEntriesOf(importedObjectsMap);
	}

	/**
	 * Verifies exported results.
	 *
	 * @param resultType the result type
	 * @param expected   the expected value
	 */
	public void verifyExportResult(final String resultType, final int expected) {
		assertThat(exportedObjectsMap.get(resultType))
				.as("The export " + resultType + ARE_NOT_AS_EXPECTED)
				.isEqualTo(expected);
	}

	/**
	 * Verifies imported result.
	 *
	 * @param resultType the result type
	 * @param expected   the expected value
	 */
	public void verifyImportResult(final String resultType, final int expected) {
		assertThat(importedObjectsMap.get(resultType))
				.as("The import " + resultType + ARE_NOT_AS_EXPECTED)
				.isEqualTo(expected);
	}

	/**
	 * Verifies result.
	 *
	 * @param resultType the result type
	 * @param expected   the expected value
	 */
	public void verifyResultGreaterThan(final String resultType, final int expected) {
		assertThat(exportedObjectsMap.get(resultType))
				.as("The " + resultType + ARE_NOT_AS_EXPECTED)
				.isGreaterThan(expected);
	}

	private void modifyExportConfigFile() {
		exportDbHost = propertyManager.getProperty("export.db.connection.host");
		exportDBPort = propertyManager.getProperty("export.db.connection.port");
		String exportImportSchemaName = propertyManager.getProperty("export.import.db.schemaname");
		String filePath = newImportExportFolderPath + "/importexporttool.config";
		replaceString.replaceString(filePath, "127.0.0.1", exportDbHost);
		replaceString.replaceString(filePath, "localhost", exportDbHost);
		replaceString.replaceString(filePath, "3306", exportDBPort);
		replaceString.replaceString(filePath, "COMMERCEDB", exportImportSchemaName);
		replaceString.replaceString(filePath, "(?s)db.connection.username[^=]*=.*?\n",
				"db.connection.username=" + propertyManager.getProperty("export.import.db.connection.username") + "\n");
		replaceString.replaceString(filePath, "(?s)db.connection.password[^=]*=.*?\n",
				"db.connection.password=" + propertyManager.getProperty("export.import.db.connection.password") + "\n");
		replaceString.replaceString(filePath, "localhost", propertyManager.getProperty("ep.jms.host"));
		replaceString.replaceString(filePath, "127.0.0.1", propertyManager.getProperty("ep.jms.host"));
		replaceString.replaceString(filePath, "61616", propertyManager.getProperty("ep.jms.port.tcp"));
	}

	/**
	 * Updates searchconfiguration.xml.
	 *
	 * @param query the query
	 */
	public void updateSearchConfigXml(final String query) {
		String filePath = newImportExportFolderPath + "/searchconfiguration.xml";
		replaceString.replaceString(filePath, "(?s)<searchconfiguration[^>]*>.*?</searchconfiguration>",
				"<searchconfiguration>\n" + query + "</searchconfiguration>");
	}

	/**
	 * Updates searchconfiguration.xml.
	 *
	 * @param exporterString the exporter string
	 */
	public void updateExportConfigXml(final String exporterString) {
		String filePath = newImportExportFolderPath + "/exportconfiguration.xml";
		replaceString.replaceString(filePath, "(?s)<exporters[^>]*>.*?</exporters>",
				"<exporters>\n" + exporterString + "</exporters>");
	}

	/**
	 * Prints the importexporttool.config file.
	 */
	public void printConfigFile() {
		LOGGER.info("*************** importexporttool.config ***************");
		printCliFile("importexporttool.config");
	}

	/**
	 * Prints the searchconfiguration.xml file.
	 */
	public void printSearchConfigurationFile() {
		LOGGER.info("*************** searchconfiguration.xml ***************");
		printCliFile("searchconfiguration.xml");
	}

	/**
	 * Prints the cli file.
	 */
	public void printCliFile(final String fileName) {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(newImportExportFolderPath + "/" + fileName))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				LOGGER.info(line);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}


	private void modifyImportExportToolConfigFile() {
		String filePath = newImportExportFolderPath + "/importexporttool.config";
		replaceString.replaceString(filePath, exportDbHost, propertyManager.getProperty("import.db.connection.host"));
		replaceString.replaceString(filePath, exportDBPort, propertyManager.getProperty("import.db.connection.port"));
	}

	private void modifyImportExportConfigXmlFile() {
		String filePath = newImportExportFolderPath + "/importconfiguration.xml";
		replaceString.replaceString(filePath, "(?s)<source[^>]*>.*?</source>", "<source>"
				+ newImportExportFolderPath + "/target/exported/data/</source>");

		filePath = newImportExportFolderPath + "/exportconfiguration.xml";
		replaceString.replaceString(filePath, "(?s)<target[^>]*>.*?</target>", "<target>"
				+ newImportExportFolderPath + "/target/exported/data/</target>");
	}

	/**
	 * Returns the new import export folder path.
	 *
	 * @return newImportExportFolderPath the new import export folder path
	 */
	public String getNewImportExportFolderPath() {
		return newImportExportFolderPath;
	}

}

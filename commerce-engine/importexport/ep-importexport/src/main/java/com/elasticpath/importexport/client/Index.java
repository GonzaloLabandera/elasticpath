/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.importexport.common.ImportExportContextIdNames;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.summary.impl.SimpleSummaryLayout;
import com.elasticpath.importexport.common.util.MessageResolver;
import com.elasticpath.importexport.common.util.MetaDataMapPopulator;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.importexport.importer.controller.ImportController;

/**
 * The client that is responsible for working with import-export operations.
 */
@SuppressWarnings({"PMD.DoNotCallSystemExit", "PMD.GodClass"})
public class Index {

	private static final String HELP_STRING = "-i [-c importconfiguration.xml] [-g changeSetGuid] [-s stage1|stage2]\n"
			+ "-e searchconfiguration.xml [-c exportconfiguration.xml] [-l locale] [-p importexporttool.config]";

	private static final Logger LOG = LogManager.getLogger(Index.class);

	private final EngineInitialization engine;

	private final MessageResolver messageResolver;

	private String stage;

	private String changeSetGuid;

	private static final String DEFAULT_EXPORT_CONFIGURATION_FILE = "exportconfiguration.xml";

	private static final String DEFAULT_IMPORT_CONFIGURATION_FILE = "importconfiguration.xml";

	private static final int SUCCESS = 0;

	private static final int PARTIAL_SUCCESS = 1;

	private static final int WRONG_COMMAND_LINE_ARGUMENTS = 2;

	private static final int WRONG_CONFIGURATION = 4;

	private static final int COMPLETE_FAIL = 8;

	private static final String FILE_PATH = "file:";

	/**
	 * Constructs Index.
	 */
	public Index() {
		engine = EngineInitialization.getInstance();
		messageResolver = engine.getBeanFactory().getSingletonBean("messageResolver", MessageResolver.class);

		LOG.info("Engine Initialization...");
	}

	/**
	 * Starts this application with given arguments.
	 * <p>
	 * Available three options for starting:
	 * <p>
	 * -h prints help information
	 * <p>
	 * -i starts the import <b>Note: the information for import receives from importconfiguration.xml file by default.
	 * Custom configuration filename can be specified by argument for -c option. If the configuration file does not exist exception
	 * will be thrown.</b>
	 * <p>
	 * -e starts the export <b>Note: the information for export receives from exportconfiguration.xml file.
	 * Custom configuration filename can be specified by argument for -c option. If this file does not exist exception
	 * will be thrown.</b>
	 * <p>
	 * configuration.xml file with search settings must be provided as argument for -e option.
	 *
	 * @param args the available arguments (-h, -i, -e, -c)
	 */
	public static void main(final String[] args) {
		// create Options object
		Options options = new Options();

		// add options
		options.addOption("p", "properties", true, "set the import/export tool application properties file location");
		options.addOption("e", "export", true, "do the export");
		options.addOption("i", "import", false, "do the import");
		options.addOption("h", "help", false, "prints current message");
		options.addOption("c", "config", true, "set the import or export configuration file location");
		options.addOption("l", "locale", true, "set the locale");
		options.addOption("g", "changeset", true, "set the change set guid to import into");
		options.addOption("s", "stage", true, "set the processing stage to run [default: both stage 1 and 2]. "
				+ "stage1 only adds objects to the specified change set. stage2 means only import data"
				+ "If change set guid is not specified, stage parameter will be ignored");

		try {
			// parse
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("p")) {
				LOG.info("Setting the config location argument with " + cmd.getOptionValue('p'));
				System.setProperty("configLocation", FILE_PATH + cmd.getOptionValue("p"));
			}

			if (cmd.hasOption("e")) {
				int exitCode = createIndex(cmd).doExport(
						getConfigurationFileName(cmd, DEFAULT_EXPORT_CONFIGURATION_FILE),
						cmd.getOptionValue("e"));
				System.exit(exitCode);
			} else if (cmd.hasOption("i")) {
				int exitCode = createIndex(cmd).doImport(
						getConfigurationFileName(cmd, DEFAULT_IMPORT_CONFIGURATION_FILE));
				System.exit(exitCode);
			} else {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(HELP_STRING, options);
				System.exit(WRONG_COMMAND_LINE_ARGUMENTS);
			}
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HELP_STRING, options);
			System.exit(WRONG_COMMAND_LINE_ARGUMENTS);
		} catch (FileNotFoundException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HELP_STRING, null, options, "Configuration file could not be found");
			System.exit(WRONG_COMMAND_LINE_ARGUMENTS);
		}
	}

	private MetaDataMapPopulator getMetaDataMapPopulator() {
		return getBeanFactory().getSingletonBean("metaDataMapPopulator", MetaDataMapPopulator.class);
	}

	private static String getConfigurationFileName(final CommandLine cmd, final String defaultConfiguration) {
		String configurationFileName = defaultConfiguration;
		if (cmd.hasOption("c")) {
			configurationFileName = cmd.getOptionValue("c");
		}
		return configurationFileName;
	}

	/**
	 * Does the Import.
	 *
	 * @param configFileName the name of file containing import configuration
	 * @return result code: SUCCESS, PARTIAL_SUCCESS, WRONG_CONFIGURATION or COMPLETE_FAIL
	 * @throws FileNotFoundException in case configuration hasn't been loaded
	 */
	public int doImport(final String configFileName) throws FileNotFoundException {

		ImportController controller = getBeanFactory().getSingletonBean(ImportExportContextIdNames.IMPORT_CONTROLLER, ImportController.class);

		try (FileInputStream configStream = new FileInputStream(configFileName)) {
			getMetaDataMapPopulator().configureMetadataMapForImport(changeSetGuid, stage);

			controller.loadConfiguration(configStream);
			LOG.debug("Import Controller Prepared.");
			LOG.info("Import Started.");
			Summary summary = controller.executeImport();
			printResults(summary);
			LOG.info("Import Executed.");
			if (controller.failuresExist()) {
				return PARTIAL_SUCCESS;
			}
			return SUCCESS;
		} catch (ConfigurationException e) {
			LOG.error("There are configuration problems on import", e);
			return WRONG_CONFIGURATION;
		} catch (ImportRuntimeException e) {
			LOG.error("Fatal error during import. See log for details.", e);
			LOG.error(e.getIEMessage());
			return COMPLETE_FAIL;
		} catch (IOException | RuntimeException e) {
			LOG.error("Fatal error during import", e);
			return COMPLETE_FAIL;
		}

	}

	/**
	 * Does the Export.
	 *
	 * @param configFileName         the name of file containing export configuration
	 * @param searchCriteriaFileName the name of file containing export search query
	 * @return result code: SUCCESS, PARTIAL_SUCCESS, WRONG_CONFIGURATION or COMPLETE_FAIL
	 * @throws FileNotFoundException in case configuration hasn't been loaded
	 */
	public int doExport(final String configFileName, final String searchCriteriaFileName) throws FileNotFoundException {
		ExportController controller = getBeanFactory().getSingletonBean(ImportExportContextIdNames.EXPORT_CONTROLLER, ExportController.class);
		try (FileInputStream configStream = new FileInputStream(configFileName);
			 FileInputStream searchCriteriaStream = new FileInputStream(searchCriteriaFileName)
		) {
			controller.loadConfiguration(configStream, searchCriteriaStream);
			LOG.debug("Export Controller Prepared.");
			LOG.info("Export Started.");
			Summary summary = controller.executeExport();
			printResults(summary);
			LOG.info("Export Executed.");
			if (controller.failuresExist()) {
				return PARTIAL_SUCCESS;
			}
			return SUCCESS;
		} catch (ConfigurationException e) {
			LOG.error("There are configuration problems on export", e);
			return WRONG_CONFIGURATION;
		} catch (RuntimeException | IOException e) {
			LOG.error("Fatal error during export", e);
			return COMPLETE_FAIL;
		}
	}

	private void printResults(final Summary summary) {
		final SimpleSummaryLayout layout = new SimpleSummaryLayout();
		layout.setMessageResolver(messageResolver);
		LOG.info(layout.format(summary));
	}

	private BeanFactory getBeanFactory() {
		return engine.getBeanFactory();
	}

	private void setLocale(final Locale locale) {
		messageResolver.setLocale(locale);
	}

	private void setStage(final String stage) {
		this.stage = stage;
	}

	private void setChangeSetGuid(final String changeSetGuid) {
		this.changeSetGuid = changeSetGuid;
	}

	private static Locale parseLocale(final CommandLine cmd) {
		Locale locale = LocaleUtils.toLocale(cmd.getOptionValue("l"));
		if (!LocaleUtils.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("Could not find locale with code " + locale + ". Locale is not supported.");
		}
		return locale;
	}

	private static Index createIndex(final CommandLine cmd) {
		Locale locale = null;
		if (cmd.hasOption("l")) {
			locale = parseLocale(cmd);
		}
		Index index = new Index();
		if (locale != null) {
			index.setLocale(locale);
		}
		if (cmd.hasOption("g")) {
			index.setChangeSetGuid(cmd.getOptionValue("g"));
		}
		if (cmd.hasOption("s")) {
			index.setStage(cmd.getOptionValue("s"));
		}
		return index;
	}

}

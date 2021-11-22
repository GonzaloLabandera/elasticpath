/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.tools.epcoretool.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.elasticpath.tools.epcoretool.client.parsers.BulkSetSettingParser;
import com.elasticpath.tools.epcoretool.client.parsers.BulkSetSettingsMetadataParser;
import com.elasticpath.tools.epcoretool.client.parsers.IndexBuildStatusParser;
import com.elasticpath.tools.epcoretool.client.parsers.PingSearchServerParser;
import com.elasticpath.tools.epcoretool.client.parsers.RecompileRuleBaseParser;
import com.elasticpath.tools.epcoretool.client.parsers.RequestReindexAndWaitParser;
import com.elasticpath.tools.epcoretool.client.parsers.RequestReindexParser;
import com.elasticpath.tools.epcoretool.client.parsers.SetCmUserPasswordParser;
import com.elasticpath.tools.epcoretool.client.parsers.SetSettingMetadataParser;
import com.elasticpath.tools.epcoretool.client.parsers.SetSettingParser;
import com.elasticpath.tools.epcoretool.client.parsers.SetStoreURLParser;
import com.elasticpath.tools.epcoretool.client.parsers.UnSetSettingParser;

/**
 * EP Core Tool CLI Main Class.
 */
@SuppressWarnings({"PMD.DoNotCallSystemExit", "PMD.ShortClassName"}) // This is a not a J2EE app
public final class Cli {

	private static final Logger LOG = LogManager.getLogger(Cli.class);

	private static final int SUCCESS = 0;

	private static final int WRONG_COMMAND_LINE_ARGUMENTS = 2;

	private static final int COMPLETE_FAIL = 8;

	private static Map<String, CliParser> parsers;

	private Cli() {
		// Do not allow this class to be instantiated directly.
	}

	/**
	 * Starts this application with given arguments.
	 *
	 * @param args the available arguments
	 */
	public static void main(final String[] args) {
		// https://logging.apache.org/log4j/2.x/faq.html#reconfig_from_code
		// configure log4j
		try {
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.setConfigLocation(getConfigurationFileURI());
		} catch (Exception ex) {
            LOG.error("Logger context init failed.", ex);
			System.exit(1);
		}

		parsers = new TreeMap<>();
		parsers.put("bulk-set-settings", new BulkSetSettingParser());
		parsers.put("index-status", new IndexBuildStatusParser());
		parsers.put("ping-search", new PingSearchServerParser());
		parsers.put("recompile-rulebase", new RecompileRuleBaseParser());
		parsers.put("request-reindex", new RequestReindexParser());
		parsers.put("request-reindex-and-wait", new RequestReindexAndWaitParser());
		parsers.put("set-cmuser-password", new SetCmUserPasswordParser());
		parsers.put("set-setting", new SetSettingParser());
		parsers.put("set-store-url", new SetStoreURLParser());
		parsers.put("unset-setting", new UnSetSettingParser());
		parsers.put("set-settings-metadata", new SetSettingMetadataParser());
		parsers.put("bulk-set-settings-metadata", new BulkSetSettingsMetadataParser());

		try {
			if (args.length == 0) {
				LOG.error("Please specify a command. \nEnter 'help' as a command-line argument for list of available commands.");
				System.exit(WRONG_COMMAND_LINE_ARGUMENTS);
			}

			if ("help".equals(args[0])) {
				help();
				System.exit(SUCCESS);
			}

			CliParser parser = parsers.get(args[0]);

			if (parser == null) {
				LOG.error("Please specify a valid command. \nEnter 'help' as a command-line argument for list of available commands.");
				System.exit(WRONG_COMMAND_LINE_ARGUMENTS);
			}

			if (args.length == 1) {
				parser.execute(null);
			} else {
				parser.execute(args[1]);
			}
		} catch (RuntimeException ex) {
			LOG.error("Unexpected error occurred.", ex);
			System.exit(COMPLETE_FAIL);
		}
		System.exit(SUCCESS);
	}

	/**
	 * CLI help command.
	 */
	public static void help() {
		StringBuilder builder = new StringBuilder();
		for (CliParser parser : parsers.values()) {
			builder.append(parser.help());
		}
		LOG.info("\n{}", builder);
	}

	/**
	 * Get misc/log4j2.xml configuration file URI.
	 * @return file of log4j2.xml
	 * @throws URISyntaxException uri syntax exception
	 */
	public static URI getConfigurationFileURI() throws URISyntaxException {
		URL resource = Cli.class.getClassLoader().getResource("misc/log4j2.xml");
		if (resource == null) {
			throw new IllegalArgumentException("File misc/log4j2.xml not found!");
		} else {
			return resource.toURI();
		}
	}

}
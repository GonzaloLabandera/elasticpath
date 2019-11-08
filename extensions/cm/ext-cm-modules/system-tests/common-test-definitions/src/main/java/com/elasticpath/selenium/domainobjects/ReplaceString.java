package com.elasticpath.selenium.domainobjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ReplaceString {

	private static final Logger LOGGER = Logger.getLogger(ReplaceString.class);

	/**
	 * Replaces string.
	 *
	 * @param filePath  the file path
	 * @param oldString the old string
	 * @param newString the new string
	 */
	public void replaceString(final String filePath, final String oldString, final String newString) {
		File fileToBeModified = new File(filePath);
		String oldContent = "";
		BufferedReader reader = null;
		FileWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(fileToBeModified));
			String line = reader.readLine();

			while (line != null) {
				oldContent = oldContent + line + System.lineSeparator();
				line = reader.readLine();
			}
			reader.close();
			String newContent = oldContent.replaceFirst(oldString, newString);
			writer = new FileWriter(fileToBeModified);
			writer.write(newContent);
		} catch (IOException e) {
			LOGGER.error(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

}

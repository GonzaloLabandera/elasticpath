/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

/** File utils class. */
public final class FileUtils {
	private FileUtils() {
		//noop
	}

	/**
	 * Read the file content.
	 *
	 * @param parentFolderPath the parent folder path
	 * @param fileName the file name
	 * @return the file content
	 * @throws IOException the exception
	 */
	public static String readFile(final String parentFolderPath, final String fileName) throws IOException {
		File fileToRead = new File(parentFolderPath, fileName);

		try (InputStream fileStream = Files.newInputStream(fileToRead.toPath())) {
			return IOUtils.toString(fileStream, UTF_8);
		}
	}

	/**
	 * Read the file content.
	 *
	 * @param filePath the absolute file path
	 * @return the file content
	 * @throws IOException the excepiton
	 */
	public static String readFile(final String filePath) throws IOException {
		File fileToRead = new File(filePath);

		try (InputStream fileStream = Files.newInputStream(fileToRead.toPath())) {
			return IOUtils.toString(fileStream, UTF_8);
		}
	}
}

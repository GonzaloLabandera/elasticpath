/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.batch.item.ItemWriter;

/**
 * Writer that fails a given number of times.
 */
public class FileWriter implements ItemWriter<String> {

	private final String id;

	/**
	 * Constructor.
	 *
	 * @param id id of the writer. Will be used to generate a file name.
	 */
	public FileWriter(final String id) {
		this.id = id;
	}

	@Override
	public void write(final List<? extends String> items) throws Exception {
		Files.write(Paths.get("target", id + "_batch_committed.txt"), items);
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.junit.Test;

import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.common.util.runner.AbstractPipedStreamRunner;

/**
 * Tests AbstractPipedStreamRunner.
 */
public class AbstractPipedStreamRunnerTest {

	/**
	 * Tests GetResultStream.
	 *
	 * @throws IOException for probable errors while reading.
	 */
	@Test
	public void testGetResultStream() throws IOException {
		final String content = "Some Content";
		
		final InputStream stream = new AbstractPipedStreamRunner() {
			@Override
			protected void runInternal(final OutputStream outputStream) {
			try {
				outputStream.write(content.getBytes());
			} catch (IOException e) {
				// Something went wrong
				throw new AssertionError("Writing to output stream failed.\n" + e);
			}
			}
		} .createResultStream();
		
		final BufferedReader lineStream = new BufferedReader(new InputStreamReader(stream));
		assertThat(lineStream.readLine()).isEqualTo(content);
		assertThat(lineStream.read()).isEqualTo(-1);
		lineStream.close();
	}
	
	/**
	 * Tests the get result stream with operation that throws exception. 
	 * @throws IOException for probable errors while reading.
	 */
	@Test
	public void testGetResultStreamWithException() throws IOException {
		final InputStream stream = new AbstractPipedStreamRunner() {
			@Override
			protected void runInternal(final OutputStream outputStream) {
				throw new ImportRuntimeException("Intentional exception");
				
			}
		} .createResultStream();
		
		assertThat(stream.read()).isEqualTo(-1);
	}
}

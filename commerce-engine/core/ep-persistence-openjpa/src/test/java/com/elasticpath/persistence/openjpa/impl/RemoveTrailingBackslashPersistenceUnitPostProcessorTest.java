/*
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.persistence.openjpa.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * Test that the RemoveTrailingBackslashPersistenceUnitPostProcessor behaves as expected.
 */
public class RemoveTrailingBackslashPersistenceUnitPostProcessorTest {

	private PersistenceUnitPostProcessor processor;

	/**
	 * Set up required for each test.
	 */
	@Before
	public void setUp() {
		processor = new RemoveTrailingBackslashPersistenceUnitPostProcessor();
	}

	/**
	 * Test that a trailing slash gets removed.
	 */
	@Test
	public void testSlashGetsRemoved() throws MalformedURLException {
		MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo();
		URL url = new URL("file:/root/somefile.jar/");
		pui.setPersistenceUnitRootUrl(url);
		processor.postProcessPersistenceUnitInfo(pui);
		assertThat(pui.getPersistenceUnitRootUrl().toString())
			.as("The url should have the slash removed")
			.isEqualTo("file:/root/somefile.jar");
	}

	/**
	 * Test that if there is no trailing slash nothing gets changed.
	 */
	@Test
	public void testNothingHappensWithNoSlash() throws MalformedURLException {
		MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo();
		URL url = new URL("file:/root/somefile.jar");
		pui.setPersistenceUnitRootUrl(url);
		processor.postProcessPersistenceUnitInfo(pui);
		assertThat(pui.getPersistenceUnitRootUrl())
			.as("The url not have changed if there was no slash")
			.isEqualTo(url);
	}
}

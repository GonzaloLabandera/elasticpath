/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.service.environment.EnvironmentInfoService;

/**
 * Test cases for <code>PropertiesDaoImpl</code>.
 */
public class PropertiesDaoImplTest {

	private static final String BUILD_INDEX_PROP_FILE = "buildIndex";

	private static final String LAST_BUILD_DATE_PROP = "LastBuildDate";

	private static final String OVERRIDDEN_LAST_BUILD_DATE_PROP_VALUE = "foo";

	private static final String LAST_CHECK_PROP = "lastCheck";

	private static final String PROPERTIES_DIRECTORY = "conf/resources";

	private static final String ROOT_PATH = "target/classes";

	private PropertiesDaoImpl propertiesDaoImpl;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock 
	private EnvironmentInfoService mockEnvironmentInfoService;

	/**
	 * Prepare for test.
	 *
	 * @throws Exception in case of error
	 */
	@Before
	public void setUp() throws Exception {
		propertiesDaoImpl = new PropertiesDaoImpl();
		propertiesDaoImpl.setPropertiesLocation("classpath*:resources");
		propertiesDaoImpl.setResourceLoader(new PathMatchingResourcePatternResolver());
		propertiesDaoImpl.setEnvironmentInfoService(mockEnvironmentInfoService);
	}

	/**
	 * Test for propertiesDaoImpl.loadProperties().
	 */
	@Test
	public void testLoadProperties() {
		context.checking(new Expectations() {
			{   
				allowing(mockEnvironmentInfoService).getConfigurationRootPath();
				will(returnValue(ROOT_PATH));
			}   
		}); 
		final Map<String, Properties> propMap = propertiesDaoImpl.loadProperties();
		assertNotNull(propMap);
		Properties countryProps = propMap.get("country");
		assertNotNull(countryProps);
		assertNotNull(countryProps.get("CA"));
	}

	/**
	 * Test that propertiesDaoImpl.loadProperties() correctly loads property files on the file system that override the classpath.
	 */
	@Test
	public void testLoadPropertiesCorrectlyLoadsFilesystemOverrides() {
		context.checking(new Expectations() {
			{   
				allowing(mockEnvironmentInfoService).getConfigurationRootPath();
				will(returnValue(ROOT_PATH));
			}   
		}); 
		propertiesDaoImpl.setStoredPropertiesLocation("target" + File.separator + "test-classes");

		final Map<String, Properties> propMap = propertiesDaoImpl.loadProperties();
		final Properties buildIndexProperties = propMap.get(BUILD_INDEX_PROP_FILE);

		assertNotNull(buildIndexProperties);
		assertEquals("The properties loaded were not overridden by properties stored on the filesystem under storedPropertiesLocation.",
				OVERRIDDEN_LAST_BUILD_DATE_PROP_VALUE, buildIndexProperties.get(LAST_BUILD_DATE_PROP));
	}

	/**
	 * Test loading a directory that doesn't exist.
	 */
	@Test(expected = EpPersistenceException.class)
	public void testLoadPropertiesWrongDir() {
		propertiesDaoImpl.setPropertiesLocation("wrongDir");
		propertiesDaoImpl.loadProperties();
	}

	/**
	 * Test for propertiesDaoImpl.getPropertiesFile().
	 */
	@Test
	public void testGetPropertiesFile() {
		context.checking(new Expectations() {
			{   
				allowing(mockEnvironmentInfoService).getConfigurationRootPath();
				will(returnValue("wrong-webinf-dir"));
			}   
		}); 
		// Changed on the filesystem
		propertiesDaoImpl.setStoredPropertiesLocation("target" + File.separator + "test-classes");
		final Properties buildIndexProp = propertiesDaoImpl.getPropertiesFile(BUILD_INDEX_PROP_FILE);
		assertNotNull(buildIndexProp);
		assertTrue(buildIndexProp.keySet().contains(LAST_BUILD_DATE_PROP));
		assertEquals("Value changed on the filesystem", "foo", buildIndexProp.getProperty(LAST_BUILD_DATE_PROP));
		// Read from jar
		final Properties buildIndexProp2 = propertiesDaoImpl.getPropertiesFile("catalogPromo.properties");
		assertNotNull(buildIndexProp);
		assertTrue(buildIndexProp2.keySet().contains(LAST_CHECK_PROP));
	}

	/**
	 * Test for propertiesDaoImpl.storePropertiesFile().
	 */
	@Test
	public void testStorePropertiesFile() {
		context.checking(new Expectations() {
			{   
				allowing(mockEnvironmentInfoService).getConfigurationRootPath();
				will(returnValue(ROOT_PATH));
			}   
		}); 
		final Date currentDate = new Date();
		final Properties buildIndexProp = propertiesDaoImpl.getPropertiesFile(BUILD_INDEX_PROP_FILE);
		buildIndexProp.setProperty(LAST_BUILD_DATE_PROP, currentDate.toString());
		propertiesDaoImpl.storePropertiesFile(buildIndexProp, BUILD_INDEX_PROP_FILE);
		final Properties newBuildIndexProp = propertiesDaoImpl.getPropertiesFile(BUILD_INDEX_PROP_FILE);
		assertEquals(buildIndexProp.getProperty(LAST_BUILD_DATE_PROP), newBuildIndexProp.getProperty(LAST_BUILD_DATE_PROP));

		// clean up, fall back changes to original properties file
		final StringBuilder fileName = new StringBuilder();
		fileName.append(ROOT_PATH).append(File.separator).append(PROPERTIES_DIRECTORY).append(File.separator)
				.append(BUILD_INDEX_PROP_FILE).append(".properties");
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(fileName.toString());
			new PrintStream(fout).print("Rebuild=false\nLastBuildDate=");
			fout.close();
		} catch (IOException e) {
			System.out.print("error in fallback changes in testStorePropertiesFile"); // NOPMD
		}
	}
}

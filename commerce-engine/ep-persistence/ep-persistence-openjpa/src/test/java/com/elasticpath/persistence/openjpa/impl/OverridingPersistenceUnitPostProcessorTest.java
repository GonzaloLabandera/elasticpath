/*
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.persistence.openjpa.impl;

import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.PERSISTENCE_UNIT_NAME;
import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.givenAPersistenceUnit;
import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.givenAPersistenceUnitWithJarFileUrls;
import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.givenAPersistenceUnitWithManagedClassNames;
import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.givenAPersistenceUnitWithMappingFiles;
import static com.elasticpath.persistence.openjpa.impl.PostProcessorTestHelper.whenTheProcessorIsCalled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * OverridingPersistenceUnitPostProcessorTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class OverridingPersistenceUnitPostProcessorTest {

	@Test
	public void testProcessorHandlesNullCollections() {
		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludedMappingFiles(null);
		processor.setExcludedManagedClassNames(null);
		processor.setExcludedJarFileUrls(null);

		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnit();

		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
	}

	/**
	 * Verify that the processor removes mapping files from the persistence unit.
	 */
	@Test
	public void testRemoveMappingFiles() {
		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludedMappingFiles(ImmutableList.of("file2.orm"));

		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnitWithMappingFiles("file1.orm", "file2.orm", "file3.orm");
		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.getMappingFileNames())
			.contains("file1.orm", "file3.orm")
			.doesNotContain("file2.orm");
	}

	/**
	 * Verify that the processor removes managed classes from the persistence unit.
	 */
	@Test
	public void testRemoveManagedClasses() {
		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludedManagedClassNames(ImmutableList.of("SomeBaseClassImpl"));

		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnitWithManagedClassNames("SomeBaseClassImpl", "AnotherClassImpl");
		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.getManagedClassNames())
			.contains("AnotherClassImpl")
			.doesNotContain("SomeBaseClassImpl");
	}

	/**
	 * Verify that the processor removes Jar File URLS from the persistence unit.
	 *
	 * @throws MalformedURLException in case of malformed URLS
	 */
	@Test
	public void testRemoveJarFileUrls() throws MalformedURLException {
		URL jarFile1 = new URL("file:///jar1");
		URL jarFile2 = new URL("file:///jar2");

		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludedJarFileUrls(ImmutableList.of(jarFile2));

		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnitWithJarFileUrls(jarFile1, jarFile2);
		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.getJarFileUrls())
			.contains(jarFile1)
			.doesNotContain(jarFile2);
	}

	/**
	 * Verify that the processor adds property overrides to the persistence unit.
	 */
	@Test
	public void testAddPropertyOverrides() {
		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnitWithProperties(ImmutableMap.of("openjpa.Log", "log4j"));

		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setPropertyOverrides(ImmutableMap.of("openjpa.DataCacheTimeout", "7000"));

		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		ImmutableMap<String, String> finalProperties = Maps.fromProperties(persistenceUnitInfo.getProperties());
		assertThat(finalProperties).contains(entry("openjpa.Log", "log4j"), entry("openjpa.DataCacheTimeout", "7000"));
	}

	/**
	 * Verify that the processor sets the exclude unlisted classes to true on the persistence unit.
	 */
	@Test
	public void testExcludeUnlistedClassesTrue() {
		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnit();

		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludeUnlistedClasses(true);

		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.excludeUnlistedClasses())
			.as("Exclude unlisted classes should be true")
			.isTrue();
	}

	/**
	 * Verify that the processor sets the exclude unlisted classes to false on the persistence unit.
	 */
	@Test
	public void testExcludeUnlistedClassesFalse() {
		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnit();

		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludeUnlistedClasses(false);

		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.excludeUnlistedClasses())
			.as("Exclude unlisted classes should be false")
			.isFalse();
	}

	/**
	 * Verify that the processor handles a null value for exclude unlisted classes.
	 */
	@Test
	public void testExcludeUnlistedClassesFalseWhenNull() {
		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnit();

		OverridingPersistenceUnitPostProcessor processor = givenAnOverridingProcessor();
		processor.setExcludeUnlistedClasses(null);

		whenTheProcessorIsCalled(processor, persistenceUnitInfo);
		assertThat(persistenceUnitInfo.excludeUnlistedClasses())
			.as("The default value of exclude unlisted classes should be false")
			.isFalse();
	}

	// Methods to set up expectations

	private OverridingPersistenceUnitPostProcessor givenAnOverridingProcessor() {
		OverridingPersistenceUnitPostProcessor processor = new OverridingPersistenceUnitPostProcessor();
		processor.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
		return processor;
	}

	private MutablePersistenceUnitInfo givenAPersistenceUnitWithProperties(final Map<String, String> propertyMap) {
		MutablePersistenceUnitInfo persistenceUnitInfo = givenAPersistenceUnit();
		Properties properties = new Properties();
		properties.putAll(propertyMap);

		persistenceUnitInfo.setProperties(properties);
		return persistenceUnitInfo;
	}


}

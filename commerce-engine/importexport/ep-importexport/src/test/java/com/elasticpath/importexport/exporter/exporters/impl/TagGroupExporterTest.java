/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.collections.CollectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.ExporterConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.domain.impl.TagGroupImpl;
import com.elasticpath.tags.service.TagGroupService;

/**
 * Tests <code>TagGroupExporter</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagGroupExporterTest {

	@InjectMocks
	private TagGroupExporter tagGroupExporter;

	@Mock
	private ImportExportSearcher importExportSearcher;

	@Mock
	private TagGroupService tagGroupService;

	@Mock
	private DomainAdapter<TagGroup, TagGroupDTO> tagGroupAdapter;

	private ExportContext exportContext;

	private static final String GUID1 = "GUID1";
	private static final String GUID2 = "GUID2";
	private static final String GUID3 = "GUID3";
	private static final String GUID_DNE = "GUID_DNE";
	private static final int THREE = 3;

	/**
	 * All guids found during initialization should be exportable.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testInitialize() throws ConfigurationException {

		final TagGroup tagGroup1 = new TagGroupImpl();
		tagGroup1.setGuid(GUID1);
		final TagGroup tagGroup2 = new TagGroupImpl();
		tagGroup2.setGuid(GUID2);
		final TagGroup tagGroup3 = new TagGroupImpl();
		tagGroup3.setGuid(GUID3);
		final List<TagGroup> tagGroupList = Arrays.asList(tagGroup1, tagGroup2, tagGroup3);
		final List<String> foundGuids = Arrays.asList(GUID1, GUID2, GUID3);

		when(tagGroupService.getTagGroups()).thenReturn(tagGroupList);

		exportContext = new ExportContext(new ExportConfiguration(), new SearchConfiguration());
		tagGroupExporter.initialize(exportContext);

		assertThat(CollectionUtils.isEqualCollection(foundGuids, tagGroupExporter.getListExportableIDs()))
				.isTrue();
	}

	/**
	 * Searching for a guids should return all guids regardless whether they were found during initialization.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testFindByGuid() throws ConfigurationException {
		testInitialize();

		final TagGroup tagGroup1 = mock(TagGroup.class);
		final TagGroup tagGroup2 = mock(TagGroup.class);

		final List<String> guidList = Arrays.asList(GUID1, GUID_DNE);
		final List<TagGroup> tagGroupList = Arrays.asList(tagGroup1, tagGroup2);

		when(tagGroup1.getGuid()).thenReturn(GUID1);
		when(tagGroup2.getGuid()).thenReturn(GUID_DNE);
		when(tagGroupService.getTagGroups()).thenReturn(tagGroupList);

		List<TagGroup> results = tagGroupExporter.findByIDs(guidList);

		assertThat(CollectionUtils.isEqualCollection(results, tagGroupList))
				.as("Missing returned tag group")
				.isTrue();

		assertThat(results)
				.as("Extra tag group returned")
				.hasSize(2);
	}


	/**
	 * Test method for {@link TagGroupExporter#getListExportableIDs()}.
	 *
	 * @throws ConfigurationException not expected exception.
	 */
	@Test
	public void testGetListExportableIDs() throws ConfigurationException {
		ExportConfiguration exportConfiguration = new ExportConfiguration();
		SearchConfiguration searchConfiguration = new SearchConfiguration();

		tagGroupExporter.initialize(new ExportContext(exportConfiguration, searchConfiguration));
		tagGroupExporter.getContext()
				.setDependencyRegistry(new DependencyRegistry(Arrays.asList(new Class<?>[]{TagGroup.class})));

		ExporterConfiguration exporterConfiguration = new ExporterConfiguration();
		exportConfiguration.setExporterConfiguration(exporterConfiguration);

		tagGroupExporter.getContext().getDependencyRegistry().addGuidDependency(TagGroup.class, GUID1);
		tagGroupExporter.getContext().getDependencyRegistry().addGuidDependency(TagGroup.class, GUID2);
		tagGroupExporter.getContext().getDependencyRegistry().addGuidDependency(TagGroup.class, GUID3);

		final List<String> listExportableIDs = tagGroupExporter.getListExportableIDs();

		assertThat(CollectionUtils.isEqualCollection(listExportableIDs, Arrays.asList(GUID1, GUID2, GUID3)))
				.as("Missing returned tag group")
				.isTrue();

		assertThat(listExportableIDs)
				.as("Extra tag group returned")
				.hasSize(THREE);
	}

	/**
	 * Ensures the proper {@link JobType} is returned.
	 */
	@Test
	public void testJobType() {
		assertThat(JobType.TAGGROUP)
				.as("Incorrect job type returned.")
				.isEqualTo(tagGroupExporter.getJobType());
	}

	/**
	 * Tests getDependentClasses.
	 */
	@Test
	public void testDependantClasses() {
		assertThat(new Class<?>[]{TagGroup.class})
				.as("Incorrect dependent classes returned.")
				.isEqualTo(tagGroupExporter.getDependentClasses());
	}

	/**
	 * Tests getDomainAdapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(tagGroupAdapter)
				.as("Incorrect domain adapter returned.")
				.isEqualTo(tagGroupExporter.getDomainAdapter());
	}

	/**
	 * Tests getImportExportSearcher.
	 */
	@Test
	public void testImportExportSearcher() {
		assertThat(importExportSearcher)
				.as("Incorrect export searcher returned.")
				.isEqualTo(tagGroupExporter.getImportExportSearcher());
	}
}

/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.adapters.tag.TagGroupAdapter;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.service.TagGroupService;

/**
 * Test for <code>TagGroupImporter</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagGroupImporterTest {

	private static final String TAG_GROUP_GUID = "TAG_GROUP_GUID";
	private static final String LANGUAGE = "en";
	private static final String TAG_GROUP_DISPLAY_NAME = "TAG_GROUP_DISPLAY_NAME";

	private TagGroupImporter tagGroupImporter;

	private final DomainAdapter<TagGroup, TagGroupDTO> domainAdapter = new TagGroupAdapter();

	@Mock
	private TagGroupService mockTagGroupService;

	private SavingStrategy<TagGroup, TagGroupDTO> mockSavingStrategy;

	/**
	 * SetUps the test.
	 */
	@Before
	public void setUp() {
		tagGroupImporter = new TagGroupImporter();
		tagGroupImporter.setStatusHolder(new ImportStatusHolder());
		tagGroupImporter.setTagGroupService(mockTagGroupService);
		tagGroupImporter.setTagGroupAdapter(domainAdapter);

		mockSavingStrategy = AbstractSavingStrategy.createStrategy(ImportStrategyType.INSERT_OR_UPDATE, new SavingManager<TagGroup>() {
			@Override
			public void save(final TagGroup persistable) {
				// do nothing
			}

			@Override
			public TagGroup update(final TagGroup persistable) {
				return null;
			}
		});
	}

	/**
	 * Check an import with non-initialized importer.
	 */
	public void testExecuteNonInitializedImport() {
		assertThatThrownBy(() -> tagGroupImporter.executeImport(createTagGroupDTO()))
				.isInstanceOf(ImportRuntimeException.class)
				.hasMessageStartingWith("IE-30501");
	}

	/**
	 * Check an import of tag groups.
	 */
	@Test
	public void testExecuteImport() {
		final ImportConfiguration importConfiguration = new ImportConfiguration();
		importConfiguration.setImporterConfigurationMap(new HashMap<>());

		final TagGroup mockTagGroup = mock(TagGroup.class);
		when(mockTagGroupService.findByGuid(TAG_GROUP_GUID)).thenReturn(mockTagGroup);
		when(mockTagGroup.getLocalizedProperties()).thenReturn(mock(LocalizedProperties.class));

		tagGroupImporter.initialize(new ImportContext(importConfiguration), mockSavingStrategy);
		tagGroupImporter.executeImport(createTagGroupDTO());

		assertThat(TagGroupDTO.ROOT_ELEMENT)
				.isEqualTo(tagGroupImporter.getImportedObjectName());
		assertThat(tagGroupImporter.getSavingStrategy())
				.isNotNull();
	}

	/**
	 * Test method for {@link TagGroupImporter#findPersistentObject(TagGroupDTO)}.
	 */
	@Test
	public void testFindPersistentObjectTagGroupDTO() {
		final TagGroup tagGroup = mock(TagGroup.class);

		when(mockTagGroupService.findByGuid(TAG_GROUP_GUID)).thenReturn(tagGroup);

		assertThat(tagGroup)
				.isEqualTo(tagGroupImporter.findPersistentObject(createTagGroupDTO()));
	}


	/**
	 * Test method for {@link TagGroupImporter#getDomainAdapter()}.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(domainAdapter)
				.isEqualTo(tagGroupImporter.getDomainAdapter());
	}

	/**
	 * Test method for
	 * {@link TagGroupImporter
	 * #getDtoGuid{@link com.elasticpath.importexport.common.dto.tag.TagGroupDTO})}.
	 */
	@Test
	public void testGetDtoGuidTagGroupDTO() {
		final TagGroupDTO dto = createTagGroupDTO();

		assertThat(TAG_GROUP_GUID)
				.isEqualTo(tagGroupImporter.getDtoGuid(dto));
	}

	/**
	 * Test method for {@link TagGroupImporter#getImportedObjectName()}.
	 */
	@Test
	public void testGetImportedObjectName() {
		assertThat(TagGroupDTO.ROOT_ELEMENT)
				.isEqualTo(tagGroupImporter.getImportedObjectName());
	}

	/**
	 * The import classes should at least contain the DTO class we are operating on.
	 */
	@Test
	public void testDtoClass() {
		assertThat(TagGroupDTO.class)
				.isEqualTo(tagGroupImporter.getDtoClass());
	}

	/**
	 * The auxiliary JAXB class list must not be null (can be empty).
	 */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertThat(tagGroupImporter.getAuxiliaryJaxbClasses())
				.isNotNull();
	}

	private TagGroupDTO createTagGroupDTO() {
		final TagGroupDTO tagGroupDTO = domainAdapter.createDtoObject();
		tagGroupDTO.setCode(TAG_GROUP_GUID);
		tagGroupDTO.setNameValues(Collections.singletonList(new DisplayValue(LANGUAGE, TAG_GROUP_DISPLAY_NAME)));

		return tagGroupDTO;
	}
}

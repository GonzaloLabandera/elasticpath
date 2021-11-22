/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.service.TagGroupService;

/**
 * Exporter for {@link TagGroup}.
 */
public class TagGroupExporter extends AbstractExporterImpl<TagGroup, TagGroupDTO, String> {

	private static final Logger LOG = LogManager.getLogger(TagGroupExporter.class);

	private List<String> tagGroupGuids;

	private ImportExportSearcher importExportSearcher;

	private TagGroupService tagGroupService;

	private DomainAdapter<TagGroup, TagGroupDTO> tagGroupAdapter;
	private List<TagGroup> allTagGroups;

	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		List<TagGroup> tagGroups = tagGroupService.getTagGroups();
		tagGroupGuids = new ArrayList<>(tagGroups.size());
		for (TagGroup tagGroup : tagGroups) {
			tagGroupGuids.add(tagGroup.getGuid());
		}

		LOG.info("The list for " + tagGroupGuids.size() + " tagGroup(s) are retrieved from the database.");
	}

	@Override
	protected Class<? extends TagGroupDTO> getDtoClass() {
		return TagGroupDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(TagGroup.class)) {
			tagGroupGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(TagGroup.class));
		}

		return tagGroupGuids;
	}

	@Override
	protected List<TagGroup> findByIDs(final List<String> subList) {
		List<TagGroup> tagGroups = new ArrayList<>(subList.size());
		for (String guid : subList) {
			findTagGroup(guid).ifPresent(tagGroups::add);
		}
		return tagGroups;
	}

	private Optional<TagGroup> findTagGroup(final String guid) {
		if (allTagGroups == null) {
			allTagGroups = tagGroupService.getTagGroups();
		}
		return allTagGroups.stream().filter(tagGroup -> tagGroup.getGuid().equals(guid)).findFirst();
	}

	@Override
	public JobType getJobType() {
		return JobType.TAGGROUP;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[] { TagGroup.class };
	}

	@Override
	protected DomainAdapter<TagGroup, TagGroupDTO> getDomainAdapter() {
		return tagGroupAdapter;
	}

	public void setTagGroupAdapter(final DomainAdapter<TagGroup, TagGroupDTO> tagGroupAdapter) {
		this.tagGroupAdapter = tagGroupAdapter;
	}

	public ImportExportSearcher getImportExportSearcher() {
		return importExportSearcher;
	}

	public void setImportExportSearcher(final ImportExportSearcher importExportSearcher) {
		this.importExportSearcher = importExportSearcher;
	}

	public TagGroupService getTagGroupService() {
		return tagGroupService;
	}

	public void setTagGroupService(final TagGroupService tagGroupService) {
		this.tagGroupService = tagGroupService;
	}
}

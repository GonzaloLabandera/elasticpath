/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.service.TagGroupService;

/**
 * DTO for {@link TagGroup}.
 */
public class TagGroupImporter extends AbstractImporterImpl<TagGroup, TagGroupDTO> {

	private DomainAdapter<TagGroup, TagGroupDTO> tagGroupAdapter;

	private TagGroupService tagGroupService;

	@Override
	protected String getDtoGuid(final TagGroupDTO dto) {
		return dto.getCode();
	}

	@Override
	protected DomainAdapter<TagGroup, TagGroupDTO> getDomainAdapter() {
		return tagGroupAdapter;
	}

	@Override
	protected TagGroup findPersistentObject(final TagGroupDTO dto) {
		return tagGroupService.findByGuid(dto.getCode());
	}

	@Override
	protected void setImportStatus(final TagGroupDTO object) {
		getStatusHolder().setImportStatus("(" + object.getCode() + ")");
	}

	@Override
	public Class<? extends TagGroupDTO> getDtoClass() {
		return TagGroupDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return TagGroupDTO.ROOT_ELEMENT;
	}

	public void setTagGroupAdapter(final DomainAdapter<TagGroup, TagGroupDTO> tagGroupAdapter) {
		this.tagGroupAdapter = tagGroupAdapter;
	}

	public void setTagGroupService(final TagGroupService tagGroupService) {
		this.tagGroupService = tagGroupService;
	}
}

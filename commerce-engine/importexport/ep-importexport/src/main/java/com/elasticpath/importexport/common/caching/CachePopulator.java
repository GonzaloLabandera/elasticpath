/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching;

import java.util.List;

import com.elasticpath.common.dto.Dto;

/**
 * Cache populator.
 *
 * @param <DTO> the dto type
 */
public interface CachePopulator<DTO extends Dto> {

	/**
	 * Populate caches for the given dtos.
	 *
	 * @param dtos the dtos
	 */
	void populate(List<DTO> dtos);
}

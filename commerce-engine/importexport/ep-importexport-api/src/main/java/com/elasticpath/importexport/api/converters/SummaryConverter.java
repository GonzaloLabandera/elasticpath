/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.converters;

import com.elasticpath.importexport.api.models.SummaryDto;
import com.elasticpath.importexport.common.summary.Summary;

/**
 * Converter class for converting a Summary object to a SummaryDto object.
 */
public interface SummaryConverter {

	/**
	 * Convert passed Summary object to SummaryDto.
	 * @param summary the Summary object
	 * @return the SummaryDto object
	 */
	SummaryDto convert(Summary summary);
}

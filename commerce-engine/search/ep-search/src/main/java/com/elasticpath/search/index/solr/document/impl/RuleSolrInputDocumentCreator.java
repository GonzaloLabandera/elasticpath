/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.document.impl;

import org.apache.solr.common.SolrInputDocument;

import com.elasticpath.domain.rules.Rule;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Creates a {@link SolrInputDocument} for an {@link Rule}.
 */
public class RuleSolrInputDocumentCreator extends AbstractDocumentCreatingTask<Rule> {

	/**
	 * Creates a {@link SolrInputDocument} based on the {@link Rule}.
	 * 
	 * @return the {@link SolrInputDocument}.
	 */
	@Override
	public SolrInputDocument createDocument() {
		if (getEntity() == null) {
			return null;
		}

		final SolrInputDocument document = new SolrInputDocument();
		addFieldToDocument(document, SolrIndexConstants.OBJECT_UID, getAnalyzer().analyze(String.valueOf(getEntity().getUidPk())));
		addFieldToDocument(document, SolrIndexConstants.PROMOTION_NAME, getAnalyzer().analyze(getEntity().getName()));
		addFieldToDocument(document, SolrIndexConstants.PROMOTION_STATE, getAnalyzer().analyze(String.valueOf(getEntity().isEnabled())));

		if (getEntity().getRuleSet() != null) {
			addFieldToDocument(document, SolrIndexConstants.PROMOTION_RULESET_UID, getAnalyzer().analyze(getEntity().getRuleSet().getUidPk()));
			addFieldToDocument(document, SolrIndexConstants.PROMOTION_RULESET_NAME, getAnalyzer().analyze(getEntity().getRuleSet().getName()));
		}

		addFieldToDocument(document, SolrIndexConstants.START_DATE, getAnalyzer().analyze(getEntity().getEffectiveStartDate()));
		addFieldToDocument(document, SolrIndexConstants.END_DATE, getAnalyzer().analyze(getEntity().getEffectiveEndDate()));

		if (getEntity().getCatalog() == null) {
			addFieldToDocument(document, SolrIndexConstants.STORE_CODE, getAnalyzer().analyze(
					getAnalyzer().analyze(getEntity().getStore().getCode())));
		} else {
			addFieldToDocument(document, SolrIndexConstants.CATALOG_UID, getAnalyzer().analyze(
					getAnalyzer().analyze(getEntity().getCatalog().getUidPk())));
			addFieldToDocument(document, SolrIndexConstants.CATALOG_CODE, getAnalyzer().analyze(
					getAnalyzer().analyze(getEntity().getCatalog().getCode())));
		}

		return document;
	}
}
